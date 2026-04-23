package com.librelibraria.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.librelibraria.data.model.Borrower;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Data Access Object for Borrower entity.
 */
@Dao
public interface BorrowerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Single<Long> insert(Borrower borrower);

    @Update
    Completable update(Borrower borrower);

    @Delete
    Completable delete(Borrower borrower);

    @Query("DELETE FROM borrowers WHERE id = :id")
    Completable deleteById(long id);

    @Query("SELECT * FROM borrowers WHERE id = :id")
    Single<Borrower> getById(long id);

    @Query("SELECT * FROM borrowers ORDER BY name ASC")
    Flowable<List<Borrower>> getAllBorrowers();

    @Query("SELECT * FROM borrowers ORDER BY name ASC")
    LiveData<List<Borrower>> getAllBorrowersLive();

    @Query("SELECT * FROM borrowers WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    Flowable<List<Borrower>> searchBorrowers(String query);

    @Query("SELECT * FROM borrowers WHERE isSynced = 0")
    Single<List<Borrower>> getUnsyncedBorrowers();

    @Query("UPDATE borrowers SET totalBorrowed = totalBorrowed + 1, lastModified = :timestamp WHERE id = :id")
    Completable incrementBorrowed(long id, long timestamp);

    @Query("UPDATE borrowers SET isSynced = 1 WHERE id = :id")
    Completable markAsSynced(long id);

    @Query("SELECT * FROM borrowers ORDER BY totalBorrowed DESC LIMIT :limit")
    Single<List<Borrower>> getTopBorrowers(int limit);

    @Query("SELECT COUNT(*) FROM borrowers")
    Single<Integer> getTotalCount();

    @Query("SELECT COUNT(*) FROM borrowers")
    Single<Integer> getBorrowerCount();
}
