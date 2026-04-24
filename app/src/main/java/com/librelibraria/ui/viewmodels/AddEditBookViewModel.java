package com.librelibraria.ui.viewmodels;

import android.app.Application;
import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.librelibraria.LibreLibrariaApp;
import com.librelibraria.R;
import com.librelibraria.data.model.Book;
import com.librelibraria.data.model.Tag;
import com.librelibraria.data.service.CatalogService;
import com.librelibraria.data.service.HybridLibraryService;
import com.librelibraria.data.service.TagService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * ViewModel for Add/Edit Book screen.
 */
public class AddEditBookViewModel extends AndroidViewModel {

    private final CatalogService catalogService;
    private final TagService tagService;
    private final HybridLibraryService hybridLibraryService;
    private final CompositeDisposable disposables;

    private final MutableLiveData<Book> book = new MutableLiveData<>();
    private final MutableLiveData<List<String>> genres = new MutableLiveData<>();
    private final MutableLiveData<List<Tag>> tags = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Book> lookupResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isSearchEnabled = new MutableLiveData<>(false);

    public AddEditBookViewModel(@NonNull Application application) {
        super(application);

        LibreLibrariaApp app = (LibreLibrariaApp) application;
        catalogService = app.getCatalogService();
        tagService = app.getTagService();
        hybridLibraryService = app.getHybridLibraryService();
        disposables = new CompositeDisposable();

        loadGenresAndTags();
    }

    public LiveData<Book> getBook() {
        return book;
    }

    public LiveData<List<String>> getGenres() {
        return genres;
    }

    public LiveData<List<Tag>> getTags() {
        return tags;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getSaveSuccess() {
        return saveSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Book> getLookupResult() {
        return lookupResult;
    }

    public LiveData<Boolean> getIsSearchEnabled() {
        return isSearchEnabled;
    }

    public void loadBook(long bookId) {
        isLoading.setValue(true);
        disposables.add(
            appBookRepo().getBookById(bookId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    result -> {
                        isLoading.setValue(false);
                        if (result != null) {
                            book.setValue(result);
                        } else {
                            errorMessage.setValue("Book not found");
                        }
                    },
                    error -> {
                        isLoading.setValue(false);
                        errorMessage.setValue("Load failed: " + error.getMessage());
                    }
                )
        );
    }

    private com.librelibraria.data.repository.BookRepository appBookRepo() {
        LibreLibrariaApp app = (LibreLibrariaApp) getApplication();
        return app.getBookRepository();
    }

    public LiveData<String> getLookupError() {
        return errorMessage;
    }

    public void loadGenresAndTags() {
        try {
            Resources resources = getApplication().getResources();
            String[] genreArray = resources.getStringArray(R.array.genres);
            genres.setValue(new ArrayList<>(Arrays.asList(genreArray)));
        } catch (Exception e) {
            genres.setValue(new ArrayList<>());
        }

        disposables.add(
            tagService.getAllTags()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    tagList -> tags.setValue(tagList),
                    error -> tags.setValue(new ArrayList<>())
                )
        );
    }

    public void setBook(Book bookToEdit) {
        book.setValue(bookToEdit);
    }

    public void saveBook(Book bookToSave) {
        isLoading.setValue(true);
        Disposable d = hybridLibraryService.saveBook(bookToSave)
            .ignoreElement()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                () -> {
                    isLoading.setValue(false);
                    saveSuccess.setValue(true);
                },
                error -> {
                    android.util.Log.e("AddEditBookVM", "Error saving book", error);
                    isLoading.setValue(false);
                    saveSuccess.setValue(false);
                }
            );
        disposables.add(d);
    }

    public void searchByIsbn(String isbn) {
        if (isbn == null || isbn.isEmpty()) {
            errorMessage.setValue("ISBN cannot be empty");
            return;
        }

        isLoading.setValue(true);
        disposables.add(
            catalogService.searchByIsbn(isbn)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    result -> {
                        if (result != null && result.getTitle() != null && !result.getTitle().isEmpty()) {
                            lookupResult.setValue(result);
                            isLoading.setValue(false);
                        } else {
                            catalogService.searchOnlineByIsbn(isbn)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                    onlineResult -> {
                                        isLoading.setValue(false);
                                        if (onlineResult != null && onlineResult.getTitle() != null) {
                                            lookupResult.setValue(onlineResult);
                                        } else {
                                            errorMessage.setValue("Book not found");
                                        }
                                    },
                                    error -> {
                                        isLoading.setValue(false);
                                        errorMessage.setValue("Book not found");
                                    }
                                );
                        }
                    },
                    error -> {
                        catalogService.searchOnlineByIsbn(isbn)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                onlineResult -> {
                                    isLoading.setValue(false);
                                    if (onlineResult != null && onlineResult.getTitle() != null) {
                                        lookupResult.setValue(onlineResult);
                                    } else {
                                        errorMessage.setValue("Book not found");
                                    }
                                },
                                error2 -> {
                                    isLoading.setValue(false);
                                    errorMessage.setValue("Book not found");
                                }
                            );
                    }
                )
        );
    }

    public void searchByTitle(String title) {
        if (title == null || title.isEmpty()) {
            errorMessage.setValue("Title cannot be empty");
            return;
        }

        isLoading.setValue(true);
        disposables.add(
            catalogService.searchByTitle(title)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    result -> {
                        isLoading.setValue(false);
                        if (result != null) {
                            lookupResult.setValue(result);
                        } else {
                            errorMessage.setValue("Book not found");
                        }
                    },
                    error -> {
                        isLoading.setValue(false);
                        errorMessage.setValue("Search failed: " + error.getMessage());
                    }
                )
        );
    }

    public void lookupIsbn(String isbn) {
        searchByIsbn(isbn);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}