package com.librelibraria.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.librelibraria.LibreLibrariaApp;
import com.librelibraria.R;
import com.librelibraria.data.service.AutomationService;
import com.librelibraria.service.LocalServerService;
import com.librelibraria.ui.activities.DiaryActivity;
import com.librelibraria.ui.activities.SettingsActivity;
import com.librelibraria.ui.activities.StatisticsActivity;
import com.librelibraria.ui.activities.TagsActivity;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

/**
 * Fragment for additional features (statistics, diary, tags, settings).
 */
public class MoreFragment extends Fragment {
    private final CompositeDisposable disposables = new CompositeDisposable();

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
        LibreLibrariaApp app = (LibreLibrariaApp) requireActivity().getApplication();
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.import_export)
                .setItems(new String[]{
                        getString(R.string.export_library),
                        getString(R.string.import_library),
                        "Refresh inventory status"
                }, (dialog, which) -> {
                    if (which == 0) {
                        exportLibrary();
                    } else if (which == 1) {
                        importLibrary();
                    } else {
                        disposables.add(app.getAutomationService().refreshInventoryStatus()
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        () -> {},
                                        error -> {}
                                ));
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
        LibreLibrariaApp app = (LibreLibrariaApp) requireActivity().getApplication();
        disposables.add(app.getSyncManager().exportLibrary()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        payload -> new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                                .setTitle(R.string.export_library)
                                .setMessage(payload.length() > 1200 ? payload.substring(0, 1200) + "..." : payload)
                                .setPositiveButton(R.string.ok, null)
                                .show(),
                        error -> {}
                ));
    }

    private void importLibrary() {
        EditText input = new EditText(requireContext());
        input.setHint(getString(R.string.bcl_json_payload));
        input.setMinLines(6);
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.import_library)
                .setView(input)
                .setPositiveButton(R.string.import_library, (d, w) -> {
                    LibreLibrariaApp app = (LibreLibrariaApp) requireActivity().getApplication();
                    disposables.add(app.getSyncManager().importLibrary(input.getText() == null ? "" : input.getText().toString())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    count -> {},
                                    error -> {}
                            ));
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void startServerMode() {
        Intent intent = new Intent(requireContext(), LocalServerService.class);
        intent.setAction(LocalServerService.ACTION_START);
        requireContext().startService(intent);
    }

    private void stopServerMode() {
        Intent intent = new Intent(requireContext(), LocalServerService.class);
        intent.setAction(LocalServerService.ACTION_STOP);
        requireContext().startService(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.clear();
    }
}
