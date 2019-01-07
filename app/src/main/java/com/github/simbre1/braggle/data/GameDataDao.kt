package com.github.simbre1.braggle.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface GameDataDao {
    @Query("select * from gameData")
    fun getAll(): List<GameData>

    @Query("select * from gameData order by startTime desc limit 1")
    fun getLast(): GameData?

    @Query("select * from gameData where uid in (:uids)")
    fun loadAllByIds(uids: IntArray): List<GameData>

    @Insert
    fun insertAll(vararg gameDatas: GameData)

    @Delete
    fun delete(gameData: GameData)
}