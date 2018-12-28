package com.github.simbre1.braggle

import java.util.*

class Dictionary(private var words: TreeSet<String>) {

    fun isWord(s: String): Boolean {
        return words.contains(s)
    }
}