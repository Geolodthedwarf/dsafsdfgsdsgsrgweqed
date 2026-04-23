package com.librelibraria.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

/**
 * Repository for app settings and preferences.
 */
public class SettingsRepository {

    private static final String KEY_SERVER_URL = "server_url";
    private static final String KEY_SERVER_PORT = "server_port";
    private static final String KEY_SERVER_ENABLED = "server_enabled";
    private static final String KEY_SYNC_ENABLED = "sync_enabled";
    private static final String KEY_SYNC_WIFI_ONLY = "sync_wifi_only";
    private static final String KEY_THEME = "theme";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_LAST_SYNC = "last_sync";

    private final SharedPreferences preferences;

    public SettingsRepository(Context context) {
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    // Server settings
    public String getServerUrl() {
        return preferences.getString(KEY_SERVER_URL, "");
    }

    public void setServerUrl(String url) {
        preferences.edit().putString(KEY_SERVER_URL, url).apply();
    }

    public int getServerPort() {
        return preferences.getInt(KEY_SERVER_PORT, 8080);
    }

    public void setServerPort(int port) {
        preferences.edit().putInt(KEY_SERVER_PORT, port).apply();
    }

    public boolean isServerEnabled() {
        return preferences.getBoolean(KEY_SERVER_ENABLED, false);
    }

    public void setServerEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_SERVER_ENABLED, enabled).apply();
    }

    // Sync settings
    public boolean isSyncEnabled() {
        return preferences.getBoolean(KEY_SYNC_ENABLED, true);
    }

    public void setSyncEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_SYNC_ENABLED, enabled).apply();
    }

    public boolean isSyncWifiOnly() {
        return preferences.getBoolean(KEY_SYNC_WIFI_ONLY, true);
    }

    public void setSyncWifiOnly(boolean wifiOnly) {
        preferences.edit().putBoolean(KEY_SYNC_WIFI_ONLY, wifiOnly).apply();
    }

    public long getLastSyncTime() {
        return preferences.getLong(KEY_LAST_SYNC, 0);
    }

    public void setLastSyncTime(long time) {
        preferences.edit().putLong(KEY_LAST_SYNC, time).apply();
    }

    // Appearance settings
    public String getTheme() {
        return preferences.getString(KEY_THEME, "default");
    }

    public void setTheme(String theme) {
        preferences.edit().putString(KEY_THEME, theme).apply();
    }

    public String getLanguage() {
        return preferences.getString(KEY_LANGUAGE, "uk");
    }

    public void setLanguage(String language) {
        preferences.edit().putString(KEY_LANGUAGE, language).apply();
    }

    public boolean isDarkMode() {
        return preferences.getBoolean(KEY_DARK_MODE, false);
    }

    public void setDarkMode(boolean darkMode) {
        preferences.edit().putBoolean(KEY_DARK_MODE, darkMode).apply();
    }

    // Clear all settings
    public void clearAll() {
        preferences.edit().clear().apply();
    }
}
