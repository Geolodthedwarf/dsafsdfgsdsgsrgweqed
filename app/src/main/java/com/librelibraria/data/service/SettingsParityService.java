package com.librelibraria.data.service;

import com.librelibraria.data.database.AppSettingDao;
import com.librelibraria.data.model.AppSetting;
import com.librelibraria.data.repository.SettingsRepository;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Service for managing application settings parity between local and remote storage.
 */
public class SettingsParityService {

    private final AppSettingDao appSettingDao;
    private final SettingsRepository settingsRepository;

    public SettingsParityService(AppSettingDao appSettingDao, SettingsRepository settingsRepository) {
        this.appSettingDao = appSettingDao;
        this.settingsRepository = settingsRepository;
    }

    /**
     * Save a setting locally.
     */
    public Completable saveSetting(String key, String value) {
        AppSetting setting = new AppSetting(key, value);
        return appSettingDao.insert(setting);
    }

    /**
     * Get a setting by key.
     */
    public Single<String> getSetting(String key) {
        return appSettingDao.getByKey(key)
                .map(setting -> setting != null ? setting.getValue() : null)
                .onErrorReturnItem(null);
    }

    /**
     * Get all settings.
     */
    public Single<List<AppSetting>> getAllSettings() {
        return appSettingDao.getAllSettingsSync();
    }

    /**
     * Delete a setting.
     */
    public Completable deleteSetting(String key) {
        return appSettingDao.deleteByKey(key);
    }

    /**
     * Sync settings to remote server.
     */
    public Completable syncToRemote() {
        return appSettingDao.getAllSettingsSync()
                .flatMapCompletable(settings -> {
                    // Convert settings to JSON and sync
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    String json = gson.toJson(settings);
                    return settingsRepository.saveSettingsToRemote(json);
                })
                .subscribeOn(Schedulers.io());
    }

    /**
     * Sync settings from remote server.
     */
    public Completable syncFromRemote() {
        return settingsRepository.getSettingsFromRemote()
                .flatMapCompletable(json -> {
                    if (json == null || json.isEmpty()) {
                        return Completable.complete();
                    }
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<List<AppSetting>>(){}.getType();
                    List<AppSetting> settings = gson.fromJson(json, listType);
                    return Completable.fromAction(() -> {
                        for (AppSetting setting : settings) {
                            appSettingDao.insert(setting).blockingAwait();
                        }
                    });
                })
                .subscribeOn(Schedulers.io());
    }

    /**
     * Sync all settings (bidirectional).
     */
    public Completable syncAll() {
        return syncToRemote()
                .andThen(syncFromRemote());
    }

    /**
     * Check if sync is needed.
     */
    public Single<Boolean> isSyncNeeded() {
        return appSettingDao.getAllSettingsSync()
                .map(settings -> !settings.isEmpty())
                .onErrorReturnItem(false);
    }

    /**
     * Get setting with default value.
     */
    public String getSettingWithDefault(String key, String defaultValue) {
        try {
            return appSettingDao.getByKey(key)
                    .map(AppSetting::getValue)
                    .blockingGet();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Save boolean setting.
     */
    public Completable saveBooleanSetting(String key, boolean value) {
        return saveSetting(key, String.valueOf(value));
    }

    /**
     * Get boolean setting.
     */
    public Boolean getBooleanSetting(String key, boolean defaultValue) {
        String value = getSettingWithDefault(key, null);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    /**
     * Save integer setting.
     */
    public Completable saveIntSetting(String key, int value) {
        return saveSetting(key, String.valueOf(value));
    }

    /**
     * Get integer setting.
     */
    public Integer getIntSetting(String key, int defaultValue) {
        String value = getSettingWithDefault(key, null);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
