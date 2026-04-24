package com.librelibraria.ui.activities;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.librelibraria.R;
import com.librelibraria.data.model.AppTheme;
import com.librelibraria.data.storage.FileStorageManager;
import java.io.File;

public class ThemeEditorActivity extends AppCompatActivity {
    
    private Toolbar toolbar;
    private TextInputEditText etThemeName;
    private MaterialCardView cardPreview;
    private TextView tvPreviewTitle, tvPreviewSubtitle;
    private MaterialButton btnPreviewAction;
    
    private View viewPrimaryColor, viewSecondaryColor, viewTertiaryColor, viewBackgroundColor, viewSurfaceColor;
    private TextView tvPrimaryHex, tvSecondaryHex, tvTertiaryHex, tvBackgroundHex, tvSurfaceHex;
    
    private LinearLayout llPrimaryColor, llSecondaryColor, llTertiaryColor, llBackgroundColor, llSurfaceColor;
    
    private MaterialButton btnDeleteTheme, btnSaveTheme, btnApplyTheme;
    
    private FileStorageManager storageManager;
    
    private int primaryColor = 0xFF6750A4;
    private int secondaryColor = 0xFF625B71;
    private int tertiaryColor = 0xFF7D5260;
    private int backgroundColor = 0xFFFFFBFE;
    private int surfaceColor = 0xFFFFFBFE;
    
    private long themeId = 0;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_editor);
        
        storageManager = FileStorageManager.getInstance(this);
        
        themeId = getIntent().getLongExtra("theme_id", 0);
        isEditMode = themeId > 0;
        
        initViews();
        setupToolbar();
        setupColorPickers();
        setupButtons();
        
        if (isEditMode) {
            loadExistingTheme();
        }
        
        updatePreview();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etThemeName = findViewById(R.id.et_theme_name);
        cardPreview = findViewById(R.id.card_preview);
        tvPreviewTitle = findViewById(R.id.tv_preview_title);
        tvPreviewSubtitle = findViewById(R.id.tv_preview_subtitle);
        btnPreviewAction = findViewById(R.id.btn_preview_action);
        
        viewPrimaryColor = findViewById(R.id.view_primary_color);
        viewSecondaryColor = findViewById(R.id.view_secondary_color);
        viewTertiaryColor = findViewById(R.id.view_tertiary_color);
        viewBackgroundColor = findViewById(R.id.view_background_color);
        viewSurfaceColor = findViewById(R.id.view_surface_color);
        
        tvPrimaryHex = findViewById(R.id.tv_primary_hex);
        tvSecondaryHex = findViewById(R.id.tv_secondary_hex);
        tvTertiaryHex = findViewById(R.id.tv_tertiary_hex);
        tvBackgroundHex = findViewById(R.id.tv_background_hex);
        tvSurfaceHex = findViewById(R.id.tv_surface_hex);
        
        llPrimaryColor = findViewById(R.id.ll_primary_color);
        llSecondaryColor = findViewById(R.id.ll_secondary_color);
        llTertiaryColor = findViewById(R.id.ll_tertiary_color);
        llBackgroundColor = findViewById(R.id.ll_background_color);
        llSurfaceColor = findViewById(R.id.ll_surface_color);
        
        btnDeleteTheme = findViewById(R.id.btn_delete_theme);
        btnSaveTheme = findViewById(R.id.btn_save_theme);
        btnApplyTheme = findViewById(R.id.btn_apply_theme);
        
        btnDeleteTheme.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isEditMode ? R.string.theme_editor : R.string.customize_colors);
        }
    }

    private void setupColorPickers() {
        llPrimaryColor.setOnClickListener(v -> showColorPicker(0, "Primary"));
        llSecondaryColor.setOnClickListener(v -> showColorPicker(1, "Secondary"));
        llTertiaryColor.setOnClickListener(v -> showColorPicker(2, "Tertiary"));
        llBackgroundColor.setOnClickListener(v -> showColorPicker(3, "Background"));
        llSurfaceColor.setOnClickListener(v -> showColorPicker(4, "Surface"));
    }

    private void showColorPicker(int colorType, String colorName) {
        int currentColor = getColorForType(colorType);
        String currentHex = String.format("#%06X", currentColor & 0xFFFFFF);
        
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(colorName + " Color");
        
        String[] presetNames = {"Purple", "Blue", "Green", "Orange", "Pink", "Teal", "Deep Purple", "Cyan", "Deep Orange", "Brown", "Custom Hex..."};
        final int[] presetColors = {
            0xFF6750A4, 0xFF1976D2, 0xFF388E3C, 0xFFF57C00, 0xFFE91E63,
            0xFF00796B, 0xFF512DA8, 0xFF00ACC1, 0xFFFF5722, 0xFF795548
        };
        
        builder.setItems(presetNames, (dialog, which) -> {
            if (which == presetColors.length) {
                showCustomHexInput(colorType, colorName);
            } else {
                setColorForType(colorType, presetColors[which]);
                updateColorViews();
                updatePreview();
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void showCustomHexInput(int colorType, String colorName) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Enter " + colorName + " Hex Color");
        
        com.google.android.material.textfield.TextInputLayout til = new com.google.android.material.textfield.TextInputLayout(this);
        til.setBoxBackgroundMode(com.google.android.material.textfield.TextInputLayout.BOX_BACKGROUND_OUTLINE);
        til.setHint("Hex Color (e.g. #FF5722)");
        
        com.google.android.material.textfield.TextInputEditText editText = new com.google.android.material.textfield.TextInputEditText(this);
        editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        editText.setText(String.format("#%06X", getColorForType(colorType) & 0xFFFFFF));
        
        til.addView(editText);
        builder.setView(til);
        
        builder.setPositiveButton("Apply", (dialog, which) -> {
            String hex = editText.getText() != null ? editText.getText().toString().trim() : "";
            int color = parseHexColor(hex);
            if (color != 0) {
                setColorForType(colorType, color);
                updateColorViews();
                updatePreview();
            } else {
                Toast.makeText(this, "Invalid hex color", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private int parseHexColor(String hex) {
        if (hex == null || hex.isEmpty()) return 0;
        hex = hex.trim();
        if (!hex.startsWith("#")) {
            hex = "#" + hex;
        }
        try {
            return Color.parseColor(hex);
        } catch (Exception e) {
            return 0;
        }
    }

    private int getColorForType(int type) {
        switch (type) {
            case 0: return primaryColor;
            case 1: return secondaryColor;
            case 2: return tertiaryColor;
            case 3: return backgroundColor;
            case 4: return surfaceColor;
            default: return Color.BLACK;
        }
    }

    private void setColorForType(int type, int color) {
        switch (type) {
            case 0: primaryColor = color; break;
            case 1: secondaryColor = color; break;
            case 2: tertiaryColor = color; break;
            case 3: backgroundColor = color; break;
            case 4: surfaceColor = color; break;
        }
    }

    private void updateColorViews() {
        viewPrimaryColor.setBackgroundColor(primaryColor);
        viewSecondaryColor.setBackgroundColor(secondaryColor);
        viewTertiaryColor.setBackgroundColor(tertiaryColor);
        viewBackgroundColor.setBackgroundColor(backgroundColor);
        viewSurfaceColor.setBackgroundColor(surfaceColor);
        
        tvPrimaryHex.setText(String.format("#%06X", primaryColor & 0xFFFFFF));
        tvSecondaryHex.setText(String.format("#%06X", secondaryColor & 0xFFFFFF));
        tvTertiaryHex.setText(String.format("#%06X", tertiaryColor & 0xFFFFFF));
        tvBackgroundHex.setText(String.format("#%06X", backgroundColor & 0xFFFFFF));
        tvSurfaceHex.setText(String.format("#%06X", surfaceColor & 0xFFFFFF));
    }

    private void updatePreview() {
        cardPreview.setCardBackgroundColor(surfaceColor);
        tvPreviewTitle.setTextColor(primaryColor);
        tvPreviewSubtitle.setTextColor(secondaryColor);
        btnPreviewAction.setBackgroundColor(primaryColor);
        
        updateColorViews();
    }

    private void loadExistingTheme() {
        if (getBasePath() == null) return;
        
        File dir = new File(getBasePath(), "themes");
        if (dir.exists()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".bct"));
            if (files != null) {
                for (File file : files) {
                    String name = file.getName();
                    if (name.contains("_" + themeId + ".bct")) {
                        storageManager.loadTheme(file);
                        break;
                    }
                }
            }
        }
    }

    private java.io.File getBasePath() {
        return new java.io.File(storageManager.getBasePath());
    }

    private void setupButtons() {
        btnSaveTheme.setOnClickListener(v -> saveTheme());
        btnApplyTheme.setOnClickListener(v -> applyTheme());
        btnDeleteTheme.setOnClickListener(v -> deleteTheme());
    }

    private void saveTheme() {
        String name = etThemeName.getText() != null ? etThemeName.getText().toString() : "";
        if (name.isEmpty()) {
            name = "Custom Theme " + System.currentTimeMillis();
        }
        
        if (themeId == 0) {
            themeId = System.currentTimeMillis();
        }
        
        AppTheme theme = new AppTheme(
            themeId, name, primaryColor, secondaryColor, tertiaryColor,
            backgroundColor, surfaceColor, false, false
        );
        
        boolean saved = storageManager.saveTheme(theme);
        if (saved) {
            Toast.makeText(this, R.string.theme_saved, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, R.string.error_creating_folders, Toast.LENGTH_SHORT).show();
        }
    }

    private void applyTheme() {
        saveTheme();
        
        getSharedPreferences("theme_prefs", MODE_PRIVATE)
            .edit().putLong("theme_id", themeId).apply();
        
        Toast.makeText(this, R.string.theme_saved, Toast.LENGTH_SHORT).show();
    }

    private void deleteTheme() {
        if (themeId > 0) {
            File themesDir = new File(getBasePath(), "themes");
            File themeFile = new File(themesDir, "theme_" + themeId + ".bct");
            if (themeFile.exists()) {
                themeFile.delete();
            }
            Toast.makeText(this, R.string.theme_deleted, Toast.LENGTH_SHORT).show();
            finish();
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