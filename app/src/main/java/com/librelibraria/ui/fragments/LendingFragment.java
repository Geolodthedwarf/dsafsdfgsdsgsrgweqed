package com.librelibraria.ui.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.librelibraria.data.model.Book;
import com.librelibraria.data.model.Borrower;
import com.librelibraria.data.model.Loan;
import com.librelibraria.ui.adapters.LoanAdapter;
import com.librelibraria.ui.util.AppAnimations;
import com.librelibraria.ui.viewmodels.LendingViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class LendingFragment extends Fragment implements LoanAdapter.OnLoanClickListener {

    private static final String TAG = "LendingFragment";

    private RecyclerView rvLoans;
    private View emptyView;
    private View progressView;

    private LoanAdapter loanAdapter;
    private LendingViewModel viewModel;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    private List<Book> availableBooksList = new ArrayList<>();
    private List<Borrower> borrowersList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            return inflater.inflate(R.layout.fragment_lending, container, false);
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
            view.post(() -> AppAnimations.slideInFromBottom(view));
        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated", e);
        }
    }

    private void initViews(View view) {
        try {
            rvLoans = view.findViewById(R.id.recycler_loans);
            emptyView = view.findViewById(R.id.layout_empty);
            progressView = view.findViewById(R.id.progress_loading);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
        }
    }

    private void setupRecyclerView() {
        try {
            if (rvLoans == null || getContext() == null) {
                Log.w(TAG, "rvLoans or getContext is null");
                return;
            }
            
            loanAdapter = new LoanAdapter(new ArrayList<>(), this);
            rvLoans.setLayoutManager(new LinearLayoutManager(getContext()));
            rvLoans.setAdapter(loanAdapter);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView", e);
        }
    }

    private void setupViewModel() {
        try {
            viewModel = new ViewModelProvider(this).get(LendingViewModel.class);
            
            if (viewModel != null) {
                viewModel.getActiveLoans().observe(getViewLifecycleOwner(), loans -> {
                    try {
                        if (loanAdapter != null && loans != null && !loans.isEmpty()) {
                            loanAdapter.updateLoans(loans);
                            if (emptyView != null) emptyView.setVisibility(View.GONE);
                            if (rvLoans != null) rvLoans.setVisibility(View.VISIBLE);
                        } else {
                            if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
                            if (rvLoans != null) rvLoans.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error in loans observer", e);
                    }
                });

                viewModel.getAvailableBooks().observe(getViewLifecycleOwner(), books -> {
                    try {
                        if (books != null) availableBooksList = books;
                    } catch (Exception e) {
                        Log.e(TAG, "Error in books observer", e);
                    }
                });

                viewModel.getBorrowers().observe(getViewLifecycleOwner(), borrowers -> {
                    try {
                        if (borrowers != null) borrowersList = borrowers;
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
            FloatingActionButton fab = getView() != null ? getView().findViewById(R.id.fab_new_loan) : null;
            if (fab != null) {
                fab.setOnClickListener(v -> {
                    AppAnimations.ripple(v);
                    v.postDelayed(this::showLendBookDialog, 150);
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up listeners", e);
        }
    }

    private void showLendBookDialog() {
        try {
            if (getContext() == null) return;
            
            View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_new_loan, null);

            AutoCompleteTextView actvBook = dialogView.findViewById(R.id.dropdown_book);
            AutoCompleteTextView actvBorrower = dialogView.findViewById(R.id.dropdown_borrower);
            EditText etDueDate = dialogView.findViewById(R.id.edit_due_date);

            // Setup book dropdown
            List<String> bookTitles = new ArrayList<>();
            for (Book b : availableBooksList) {
                if (b.getTitle() != null) bookTitles.add(b.getTitle());
            }
            ArrayAdapter<String> bookAdapter = new ArrayAdapter<String>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, bookTitles);
            actvBook.setAdapter(bookAdapter);

            // Setup borrower dropdown
            List<String> borrowerNames = new ArrayList<>();
            for (Borrower b : borrowersList) {
                if (b.getName() != null) borrowerNames.add(b.getName());
            }
            ArrayAdapter<String> borrowerAdapter = new ArrayAdapter<String>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, borrowerNames);
            actvBorrower.setAdapter(borrowerAdapter);

            // Set default due date (14 days from now)
            final long[] defaultDueDate = {Calendar.getInstance().getTimeInMillis() + 14L * 24 * 60 * 60 * 1000};
            etDueDate.setText(dateFormat.format(new java.util.Date(defaultDueDate[0])));

            // Due date picker
            etDueDate.setOnClickListener(v -> {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(defaultDueDate[0]);
                DatePickerDialog datePicker = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        Calendar selected = Calendar.getInstance();
                        selected.set(year, month, dayOfMonth);
                        defaultDueDate[0] = selected.getTimeInMillis();
                        etDueDate.setText(dateFormat.format(selected.getTime()));
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                );
                datePicker.show();
            });

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
            builder.setView(dialogView)
                .setTitle(R.string.lend_book)
                .setPositiveButton(R.string.lend, (dialog, which) -> {
                    try {
                        String bookTitle = actvBook.getText().toString().trim();
                        String borrowerName = actvBorrower.getText().toString().trim();

                        if (bookTitle.isEmpty() || borrowerName.isEmpty()) {
                            Toast.makeText(getContext(), R.string.error_required_field, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Find book and borrower by name
                        Long bookId = null;
                        Long borrowerId = null;
                        for (Book b : availableBooksList) {
                            if (b.getTitle() != null && b.getTitle().equals(bookTitle)) {
                                bookId = b.getId();
                                break;
                            }
                        }
                        for (Borrower b : borrowersList) {
                            if (b.getName() != null && b.getName().equals(borrowerName)) {
                                borrowerId = b.getId();
                                break;
                            }
                        }

                        if (bookId != null && borrowerId != null && viewModel != null) {
                            viewModel.lendBook(bookId, borrowerId, borrowerName, defaultDueDate[0]);
                            Toast.makeText(getContext(), R.string.loan_created, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error in lend dialog", e);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing lend dialog", e);
        }
    }

    @Override
    public void onLoanClick(Loan loan) {
        try {
            showReturnDialog(loan);
        } catch (Exception e) {
            Log.e(TAG, "Error on loan click", e);
        }
    }

    private void showReturnDialog(Loan loan) {
        try {
            if (getContext() == null || loan == null) return;
            
            new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.return_book)
                .setMessage(getString(R.string.return_book_message, loan.getBorrowerName()))
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    try {
                        if (viewModel != null) {
                            viewModel.changeReturnBook(loan.getId());
                            Toast.makeText(getContext(), R.string.loan_returned, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error returning book", e);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing return dialog", e);
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