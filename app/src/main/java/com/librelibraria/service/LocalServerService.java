package com.librelibraria.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.librelibraria.LibreLibrariaApp;
import com.librelibraria.R;
import com.librelibraria.data.database.AppDatabase;
import com.librelibraria.ui.activities.MainActivity;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fi.iki.elonen.NanoHTTPD;

/**
 * Local HTTP server service for peer-to-peer sync.
 * Allows other LibreLibraria instances to sync with this device.
 */
public class LocalServerService extends Service {

    public static final int PORT = 8080;
    public static final String ACTION_START = "com.librelibraria.START_SERVER";
    public static final String ACTION_STOP = "com.librelibraria.STOP_SERVER";

    private static final int NOTIFICATION_ID = 1001;

    private LibreServer server;
    private ExecutorService executor;
    private AppDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        database = AppDatabase.getInstance(this);
        executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_START.equals(action)) {
                startServer();
            } else if (ACTION_STOP.equals(action)) {
                stopServer();
            }
        }
        return START_STICKY;
    }

    private void startServer() {
        startForeground(NOTIFICATION_ID, createNotification());

        try {
            server = new LibreServer(PORT);
            executor.execute(() -> {
                try {
                    server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
    }

    private void stopServer() {
        if (server != null) {
            server.stop();
            server = null;
        }
        stopForeground(true);
        stopSelf();
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        Intent stopIntent = new Intent(this, LocalServerService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(
                this, 0, stopIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, LibreLibrariaApp.CHANNEL_SERVER)
                .setContentTitle(getString(R.string.server_running))
                .setContentText(getString(R.string.server_port, PORT))
                .setSmallIcon(R.drawable.ic_server)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_stop, getString(R.string.stop), stopPendingIntent)
                .setOngoing(true)
                .build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopServer();
        if (executor != null) {
            executor.shutdown();
        }
    }

    /**
     * NanoHTTPD server implementation for LibreLibraria sync.
     */
    private class LibreServer extends NanoHTTPD {

        public LibreServer(int port) throws IOException {
            super(port);
        }

        @Override
        public Response serve(IHTTPSession session) {
            String uri = session.getUri();
            String method = session.getMethod().name();

            try {
                // API routing
                if (uri.startsWith("/api/")) {
                    return handleApiRequest(session, uri, method);
                }

                // Default response
                return newFixedLengthResponse(
                        Response.Status.OK,
                        "application/json",
                        "{\"status\":\"LibreLibraria Server Running\",\"version\":\"1.0\"}"
                );
            } catch (Exception e) {
                return newFixedLengthResponse(
                        Response.Status.INTERNAL_ERROR,
                        "application/json",
                        "{\"error\":\"" + e.getMessage() + "\"}"
                );
            }
        }

        private Response handleApiRequest(IHTTPSession session, String uri, String method) {
            // Simplified API handling
            if (uri.equals("/api/books") && method.equals("GET")) {
                return getAllBooks();
            } else if (uri.equals("/api/sync") && method.equals("POST")) {
                return syncData(session);
            } else if (uri.equals("/api/health") && method.equals("GET")) {
                return healthCheck();
            }

            return newFixedLengthResponse(
                    Response.Status.NOT_FOUND,
                    "application/json",
                    "{\"error\":\"Endpoint not found\"}"
            );
        }

        private Response getAllBooks() {
            try {
                // Fetch books from database
                // This is simplified - real implementation would use async queries
                String json = "[]"; // Placeholder
                return newFixedLengthResponse(
                        Response.Status.OK,
                        "application/json",
                        json
                );
            } catch (Exception e) {
                return newFixedLengthResponse(
                        Response.Status.INTERNAL_ERROR,
                        "application/json",
                        "{\"error\":\"" + e.getMessage() + "\"}"
                );
            }
        }

        private Response syncData(IHTTPSession session) {
            try {
                // Process incoming sync data
                return newFixedLengthResponse(
                        Response.Status.OK,
                        "application/json",
                        "{\"success\":true,\"synced\":0}"
                );
            } catch (Exception e) {
                return newFixedLengthResponse(
                        Response.Status.INTERNAL_ERROR,
                        "application/json",
                        "{\"error\":\"" + e.getMessage() + "\"}"
                );
            }
        }

        private Response healthCheck() {
            return newFixedLengthResponse(
                    Response.Status.OK,
                    "application/json",
                    "{\"healthy\":true,\"serverTime\":" + System.currentTimeMillis() + "}"
            );
        }
    }
}
