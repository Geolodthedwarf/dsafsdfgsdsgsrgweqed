package com.librelibraria.data.database;

import androidx.room.TypeConverter;

import com.librelibraria.data.model.LoanStatus;
import com.librelibraria.data.model.ReadingStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Type converters for Room database.
 */
public class Converters {

    @TypeConverter
    public static ReadingStatus toReadingStatus(String value) {
        return value == null ? ReadingStatus.OWN : ReadingStatus.fromString(value);
    }

    @TypeConverter
    public static String fromReadingStatus(ReadingStatus status) {
        return status == null ? ReadingStatus.OWN.name() : status.name();
    }

    @TypeConverter
    public static LoanStatus toLoanStatus(String value) {
        return value == null ? LoanStatus.ACTIVE : LoanStatus.fromString(value);
    }

    @TypeConverter
    public static String fromLoanStatus(LoanStatus status) {
        return status == null ? LoanStatus.ACTIVE.name() : status.name();
    }

    @TypeConverter
    public static List<String> fromString(String value) {
        if (value == null || value.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(value.split(","));
    }

    @TypeConverter
    public static String fromList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        return String.join(",", list);
    }
}
