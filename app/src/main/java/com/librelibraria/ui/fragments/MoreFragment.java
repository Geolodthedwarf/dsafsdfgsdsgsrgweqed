package com.librelibraria.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.librelibraria.R;
import com.librelibraria.ui.activities.DiaryActivity;
import com.librelibraria.ui.activities.SettingsActivity;
import com.librelibraria.ui.activities.StatisticsActivity;
import com.librelibraria.ui.activities.TagsActivity;

/**
 * Fragment for additional features (statistics, diary, tags, settings).
 */
public class MoreFragment extends Fragment {

    private LinearLayout llStatistics;
    private LinearLayout llDiary;
    private LinearLayout llTags;
    private LinearLayout llSettings;
    private LinearLayout llImportExport;
    private LinearLayout llServerMode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_more, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupListeners();
    }

    private void initViews(View view) {
        llStatistics = view.findViewById(R.id.ll_statistics);
        llDiary = view.findViewById(R.id.ll_diary);
        llTags = view.findViewById(R.id.ll_tags);
        llSettings = view.findViewById(R.id.ll_settings);
        llImportExport = view.findViewById(R.id.ll_import_export);
        llServerMode = view.findViewById(R.id.ll_server_mode);
    }

    private void setupListeners() {
        llStatistics.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), StatisticsActivity.class));
        });

        llDiary.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), DiaryActivity.class));
        });

        llTags.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), TagsActivity.class));
        });

        llSettings.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        });

        llImportExport.setOnClickListener(v -> {
            // Show import/export dialog
            showImportExportDialog();
        });

        llServerMode.setOnClickListener(v -> {
            // Show server mode settings
            showServerModeDialog();
        });
    }

    private void showImportExportDialog() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.import_export)
                .setItems(new String[]{
                        getString(R.string.export_library),
                        getString(R.string.import_library)
                }, (dialog, which) -> {
                    if (which == 0) {
                        // Export
                        exportLibrary();
                    } else {
                        // Import
                        importLibrary();
                    }
                })
                .show();
    }

    private void showServerModeDialog() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.server_mode)
                .setMessage(R.string.server_mode_description)
                .setPositiveButton(R.string.start_server, (dialog, which) -> {
                    startServerMode();
                })
                .setNegativeButton(R.string.stop_server, (dialog, which) -> {
                    stopServerMode();
                })
                .setNeutralButton(R.string.cancel, null)
                .show();
    }

    private void exportLibrary() {
        // TODO: Implement export functionality
    }

    private void importLibrary() {
        // TODO: Implement import functionality
    }

    private void startServerMode() {
        // TODO: Start local server service
    }

    private void stopServerMode() {
        // TODO: Stop local server service
    }
}
