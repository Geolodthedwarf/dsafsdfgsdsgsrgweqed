package com.librelibraria.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.librelibraria.R;
import com.librelibraria.ui.activities.AddEditBookActivity;
import com.librelibraria.ui.util.AppAnimations;
import com.librelibraria.ui.viewmodels.CatalogViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.librelibraria.R;
import com.librelibraria.data.model.Book;
import com.librelibraria.ui.activities.AddEditBookActivity;
import com.librelibraria.ui.activities.BookDetailActivity;
import com.librelibraria.ui.adapters.BookAdapter;
import com.librelibraria.ui.viewmodels.CatalogViewModel;

import java.util.ArrayList;

public class CatalogFragment extends Fragment implements BookAdapter.OnBookClickListener {

    private static final String TAG = "CatalogFragment";

    private RecyclerView rvBooks;
    private EditText etSearch;
    private View emptyView;
    private ChipGroup chipGroupFilter;
    private SwipeRefreshLayout swipeRefresh;

    private BookAdapter bookAdapter;
    private CatalogViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            return inflater.inflate(R.layout.fragment_catalog, container, false);
        } catch (Exception e) {
            Log.e(TAG, "Error inflating layout", e);
            return null;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        try {
            initViews(view);
            setupRecyclerView();
            setupViewModel();
            setupListeners();
            
            // Animate entry
            view.post(() -> {
                AppAnimations.fadeIn(view);
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated", e);
        }
    }

    private void initViews(View view) {
        try {
            // Handle SwipeRefreshLayout as root
            if (view instanceof SwipeRefreshLayout) {
                swipeRefresh = (SwipeRefreshLayout) view;
            } else {
                swipeRefresh = view.findViewById(R.id.swipe_refresh);
            }
            
            if (swipeRefresh != null) {
                rvBooks = swipeRefresh.findViewById(R.id.recycler_books);
                etSearch = swipeRefresh.findViewById(R.id.edit_search);
                emptyView = swipeRefresh.findViewById(R.id.layout_empty);
                chipGroupFilter = swipeRefresh.findViewById(R.id.chip_group_filter);
            } else {
                rvBooks = view.findViewById(R.id.recycler_books);
                etSearch = view.findViewById(R.id.edit_search);
                emptyView = view.findViewById(R.id.layout_empty);
                chipGroupFilter = view.findViewById(R.id.chip_group_filter);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
        }
    }

    private void setupRecyclerView() {
        try {
            if (rvBooks == null || getContext() == null) {
                Log.w(TAG, "rvBooks or getContext is null");
                return;
            }
            
            bookAdapter = new BookAdapter(new ArrayList<>(), this);
            rvBooks.setLayoutManager(new LinearLayoutManager(getContext()));
            rvBooks.setAdapter(bookAdapter);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView", e);
        }
    }

    private void setupViewModel() {
        try {
            viewModel = new ViewModelProvider(this).get(CatalogViewModel.class);
            
            // Setup observers after ViewModel is created
            if (viewModel != null) {
                viewModel.getBooks().observe(getViewLifecycleOwner(), books -> {
                    try {
                        if (bookAdapter == null) return;
                        if (books != null && !books.isEmpty()) {
                            bookAdapter.updateBooks(books);
                            if (emptyView != null) emptyView.setVisibility(View.GONE);
                            if (rvBooks != null) rvBooks.setVisibility(View.VISIBLE);
                        } else {
                            if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
                            if (rvBooks != null) rvBooks.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error in books observer", e);
                    }
                });

                viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
                    try {
                        if (swipeRefresh != null) swipeRefresh.setRefreshing(isLoading);
                    } catch (Exception e) {
                        Log.e(TAG, "Error in loading observer", e);
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up ViewModel", e);
        }
    }

    private void setupListeners() {
        try {
            // FAB
            View fab = getView() != null ? getView().findViewById(R.id.fab_add_book) : null;
            if (fab != null) {
                fab.setOnClickListener(v -> {
                    try {
                        AppAnimations.ripple(v);
                        v.postDelayed(() -> {
                            startActivity(new Intent(getActivity(), AddEditBookActivity.class));
                        }, 150);
                    } catch (Exception e) {
                        Log.e(TAG, "Error starting AddEditBookActivity", e);
                    }
                });
            }

            // Swipe to refresh
            if (swipeRefresh != null) {
                swipeRefresh.setOnRefreshListener(() -> {
                    try {
                        if (viewModel != null) viewModel.refresh();
                    } catch (Exception e) {
                        Log.e(TAG, "Error refreshing", e);
                    }
                });
                swipeRefresh.setColorSchemeResources(R.color.primary);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up listeners", e);
        }
    }

    @Override
    public void onBookClick(Book book) {
        try {
            Intent intent = new Intent(getActivity(), BookDetailActivity.class);
            intent.putExtra(BookDetailActivity.EXTRA_BOOK_ID, book.getId());
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening book details", e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            if (viewModel != null) viewModel.refresh();
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume", e);
        }
    }
}