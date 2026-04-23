package com.librelibraria.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.librelibraria.LibreLibrariaApp;
import com.librelibraria.data.model.Book;
import com.librelibraria.data.repository.BookRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * ViewModel for Catalog screen.
 */
public class CatalogViewModel extends AndroidViewModel {

    private final BookRepository bookRepository;
    private final CompositeDisposable disposables;

    private final MutableLiveData<List<Book>> allBooks = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> genres = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    private String currentSearchQuery = "";
    private String currentGenreFilter = "";

    public CatalogViewModel(@NonNull Application application) {
        super(application);

        LibreLibrariaApp app = (LibreLibrariaApp) application;
        bookRepository = app.getBookRepository();
        disposables = new CompositeDisposable();

        loadBooks();
        loadGenres();
    }

    public LiveData<List<Book>> getBooks() {
        return allBooks;
    }

    public LiveData<List<String>> getGenres() {
        return genres;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void refresh() {
        loadBooks();
        loadGenres();
    }

    public void searchBooks(String query) {
        currentSearchQuery = query;
        applyFilters();
    }

    public void filterByGenre(String genre) {
        currentGenreFilter = genre != null ? genre : "";
        applyFilters();
    }

    private void loadBooks() {
        isLoading.setValue(true);

        disposables.add(
            bookRepository.getAllBooks()
                .firstOrError()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    books -> {
                        allBooks.setValue(books);
                        isLoading.setValue(false);
                        applyFilters();
                    },
                    error -> {
                        isLoading.setValue(false);
                    }
                )
        );
    }

    private void loadGenres() {
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

    private void applyFilters() {
        List<Book> filtered = new ArrayList<>();
        List<Book> source = allBooks.getValue();

        if (source == null) {
            allBooks.setValue(new ArrayList<>());
            return;
        }

        for (Book book : source) {
            boolean matchesSearch = currentSearchQuery.isEmpty() ||
                    book.getTitle().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                    (book.getAuthor() != null && book.getAuthor().toLowerCase().contains(currentSearchQuery.toLowerCase())) ||
                    (book.getIsbn() != null && book.getIsbn().contains(currentSearchQuery));

            boolean matchesGenre = currentGenreFilter.isEmpty() ||
                    (book.getGenre() != null && book.getGenre().equalsIgnoreCase(currentGenreFilter));

            if (matchesSearch && matchesGenre) {
                filtered.add(book);
            }
        }

        allBooks.setValue(filtered);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
