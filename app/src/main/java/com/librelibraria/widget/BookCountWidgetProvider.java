package com.librelibraria.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import com.librelibraria.R;
import com.librelibraria.data.service.CatalogService;
import com.librelibraria.ui.activities.MainActivity;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;

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

        CatalogService catalogService = new CatalogService(context);
        currentDisposable = catalogService.getAllBooks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(books -> {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_book_count);
                int count = (books != null) ? books.size() : 0;
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