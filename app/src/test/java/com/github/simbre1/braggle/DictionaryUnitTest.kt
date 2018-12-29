package com.github.simbre1.braggle

import org.junit.Test

import org.junit.Assert.*
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class DictionaryUnitTest {
    @Test
    fun prefix_isCorrect() {
        val dict = Dictionary(
            TreeSet(
                setOf("ab", "abc", "def")))

        assert(dict.isPrefix("ab"))
        assert(!dict.isPrefix("abc"))
        assert(!dict.isPrefix("b"))
        assert(dict.isPrefix("de"))
    }
}
