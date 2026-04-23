package com.librelibraria.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * AppSetting entity for storing key-value application settings.
 */
@Entity(tableName = "app_settings")
public class AppSetting {

    @PrimaryKey
    @NonNull
    private String key;

    private String value;
    private long updatedAt;

    public AppSetting() {
        this.key = "";
        this.updatedAt = System.currentTimeMillis();
    }

    @Ignore
    public AppSetting(String key, String value) {
        this();
        this.key = key;
        this.value = value;
    }

    @Ignore
    public AppSetting(String key, String value, long updatedAt) {
        this.key = key;
        this.value = value;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    @NonNull
    public String getKey() {
        return key;
    }

    public void setKey(@NonNull String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
        this.updatedAt = System.currentTimeMillis();
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper methods
    public int getIntValue() {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public boolean getBooleanValue() {
        return "true".equalsIgnoreCase(value);
    }

    public void setIntValue(int intValue) {
        this.value = String.valueOf(intValue);
        this.updatedAt = System.currentTimeMillis();
    }

    public void setBooleanValue(boolean boolValue) {
        this.value = String.valueOf(boolValue);
        this.updatedAt = System.currentTimeMillis();
    }
}
