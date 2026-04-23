package com.librelibraria.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.librelibraria.data.model.AuditLog;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Data Access Object for AuditLog entity.
 */
@Dao
public interface AuditLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Single<Long> insert(AuditLog log);

    @Query("SELECT * FROM audit_log ORDER BY timestamp DESC")
    Flowable<List<AuditLog>> getAllLogs();

    @Query("SELECT * FROM audit_log ORDER BY timestamp DESC LIMIT :limit")
    Flowable<List<AuditLog>> getRecentLogs(int limit);

    @Query("SELECT * FROM audit_log ORDER BY timestamp DESC LIMIT :limit")
    LiveData<List<AuditLog>> getRecentLogsLive(int limit);

    @Query("SELECT * FROM audit_log WHERE entityType = :entityType ORDER BY timestamp DESC")
    Flowable<List<AuditLog>> getLogsByEntityType(String entityType);

    @Query("SELECT * FROM audit_log WHERE action = :action ORDER BY timestamp DESC")
    Flowable<List<AuditLog>> getLogsByAction(String action);

    @Query("SELECT * FROM audit_log WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    Flowable<List<AuditLog>> getLogsBetweenDates(long startTime, long endTime);

    @Query("SELECT * FROM audit_log WHERE isSynced = 0")
    Single<List<AuditLog>> getUnsyncedLogs();

    @Query("UPDATE audit_log SET isSynced = 1 WHERE id = :id")
    Completable markAsSynced(long id);

    @Query("DELETE FROM audit_log WHERE timestamp < :beforeTime")
    Completable deleteOldLogs(long beforeTime);
}
