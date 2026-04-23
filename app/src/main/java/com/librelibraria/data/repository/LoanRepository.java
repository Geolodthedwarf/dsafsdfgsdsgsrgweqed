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

/**
 * Repository for Loan and Borrower data access.
 */
public class LoanRepository {

    private final LoanDao loanDao;
    private final BorrowerDao borrowerDao;

    public LoanRepository(LoanDao loanDao, BorrowerDao borrowerDao) {
        this.loanDao = loanDao;
        this.borrowerDao = borrowerDao;
    }

    // Loan operations
    public Single<Long> createLoan(Loan loan) {
        loan.setLastModified(System.currentTimeMillis());
        loan.setSynced(false);
        return loanDao.insert(loan)
                .subscribeOn(Schedulers.io());
    }

    public Completable updateLoan(Loan loan) {
        loan.setLastModified(System.currentTimeMillis());
        loan.setSynced(false);
        return loanDao.update(loan)
                .subscribeOn(Schedulers.io());
    }

    public Completable returnBook(long loanId, double lateFee) {
        return loanDao.returnBook(loanId, LoanStatus.RETURNED, System.currentTimeMillis(), lateFee)
                .subscribeOn(Schedulers.io());
    }

    public Single<Loan> getLoanById(long id) {
        return loanDao.getById(id)
                .subscribeOn(Schedulers.io());
    }

    public Flowable<List<Loan>> getAllLoans() {
        return loanDao.getAllLoans()
                .subscribeOn(Schedulers.io());
    }

    public Flowable<List<Loan>> getActiveLoans() {
        return loanDao.getActiveLoans()
                .subscribeOn(Schedulers.io());
    }

    public Flowable<List<Loan>> getLoansForBook(long bookId) {
        return loanDao.getLoansForBook(bookId)
                .subscribeOn(Schedulers.io());
    }

    public Single<Integer> getActiveLoansCount() {
        return loanDao.getActiveLoansCount()
                .subscribeOn(Schedulers.io());
    }

    public Single<Integer> getOverdueLoansCount() {
        return loanDao.getOverdueLoansCount(System.currentTimeMillis())
                .subscribeOn(Schedulers.io());
    }

    public Single<List<LoanDao.BorrowerLoanCount>> getTopBorrowers(int limit) {
        return loanDao.getTopBorrowers(limit)
                .subscribeOn(Schedulers.io());
    }

    // Borrower operations
    public Single<Long> insertBorrower(Borrower borrower) {
        borrower.setLastModified(System.currentTimeMillis());
        borrower.setSynced(false);
        return borrowerDao.insert(borrower)
                .subscribeOn(Schedulers.io());
    }

    public Completable updateBorrower(Borrower borrower) {
        borrower.setLastModified(System.currentTimeMillis());
        borrower.setSynced(false);
        return borrowerDao.update(borrower)
                .subscribeOn(Schedulers.io());
    }

    public Completable deleteBorrower(Borrower borrower) {
        return borrowerDao.delete(borrower)
                .subscribeOn(Schedulers.io());
    }

    public Single<Borrower> getBorrowerById(long id) {
        return borrowerDao.getById(id)
                .subscribeOn(Schedulers.io());
    }

    public Flowable<List<Borrower>> getAllBorrowers() {
        return borrowerDao.getAllBorrowers()
                .subscribeOn(Schedulers.io());
    }

    public Flowable<List<Borrower>> searchBorrowers(String query) {
        return borrowerDao.searchBorrowers(query)
                .subscribeOn(Schedulers.io());
    }

    public Single<List<Borrower>> getTopBorrowersList(int limit) {
        return borrowerDao.getTopBorrowers(limit)
                .subscribeOn(Schedulers.io());
    }

    public Completable incrementBorrowerCount(long borrowerId) {
        return borrowerDao.incrementBorrowed(borrowerId, System.currentTimeMillis())
                .subscribeOn(Schedulers.io());
    }
}
