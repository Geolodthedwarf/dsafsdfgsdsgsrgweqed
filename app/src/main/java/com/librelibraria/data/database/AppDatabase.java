package com.librelibraria.data.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.librelibraria.data.model.AuditLog;
import com.librelibraria.data.model.Book;
import com.librelibraria.data.model.Borrower;
import com.librelibraria.data.model.DiaryEntry;
import com.librelibraria.data.model.Loan;
import com.librelibraria.data.model.Tag;

import java.util.concurrent.Executors;

/**
 * Room database for LibreLibraria app.
 */
@Database(
    entities = {
        Book.class,
        Borrower.class,
        Loan.class,
        Tag.class,
        DiaryEntry.class,
        AuditLog.class
    },
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "librelibraria_db";
    private static volatile AppDatabase INSTANCE;

    public abstract BookDao bookDao();
    public abstract BorrowerDao borrowerDao();
    public abstract LoanDao loanDao();
    public abstract TagDao tagDao();
    public abstract DiaryDao diaryDao();
    public abstract AuditLogDao auditLogDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME
                        )
                        .addCallback(new Callback() {
                            @Override
                            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                super.onCreate(db);
                                // Populate with default tags on first creation
                                Executors.newSingleThreadExecutor().execute(() -> {
                                    populateDefaultData(getInstance(context));
                                });
                            }
                        })
                        .fallbackToDestructiveMigration()
                        .build();
                }
            }
        }
        return INSTANCE;
    }

    private static void populateDefaultData(AppDatabase database) {
        // Add default tags
        TagDao tagDao = database.tagDao();
        String[] defaultTags = {
            "Fiction", "Non-Fiction", "Science", "History", "Biography",
            "Fantasy", "Mystery", "Romance", "Self-Help", "Technology"
        };

        for (String tagName : defaultTags) {
            Tag tag = new Tag(tagName);
            tagDao.insert(tag).blockingGet();
        }
    }

    public void clearAllTables() {
        clearAllTables();
    }
}
