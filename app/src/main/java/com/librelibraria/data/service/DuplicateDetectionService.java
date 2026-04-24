package com.librelibraria.data.service;

import android.content.Context;
import com.librelibraria.data.model.Book;
import com.librelibraria.data.storage.FileStorageManager;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DuplicateDetectionService {

    private final FileStorageManager storageManager;
    private final Context context;

    public DuplicateDetectionService(Context context) {
        this.context = context.getApplicationContext();
        this.storageManager = FileStorageManager.getInstance(context);
    }

    public Single<List<DuplicateGroup>> findDuplicates() {
        return Single.fromCallable(() -> {
            List<Book> books = storageManager.loadAllBooks();
            List<DuplicateGroup> duplicates = new ArrayList<>();
            
            // Map by ISBN (exact match)
            Map<String, List<Book>> byIsbn = books.stream()
                .filter(b -> b.getIsbn() != null && !b.getIsbn().isEmpty())
                .collect(Collectors.groupingBy(b -> b.getIsbn().trim().toUpperCase()));
            
            for (Map.Entry<String, List<Book>> entry : byIsbn.entrySet()) {
                if (entry.getValue().size() > 1) {
                    duplicates.add(new DuplicateGroup("ISBN", entry.getKey(), entry.getValue()));
                }
            }
            
            // Map by normalized title+author (fuzzy match)
            Map<String, List<Book>> byTitle = books.stream()
                .filter(b -> b.getTitle() != null && !b.getTitle().isEmpty())
                .collect(Collectors.groupingBy(b -> normalizeTitleAuthor(b)));
            
            for (Map.Entry<String, List<Book>> entry : byTitle.entrySet()) {
                if (entry.getValue().size() > 1 && !isISBNDuplicate(entry.getKey(), duplicates)) {
                    duplicates.add(new DuplicateGroup("Title+Author", entry.getKey(), entry.getValue()));
                }
            }
            
            // Sort by number of duplicates (largest first)
            duplicates.sort((a, b) -> Integer.compare(b.books.size(), a.books.size()));
            
            return duplicates;
        }).subscribeOn(Schedulers.io());
    }

    public Single<List<DuplicateGroup>> findSimilarBooks(String query) {
        return Single.fromCallable(() -> {
            List<Book> books = storageManager.loadAllBooks();
            List<DuplicateGroup> similar = new ArrayList<>();
            
            String normalizedQuery = normalize(query);
            
            for (Book book : books) {
                String title = book.getTitle() != null ? book.getTitle() : "";
                String author = book.getAuthor() != null ? book.getAuthor() : "";
                
                if (normalize(title).contains(normalizedQuery) || 
                    normalize(author).contains(normalizedQuery)) {
                    List<Book> group = new ArrayList<>();
                    group.add(book);
                    similar.add(new DuplicateGroup("Search", title + " by " + author, group));
                }
            }
            
            similar.sort((a, b) -> Integer.compare(b.books.size(), a.books.size()));
            return similar;
        }).subscribeOn(Schedulers.io());
    }

    private String normalizeTitleAuthor(Book book) {
        String title = book.getTitle() != null ? book.getTitle().trim().toUpperCase() : "";
        String author = book.getAuthor() != null ? book.getAuthor().trim().toUpperCase() : "";
        // Remove common words
        title = title.replaceAll("(THE|A|AN)\\s+", "");
        return title + "|" + author;
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.trim().toLowerCase()
            .replaceAll("[^a-z0-9]", "")
            .replaceAll("\\s+", "");
    }

    private boolean isISBNDuplicate(String key, List<DuplicateGroup> duplicates) {
        for (DuplicateGroup d : duplicates) {
            if ("ISBN".equals(d.matchType)) {
                return true;
            }
        }
        return false;
    }

    public static class DuplicateGroup {
        public final String matchType;
        public final String matchValue;
        public final List<Book> books;

        public DuplicateGroup(String matchType, String matchValue, List<Book> books) {
            this.matchType = matchType;
            this.matchValue = matchValue;
            this.books = books;
        }

        public int getCount() {
            return books.size();
        }
    }
}