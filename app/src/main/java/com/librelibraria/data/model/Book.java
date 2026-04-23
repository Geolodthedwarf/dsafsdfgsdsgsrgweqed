package com.librelibraria.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.librelibraria.data.database.Converters;

import java.util.List;

/**
 * Book entity representing a book in the library.
 */
@Entity(tableName = "books")
@TypeConverters(Converters.class)
public class Book {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String title;
    private String author;
    private String isbn;
    private String publisher;
    private String edition;
    private String publishYear;
    private String genre;
    private String language;
    private int copies;
    private int availableCopies;
    private String shelfLocation;
    private String tags;
    private String description;
    private long dateAdded;
    private double rating;
    private String review;
    private ReadingStatus readingStatus;
    private String quickNotes;
    private int readCount;
    private String customCoverUrl;
    private long lastModified;
    private boolean isSynced;
    private long localId;
    private String status; // "AVAILABLE", "ON_LOAN", etc.

    public Book() {
        this.dateAdded = System.currentTimeMillis();
        this.lastModified = System.currentTimeMillis();
        this.copies = 1;
        this.availableCopies = 1;
        this.language = "Ukrainian";
        this.readingStatus = ReadingStatus.OWN;
        this.isSynced = false;
        this.status = "AVAILABLE";
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public String getEdition() { return edition; }
    public void setEdition(String edition) { this.edition = edition; }

    public String getPublishYear() { return publishYear; }
    public void setPublishYear(String publishYear) { this.publishYear = publishYear; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public int getCopies() { return copies; }
    public void setCopies(int copies) { this.copies = copies; }

    public int getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(int availableCopies) { this.availableCopies = availableCopies; }

    public String getShelfLocation() { return shelfLocation; }
    public void setShelfLocation(String shelfLocation) { this.shelfLocation = shelfLocation; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getDateAdded() { return dateAdded; }
    public void setDateAdded(long dateAdded) { this.dateAdded = dateAdded; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }

    public ReadingStatus getReadingStatus() { return readingStatus; }
    public void setReadingStatus(ReadingStatus readingStatus) { this.readingStatus = readingStatus; }

    public String getQuickNotes() { return quickNotes; }
    public void setQuickNotes(String quickNotes) { this.quickNotes = quickNotes; }

    public int getReadCount() { return readCount; }
    public void setReadCount(int readCount) { this.readCount = readCount; }

    public String getCustomCoverUrl() { return customCoverUrl; }
    public void setCustomCoverUrl(String customCoverUrl) { this.customCoverUrl = customCoverUrl; }

    public long getLastModified() { return lastModified; }
    public void setLastModified(long lastModified) { this.lastModified = lastModified; }

    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { isSynced = synced; }

    public long getLocalId() { return localId; }
    public void setLocalId(long localId) { this.localId = localId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isAvailable() {
        return availableCopies > 0;
    }

    public String getTagsAsList() {
        if (tags == null || tags.isEmpty()) return "";
        return tags;
    }

    public String getRatingDisplay() {
        if (rating <= 0) return "";
        return String.format("%.1f/5", rating);
    }

    @Override
    public String toString() {
        return title + " by " + author;
    }
}
