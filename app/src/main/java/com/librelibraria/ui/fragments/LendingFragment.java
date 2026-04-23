package com.librelibraria.ui.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.librelibraria.R;
import com.librelibraria.data.model.Book;
import com.librelibraria.data.model.Borrower;
import com.librelibraria.data.model.Loan;
import com.librelibraria.ui.adapters.LoanAdapter;
import com.librelibraria.ui.dialogs.LendBookDialog;
import com.librelibraria.ui.viewmodels.LendingViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Fragment for managing book loans.
 */
public class LendingFragment extends Fragment implements LoanAdapter.OnLoanClickListener {

    private RecyclerView rvLoans;
    private View emptyView;

    private LoanAdapter loanAdapter;
    private LendingViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lending, container, false);
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
        rvLoans = view.findViewById(R.id.recycler_loans);
        emptyView = view.findViewById(R.id.layout_empty);
    }

    private void setupRecyclerView() {
        loanAdapter = new LoanAdapter(new ArrayList<>(), this);
        rvLoans.setLayoutManager(new LinearLayoutManager(getContext()));
        rvLoans.setAdapter(loanAdapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(LendingViewModel.class);
    }

    private void setupObservers() {
        viewModel.getActiveLoans().observe(getViewLifecycleOwner(), loans -> {
            if (loans != null) {
                loanAdapter.updateLoans(loans);
                emptyView.setVisibility(loans.isEmpty() ? View.VISIBLE : View.GONE);
                rvLoans.setVisibility(loans.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });

        viewModel.getActiveLoansCount().observe(getViewLifecycleOwner(), count -> {});
        viewModel.getOverdueLoansCount().observe(getViewLifecycleOwner(), count -> {});
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {});
    }

    private void setupListeners() {
        FloatingActionButton fab = getView().findViewById(R.id.fab_new_loan);
        if (fab != null) {
            fab.setOnClickListener(v -> showLendBookDialog());
        }
    }

    private void showLendBookDialog() {
        LendBookDialog dialog = new LendBookDialog();
        dialog.setOnLoanCreatedListener(() -> viewModel.refresh());
        dialog.show(getParentFragmentManager(), "LendBookDialog");
    }

    @Override
    public void onLoanClick(Loan loan) {
        // Show loan details or return dialog
        showReturnDialog(loan);
    }

    private void showReturnDialog(Loan loan) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.return_book)
                .setMessage(getString(R.string.return_book_message, loan.getBorrowerName()))
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    viewModel.returnBook(loan.getId(), "");
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.refresh();
    }
}
