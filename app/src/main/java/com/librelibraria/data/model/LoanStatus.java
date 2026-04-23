package com.librelibraria.data.model;

/**
 * Enum representing the status of a loan.
 */
public enum LoanStatus {
    ACTIVE("Active", "📖"),
    RETURNED("Returned", "✅"),
    OVERDUE("Overdue", "⚠️"),
    LOST("Lost", "❌");

    private final String displayName;
    private final String emoji;

    LoanStatus(String displayName, String emoji) {
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

    public static LoanStatus fromString(String value) {
        if (value == null) return ACTIVE;
        for (LoanStatus status : values()) {
            if (status.name().equalsIgnoreCase(value) ||
                status.displayName.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return ACTIVE;
    }
}
