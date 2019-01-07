package com.github.simbre1.braggle

import com.github.simbre1.braggle.domain.Board
import com.github.simbre1.braggle.domain.Dictionary
import com.github.simbre1.braggle.domain.Language
import com.github.simbre1.braggle.domain.WordFinder
import org.junit.Test

class WordFinderUnitTest {
    @Test
    fun testFind4() {
        val letters = arrayOf(
                arrayOf(" ",  " ", "B", " "),
                arrayOf("Qu", "L", "O", "E"),
                arrayOf("S",  "E", "I", "P"),
                arrayOf("A",  "T", "P", "E")
        )

        val board = Board(0L, null, letters)
        val dict = Dictionary(
            sortedSetOf("BLIEP", "BLOEP", "BLAAP", "QUEST"),
            Language.EN
        )

        val finder = WordFinder(board, dict)
        val found = finder.find(4)

        assert(found.contains("BLIEP"))
        assert(found.contains("BLOEP"))
        assert(found.contains("QUEST"))
        assert(!found.contains("BLAAP"))
    }
}
