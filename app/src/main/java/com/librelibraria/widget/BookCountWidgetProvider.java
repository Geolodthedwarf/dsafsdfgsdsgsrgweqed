package com.librelibraria.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import com.librelibraria.R;
import com.librelibraria.data.database.AppDatabase;
import com.librelibraria.ui.activities.MainActivity;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class BookCountWidgetProvider extends AppWidgetProvider {

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

        AppDatabase db = AppDatabase.getInstance(context);
        currentDisposable = db.bookDao().getTotalCount()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(count -> {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_book_count);
                views.setTextViewText(R.id.widget_book_count, String.valueOf(count));
                views.setTextViewText(R.id.widget_title, context.getString(R.string.app_name));

                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("tab", "catalog");
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                views.setOnClickPendingIntent(R.id.widget_book_count, pendingIntent);

                appWidgetManager.updateAppWidget(appWidgetId, views);
            }, error -> {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_book_count);
                views.setTextViewText(R.id.widget_book_count, "0");
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