package com.librelibraria.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.librelibraria.LibreLibrariaApp;
import com.librelibraria.data.database.DiaryDao;
import com.librelibraria.data.model.DiaryEntry;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * ViewModel for the DiaryActivity, managing diary entry data and business logic.
 */
public class DiaryViewModel extends AndroidViewModel {

    private final DiaryDao diaryDao;
    private final ExecutorService executor;
    private final CompositeDisposable disposables;

    private final MutableLiveData<java.util.List<DiaryEntry>> diaryEntries = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private long filterStartDate = 0;
    private long filterEndDate = Long.MAX_VALUE;

    public DiaryViewModel(@NonNull Application application) {
        super(application);
        LibreLibrariaApp app = (LibreLibrariaApp) application;
        diaryDao = app.getDatabase().diaryDao();
        executor = Executors.newSingleThreadExecutor();
        disposables = new CompositeDisposable();

        loadEntries();
    }

    /**
     * Load all diary entries from the database.
     */
    public void loadEntries() {
        isLoading.setValue(true);

        Single<java.util.List<DiaryEntry>> single;
        if (filterStartDate > 0) {
            single = diaryDao.getEntriesBetweenDates(filterStartDate, filterEndDate)
                    .firstOrError();
        } else {
            single = diaryDao.getAllEntries()
                    .firstOrError();
        }

        disposables.add(single
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        entries -> {
                            diaryEntries.setValue(entries);
                            isLoading.setValue(false);
                        },
                        throwable -> {
                            errorMessage.setValue("Failed to load entries: " + throwable.getMessage());
                            isLoading.setValue(false);
                        }
                ));
    }

    /**
     * Add a new diary entry.
     */
    public void addEntry(DiaryEntry entry) {
        entry.setLastModified(System.currentTimeMillis());
        entry.setSynced(false);

        disposables.add(diaryDao.insert(entry)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> loadEntries(),
                        throwable -> errorMessage.setValue("Failed to add entry: " + throwable.getMessage())
                ));
    }

    /**
     * Update an existing diary entry.
     */
    public void updateEntry(DiaryEntry entry) {
        entry.setLastModified(System.currentTimeMillis());
        entry.setSynced(false);

        disposables.add(diaryDao.update(entry)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> loadEntries(),
                        throwable -> errorMessage.setValue("Failed to update entry: " + throwable.getMessage())
                ));
    }

    /**
     * Delete a diary entry.
     */
    public void deleteEntry(DiaryEntry entry) {
        disposables.add(diaryDao.delete(entry)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::loadEntries,
                        throwable -> errorMessage.setValue("Failed to delete entry: " + throwable.getMessage())
                ));
    }

    /**
     * Filter entries by date range.
     */
    public void filterByDate(long startDate) {
        this.filterStartDate = startDate;
        this.filterEndDate = Long.MAX_VALUE;
        loadEntries();
    }

    /**
     * Filter entries between specific dates.
     */
    public void filterByDateRange(long startDate, long endDate) {
        this.filterStartDate = startDate;
        this.filterEndDate = endDate;
        loadEntries();
    }

    /**
     * Clear the date filter.
     */
    public void clearFilter() {
        this.filterStartDate = 0;
        this.filterEndDate = Long.MAX_VALUE;
        loadEntries();
    }

    /**
     * Get diary entries for a specific book.
     */
    public void getEntriesForBook(long bookId) {
        isLoading.setValue(true);

        disposables.add(diaryDao.getEntriesForBook(bookId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        entries -> {
                            diaryEntries.setValue(entries);
                            isLoading.setValue(false);
                        },
                        throwable -> {
                            errorMessage.setValue("Failed to load entries: " + throwable.getMessage());
                            isLoading.setValue(false);
                        }
                ));
    }

    /**
     * Get entries for statistics.
     */
    public void getEntriesForStatistics(long startDate, long endDate) {
        isLoading.setValue(true);

        disposables.add(diaryDao.getEntriesBetweenDates(startDate, endDate)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        entries -> {
                            diaryEntries.setValue(entries);
                            isLoading.setValue(false);
                        },
                        throwable -> {
                            errorMessage.setValue("Failed to load statistics: " + throwable.getMessage());
                            isLoading.setValue(false);
                        }
                ));
    }

    // Getters for LiveData
    public LiveData<java.util.List<DiaryEntry>> getDiaryEntries() {
        return diaryEntries;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
        executor.shutdown();
    }
}
