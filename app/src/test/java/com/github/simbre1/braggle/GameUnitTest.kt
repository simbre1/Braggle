package com.github.simbre1.braggle

import com.github.simbre1.braggle.domain.Game
import org.junit.Test

class GameUnitTest {
    @Test
    fun testScore() {
        assert(Game.getScore(setOf("1", "22")) == 0)
        assert(Game.getScore(setOf("1", "22", "333")) == 1)
        assert(Game.getScore(setOf("1", "22", "333", "4444")) == 2)
        assert(Game.getScore(setOf("1", "22", "333", "4444", "55555")) == 4)
        assert(Game.getScore(setOf("1", "22", "333", "4444", "55555", "666666")) == 7)
        assert(Game.getScore(setOf("1", "22", "333", "4444", "55555", "666666", "7777777")) == 12)
        assert(Game.getScore(setOf("1", "22", "333", "4444", "55555", "666666", "7777777", "88888888")) == 23)
        assert(Game.getScore(setOf("1", "22", "333", "4444", "55555", "666666", "7777777", "88888888", "999999999")) == 34)
    }
}
