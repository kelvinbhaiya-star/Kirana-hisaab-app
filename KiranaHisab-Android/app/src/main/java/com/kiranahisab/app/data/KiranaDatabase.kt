package com.kiranahisab.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BaseUnitConverters {
    @TypeConverter
    fun fromBaseUnit(unit: BaseUnit): String = unit.name

    @TypeConverter
    fun toBaseUnit(raw: String): BaseUnit = BaseUnit.valueOf(raw)
}

@Database(entities = [KiranaItem::class], version = 1, exportSchema = false)
@TypeConverters(BaseUnitConverters::class)
abstract class KiranaDatabase : RoomDatabase() {
    abstract fun kiranaDao(): KiranaDao

    companion object {
        @Volatile
        private var INSTANCE: KiranaDatabase? = null

        val DEFAULT_STAPLES = listOf(
            KiranaItem(name = "Sugar", rate = 44.0, unit = BaseUnit.KG),
            KiranaItem(name = "Toor Dal", rate = 155.0, unit = BaseUnit.KG),
            KiranaItem(name = "Chana Dal", rate = 85.0, unit = BaseUnit.KG),
            KiranaItem(name = "Wheat Atta", rate = 38.0, unit = BaseUnit.KG),
            KiranaItem(name = "Basmati Rice", rate = 80.0, unit = BaseUnit.KG),
            KiranaItem(name = "Regular Rice", rate = 48.0, unit = BaseUnit.KG),
            KiranaItem(name = "Mustard Oil", rate = 145.0, unit = BaseUnit.LITER),
            KiranaItem(name = "Refined Oil", rate = 130.0, unit = BaseUnit.LITER),
            KiranaItem(name = "Tea Leaves", rate = 320.0, unit = BaseUnit.KG),
            KiranaItem(name = "Salt", rate = 28.0, unit = BaseUnit.KG),
            KiranaItem(name = "Poha", rate = 52.0, unit = BaseUnit.KG),
            KiranaItem(name = "Besan", rate = 90.0, unit = BaseUnit.KG)
        )

        fun getInstance(context: Context): KiranaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KiranaDatabase::class.java,
                    "kirana_hisab.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getInstance(context).kiranaDao()
                                if (dao.getItemCount() == 0) {
                                    dao.insertAll(DEFAULT_STAPLES)
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
