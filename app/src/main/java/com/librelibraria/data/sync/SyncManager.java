package com.librelibraria.data.sync;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.gson.Gson;
import com.librelibraria.LibreLibrariaApp;
import com.librelibraria.data.api.ApiClient;
import com.librelibraria.data.api.ApiService;
import com.librelibraria.data.database.AppDatabase;
import com.librelibraria.data.model.AuditLog;
import com.librelibraria.data.model.Book;
import com.librelibraria.data.model.Loan;
import com.librelibraria.data.repository.SettingsRepository;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Sync manager for synchronizing local database with PostgreSQL server.
 */
public class SyncManager {

    private final AppDatabase database;
    private final ApiClient apiClient;
    private final SettingsRepository settings;
    private final ExecutorService executor;
    private final CompositeDisposable disposables;
    private final Gson gson;

    private SyncStatus currentStatus = SyncStatus.IDLE;
    private long lastSyncTime = 0;

    public enum SyncStatus {
        IDLE,
        SYNCING,
        SUCCESS,
        ERROR,
        OFFLINE
    }

    public interface SyncCallback {
        void onSyncStart();
        void onSyncComplete(int syncedCount);
        void onSyncError(String error);
        void onSyncStatusChanged(SyncStatus status);
    }

    private SyncCallback callback;

    public SyncManager(AppDatabase database, ApiClient apiClient, SettingsRepository settings) {
        this.database = database;
        this.apiClient = apiClient;
        this.settings = settings;
        this.executor = Executors.newSingleThreadExecutor();
        this.disposables = new CompositeDisposable();
        this.gson = new Gson();
        this.lastSyncTime = settings.getLastSyncTime();
    }

    public void setCallback(SyncCallback callback) {
        this.callback = callback;
    }

    public SyncStatus getCurrentStatus() {
        return currentStatus;
    }

    public long getLastSyncTime() {
        return lastSyncTime;
    }

    public boolean isSyncEnabled() {
        return settings.isSyncEnabled();
    }

    public void sync() {
        if (currentStatus == SyncStatus.SYNCING) {
            return;
        }

        if (!isNetworkAvailable()) {
            updateStatus(SyncStatus.OFFLINE);
            if (callback != null) {
                callback.onSyncError("No network connection");
            }
            return;
        }

        String serverUrl = settings.getServerUrl();
        if (serverUrl == null || serverUrl.isEmpty()) {
            updateStatus(SyncStatus.ERROR);
            if (callback != null) {
                callback.onSyncError("Server URL not configured");
            }
            return;
        }

        updateStatus(SyncStatus.SYNCING);
        if (callback != null) {
            callback.onSyncStart();
        }

        // Create API client with configured server URL
        ApiClient client = ApiService.createApiClient(serverUrl);

        // Sync books
        disposables.add(
            syncBooks(client)
                .flatMap(synced -> syncLoans(client))
                .flatMap(synced -> {
                    lastSyncTime = System.currentTimeMillis();
                    settings.setLastSyncTime(lastSyncTime);
                    return Single.just(synced);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    count -> {
                        updateStatus(SyncStatus.SUCCESS);
                        if (callback != null) {
                            callback.onSyncComplete(count);
                        }
                    },
                    error -> {
                        updateStatus(SyncStatus.ERROR);
                        if (callback != null) {
                            callback.onSyncError(error.getMessage());
                        }
                    }
                )
        );
    }

    private Single<Integer> syncBooks(ApiClient client) {
        return Single.fromCallable(() -> {
            // Get unsynced local books
            List<Book> unsyncedBooks = database.bookDao().getUnsyncedBooks().blockingGet();
            int syncedCount = 0;

            for (Book book : unsyncedBooks) {
                try {
                    // Push to server
                    if (book.getId() > 0) {
                        client.updateBook(book.getId(), book).blockingGet();
                    } else {
                        client.createBook(book).blockingGet();
                    }
                    // Mark as synced
                    database.bookDao().markAsSynced(book.getId()).blockingAwait();
                    syncedCount++;
                } catch (Exception e) {
                    // Log error but continue
                }
            }

            // Pull latest from server
            try {
                List<Book> serverBooks = client.getAllBooks().blockingGet();
                for (Book serverBook : serverBooks) {
                    serverBook.setSynced(true);
                    try {
                        database.bookDao().insert(serverBook).blockingAwait();
                    } catch (Exception e) {
                        database.bookDao().update(serverBook).blockingAwait();
                    }
                }
            } catch (Exception e) {
                // Handle error
            }

            return syncedCount;
        });
    }

    private Single<Integer> syncLoans(ApiClient client) {
        return Single.fromCallable(() -> {
            List<Loan> unsyncedLoans = database.loanDao().getUnsyncedLoans().blockingGet();
            int syncedCount = 0;

            for (Loan loan : unsyncedLoans) {
                try {
                    if (loan.getId() > 0) {
                        client.updateLoan(loan.getId(), loan).blockingGet();
                    } else {
                        client.createLoan(loan).blockingGet();
                    }
                    database.loanDao().markAsSynced(loan.getId()).blockingAwait();
                    syncedCount++;
                } catch (Exception e) {
                    // Handle error
                }
            }

            return syncedCount;
        });
    }

    public Single<String> exportLibrary() {
        return Single.fromCallable(() -> {
            StringBuilder json = new StringBuilder();
            json.append("{\n");

            // Export books
            List<Book> books = database.bookDao().getUnsyncedBooks().blockingGet();
            json.append("\"books\":").append(gson.toJson(books)).append(",\n");

            // Export loans
            List<Loan> loans = database.loanDao().getUnsyncedLoans().blockingGet();
            json.append("\"loans\":").append(gson.toJson(loans)).append(",\n");

            json.append("\"exportDate\":\"").append(System.currentTimeMillis()).append("\"\n");
            json.append("}");

            return json.toString();
        }).subscribeOn(Schedulers.io());
    }

    public Single<Integer> importLibrary(String jsonData) {
        return Single.fromCallable(() -> {
            int imported = 0;

            try {
                // Parse and import books
                // Implementation would parse JSON and insert into database
                // This is a simplified version
                imported = 0;
            } catch (Exception e) {
                throw new RuntimeException("Import failed: " + e.getMessage());
            }

            return imported;
        }).subscribeOn(Schedulers.io());
    }

    private boolean isNetworkAvailable() {
        Context context = LibreLibrariaApp.getInstance();
        if (context == null) return false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private void updateStatus(SyncStatus status) {
        this.currentStatus = status;
        if (callback != null) {
            callback.onSyncStatusChanged(status);
        }
    }

    public void dispose() {
        disposables.clear();
        executor.shutdown();
    }
}
