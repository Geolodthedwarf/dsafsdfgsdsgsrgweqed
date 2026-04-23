package com.librelibraria.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.librelibraria.LibreLibrariaApp;
import com.librelibraria.data.model.Book;
import com.librelibraria.data.model.Loan;
import com.librelibraria.data.model.LoanStatus;
import com.librelibraria.data.repository.BookRepository;
import com.librelibraria.data.repository.LoanRepository;
import com.librelibraria.data.service.LendingService;
import com.librelibraria.data.service.RatingService;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * ViewModel for Book Detail screen.
 */
public class BookDetailViewModel extends AndroidViewModel {

    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;
    private final RatingService ratingService;
    private final LendingService lendingService;
    private final CompositeDisposable disposables;

    private final MutableLiveData<Book> book = new MutableLiveData<>();
    private final MutableLiveData<List<Loan>> loans = new MutableLiveData<>();
    private final MutableLiveData<Loan> activeLoan = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> deleteSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>();

    public BookDetailViewModel(@NonNull Application application) {
        super(application);

        LibreLibrariaApp app = (LibreLibrariaApp) application;
        bookRepository = app.getBookRepository();
        loanRepository = app.getLoanRepository();
        ratingService = app.getRatingService();
        lendingService = app.getLendingService();
        disposables = new CompositeDisposable();
    }

    public LiveData<Book> getBook() {
        return book;
    }

    public LiveData<List<Loan>> getLoans() {
        return loans;
    }

    public LiveData<Loan> getActiveLoan() {
        return activeLoan;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getDeleteSuccess() {
        return deleteSuccess;
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
                        loadLoans(bookId);
                        isLoading.setValue(false);
                    },
                    error -> isLoading.setValue(false)
                )
        );
    }

    private void loadLoans(long bookId) {
        disposables.add(
            loanRepository.getLoansForBook(bookId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    loanList -> {
                        loans.setValue(loanList);
                        // Find active loan
                        for (Loan loan : loanList) {
                            if (loan.getStatus() == LoanStatus.ACTIVE) {
                                activeLoan.setValue(loan);
                                break;
                            }
                        }
                    },
                    error -> {}
                )
        );
    }

    public void deleteBook() {
        Book currentBook = book.getValue();
        if (currentBook == null) return;

        isLoading.setValue(true);

        disposables.add(
            bookRepository.deleteBook(currentBook)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> {
                        isLoading.setValue(false);
                        deleteSuccess.setValue(true);
                    },
                    error -> {
                        isLoading.setValue(false);
                        deleteSuccess.setValue(false);
                    }
                )
        );
    }

    public void saveRating(double rating, String review) {
        Book currentBook = book.getValue();
        if (currentBook == null) return;

        disposables.add(
            ratingService.saveRating(currentBook.getId(), rating, review, false)
                .andThen(bookRepository.getBookById(currentBook.getId()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    updatedBook -> {
                        book.setValue(updatedBook);
                        saveSuccess.setValue(true);
                    },
                    error -> saveSuccess.setValue(false)
                )
        );
    }

    public void returnBook() {
        Loan loan = activeLoan.getValue();
        if (loan == null) return;

        Book currentBook = book.getValue();
        if (currentBook == null) return;

        disposables.add(
            lendingService.returnBook(loan.getId())
                .andThen(bookRepository.getBookById(currentBook.getId()))
                .flatMapCompletable(bookToUpdate -> {
                    // LendingService already updates availability; only refresh local view model state.
                    return io.reactivex.rxjava3.core.Completable.complete();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> {
                        loadBook(currentBook.getId());
                        saveSuccess.setValue(true);
                    },
                    error -> saveSuccess.setValue(false)
                )
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
