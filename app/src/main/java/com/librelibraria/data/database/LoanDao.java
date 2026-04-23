package com.librelibraria.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.librelibraria.data.model.Loan;
import com.librelibraria.data.model.LoanStatus;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Data Access Object for Loan entity.
 */
@Dao
public interface LoanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Single<Long> insert(Loan loan);

    @Update
    Completable update(Loan loan);

    @Delete
    Completable delete(Loan loan);

    @Query("DELETE FROM loans WHERE id = :id")
    Completable deleteById(long id);

    @Query("SELECT * FROM loans WHERE id = :id")
    Single<Loan> getById(long id);

    @Query("SELECT * FROM loans ORDER BY loanDate DESC")
    Flowable<List<Loan>> getAllLoans();

    @Query("SELECT * FROM loans ORDER BY loanDate DESC")
    LiveData<List<Loan>> getAllLoansLive();

    @Query("SELECT * FROM loans WHERE status = :status ORDER BY dueDate ASC")
    Flowable<List<Loan>> getLoansByStatus(LoanStatus status);

    @Query("SELECT * FROM loans WHERE status = 'ACTIVE' ORDER BY dueDate ASC")
    Flowable<List<Loan>> getActiveLoans();

    @Query("SELECT * FROM loans WHERE status = 'ACTIVE' ORDER BY dueDate ASC")
    LiveData<List<Loan>> getActiveLoansLive();

    @Query("SELECT * FROM loans WHERE bookId = :bookId ORDER BY loanDate DESC")
    Flowable<List<Loan>> getLoansForBook(long bookId);

    @Query("SELECT * FROM loans WHERE borrowerId = :borrowerId ORDER BY loanDate DESC")
    Flowable<List<Loan>> getLoansForBorrower(long borrowerId);

    @Query("SELECT * FROM loans WHERE isSynced = 0")
    Single<List<Loan>> getUnsyncedLoans();

    @Query("SELECT COUNT(*) FROM loans WHERE status = 'ACTIVE'")
    Single<Integer> getActiveLoansCount();

    @Query("SELECT COUNT(*) FROM loans WHERE status = 'ACTIVE' AND dueDate < :currentTime")
    Single<Integer> getOverdueLoansCount(long currentTime);

    @Query("UPDATE loans SET status = :status, returnDate = :returnDate, lateFee = :lateFee WHERE id = :loanId")
    Completable returnBook(long loanId, LoanStatus status, long returnDate, double lateFee);

    @Query("UPDATE loans SET isSynced = 1 WHERE id = :id")
    Completable markAsSynced(long id);

    @Query("SELECT borrowerId, COUNT(*) as count FROM loans WHERE borrowerId IS NOT NULL GROUP BY borrowerId ORDER BY count DESC LIMIT :limit")
    Single<List<BorrowerLoanCount>> getTopBorrowers(int limit);

    class BorrowerLoanCount {
        public Long borrowerId;
        public int count;
    }
}
