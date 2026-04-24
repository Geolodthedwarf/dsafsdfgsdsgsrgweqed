package com.librelibraria.data.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.librelibraria.data.model.AppPreset;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface PresetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Single<Long> insert(AppPreset preset);

    @Update
    Completable update(AppPreset preset);

    @Delete
    Completable delete(AppPreset preset);

    @Query("DELETE FROM app_presets WHERE id = :id")
    Completable deleteById(long id);

    @Query("SELECT * FROM app_presets WHERE id = :id")
    Single<AppPreset> getById(long id);

    @Query("SELECT * FROM app_presets ORDER BY name ASC")
    Single<List<AppPreset>> getAllPresets();

    @Query("SELECT * FROM app_presets ORDER BY name ASC")
    io.reactivex.rxjava3.core.Flowable<List<AppPreset>> getAllPresetsFlowable();

    @Query("SELECT * FROM app_presets WHERE isDefault = 1 LIMIT 1")
    Single<AppPreset> getDefaultPreset();

    @Query("UPDATE app_presets SET isDefault = 0")
    Completable clearDefaultPreset();

    @Query("UPDATE app_presets SET isDefault = 1 WHERE id = :id")
    Completable setDefaultPreset(long id);

    @Query("SELECT COUNT(*) FROM app_presets")
    io.reactivex.rxjava3.core.Single<Integer> getCount();
}