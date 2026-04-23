package com.librelibraria.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.librelibraria.R;
import com.librelibraria.data.model.Book;
import com.librelibraria.ui.activities.AddEditBookActivity;
import com.librelibraria.ui.activities.BookDetailActivity;
import com.librelibraria.ui.adapters.BookAdapter;
import com.librelibraria.ui.viewmodels.DashboardViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Dashboard fragment showing overview and recent books.
 */
public class DashboardFragment extends Fragment implements BookAdapter.OnBookClickListener {

    private TextView tvTotalBooks;
    private TextView tvActiveLoans;
    private TextView tvTotalBorrowers;
    private TextView tvOverdueBooks;

    private RecyclerView rvRecentBooks;
    private BookAdapter recentBooksAdapter;

    private DashboardViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupViewModel();
        setupObservers();
        setupListeners();
    }

    private void initViews(View view) {
        tvTotalBooks = view.findViewById(R.id.text_total_books);
        tvActiveLoans = view.findViewById(R.id.text_active_loans);
        tvTotalBorrowers = view.findViewById(R.id.text_total_borrowers);
        tvOverdueBooks = view.findViewById(R.id.text_overdue);
        rvRecentBooks = view.findViewById(R.id.recycler_recent_activity);
    }

    private void setupRecyclerView() {
        recentBooksAdapter = new BookAdapter(new ArrayList<>(), this);
        rvRecentBooks.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRecentBooks.setAdapter(recentBooksAdapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
    }

    private void setupObservers() {
        viewModel.getStatistics().observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                tvTotalBooks.setText(String.valueOf(stats.getTotalBooks()));
                tvActiveLoans.setText(String.valueOf(stats.getBorrowedBooks()));
                tvTotalBorrowers.setText(String.valueOf(stats.getAvailableBooks()));
                tvOverdueBooks.setText(String.valueOf(stats.getOverdueBooks()));
            }
        });

        viewModel.getRecentBooks().observe(getViewLifecycleOwner(), books -> {
            if (books != null) {
                recentBooksAdapter.updateBooks(books);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {});
    }

    private void setupListeners() {
        FloatingActionButton fabAdd = getView().findViewById(R.id.fab_add_book);
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), AddEditBookActivity.class);
                startActivity(intent);
            });
        }
    }

    @Override
    public void onBookClick(Book book) {
        Intent intent = new Intent(getActivity(), BookDetailActivity.class);
        intent.putExtra(BookDetailActivity.EXTRA_BOOK_ID, book.getId());
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.refresh();
    }
}
