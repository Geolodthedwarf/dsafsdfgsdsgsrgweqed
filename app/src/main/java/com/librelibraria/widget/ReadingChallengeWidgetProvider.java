package com.librelibraria.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import com.librelibraria.R;
import com.librelibraria.data.model.Book;
import com.librelibraria.data.service.CatalogService;
import com.librelibraria.data.service.ChallengeService;
import com.librelibraria.ui.activities.MainActivity;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ReadingChallengeWidgetProvider extends AppWidgetProvider {

    private static Disposable currentDisposable;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    public static void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        if (currentDisposable != null && !currentDisposable.isDisposed()) {
            currentDisposable.dispose();
        }

        ChallengeService challengeService = new ChallengeService(context);
        CatalogService catalogService = new CatalogService(context);

        currentDisposable = challengeService.getActiveChallenge()
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap(challenge -> catalogService.getAllBooks())
            .subscribe(books -> {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_reading_challenge);

                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                int booksThisYear = 0;
                long challengeGoal = 12;

                if (books != null) {
                    for (Book book : books) {
                        String status = book.getStatus();
                        if ("READ".equals(status) && book.getLastModified() > 0) {
                            java.util.Calendar cal = java.util.Calendar.getInstance();
                            cal.setTimeInMillis(book.getLastModified());
                            if (cal.get(java.util.Calendar.YEAR) == currentYear) {
                                booksThisYear++;
                            }
                        }
                    }
                }

                int percentage = (int) ((booksThisYear * 100) / Math.max(1, challengeGoal));

                views.setTextViewText(R.id.widget_challenge_progress, booksThisYear + "/" + challengeGoal);
                views.setInt(R.id.widget_challenge_bar, "setProgress", percentage);

                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("tab", "challenges");
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                views.setOnClickPendingIntent(R.id.widget_challenge_progress, pendingIntent);
                views.setOnClickPendingIntent(R.id.widget_challenge_bar, pendingIntent);

                appWidgetManager.updateAppWidget(appWidgetId, views);
            }, error -> {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_reading_challenge);
                views.setTextViewText(R.id.widget_challenge_progress, "0/12");
                appWidgetManager.updateAppWidget(appWidgetId, views);
            });
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        if (currentDisposable != null && !currentDisposable.isDisposed()) {
            currentDisposable.dispose();
        }
    }
}