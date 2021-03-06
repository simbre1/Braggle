package com.github.simbre1.braggle

import com.github.simbre1.braggle.domain.Dictionary
import com.github.simbre1.braggle.domain.Language
import org.junit.Test
import java.util.*

class DictionaryUnitTest {
    @Test
    fun testPrefix() {
        val dict = Dictionary(
            TreeSet(
                setOf("AB", "ABC", "DEF")
            ),
            Language.EN
        )

        assert(dict.isPrefix("AB"))
        assert(!dict.isPrefix("ABC"))
        assert(!dict.isPrefix("B"))
        assert(dict.isPrefix("DE"))
    }
}
