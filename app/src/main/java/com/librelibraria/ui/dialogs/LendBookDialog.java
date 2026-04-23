package com.librelibraria.ui.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.librelibraria.R;
import com.librelibraria.data.model.Book;
import com.librelibraria.data.model.Borrower;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Dialog for lending a book.
 */
public class LendBookDialog extends DialogFragment {

    private AutoCompleteTextView actvBook;
    private AutoCompleteTextView actvBorrower;
    private EditText etDueDate;
    private TextInputLayout tilBook;
    private TextInputLayout tilBorrower;

    private List<Book> availableBooks;
    private List<Borrower> borrowers;
    private long selectedDueDate;
    private OnLoanCreatedListener listener;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    public interface OnLoanCreatedListener {
        void onLoanCreated();
    }

    public void setOnLoanCreatedListener(OnLoanCreatedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_lend_book, null);

        initViews(view);
        setupDatePicker();
        loadData();

        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.lend_book)
                .setView(view)
                .setPositiveButton(R.string.lend, null)
                .setNegativeButton(R.string.cancel, null)
                .create();
    }

    private void initViews(View view) {
        tilBook = view.findViewById(R.id.til_book);
        tilBorrower = view.findViewById(R.id.til_borrower);
        actvBook = view.findViewById(R.id.actv_book);
        actvBorrower = view.findViewById(R.id.actv_borrower);
        etDueDate = view.findViewById(R.id.et_due_date);

        // Set default due date (14 days from now)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 14);
        selectedDueDate = calendar.getTimeInMillis();
        etDueDate.setText(dateFormat.format(calendar.getTime()));
    }

    private void setupDatePicker() {
        etDueDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selectedDueDate);

            DatePickerDialog dialog = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        Calendar selected = Calendar.getInstance();
                        selected.set(year, month, dayOfMonth);
                        selectedDueDate = selected.getTimeInMillis();
                        etDueDate.setText(dateFormat.format(selected.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            dialog.getDatePicker().setMinDate(System.currentTimeMillis());
            dialog.show();
        });
    }

    private void loadData() {
        // In a real implementation, this would load from ViewModel
        // For now, we'll use empty adapters
    }

    private void showValidationError(String message) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.error)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .show();
    }
}
