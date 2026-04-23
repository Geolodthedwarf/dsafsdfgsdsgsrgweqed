package com.librelibraria;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.librelibraria.data.database.AppDatabase;
import com.librelibraria.data.repository.BookRepository;
import com.librelibraria.data.repository.LoanRepository;
import com.librelibraria.data.repository.SettingsRepository;
import com.librelibraria.data.service.AuditService;
import com.librelibraria.data.service.AutomationService;
import com.librelibraria.data.service.CatalogService;
import com.librelibraria.data.service.DiaryService;
import com.librelibraria.data.service.LendingService;
import com.librelibraria.data.service.RatingService;
import com.librelibraria.data.service.SettingsParityService;
import com.librelibraria.data.service.StatsService;
import com.librelibraria.data.service.TagService;
import com.librelibraria.data.sync.SyncManager;
import com.librelibraria.data.api.ApiClient;
import com.librelibraria.data.api.ApiService;

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
    private AuditService auditService;
    private CatalogService catalogService;
    private LendingService lendingService;
    private RatingService ratingService;
    private DiaryService diaryService;
    private TagService tagService;
    private SettingsParityService settingsParityService;
    private StatsService statsService;
    private AutomationService automationService;

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
        auditService = new AuditService(database.auditLogDao());
        catalogService = new CatalogService(bookRepository, auditService);
        lendingService = new LendingService(loanRepository, bookRepository, auditService);
        ratingService = new RatingService(bookRepository, auditService);
        diaryService = new DiaryService(database.diaryDao(), auditService);
        tagService = new TagService(bookRepository, auditService);
        settingsParityService = new SettingsParityService(database.appSettingDao(), settingsRepository);
        statsService = new StatsService(database.bookDao(), database.loanDao(), database.borrowerDao(), database.auditLogDao());
        automationService = new AutomationService(bookRepository, loanRepository, auditService);

        // Initialize API client
        apiClient = ApiService.createApiClient(settingsRepository.getServerUrl());

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

    public AuditService getAuditService() {
        return auditService;
    }

    public CatalogService getCatalogService() {
        return catalogService;
    }

    public LendingService getLendingService() {
        return lendingService;
    }

    public RatingService getRatingService() {
        return ratingService;
    }

    public DiaryService getDiaryService() {
        return diaryService;
    }

    public TagService getTagService() {
        return tagService;
    }

    public SettingsParityService getSettingsParityService() {
        return settingsParityService;
    }

    public StatsService getStatsService() {
        return statsService;
    }

    public AutomationService getAutomationService() {
        return automationService;
    }
}
