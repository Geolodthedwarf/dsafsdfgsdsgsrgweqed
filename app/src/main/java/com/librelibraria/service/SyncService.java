package com.librelibraria.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.librelibraria.LibreLibrariaApp;
import com.librelibraria.R;
import com.librelibraria.data.sync.SyncManager;
import com.librelibraria.ui.activities.MainActivity;

/**
 * Foreground service for background synchronization with PostgreSQL server.
 */
public class SyncService extends Service implements SyncManager.SyncCallback {

    public static final String ACTION_SYNC = "com.librelibraria.SYNC";
    public static final String ACTION_SYNC_NOW = "com.librelibraria.SYNC_NOW";

    private static final int NOTIFICATION_ID = 1002;

    private SyncManager syncManager;

    @Override
    public void onCreate() {
        super.onCreate();
        LibreLibrariaApp app = (LibreLibrariaApp) getApplication();
        syncManager = app.getSyncManager();
        syncManager.setCallback(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_SYNC.equals(action) || ACTION_SYNC_NOW.equals(action)) {
                startForeground(NOTIFICATION_ID, createNotification(getString(R.string.syncing)));
                startSync();
            }
        }
        return START_STICKY;
    }

    private void startSync() {
        if (syncManager.isSyncEnabled()) {
            syncManager.sync();
        } else {
            stopForeground(true);
            stopSelf();
        }
    }

    private Notification createNotification(String text) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, LibreLibrariaApp.CHANNEL_SYNC)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_sync)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setProgress(0, 0, true)
                .build();
    }

    private Notification createCompleteNotification(int syncedCount) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, LibreLibrariaApp.CHANNEL_SYNC)
                .setContentTitle(getString(R.string.sync_complete))
                .setContentText(getString(R.string.synced_count, syncedCount))
                .setSmallIcon(R.drawable.ic_sync)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
    }

    private Notification createErrorNotification(String error) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, LibreLibrariaApp.CHANNEL_SYNC)
                .setContentTitle(getString(R.string.sync_error))
                .setContentText(error)
                .setSmallIcon(R.drawable.ic_sync)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
    }

    @Override
    public void onSyncStart() {
        // Update notification to show syncing state
    }

    @Override
    public void onSyncComplete(int syncedCount) {
        stopForeground(true);
        stopSelf();

        // Show completion notification
        Notification notification = createCompleteNotification(syncedCount);
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public void onSyncError(String error) {
        stopForeground(true);
        stopSelf();

        // Show error notification
        Notification notification = createErrorNotification(error);
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public void onSyncStatusChanged(SyncManager.SyncStatus status) {
        // Handle status changes
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (syncManager != null) {
            syncManager.setCallback(null);
        }
    }
}
