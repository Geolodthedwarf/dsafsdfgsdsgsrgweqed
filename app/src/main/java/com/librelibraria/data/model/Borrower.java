package com.librelibraria.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Borrower entity representing a person who borrows books.
 */
@Entity(tableName = "borrowers")
public class Borrower {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;
    private String email;
    private String phone;
    private String notes;
    private int totalBorrowed;
    private long dateAdded;
    private long lastModified;
    private boolean isSynced;

    public Borrower() {
        this.dateAdded = System.currentTimeMillis();
        this.lastModified = System.currentTimeMillis();
        this.totalBorrowed = 0;
        this.isSynced = false;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public int getTotalBorrowed() { return totalBorrowed; }
    public void setTotalBorrowed(int totalBorrowed) { this.totalBorrowed = totalBorrowed; }

    public long getDateAdded() { return dateAdded; }
    public void setDateAdded(long dateAdded) { this.dateAdded = dateAdded; }

    public long getLastModified() { return lastModified; }
    public void setLastModified(long lastModified) { this.lastModified = lastModified; }

    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { isSynced = synced; }

    public void incrementBorrowed() {
        this.totalBorrowed++;
        this.lastModified = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return name;
    }
}
