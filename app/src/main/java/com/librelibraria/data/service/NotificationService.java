package com.librelibraria.data.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.librelibraria.R;
import com.librelibraria.data.model.Loan;
import com.librelibraria.ui.activities.MainActivity;
import java.util.List;

public class NotificationService {

    private static final String CHANNEL_REMINDERS = "loan_reminders";
    private static final String CHANNEL_OVERDUE = "loan_overdue";
    private static final int NOTIFY_REMINDER = 1001;
    private static final int NOTIFY_OVERDUE = 1002;

    private final Context context;
    private final LendingService lendingService;

    public NotificationService(Context context) {
        this.context = context.getApplicationContext();
        this.lendingService = new LendingService(context);
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            
            NotificationChannel reminderChannel = new NotificationChannel(
                CHANNEL_REMINDERS,
                "Loan Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            reminderChannel.setDescription("Notifications for upcoming book returns");
            
            NotificationChannel overdueChannel = new NotificationChannel(
                CHANNEL_OVERDUE,
                "Overdue Books",
                NotificationManager.IMPORTANCE_HIGH
            );
            overdueChannel.setDescription("Notifications for overdue books");
            
            if (manager != null) {
                manager.createNotificationChannel(reminderChannel);
                manager.createNotificationChannel(overdueChannel);
            }
        }
    }

    public void checkAndNotifyLoans() {
        new Thread(() -> {
            try {
                List<Loan> loans = lendingService.getAllLoans().blockingGet();
                long now = System.currentTimeMillis();
                long threeDays = now + (3L * 24 * 60 * 60 * 1000);
                long oneDay = now + (1L * 24 * 60 * 60 * 1000);
                
                int reminderCount = 0;
                int overdueCount = 0;
                
                for (Loan loan : loans) {
                    if (loan.getStatus() != com.librelibraria.data.model.LoanStatus.ACTIVE) {
                        continue;
                    }
                    
                    if (loan.getDueDate() < now) {
                        overdueCount++;
                    } else if (loan.getDueDate() < threeDays) {
                        reminderCount++;
                    }
                }
                
                if (overdueCount > 0) {
                    showOverdueNotification(overdueCount);
                } else if (reminderCount > 0) {
                    showReminderNotification(reminderCount);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showReminderNotification(int count) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_library)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(count + " book(s) due within 3 days")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFY_REMINDER, builder.build());
        }
    }

    private void showOverdueNotification(int count) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_OVERDUE)
            .setSmallIcon(R.drawable.ic_library)
            .setContentTitle(context.getString(R.string.overdue_books))
            .setContentText(count + " book(s) overdue!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFY_OVERDUE, builder.build());
        }
    }

    public void cancelAllNotifications() {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(NOTIFY_REMINDER);
            manager.cancel(NOTIFY_OVERDUE);
        }
    }
}