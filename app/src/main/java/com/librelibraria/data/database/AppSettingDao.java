package com.librelibraria.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.librelibraria.data.model.AppSetting;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

/**
 * Data Access Object for AppSetting entity.
 */
@Dao
public interface AppSettingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(AppSetting setting);

    @Update
    Completable update(AppSetting setting);

    @Delete
    Completable delete(AppSetting setting);

    @Query("SELECT * FROM app_settings WHERE `key` = :key LIMIT 1")
    Single<AppSetting> getByKey(String key);

    @Query("SELECT * FROM app_settings")
    LiveData<List<AppSetting>> getAllSettings();

    @Query("SELECT * FROM app_settings")
    Single<List<AppSetting>> getAllSettingsSync();

    @Query("DELETE FROM app_settings WHERE `key` = :key")
    Completable deleteByKey(String key);

    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    Single<String> getValueByKey(String key);

    @Query("SELECT COUNT(*) FROM app_settings")
    Single<Integer> getCount();
}
