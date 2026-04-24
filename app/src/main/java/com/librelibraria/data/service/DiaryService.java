package com.librelibraria.data.service;

import com.librelibraria.data.database.DiaryDao;
import com.librelibraria.data.model.DiaryEntry;

import java.util.List;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DiaryService {

    private final DiaryDao diaryDao;
    private final AuditService auditService;

    public DiaryService(DiaryDao diaryDao, AuditService auditService) {
        this.diaryDao = diaryDao;
        this.auditService = auditService;
    }

    public Single<Long> addEntry(DiaryEntry entry) {
        entry.setLastModified(System.currentTimeMillis());
        entry.setSynced(false);

        return diaryDao.insert(entry)
                .doOnSuccess(id -> auditService.log("DIARY_ENTRY_ADDED", "Diary entry added for: " + entry.getBookTitle()));
    }

    public Completable updateEntry(DiaryEntry entry) {
        entry.setLastModified(System.currentTimeMillis());
        entry.setSynced(false);

        return diaryDao.update(entry)
                .doOnComplete(() -> auditService.log("DIARY_ENTRY_UPDATED", "Diary entry updated for: " + entry.getBookTitle()));
    }

    public Completable deleteEntry(DiaryEntry entry) {
        return diaryDao.delete(entry)
                .doOnComplete(() -> auditService.log("DIARY_ENTRY_DELETED", "Diary entry deleted for: " + entry.getBookTitle()));
    }

    public Single<List<DiaryEntry>> getAllEntries() {
        return diaryDao.getAllEntries().firstOrError();
    }

    public Single<DiaryEntry> getEntryById(long entryId) {
        return diaryDao.getEntryById(entryId);
    }

    public Single<List<DiaryEntry>> getEntriesForBook(long bookId) {
        return diaryDao.getEntriesForBook(bookId).firstOrError();
    }

    public Single<List<DiaryEntry>> getEntriesBetweenDates(long startDate, long endDate) {
        return diaryDao.getEntriesBetweenDates(startDate, endDate).firstOrError();
    }

    public Single<List<DiaryEntry>> getRecentEntries(int limit) {
        return diaryDao.getRecentEntries(limit);
    }

    public Completable markAsSynced(long entryId) {
        return diaryDao.markAsSynced(entryId);
    }

    public Single<List<DiaryEntry>> getUnsyncedEntries() {
        return diaryDao.getUnsyncedEntries();
    }

    public Single<Integer> getEntriesCount() {
        return diaryDao.getEntriesCount();
    }

    public Single<List<DiaryEntry>> searchEntries(String query) {
        return diaryDao.searchEntries(query);
    }

    public Single<String> exportEntries() {
        return getAllEntries()
                .map(entries -> {
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    return gson.toJson(entries);
                });
    }

    public Completable importEntries(String json) {
        return Completable.fromAction(() -> {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            Type listType = new TypeToken<List<DiaryEntry>>(){}.getType();
            List<DiaryEntry> entries = gson.fromJson(json, listType);
            for (DiaryEntry entry : entries) {
                entry.setId(0);
                entry.setSynced(false);
                diaryDao.insert(entry).blockingGet();
            }
        }).subscribeOn(Schedulers.io());
    }
}