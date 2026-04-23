package com.librelibraria.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.librelibraria.LibreLibrariaApp;
import com.librelibraria.data.model.AppSetting;
import com.librelibraria.data.repository.SettingsRepository;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * ViewModel for Settings screen.
 */
public class SettingsViewModel extends AndroidViewModel {

    private final SettingsRepository settingsRepository;
    private final CompositeDisposable disposables;

    private final MutableLiveData<List<AppSetting>> settings = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> serverStatus = new MutableLiveData<>();
    private final MutableLiveData<Boolean> syncEnabled = new MutableLiveData<>(false);
    private final MutableLiveData<String> databaseUrl = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> settingsSaved = new MutableLiveData<>();

    public SettingsViewModel(@NonNull Application application) {
        super(application);

        LibreLibrariaApp app = (LibreLibrariaApp) application;
        settingsRepository = app.getSettingsRepository();
        disposables = new CompositeDisposable();

        loadSettings();
    }

    public void loadSettings() {
        isLoading.setValue(true);

        disposables.add(
            settingsRepository.getAllSettings()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    settingsList -> {
                        settings.setValue(settingsList);
                        isLoading.setValue(false);
                        // Extract individual settings
                        for (AppSetting setting : settingsList) {
                            applySetting(setting);
                        }
                    },
                    error -> {
                        errorMessage.setValue("Failed to load settings: " + error.getMessage());
                        isLoading.setValue(false);
                    }
                )
        );
    }

    private void applySetting(AppSetting setting) {
        String key = setting.getKey();
        String value = setting.getValue();

        switch (key) {
            case "server_enabled":
                syncEnabled.setValue("true".equals(value));
                break;
            case "database_url":
                databaseUrl.setValue(value);
                break;
            case "server_status":
                serverStatus.setValue(value);
                break;
        }
    }

    public void saveSetting(String key, String value) {
        disposables.add(
            settingsRepository.saveSetting(key, value)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> {
                        settingsSaved.setValue(true);
                        applySetting(new AppSetting(key, value, System.currentTimeMillis()));
                    },
                    error -> errorMessage.setValue("Failed to save setting: " + error.getMessage())
                )
        );
    }

    public void enableServerMode(boolean enable) {
        saveSetting("server_enabled", enable ? "true" : "false");
        syncEnabled.setValue(enable);
    }

    public void setDatabaseUrl(String url) {
        saveSetting("database_url", url);
        databaseUrl.setValue(url);
    }

    public void connectToDatabase(String url) {
        isLoading.setValue(true);
        saveSetting("database_url", url);
        // The actual connection logic would be handled by a service
    }

    public void disconnectFromDatabase() {
        saveSetting("database_url", "");
        databaseUrl.setValue("");
    }

    public void clearAllData() {
        isLoading.setValue(true);
        disposables.add(
            settingsRepository.clearAllData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> {
                        isLoading.setValue(false);
                        loadSettings();
                    },
                    error -> {
                        errorMessage.setValue("Failed to clear data: " + error.getMessage());
                        isLoading.setValue(false);
                    }
                )
        );
    }

    public void exportLibrary() {
        // Export logic would be handled by a service
    }

    public void importLibrary() {
        // Import logic would be handled by a service
    }

    // Getters
    public LiveData<List<AppSetting>> getSettings() {
        return settings;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getServerStatus() {
        return serverStatus;
    }

    public LiveData<Boolean> getSyncEnabled() {
        return syncEnabled;
    }

    public LiveData<String> getDatabaseUrl() {
        return databaseUrl;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getSettingsSaved() {
        return settingsSaved;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
