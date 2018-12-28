package com.github.simbre1.braggle

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class WordFinderUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun find() {
        val letters = arrayOf(
                arrayOf(' ', ' ', 'b', ' '),
                arrayOf(' ', 'l', 'o', 'e'),
                arrayOf('a', ' ', 'i', 'p'),
                arrayOf('a', ' ', 'p', 'e')
        )

        val board = Board(letters)
        val dict = Dictionary(sortedSetOf("bliep", "bloep", "blaap"))

        val finder = WordFinder(board, dict)
        val found = finder.find()

        assert(found.contains("bliep"))
        assert(found.contains("bloep"))
        assert(!found.contains("blaap"))
    }
}
