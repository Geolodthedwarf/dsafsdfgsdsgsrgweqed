package com.librelibraria.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.librelibraria.data.model.Tag;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Data Access Object for Tag entity.
 */
@Dao
public interface TagDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Single<Long> insert(Tag tag);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAll(List<Tag> tags);

    @Update
    Completable update(Tag tag);

    @Delete
    Completable delete(Tag tag);

    @Query("DELETE FROM tags WHERE id = :id")
    Completable deleteById(long id);

    @Query("SELECT * FROM tags WHERE id = :id")
    Single<Tag> getById(long id);

    @Query("SELECT * FROM tags ORDER BY name ASC")
    Flowable<List<Tag>> getAllTags();

    @Query("SELECT * FROM tags ORDER BY name ASC")
    LiveData<List<Tag>> getAllTagsLive();

    @Query("SELECT * FROM tags ORDER BY usageCount DESC")
    Flowable<List<Tag>> getTagsByUsage();

    @Query("SELECT * FROM tags WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    Flowable<List<Tag>> searchTags(String query);

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    Single<Tag> getByName(String name);

    @Query("UPDATE tags SET usageCount = usageCount + 1 WHERE id = :id")
    Completable incrementUsage(long id);

    @Query("UPDATE tags SET isSynced = 1 WHERE id = :id")
    Completable markAsSynced(long id);
}
