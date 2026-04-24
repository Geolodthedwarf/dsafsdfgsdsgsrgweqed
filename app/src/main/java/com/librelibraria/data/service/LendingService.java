package com.librelibraria.data.service;

import android.content.Context;
import com.librelibraria.data.model.Book;
import com.librelibraria.data.model.Borrower;
import com.librelibraria.data.model.Loan;
import com.librelibraria.data.model.LoanStatus;
import com.librelibraria.data.storage.FileStorageManager;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.io.File;
import java.util.List;

public class LendingService {

    private final FileStorageManager storageManager;
    private final Context context;

    public LendingService(Context context) {
        this.context = context.getApplicationContext();
        this.storageManager = FileStorageManager.getInstance(context);
    }

    public Single<List<Loan>> getAllLoans() {
        return Single.fromCallable(() -> storageManager.loadAllLoans())
                .subscribeOn(Schedulers.io());
    }

    public Single<List<Loan>> getActiveLoans() {
        return getAllLoans()
                .map(loans -> {
                    List<Loan> active = new java.util.ArrayList<>();
                    for (Loan loan : loans) {
                        if (loan.getStatus() == LoanStatus.ACTIVE) {
                            active.add(loan);
                        }
                    }
                    return active;
                });
    }

    public Single<Loan> getLoanById(long loanId) {
        return Single.fromCallable(() -> {
            List<Loan> loans = storageManager.loadAllLoans();
            for (Loan loan : loans) {
                if (loan.getId() == loanId) {
                    return loan;
                }
            }
            return null;
        }).subscribeOn(Schedulers.io());
    }

    public Single<Long> lendBook(Loan loan) {
        return Completable.fromAction(() -> {
            loan.setStatus(LoanStatus.ACTIVE);
            loan.setLoanDate(System.currentTimeMillis());
            if (loan.getDueDate() == 0) {
                loan.setDueDate(System.currentTimeMillis() + (14L * 24 * 60 * 60 * 1000));
            }
            storageManager.saveLoan(loan);
        }).toSingleDefault(loan.getId()).subscribeOn(Schedulers.io());
    }

    public Single<Long> lendBook(long bookId, Long borrowerId, String borrowerName, long dueDate) {
        Loan loan = new Loan();
        loan.setBookId(bookId);
        loan.setBorrowerId(borrowerId);
        loan.setBorrowerName(borrowerName);
        loan.setDueDate(dueDate > 0 ? dueDate : System.currentTimeMillis() + (14L * 24 * 60 * 60 * 1000));
        return lendBook(loan);
    }

    public Completable returnBook(long loanId) {
        return Completable.fromAction(() -> {
            List<Loan> loans = storageManager.loadAllLoans();
            for (Loan loan : loans) {
                if (loan.getId() == loanId) {
                    loan.setStatus(LoanStatus.RETURNED);
                    loan.setReturnDate(System.currentTimeMillis());
                    storageManager.saveLoan(loan);
                    break;
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    public Completable renewLoan(long loanId, long newDueDate) {
        return Completable.fromAction(() -> {
            List<Loan> loans = storageManager.loadAllLoans();
            for (Loan loan : loans) {
                if (loan.getId() == loanId) {
                    loan.setDueDate(newDueDate);
                    loan.setRenewalCount(loan.getRenewalCount() + 1);
                    storageManager.saveLoan(loan);
                    break;
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    public Single<List<Borrower>> getAllBorrowers() {
        return Single.fromCallable(() -> storageManager.loadAllBorrowers())
                .subscribeOn(Schedulers.io());
    }

    public Completable addBorrower(Borrower borrower) {
        return Completable.fromAction(() -> storageManager.saveBorrower(borrower))
                .subscribeOn(Schedulers.io());
    }

    public Completable deleteBorrower(Borrower borrower) {
        return Completable.fromAction(() -> {
            if (borrower.getId() > 0) {
                File dir = new File(storageManager.getBasePath(), "borrowers");
                File file = new File(dir, "borrower_" + borrower.getId() + ".bcbr");
                if (file.exists()) file.delete();
            }
        }).subscribeOn(Schedulers.io());
    }
}