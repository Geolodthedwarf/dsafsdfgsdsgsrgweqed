package com.librelibraria.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.librelibraria.data.model.Book;
import com.librelibraria.data.model.ReadingStatus;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Data Access Object for Book entity.
 */
@Dao
public interface BookDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(Book book);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAll(List<Book> books);

    @Update
    Completable update(Book book);

    @Delete
    Completable delete(Book book);

    @Query("DELETE FROM books WHERE id = :id")
    Completable deleteById(long id);

    @Query("SELECT * FROM books WHERE id = :id")
    Single<Book> getById(long id);

    @Query("SELECT * FROM books WHERE id = :id")
    LiveData<Book> getByIdLive(long id);

    @Query("SELECT * FROM books ORDER BY dateAdded DESC")
    Flowable<List<Book>> getAllBooks();

    @Query("SELECT * FROM books ORDER BY dateAdded DESC")
    LiveData<List<Book>> getAllBooksLive();

    @Query("SELECT * FROM books WHERE isSynced = 0")
    Single<List<Book>> getUnsyncedBooks();

    @Query("SELECT * FROM books WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%' OR isbn LIKE '%' || :query || '%' ORDER BY title ASC")
    Flowable<List<Book>> searchBooks(String query);

    @Query("SELECT * FROM books WHERE genre = :genre ORDER BY title ASC")
    Flowable<List<Book>> getBooksByGenre(String genre);

    @Query("SELECT * FROM books WHERE readingStatus = :status ORDER BY title ASC")
    Flowable<List<Book>> getBooksByReadingStatus(ReadingStatus status);

    @Query("SELECT * FROM books WHERE availableCopies > 0 ORDER BY title ASC")
    Flowable<List<Book>> getAvailableBooks();

    @Query("SELECT * FROM books ORDER BY rating DESC LIMIT :limit")
    Single<List<Book>> getTopRatedBooks(int limit);

    @Query("SELECT * FROM books WHERE rating > 0 ORDER BY rating DESC LIMIT :limit")
    LiveData<List<Book>> getTopRatedBooksLive(int limit);

    @Query("SELECT * FROM books WHERE dateAdded >= :startDate ORDER BY dateAdded DESC")
    Single<List<Book>> getRecentlyAddedBooks(long startDate);

    @Query("SELECT COUNT(*) FROM books")
    Single<Integer> getTotalCount();

    @Query("SELECT COUNT(*) FROM books WHERE availableCopies > 0")
    Single<Integer> getAvailableCount();

    @Query("SELECT COUNT(*) FROM books WHERE readingStatus = :status")
    Single<Integer> getCountByReadingStatus(ReadingStatus status);

    @Query("SELECT genre, COUNT(*) as count FROM books WHERE genre IS NOT NULL AND genre != '' GROUP BY genre ORDER BY count DESC")
    Single<List<GenreCount>> getGenreDistribution();

    @Query("SELECT AVG(rating) FROM books WHERE rating > 0")
    Single<Double> getAverageRating();

    @Query("UPDATE books SET availableCopies = :count WHERE id = :bookId")
    Completable updateAvailableCopies(long bookId, int count);

    @Query("UPDATE books SET rating = :rating, review = :review WHERE id = :bookId")
    Completable updateRating(long bookId, double rating, String review);

    @Query("UPDATE books SET readingStatus = :status WHERE id = :bookId")
    Completable updateReadingStatus(long bookId, ReadingStatus status);

    @Query("UPDATE books SET isSynced = 1 WHERE id = :id")
    Completable markAsSynced(long id);

    @Query("SELECT DISTINCT genre FROM books WHERE genre IS NOT NULL AND genre != '' ORDER BY genre ASC")
    Single<List<String>> getAllGenres();

    @Query("SELECT * FROM books WHERE tags LIKE '%' || :tag || '%' ORDER BY title ASC")
    Flowable<List<Book>> getBooksByTag(String tag);

    class GenreCount {
        public String genre;
        public int count;
    }
}
