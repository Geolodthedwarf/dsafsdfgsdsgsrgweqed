package com.librelibraria.data.service;

import com.librelibraria.data.model.Book;
import com.librelibraria.data.repository.BookRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Service for catalog operations - book management and searching.
 */
public class CatalogService {

    private final BookRepository bookRepository;
    private final AuditService auditService;
    private final ExecutorService executor;

    public CatalogService(BookRepository bookRepository, AuditService auditService) {
        this.bookRepository = bookRepository;
        this.auditService = auditService;
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Add a new book to the catalog.
     */
    public Completable addBook(Book book) {
        return bookRepository.insert(book)
                .doOnSuccess(id -> {
                    book.setId(id);
                    auditService.logBookAction("BOOK_ADDED", "Book added: " + book.getTitle(), id);
                })
                .ignoreElement();
    }

    /**
     * Update an existing book.
     */
    public Completable updateBook(Book book) {
        return bookRepository.update(book)
                .doOnSuccess(() -> auditService.logBookAction("BOOK_UPDATED", "Book updated: " + book.getTitle(), book.getId()))
                .ignoreElement();
    }

    /**
     * Delete a book.
     */
    public Completable deleteBook(Book book) {
        return bookRepository.delete(book)
                .doOnSuccess(() -> auditService.logBookAction("BOOK_DELETED", "Book deleted: " + book.getTitle(), book.getId()))
                .ignoreElement();
    }

    /**
     * Save a book (add or update).
     */
    public Completable saveBook(Book book, boolean isUpdate) {
        if (isUpdate) {
            return updateBook(book);
        } else {
            return addBook(book);
        }
    }

    /**
     * Get all books.
     */
    public Single<java.util.List<Book>> getAllBooks() {
        return bookRepository.getAllBooks();
    }

    /**
     * Load catalog with search and filters.
     */
    public Single<java.util.List<Book>> loadCatalog(String searchQuery, String genreFilter, int page, int pageSize) {
        if (searchQuery == null) searchQuery = "";
        if (genreFilter == null || genreFilter.isEmpty()) {
            return bookRepository.searchBooks(searchQuery)
                    .firstOrError();
        } else {
            return bookRepository.searchBooksWithFilters(searchQuery, null, genreFilter)
                    .firstOrError();
        }
    }

    /**
     * Search books by title.
     */
    public Single<java.util.List<Book>> searchBooks(String query) {
        return bookRepository.searchBooks(query);
    }

    /**
     * Get book by ID.
     */
    public Single<Book> getBookById(long bookId) {
        return bookRepository.getBookById(bookId);
    }

    /**
     * Get books by reading status.
     */
    public Single<java.util.List<Book>> getBooksByStatus(String status) {
        return bookRepository.getBooksByStatus(status);
    }

    /**
     * Get available books count.
     */
    public Single<Integer> getAvailableBooksCount() {
        return bookRepository.getAvailableCount();
    }

    /**
     * Get total books count.
     */
    public Single<Integer> getTotalBooksCount() {
        return bookRepository.getTotalCount();
    }

    /**
     * Update book reading status.
     */
    public Completable updateBookStatus(long bookId, String newStatus) {
        return bookRepository.getBookById(bookId)
                .flatMapCompletable(book -> {
                    book.setReadingStatus(newStatus);
                    return bookRepository.update(book);
                });
    }

    /**
     * Search books with filters.
     */
    public Single<java.util.List<Book>> searchBooksWithFilters(
            String query, String status, String genre) {
        return bookRepository.searchBooksWithFilters(query, status, genre);
    }
}
