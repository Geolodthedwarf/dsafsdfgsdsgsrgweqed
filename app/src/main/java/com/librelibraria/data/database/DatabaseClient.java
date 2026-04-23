package com.librelibraria.data.database;

import android.content.Context;

/**
 * Singleton wrapper for accessing the database instance.
 * Provides thread-safe access to the AppDatabase.
 */
public class DatabaseClient {

    private static volatile DatabaseClient INSTANCE;
    private final AppDatabase database;

    private DatabaseClient(Context context) {
        database = AppDatabase.getInstance(context.getApplicationContext());
    }

    public static DatabaseClient getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (DatabaseClient.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DatabaseClient(context);
                }
            }
        }
        return INSTANCE;
    }

    public AppDatabase getDatabase() {
        return database;
    }

    public BookDao getBookDao() {
        return database.bookDao();
    }

    public BorrowerDao getBorrowerDao() {
        return database.borrowerDao();
    }

    public LoanDao getLoanDao() {
        return database.loanDao();
    }

    public TagDao getTagDao() {
        return database.tagDao();
    }

    public DiaryDao getDiaryDao() {
        return database.diaryDao();
    }

    public AuditLogDao getAuditLogDao() {
        return database.auditLogDao();
    }

    public AppSettingDao getAppSettingDao() {
        return database.appSettingDao();
    }
}
