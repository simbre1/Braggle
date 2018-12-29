package com.github.simbre1.braggle

import java.util.*

class Dictionary(private var words: TreeSet<String>) {

    fun isWord(s: String): Boolean {
        return words.contains(s)
    }

    fun getWords() : SortedSet<String> = words

    /**
     * String is a prefix for a larger word in this dictionary.
     */
    fun isPrefix(s: String) : Boolean = Companion.isPrefix(words, s)

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
                return tailWord.startsWith(s);
            }

            return i.hasNext() && i.next().startsWith(s)
        }
    }
}