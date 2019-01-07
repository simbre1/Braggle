package com.github.simbre1.braggle.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(GameData::class), version = 1)
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
                    .build()
            }
            return instance as AppDatabase
        }
    }
}