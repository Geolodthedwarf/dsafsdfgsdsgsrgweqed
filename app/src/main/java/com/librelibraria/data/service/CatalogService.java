package com.librelibraria.data.service;

import com.librelibraria.data.model.Book;
import com.librelibraria.data.repository.BookRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class CatalogService {

    private final BookRepository bookRepository;
    private final AuditService auditService;
    private final ExecutorService executor;

    public CatalogService(BookRepository bookRepository, AuditService auditService) {
        this.bookRepository = bookRepository;
        this.auditService = auditService;
        this.executor = Executors.newSingleThreadExecutor();
    }

    public Completable addBook(Book book) {
        return bookRepository.insert(book)
                .doOnSuccess(id -> {
                    book.setId(id);
                    auditService.logBookAction("BOOK_ADDED", "Book added: " + book.getTitle(), id);
                })
                .ignoreElement();
    }

    public Completable updateBook(Book book) {
        return bookRepository.update(book)
                .doOnComplete(() ->
                        auditService.logBookAction("BOOK_UPDATED", "Book updated: " + book.getTitle(), book.getId()));
    }

    public Completable deleteBook(Book book) {
        return bookRepository.delete(book)
                .doOnComplete(() ->
                        auditService.logBookAction("BOOK_DELETED", "Book deleted: " + book.getTitle(), book.getId()));
    }

    public Completable saveBook(Book book, boolean isUpdate) {
        return isUpdate ? updateBook(book) : addBook(book);
    }

    /** Returns all books as a Single snapshot. */
    public Single<List<Book>> getAllBooks() {
        return bookRepository.getAllBooks()
                .firstOrError()
                .subscribeOn(Schedulers.io());
    }

    /** Load catalog with optional search and genre filter. */
    public Single<List<Book>> loadCatalog(String searchQuery, String genreFilter, int page, int pageSize) {
        String query = searchQuery != null ? searchQuery : "";
        if (genreFilter == null || genreFilter.isEmpty()) {
            return bookRepository.searchBooks(query)
                    .firstOrError()
                    .subscribeOn(Schedulers.io());
        } else {
            return bookRepository.searchBooksWithFilters(query, null, genreFilter)
                    .subscribeOn(Schedulers.io());
        }
    }

    /** Search books by title/author/ISBN. */
    public Single<List<Book>> searchBooks(String query) {
        return bookRepository.searchBooks(query)
                .firstOrError()
                .subscribeOn(Schedulers.io());
    }

    public Single<Book> getBookById(long bookId) {
        return bookRepository.getBookById(bookId);
    }

    public Single<List<Book>> getBooksByStatus(String status) {
        return bookRepository.getBooksByStatus(status);
    }

    public Single<Integer> getAvailableBooksCount() {
        return bookRepository.getAvailableCount();
    }

    public Single<Integer> getTotalBooksCount() {
        return bookRepository.getTotalCount();
    }

    public Completable updateBookStatus(long bookId, String newStatus) {
        return bookRepository.getBookById(bookId)
                .flatMapCompletable(book -> {
                    book.setReadingStatus(
                            com.librelibraria.data.model.ReadingStatus.fromString(newStatus));
                    return bookRepository.update(book);
                });
    }

    public Single<List<Book>> searchBooksWithFilters(String query, String status, String genre) {
        return bookRepository.searchBooksWithFilters(query, status, genre);
    }
}