package com.librelibraria.ui.bottomsheet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.librelibraria.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ReturnBookBottomSheet extends BottomSheetDialogFragment {

    private TextView tvLoanInfo;
    private TextView tvLateFee;
    private View cardLateFee;

    private OnReturnBookListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    private long loanId;
    private String borrowerName;
    private long dueDate;
    private double lateFee;

    public interface OnReturnBookListener {
        void onReturnBook(long loanId, String condition, double lateFee);
    }

    public static ReturnBookBottomSheet newInstance() {
        return new ReturnBookBottomSheet();
    }

    public void setLoanData(long loanId, String borrowerName, long dueDate) {
        this.loanId = loanId;
        this.borrowerName = borrowerName;
        this.dueDate = dueDate;
    }

    public void setOnReturnBookListener(OnReturnBookListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_return_book, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvLoanInfo = view.findViewById(R.id.tv_loan_info);
        tvLateFee = view.findViewById(R.id.tv_late_fee);
        cardLateFee = view.findViewById(R.id.card_late_fee);

        if (borrowerName != null) {
            String info = getString(R.string.return_book_message, borrowerName);
            if (dueDate > 0) {
                info += "\n" + getString(R.string.due_date_full) + ": " + dateFormat.format(dueDate);
            }
            tvLoanInfo.setText(info);

            long now = Calendar.getInstance().getTimeInMillis();
            if (dueDate > 0 && now > dueDate) {
                long daysLate = (now - dueDate) / (24 * 60 * 60 * 1000);
                lateFee = daysLate * 0.50;
                cardLateFee.setVisibility(View.VISIBLE);
                tvLateFee.setText(String.format(Locale.getDefault(), "$%.2f", lateFee));
            } else {
                cardLateFee.setVisibility(View.GONE);
            }
        }

        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btn_return).setOnClickListener(v -> returnBook(view));
    }

    private void returnBook(View view) {
        if (listener == null) {
            dismiss();
            return;
        }

        String condition = "good";
        View chipGroup = view.findViewById(R.id.chip_group_condition);
        if (chipGroup instanceof com.google.android.material.chip.ChipGroup) {
            int selectedId = ((com.google.android.material.chip.ChipGroup) chipGroup).getCheckedChipId();
            if (selectedId == R.id.chip_damaged) {
                condition = "damaged";
            } else if (selectedId == R.id.chip_lost) {
                condition = "lost";
            }
        }

        listener.onReturnBook(loanId, condition, lateFee);
        dismiss();
    }
}