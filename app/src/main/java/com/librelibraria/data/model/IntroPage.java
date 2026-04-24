package com.librelibraria.data.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

public class IntroPage {
    @StringRes private final int title;
    @StringRes private final int description;
    @DrawableRes private final int icon;
    
    public IntroPage(@StringRes int title, @StringRes int description, @DrawableRes int icon) {
        this.title = title;
        this.description = description;
        this.icon = icon;
    }
    
    public int getTitle() { return title; }
    public int getDescription() { return description; }
    public int getIcon() { return icon; }
    
    public static IntroPage[] getPages() {
        return new IntroPage[] {
            new IntroPage(com.librelibraria.R.string.welcome_title, com.librelibraria.R.string.welcome_desc, com.librelibraria.R.drawable.ic_library),
            new IntroPage(com.librelibraria.R.string.catalog_title, com.librelibraria.R.string.catalog_desc, com.librelibraria.R.drawable.ic_book),
            new IntroPage(com.librelibraria.R.string.lending_title, com.librelibraria.R.string.lending_desc, com.librelibraria.R.drawable.ic_loan),
            new IntroPage(com.librelibraria.R.string.import_title, com.librelibraria.R.string.import_desc, com.librelibraria.R.drawable.ic_import),
            new IntroPage(com.librelibraria.R.string.theme_title, com.librelibraria.R.string.theme_desc, com.librelibraria.R.drawable.ic_palette)
        };
    }
}