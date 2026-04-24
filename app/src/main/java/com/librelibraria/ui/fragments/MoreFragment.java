package com.librelibraria.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.librelibraria.LibreLibrariaApp;
import com.librelibraria.R;
import com.librelibraria.data.service.AutomationService;
import com.librelibraria.data.service.ExportService;
import com.librelibraria.data.storage.FileStorageManager;
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

    private ActivityResultLauncher<String> importBclLauncher;
    private ActivityResultLauncher<String> exportBclLauncher;
    private Uri pendingImportUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_more, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        importBclLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        pendingImportUri = uri;
                        showBclImportOptionsDialog();
                    }
                }
        );

        exportBclLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("application/octet-stream"),
                uri -> {
                    if (uri != null) {
                        exportBclToUri(uri);
                    }
                }
        );

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
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US)
                .format(new java.util.Date());
        String suggested = "LibreLibraria_Export_" + timestamp + ".bcl";
        exportBclLauncher.launch(suggested);
    }

    private void importLibrary() {
        importBclLauncher.launch("*/*");
    }

    private void showBclImportOptionsDialog() {
        if (pendingImportUri == null) return;

        String[] options = new String[]{
                "Overwrite (replace existing)",
                "Skip duplicates",
                "Create new (always import as new)"
        };

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.import_library)
                .setItems(options, (dialog, which) -> {
                    FileStorageManager.ImportMode mode;
                    if (which == 0) mode = FileStorageManager.ImportMode.OVERWRITE;
                    else if (which == 1) mode = FileStorageManager.ImportMode.SKIP;
                    else mode = FileStorageManager.ImportMode.CREATE_NEW;
                    importBclWithMode(pendingImportUri, mode);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void importBclWithMode(Uri uri, FileStorageManager.ImportMode mode) {
        FileStorageManager storage = FileStorageManager.getInstance(requireContext());

        disposables.add(io.reactivex.rxjava3.core.Single.fromCallable(() -> {
                    try (java.io.InputStream in = requireContext().getContentResolver().openInputStream(uri)) {
                        if (in == null) return 0;
                        return storage.importFromBcl(in, mode);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        count -> android.widget.Toast.makeText(requireContext(),
                                "Imported " + count + " files", android.widget.Toast.LENGTH_LONG).show(),
                        error -> android.widget.Toast.makeText(requireContext(),
                                "Import failed: " + error.getMessage(), android.widget.Toast.LENGTH_LONG).show()
                ));
    }

    private void exportBclToUri(Uri uri) {
        ExportService exportService = new ExportService(requireContext());
        disposables.add(exportService.exportToBCL(uri)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> android.widget.Toast.makeText(requireContext(),
                                "Exported .bcl successfully", android.widget.Toast.LENGTH_LONG).show(),
                        error -> android.widget.Toast.makeText(requireContext(),
                                "Export failed: " + error.getMessage(), android.widget.Toast.LENGTH_LONG).show()
                ));
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
