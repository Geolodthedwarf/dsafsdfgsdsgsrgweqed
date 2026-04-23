package com.librelibraria;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.librelibraria.data.database.AppDatabase;
import com.librelibraria.data.repository.BookRepository;
import com.librelibraria.data.repository.LoanRepository;
import com.librelibraria.data.repository.SettingsRepository;
import com.librelibraria.data.sync.SyncManager;
import com.librelibraria.data.api.ApiClient;

/**
 * Main Application class for LibreLibraria.
 * Provides application-wide singletons and initialization.
 */
public class LibreLibrariaApp extends Application {

    public static final String CHANNEL_SYNC = "sync_channel";
    public static final String CHANNEL_SERVER = "server_channel";

    private static LibreLibrariaApp instance;
    private AppDatabase database;
    private BookRepository bookRepository;
    private LoanRepository loanRepository;
    private SettingsRepository settingsRepository;
    private SyncManager syncManager;
    private ApiClient apiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Initialize database
        database = AppDatabase.getInstance(this);

        // Initialize repositories
        bookRepository = new BookRepository(database.bookDao(), database.tagDao());
        loanRepository = new LoanRepository(database.loanDao(), database.borrowerDao());
        settingsRepository = new SettingsRepository(this);

        // Initialize API client
        apiClient = new ApiClient();

        // Initialize sync manager
        syncManager = new SyncManager(database, apiClient, settingsRepository);

        // Create notification channels
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel syncChannel = new NotificationChannel(
                    CHANNEL_SYNC,
                    getString(R.string.channel_sync_name),
                    NotificationManager.IMPORTANCE_LOW
            );
            syncChannel.setDescription(getString(R.string.channel_sync_description));

            NotificationChannel serverChannel = new NotificationChannel(
                    CHANNEL_SERVER,
                    getString(R.string.channel_server_name),
                    NotificationManager.IMPORTANCE_LOW
            );
            serverChannel.setDescription(getString(R.string.channel_server_description));

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(syncChannel);
                manager.createNotificationChannel(serverChannel);
            }
        }
    }

    public static LibreLibrariaApp getInstance() {
        return instance;
    }

    public AppDatabase getDatabase() {
        return database;
    }

    public BookRepository getBookRepository() {
        return bookRepository;
    }

    public LoanRepository getLoanRepository() {
        return loanRepository;
    }

    public SettingsRepository getSettingsRepository() {
        return settingsRepository;
    }

    public SyncManager getSyncManager() {
        return syncManager;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }
}
