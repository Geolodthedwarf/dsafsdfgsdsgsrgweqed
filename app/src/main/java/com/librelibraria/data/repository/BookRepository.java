package com.librelibraria.data.repository;

import com.librelibraria.data.database.BookDao;
import com.librelibraria.data.database.TagDao;
import com.librelibraria.data.model.Book;
import com.librelibraria.data.model.ReadingStatus;
import com.librelibraria.data.model.Tag;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Repository for Book data access.
 */
public class BookRepository {

    private final BookDao bookDao;
    private final TagDao tagDao;

    public BookRepository(BookDao bookDao, TagDao tagDao) {
        this.bookDao = bookDao;
        this.tagDao = tagDao;
    }

    public Completable insertBook(Book book) {
        book.setLastModified(System.currentTimeMillis());
        book.setSynced(false);
        return bookDao.insert(book)
                .subscribeOn(Schedulers.io())
                .ignoreElement();
    }

    public Completable updateBook(Book book) {
        book.setLastModified(System.currentTimeMillis());
        book.setSynced(false);
        return bookDao.update(book)
                .subscribeOn(Schedulers.io())
                .ignoreElement();
    }

    public Completable deleteBook(Book book) {
        return bookDao.delete(book)
                .subscribeOn(Schedulers.io())
                .ignoreElement();
    }

    public Completable deleteBookById(long id) {
        return bookDao.deleteById(id)
                .subscribeOn(Schedulers.io())
                .ignoreElement();
    }

    public Single<Book> getBookById(long id) {
        return bookDao.getById(id)
                .subscribeOn(Schedulers.io());
    }

    public Flowable<List<Book>> getAllBooks() {
        return bookDao.getAllBooks()
                .subscribeOn(Schedulers.io());
    }

    public Flowable<List<Book>> searchBooks(String query) {
        return bookDao.searchBooks(query)
                .subscribeOn(Schedulers.io());
    }

    public Flowable<List<Book>> getBooksByGenre(String genre) {
        return bookDao.getBooksByGenre(genre)
                .subscribeOn(Schedulers.io());
    }

    public Flowable<List<Book>> getBooksByReadingStatus(ReadingStatus status) {
        return bookDao.getBooksByReadingStatus(status)
                .subscribeOn(Schedulers.io());
    }

    public Flowable<List<Book>> getAvailableBooks() {
        return bookDao.getAvailableBooks()
                .subscribeOn(Schedulers.io());
    }

    public Single<List<Book>> getTopRatedBooks(int limit) {
        return bookDao.getTopRatedBooks(limit)
                .subscribeOn(Schedulers.io());
    }

    public Single<List<Book>> getRecentlyAddedBooks(int days) {
        long startDate = System.currentTimeMillis() - (days * 24L * 60 * 60 * 1000);
        return bookDao.getRecentlyAddedBooks(startDate)
                .subscribeOn(Schedulers.io());
    }

    public Single<Integer> getTotalBookCount() {
        return bookDao.getTotalCount()
                .subscribeOn(Schedulers.io());
    }

    public Single<Integer> getAvailableBookCount() {
        return bookDao.getAvailableCount()
                .subscribeOn(Schedulers.io());
    }

    public Single<Integer> getBookCountByStatus(ReadingStatus status) {
        return bookDao.getCountByReadingStatus(status)
                .subscribeOn(Schedulers.io());
    }

    public Single<List<BookDao.GenreCount>> getGenreDistribution() {
        return bookDao.getGenreDistribution()
                .subscribeOn(Schedulers.io());
    }

    public Single<Double> getAverageRating() {
        return bookDao.getAverageRating()
                .subscribeOn(Schedulers.io());
    }

    public Completable updateBookRating(long bookId, double rating, String review) {
        return bookDao.updateRating(bookId, rating, review)
                .subscribeOn(Schedulers.io())
                .ignoreElement();
    }

    public Completable updateReadingStatus(long bookId, ReadingStatus status) {
        return bookDao.updateReadingStatus(bookId, status)
                .subscribeOn(Schedulers.io())
                .ignoreElement();
    }

    public Completable updateAvailableCopies(long bookId, int count) {
        return bookDao.updateAvailableCopies(bookId, count)
                .subscribeOn(Schedulers.io())
                .ignoreElement();
    }

    public Single<List<String>> getAllGenres() {
        return bookDao.getAllGenres()
                .subscribeOn(Schedulers.io());
    }

    public Single<List<Book>> getUnsyncedBooks() {
        return bookDao.getUnsyncedBooks()
                .subscribeOn(Schedulers.io());
    }

    public Completable markAsSynced(long bookId) {
        return bookDao.markAsSynced(bookId)
                .subscribeOn(Schedulers.io())
                .ignoreElement();
    }

    // Tag operations
    public Single<Long> insertTag(Tag tag) {
        return tagDao.insert(tag)
                .subscribeOn(Schedulers.io());
    }

    public Flowable<List<Tag>> getAllTags() {
        return tagDao.getAllTags()
                .subscribeOn(Schedulers.io());
    }

    public Single<Tag> getTagByName(String name) {
        return tagDao.getByName(name)
                .subscribeOn(Schedulers.io());
    }

    public Completable deleteTag(Tag tag) {
        return tagDao.delete(tag)
                .subscribeOn(Schedulers.io())
                .ignoreElement();
    }
}
