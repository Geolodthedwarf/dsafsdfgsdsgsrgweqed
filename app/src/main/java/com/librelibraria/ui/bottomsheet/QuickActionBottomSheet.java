package com.librelibraria.ui.bottomsheet;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.librelibraria.R;
import com.librelibraria.ui.activities.AddEditBookActivity;
import com.librelibraria.ui.activities.DiaryActivity;

public class QuickActionBottomSheet extends BottomSheetDialogFragment {

    private OnActionSelectedListener listener;

    public interface OnActionSelectedListener {
        void onAddBook();
        void onQuickLend();
        void onAddNote();
        void onScanIsbn();
    }

    public static QuickActionBottomSheet newInstance() {
        return new QuickActionBottomSheet();
    }

    public void setOnActionSelectedListener(OnActionSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_quick_action, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btn_add_book).setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddBook();
            } else {
                startActivity(new Intent(getActivity(), AddEditBookActivity.class));
            }
            dismiss();
        });

        view.findViewById(R.id.btn_quick_lend).setOnClickListener(v -> {
            if (listener != null) {
                listener.onQuickLend();
            }
            dismiss();
        });

        view.findViewById(R.id.btn_add_note).setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddNote();
            } else {
                startActivity(new Intent(getActivity(), DiaryActivity.class));
            }
            dismiss();
        });

        view.findViewById(R.id.btn_scan_isbn).setOnClickListener(v -> {
            if (listener != null) {
                listener.onScanIsbn();
            }
            dismiss();
        });
    }
}