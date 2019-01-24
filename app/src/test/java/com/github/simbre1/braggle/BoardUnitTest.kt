package com.github.simbre1.braggle

import com.github.simbre1.braggle.domain.*
import org.junit.Test

class BoardUnitTest {
    @Test
    fun testSeed() {
        assert(
            Board.create(Language.EN, 4, Seed.create("test"))
                .getLetters()
                    == "A,J,H,T;U,L,D,A;P,E,N,U;S,I,T,T")
    }

    @Test
    fun testFindCow() {
        val dict = Dictionary(
            sortedSetOf("KOE", "COW", "VACHE"),
            Language.EN)

        var cow = false
        var i = -1

        while(!cow) {
            i += 1
            val board = Board.create(Language.EN, 4, Seed.create(i.toString()))
            cow = !WordFinder(board, dict).find().isEmpty()
        }
        assert(i == 11)
    }
}
