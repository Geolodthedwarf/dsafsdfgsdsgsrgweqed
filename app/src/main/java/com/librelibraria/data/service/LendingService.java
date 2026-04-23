package com.librelibraria.data.service;

import com.librelibraria.data.model.Book;
import com.librelibraria.data.model.Loan;
import com.librelibraria.data.model.LoanStatus;
import com.librelibraria.data.repository.BookRepository;
import com.librelibraria.data.repository.LoanRepository;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LendingService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final AuditService auditService;

    public LendingService(LoanRepository loanRepository, BookRepository bookRepository,
                          AuditService auditService) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.auditService = auditService;
    }

    /** Create a new loan from a fully constructed Loan object. */
    public Single<Long> lendBook(Loan loan) {
        return loanRepository.insert(loan)
                .doOnSuccess(id -> {
                    loan.setId(id);
                    // Decrement available copies
                    bookRepository.getBookById(loan.getBookId())
                            .flatMapCompletable(book -> {
                                int available = Math.max(0, book.getAvailableCopies() - 1);
                                book.setAvailableCopies(available);
                                return bookRepository.update(book);
                            })
                            .subscribeOn(Schedulers.io())
                            .subscribe(() -> {}, e -> {});
                    auditService.logLoanAction("LOAN_CREATED", "Book loaned to " + loan.getBorrowerName(), id);
                });
    }

    /** Create a new loan from individual parameters. */
    public Single<Long> lendBook(long bookId, Long borrowerId, String borrowerName, long dueDate) {
        Loan loan = new Loan();
        loan.setBookId(bookId);
        loan.setBorrowerId(borrowerId);
        loan.setBorrowerName(borrowerName);
        loan.setLoanDate(System.currentTimeMillis());
        loan.setDueDate(dueDate);
        loan.setStatus(LoanStatus.ACTIVE);
        return lendBook(loan);
    }

    /** Mark a loan as returned and restore book availability. */
    public Completable returnBook(long loanId) {
        return loanRepository.getLoanById(loanId)
                .flatMapCompletable(loan -> {
                    loan.setStatus(LoanStatus.RETURNED);
                    loan.setReturnDate(System.currentTimeMillis());
                    return loanRepository.update(loan)
                            .andThen(
                                    bookRepository.getBookById(loan.getBookId())
                                            .flatMapCompletable(book -> {
                                                book.setAvailableCopies(book.getAvailableCopies() + 1);
                                                return bookRepository.update(book);
                                            })
                            )
                            .doOnComplete(() ->
                                    auditService.logLoanAction("LOAN_RETURNED", "Book returned", loanId));
                });
    }

    /** Renew (extend the due date of) an active loan. */
    public Completable renewLoan(long loanId, long newDueDate) {
        return loanRepository.getLoanById(loanId)
                .flatMapCompletable(loan -> {
                    loan.setDueDate(newDueDate);
                    loan.setRenewalCount(loan.getRenewalCount() + 1);
                    return loanRepository.update(loan)
                            .doOnComplete(() ->
                                    auditService.logLoanAction("LOAN_RENEWED", "Loan renewed", loanId));
                });
    }

    public Single<List<Loan>> getActiveLoans() {
        return loanRepository.getActiveLoansOnce();
    }

    public Single<List<Loan>> getReturnedLoans() {
        return loanRepository.getReturnedLoans();
    }

    public Single<List<Loan>> getAllLoans() {
        return loanRepository.getAllLoansOnce();
    }

    public Single<List<Loan>> getOverdueLoans() {
        return loanRepository.getOverdueLoans(System.currentTimeMillis());
    }

    public Single<List<Loan>> getLoansDueSoon(int days) {
        long futureDate = System.currentTimeMillis() + (days * 24L * 60 * 60 * 1000);
        return loanRepository.getLoansDueBefore(futureDate);
    }

    public Single<List<Loan>> getLoansForBorrower(long borrowerId) {
        return loanRepository.getLoansForBorrowerOnce(borrowerId);
    }

    public Single<List<Loan>> getLoansForBook(long bookId) {
        return loanRepository.getLoansForBookOnce(bookId);
    }

    public Single<Loan> getLoanById(long loanId) {
        return loanRepository.getLoanById(loanId);
    }

    public Single<Integer> getActiveLoansCount() {
        return loanRepository.getActiveLoansCount();
    }

    public Single<Integer> getOverdueLoansCount() {
        return loanRepository.getOverdueLoansCount();
    }

    public Completable deleteLoan(Loan loan) {
        return loanRepository.delete(loan)
                .doOnComplete(() ->
                        auditService.logLoanAction("LOAN_DELETED", "Loan deleted", loan.getId()));
    }
}