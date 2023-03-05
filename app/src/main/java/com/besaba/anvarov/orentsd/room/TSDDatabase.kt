package com.besaba.anvarov.orentsd.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ScanData::class, NomenData::class], exportSchema = false, version = 1)
abstract class TSDDatabase : RoomDatabase() {

    abstract fun scanDataDao(): ScanDataDao
    abstract fun nomenDataDao(): NomenDataDao

    companion object {
        @Volatile
        private var sINSTANCE: TSDDatabase? = null

        fun getDatabase(
            context: Context
        ): TSDDatabase {
            // возвращает экземпляр, если нет, то создаю базу
            return sINSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TSDDatabase::class.java,
                    "scanDatabase"
                )
                    // если при запуске приложения Room увидит, что необходима миграция,
                    // то он просто пересоздаст базу с новой структурой Entity классов и все данные пропадут.
                    .fallbackToDestructiveMigration()
                    .build()
                sINSTANCE = instance
                // return instance
                instance
            }
        }
    }

}