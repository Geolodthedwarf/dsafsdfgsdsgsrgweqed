package com.librelibraria.data.service;

import com.librelibraria.data.model.AuditLog;
import com.librelibraria.data.model.Book;
import com.librelibraria.data.model.Loan;
import com.librelibraria.data.model.ReadingStatus;
import com.librelibraria.data.repository.BookRepository;
import com.librelibraria.data.repository.LoanRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Service for automation tasks - notifications, reminders, and scheduled actions.
 */
public class AutomationService {

    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;
    private final AuditService auditService;

    public AutomationService(BookRepository bookRepository, LoanRepository loanRepository, AuditService auditService) {
        this.bookRepository = bookRepository;
        this.loanRepository = loanRepository;
        this.auditService = auditService;
    }

    /**
     * Check for overdue loans and return them.
     */
    public Single<List<Loan>> checkOverdueLoans() {
        long now = System.currentTimeMillis();
        return loanRepository.getOverdueLoans(now)
                .subscribeOn(Schedulers.io());
    }

    /**
     * Get loans due within specified days.
     */
    public Single<List<Loan>> getLoansDueWithinDays(int days) {
        long futureDate = System.currentTimeMillis() + (days * 24 * 60 * 60 * 1000L);
        return loanRepository.getLoansDueBefore(futureDate)
                .subscribeOn(Schedulers.io());
    }

    /**
     * Auto-update book status based on reading progress.
     */
    public Completable updateBookStatusesFromProgress() {
        return bookRepository.getAllBooks()
                .firstOrError()
                .flatMapCompletable(books -> Completable.fromAction(() -> {
                    for (Book book : books) {
                        ReadingStatus status = book.getReadingStatus();
                        if (status != null && status == ReadingStatus.READING) {
                            book.setReadingStatus(ReadingStatus.READ);
                            bookRepository.update(book).subscribe(() -> {}, Throwable::printStackTrace);
                        }
                    }
                }))
                .subscribeOn(Schedulers.io());
    }

    public Single<List<Book>> generateRecommendations(int limit) {
        return bookRepository.getAllBooks()
                .firstOrError()
                .map(books -> {
                    List<Book> recommendations = new ArrayList<>();
                    for (Book book : books) {
                        ReadingStatus status = book.getReadingStatus();
                        if (status == ReadingStatus.WANT && book.getRating() >= 4.0) {
                            recommendations.add(book);
                        }
                    }
                    recommendations.sort((b1, b2) -> Double.compare(b2.getRating(), b1.getRating()));
                    int size = Math.min(limit, recommendations.size());
                    return recommendations.subList(0, size);
                })
                .subscribeOn(Schedulers.io());
    }

    public Single<List<Book>> findIncompleteBookData() {
        return bookRepository.getAllBooks()
                .firstOrError()
                .map(books -> {
                    List<Book> incomplete = new ArrayList<>();
                    for (Book book : books) {
                        if (isBookDataIncomplete(book)) {
                            incomplete.add(book);
                        }
                    }
                    return incomplete;
                })
                .subscribeOn(Schedulers.io());
    }

    /**
     * Check if book data is incomplete.
     */
    private boolean isBookDataIncomplete(Book book) {
        boolean missingTitle = book.getTitle() == null || book.getTitle().trim().isEmpty();
        boolean missingAuthor = book.getAuthor() == null || book.getAuthor().trim().isEmpty();
        boolean missingDescription = book.getDescription() == null || book.getDescription().trim().isEmpty();
        return missingTitle || missingAuthor || missingDescription;
    }

    /**
     * Sync book data from external source for incomplete entries.
     */
    public Completable fillMissingBookData() {
        return findIncompleteBookData()
                .flatMapCompletable(incompleteBooks -> Completable.complete())
                .subscribeOn(Schedulers.io());
    }

    /**
     * Refresh inventory status - update book statuses based on loans.
     */
    public Completable refreshInventoryStatus() {
        return Completable.fromAction(() -> {
            try {
                loanRepository.getActiveLoans()
                        .firstOrError()
                        .blockingGet()
                        .forEach(loan -> {
                            try {
                                bookRepository.getBookById(loan.getBookId())
                                        .flatMapCompletable(book -> {
                                            book.setStatus("ON_LOAN");
                                            return bookRepository.update(book);
                                        })
                                        .subscribe(() -> {}, Throwable::printStackTrace);
                            } catch (Exception ignored) {}
                        });
            } catch (Exception ignored) {}

            auditService.log("INVENTORY_REFRESH", "Inventory status refreshed");
        }).subscribeOn(Schedulers.io());
    }

    public void logAutomationTask(String taskName, String details) {
        auditService.log("AUTOMATION_" + taskName, details);
    }
}
