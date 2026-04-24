package com.librelibraria.data.model;

/**
 * Reading Challenge model - tracks yearly/monthly reading goals
 */
public class Challenge {
    private long id;
    private String title;
    private int goalBooks;
    private int goalPages;
    private long startDate;
    private long endDate;
    private int completedBooks;
    private int completedPages;
    private boolean active;
    private String status; // ACTIVE, COMPLETED, FAILED

    public Challenge() {
        this.startDate = System.currentTimeMillis();
        this.active = true;
        this.status = "ACTIVE";
        this.completedBooks = 0;
        this.completedPages = 0;
    }

    public Challenge(long id, String title, int goalBooks, int goalPages, long endDate) {
        this();
        this.id = id;
        this.title = title;
        this.goalBooks = goalBooks;
        this.goalPages = goalPages;
        this.endDate = endDate;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getGoalBooks() { return goalBooks; }
    public void setGoalBooks(int goalBooks) { this.goalBooks = goalBooks; }

    public int getGoalPages() { return goalPages; }
    public void setGoalPages(int goalPages) { this.goalPages = goalPages; }

    public long getStartDate() { return startDate; }
    public void setStartDate(long startDate) { this.startDate = startDate; }

    public long getEndDate() { return endDate; }
    public void setEndDate(long endDate) { this.endDate = endDate; }

    public int getCompletedBooks() { return completedBooks; }
    public void setCompletedBooks(int completedBooks) { this.completedBooks = completedBooks; }

    public int getCompletedPages() { return completedPages; }
    public void setCompletedPages(int completedPages) { this.completedPages = completedPages; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getProgressPercent() {
        if (goalBooks <= 0) return 0;
        return Math.min(100, (completedBooks * 100) / goalBooks);
    }

    public boolean isCompleted() {
        return completedBooks >= goalBooks;
    }

    public void incrementBook(int pages) {
        this.completedBooks++;
        this.completedPages += pages;
        if (isCompleted()) {
            this.status = "COMPLETED";
            this.active = false;
        }
    }
}