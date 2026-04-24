package com.librelibraria.ui.util;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import com.librelibraria.LibreLibrariaApp;
import com.librelibraria.R;
import com.librelibraria.data.model.AppTheme;

public class ThemeManager {
    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME_ID = "theme_id";
    private static final long SYSTEM_THEME_ID = 100;
    private static final long DARK_THEME_ID = 101;
    
    private final SharedPreferences prefs;
    private static ThemeManager instance;
    
    private ThemeManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public static synchronized ThemeManager getInstance(Context context) {
        if (instance == null) {
            instance = new ThemeManager(context.getApplicationContext());
        }
        return instance;
    }
    
    public long getCurrentThemeId() {
        return prefs.getLong(KEY_THEME_ID, SYSTEM_THEME_ID);
    }
    
    public void setTheme(long themeId) {
        prefs.edit().putLong(KEY_THEME_ID, themeId).apply();
        applyTheme(themeId);
    }
    
    public void applyTheme(long themeId) {
        switch ((int) themeId) {
            case (int) SYSTEM_THEME_ID:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case (int) DARK_THEME_ID:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
        }
    }
    
    public void applySavedTheme() {
        applyTheme(getCurrentThemeId());
    }
    
    public AppTheme getCurrentTheme() {
        long id = getCurrentThemeId();
        for (int i = 1; i <= 8; i++) {
            if (id == i) {
                return AppTheme.createDefault();
            }
        }
        return AppTheme.createDefault();
    }
    
    public int[] getThemeColor(Context context, long themeId, String colorType) {
        int colorRes;
        switch ((int) themeId) {
            case 1: colorRes = getColorRes(context, "theme_purple", colorType); break;
            case 2: colorRes = getColorRes(context, "theme_blue", colorType); break;
            case 3: colorRes = getColorRes(context, "theme_green", colorType); break;
            case 4: colorRes = getColorRes(context, "theme_orange", colorType); break;
            case 5: colorRes = getColorRes(context, "theme_pink", colorType); break;
            case 6: colorRes = getColorRes(context, "theme_teal", colorType); break;
            case 7: colorRes = getColorRes(context, "theme_deeppurple", colorType); break;
            case 8: colorRes = getColorRes(context, "theme_cyan", colorType); break;
            default: colorRes = getColorRes(context, "theme_purple", colorType); break;
        }
        return new int[] { colorRes };
    }
    
    private int getColorRes(Context context, String theme, String colorType) {
        String colorName = theme + "_" + colorType;
        int resId = context.getResources().getIdentifier(colorName, "color", context.getPackageName());
        return resId != 0 ? resId : R.color.theme_purple_primary;
    }
    
    public int getThemePrimaryColor(Context context) {
        long id = getCurrentThemeId();
        if (id == SYSTEM_THEME_ID || id == DARK_THEME_ID) {
            return AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES 
                ? 0xFFD0BCFF : 0xFF6750A4;
        }
        String themeName = getThemeName(id);
        int resId = context.getResources().getIdentifier(themeName + "_primary", "color", context.getPackageName());
        return resId != 0 ? context.getColor(resId) : context.getColor(R.color.theme_purple_primary);
    }
    
    private String getThemeName(long id) {
        switch ((int) id) {
            case 1: return "theme_purple";
            case 2: return "theme_blue";
            case 3: return "theme_green";
            case 4: return "theme_orange";
            case 5: return "theme_pink";
            case 6: return "theme_teal";
            case 7: return "theme_deeppurple";
            case 8: return "theme_cyan";
            default: return "theme_purple";
        }
    }
}