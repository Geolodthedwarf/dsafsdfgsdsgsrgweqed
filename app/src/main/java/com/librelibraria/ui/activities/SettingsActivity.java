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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.librelibraria.LibreLibrariaApp;
import com.librelibraria.R;
import com.librelibraria.ui.viewmodels.SettingsViewModel;

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
    private SwitchCompat switchServerEnabled;
    private TextView tvServerStatus;
    private TextView tvDatabaseUrl;

    private SettingsViewModel viewModel;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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

        llAbout.setOnClickListener(v -> showAboutDialog());
        llClearData.setOnClickListener(v -> showClearDataConfirmation());
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

    private void showClearDataConfirmation() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete)
                .setMessage("Are you sure you want to clear all data? This action cannot be undone.")
                .setPositiveButton(R.string.delete, (dialog, which) -> viewModel.clearAllData())
                .setNegativeButton(R.string.cancel, null)
                .show();
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
