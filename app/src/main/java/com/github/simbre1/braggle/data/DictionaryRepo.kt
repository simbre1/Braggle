package com.github.simbre1.braggle.data

import android.app.Application
import com.github.simbre1.braggle.domain.Dictionary
import com.github.simbre1.braggle.domain.Language
import java.util.*
import kotlin.collections.HashMap

class DictionaryRepo(private val application: Application) {

    private var dicts = HashMap<Language, Dictionary>()

    fun get(language: Language): Dictionary {
        val dict = dicts.get(language)
        return if (dict != null) {
            dict
        } else {
            val newDict = loadDictionary(language.code + ".txt", language)
            dicts[language] = newDict
            newDict
        }
    }

    private fun loadDictionary(asset: String,
                               language: Language
    ) : Dictionary {
        return Dictionary(
            application.assets.open(asset)
                .bufferedReader()
                .readLines()
                .toCollection(TreeSet()),
            language
        )
    }
}