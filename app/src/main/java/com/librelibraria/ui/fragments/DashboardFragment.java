package com.librelibraria.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import com.librelibraria.ui.activities.DiaryActivity;
import com.librelibraria.ui.adapters.BookAdapter;
import com.librelibraria.ui.adapterutil.LayoutAnimationUtil;
import com.librelibraria.ui.adapters.ActivityAdapter;
import com.librelibraria.ui.bottomsheet.QuickActionBottomSheet;
import com.librelibraria.ui.viewmodels.DashboardViewModel;

import java.util.ArrayList;

public class DashboardFragment extends Fragment implements BookAdapter.OnBookClickListener {

    private static final String TAG = "DashboardFragment";

    private TextView tvTotalBooks;
    private TextView tvActiveLoans;
    private TextView tvTotalBorrowers;
    private TextView tvOverdueBooks;

    private RecyclerView rvRecentBooks;
    private RecyclerView rvRecentActivity;
    private SwipeRefreshLayout swipeRefresh;
    private View fab;

    private BookAdapter recentBooksAdapter;
    private ActivityAdapter recentActivityAdapter;
    private DashboardViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            return inflater.inflate(R.layout.fragment_dashboard, container, false);
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
            setupFab();
        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated", e);
        }
    }

    private void initViews(View view) {
        try {
            if (view instanceof SwipeRefreshLayout) {
                swipeRefresh = (SwipeRefreshLayout) view;
            } else {
                swipeRefresh = view.findViewById(R.id.swipe_refresh);
            }
            
            if (swipeRefresh != null) {
                tvTotalBooks = swipeRefresh.findViewById(R.id.text_total_books);
                tvActiveLoans = swipeRefresh.findViewById(R.id.text_active_loans);
                tvTotalBorrowers = swipeRefresh.findViewById(R.id.text_total_borrowers);
                tvOverdueBooks = swipeRefresh.findViewById(R.id.text_overdue);
                rvRecentBooks = swipeRefresh.findViewById(R.id.recycler_recent_activity);
                rvRecentActivity = swipeRefresh.findViewById(R.id.recycler_activity);
            } else {
                tvTotalBooks = view.findViewById(R.id.text_total_books);
                tvActiveLoans = view.findViewById(R.id.text_active_loans);
                tvTotalBorrowers = view.findViewById(R.id.text_total_borrowers);
                tvOverdueBooks = view.findViewById(R.id.text_overdue);
                rvRecentBooks = view.findViewById(R.id.recycler_recent_activity);
                rvRecentActivity = view.findViewById(R.id.recycler_activity);
            }
            fab = view.findViewById(R.id.fab);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
        }
    }

    private void setupRecyclerView() {
        try {
            if (getContext() == null) return;
            
            recentBooksAdapter = new BookAdapter(new ArrayList<>(), this);
            if (rvRecentBooks != null) {
                rvRecentBooks.setLayoutManager(new LinearLayoutManager(getContext()));
                rvRecentBooks.setAdapter(recentBooksAdapter);
                LayoutAnimationUtil.runLayoutAnimation(rvRecentBooks);
            }

            recentActivityAdapter = new ActivityAdapter();
            if (rvRecentActivity != null) {
                rvRecentActivity.setLayoutManager(new LinearLayoutManager(getContext()));
                rvRecentActivity.setAdapter(recentActivityAdapter);
                LayoutAnimationUtil.runLayoutAnimation(rvRecentActivity);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView", e);
        }
    }

    private void setupViewModel() {
        try {
            viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
            
            if (viewModel != null) {
                viewModel.getStatistics().observe(getViewLifecycleOwner(), stats -> {
                    try {
                        if (stats != null) {
                            if (tvTotalBooks != null) tvTotalBooks.setText(String.valueOf(stats.getTotalBooks()));
                            if (tvActiveLoans != null) tvActiveLoans.setText(String.valueOf(stats.getActiveLoans()));
                            if (tvTotalBorrowers != null) tvTotalBorrowers.setText(String.valueOf(stats.getTotalBorrowers()));
                            if (tvOverdueBooks != null) tvOverdueBooks.setText(String.valueOf(stats.getOverdueBooks()));
                        }
                        if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                    } catch (Exception e) {
                        Log.e(TAG, "Error in stats observer", e);
                    }
                });

                viewModel.getRecentBooks().observe(getViewLifecycleOwner(), books -> {
                    try {
                        if (recentBooksAdapter != null && books != null) {
                            recentBooksAdapter.updateBooks(books);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error in books observer", e);
                    }
                });

                viewModel.getRecentActivity().observe(getViewLifecycleOwner(), activities -> {
                    try {
                        if (recentActivityAdapter != null && activities != null) {
                            recentActivityAdapter.updateActivities(activities);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error in activity observer", e);
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
            View btnAddBook = getView() != null ? getView().findViewById(R.id.button_add_book) : null;
            if (btnAddBook != null) {
                btnAddBook.setOnClickListener(v -> {
                    try {
                        startActivity(new Intent(getActivity(), AddEditBookActivity.class));
                    } catch (Exception e) {
                        Log.e(TAG, "Error starting AddEditBookActivity", e);
                    }
                });
            }

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

    private void setupFab() {
        try {
            if (fab != null) {
                final View fabRef = fab;
                fab.setOnClickListener(v -> showQuickActions());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up FAB", e);
        }
    }

    private void showQuickActions() {
        try {
            QuickActionBottomSheet bottomSheet = QuickActionBottomSheet.newInstance();
            bottomSheet.setOnActionSelectedListener(new QuickActionBottomSheet.OnActionSelectedListener() {
                @Override
                public void onAddBook() {
                    try {
                        startActivity(new Intent(getActivity(), AddEditBookActivity.class));
                    } catch (Exception e) {
                        Log.e(TAG, "Error in quick action", e);
                    }
                }

                @Override
                public void onQuickLend() {
                    navigateToLending();
                }

                @Override
                public void onAddNote() {
                    try {
                        startActivity(new Intent(getActivity(), DiaryActivity.class));
                    } catch (Exception e) {
                        Log.e(TAG, "Error starting DiaryActivity", e);
                    }
                }

                @Override
                public void onScanIsbn() {
                    try {
                        Intent intent = new Intent(getActivity(), AddEditBookActivity.class);
                        intent.putExtra(AddEditBookActivity.EXTRA_SCAN_MODE, true);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "Error starting scan", e);
                    }
                }
            });
            bottomSheet.show(getParentFragmentManager(), "quick_actions");
        } catch (Exception e) {
            Log.e(TAG, "Error showing quick actions", e);
        }
    }

    private void navigateToLending() {
        try {
            if (getActivity() instanceof com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener) {
                View nav = getActivity().findViewById(R.id.bottom_navigation);
                if (nav != null) {
                    ((com.google.android.material.bottomnavigation.BottomNavigationView) nav)
                        .setSelectedItemId(R.id.nav_lending);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to lending", e);
        }
    }

    @Override
    public void onBookClick(Book book) {
        try {
            Intent intent = new Intent(getActivity(), BookDetailActivity.class);
            intent.putExtra(BookDetailActivity.EXTRA_BOOK_ID, book.getId());
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening book", e);
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