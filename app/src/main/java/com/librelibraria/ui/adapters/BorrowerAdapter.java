package com.librelibraria.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.librelibraria.R;
import com.librelibraria.data.model.Borrower;

import java.util.ArrayList;
import java.util.List;

public class BorrowerAdapter extends RecyclerView.Adapter<BorrowerAdapter.BorrowerViewHolder> {

    private List<Borrower> borrowers = new ArrayList<>();

    public BorrowerAdapter(List<Borrower> initialList) {
        this.borrowers = initialList != null ? initialList : new ArrayList<>();
    }

    public void updateBorrowers(List<Borrower> newBorrowers) {
        this.borrowers = newBorrowers != null ? newBorrowers : new ArrayList<>();
        notifyDataSetChanged();
    }

    public Borrower getBorrowerAt(int position) {
        if (position >= 0 && position < borrowers.size()) {
            return borrowers.get(position);
        }
        return null;
    }

    @NonNull
    @Override
    public BorrowerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_borrower, parent, false);
        return new BorrowerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BorrowerViewHolder holder, int position) {
        Borrower borrower = borrowers.get(position);
        holder.bind(borrower);
    }

    @Override
    public int getItemCount() {
        return borrowers.size();
    }

    static class BorrowerViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvEmail;
        private final TextView tvPhone;

        public BorrowerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.text_name);
            tvEmail = itemView.findViewById(R.id.text_email);
            tvPhone = itemView.findViewById(R.id.text_phone);
        }

        public void bind(Borrower borrower) {
            if (tvName != null) tvName.setText(borrower.getName());
            if (tvEmail != null) tvEmail.setText(borrower.getEmail() != null ? borrower.getEmail() : "");
            if (tvPhone != null) tvPhone.setText(borrower.getPhone() != null ? borrower.getPhone() : "");
        }
    }
}