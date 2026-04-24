package com.librelibraria.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.librelibraria.LibreLibrariaApp;
import com.librelibraria.data.model.Book;
import com.librelibraria.data.service.CatalogService;
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
    private final CatalogService catalogService;
    private final CompositeDisposable disposables;

    private final MutableLiveData<List<Book>> allBooks = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Book>> filteredBooks = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> genres = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    private String currentSearchQuery = "";
    private String currentGenreFilter = "";

    public CatalogViewModel(@NonNull Application application) {
        super(application);

        LibreLibrariaApp app = (LibreLibrariaApp) application;
        bookRepository = app.getBookRepository();
        catalogService = app.getCatalogService();
        disposables = new CompositeDisposable();

        loadBooks();
        loadGenres();
    }

    public LiveData<List<Book>> getBooks() {
        return filteredBooks;
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

    public void filterByStatus(String status) {
        currentGenreFilter = status != null ? status : "";
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
                        filteredBooks.setValue(books);
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
        List<Book> source = allBooks.getValue();
        if (source == null) {
            filteredBooks.setValue(new ArrayList<>());
            return;
        }

        String q = currentSearchQuery != null ? currentSearchQuery.trim().toLowerCase() : "";
        String genre = currentGenreFilter != null ? currentGenreFilter.trim() : "";

        List<Book> result = new ArrayList<>();
        for (Book b : source) {
            if (b == null) continue;

            boolean matchesQuery = true;
            if (!q.isEmpty()) {
                String title = b.getTitle() != null ? b.getTitle().toLowerCase() : "";
                String author = b.getAuthor() != null ? b.getAuthor().toLowerCase() : "";
                String isbn = b.getIsbn() != null ? b.getIsbn().toLowerCase() : "";
                matchesQuery = title.contains(q) || author.contains(q) || isbn.contains(q);
            }

            boolean matchesGenre = true;
            if (!genre.isEmpty()) {
                String bookGenre = b.getGenre() != null ? b.getGenre().trim() : "";
                matchesGenre = genre.equalsIgnoreCase(bookGenre);
            }

            if (matchesQuery && matchesGenre) {
                result.add(b);
            }
        }

        filteredBooks.setValue(result);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
