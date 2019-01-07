package com.github.simbre1.braggle.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class GameData (
    @PrimaryKey var uid: Int?,
    @ColumnInfo(name = "language") var language: String,
    @ColumnInfo(name = "boardSize") var boardSize: Int,
    @ColumnInfo(name = "seed") var seed: Long,
    @ColumnInfo(name = "seedString") var seedString: String?,
    @ColumnInfo(name = "board") var board: String,
    @ColumnInfo(name = "foundWords") var foundWords: String?,
    @ColumnInfo(name = "startTime") var startTime: Date,
    @ColumnInfo(name = "stopTime") var stopTime: Date?
)