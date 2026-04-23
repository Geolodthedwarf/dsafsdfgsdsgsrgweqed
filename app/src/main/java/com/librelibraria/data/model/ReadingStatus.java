package com.librelibraria.data.model;

/**
 * Enum representing reading status of a book.
 */
public enum ReadingStatus {
    WANT("Want to Read", "📖"),
    READING("Currently Reading", "📚"),
    READ("Already Read", "✅"),
    OWN("Own", "📦"),
    STOPPED("Stopped", "⏹️"),
    PROCESSING("Processing", "⚙️");

    private final String displayName;
    private final String emoji;

    ReadingStatus(String displayName, String emoji) {
        this.displayName = displayName;
        this.emoji = emoji;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getFullDisplay() {
        return emoji + " " + displayName;
    }

    public static ReadingStatus fromString(String value) {
        if (value == null) return OWN;
        for (ReadingStatus status : values()) {
            if (status.name().equalsIgnoreCase(value) ||
                status.displayName.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return OWN;
    }
}
