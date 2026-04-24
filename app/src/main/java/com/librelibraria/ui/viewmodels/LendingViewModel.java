package com.librelibraria.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.librelibraria.LibreLibrariaApp;
import com.librelibraria.data.model.Book;
import com.librelibraria.data.model.Borrower;
import com.librelibraria.data.model.Loan;
import com.librelibraria.data.repository.BookRepository;
import com.librelibraria.data.repository.LoanRepository;
import com.librelibraria.data.service.HybridLibraryService;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * ViewModel for Lending screen.
 */
public class LendingViewModel extends AndroidViewModel {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final HybridLibraryService hybridLibraryService;
    private final CompositeDisposable disposables;

    private final MutableLiveData<List<Loan>> activeLoans = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Book>> availableBooks = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Borrower>> borrowers = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> activeLoansCount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> overdueLoansCount = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LendingViewModel(@NonNull Application application) {
        super(application);

        LibreLibrariaApp app = (LibreLibrariaApp) application;
        loanRepository = app.getLoanRepository();
        bookRepository = app.getBookRepository();
        hybridLibraryService = app.getHybridLibraryService();
        disposables = new CompositeDisposable();

        loadData();
    }

    public LiveData<List<Loan>> getActiveLoans() {
        return activeLoans;
    }

    public LiveData<List<Book>> getAvailableBooks() {
        return availableBooks;
    }

    public LiveData<List<Borrower>> getBorrowers() {
        return borrowers;
    }

    public LiveData<Integer> getActiveLoansCount() {
        return activeLoansCount;
    }

    public LiveData<Integer> getOverdueLoansCount() {
        return overdueLoansCount;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadData() {
        isLoading.setValue(true);

        disposables.add(
            loanRepository.getAllLoans()
                .firstOrError()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    loans -> {
                        List<Loan> active = new ArrayList<>();
                        int overdue = 0;
                        long now = System.currentTimeMillis();
                        for (Loan loan : loans) {
                            if (loan.getStatus() == com.librelibraria.data.model.LoanStatus.ACTIVE) {
                                active.add(loan);
                                if (loan.getDueDate() < now) {
                                    overdue++;
                                }
                            }
                        }
                        activeLoans.setValue(active);
                        activeLoansCount.setValue(active.size());
                        overdueLoansCount.setValue(overdue);
                        isLoading.setValue(false);
                    },
                    error -> {
                        errorMessage.setValue("Failed to load loans: " + error.getMessage());
                        isLoading.setValue(false);
                    }
                )
        );

        disposables.add(
            loanRepository.getAllBorrowers()
                .firstOrError()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    borrowerList -> {
                        borrowers.setValue(borrowerList);
                    },
                    error -> {
                        errorMessage.setValue("Failed to load borrowers: " + error.getMessage());
                    }
                )
        );

        disposables.add(
            bookRepository.getAvailableBooks()
                .firstOrError()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    availableBooks::setValue,
                    error -> {}
                )
        );
    }

    public void lendBook(long bookId, Long borrowerId, String borrowerName, long dueDate) {
        isLoading.setValue(true);

        disposables.add(
            hybridLibraryService.lendBook(bookId, borrowerId, borrowerName, dueDate)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    id -> {
                        isLoading.setValue(false);
                        loadData();
                    },
                    error -> {
                        errorMessage.setValue("Failed to lend book: " + error.getMessage());
                        isLoading.setValue(false);
                    }
                )
        );
    }

    public void returnBook(long loanId) {
        isLoading.setValue(true);

        disposables.add(
            hybridLibraryService.returnBook(loanId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> {
                        isLoading.setValue(false);
                        loadData();
                    },
                    error -> {
                        errorMessage.setValue("Failed to return book: " + error.getMessage());
                        isLoading.setValue(false);
                    }
                )
        );
    }

    public void addBorrower(String name, String email, String phone) {
        isLoading.setValue(true);

        Borrower borrower = new Borrower();
        borrower.setName(name);
        borrower.setEmail(email);
        borrower.setPhone(phone);

        disposables.add(
            hybridLibraryService.saveBorrower(borrower)
                .ignoreElement()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> {
                        isLoading.setValue(false);
                        loadData();
                    },
                    error -> {
                        errorMessage.setValue("Failed to add borrower: " + error.getMessage());
                        isLoading.setValue(false);
                    }
                )
        );
    }

    public void deleteBorrower(Borrower borrower) {
        disposables.add(
            hybridLibraryService.deleteBorrower(borrower.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> loadData(),
                    error -> errorMessage.setValue("Failed to delete borrower")
                )
        );
    }

    public void refresh() {
        loadData();
    }

    public void changeReturnBook(long loanId) {
        isLoading.setValue(true);

        disposables.add(
            hybridLibraryService.returnBook(loanId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> {
                        isLoading.setValue(false);
                        loadData();
                    },
                    error -> {
                        errorMessage.setValue("Failed to return book: " + error.getMessage());
                        isLoading.setValue(false);
                    }
                )
        );
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}