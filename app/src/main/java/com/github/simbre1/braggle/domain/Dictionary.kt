package com.github.simbre1.braggle.domain

import java.util.*

class Dictionary(private val words: TreeSet<String>,
                 val language: Language
) {

    fun isWord(s: String): Boolean {
        return words.contains(s.toUpperCase())
    }

    fun getWords() : SortedSet<String> = words

    /**
     * String is a prefix for a larger word in this dictionary.
     */
    fun isPrefix(s: String) : Boolean =
        isPrefix(words, s.toUpperCase())

    companion object {
        /**
         * String is a prefix for a larger word.
         */
        fun isPrefix(words: SortedSet<String>, s: String): Boolean {
            val tailSet = words.tailSet(s)
            if(tailSet.isEmpty()) {
                return false
            }

            val i = tailSet.iterator()
            val tailWord = i.next()
            if (tailWord != s) {
                return tailWord.startsWith(s)
            }

            return i.hasNext() && i.next().startsWith(s)
        }
    }
}