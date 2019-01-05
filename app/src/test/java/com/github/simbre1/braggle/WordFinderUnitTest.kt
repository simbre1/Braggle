package com.github.simbre1.braggle

import org.junit.Test

class WordFinderUnitTest {
    @Test
    fun testFind4() {
        val letters = arrayOf(
                arrayOf(' ', ' ', 'b', ' '),
                arrayOf(' ', 'l', 'o', 'e'),
                arrayOf('a', ' ', 'i', 'p'),
                arrayOf('a', ' ', 'p', 'e')
        )

        val board = Board(letters)
        val dict = Dictionary(sortedSetOf("bliep", "bloep", "blaap"), Language.EN)

        val finder = WordFinder(board, dict)
        val found = finder.find(4)

        assert(found.contains("bliep"))
        assert(found.contains("bloep"))
        assert(!found.contains("blaap"))
    }
}
