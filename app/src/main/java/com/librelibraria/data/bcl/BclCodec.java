package com.librelibraria.data.bcl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.librelibraria.data.model.Book;
import com.librelibraria.data.model.Borrower;
import com.librelibraria.data.model.DiaryEntry;
import com.librelibraria.data.model.Loan;
import com.librelibraria.data.model.Tag;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Codec for encoding and decoding library data to/from BCL (Biblioteca Catalog Library) JSON format.
 */
public class BclCodec {

    private final Gson gson;

    /**
     * Container for library export data.
     */
    public static class LibraryData {
        public String version = "1.0";
        public long exportDate;
        public String appName = "LibreLibraria";
        public List<Book> books = new ArrayList<>();
        public List<Borrower> borrowers = new ArrayList<>();
        public List<Loan> loans = new ArrayList<>();
        public List<Tag> tags = new ArrayList<>();
        public List<DiaryEntry> diaryEntries = new ArrayList<>();
    }

    public BclCodec() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                .registerTypeAdapter(Date.class, new DateTypeAdapter())
                .create();
    }

    /**
     * Encode library data to BCL JSON string.
     */
    public String encode(LibraryData data) {
        data.exportDate = System.currentTimeMillis();
        return gson.toJson(data);
    }

    /**
     * Decode BCL JSON string to library data.
     */
    public LibraryData decode(String json) throws JsonParseException {
        return gson.fromJson(json, LibraryData.class);
    }

    /**
     * Encode books to BCL format.
     */
    public String encodeBooks(List<Book> books) {
        LibraryData data = new LibraryData();
        data.books = books;
        return encode(data);
    }

    /**
     * Decode books from BCL format.
     */
    public List<Book> decodeBooks(String json) {
        LibraryData data = decode(json);
        return data != null ? data.books : new ArrayList<>();
    }

    /**
     * Encode borrowers to BCL format.
     */
    public String encodeBorrowers(List<Borrower> borrowers) {
        LibraryData data = new LibraryData();
        data.borrowers = borrowers;
        return encode(data);
    }

    /**
     * Decode borrowers from BCL format.
     */
    public List<Borrower> decodeBorrowers(String json) {
        LibraryData data = decode(json);
        return data != null ? data.borrowers : new ArrayList<>();
    }

    /**
     * Encode loans to BCL format.
     */
    public String encodeLoans(List<Loan> loans) {
        LibraryData data = new LibraryData();
        data.loans = loans;
        return encode(data);
    }

    /**
     * Decode loans from BCL format.
     */
    public List<Loan> decodeLoans(String json) {
        LibraryData data = decode(json);
        return data != null ? data.loans : new ArrayList<>();
    }

    /**
     * Encode full library to BCL format.
     */
    public String encodeLibrary(List<Book> books, List<Borrower> borrowers,
                                 List<Loan> loans, List<Tag> tags, List<DiaryEntry> diaryEntries) {
        LibraryData data = new LibraryData();
        data.books = books;
        data.borrowers = borrowers;
        data.loans = loans;
        data.tags = tags;
        data.diaryEntries = diaryEntries;
        return encode(data);
    }

    /**
     * Decode full library from BCL format.
     */
    public LibraryData decodeLibrary(String json) {
        return decode(json);
    }

    /**
     * Validate BCL JSON format.
     */
    public boolean validate(String json) {
        try {
            LibraryData data = gson.fromJson(json, LibraryData.class);
            return data != null && data.appName != null && data.version != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get version from BCL JSON.
     */
    public String getVersion(String json) {
        try {
            JsonObject obj = gson.fromJson(json, JsonObject.class);
            return obj != null ? obj.get("version").getAsString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Custom date type adapter for handling Date serialization.
     */
    private static class DateTypeAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {
        @Override
        public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
            return new com.google.gson.JsonPrimitive(src.getTime());
        }

        @Override
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new Date(json.getAsLong());
        }
    }
}
