package com.librelibraria.ui.util;

import androidx.annotation.Nullable;

public final class OpenLibraryCover {
    private OpenLibraryCover() {}

    public static @Nullable String medium(@Nullable String isbn) {
        String clean = cleanIsbn(isbn);
        if (clean == null) return null;
        return "https://covers.openlibrary.org/b/isbn/" + clean + "-M.jpg";
    }

    public static @Nullable String large(@Nullable String isbn) {
        String clean = cleanIsbn(isbn);
        if (clean == null) return null;
        return "https://covers.openlibrary.org/b/isbn/" + clean + "-L.jpg";
    }

    private static @Nullable String cleanIsbn(@Nullable String isbn) {
        if (isbn == null) return null;
        String trimmed = isbn.trim();
        if (trimmed.isEmpty()) return null;
        return trimmed.replaceAll("[^0-9Xx]", "");
    }
}

