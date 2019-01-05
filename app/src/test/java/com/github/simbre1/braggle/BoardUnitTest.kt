package com.github.simbre1.braggle

import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class BoardUnitTest {
    @Test
    fun seedTest() {
        assert(
            Board.random(Language.EN, 4, "test")
                .getLetters()
                    == "[E, O, H, K], [W, C, L, L], [Y, H, N, T], [T, Y, H, L]")
    }
}
