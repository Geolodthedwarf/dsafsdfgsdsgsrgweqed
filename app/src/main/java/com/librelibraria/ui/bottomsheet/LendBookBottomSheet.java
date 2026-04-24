package com.librelibraria.ui.bottomsheet;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.librelibraria.R;
import com.librelibraria.data.model.Book;
import com.librelibraria.data.model.Borrower;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class LendBookBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_BOOKS = "books";
    private static final String ARG_BORROWERS = "borrowers";

    private AutoCompleteTextView actvBook;
    private AutoCompleteTextView actvBorrower;
    private EditText etDueDate;
    private EditText etNotes;

    private List<Book> books = new ArrayList<>();
    private List<Borrower> borrowers = new ArrayList<>();
    private long defaultDueDate;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    private OnLendBookListener listener;

    public interface OnLendBookListener {
        void onLendBook(long bookId, long borrowerId, long dueDate, String notes);
    }

    public static LendBookBottomSheet newInstance(List<Book> books, List<Borrower> borrowers) {
        LendBookBottomSheet fragment = new LendBookBottomSheet();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    public void setBorrowers(List<Borrower> borrowers) {
        this.borrowers = borrowers;
    }

    public void setOnLendBookListener(OnLendBookListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_lend_book, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        actvBook = view.findViewById(R.id.actv_book);
        actvBorrower = view.findViewById(R.id.actv_borrower);
        etDueDate = view.findViewById(R.id.et_due_date);
        etNotes = view.findViewById(R.id.et_notes);

        setupDropdowns();
        setupDatePicker(view);

        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btn_lend).setOnClickListener(v -> lendBook());
    }

    private void setupDropdowns() {
        List<String> bookTitles = new ArrayList<>();
        for (Book b : books) {
            if (b.getTitle() != null) bookTitles.add(b.getTitle());
        }
        ArrayAdapter<String> bookAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, bookTitles);
        actvBook.setAdapter(bookAdapter);

        List<String> borrowerNames = new ArrayList<>();
        for (Borrower b : borrowers) {
            if (b.getName() != null) borrowerNames.add(b.getName());
        }
        ArrayAdapter<String> borrowerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, borrowerNames);
        actvBorrower.setAdapter(borrowerAdapter);

        defaultDueDate = Calendar.getInstance().getTimeInMillis() + 14L * 24 * 60 * 60 * 1000;
        etDueDate.setText(dateFormat.format(Calendar.getInstance().getTime()));
    }

    private void setupDatePicker(View view) {
        etDueDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(R.string.select_due_date)
                    .setSelection(defaultDueDate)
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                defaultDueDate = selection;
                etDueDate.setText(dateFormat.format(selection));
            });

            datePicker.show(getParentFragmentManager(), "due_date_picker");
        });
    }

    private void lendBook() {
        String bookTitle = actvBook.getText().toString().trim();
        String borrowerName = actvBorrower.getText().toString().trim();
        String notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";

        if (bookTitle.isEmpty() || borrowerName.isEmpty()) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.error)
                    .setMessage(R.string.error_required_field)
                    .setPositiveButton(R.string.ok, null)
                    .show();
            return;
        }

        Long bookId = null;
        Long borrowerId = null;
        for (Book b : books) {
            if (b.getTitle() != null && b.getTitle().equals(bookTitle)) {
                bookId = b.getId();
                break;
            }
        }
        for (Borrower b : borrowers) {
            if (b.getName() != null && b.getName().equals(borrowerName)) {
                borrowerId = b.getId();
                break;
            }
        }

        if (bookId != null && borrowerId != null && listener != null) {
            listener.onLendBook(bookId, borrowerId, defaultDueDate, notes);
            dismiss();
        }
    }
}