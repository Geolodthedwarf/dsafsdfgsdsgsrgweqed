package com.librelibraria.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.librelibraria.LibreLibrariaApp;
import com.librelibraria.data.database.BookDao;
import com.librelibraria.data.database.LoanDao;
import com.librelibraria.data.database.BorrowerDao;
import com.librelibraria.data.model.Book;
import com.librelibraria.data.model.Loan;
import com.librelibraria.data.model.Borrower;
import com.librelibraria.data.model.Statistics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * ViewModel for Statistics screen.
 */
public class StatisticsViewModel extends AndroidViewModel {

    private final BookDao bookDao;
    private final LoanDao loanDao;
    private final BorrowerDao borrowerDao;
    private final CompositeDisposable disposables;

    private final MutableLiveData<Statistics> statistics = new MutableLiveData<>();
    private final MutableLiveData<Statistics.ReadingStatusStats> statusStats = new MutableLiveData<>();
    private final MutableLiveData<List<Statistics.GenreCount>> genreStats = new MutableLiveData<>();
    private final MutableLiveData<List<Statistics.MonthlyCount>> monthlyStats = new MutableLiveData<>();
    private final MutableLiveData<List<Borrower>> topBorrowers = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Integer> totalBooks = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> totalBorrowers = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> activeLoans = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> overdueLoans = new MutableLiveData<>(0);
    private final MutableLiveData<Double> averageRating = new MutableLiveData<>(0.0);

    public StatisticsViewModel(@NonNull Application application) {
        super(application);

        LibreLibrariaApp app = (LibreLibrariaApp) application;
        bookDao = app.getDatabase().bookDao();
        loanDao = app.getDatabase().loanDao();
        borrowerDao = app.getDatabase().borrowerDao();
        disposables = new CompositeDisposable();
    }

    public void loadAllStatistics() {
        isLoading.setValue(true);

        // Load reading status stats
        disposables.add(
            bookDao.getBookCount()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    count -> totalBooks.setValue(count),
                    error -> {}
                )
        );

        // Load borrower count
        disposables.add(
            borrowerDao.getBorrowerCount()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    count -> totalBorrowers.setValue(count),
                    error -> {}
                )
        );

        // Load active loans
        disposables.add(
            loanDao.getActiveLoansCount()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    count -> activeLoans.setValue(count),
                    error -> {}
                )
        );

        // Load overdue loans
        disposables.add(
            loanDao.getOverdueLoansCount(System.currentTimeMillis())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    count -> overdueLoans.setValue(count),
                    error -> {}
                )
        );

        // Load status breakdown
        loadStatusBreakdown();

        // Load genre distribution
        loadGenreDistribution();

        // Load monthly activity
        loadMonthlyActivity();

        // Load top borrowers
        loadTopBorrowers();

        isLoading.setValue(false);
    }

    private void loadStatusBreakdown() {
        disposables.add(
            bookDao.getAvailableBooks()
                .firstOrError()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    availableBooks -> {
                        Statistics.ReadingStatusStats stats = new Statistics.ReadingStatusStats();
                        stats.setAvailableBooks(availableBooks.size());
                        statusStats.setValue(stats);
                    },
                    error -> {}
                )
        );
    }

    public void loadGenreDistribution() {
        disposables.add(
            bookDao.getAllBooks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    books -> {
                        Map<String, Integer> genreCount = new HashMap<>();
                        for (Book book : books) {
                            String genre = book.getGenre() != null ? book.getGenre() : "Uncategorized";
                            genreCount.put(genre, genreCount.getOrDefault(genre, 0) + 1);
                        }

                        List<Statistics.GenreCount> genreStatsList = new ArrayList<>();
                        for (Map.Entry<String, Integer> entry : genreCount.entrySet()) {
                            genreStatsList.add(new Statistics.GenreCount(entry.getKey(), entry.getValue()));
                        }

                        // Sort by count descending
                        genreStatsList.sort((a, b) -> Integer.compare(b.getCount(), a.getCount()));

                        genreStats.setValue(genreStatsList);
                    },
                    error -> genreStats.setValue(new ArrayList<>())
                )
        );
    }

    public void loadMonthlyActivity() {
        disposables.add(
            loanDao.getAllLoans()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    loans -> {
                        Map<String, Integer> loansByMonth = new HashMap<>();
                        Map<String, Integer> returnsByMonth = new HashMap<>();

                        Calendar cal = Calendar.getInstance();
                        java.text.SimpleDateFormat monthFormat = new java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.getDefault());

                        for (Loan loan : loans) {
                            // Count loans by month
                            cal.setTimeInMillis(loan.getLoanDate());
                            String loanMonth = monthFormat.format(cal.getTime());
                            loansByMonth.put(loanMonth, loansByMonth.getOrDefault(loanMonth, 0) + 1);

                            // Count returns by month
                            if (loan.getReturnDate() > 0) {
                                cal.setTimeInMillis(loan.getReturnDate());
                                String returnMonth = monthFormat.format(cal.getTime());
                                returnsByMonth.put(returnMonth, returnsByMonth.getOrDefault(returnMonth, 0) + 1);
                            }
                        }

                        // Combine into MonthlyCount list
                        List<Statistics.MonthlyCount> monthlyList = new ArrayList<>();
                        for (String month : loansByMonth.keySet()) {
                            int loansCount = loansByMonth.getOrDefault(month, 0);
                            int returnsCount = returnsByMonth.getOrDefault(month, 0);
                            monthlyList.add(new Statistics.MonthlyCount(month, loansCount, returnsCount));
                        }

                        monthlyStats.setValue(monthlyList);
                    },
                    error -> monthlyStats.setValue(new ArrayList<>())
                )
        );
    }

    public void loadTopBorrowers() {
        disposables.add(
            borrowerDao.getAllBorrowers()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    borrowers -> {
                        // Sort by loan count (would need loan count from loans)
                        borrowers.sort((a, b) -> Long.compare(b.getId(), a.getId()));
                        if (borrowers.size() > 10) {
                            topBorrowers.setValue(borrowers.subList(0, 10));
                        } else {
                            topBorrowers.setValue(borrowers);
                        }
                    },
                    error -> topBorrowers.setValue(new ArrayList<>())
                )
        );
    }

    public void loadStatisticsForPeriod(long startDate, long endDate) {
        isLoading.setValue(true);

        disposables.add(
            bookDao.getBooksAddedBetween(startDate, endDate)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    books -> {
                        Statistics stats = new Statistics();
                        stats.setTotalBooks(books.size());
                        statistics.setValue(stats);
                        isLoading.setValue(false);
                    },
                    error -> {
                        errorMessage.setValue("Failed to load statistics");
                        isLoading.setValue(false);
                    }
                )
        );
    }

    // Getters
    public LiveData<Statistics> getStatistics() {
        return statistics;
    }

    public LiveData<Statistics.ReadingStatusStats> getStatusStats() {
        return statusStats;
    }

    public LiveData<List<Statistics.GenreCount>> getGenreStats() {
        return genreStats;
    }

    public LiveData<List<Statistics.MonthlyCount>> getMonthlyStats() {
        return monthlyStats;
    }

    public LiveData<List<Borrower>> getTopBorrowers() {
        return topBorrowers;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Integer> getTotalBooks() {
        return totalBooks;
    }

    public LiveData<Integer> getTotalBorrowers() {
        return totalBorrowers;
    }

    public LiveData<Integer> getActiveLoans() {
        return activeLoans;
    }

    public LiveData<Integer> getOverdueLoans() {
        return overdueLoans;
    }

    public LiveData<Double> getAverageRating() {
        return averageRating;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
