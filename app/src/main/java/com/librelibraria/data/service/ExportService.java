package com.librelibraria.data.service;

import android.content.Context;
import android.os.Environment;
import com.librelibraria.data.model.Book;
import com.librelibraria.data.storage.FileStorageManager;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class ExportService {

    private final FileStorageManager storageManager;
    private final Context context;

    public ExportService(Context context) {
        this.context = context.getApplicationContext();
        this.storageManager = FileStorageManager.getInstance(context);
    }

    public Single<String> exportToCSV() {
        return Single.fromCallable(() -> {
            List<Book> books = storageManager.loadAllBooks();
            
            File exportsDir = new File(context.getExternalFilesDir(null), "exports");
            if (!exportsDir.exists()) exportsDir.mkdirs();
            
            String timestamp = String.valueOf(System.currentTimeMillis());
            File csvFile = new File(exportsDir, "library_export_" + timestamp + ".csv");
            
            FileWriter writer = new FileWriter(csvFile);
            
            // CSV Header
            writer.write("ID,Title,Author,ISBN,Publisher,Year,Genre,Language,Description,Copies,Available,Status,DateAdded\n");
            
            // CSV Data
            for (Book book : books) {
                writer.write(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d,%d,\"%s\",%d\n",
                    book.getId(),
                    escapeCSV(book.getTitle()),
                    escapeCSV(book.getAuthor()),
                    book.getIsbn() != null ? book.getIsbn() : "",
                    escapeCSV(book.getPublisher()),
                    book.getPublishYear() != null ? book.getPublishYear() : "",
                    escapeCSV(book.getGenre()),
                    escapeCSV(book.getLanguage()),
                    escapeCSV(book.getDescription()),
                    book.getCopies(),
                    book.getAvailableCopies(),
                    book.getStatus() != null ? book.getStatus() : "AVAILABLE",
                    book.getDateAdded()
                ));
            }
            
            writer.close();
            
            return csvFile.getAbsolutePath();
        }).subscribeOn(Schedulers.io());
    }

    public Single<String> exportToBCL() {
        return Single.fromCallable(() -> {
            boolean success = storageManager.exportToBCL("library_export");
            if (success) {
                String path = storageManager.getBasePath();
                File libraryDir = new File(path, "library");
                File[] files = libraryDir.listFiles((d, name) -> name.endsWith(".bcl"));
                if (files != null && files.length > 0) {
                    return files[files.length - 1].getAbsolutePath();
                }
            }
            return "";
        }).subscribeOn(Schedulers.io());
    }

    public Single<String> exportWishlist() {
        return Single.fromCallable(() -> {
            List<Book> books = storageManager.loadAllBooks();
            
            File exportsDir = new File(context.getExternalFilesDir(null), "exports");
            if (!exportsDir.exists()) exportsDir.mkdirs();
            
            String timestamp = String.valueOf(System.currentTimeMillis());
            File txtFile = new File(exportsDir, "wishlist_" + timestamp + ".txt");
            
            FileWriter writer = new FileWriter(txtFile);
            
            writer.write("=== MY WISHLIST ===\n\n");
            
            int count = 0;
            for (Book book : books) {
                if ("WANT".equals(book.getStatus())) {
                    count++;
                    writer.write(String.format("%d. %s by %s\n", 
                        count, 
                        book.getTitle(), 
                        book.getAuthor() != null ? book.getAuthor() : "Unknown"
                    ));
                    if (book.getIsbn() != null && !book.getIsbn().isEmpty()) {
                        writer.write("   ISBN: " + book.getIsbn() + "\n");
                    }
                    writer.write("\n");
                }
            }
            
            writer.write("\n=== END OF WISHLIST ===\n");
            writer.write("Total: " + count + " books\n");
            
            writer.close();
            
            return txtFile.getAbsolutePath();
        }).subscribeOn(Schedulers.io());
    }

    private String escapeCSV(String s) {
        if (s == null) return "";
        return s.replace("\"", "\"\"");
    }
}