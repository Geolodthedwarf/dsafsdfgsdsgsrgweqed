package com.librelibraria.data.service;

import com.librelibraria.data.model.Book;
import com.librelibraria.data.model.Tag;
import com.librelibraria.data.repository.BookRepository;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Service for tag-related operations.
 */
public class TagService {

    private final BookRepository bookRepository;

    public TagService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Single<List<Tag>> getAllTags() {
        return bookRepository.getAllTags()
                .subscribeOn(Schedulers.io());
    }

    public Single<Tag> getTagById(long tagId) {
        return bookRepository.getTagById(tagId)
                .subscribeOn(Schedulers.io());
    }

    public Completable addTag(Tag tag) {
        return bookRepository.insertTag(tag)
                .subscribeOn(Schedulers.io());
    }

    public Completable updateTag(Tag tag) {
        return bookRepository.updateTag(tag)
                .subscribeOn(Schedulers.io());
    }

    public Completable deleteTag(Tag tag) {
        return bookRepository.deleteTag(tag)
                .subscribeOn(Schedulers.io());
    }

    public Single<List<Book>> getBooksForTag(long tagId) {
        return bookRepository.getBooksByTag(tagId)
                .subscribeOn(Schedulers.io());
    }
}
