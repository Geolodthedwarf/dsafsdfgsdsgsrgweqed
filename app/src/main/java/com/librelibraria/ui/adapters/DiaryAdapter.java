package com.librelibraria.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.librelibraria.R;
import com.librelibraria.data.model.DiaryEntry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Adapter for displaying diary entries in a RecyclerView.
 */
public class DiaryAdapter extends ListAdapter<DiaryEntry, DiaryAdapter.DiaryViewHolder> {

    private final OnDiaryEntryClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public interface OnDiaryEntryClickListener {
        void onEntryClick(DiaryEntry entry);
    }

    public DiaryAdapter(OnDiaryEntryClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<DiaryEntry> DIFF_CALLBACK = new DiffUtil.ItemCallback<DiaryEntry>() {
        @Override
        public boolean areItemsTheSame(@NonNull DiaryEntry oldItem, @NonNull DiaryEntry newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull DiaryEntry oldItem, @NonNull DiaryEntry newItem) {
            return oldItem.getBookTitle().equals(newItem.getBookTitle()) &&
                    oldItem.getNote().equals(newItem.getNote()) &&
                    oldItem.getDate() == newItem.getDate();
        }
    };

    @NonNull
    @Override
    public DiaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_diary_entry, parent, false);
        return new DiaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiaryViewHolder holder, int position) {
        DiaryEntry entry = getItem(position);
        holder.bind(entry, listener);
    }

    public DiaryEntry getEntryAt(int position) {
        if (position >= 0 && position < getCurrentList().size()) {
            return getItem(position);
        }
        return null;
    }

    static class DiaryViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageIcon;
        private final TextView textTitle;
        private final TextView textDescription;
        private final TextView textTimestamp;

        public DiaryViewHolder(@NonNull View itemView) {
            super(itemView);
            imageIcon = itemView.findViewById(R.id.image_icon);
            textTitle = itemView.findViewById(R.id.text_title);
            textDescription = itemView.findViewById(R.id.text_description);
            textTimestamp = itemView.findViewById(R.id.text_timestamp);
        }

        public void bind(DiaryEntry entry, OnDiaryEntryClickListener listener) {
            // Set title (book title)
            textTitle.setText(entry.getBookTitle() != null ? entry.getBookTitle() : "Untitled");

            // Set description (note preview or quote)
            String description = "";
            if (entry.getQuote() != null && !entry.getQuote().isEmpty()) {
                description = "\"" + entry.getQuote() + "\"";
            } else if (entry.getNote() != null && !entry.getNote().isEmpty()) {
                description = entry.getPreview(80);
            }
            textDescription.setText(description);

            // Set timestamp
            textTimestamp.setText(getRelativeTime(entry.getDate()));

            // Set icon based on content type
            if (entry.getQuote() != null && !entry.getQuote().isEmpty()) {
                imageIcon.setImageResource(R.drawable.ic_quote);
            } else {
                imageIcon.setImageResource(R.drawable.ic_book);
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEntryClick(entry);
                }
            });
        }

        private String getRelativeTime(long timestamp) {
            long now = System.currentTimeMillis();
            long diff = now - timestamp;

            if (diff < TimeUnit.MINUTES.toMillis(1)) {
                return "Just now";
            } else if (diff < TimeUnit.HOURS.toMillis(1)) {
                long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
                return minutes + "m ago";
            } else if (diff < TimeUnit.DAYS.toMillis(1)) {
                long hours = TimeUnit.MILLISECONDS.toHours(diff);
                return hours + "h ago";
            } else if (diff < TimeUnit.DAYS.toMillis(7)) {
                long days = TimeUnit.MILLISECONDS.toDays(diff);
                return days + "d ago";
            } else {
                return new SimpleDateFormat("MMM dd", Locale.getDefault()).format(new Date(timestamp));
            }
        }
    }
}
