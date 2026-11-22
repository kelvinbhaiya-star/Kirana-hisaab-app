package com.kiranahisab.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kirana_items")
data class KiranaItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val rate: Double,
    val unit: BaseUnit,
    val updatedAt: Long = System.currentTimeMillis()
)
