package com.librelibraria.data.model;

import androidx.annotation.NonNull;

public class AppTheme {
    private final long id;
    private final String name;
    private final int primaryColor;
    private final int secondaryColor;
    private final int tertiaryColor;
    private final int backgroundColor;
    private final int surfaceColor;
    private final boolean isDark;
    private final boolean isSystem;

    public AppTheme(long id, @NonNull String name, int primaryColor, int secondaryColor,
                   int tertiaryColor, int backgroundColor, int surfaceColor,
                   boolean isDark, boolean isSystem) {
        this.id = id;
        this.name = name;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        this.tertiaryColor = tertiaryColor;
        this.backgroundColor = backgroundColor;
        this.surfaceColor = surfaceColor;
        this.isDark = isDark;
        this.isSystem = isSystem;
    }

    public static AppTheme createDefault() {
        return new AppTheme(1, "Default", 0xFF6750A4, 0xFF625B71, 0xFF7D5260,
            0xFFFFFBFE, 0xFFFFFBFE, false, true);
    }

    public static AppTheme fromColors(int primary, int secondary, int tertiary, int background, int surface) {
        return new AppTheme(System.currentTimeMillis(), "Custom", primary, secondary,
            tertiary, background, surface, false, false);
    }

    public long getId() { return id; }
    @NonNull public String getName() { return name; }
    public int getPrimaryColor() { return primaryColor; }
    public int getSecondaryColor() { return secondaryColor; }
    public int getTertiaryColor() { return tertiaryColor; }
    public int getBackgroundColor() { return backgroundColor; }
    public int getSurfaceColor() { return surfaceColor; }
    public boolean isDark() { return isDark; }
    public boolean isSystem() { return isSystem; }
    
    public String toCss() {
        return ":root {\n" +
            "  --color-primary: #" + String.format("%06X", primaryColor & 0xFFFFFF) + ";\n" +
            "  --color-secondary: #" + String.format("%06X", secondaryColor & 0xFFFFFF) + ";\n" +
            "  --color-tertiary: #" + String.format("%06X", tertiaryColor & 0xFFFFFF) + ";\n" +
            "  --color-background: #" + String.format("%06X", backgroundColor & 0xFFFFFF) + ";\n" +
            "  --color-surface: #" + String.format("%06X", surfaceColor & 0xFFFFFF) + ";\n" +
            "}";
    }
}