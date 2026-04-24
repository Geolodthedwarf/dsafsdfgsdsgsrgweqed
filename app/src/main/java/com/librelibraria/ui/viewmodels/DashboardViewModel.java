package com.librelibraria.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.librelibraria.LibreLibrariaApp;
import com.librelibraria.data.model.AuditLog;
import com.librelibraria.data.model.Book;
import com.librelibraria.data.model.Statistics;
import com.librelibraria.data.repository.BookRepository;
import com.librelibraria.data.repository.LoanRepository;
import com.librelibraria.data.service.AuditService;
import com.librelibraria.data.service.StatsService;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * ViewModel for Dashboard screen.
 */
public class DashboardViewModel extends AndroidViewModel {

    private final BookRepository bookRepository;
    private final StatsService statsService;
    private final AuditService auditService;
    private final CompositeDisposable disposables;

    private final MutableLiveData<Statistics> statistics = new MutableLiveData<>();
    private final MutableLiveData<List<Book>> recentBooks = new MutableLiveData<>();
    private final MutableLiveData<List<AuditLog>> recentActivity = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public DashboardViewModel(@NonNull Application application) {
        super(application);

        LibreLibrariaApp app = (LibreLibrariaApp) application;
        bookRepository = app.getBookRepository();
        statsService = app.getStatsService();
        auditService = app.getAuditService();
        disposables = new CompositeDisposable();

        loadData();
    }

    public LiveData<Statistics> getStatistics() {
        return statistics;
    }

    public LiveData<List<Book>> getRecentBooks() {
        return recentBooks;
    }

    public LiveData<List<AuditLog>> getRecentActivity() {
        return recentActivity;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void refresh() {
        loadData();
    }

    private void loadData() {
        isLoading.setValue(true);

        // Load statistics
        disposables.add(
            statsService.dashboardStats()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    stats -> {
                        statistics.setValue(stats);
                        isLoading.setValue(false);
                    },
                    error -> {
                        isLoading.setValue(false);
                    }
                )
        );

        // Load recent books
        disposables.add(
            bookRepository.getRecentlyAddedBooks(7)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    books -> recentBooks.setValue(books),
                    error -> recentBooks.setValue(null)
                )
        );

        // Load recent activity
        disposables.add(
            auditService.getRecentLogs(10)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    logs -> recentActivity.setValue(logs),
                    error -> recentActivity.setValue(null)
                )
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
