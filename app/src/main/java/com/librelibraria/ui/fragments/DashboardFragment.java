package com.librelibraria.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

    private SwipeRefreshLayout swipeRefresh;
    private View statsContainer;
    private View recentBooksContainer;

    private TextView tvTotalBooks;
    private TextView tvAvailableBooks;
    private TextView tvBorrowedBooks;
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
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        statsContainer = view.findViewById(R.id.stats_container);
        recentBooksContainer = view.findViewById(R.id.recent_books_container);

        tvTotalBooks = view.findViewById(R.id.tv_total_books);
        tvAvailableBooks = view.findViewById(R.id.tv_available_books);
        tvBorrowedBooks = view.findViewById(R.id.tv_borrowed_books);
        tvOverdueBooks = view.findViewById(R.id.tv_overdue_books);

        rvRecentBooks = view.findViewById(R.id.rv_recent_books);
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
                tvAvailableBooks.setText(String.valueOf(stats.getAvailableBooks()));
                tvBorrowedBooks.setText(String.valueOf(stats.getBorrowedBooks()));
                tvOverdueBooks.setText(String.valueOf(stats.getOverdueBooks()));
            }
        });

        viewModel.getRecentBooks().observe(getViewLifecycleOwner(), books -> {
            if (books != null) {
                recentBooksAdapter.updateBooks(books);
                recentBooksContainer.setVisibility(books.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            swipeRefresh.setRefreshing(isLoading);
        });
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refresh();
        });

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
