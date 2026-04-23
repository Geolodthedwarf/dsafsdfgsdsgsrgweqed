package com.librelibraria.data.service;

import com.librelibraria.data.database.DiaryDao;
import com.librelibraria.data.model.DiaryEntry;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Service for managing reading diary entries.
 */
public class DiaryService {

    private final DiaryDao diaryDao;
    private final AuditService auditService;

    public DiaryService(DiaryDao diaryDao, AuditService auditService) {
        this.diaryDao = diaryDao;
        this.auditService = auditService;
    }

    /**
     * Add a new diary entry.
     */
    public Single<Long> addEntry(DiaryEntry entry) {
        entry.setLastModified(System.currentTimeMillis());
        entry.setSynced(false);

        return diaryDao.insert(entry)
                .doOnSuccess(id -> auditService.log("DIARY_ENTRY_ADDED", "Diary entry added for: " + entry.getBookTitle()));
    }

    /**
     * Update an existing diary entry.
     */
    public Completable updateEntry(DiaryEntry entry) {
        entry.setLastModified(System.currentTimeMillis());
        entry.setSynced(false);

        return diaryDao.update(entry)
                .doOnSuccess(() -> auditService.log("DIARY_ENTRY_UPDATED", "Diary entry updated for: " + entry.getBookTitle()));
    }

    /**
     * Delete a diary entry.
     */
    public Completable deleteEntry(DiaryEntry entry) {
        return diaryDao.delete(entry)
                .doOnSuccess(() -> auditService.log("DIARY_ENTRY_DELETED", "Diary entry deleted for: " + entry.getBookTitle()));
    }

    /**
     * Get all diary entries.
     */
    public Single<List<DiaryEntry>> getAllEntries() {
        return diaryDao.getAllEntries();
    }

    /**
     * Get diary entry by ID.
     */
    public Single<DiaryEntry> getEntryById(long entryId) {
        return diaryDao.getEntryById(entryId);
    }

    /**
     * Get entries for a specific book.
     */
    public Single<List<DiaryEntry>> getEntriesForBook(long bookId) {
        return diaryDao.getEntriesForBook(bookId);
    }

    /**
     * Get entries between dates.
     */
    public Single<List<DiaryEntry>> getEntriesBetweenDates(long startDate, long endDate) {
        return diaryDao.getEntriesBetweenDates(startDate, endDate);
    }

    /**
     * Get recent entries.
     */
    public Single<List<DiaryEntry>> getRecentEntries(int limit) {
        return diaryDao.getRecentEntries(limit);
    }

    /**
     * Mark entry as synced.
     */
    public Completable markAsSynced(long entryId) {
        return diaryDao.markAsSynced(entryId);
    }

    /**
     * Get unsynced entries.
     */
    public Single<List<DiaryEntry>> getUnsyncedEntries() {
        return diaryDao.getUnsyncedEntries();
    }

    /**
     * Get entries count.
     */
    public Single<Integer> getEntriesCount() {
        return diaryDao.getEntriesCount();
    }

    /**
     * Search entries by book title.
     */
    public Single<List<DiaryEntry>> searchEntries(String query) {
        return diaryDao.searchEntries(query);
    }

    /**
     * Export diary entries to JSON format.
     */
    public Single<String> exportEntries() {
        return getAllEntries()
                .map(entries -> {
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    return gson.toJson(entries);
                });
    }

    /**
     * Import diary entries from JSON.
     */
    public Completable importEntries(String json) {
        return Completable.fromAction(() -> {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<List<DiaryEntry>>(){}.getType();
            List<DiaryEntry> entries = gson.fromJson(json, listType);
            for (DiaryEntry entry : entries) {
                entry.setId(0); // Reset ID for new insertion
                entry.setSynced(false);
                diaryDao.insert(entry).blockingGet();
            }
        }).subscribeOn(Schedulers.io());
    }
}
