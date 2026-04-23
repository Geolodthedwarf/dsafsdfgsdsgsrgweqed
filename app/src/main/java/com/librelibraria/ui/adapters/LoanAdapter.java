package com.librelibraria.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.librelibraria.R;
import com.librelibraria.data.model.Loan;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying loans in a RecyclerView.
 */
public class LoanAdapter extends RecyclerView.Adapter<LoanAdapter.LoanViewHolder> {

    private List<Loan> loans;
    private final OnLoanClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    public interface OnLoanClickListener {
        void onLoanClick(Loan loan);
    }

    public LoanAdapter(List<Loan> loans, OnLoanClickListener listener) {
        this.loans = loans;
        this.listener = listener;
    }

    public void updateLoans(List<Loan> newLoans) {
        this.loans = newLoans;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LoanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_loan, parent, false);
        return new LoanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LoanViewHolder holder, int position) {
        Loan loan = loans.get(position);
        holder.bind(loan, listener, dateFormat);
    }

    @Override
    public int getItemCount() {
        return loans != null ? loans.size() : 0;
    }

    static class LoanViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvBookTitle;
        private final TextView tvBorrower;
        private final TextView tvDueDate;
        private final TextView tvStatus;
        private final View statusIndicator;

        LoanViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookTitle = itemView.findViewById(R.id.tv_book_title);
            tvBorrower = itemView.findViewById(R.id.tv_borrower);
            tvDueDate = itemView.findViewById(R.id.tv_due_date);
            tvStatus = itemView.findViewById(R.id.tv_status);
            statusIndicator = itemView.findViewById(R.id.status_indicator);
        }

        void bind(Loan loan, OnLoanClickListener listener, SimpleDateFormat dateFormat) {
            tvBookTitle.setText(String.valueOf(loan.getBookId())); // Would need to load book title
            tvBorrower.setText(loan.getBorrowerName());
            tvDueDate.setText(dateFormat.format(new Date(loan.getDueDate())));

            if (loan.isOverdue()) {
                tvStatus.setText(itemView.getContext().getString(R.string.status_overdue));
                tvStatus.setTextColor(itemView.getContext().getColor(R.color.status_overdue));
                statusIndicator.setBackgroundColor(itemView.getContext().getColor(R.color.status_overdue));
            } else {
                tvStatus.setText(itemView.getContext().getString(R.string.status_active));
                tvStatus.setTextColor(itemView.getContext().getColor(R.color.status_borrowed));
                statusIndicator.setBackgroundColor(itemView.getContext().getColor(R.color.status_borrowed));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLoanClick(loan);
                }
            });
        }
    }
}
