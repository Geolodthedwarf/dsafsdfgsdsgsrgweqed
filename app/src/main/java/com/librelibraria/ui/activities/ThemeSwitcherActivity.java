package com.librelibraria.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.librelibraria.R;
import com.librelibraria.ui.adapterutil.ThemeAdapter;
import com.librelibraria.ui.util.AppAnimations;
import com.librelibraria.ui.util.ThemeManager;

public class ThemeSwitcherActivity extends AppCompatActivity {
    
    private ThemeManager themeManager;
    private ThemeAdapter adapter;
    private RadioGroup rgMode;
    private RadioButton rbSystem, rbLight, rbDark;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_theme_switcher);
        
        themeManager = ThemeManager.getInstance(this);
        
        initViews();
        setupToolbar();
        setupThemeGrid();
        setupModeSelection();
    }
    
    private void initViews() {
        RecyclerView rvThemes = findViewById(R.id.rv_themes);
        rgMode = findViewById(R.id.rg_mode);
        rbSystem = findViewById(R.id.rb_system);
        rbLight = findViewById(R.id.rb_light);
        rbDark = findViewById(R.id.rb_dark);
        
        adapter = new ThemeAdapter(themeManager.getCurrentThemeId(), themeId -> {
            themeManager.setTheme(themeId);
            AppAnimations.pulse(findViewById(R.id.rv_themes));
        });
        
        rvThemes.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvThemes.setAdapter(adapter);
    }
    
    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }
    
    private void setupModeSelection() {
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        
        switch (currentMode) {
            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
                rbSystem.setChecked(true);
                break;
            case AppCompatDelegate.MODE_NIGHT_NO:
                rbLight.setChecked(true);
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                rbDark.setChecked(true);
                break;
        }
        
        rgMode.setOnCheckedChangeListener((group, checkedId) -> {
            int mode;
            if (checkedId == R.id.rb_system) {
                mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            } else if (checkedId == R.id.rb_light) {
                mode = AppCompatDelegate.MODE_NIGHT_NO;
            } else {
                mode = AppCompatDelegate.MODE_NIGHT_YES;
            }
            AppCompatDelegate.setDefaultNightMode(mode);
            long themeId = checkedId == R.id.rb_system ? 100 : (checkedId == R.id.rb_dark ? 101 : 1);
            themeManager.setTheme(themeId);
        });
    }
    
    private void setupThemeGrid() {
        if (adapter != null) {
            adapter.setSelectedTheme(themeManager.getCurrentThemeId());
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        AppAnimations.fadeIn(findViewById(android.R.id.content));
    }
}