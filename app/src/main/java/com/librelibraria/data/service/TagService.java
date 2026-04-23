package com.librelibraria.data.service;

import com.librelibraria.data.model.Book;
import com.librelibraria.data.model.Tag;
import com.librelibraria.data.repository.BookRepository;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class TagService {

    private final BookRepository bookRepository;
    private final AuditService auditService;

    public TagService(BookRepository bookRepository, AuditService auditService) {
        this.bookRepository = bookRepository;
        this.auditService = auditService;
    }

    public Single<List<Tag>> getAllTags() {
        return bookRepository.getAllTags()
                .firstOrError()
                .subscribeOn(Schedulers.io());
    }

    public Single<Tag> getTagById(long tagId) {
        return bookRepository.getTagById(tagId)
                .subscribeOn(Schedulers.io());
    }

    /** Add a tag and return Completable (ignores generated ID). */
    public Completable addTag(Tag tag) {
        return bookRepository.insertTag(tag)
                .doOnSuccess(id -> auditService.log("TAG_ADDED", "Tag added: " + tag.getName()))
                .ignoreElement()
                .subscribeOn(Schedulers.io());
    }

    public Completable updateTag(Tag tag) {
        return bookRepository.updateTag(tag)
                .doOnComplete(() -> auditService.log("TAG_UPDATED", "Tag updated: " + tag.getName()))
                .subscribeOn(Schedulers.io());
    }

    public Completable deleteTag(Tag tag) {
        return bookRepository.deleteTag(tag)
                .doOnComplete(() -> auditService.log("TAG_DELETED", "Tag deleted: " + tag.getName()))
                .subscribeOn(Schedulers.io());
    }

    public Single<List<Book>> getBooksForTag(long tagId) {
        return bookRepository.getBooksByTag(tagId)
                .firstOrError()
                .subscribeOn(Schedulers.io());
    }
}