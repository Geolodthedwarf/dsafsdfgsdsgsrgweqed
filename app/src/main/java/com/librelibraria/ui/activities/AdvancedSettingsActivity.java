package com.librelibraria.ui.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.librelibraria.R;
import com.librelibraria.data.model.AppTheme;
import com.librelibraria.data.storage.FileStorageManager;
import java.io.File;
import java.io.FileWriter;

public class AdvancedSettingsActivity extends AppCompatActivity {
    
    private Toolbar toolbar;
    private EditText etCss;
    private MaterialButton btnSaveCss, btnExportCss;
    private MaterialButton btnPresetPurple, btnPresetBlue, btnPresetGreen, btnPresetOrange, btnPresetPink;
    
    private FileStorageManager storageManager;
    
    private String currentCss = "";
    
    private static final String CSS_PURPLE = ":root {\n" +
        "  --color-primary: #6750A4;\n" +
        "  --color-primary-container: #E8DEF8;\n" +
        "  --color-on-primary: #FFFFFF;\n" +
        "  --color-secondary: #625B71;\n" +
        "  --color-secondary-container: #E8DEF8;\n" +
        "  --color-tertiary: #7D5260;\n" +
        "  --color-background: #FFFBFE;\n" +
        "  --color-surface: #FFFBFE;\n" +
        "  --color-surface-variant: #E7E0EC;\n" +
        "  --color-on-surface: #1C1B1F;\n" +
        "  --color-on-surface-variant: #49454F;\n" +
        "  --color-error: #B3261E;\n" +
        "  --color-outline: #79747E;\n" +
        "}";
    
    private static final String CSS_BLUE = ":root {\n" +
        "  --color-primary: #1976D2;\n" +
        "  --color-primary-container: #BBDEFB;\n" +
        "  --color-on-primary: #FFFFFF;\n" +
        "  --color-secondary: #0288D1;\n" +
        "  --color-secondary-container: #B3E5FC;\n" +
        "  --color-tertiary: #03A9F4;\n" +
        "  --color-background: #FAFAFA;\n" +
        "  --color-surface: #FFFFFF;\n" +
        "  --color-surface-variant: #E0E0E0;\n" +
        "  --color-on-surface: #212121;\n" +
        "  --color-on-surface-variant: #757575;\n" +
        "  --color-error: #D32F2F;\n" +
        "  --color-outline: #9E9E9E;\n" +
        "}";
    
    private static final String CSS_GREEN = ":root {\n" +
        "  --color-primary: #388E3C;\n" +
        "  --color-primary-container: #C8E6C9;\n" +
        "  --color-on-primary: #FFFFFF;\n" +
        "  --color-secondary: #689F38;\n" +
        "  --color-secondary-container: #DCEDC8;\n" +
        "  --color-tertiary: #8BC34A;\n" +
        "  --color-background: #F5F5F5;\n" +
        "  --color-surface: #FFFFFF;\n" +
        "  --color-surface-variant: #E0E0E0;\n" +
        "  --color-on-surface: #212121;\n" +
        "  --color-on-surface-variant: #757575;\n" +
        "  --color-error: #388E3C;\n" +
        "  --color-outline: #9E9E9E;\n" +
        "}";
    
    private static final String CSS_ORANGE = ":root {\n" +
        "  --color-primary: #F57C00;\n" +
        "  --color-primary-container: #FFE0B2;\n" +
        "  --color-on-primary: #FFFFFF;\n" +
        "  --color-secondary: #EF6C00;\n" +
        "  --color-secondary-container: #FFCC80;\n" +
        "  --color-tertiary: #FF9800;\n" +
        "  --color-background: #FFF8F0;\n" +
        "  --color-surface: #FFFFFF;\n" +
        "  --color-surface-variant: #FFE0B2;\n" +
        "  --color-on-surface: #212121;\n" +
        "  --color-on-surface-variant: #757575;\n" +
        "  --color-error: #E65100;\n" +
        "  --color-outline: #9E9E9E;\n" +
        "}";
    
    private static final String CSS_PINK = ":root {\n" +
        "  --color-primary: #E91E63;\n" +
        "  --color-primary-container: #FCE4EC;\n" +
        "  --color-on-primary: #FFFFFF;\n" +
        "  --color-secondary: #C2185B;\n" +
        "  --color-secondary-container: #F8BBD0;\n" +
        "  --color-tertiary: #F06292;\n" +
        "  --color-background: #FCE4EC;\n" +
        "  --color-surface: #FDF5F8;\n" +
        "  --color-surface-variant: #F8BBD0;\n" +
        "  --color-on-surface: #212121;\n" +
        "  --color-on-surface-variant: #757575;\n" +
        "  --color-error: #C2185B;\n" +
        "  --color-outline: #9E9E9E;\n" +
        "}";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_settings);
        
        storageManager = FileStorageManager.getInstance(this);
        
        initViews();
        setupToolbar();
        setupPresets();
        setupButtons();
        loadExistingCss();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etCss = findViewById(R.id.et_css);
        btnSaveCss = findViewById(R.id.btn_save_css);
        btnExportCss = findViewById(R.id.btn_export_css);
        btnPresetPurple = findViewById(R.id.btn_preset_purple);
        btnPresetBlue = findViewById(R.id.btn_preset_blue);
        btnPresetGreen = findViewById(R.id.btn_preset_green);
        btnPresetOrange = findViewById(R.id.btn_preset_orange);
        btnPresetPink = findViewById(R.id.btn_preset_pink);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.advanced_settings);
        }
    }
    
    private void setupPresets() {
        btnPresetPurple.setOnClickListener(v -> applyPreset("purple"));
        btnPresetBlue.setOnClickListener(v -> applyPreset("blue"));
        btnPresetGreen.setOnClickListener(v -> applyPreset("green"));
        btnPresetOrange.setOnClickListener(v -> applyPreset("orange"));
        btnPresetPink.setOnClickListener(v -> applyPreset("pink"));
    }
    
    private void applyPreset(String preset) {
        String css = "";
        switch (preset) {
            case "purple": css = CSS_PURPLE; break;
            case "blue": css = CSS_BLUE; break;
            case "green": css = CSS_GREEN; break;
            case "orange": css = CSS_ORANGE; break;
            case "pink": css = CSS_PINK; break;
        }
        etCss.setText(css);
        etCss.setSelection(css.length());
    }
    
    private void setupButtons() {
        btnSaveCss.setOnClickListener(v -> saveCss());
        btnExportCss.setOnClickListener(v -> exportCss());
    }
    
    private void loadExistingCss() {
        String basePath = storageManager.getBasePath();
        if (basePath != null) {
            File themeDir = new File(basePath, "themes");
            File defaultTheme = new File(themeDir, "custom.css");
            if (defaultTheme.exists()) {
                try {
                    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(defaultTheme));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    reader.close();
                    currentCss = sb.toString();
                    etCss.setText(currentCss);
                } catch (Exception e) {
                    etCss.setText(CSS_PURPLE);
                }
            } else {
                etCss.setText(CSS_PURPLE);
            }
        } else {
            etCss.setText(CSS_PURPLE);
        }
    }
    
    private void saveCss() {
        String css = etCss.getText() != null ? etCss.getText().toString() : "";
        if (css.isEmpty()) {
            Toast.makeText(this, "CSS cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            String basePath = storageManager.getBasePath();
            if (basePath == null) {
                Toast.makeText(this, "No library folder selected", Toast.LENGTH_SHORT).show();
                return;
            }
            
            File themeDir = new File(basePath, "themes");
            if (!themeDir.exists()) themeDir.mkdirs();
            
            File cssFile = new File(themeDir, "custom.css");
            FileWriter writer = new FileWriter(cssFile);
            writer.write(css);
            writer.close();
            
            AppTheme theme = parseCssToTheme(css);
            storageManager.saveTheme(theme);
            
            Toast.makeText(this, R.string.theme_saved, Toast.LENGTH_SHORT).show();
            
            getSharedPreferences("theme_prefs", MODE_PRIVATE)
                .edit().putBoolean("custom_css", true).apply();
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving CSS: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void exportCss() {
        String css = etCss.getText() != null ? etCss.getText().toString() : "";
        try {
            File exportDir = getExternalFilesDir(null);
            if (exportDir != null) {
                File cssFile = new File(exportDir, "librelibraria_theme.css");
                FileWriter writer = new FileWriter(cssFile);
                writer.write(css);
                writer.close();
                Toast.makeText(this, "Exported to " + cssFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error exporting: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private AppTheme parseCssToTheme(String css) {
        int primary = parseColor(css, "--color-primary");
        int primaryContainer = parseColor(css, "--color-primary-container");
        int onPrimary = parseColor(css, "--color-on-primary");
        int secondary = parseColor(css, "--color-secondary");
        int secondaryContainer = parseColor(css, "--color-secondary-container");
        int tertiary = parseColor(css, "--color-tertiary");
        int tertiaryContainer = parseColor(css, "--color-tertiary-container");
        int background = parseColor(css, "--color-background");
        int surface = parseColor(css, "--color-surface");
        int surfaceVariant = parseColor(css, "--color-surface-variant");
        int onSurface = parseColor(css, "--color-on-surface");
        int onSurfaceVariant = parseColor(css, "--color-on-surface-variant");
        int error = parseColor(css, "--color-error");
        int outline = parseColor(css, "--color-outline");
        
        return AppTheme.createDefault();
    }
    
    private int parseColor(String css, String varName) {
        try {
            int start = css.indexOf(varName);
            if (start < 0) return 0;
            int colon = css.indexOf(":", start);
            int semicolon = css.indexOf(";", colon);
            if (colon < 0 || semicolon < 0) return 0;
            String hex = css.substring(colon + 1, semicolon).trim();
            hex = hex.replace("#", "");
            return (int) Long.parseLong(hex, 16) | 0xFF000000;
        } catch (Exception e) {
            return 0;
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}