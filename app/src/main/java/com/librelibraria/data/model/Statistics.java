package com.librelibraria.data.model;

import java.util.Map;

/**
 * Statistics model for dashboard data.
 */
public class Statistics {

    private int totalBooks;
    private int availableBooks;
    private int borrowedBooks;
    private int overdueBooks;
    private int readingBooks;
    private int readBooks;
    private int ownBooks;
    private int wantBooks;
    private int stoppedBooks;
    private int processingBooks;
    private double averageRating;
    private Map<String, Integer> genreDistribution;
    private Map<String, Integer> monthlyAdditions;
    private Map<Long, Integer> topBorrowers;

    public Statistics() {
        this.totalBooks = 0;
        this.availableBooks = 0;
        this.borrowedBooks = 0;
        this.overdueBooks = 0;
        this.readingBooks = 0;
        this.readBooks = 0;
        this.ownBooks = 0;
        this.wantBooks = 0;
        this.stoppedBooks = 0;
        this.processingBooks = 0;
        this.averageRating = 0.0;
    }

    // Getters and Setters
    public int getTotalBooks() { return totalBooks; }
    public void setTotalBooks(int totalBooks) { this.totalBooks = totalBooks; }

    public int getAvailableBooks() { return availableBooks; }
    public void setAvailableBooks(int availableBooks) { this.availableBooks = availableBooks; }

    public int getBorrowedBooks() { return borrowedBooks; }
    public void setBorrowedBooks(int borrowedBooks) { this.borrowedBooks = borrowedBooks; }

    public int getOverdueBooks() { return overdueBooks; }
    public void setOverdueBooks(int overdueBooks) { this.overdueBooks = overdueBooks; }

    public int getReadingBooks() { return readingBooks; }
    public void setReadingBooks(int readingBooks) { this.readingBooks = readingBooks; }

    public int getReadBooks() { return readBooks; }
    public void setReadBooks(int readBooks) { this.readBooks = readBooks; }

    public int getOwnBooks() { return ownBooks; }
    public void setOwnBooks(int ownBooks) { this.ownBooks = ownBooks; }

    public int getWantBooks() { return wantBooks; }
    public void setWantBooks(int wantBooks) { this.wantBooks = wantBooks; }

    public int getStoppedBooks() { return stoppedBooks; }
    public void setStoppedBooks(int stoppedBooks) { this.stoppedBooks = stoppedBooks; }

    public int getProcessingBooks() { return processingBooks; }
    public void setProcessingBooks(int processingBooks) { this.processingBooks = processingBooks; }

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    public Map<String, Integer> getGenreDistribution() { return genreDistribution; }
    public void setGenreDistribution(Map<String, Integer> genreDistribution) { this.genreDistribution = genreDistribution; }

    public Map<String, Integer> getMonthlyAdditions() { return monthlyAdditions; }
    public void setMonthlyAdditions(Map<String, Integer> monthlyAdditions) { this.monthlyAdditions = monthlyAdditions; }

    public Map<Long, Integer> getTopBorrowers() { return topBorrowers; }
    public void setTopBorrowers(Map<Long, Integer> topBorrowers) { this.topBorrowers = topBorrowers; }
}
