package com.librelibraria.data.service;

import android.content.Context;
import com.librelibraria.data.model.Book;
import com.librelibraria.data.storage.FileStorageManager;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class CatalogService {

    private final FileStorageManager storageManager;
    private final Context context;

    public CatalogService(Context context) {
        this.context = context.getApplicationContext();
        this.storageManager = FileStorageManager.getInstance(context);
    }

public Completable addBook(Book book) {
        return Completable.fromAction(() -> {
            if (book.getId() == 0) {
                book.setId(System.currentTimeMillis());
            }
            storageManager.createBaseFolders();
            storageManager.saveBook(book);
        }).subscribeOn(Schedulers.io());
    }
    
    public Completable updateBook(Book book) {
        return Completable.fromAction(() -> {
            book.setLastModified(System.currentTimeMillis());
            storageManager.createBaseFolders();
            storageManager.saveBook(book);
        }).subscribeOn(Schedulers.io());
    }

    public Completable deleteBook(Book book) {
        return Completable.fromAction(() -> {
            if (book.getId() > 0) {
                java.io.File booksDir = new java.io.File(storageManager.getBasePath(), "books");
                java.io.File bookFile = new java.io.File(booksDir, "book_" + book.getId() + ".bcb");
                if (bookFile.exists()) bookFile.delete();
            }
        }).subscribeOn(Schedulers.io());
    }

    public Completable saveBook(Book book, boolean isUpdate) {
        return isUpdate ? updateBook(book) : addBook(book);
    }

    public Single<List<Book>> getAllBooks() {
        return Single.fromCallable(() -> storageManager.loadAllBooks())
                .subscribeOn(Schedulers.io());
    }

    public Single<List<Book>> loadCatalog(String searchQuery, String genreFilter, int page, int pageSize) {
        return getAllBooks();
    }

    public Single<List<Book>> searchBooks(String query) {
        return getAllBooks()
                .map(books -> {
                    if (query == null || query.isEmpty()) return books;
                    java.util.List<Book> filtered = new java.util.ArrayList<>();
                    String q = query.toLowerCase();
                    for (Book b : books) {
                        if (b.getTitle() != null && b.getTitle().toLowerCase().contains(q) ||
                            b.getAuthor() != null && b.getAuthor().toLowerCase().contains(q) ||
                            b.getIsbn() != null && b.getIsbn().contains(q)) {
                            filtered.add(b);
                        }
                    }
                    return filtered;
                });
    }

    public Single<Book> getBookById(long bookId) {
        return Single.fromCallable(() -> {
            List<Book> books = storageManager.loadAllBooks();
            for (Book book : books) {
                if (book.getId() == bookId) return book;
            }
            return null;
        }).subscribeOn(Schedulers.io());
    }

    public Single<List<Book>> getBooksByStatus(String status) {
        return getAllBooks()
                .map(books -> {
                    java.util.List<Book> filtered = new java.util.ArrayList<>();
                    for (Book b : books) {
                        if (status.equals(b.getStatus())) filtered.add(b);
                    }
                    return filtered;
                });
    }

    public Single<Integer> getAvailableBooksCount() {
        return getAllBooks()
                .map(books -> {
                    int count = 0;
                    for (Book b : books) {
                        if (b.getAvailableCopies() > 0) count++;
                    }
                    return count;
                });
    }

    public Single<Integer> getTotalBooksCount() {
        return getAllBooks()
                .map(List::size);
    }

    public Completable saveBook(Book bookToSave, Boolean isUpdate) {
        return Completable.fromAction(() -> storageManager.saveBook(bookToSave))
                .subscribeOn(Schedulers.io());
    }

    public Single<Book> searchByIsbn(String isbn) {
        return getAllBooks()
                .map(books -> {
                    for (Book b : books) {
                        if (b.getIsbn() != null && b.getIsbn().equals(isbn)) {
                            return b;
                        }
                    }
                    return (Book) null;
                })
                .onErrorResumeNext(error -> Single.just((Book) null))
                .subscribeOn(Schedulers.io());
    }
    
    public Single<Book> searchOnlineByIsbn(String isbn) {
        return Single.fromCallable(() -> {
            try {
                String cleanIsbn = isbn.replaceAll("[^0-9X]", "");
                if (cleanIsbn.length() < 10) {
                    android.util.Log.w("CatalogService", "ISBN too short: " + cleanIsbn);
                    return (Book) null;
                }
                
                String urlStr = "https://openlibrary.org/api/books?bibkeys=ISBN:" + isbn + "&format=json&jscmd=data";
                java.net.URL url = new java.net.URL(urlStr);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                conn.setRequestProperty("User-Agent", "LibreLibraria/1.0 (Android)");
                conn.setRequestProperty("Accept", "application/json");
                
                int responseCode = conn.getResponseCode();
                android.util.Log.d("CatalogService", "Response code: " + responseCode);
                
                if (responseCode == 200) {
                    java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(conn.getInputStream(), "UTF-8"));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    conn.disconnect();
                    
                    String responseStr = response.toString();
                    android.util.Log.d("CatalogService", "Response length: " + responseStr.length());
                    
                    String key = "ISBN:" + isbn;
                    if (responseStr.contains(key) && responseStr.length() > 10) {
                        Book book = new Book();
                        book.setIsbn(isbn);
                        parseOpenLibraryBook(responseStr, isbn, book);
                        return book;
                    } else {
                        android.util.Log.w("CatalogService", "No book found for key: " + key);
                    }
                } else {
                    android.util.Log.w("CatalogService", "HTTP error: " + responseCode);
                }
                conn.disconnect();
            } catch (Exception e) {
                android.util.Log.e("CatalogService", "Network error", e);
            }
            return (Book) null;
        }).subscribeOn(Schedulers.io());
    }
    
    private void parseOpenLibraryBook(String json, String isbn, Book book) {
        try {
            android.util.Log.d("CatalogService", "Parsing book for ISBN: " + isbn);
            
            JSONObject root = new JSONObject(json);
            String key = "ISBN:" + isbn;
            if (!root.has(key)) {
                android.util.Log.w("CatalogService", "Key not found: " + key);
                return;
            }
            
            Object bookDataObj = root.get(key);
            if (!(bookDataObj instanceof JSONObject)) {
                android.util.Log.w("CatalogService", "Not a JSON object");
                return;
            }
            
            JSONObject bookData = (JSONObject) bookDataObj;
            
            if (bookData.has("title") && !bookData.isNull("title")) {
                book.setTitle(bookData.getString("title"));
                android.util.Log.d("CatalogService", "Title: " + book.getTitle());
            }
            if (bookData.has("authors") && !bookData.isNull("authors")) {
                JSONArray authors = bookData.getJSONArray("authors");
                StringBuilder authorNames = new StringBuilder();
                for (int i = 0; i < authors.length(); i++) {
                    if (i > 0) authorNames.append(", ");
                    Object authorObj = authors.get(i);
                    if (authorObj instanceof JSONObject) {
                        authorNames.append(((JSONObject) authorObj).optString("name", ""));
                    } else {
                        authorNames.append(authorObj.toString());
                    }
                }
                if (authorNames.length() > 0) {
                    book.setAuthor(authorNames.toString());
                }
            }
            if (bookData.has("publishers") && !bookData.isNull("publishers")) {
                JSONArray publishers = bookData.getJSONArray("publishers");
                if (publishers.length() > 0) {
                    Object pubObj = publishers.get(0);
                    if (pubObj instanceof JSONObject) {
                        book.setPublisher(((JSONObject) pubObj).optString("name", ""));
                    }
                }
            }
            if (bookData.has("publish_date") && !bookData.isNull("publish_date")) {
                book.setPublishYear(bookData.getString("publish_date"));
            }
            if (bookData.has("number_of_pages") && !bookData.isNull("number_of_pages")) {
                book.setDescription(bookData.getString("number_of_pages") + " pages");
            }
            
            android.util.Log.d("CatalogService", "Parsed book: " + book.getTitle() + " by " + book.getAuthor());
        } catch (Exception e) {
            android.util.Log.e("CatalogService", "Parse error", e);
        }
    }

    public Single<Book> searchByTitle(String title) {
        return Single.fromCallable(() -> {
            List<Book> books = storageManager.loadAllBooks();
            for (Book b : books) {
                if (b.getTitle() != null && b.getTitle().equalsIgnoreCase(title)) {
                    return b;
                }
            }
            return null;
        }).subscribeOn(Schedulers.io());
    }

    public String getCoverUrl(String isbn) {
        if (isbn == null || isbn.isEmpty()) return null;
        return "https://covers.openlibrary.org/b/isbn/" + isbn + "-M.jpg";
    }

    public String getCoverUrlLarge(String isbn) {
        if (isbn == null || isbn.isEmpty()) return null;
        return "https://covers.openlibrary.org/b/isbn/" + isbn + "-L.jpg";
    }
}