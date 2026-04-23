package com.librelibraria.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.librelibraria.data.model.DiaryEntry;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface DiaryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Single<Long> insert(DiaryEntry entry);

    @Update
    Completable update(DiaryEntry entry);

    @Delete
    Completable delete(DiaryEntry entry);

    @Query("DELETE FROM diary_entries WHERE id = :id")
    Completable deleteById(long id);

    @Query("SELECT * FROM diary_entries WHERE id = :id")
    Single<DiaryEntry> getById(long id);

    // Fixed: was incorrectly returning DiaryEntry synchronously; Room requires reactive/async return
    @Query("SELECT * FROM diary_entries WHERE id = :id")
    Single<DiaryEntry> getEntryById(long id);

    @Query("SELECT * FROM diary_entries ORDER BY date DESC")
    Flowable<List<DiaryEntry>> getAllEntries();

    @Query("SELECT * FROM diary_entries ORDER BY date DESC")
    LiveData<List<DiaryEntry>> getAllEntriesLive();

    @Query("SELECT * FROM diary_entries WHERE bookId = :bookId ORDER BY date DESC")
    Flowable<List<DiaryEntry>> getEntriesForBook(long bookId);

    @Query("SELECT * FROM diary_entries WHERE bookId = :bookId ORDER BY date DESC")
    LiveData<List<DiaryEntry>> getEntriesForBookLive(long bookId);

    @Query("SELECT * FROM diary_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    Flowable<List<DiaryEntry>> getEntriesBetweenDates(long startDate, long endDate);

    @Query("SELECT * FROM diary_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    Single<List<DiaryEntry>> getEntriesBetweenDatesSync(long startDate, long endDate);

    @Query("SELECT * FROM diary_entries ORDER BY date DESC LIMIT :limit")
    Single<List<DiaryEntry>> getRecentEntries(int limit);

    @Query("SELECT * FROM diary_entries WHERE bookTitle LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%' ORDER BY date DESC")
    Single<List<DiaryEntry>> searchEntries(String query);

    @Query("SELECT * FROM diary_entries WHERE isSynced = 0")
    Single<List<DiaryEntry>> getUnsyncedEntries();

    @Query("UPDATE diary_entries SET isSynced = 1 WHERE id = :id")
    Completable markAsSynced(long id);

    @Query("SELECT COUNT(*) FROM diary_entries")
    Single<Integer> getEntriesCount();

    @Query("SELECT COUNT(*) FROM diary_entries")
    Single<Integer> getTotalCount();

    @Query("SELECT COUNT(*) FROM diary_entries WHERE bookId = :bookId")
    Single<Integer> getCountForBook(long bookId);
}