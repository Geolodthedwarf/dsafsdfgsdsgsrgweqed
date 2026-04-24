package com.librelibraria.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.librelibraria.R;
import com.librelibraria.ui.util.ThemeManager;

public class SplashActivity extends AppCompatActivity {
    
    private static final int SPLASH_DELAY = 1500;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ThemeManager.getInstance(this).applySavedTheme();
        
        setContentView(R.layout.activity_splash);
        
        new Handler(Looper.getMainLooper()).postDelayed(this::checkIntroAndNavigate, SPLASH_DELAY);
    }
    
    private void checkIntroAndNavigate() {
        boolean introCompleted = getSharedPreferences("app_prefs", MODE_PRIVATE)
            .getBoolean("intro_completed", false);
        
        Intent intent;
        if (!introCompleted) {
            intent = new Intent(this, IntroActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}