package com.librelibraria.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.librelibraria.R;
import com.librelibraria.data.model.AuditLog;

import java.util.ArrayList;
import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {

    private List<AuditLog> activities = new ArrayList<>();

    public ActivityAdapter() {
    }

    public ActivityAdapter(List<AuditLog> initialList) {
        this.activities = initialList != null ? initialList : new ArrayList<>();
    }

    public void updateActivities(List<AuditLog> newActivities) {
        this.activities = newActivities != null ? newActivities : new ArrayList<>();
        notifyDataSetChanged();
    }

    public AuditLog getActivityAt(int position) {
        if (position >= 0 && position < activities.size()) {
            return activities.get(position);
        }
        return null;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        AuditLog activity = activities.get(position);
        holder.bind(activity);
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    class ActivityViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivIcon;
        private final TextView tvAction;
        private final TextView tvDetails;
        private final TextView tvTimestamp;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvAction = itemView.findViewById(R.id.tv_action);
            tvDetails = itemView.findViewById(R.id.tv_details);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
        }

        public void bind(AuditLog activity) {
            tvAction.setText(formatAction(activity.getAction()));
            tvDetails.setText(activity.getDetails());
            tvTimestamp.setText(activity.getFormattedTimestamp());

            int iconRes = getIconForAction(activity.getAction());
            ivIcon.setImageResource(iconRes);
        }

        private String formatAction(String action) {
            if (action == null) return "";
            return action.replace("_", " ");
        }

        private int getIconForAction(String action) {
            if (action == null) return R.drawable.ic_note;

            switch (action) {
                case "BOOK_ADDED":
                    return R.drawable.ic_add;
                case "BOOK_UPDATED":
                    return R.drawable.ic_edit;
                case "BOOK_DELETED":
                    return R.drawable.ic_delete;
                case "BOOK_RATED":
                    return R.drawable.ic_star;
                case "LOAN_CREATED":
                    return R.drawable.ic_loan;
                case "LOAN_RETURNED":
                    return R.drawable.ic_loan;
                case "TAG_ADDED":
                    return R.drawable.ic_tag;
                case "DIARY_ENTRY_ADDED":
                    return R.drawable.ic_diary;
                default:
                    return R.drawable.ic_note;
            }
        }
    }
}