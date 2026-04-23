package com.librelibraria.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.librelibraria.LibreLibrariaApp;
import com.librelibraria.data.model.Book;
import com.librelibraria.data.model.Borrower;
import com.librelibraria.data.model.Loan;
import com.librelibraria.data.repository.BookRepository;
import com.librelibraria.data.repository.LoanRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * ViewModel for Lending screen.
 */
public class LendingViewModel extends AndroidViewModel {

    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;
    private final CompositeDisposable disposables;

    private final MutableLiveData<List<Loan>> activeLoans = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Book>> availableBooks = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Borrower>> borrowers = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> activeLoansCount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> overdueLoansCount = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public LendingViewModel(@NonNull Application application) {
        super(application);

        LibreLibrariaApp app = (LibreLibrariaApp) application;
        bookRepository = app.getBookRepository();
        loanRepository = app.getLoanRepository();
        disposables = new CompositeDisposable();

        loadData();
    }

    public LiveData<List<Loan>> getActiveLoans() {
        return activeLoans;
    }

    public LiveData<List<Book>> getAvailableBooks() {
        return availableBooks;
    }

    public LiveData<List<Borrower>> getBorrowers() {
        return borrowers;
    }

    public LiveData<Integer> getActiveLoansCount() {
        return activeLoansCount;
    }

    public LiveData<Integer> getOverdueLoansCount() {
        return overdueLoansCount;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void refresh() {
        loadData();
    }

    private void loadData() {
        isLoading.setValue(true);

        // Load active loans
        disposables.add(
            loanRepository.getActiveLoans()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    loans -> {
                        activeLoans.setValue(loans);
                        isLoading.setValue(false);
                    },
                    error -> isLoading.setValue(false)
                )
        );

        // Load counts
        disposables.add(
            loanRepository.getActiveLoansCount()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    count -> activeLoansCount.setValue(count),
                    error -> {}
                )
        );

        disposables.add(
            loanRepository.getOverdueLoansCount()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    count -> overdueLoansCount.setValue(count),
                    error -> {}
                )
        );

        // Load available books
        disposables.add(
            bookRepository.getAvailableBooks()
                .firstOrError()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    books -> availableBooks.setValue(books),
                    error -> {}
                )
        );

        // Load borrowers
        disposables.add(
            loanRepository.getAllBorrowers()
                .firstOrError()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    borrowerList -> borrowers.setValue(borrowerList),
                    error -> {}
                )
        );
    }

    public void lendBook(long bookId, Long borrowerId, String borrowerName, long dueDate) {
        Loan loan = new Loan();
        loan.setBookId(bookId);
        loan.setBorrowerId(borrowerId);
        loan.setBorrowerName(borrowerName);
        loan.setDueDate(dueDate);
        loan.setStatus(com.librelibraria.data.model.LoanStatus.ACTIVE);

        disposables.add(
            loanRepository.createLoan(loan)
                .flatMapCompletable(loanId -> {
                    // Update available copies
                    return bookRepository.updateBook(bookId, b -> {
                        b.setAvailableCopies(b.getAvailableCopies() - 1);
                        return bookRepository.updateBook(b);
                    }).ignoreElement();
                })
                .flatMapCompletable(v -> {
                    // Increment borrower count if exists
                    if (borrowerId != null) {
                        return loanRepository.incrementBorrowerCount(borrowerId);
                    }
                    return io.reactivex.rxjava3.core.Completable.complete();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> refresh(),
                    error -> {}
                )
        );
    }

    public void returnBook(long loanId, String condition) {
        disposables.add(
            loanRepository.getLoanById(loanId)
                .flatMapCompletable(loan -> {
                    double lateFee = loan.calculateLateFee();

                    // Update loan status
                    return loanRepository.returnBook(loanId, lateFee)
                            .andThen(bookRepository.updateBook(loan.getBookId(), book -> {
                                book.setAvailableCopies(book.getAvailableCopies() + 1);
                                return bookRepository.updateBook(book);
                            }).ignoreElement());
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> refresh(),
                    error -> {}
                )
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
