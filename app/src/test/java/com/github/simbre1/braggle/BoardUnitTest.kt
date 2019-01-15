package com.github.simbre1.braggle

import com.github.simbre1.braggle.domain.Board
import com.github.simbre1.braggle.domain.Dictionary
import com.github.simbre1.braggle.domain.Language
import com.github.simbre1.braggle.domain.WordFinder
import org.junit.Test

class BoardUnitTest {
    @Test
    fun testSeed() {
        assert(
            Board.random(Language.EN, 4, "test")
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
            val board = Board.random(Language.EN, 4, i.toString())
            cow = !WordFinder(board, dict).find().isEmpty()
        }
        assert(i == 11)
    }
}
