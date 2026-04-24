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
import com.librelibraria.ui.activities.BookDetailActivity;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;

public class CurrentlyReadingWidgetProvider extends AppWidgetProvider {

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
        currentDisposable = catalogService.getBooksByStatus("READING")
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(books -> {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_currently_reading);

                if (books != null && !books.isEmpty()) {
                    com.librelibraria.data.model.Book book = books.get(0);
                    views.setTextViewText(R.id.widget_book_title, book.getTitle() != null ? book.getTitle() : "Untitled");
                    views.setTextViewText(R.id.widget_book_author, book.getAuthor() != null ? book.getAuthor() : "Unknown Author");

                    int progress = 0;
                    if (book.getReadCount() > 0 && book.getCopies() > 0) {
                        progress = Math.min(100, book.getReadCount() * 100 / book.getCopies());
                    }
                    views.setInt(R.id.widget_reading_progress, "setProgress", progress);

                    if (book.getCustomCoverUrl() != null && !book.getCustomCoverUrl().isEmpty()) {
                        views.setImageViewUri(R.id.widget_book_cover, android.net.Uri.parse(book.getCustomCoverUrl()));
                    }

                    Intent intent = new Intent(context, BookDetailActivity.class);
                    intent.putExtra("book_id", book.getId());
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) book.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                    views.setOnClickPendingIntent(R.id.widget_book_cover, pendingIntent);
                    views.setOnClickPendingIntent(R.id.widget_book_title, pendingIntent);
                } else {
                    views.setTextViewText(R.id.widget_book_title, "No book in progress");
                    views.setTextViewText(R.id.widget_book_author, "Tap to add a book");
                    views.setInt(R.id.widget_reading_progress, "setProgress", 0);
                    views.setImageViewResource(R.id.widget_book_cover, R.drawable.ic_library);

                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra("tab", "catalog");
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                    views.setOnClickPendingIntent(R.id.widget_book_cover, pendingIntent);
                }

                appWidgetManager.updateAppWidget(appWidgetId, views);
            }, error -> {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_currently_reading);
                views.setTextViewText(R.id.widget_book_title, "Error loading");
                views.setImageViewResource(R.id.widget_book_cover, R.drawable.ic_library);
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