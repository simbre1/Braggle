package com.github.simbre1.braggle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GameModelFactory(private val dictionaryRepo: DictionaryRepo) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameModel(dictionaryRepo) as T
        }
        throw IllegalArgumentException()
    }
}
