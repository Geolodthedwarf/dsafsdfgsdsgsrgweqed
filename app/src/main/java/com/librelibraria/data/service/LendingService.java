package com.librelibraria.data.service;

import com.librelibraria.data.model.Book;
import com.librelibraria.data.model.Loan;
import com.librelibraria.data.repository.BookRepository;
import com.librelibraria.data.repository.LoanRepository;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Service for managing book lending operations.
 */
public class LendingService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final AuditService auditService;

    public LendingService(LoanRepository loanRepository, BookRepository bookRepository, AuditService auditService) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.auditService = auditService;
    }

    /**
     * Create a new loan (lend a book).
     */
    public Single<Long> lendBook(Loan loan) {
        return loanRepository.insert(loan)
                .doOnSuccess(id -> {
                    loan.setId(id);
                    // Update book status to on loan
                    bookRepository.getBookById(loan.getBookId())
                            .flatMapCompletable(book -> {
                                book.setStatus("ON_LOAN");
                                return bookRepository.update(book);
                            })
                            .subscribe();
                    auditService.logLoanAction("LOAN_CREATED", "Book loaned to borrower", id);
                });
    }

    /**
     * Lend a book with individual parameters.
     */
    public Single<Long> lendBook(long bookId, Long borrowerId, String borrowerName, long dueDate) {
        Loan loan = new Loan();
        loan.setBookId(bookId);
        loan.setBorrowerId(borrowerId != null ? borrowerId : 0);
        loan.setBorrowerName(borrowerName);
        loan.setLoanDate(System.currentTimeMillis());
        loan.setDueDate(dueDate);
        loan.setStatus("ACTIVE");
        return lendBook(loan);
    }

    /**
     * Return a book (close the loan).
     */
    public Completable returnBook(long loanId) {
        return loanRepository.getLoanById(loanId)
                .flatMapCompletable(loan -> {
                    loan.setStatus("RETURNED");
                    loan.setReturnDate(System.currentTimeMillis());
                    return loanRepository.update(loan)
                            .doOnSuccess(() -> {
                                // Update book status to available
                                bookRepository.getBookById(loan.getBookId())
                                        .flatMapCompletable(book -> {
                                            book.setStatus("AVAILABLE");
                                            return bookRepository.update(book);
                                        })
                                        .subscribe();
                                auditService.logLoanAction("LOAN_RETURNED", "Book returned", loanId);
                            });
                });
    }

    /**
     * Renew a loan (extend due date).
     */
    public Completable renewLoan(long loanId, long newDueDate) {
        return loanRepository.getLoanById(loanId)
                .flatMapCompletable(loan -> {
                    loan.setDueDate(newDueDate);
                    loan.setRenewalCount(loan.getRenewalCount() + 1);
                    return loanRepository.update(loan)
                            .doOnSuccess(() -> auditService.logLoanAction("LOAN_RENEWED", "Loan renewed", loanId));
                });
    }

    /**
     * Get all active loans.
     */
    public Single<List<Loan>> getActiveLoans() {
        return loanRepository.getActiveLoans();
    }

    /**
     * Get all returned loans.
     */
    public Single<List<Loan>> getReturnedLoans() {
        return loanRepository.getReturnedLoans();
    }

    /**
     * Get all loans.
     */
    public Single<List<Loan>> getAllLoans() {
        return loanRepository.getAllLoans();
    }

    /**
     * Get overdue loans.
     */
    public Single<List<Loan>> getOverdueLoans() {
        return loanRepository.getOverdueLoans(System.currentTimeMillis());
    }

    /**
     * Get loans due soon (within days).
     */
    public Single<List<Loan>> getLoansDueSoon(int days) {
        long futureDate = System.currentTimeMillis() + (days * 24 * 60 * 60 * 1000L);
        return loanRepository.getLoansDueBefore(futureDate);
    }

    /**
     * Get loans for a specific borrower.
     */
    public Single<List<Loan>> getLoansForBorrower(long borrowerId) {
        return loanRepository.getLoansForBorrower(borrowerId);
    }

    /**
     * Get loans for a specific book.
     */
    public Single<List<Loan>> getLoansForBook(long bookId) {
        return loanRepository.getLoansForBook(bookId);
    }

    /**
     * Get loan by ID.
     */
    public Single<Loan> getLoanById(long loanId) {
        return loanRepository.getLoanById(loanId);
    }

    /**
     * Get active loans count.
     */
    public Single<Integer> getActiveLoansCount() {
        return loanRepository.getActiveLoansCount();
    }

    /**
     * Get overdue loans count.
     */
    public Single<Integer> getOverdueLoansCount() {
        return loanRepository.getOverdueLoansCount(System.currentTimeMillis());
    }

    /**
     * Delete a loan.
     */
    public Completable deleteLoan(Loan loan) {
        return loanRepository.delete(loan)
                .doOnSuccess(() -> auditService.logLoanAction("LOAN_DELETED", "Loan deleted", loan.getId()));
    }
}
