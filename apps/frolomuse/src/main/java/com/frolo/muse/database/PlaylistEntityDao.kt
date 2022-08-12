package com.frolo.muse.database

import androidx.annotation.WorkerThread
import androidx.room.*
import com.frolo.muse.database.entity.PlaylistEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import org.jetbrains.annotations.TestOnly


@Dao
abstract class PlaylistEntityDao {

    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    abstract fun getPlaylistEntities(playlistId: Long): Flowable<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists")
    abstract fun getAllPlaylistEntities(): Flowable<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE name LIKE '%' || :filter || '%'")
    abstract fun getAllPlaylistEntitiesFiltered(filter: String): Flowable<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE name = :name")
    abstract fun findPlaylistEntitiesByName(name: String): Single<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE name = :name")
    abstract fun blockingFindPlaylistEntitiesByName(name: String): List<PlaylistEntity>

    @Insert
    abstract fun createPlaylistEntity(entity: PlaylistEntity): Single<Long>

    @WorkerThread
    @Insert
    abstract fun blockingCreatePlaylistEntity(entity: PlaylistEntity): Long

    @Update
    abstract fun updatePlaylistEntity(entity: PlaylistEntity): Completable

    @Query("DELETE FROM playlists WHERE id = :id")
    abstract fun deletePlaylistEntityById(id: Long): Completable

    @Query("DELETE FROM playlists WHERE id IN (:ids)")
    abstract fun deletePlaylistEntitiesByIds(ids: Collection<Long>): Completable

    @TestOnly
    @Query("DELETE FROM playlists")
    abstract fun nuke(): Completable
}