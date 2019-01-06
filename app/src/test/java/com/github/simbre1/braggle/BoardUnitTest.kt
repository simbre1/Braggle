package com.github.simbre1.braggle

import org.junit.Test

class BoardUnitTest {
    @Test
    fun testSeed() {
        assert(
            Board.random(Language.EN, 4, "test")
                .getLetters()
                    == "[A, J, H, T], [U, L, D, A], [P, E, N, U], [S, I, T, T]")
    }
}
