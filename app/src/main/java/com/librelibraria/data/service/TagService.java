package com.librelibraria.data.service;

import android.content.Context;
import com.librelibraria.data.model.Book;
import com.librelibraria.data.model.Tag;
import com.librelibraria.data.storage.FileStorageManager;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.List;

public class TagService {

    private final FileStorageManager storageManager;
    private final Context context;

    public TagService(Context context) {
        this.context = context.getApplicationContext();
        this.storageManager = FileStorageManager.getInstance(context);
    }

    public Single<List<Tag>> getAllTags() {
        return Single.fromCallable(() -> storageManager.loadAllTags())
                .subscribeOn(Schedulers.io());
    }

    public Single<Tag> getTagById(long tagId) {
        return Single.fromCallable(() -> {
            List<Tag> tags = storageManager.loadAllTags();
            for (Tag tag : tags) {
                if (tag.getId() == tagId) {
                    return tag;
                }
            }
            return null;
        }).subscribeOn(Schedulers.io());
    }

    public Completable addTag(Tag tag) {
        return Completable.fromAction(() -> storageManager.saveTag(tag))
                .subscribeOn(Schedulers.io());
    }

    public Completable updateTag(Tag tag) {
        return Completable.fromAction(() -> storageManager.saveTag(tag))
                .subscribeOn(Schedulers.io());
    }

    public Completable deleteTag(Tag tag) {
        return Completable.fromAction(() -> {
            if (tag.getId() > 0) {
                java.io.File tagsDir = new java.io.File(storageManager.getBasePath(), "tags");
                java.io.File tagFile = new java.io.File(tagsDir, "tag_" + tag.getId() + ".bct");
                if (tagFile.exists()) {
                    tagFile.delete();
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    public Single<List<Book>> getBooksForTag(long tagId) {
        return Single.fromCallable(() -> {
            List<Book> allBooks = storageManager.loadAllBooks();
            List<Book> tagged = new java.util.ArrayList<>();
            for (Book b : allBooks) {
                if (b.getTags() != null && b.getTags().contains("tag_" + tagId)) {
                    tagged.add(b);
                }
            }
            return tagged;
        }).subscribeOn(Schedulers.io());
    }
}