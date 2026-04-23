package com.librelibraria.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.librelibraria.LibreLibrariaApp;
import com.librelibraria.data.model.Book;
import com.librelibraria.data.model.Tag;
import com.librelibraria.data.service.TagService;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * ViewModel for Tags screen.
 */
public class TagsViewModel extends AndroidViewModel {

    private final TagService tagService;
    private final CompositeDisposable disposables;

    private final MutableLiveData<List<Tag>> tags = new MutableLiveData<>();
    private final MutableLiveData<List<Tag>> filteredTags = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> tagSaved = new MutableLiveData<>();
    private final MutableLiveData<Boolean> tagDeleted = new MutableLiveData<>();

    private String currentSearchQuery = "";

    public TagsViewModel(@NonNull Application application) {
        super(application);

        LibreLibrariaApp app = (LibreLibrariaApp) application;
        tagService = app.getTagService();
        disposables = new CompositeDisposable();

        loadTags();
    }

    public void loadTags() {
        isLoading.setValue(true);

        disposables.add(
            tagService.getAllTags()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    tagList -> {
                        tags.setValue(tagList);
                        filteredTags.setValue(tagList);
                        isLoading.setValue(false);
                    },
                    error -> {
                        errorMessage.setValue("Failed to load tags: " + error.getMessage());
                        isLoading.setValue(false);
                    }
                )
        );
    }

    public void addTag(String name) {
        if (name == null || name.trim().isEmpty()) {
            errorMessage.setValue("Tag name cannot be empty");
            return;
        }

        Tag tag = new Tag(name.trim());
        tag.setBookCount(0);
        tag.setLastModified(System.currentTimeMillis());

        disposables.add(
            tagService.addTag(tag)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> {
                        tagSaved.setValue(true);
                        loadTags();
                    },
                    error -> errorMessage.setValue("Failed to add tag: " + error.getMessage())
                )
        );
    }

    public void updateTag(Tag tag) {
        if (tag == null || tag.getName() == null || tag.getName().trim().isEmpty()) {
            errorMessage.setValue("Tag name cannot be empty");
            return;
        }

        tag.setName(tag.getName().trim());
        tag.setLastModified(System.currentTimeMillis());

        disposables.add(
            tagService.updateTag(tag)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> {
                        tagSaved.setValue(true);
                        loadTags();
                    },
                    error -> errorMessage.setValue("Failed to update tag: " + error.getMessage())
                )
        );
    }

    public void deleteTag(Tag tag) {
        disposables.add(
            tagService.deleteTag(tag)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> {
                        tagDeleted.setValue(true);
                        loadTags();
                    },
                    error -> errorMessage.setValue("Failed to delete tag: " + error.getMessage())
                )
        );
    }

    public void searchTags(String query) {
        currentSearchQuery = query != null ? query.toLowerCase() : "";

        List<Tag> allTags = tags.getValue();
        if (allTags == null) {
            filteredTags.setValue(new ArrayList<>());
            return;
        }

        if (currentSearchQuery.isEmpty()) {
            filteredTags.setValue(allTags);
        } else {
            List<Tag> filtered = new ArrayList<>();
            for (Tag tag : allTags) {
                if (tag.getName().toLowerCase().contains(currentSearchQuery)) {
                    filtered.add(tag);
                }
            }
            filteredTags.setValue(filtered);
        }
    }

    public void getTagById(long tagId) {
        disposables.add(
            tagService.getTagById(tagId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    tag -> {
                        // Could emit this to a LiveData if needed
                    },
                    error -> errorMessage.setValue("Tag not found")
                )
        );
    }

    public void getBooksForTag(long tagId) {
        disposables.add(
            tagService.getBooksForTag(tagId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    books -> {
                        // Update book count for this tag
                        Tag tag = findTagById(tagId);
                        if (tag != null) {
                            tag.setBookCount(books.size());
                            tagService.updateTag(tag).blockingAwait();
                        }
                    },
                    error -> {}
                )
        );
    }

    private Tag findTagById(long tagId) {
        List<Tag> allTags = tags.getValue();
        if (allTags != null) {
            for (Tag tag : allTags) {
                if (tag.getId() == tagId) {
                    return tag;
                }
            }
        }
        return null;
    }

    // Getters
    public LiveData<List<Tag>> getTags() {
        return tags;
    }

    public LiveData<List<Tag>> getFilteredTags() {
        return filteredTags;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getTagSaved() {
        return tagSaved;
    }

    public LiveData<Boolean> getTagDeleted() {
        return tagDeleted;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
