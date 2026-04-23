package com.librelibraria.data.service;

import com.librelibraria.data.model.Book;
import com.librelibraria.data.repository.BookRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Service for managing book ratings.
 */
public class RatingService {

    private final BookRepository bookRepository;
    private final AuditService auditService;

    public RatingService(BookRepository bookRepository, AuditService auditService) {
        this.bookRepository = bookRepository;
        this.auditService = auditService;
    }

    /**
     * Rate a book with optional review.
     */
    public Completable saveRating(long bookId, double rating, String review, boolean isRecommended) {
        return bookRepository.getBookById(bookId)
                .flatMapCompletable(book -> {
                    book.setRating((float) rating);
                    book.setLastModified(System.currentTimeMillis());
                    return bookRepository.update(book)
                            .doOnComplete(() -> auditService.logBookAction("BOOK_RATED", "Book rated: " + rating + " stars", bookId));
                });
    }

    /**
     * Rate a book.
     */
    public Completable rateBook(long bookId, float rating) {
        return bookRepository.getBookById(bookId)
                .flatMapCompletable(book -> {
                    book.setRating(rating);
                    book.setLastModified(System.currentTimeMillis());
                    return bookRepository.update(book)
                            .doOnComplete(() -> auditService.logBookAction("BOOK_RATED", "Book rated: " + rating + " stars", bookId));
                });
    }

    /**
     * Get average rating for a book.
     */
    public Single<Float> getAverageRating(long bookId) {
        return bookRepository.getBookById(bookId)
                .map(book -> book.getRating() > 0 ? (float) book.getRating() : 0f);
    }

    /**
     * Get all ratings for a book.
     */
    public Single<List<Float>> getBookRatings(long bookId) {
        // Return the single rating for a book (as per current model)
        return getAverageRating(bookId)
                .map(rating -> {
                    List<Float> ratings = new ArrayList<>();
                    if (rating > 0) {
                        ratings.add(rating);
                    }
                    return ratings;
                });
    }

    /**
     * Get books by rating range.
     */
    public Single<List<Book>> getBooksByRatingRange(float minRating, float maxRating) {
        return bookRepository.getAllBooks()
                .firstOrError()
                .map(books -> {
                    List<Book> filtered = new ArrayList<>();
                    for (Book book : books) {
                        float rating = (float) book.getRating();
                        if (rating >= minRating && rating <= maxRating) {
                            filtered.add(book);
                        }
                    }
                    return filtered;
                });
    }

    /**
     * Get top rated books.
     */
    public Single<List<Book>> getTopRatedBooks(int limit) {
        return bookRepository.getAllBooks()
                .firstOrError()
                .map(books -> {
                    books.sort((b1, b2) -> Float.compare((float) b2.getRating(), (float) b1.getRating()));
                    return books.subList(0, Math.min(limit, books.size()));
                });
    }

    /**
     * Get unrated books.
     */
    public Single<List<Book>> getUnratedBooks() {
        return bookRepository.getAllBooks()
                .firstOrError()
                .map(books -> {
                    List<Book> unrated = new ArrayList<>();
                    for (Book book : books) {
                        if (book.getRating() == 0) {
                            unrated.add(book);
                        }
                    }
                    return unrated;
                });
    }

    /**
     * Update rating for multiple books.
     */
    public Completable bulkUpdateRatings(Map<Long, Float> bookRatings) {
        return Completable.fromAction(() -> {
            for (Map.Entry<Long, Float> entry : bookRatings.entrySet()) {
                rateBook(entry.getKey(), entry.getValue()).blockingSubscribe();
            }
        }).subscribeOn(Schedulers.io());
    }
}
