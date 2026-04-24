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
    private int activeLoans;
    private int totalBorrowers;
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
        this.activeLoans = 0;
        this.totalBorrowers = 0;
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

    public int getActiveLoans() { return activeLoans; }
    public void setActiveLoans(int activeLoans) { this.activeLoans = activeLoans; }

    public int getTotalBorrowers() { return totalBorrowers; }
    public void setTotalBorrowers(int totalBorrowers) { this.totalBorrowers = totalBorrowers; }

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

    // New setters for service compatibility
    public void setOnLoanBooks(int onLoanBooks) { this.borrowedBooks = onLoanBooks; }
    public void setActiveLoans(Integer activeLoans) { this.activeLoans = activeLoans != null ? activeLoans : 0; }
    public void setOverdueLoans(Integer overdueLoans) { this.overdueBooks = overdueLoans != null ? overdueLoans : 0; }
    public void setTotalBorrowers(Integer totalBorrowers) { this.totalBorrowers = totalBorrowers != null ? totalBorrowers : 0; }

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    public Map<String, Integer> getGenreDistribution() { return genreDistribution; }
    public void setGenreDistribution(Map<String, Integer> genreDistribution) { this.genreDistribution = genreDistribution; }

    public Map<String, Integer> getMonthlyAdditions() { return monthlyAdditions; }
    public void setMonthlyAdditions(Map<String, Integer> monthlyAdditions) { this.monthlyAdditions = monthlyAdditions; }

    public Map<Long, Integer> getTopBorrowers() { return topBorrowers; }
    public void setTopBorrowers(Map<Long, Integer> topBorrowers) { this.topBorrowers = topBorrowers; }

    /**
     * Statistics about reading status distribution.
     */
    public static class ReadingStatusStats {
        private int totalBooks;
        private int availableBooks;
        private int onLoanBooks;
        private int overdueBooks;
        private int lostBooks;

        public ReadingStatusStats() {}

        public ReadingStatusStats(int total, int available, int onLoan, int overdue, int lost) {
            this.totalBooks = total;
            this.availableBooks = available;
            this.onLoanBooks = onLoan;
            this.overdueBooks = overdue;
            this.lostBooks = lost;
        }

        public int getTotalBooks() { return totalBooks; }
        public void setTotalBooks(int totalBooks) { this.totalBooks = totalBooks; }

        public int getAvailableBooks() { return availableBooks; }
        public void setAvailableBooks(int availableBooks) { this.availableBooks = availableBooks; }

        public int getOnLoanBooks() { return onLoanBooks; }
        public void setOnLoanBooks(int onLoanBooks) { this.onLoanBooks = onLoanBooks; }

        public int getOverdueBooks() { return overdueBooks; }
        public void setOverdueBooks(int overdueBooks) { this.overdueBooks = overdueBooks; }

        public int getLostBooks() { return lostBooks; }
        public void setLostBooks(int lostBooks) { this.lostBooks = lostBooks; }
    }

    /**
     * Genre count data for bar chart.
     */
    public static class GenreCount {
        private String genre;
        private int count;

        public GenreCount() {}

        public GenreCount(String genre, int count) {
            this.genre = genre;
            this.count = count;
        }

        public String getGenre() { return genre; }
        public void setGenre(String genre) { this.genre = genre; }

        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
    }

    /**
     * Monthly activity count for bar chart.
     */
    public static class MonthlyCount {
        private String month;
        private int loansCount;
        private int returnsCount;

        public MonthlyCount() {}

        public MonthlyCount(String month, int loans, int returns) {
            this.month = month;
            this.loansCount = loans;
            this.returnsCount = returns;
        }

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }

        public int getLoansCount() { return loansCount; }
        public void setLoansCount(int loansCount) { this.loansCount = loansCount; }

        public int getReturnsCount() { return returnsCount; }
        public void setReturnsCount(int returnsCount) { this.returnsCount = returnsCount; }
    }
}
