package com.librelibraria.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.librelibraria.LibreLibrariaApp;
import com.librelibraria.R;
import com.librelibraria.data.service.ExportService;
import com.librelibraria.data.storage.FileStorageManager;
import com.librelibraria.ui.viewmodels.SettingsViewModel;
import com.librelibraria.ui.activities.ThemeSwitcherActivity;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

/**
 * Activity for managing application settings.
 */
public class SettingsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private LinearLayout llServerMode;
    private LinearLayout llAbout;
    private LinearLayout llClearData;
    private LinearLayout llDarkMode;
    private LinearLayout llThemeEditor;
    private LinearLayout llAdvancedSettings;
    private SwitchCompat switchServerEnabled;
    private TextView tvServerStatus;
    private TextView tvDatabaseUrl;

    private SettingsViewModel viewModel;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private ActivityResultLauncher<String> importBclLauncher;
    private ActivityResultLauncher<String> exportBclLauncher;
    private android.net.Uri pendingImportUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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

        initViews();
        setupToolbar();
        setupViewModel();
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);

        llServerMode = findViewById(R.id.ll_server_sync);
        llAbout = findViewById(R.id.ll_about);
        llClearData = findViewById(R.id.ll_refresh_status);
        llDarkMode = findViewById(R.id.ll_dark_mode);
        llThemeEditor = findViewById(R.id.ll_theme_editor);
        llAdvancedSettings = findViewById(R.id.ll_advanced_settings);

        switchServerEnabled = findViewById(R.id.switch_sync_enabled);
        tvServerStatus = findViewById(R.id.tv_server_url);
        tvDatabaseUrl = findViewById(R.id.tv_current_language);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.settings);
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        viewModel.getSyncEnabled().observe(this, enabled -> {
            if (enabled != null) {
                switchServerEnabled.setChecked(enabled);
            }
        });

        viewModel.getDatabaseUrl().observe(this, url -> {
            if (url != null && !url.isEmpty()) {
                tvDatabaseUrl.setText(url);
            } else {
                tvDatabaseUrl.setText(R.string.disconnected);
            }
        });

        viewModel.getServerStatus().observe(this, status -> {
            if (status != null) {
                tvServerStatus.setText(status);
            } else {
                tvServerStatus.setText(R.string.server_stopped);
            }
        });

    }

    private void setupListeners() {
        llServerMode.setOnClickListener(v -> showServerSettingsDialog());

        switchServerEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.enableServerMode(isChecked);
        });

        llDarkMode.setOnClickListener(v -> {
            startActivity(new Intent(this, ThemeSwitcherActivity.class));
        });

        llAbout.setOnClickListener(v -> showAboutDialog());
        llClearData.setOnClickListener(v -> showImportExportDialog());
llThemeEditor.setOnClickListener(v -> openThemeEditor());
        llAdvancedSettings.setOnClickListener(v -> openAdvancedSettings());
    }

    private void openAdvancedSettings() {
        startActivity(new Intent(this, AdvancedSettingsActivity.class));
    }

    private void openThemeEditor() {
        startActivity(new Intent(this, ThemeEditorActivity.class));
    }

    private void showServerSettingsDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_server_settings, null);
        com.google.android.material.textfield.TextInputEditText etDatabaseUrl = dialogView.findViewById(R.id.et_server_url);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.database_url)
                .setView(dialogView)
                .setPositiveButton(R.string.connect, (dialog, which) -> {
                    String url = etDatabaseUrl.getText() != null ? etDatabaseUrl.getText().toString().trim() : "";
                    viewModel.setDatabaseUrl(url);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showAboutDialog() {
        String versionName = "1.0.0";
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
            // Ignore
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.about)
                .setMessage(getString(R.string.app_name) + " v" + versionName + "\n\n" +
                        "A library management system for tracking books, loans, and reading progress.")
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void showImportExportDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.import_export)
                .setItems(new String[]{
                        getString(R.string.export_library),
                        getString(R.string.import_library)
                }, (dialog, which) -> {
                    if (which == 0) {
                        exportBclToDownloads();
                    } else {
                        importBclFromDownloads();
                    }
                })
                .show();
    }

    private void exportBclToDownloads() {
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US)
                .format(new java.util.Date());
        String suggested = "LibreLibraria_Export_" + timestamp + ".bcl";
        exportBclLauncher.launch(suggested);
    }

    private void importBclFromDownloads() {
        importBclLauncher.launch("*/*");
    }

    private void showBclImportOptionsDialog() {
        if (pendingImportUri == null) return;

        String[] options = new String[]{
                "Overwrite (replace existing)",
                "Skip duplicates",
                "Create new (always import as new)"
        };

        new MaterialAlertDialogBuilder(this)
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

    private void importBclWithMode(android.net.Uri uri, FileStorageManager.ImportMode mode) {
        FileStorageManager storage = FileStorageManager.getInstance(this);

        disposables.add(io.reactivex.rxjava3.core.Single.fromCallable(() -> {
                    try (java.io.InputStream in = getContentResolver().openInputStream(uri)) {
                        if (in == null) return 0;
                        return storage.importFromBcl(in, mode);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        count -> Toast.makeText(this, "Imported " + count + " files", Toast.LENGTH_LONG).show(),
                        error -> Toast.makeText(this, "Import failed: " + error.getMessage(), Toast.LENGTH_LONG).show()
                ));
    }

    private void exportBclToUri(android.net.Uri uri) {
        ExportService exportService = new ExportService(this);
        disposables.add(exportService.exportToBCL(uri)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> Toast.makeText(this, "Exported .bcl successfully", Toast.LENGTH_LONG).show(),
                        error -> Toast.makeText(this, "Export failed: " + error.getMessage(), Toast.LENGTH_LONG).show()
                ));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }
}
