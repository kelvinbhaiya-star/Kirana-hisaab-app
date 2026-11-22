package com.kiranahisab.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface KiranaDao {
    @Query("SELECT * FROM kirana_items ORDER BY name COLLATE NOCASE ASC")
    fun observeAllItems(): Flow<List<KiranaItem>>

    @Query("SELECT COUNT(*) FROM kirana_items")
    suspend fun getItemCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: KiranaItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<KiranaItem>)

    @Update
    suspend fun update(item: KiranaItem)

    @Delete
    suspend fun delete(item: KiranaItem)
}
