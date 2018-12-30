package com.github.simbre1.braggle

import android.app.Application
import java.text.Normalizer
import java.util.*

class DictionaryRepo(private val application: Application) {

    private var english: Dictionary? = null
    private var french: Dictionary? = null

    fun get(language: Language): Dictionary = when(language) {
        Language.EN -> getEnglish()
        Language.FR -> getFrench()
        Language.NL -> TODO()
    }

    private fun getEnglish(): Dictionary {
        var dict = english
        if (dict == null) {
            dict = loadDictionary("eowl-v1.1.2.txt", Language.EN)
            english = dict
        }
        return dict
    }

    private fun getFrench(): Dictionary {
        var dict = french
        if (dict == null) {
            dict = loadDictionary("fr-words.mcas4150.github.txt", Language.FR)
            french = dict
        }
        return dict
    }

    private fun loadDictionary(asset: String,
                               language: Language) : Dictionary {
        return Dictionary(
            application.assets.open(asset)
                .bufferedReader()
                .readLines()
                .filter { s ->
                    s.length >= minWordLengthLoad
                            && !s.contains(' ')
                            && !s.contains('-')
                }
                .map { s -> removeAccents(s).toUpperCase() }
                .toCollection(TreeSet()),
            language)
    }

    companion object {
        private const val minWordLengthLoad = 3
        private val removeAccentsRegex = "\\p{InCombiningDiacriticalMarks}+".toRegex()

        private fun removeAccents(word: String): String {
            return removeAccentsRegex.replace(
                Normalizer.normalize(word, Normalizer.Form.NFD),
                "")
        }
    }
}