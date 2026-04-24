package com.librelibraria.data.service;

import com.librelibraria.data.database.AuditLogDao;
import com.librelibraria.data.database.BookDao;
import com.librelibraria.data.database.BorrowerDao;
import com.librelibraria.data.database.LoanDao;
import com.librelibraria.data.model.Book;
import com.librelibraria.data.model.Loan;
import com.librelibraria.data.model.ReadingStatus;
import com.librelibraria.data.model.Statistics;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Service for generating and managing library statistics.
 */
public class StatsService {

    private final BookDao bookDao;
    private final LoanDao loanDao;
    private final BorrowerDao borrowerDao;

    public StatsService(BookDao bookDao, LoanDao loanDao, BorrowerDao borrowerDao) {
        this.bookDao = bookDao;
        this.loanDao = loanDao;
        this.borrowerDao = borrowerDao;
    }

    /**
     * Get overall library statistics.
     */
    public Single<Statistics> getOverallStats() {
        return Single.zip(
                bookDao.getTotalCount().onErrorReturnItem(0),
                bookDao.getAvailableCount().onErrorReturnItem(0),
                loanDao.getActiveLoansCount().onErrorReturnItem(0),
                loanDao.getOverdueLoansCount(System.currentTimeMillis()).onErrorReturnItem(0),
                borrowerDao.getTotalCount().onErrorReturnItem(0),
                (totalBooks, availableBooks, activeLoans, overdueLoans, totalBorrowers) -> {
                    Statistics stats = new Statistics();
                    stats.setTotalBooks(totalBooks);
                    stats.setAvailableBooks(availableBooks);
                    stats.setOnLoanBooks(totalBooks - availableBooks);
                    stats.setActiveLoans(activeLoans);
                    stats.setOverdueLoans(overdueLoans);
                    stats.setTotalBorrowers(totalBorrowers);
                    return stats;
                }
        ).subscribeOn(Schedulers.io());
    }

    /**
     * Get dashboard statistics (alias for getOverallStats).
     */
    public Single<Statistics> dashboardStats() {
        return getOverallStats();
    }

    /**
     * Get book statistics by reading status.
     */
    public Single<Map<String, Integer>> getBooksByStatus() {
        return bookDao.getAllBooks()
                .firstOrError()
                .map(books -> {
                    Map<String, Integer> statusCount = new HashMap<>();
                    for (Book book : books) {
                        ReadingStatus status = book.getReadingStatus();
                        String statusName = status != null ? status.name() : "UNKNOWN";
                        statusCount.put(statusName, statusCount.getOrDefault(statusName, 0) + 1);
                    }
                    return statusCount;
                })
                .onErrorReturnItem(new HashMap<>())
                .subscribeOn(Schedulers.io());
    }

    /**
     * Get lending statistics for a period.
     */
    public Single<Map<String, Integer>> getLendingStats(long startDate, long endDate) {
        return loanDao.getLoansBetweenDates(startDate, endDate)
                .map(loans -> {
                    Map<String, Integer> stats = new HashMap<>();
                    int total = loans.size();
                    int returned = 0;
                    int active = 0;
                    int overdue = 0;
                    long now = System.currentTimeMillis();

                    for (Loan loan : loans) {
                        if ("RETURNED".equals(loan.getStatus())) {
                            returned++;
                        } else {
                            active++;
                            if (loan.getDueDate() < now) {
                                overdue++;
                            }
                        }
                    }

                    stats.put("total", total);
                    stats.put("returned", returned);
                    stats.put("active", active);
                    stats.put("overdue", overdue);
                    return stats;
                })
                .onErrorReturnItem(new HashMap<>())
                .subscribeOn(Schedulers.io());
    }

    /**
     * Get books added in a period.
     */
    public Single<Integer> getBooksAddedInPeriod(long startDate, long endDate) {
        return bookDao.getBooksAddedBetween(startDate, endDate)
                .map(books -> books.size())
                .onErrorReturnItem(0)
                .subscribeOn(Schedulers.io());
    }

    /**
     * Get most active borrowers.
     */
    public Single<java.util.List<java.util.Map<String, Object>>> getMostActiveBorrowers(int limit) {
        return borrowerDao.getAllBorrowers()
                .firstOrError()
                .map(borrowers -> {
                    java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
                    for (var borrower : borrowers) {
                        java.util.Map<String, Object> map = new HashMap<>();
                        map.put("borrower", borrower);
                        // Count loans would be done by joining, simplified here
                        map.put("totalLoans", 0);
                        result.add(map);
                    }
                    return result;
                })
                .onErrorReturnItem(new java.util.ArrayList<>())
                .subscribeOn(Schedulers.io());
    }

    /**
     * Get monthly activity summary.
     */
    public Single<Map<String, Integer>> getMonthlyActivity(int year, int month) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(year, month - 1, 1, 0, 0, 0);
        long startDate = cal.getTimeInMillis();
        cal.add(java.util.Calendar.MONTH, 1);
        long endDate = cal.getTimeInMillis();

        return Single.zip(
                bookDao.getBooksAddedBetween(startDate, endDate)
                        .map(books -> books.size())
                        .onErrorReturnItem(0),
                loanDao.getLoansBetweenDates(startDate, endDate)
                        .map(loans -> loans.size())
                        .onErrorReturnItem(0),
                (booksAdded, loansCreated) -> {
                    Map<String, Integer> monthly = new HashMap<>();
                    monthly.put("booksAdded", booksAdded);
                    monthly.put("loansCreated", loansCreated);
                    monthly.put("activities", 0);
                    return monthly;
                }
        ).subscribeOn(Schedulers.io());
    }

    /**
     * Get average loan duration in days.
     */
    public Single<Double> getAverageLoanDuration() {
        return loanDao.getReturnedLoans()
                .map(loans -> {
                    if (loans.isEmpty()) return 0.0;
                    long totalDays = 0;
                    int count = 0;
                    for (Loan loan : loans) {
                        if (loan.getReturnDate() > 0 && loan.getLoanDate() > 0) {
                            long duration = loan.getReturnDate() - loan.getLoanDate();
                            totalDays += duration / (24 * 60 * 60 * 1000);
                            count++;
                        }
                    }
                    return count > 0 ? (double) totalDays / count : 0.0;
                })
                .onErrorReturnItem(0.0)
                .subscribeOn(Schedulers.io());
    }
}
