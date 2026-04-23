package com.librelibraria.data.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * DiaryEntry entity representing a reading diary note.
 */
@Entity(
    tableName = "diary_entries",
    foreignKeys = @ForeignKey(
        entity = Book.class,
        parentColumns = "id",
        childColumns = "bookId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = @Index("bookId")
)
public class DiaryEntry {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long bookId;
    private String bookTitle;
    private long date;
    private String note;
    private String quote;
    private int pageNumber;
    private long lastModified;
    private boolean isSynced;

    public DiaryEntry() {
        this.date = System.currentTimeMillis();
        this.lastModified = System.currentTimeMillis();
        this.isSynced = false;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getBookId() { return bookId; }
    public void setBookId(long bookId) { this.bookId = bookId; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public long getDate() { return date; }
    public void setDate(long date) { this.date = date; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getQuote() { return quote; }
    public void setQuote(String quote) { this.quote = quote; }

    public int getPageNumber() { return pageNumber; }
    public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }

    public long getLastModified() { return lastModified; }
    public void setLastModified(long lastModified) { this.lastModified = lastModified; }

    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { isSynced = synced; }

    public String getPreview(int maxLength) {
        if (note == null) return "";
        if (note.length() <= maxLength) return note;
        return note.substring(0, maxLength) + "...";
    }
}
