package com.github.simbre1.braggle

import org.junit.Test

class BoardUnitTest {
    @Test
    fun testSeed() {
        assert(
            Board.random(Language.EN, 4, "test")
                .getLetters()
                    == "[E, O, H, K], [W, C, L, L], [Y, H, N, T], [T, Y, H, L]")
    }
}
