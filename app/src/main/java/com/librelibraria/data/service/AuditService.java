package com.librelibraria.data.service;

import com.librelibraria.data.database.AuditLogDao;
import com.librelibraria.data.model.AuditLog;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Service for managing audit logs - tracks all library actions.
 */
public class AuditService {

    private final AuditLogDao auditLogDao;

    public AuditService(AuditLogDao auditLogDao) {
        this.auditLogDao = auditLogDao;
    }

    /**
     * Log an action without entity association.
     */
    public void log(String action, String details) {
        AuditLog log = new AuditLog(action, details);
        auditLogDao.insert(log)
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    /**
     * Log an action with entity association.
     */
    public void log(String action, String details, String entityType, long entityId) {
        AuditLog log = new AuditLog(action, details, entityType, entityId);
        auditLogDao.insert(log)
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    /**
     * Log book-related action.
     */
    public void logBookAction(String action, String details, long bookId) {
        log(action, details, "BOOK", bookId);
    }

    /**
     * Log borrower-related action.
     */
    public void logBorrowerAction(String action, String details, long borrowerId) {
        log(action, details, "BORROWER", borrowerId);
    }

    /**
     * Log loan-related action.
     */
    public void logLoanAction(String action, String details, long loanId) {
        log(action, details, "LOAN", loanId);
    }

    /**
     * Get all audit logs.
     */
    public Single<List<AuditLog>> getAllLogs() {
        return auditLogDao.getAllLogs();
    }

    /**
     * Get audit logs for a specific entity.
     */
    public Single<List<AuditLog>> getLogsForEntity(String entityType, long entityId) {
        return auditLogDao.getLogsForEntity(entityType, entityId);
    }

    /**
     * Get recent audit logs.
     */
    public Single<List<AuditLog>> getRecentLogs(int limit) {
        return auditLogDao.getRecentLogs(limit);
    }

    /**
     * Mark log as synced.
     */
    public Completable markAsSynced(long logId) {
        return auditLogDao.markAsSynced(logId);
    }

    /**
     * Get unsynced logs count.
     */
    public Single<Integer> getUnsyncedCount() {
        return auditLogDao.getUnsyncedCount();
    }

    /**
     * Get logs between dates.
     */
    public Single<List<AuditLog>> getLogsBetweenDates(long startDate, long endDate) {
        return auditLogDao.getLogsBetweenDates(startDate, endDate);
    }
}
