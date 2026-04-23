package com.librelibraria.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.librelibraria.LibreLibrariaApp;
import com.librelibraria.data.model.Book;
import com.librelibraria.data.model.Tag;
import com.librelibraria.data.repository.BookRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * ViewModel for Add/Edit Book screen.
 */
public class AddEditBookViewModel extends AndroidViewModel {

    private final BookRepository bookRepository;
    private final CompositeDisposable disposables;

    private final MutableLiveData<Book> book = new MutableLiveData<>();
    private final MutableLiveData<List<String>> genres = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Tag>> tags = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>();

    public AddEditBookViewModel(@NonNull Application application) {
        super(application);

        LibreLibrariaApp app = (LibreLibrariaApp) application;
        bookRepository = app.getBookRepository();
        disposables = new CompositeDisposable();
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

    public void loadBook(long bookId) {
        isLoading.setValue(true);

        disposables.add(
            bookRepository.getBookById(bookId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    loadedBook -> {
                        book.setValue(loadedBook);
                        isLoading.setValue(false);
                    },
                    error -> {
                        isLoading.setValue(false);
                    }
                )
        );
    }

    public void loadGenres() {
        disposables.add(
            bookRepository.getAllGenres()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    genreList -> genres.setValue(genreList),
                    error -> {}
                )
        );
    }

    public void loadTags() {
        disposables.add(
            bookRepository.getAllTags()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    tagList -> tags.setValue(tagList),
                    error -> {}
                )
        );
    }

    public void saveBook(Book newBook) {
        isLoading.setValue(true);

        disposables.add(
            bookRepository.insertBook(newBook)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> {
                        isLoading.setValue(false);
                        saveSuccess.setValue(true);
                    },
                    error -> {
                        isLoading.setValue(false);
                        saveSuccess.setValue(false);
                    }
                )
        );
    }

    public void updateBook(Book updatedBook) {
        isLoading.setValue(true);

        disposables.add(
            bookRepository.updateBook(updatedBook)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> {
                        isLoading.setValue(false);
                        saveSuccess.setValue(true);
                    },
                    error -> {
                        isLoading.setValue(false);
                        saveSuccess.setValue(false);
                    }
                )
        );
    }

    public void lookupIsbn(String isbn) {
        // TODO: Implement Open Library API lookup
        // For now, this is a placeholder
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
