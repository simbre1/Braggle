package com.github.simbre1.braggle.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.simbre1.braggle.data.DictionaryRepo

class GameModelFactory(private val dictionaryRepo: DictionaryRepo) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameModel(dictionaryRepo) as T
        }
        throw IllegalArgumentException()
    }
}
