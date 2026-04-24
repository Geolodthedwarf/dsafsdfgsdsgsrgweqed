package com.librelibraria.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "app_presets")
public class AppPreset {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;
    private String theme;
    private int primaryColor;
    private int accentColor;
    private boolean darkMode;
    private float cornerRadius;
    private int elevation;
    private String fontSize;
    private boolean compactMode;
    private long createdAt;
    private long updatedAt;

    public AppPreset() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public int getPrimaryColor() { return primaryColor; }
    public void setPrimaryColor(int primaryColor) { this.primaryColor = primaryColor; }

    public int getAccentColor() { return accentColor; }
    public void setAccentColor(int accentColor) { this.accentColor = accentColor; }

    public boolean isDarkMode() { return darkMode; }
    public void setDarkMode(boolean darkMode) { this.darkMode = darkMode; }

    public float getCornerRadius() { return cornerRadius; }
    public void setCornerRadius(float cornerRadius) { this.cornerRadius = cornerRadius; }

    public int getElevation() { return elevation; }
    public void setElevation(int elevation) { this.elevation = elevation; }

    public String getFontSize() { return fontSize; }
    public void setFontSize(String fontSize) { this.fontSize = fontSize; }

    public boolean isCompactMode() { return compactMode; }
    public void setCompactMode(boolean compactMode) { this.compactMode = compactMode; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}