package com.librelibraria.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.librelibraria.R;
import com.librelibraria.data.model.Book;
import com.librelibraria.data.model.ReadingStatus;

import java.util.List;

/**
 * Adapter for displaying books in a RecyclerView.
 */
public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private List<Book> books;
    private final OnBookClickListener listener;

    public interface OnBookClickListener {
        void onBookClick(Book book);
    }

    public BookAdapter(List<Book> books, OnBookClickListener listener) {
        this.books = books;
        this.listener = listener;
    }

    public void updateBooks(List<Book> newBooks) {
        this.books = newBooks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);
        holder.bind(book, listener);
    }

    @Override
    public int getItemCount() {
        return books != null ? books.size() : 0;
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivCover;
        private final TextView tvTitle;
        private final TextView tvAuthor;
        private final TextView tvGenre;
        private final TextView tvStatus;
        private final RatingBar ratingBar;

        BookViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_book_cover);
            tvTitle = itemView.findViewById(R.id.tv_book_title);
            tvAuthor = itemView.findViewById(R.id.tv_book_author);
            tvGenre = itemView.findViewById(R.id.tv_book_genre);
            tvStatus = itemView.findViewById(R.id.tv_book_status);
            ratingBar = itemView.findViewById(R.id.rating_bar);
        }

        void bind(Book book, OnBookClickListener listener) {
            tvTitle.setText(book.getTitle());
            tvAuthor.setText(book.getAuthor() != null ? book.getAuthor() : "");
            tvGenre.setText(book.getGenre() != null ? book.getGenre() : "");

            // Set status
            if (book.isAvailable()) {
                tvStatus.setText(itemView.getContext().getString(R.string.status_available));
                tvStatus.setBackgroundResource(R.drawable.bg_status_available);
            } else {
                tvStatus.setText(itemView.getContext().getString(R.string.status_borrowed));
                tvStatus.setBackgroundResource(R.drawable.bg_status_borrowed);
            }

            // Set rating
            if (book.getRating() > 0) {
                ratingBar.setVisibility(View.VISIBLE);
                ratingBar.setRating((float) book.getRating());
            } else {
                ratingBar.setVisibility(View.GONE);
            }

            // Load cover image
            String coverUrl = book.getCustomCoverUrl();
            if (coverUrl != null && !coverUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(coverUrl)
                        .placeholder(R.drawable.ic_book_placeholder)
                        .error(R.drawable.ic_book_placeholder)
                        .into(ivCover);
            } else {
                ivCover.setImageResource(R.drawable.ic_book_placeholder);
            }

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookClick(book);
                }
            });
        }
    }
}
