package com.librelibraria.ui.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.librelibraria.R;

/**
 * Dialog for rating a book.
 */
public class RatingDialog extends DialogFragment {

    private RatingBar ratingBar;
    private EditText etReview;
    private float initialRating = 0;
    private OnRatingSavedListener listener;

    public interface OnRatingSavedListener {
        void onRatingSaved(double rating, String review);
    }

    public void setInitialRating(float rating) {
        this.initialRating = rating;
    }

    public void setOnRatingSavedListener(OnRatingSavedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_rating, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ratingBar = view.findViewById(R.id.rating_bar);
        etReview = view.findViewById(R.id.et_review);

        if (initialRating > 0) {
            ratingBar.setRating(initialRating);
        }

        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btn_save).setOnClickListener(v -> saveRating());
    }

    private void saveRating() {
        float rating = ratingBar.getRating();
        if (rating == 0) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.error)
                    .setMessage(R.string.error_rating_required)
                    .setPositiveButton(R.string.ok, null)
                    .show();
            return;
        }

        String review = etReview.getText() != null ? etReview.getText().toString().trim() : "";

        if (listener != null) {
            listener.onRatingSaved(rating, review);
        }

        dismiss();
    }
}
