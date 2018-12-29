package com.github.simbre1.braggle

import android.app.Application
import java.util.*

class DictionaryRepo(private val application: Application) {

    private var english: Dictionary? = null

    fun getEnglish(): Dictionary {
        var dict = english
        if (dict == null) {
            dict = loadDictionary("eowl-v1.1.2.txt", "livio.pack.lang.en_US")
            english = dict
        }
        return dict
    }

    fun loadDictionary(asset: String,
                       lookupIntentPackage: String) : Dictionary {
        return Dictionary(
            application.assets.open(asset)
                .bufferedReader()
                .readLines()
                .filter { s -> s.length >= minWordLength }
                .map { s -> s.toUpperCase() }
                .toCollection(TreeSet()),
            lookupIntentPackage)
    }

    companion object {
        private const val minWordLength = 4
    }
}