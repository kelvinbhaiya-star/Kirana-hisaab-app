package com.kiranahisab.app.data

import kotlinx.coroutines.flow.Flow

class KiranaRepository(private val dao: KiranaDao) {
    val allItems: Flow<List<KiranaItem>> = dao.observeAllItems()

    suspend fun addItem(name: String, rate: Double, unit: BaseUnit) {
        dao.insert(
            KiranaItem(
                name = name.trim(),
                rate = rate,
                unit = unit,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateItem(item: KiranaItem) {
        dao.update(item.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteItem(item: KiranaItem) {
        dao.delete(item)
    }
}
