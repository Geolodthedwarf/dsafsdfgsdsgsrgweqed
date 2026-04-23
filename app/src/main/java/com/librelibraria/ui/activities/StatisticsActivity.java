package com.librelibraria.ui.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.librelibraria.R;
import com.librelibraria.data.model.Statistics;
import com.librelibraria.ui.viewmodels.StatisticsViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for displaying library statistics and visualizations.
 */
public class StatisticsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private View progressBar;

    // Summary cards
    private TextView tvTotalBooks;
    private TextView tvTotalBorrowers;
    private TextView tvActiveLoans;
    private TextView tvOverdueLoans;

    // Charts
    private PieChart pieChartStatus;
    private BarChart barChartMonthly;
    private BarChart barChartGenre;

    private StatisticsViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        initViews();
        setupToolbar();
        setupCharts();
        setupViewModel();

        // Load statistics
        viewModel.loadAllStatistics();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progress_bar);

        // Summary cards
        tvTotalBooks = findViewById(R.id.tv_total_books);
        tvTotalBorrowers = findViewById(R.id.tv_borrowed_books);
        tvActiveLoans = findViewById(R.id.tv_available_books);
        tvOverdueLoans = findViewById(R.id.tv_overdue_books);

        // Charts
        pieChartStatus = findViewById(R.id.pie_chart_status);
        barChartMonthly = findViewById(R.id.bar_chart_monthly);
        barChartGenre = findViewById(R.id.bar_chart_genre);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.statistics);
        }
    }

    private void setupCharts() {
        // Setup Pie Chart (Reading Status Distribution)
        pieChartStatus.setUsePercentValues(true);
        pieChartStatus.getDescription().setEnabled(false);
        pieChartStatus.setDrawHoleEnabled(true);
        pieChartStatus.setHoleColor(Color.WHITE);
        pieChartStatus.setHoleRadius(50f);
        pieChartStatus.setTransparentCircleRadius(55f);
        pieChartStatus.setDrawEntryLabelsEnabled(false);
        pieChartStatus.getLegend().setEnabled(true);
        pieChartStatus.getLegend().setWordWrapEnabled(true);

        // Setup Monthly Bar Chart
        barChartMonthly.getDescription().setEnabled(false);
        barChartMonthly.setDrawGridBackground(false);
        barChartMonthly.getLegend().setEnabled(false);
        barChartMonthly.setFitBars(true);
        barChartMonthly.animateY(1000);

        XAxis xAxis = barChartMonthly.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        barChartMonthly.getAxisLeft().setAxisMinimum(0f);
        barChartMonthly.getAxisRight().setEnabled(false);

        // Setup Genre Bar Chart
        barChartGenre.getDescription().setEnabled(false);
        barChartGenre.setDrawGridBackground(false);
        barChartGenre.getLegend().setEnabled(false);
        barChartGenre.animateY(1000);

        XAxis genreXAxis = barChartGenre.getXAxis();
        genreXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        genreXAxis.setDrawGridLines(false);
        genreXAxis.setGranularity(1f);
        genreXAxis.setLabelRotationAngle(-45f);

        barChartGenre.getAxisLeft().setAxisMinimum(0f);
        barChartGenre.getAxisRight().setEnabled(false);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);

        // Observe total books
        viewModel.getTotalBooks().observe(this, count -> {
            if (count != null) {
                tvTotalBooks.setText(String.valueOf(count));
            }
        });

        // Observe total borrowers
        viewModel.getTotalBorrowers().observe(this, count -> {
            if (count != null) {
                tvTotalBorrowers.setText(String.valueOf(count));
            }
        });

        // Observe active loans
        viewModel.getActiveLoans().observe(this, count -> {
            if (count != null) {
                tvActiveLoans.setText(String.valueOf(count));
            }
        });

        // Observe overdue loans
        viewModel.getOverdueLoans().observe(this, count -> {
            if (count != null) {
                tvOverdueLoans.setText(String.valueOf(count));
            }
        });

        // Observe status stats
        viewModel.getStatusStats().observe(this, statusStats -> {
            if (statusStats != null) {
                updateStatusPieChart(statusStats);
            }
        });

        // Observe genre distribution
        viewModel.getGenreStats().observe(this, genres -> {
            if (genres != null && !genres.isEmpty()) {
                updateGenreBarChart(genres);
            }
        });

        // Observe monthly stats
        viewModel.getMonthlyStats().observe(this, monthly -> {
            if (monthly != null && !monthly.isEmpty()) {
                updateMonthlyBarChart(monthly);
            }
        });

        // Observe loading state
        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
    }

    private void updateStatusPieChart(Statistics.ReadingStatusStats statusStats) {
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        int total = statusStats.getTotalBooks();
        if (total > 0) {
            int available = statusStats.getAvailableBooks();
            int onLoan = statusStats.getOnLoanBooks();
            int overdue = statusStats.getOverdueBooks();
            int lost = statusStats.getLostBooks();

            if (available > 0) {
                entries.add(new PieEntry(available, getString(R.string.available)));
                colors.add(getColor(R.color.status_available));
            }
            if (onLoan > 0) {
                entries.add(new PieEntry(onLoan, getString(R.string.on_loan)));
                colors.add(getColor(R.color.status_on_loan));
            }
            if (overdue > 0) {
                entries.add(new PieEntry(overdue, getString(R.string.overdue)));
                colors.add(getColor(R.color.status_overdue));
            }
            if (lost > 0) {
                entries.add(new PieEntry(lost, getString(R.string.lost)));
                colors.add(getColor(R.color.status_lost));
            }
        }

        if (entries.isEmpty()) {
            pieChartStatus.setNoDataText(getString(R.string.no_books));
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);

        pieChartStatus.setData(data);
        pieChartStatus.invalidate();
    }

    private void updateGenreBarChart(List<Statistics.GenreCount> genreData) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        int count = Math.min(genreData.size(), 10);
        for (int i = 0; i < count; i++) {
            entries.add(new BarEntry(i, genreData.get(i).getCount()));
            labels.add(genreData.get(i).getGenre());
        }

        BarDataSet dataSet = new BarDataSet(entries, getString(R.string.genre));
        dataSet.setColor(getColor(R.color.primary));
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.8f);

        barChartGenre.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChartGenre.setData(data);
        barChartGenre.invalidate();
    }

    private void updateMonthlyBarChart(List<Statistics.MonthlyCount> monthlyData) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < monthlyData.size(); i++) {
            entries.add(new BarEntry(i, monthlyData.get(i).getLoansCount()));
            labels.add(monthlyData.get(i).getMonth());
        }

        BarDataSet dataSet = new BarDataSet(entries, getString(R.string.loans));
        dataSet.setColor(getColor(R.color.primary));
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);

        barChartMonthly.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChartMonthly.setData(data);
        barChartMonthly.invalidate();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
