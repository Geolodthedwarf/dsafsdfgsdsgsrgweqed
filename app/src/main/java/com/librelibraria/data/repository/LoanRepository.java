package com.librelibraria.data.repository;

import com.librelibraria.data.database.LoanDao;
import com.librelibraria.data.database.BorrowerDao;
import com.librelibraria.data.model.Borrower;
import com.librelibraria.data.model.Loan;
import com.librelibraria.data.model.LoanStatus;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LoanRepository {

    private final LoanDao loanDao;
    private final BorrowerDao borrowerDao;

    public LoanRepository(LoanDao loanDao, BorrowerDao borrowerDao) {
        this.loanDao = loanDao;
        this.borrowerDao = borrowerDao;
    }

    // --- Loan CRUD ---

    public Single<Long> insert(Loan loan) {
        return createLoan(loan);
    }

    public Single<Long> createLoan(Loan loan) {
        loan.setLastModified(System.currentTimeMillis());
        loan.setSynced(false);
        return loanDao.insert(loan).subscribeOn(Schedulers.io());
    }

    public Completable update(Loan loan) {
        return updateLoan(loan);
    }

    public Completable updateLoan(Loan loan) {
        loan.setLastModified(System.currentTimeMillis());
        loan.setSynced(false);
        return loanDao.update(loan).subscribeOn(Schedulers.io());
    }

    public Completable delete(Loan loan) {
        return loanDao.delete(loan).subscribeOn(Schedulers.io());
    }

    public Completable returnBook(long loanId, double lateFee) {
        return loanDao.returnBook(loanId, LoanStatus.RETURNED, System.currentTimeMillis(), lateFee)
                .subscribeOn(Schedulers.io());
    }

    public Single<Loan> getLoanById(long id) {
        return loanDao.getById(id).subscribeOn(Schedulers.io());
    }

    // --- Loan queries returning Flowable (for UI observation) ---

    public Flowable<List<Loan>> getAllLoans() {
        return loanDao.getAllLoans().subscribeOn(Schedulers.io());
    }

    public Flowable<List<Loan>> getActiveLoans() {
        return loanDao.getActiveLoans().subscribeOn(Schedulers.io());
    }

    public Flowable<List<Loan>> getLoansForBook(long bookId) {
        return loanDao.getLoansForBook(bookId).subscribeOn(Schedulers.io());
    }

    public Flowable<List<Loan>> getLoansForBorrower(long borrowerId) {
        return loanDao.getLoansForBorrower(borrowerId).subscribeOn(Schedulers.io());
    }

    // --- Loan queries returning Single (for one-shot operations) ---

    public Single<List<Loan>> getActiveLoansOnce() {
        return loanDao.getActiveLoans().firstOrError().subscribeOn(Schedulers.io());
    }

    public Single<List<Loan>> getAllLoansOnce() {
        return loanDao.getAllLoans().firstOrError().subscribeOn(Schedulers.io());
    }

    public Single<List<Loan>> getOverdueLoans(long currentTime) {
        return loanDao.getOverdueLoans(currentTime).subscribeOn(Schedulers.io());
    }

    public Single<List<Loan>> getLoansDueBefore(long futureTime) {
        return loanDao.getLoansDueBefore(futureTime).subscribeOn(Schedulers.io());
    }

    public Single<List<Loan>> getReturnedLoans() {
        return loanDao.getReturnedLoans().subscribeOn(Schedulers.io());
    }

    public Single<List<Loan>> getLoansBetweenDates(long startDate, long endDate) {
        return loanDao.getLoansBetweenDates(startDate, endDate).subscribeOn(Schedulers.io());
    }

    public Single<List<Loan>> getLoansForBorrowerOnce(long borrowerId) {
        return loanDao.getLoansForBorrower(borrowerId).firstOrError().subscribeOn(Schedulers.io());
    }

    public Single<List<Loan>> getLoansForBookOnce(long bookId) {
        return loanDao.getLoansForBook(bookId).firstOrError().subscribeOn(Schedulers.io());
    }

    // --- Counts ---

    public Single<Integer> getActiveLoansCount() {
        return loanDao.getActiveLoansCount().subscribeOn(Schedulers.io());
    }

    public Single<Integer> getOverdueLoansCount() {
        return loanDao.getOverdueLoansCount(System.currentTimeMillis()).subscribeOn(Schedulers.io());
    }

    public Single<Integer> getOverdueLoansCount(long currentTime) {
        return loanDao.getOverdueLoansCount(currentTime).subscribeOn(Schedulers.io());
    }

    public Single<List<LoanDao.BorrowerLoanCount>> getTopBorrowers(int limit) {
        return loanDao.getTopBorrowers(limit).subscribeOn(Schedulers.io());
    }

    // --- Borrower operations ---

    public Single<Long> insertBorrower(Borrower borrower) {
        borrower.setLastModified(System.currentTimeMillis());
        borrower.setSynced(false);
        return borrowerDao.insert(borrower).subscribeOn(Schedulers.io());
    }

    public Completable updateBorrower(Borrower borrower) {
        borrower.setLastModified(System.currentTimeMillis());
        borrower.setSynced(false);
        return borrowerDao.update(borrower).subscribeOn(Schedulers.io());
    }

    public Completable deleteBorrower(Borrower borrower) {
        return borrowerDao.delete(borrower).subscribeOn(Schedulers.io());
    }

    public Single<Borrower> getBorrowerById(long id) {
        return borrowerDao.getById(id).subscribeOn(Schedulers.io());
    }

    public Flowable<List<Borrower>> getAllBorrowers() {
        return borrowerDao.getAllBorrowers().subscribeOn(Schedulers.io());
    }

    public Flowable<List<Borrower>> searchBorrowers(String query) {
        return borrowerDao.searchBorrowers(query).subscribeOn(Schedulers.io());
    }

    public Single<List<Borrower>> getTopBorrowersList(int limit) {
        return borrowerDao.getTopBorrowers(limit).subscribeOn(Schedulers.io());
    }

    public Completable incrementBorrowerCount(long borrowerId) {
        return borrowerDao.incrementBorrowed(borrowerId, System.currentTimeMillis())
                .subscribeOn(Schedulers.io());
    }
}