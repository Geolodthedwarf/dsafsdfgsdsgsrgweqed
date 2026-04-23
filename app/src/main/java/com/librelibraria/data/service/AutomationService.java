package com.librelibraria.data.service;

import com.librelibraria.data.model.AuditLog;
import com.librelibraria.data.model.Book;
import com.librelibraria.data.model.Loan;
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
                .flatMapCompletable(books -> Completable.fromAction(() -> {
                    for (Book book : books) {
                        if (book.getCurrentPage() > 0 && book.getTotalPages() > 0) {
                            int progress = (int) ((book.getCurrentPage() * 100.0) / book.getTotalPages());
                            String newStatus = determineStatusFromProgress(progress, book.getReadingStatus());
                            if (!newStatus.equals(book.getReadingStatus())) {
                                book.setReadingStatus(newStatus);
                                bookRepository.update(book).blockingAwait();
                            }
                        }
                    }
                }))
                .subscribeOn(Schedulers.io());
    }

    /**
     * Determine reading status from progress percentage.
     */
    private String determineStatusFromProgress(int progress, String currentStatus) {
        if (progress >= 100) {
            return "READ";
        } else if (progress > 0) {
            return "READING";
        } else {
            return "WANT_TO_READ";
        }
    }

    /**
     * Generate reading recommendations based on user's library.
     */
    public Single<List<Book>> generateRecommendations(int limit) {
        return bookRepository.getAllBooks()
                .map(books -> {
                    List<Book> recommendations = new ArrayList<>();
                    // Simple recommendation: unread books with good ratings
                    for (Book book : books) {
                        if ("WANT_TO_READ".equals(book.getReadingStatus()) && book.getRating() >= 4.0f) {
                            recommendations.add(book);
                        }
                    }
                    // Sort by rating
                    recommendations.sort((b1, b2) -> Float.compare(b2.getRating(), b1.getRating()));
                    return recommendations.subList(0, Math.min(limit, recommendations.size()));
                })
                .subscribeOn(Schedulers.io());
    }

    /**
     * Archive old returned loans.
     */
    public Completable archiveOldLoans(int daysOld) {
        long cutoffDate = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L);
        return loanRepository.getReturnedLoans()
                .flatMapCompletable(loans -> {
                    List<Loan> toArchive = new ArrayList<>();
                    for (Loan loan : loans) {
                        if (loan.getReturnDate() > 0 && loan.getReturnDate() < cutoffDate) {
                            toArchive.add(loan);
                        }
                    }
                    return Completable.complete();
                })
                .subscribeOn(Schedulers.io());
    }

    /**
     * Log automation task execution.
     */
    public void logAutomationTask(String taskName, String details) {
        auditService.log("AUTOMATION_" + taskName, details);
    }

    /**
     * Get automation task history.
     */
    public Single<List<AuditLog>> getAutomationHistory(int limit) {
        return auditService.getLogsForEntity("AUTOMATION", 0)
                .onErrorReturnItem(new ArrayList<>());
    }

    /**
     * Check for books with missing information.
     */
    public Single<List<Book>> findIncompleteBookData() {
        return bookRepository.getAllBooks()
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
            // Update book availability based on active loans
            loanRepository.getActiveLoans()
                    .firstOrError()
                    .blockingGet()
                    .forEach(loan -> {
                        bookRepository.getBookById(loan.getBookId())
                                .flatMapCompletable(book -> {
                                    book.setStatus("ON_LOAN");
                                    return bookRepository.update(book);
                                })
                                .blockingAwait();
                    });

            // Log the refresh action
            logAutomationTask("INVENTORY_REFRESH", "Inventory status refreshed");
        }).subscribeOn(Schedulers.io());
    }
}
