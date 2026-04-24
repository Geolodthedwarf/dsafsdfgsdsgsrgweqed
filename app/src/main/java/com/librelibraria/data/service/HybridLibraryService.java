package com.librelibraria.data.service;

import androidx.annotation.NonNull;

import com.librelibraria.data.database.AppDatabase;
import com.librelibraria.data.model.Book;
import com.librelibraria.data.model.Borrower;
import com.librelibraria.data.model.Loan;
import com.librelibraria.data.model.LoanStatus;
import com.librelibraria.data.repository.BookRepository;
import com.librelibraria.data.repository.LoanRepository;
import com.librelibraria.data.storage.FileStorageManager;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Hybrid write service: updates Room + file storage consistently.
 *
 * UI reads should prefer Room; file storage is used for backup/sync/export.
 */
public class HybridLibraryService {

    private final AppDatabase db;
    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;
    private final FileStorageManager storageManager;

    public HybridLibraryService(
            @NonNull AppDatabase db,
            @NonNull BookRepository bookRepository,
            @NonNull LoanRepository loanRepository,
            @NonNull FileStorageManager storageManager
    ) {
        this.db = db;
        this.bookRepository = bookRepository;
        this.loanRepository = loanRepository;
        this.storageManager = storageManager;
    }

    public Single<Long> saveBook(@NonNull Book book) {
        boolean isUpdate = book.getId() > 0;
        if (isUpdate) {
            return bookRepository.updateBook(book)
                    .andThen(Single.just(book.getId()))
                    .doOnSuccess(id -> storageManager.saveBook(book))
                    .subscribeOn(Schedulers.io());
        }

        return bookRepository.insert(book)
                .doOnSuccess(id -> {
                    book.setId(id);
                    storageManager.saveBook(book);
                })
                .subscribeOn(Schedulers.io());
    }

    public Completable deleteBook(long bookId) {
        return Completable.fromAction(() -> {
                    // Room cascades delete to loans + diary_entries due to FK constraints.
                    db.runInTransaction(() -> {
                        bookRepository.deleteBookById(bookId).blockingAwait();
                    });
                })
                .andThen(Completable.fromAction(() -> {
                    storageManager.deleteBook(String.valueOf(bookId));
                    storageManager.deleteLoansForBook(bookId);
                }))
                .subscribeOn(Schedulers.io());
    }

    public Single<Long> saveBorrower(@NonNull Borrower borrower) {
        boolean isUpdate = borrower.getId() > 0;
        if (isUpdate) {
            return loanRepository.updateBorrower(borrower)
                    .andThen(Single.just(borrower.getId()))
                    .doOnSuccess(id -> storageManager.saveBorrower(borrower))
                    .subscribeOn(Schedulers.io());
        }

        return loanRepository.insertBorrower(borrower)
                .doOnSuccess(id -> {
                    borrower.setId(id);
                    storageManager.saveBorrower(borrower);
                })
                .subscribeOn(Schedulers.io());
    }

    public Completable deleteBorrower(long borrowerId) {
        return loanRepository.getBorrowerById(borrowerId)
                .flatMapCompletable(borrower -> loanRepository.deleteBorrower(borrower))
                .andThen(Completable.fromAction(() -> storageManager.deleteBorrowerById(borrowerId)))
                .subscribeOn(Schedulers.io());
    }

    public Single<Long> lendBook(long bookId, @NonNull Borrower borrower, long dueDate) {
        return lendBook(bookId, borrower.getId(), borrower.getName(), dueDate);
    }

    public Single<Long> lendBook(long bookId, Long borrowerId, String borrowerName, long dueDate) {
        return bookRepository.getBookById(bookId)
                .flatMap(book -> {
                    if (book == null) return Single.error(new IllegalStateException("Book not found"));
                    if (book.getAvailableCopies() <= 0) return Single.error(new IllegalStateException("No available copies"));

                    Loan loan = new Loan();
                    loan.setBookId(bookId);
                    loan.setBorrowerId(borrowerId);
                    loan.setBorrowerName(borrowerName);
                    loan.setDueDate(dueDate > 0 ? dueDate : loan.getDueDate());
                    loan.setStatus(LoanStatus.ACTIVE);

                    return Single.fromCallable(() -> {
                                final long[] loanIdHolder = {-1};
                                db.runInTransaction(() -> {
                                    long loanId = loanRepository.createLoan(loan).blockingGet();
                                    loan.setId(loanId);
                                    loanIdHolder[0] = loanId;

                                    int newAvailable = Math.max(0, book.getAvailableCopies() - 1);
                                    bookRepository.updateAvailableCopies(bookId, newAvailable).blockingAwait();
                                });

                                // File storage mirror (best-effort)
                                storageManager.saveLoan(loan);
                                storageManager.saveBook(book);

                                return loanIdHolder[0];
                            })
                            .subscribeOn(Schedulers.io());
                });
    }

    public Completable returnBook(long loanId) {
        return loanRepository.getLoanById(loanId)
                .flatMapCompletable(loan -> {
                    if (loan == null) return Completable.error(new IllegalStateException("Loan not found"));
                    if (loan.getStatus() != LoanStatus.ACTIVE) return Completable.complete();

                    return bookRepository.getBookById(loan.getBookId())
                            .flatMapCompletable(book -> Completable.fromAction(() -> {
                                        db.runInTransaction(() -> {
                                            loan.setStatus(LoanStatus.RETURNED);
                                            loan.setReturnDate(System.currentTimeMillis());
                                            loanRepository.updateLoan(loan).blockingAwait();

                                            if (book != null) {
                                                int newAvailable = book.getAvailableCopies() + 1;
                                                bookRepository.updateAvailableCopies(book.getId(), newAvailable).blockingAwait();
                                            }
                                        });

                                        // File storage mirror (best-effort)
                                        storageManager.saveLoan(loan);
                                        if (book != null) storageManager.saveBook(book);
                                    })
                                    .subscribeOn(Schedulers.io()));
                })
                .subscribeOn(Schedulers.io());
    }
}

