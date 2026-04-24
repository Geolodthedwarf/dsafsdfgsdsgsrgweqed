package com.librelibraria.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.librelibraria.LibreLibrariaApp;
import com.librelibraria.data.model.Borrower;
import com.librelibraria.data.repository.LoanRepository;
import com.librelibraria.data.service.HybridLibraryService;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class BorrowersViewModel extends AndroidViewModel {

    private final LoanRepository loanRepository;
    private final HybridLibraryService hybridLibraryService;
    private final CompositeDisposable disposables;
    private final MutableLiveData<List<Borrower>> borrowers = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public BorrowersViewModel(@NonNull Application application) {
        super(application);
        LibreLibrariaApp app = (LibreLibrariaApp) application;
        loanRepository = app.getLoanRepository();
        hybridLibraryService = app.getHybridLibraryService();
        disposables = new CompositeDisposable();

        loadBorrowers();
    }

    public LiveData<List<Borrower>> getBorrowers() {
        return borrowers;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void refresh() {
        loadBorrowers();
    }

    public void addBorrower(Borrower borrower) {
        disposables.add(
            hybridLibraryService.saveBorrower(borrower)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    id -> loadBorrowers(),
                    error -> isLoading.setValue(false)
                )
        );
    }

    public void deleteBorrower(Borrower borrower) {
        if (borrower == null || borrower.getId() <= 0) return;
        disposables.add(
            hybridLibraryService.deleteBorrower(borrower.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> loadBorrowers(),
                    error -> {}
                )
        );
    }

    private void loadBorrowers() {
        isLoading.setValue(true);
        disposables.add(
            loanRepository.getAllBorrowers()
                .firstOrError()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    list -> {
                        borrowers.setValue(list);
                        isLoading.setValue(false);
                    },
                    error -> {
                        borrowers.setValue(new ArrayList<>());
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