package com.librelibraria.ui.fragments;

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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.librelibraria.R;
import com.librelibraria.data.model.Borrower;
import com.librelibraria.ui.adapters.BorrowerAdapter;
import com.librelibraria.ui.viewmodels.BorrowersViewModel;

import java.util.ArrayList;

public class BorrowersFragment extends Fragment {

    private static final String TAG = "BorrowersFragment";

    private RecyclerView rvBorrowers;
    private View emptyView;
    private View progressView;

    private BorrowerAdapter borrowerAdapter;
    private BorrowersViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            return inflater.inflate(R.layout.fragment_borrowers, container, false);
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
        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated", e);
        }
    }

    private void initViews(View view) {
        try {
            rvBorrowers = view.findViewById(R.id.recycler_borrowers);
            emptyView = view.findViewById(R.id.layout_empty);
            progressView = view.findViewById(R.id.progress_loading);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
        }
    }

    private void setupRecyclerView() {
        try {
            if (rvBorrowers == null || getContext() == null) return;
            
            borrowerAdapter = new BorrowerAdapter(new ArrayList<>());
            rvBorrowers.setLayoutManager(new LinearLayoutManager(getContext()));
            rvBorrowers.setAdapter(borrowerAdapter);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView", e);
        }
    }

    private void setupViewModel() {
        try {
            viewModel = new ViewModelProvider(this).get(BorrowersViewModel.class);
            
            if (viewModel != null) {
                viewModel.getBorrowers().observe(getViewLifecycleOwner(), borrowers -> {
                    try {
                        if (borrowerAdapter != null && borrowers != null && !borrowers.isEmpty()) {
                            borrowerAdapter.updateBorrowers(borrowers);
                            if (emptyView != null) emptyView.setVisibility(View.GONE);
                            if (rvBorrowers != null) rvBorrowers.setVisibility(View.VISIBLE);
                        } else {
                            if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
                            if (rvBorrowers != null) rvBorrowers.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error in borrowers observer", e);
                    }
                });

                viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
                    try {
                        if (progressView != null) {
                            progressView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                        }
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
            FloatingActionButton fab = getView() != null ? getView().findViewById(R.id.fab_add_borrower) : null;
            if (fab != null) {
                fab.setOnClickListener(v -> showAddBorrowerDialog());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up listeners", e);
        }
    }

    private void showAddBorrowerDialog() {
        try {
            if (getContext() == null) return;
            
            View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_add_borrower, null);

            EditText etName = dialogView.findViewById(R.id.edit_name);
            EditText etEmail = dialogView.findViewById(R.id.edit_email);
            EditText etPhone = dialogView.findViewById(R.id.edit_phone);

            new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setTitle(R.string.add_borrower)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    try {
                        String name = etName.getText().toString().trim();
                        String email = etEmail.getText().toString().trim();
                        String phone = etPhone.getText().toString().trim();

                        if (name.isEmpty()) {
                            Toast.makeText(getContext(), R.string.error_required_field, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Borrower borrower = new Borrower();
                        borrower.setName(name);
                        borrower.setEmail(email);
                        borrower.setPhone(phone);

                        if (viewModel != null) {
                            viewModel.addBorrower(borrower);
                            Toast.makeText(getContext(), R.string.borrower_added, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error adding borrower", e);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing dialog", e);
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