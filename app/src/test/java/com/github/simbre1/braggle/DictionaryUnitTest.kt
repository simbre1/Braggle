package com.github.simbre1.braggle

import org.junit.Test
import java.util.*

class DictionaryUnitTest {
    @Test
    fun testPrefix() {
        val dict = Dictionary(
            TreeSet(
                setOf("ab", "abc", "def")),
            Language.EN)

        assert(dict.isPrefix("ab"))
        assert(!dict.isPrefix("abc"))
        assert(!dict.isPrefix("b"))
        assert(dict.isPrefix("de"))
    }
}
