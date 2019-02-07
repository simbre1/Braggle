package com.github.simbre1.braggle.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@TypeConverters(Converters::class)
@Database(entities = arrayOf(GameData::class), version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDataDao(): GameDataDao

    companion object {
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "gameData-db")
                    .addMigrations(MIGRATION_1_2)
                    .build()
            }
            return instance as AppDatabase
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE gameData ADD COLUMN minWordLength INTEGER NOT NULL DEFAULT 4")
            }
        }
    }
}