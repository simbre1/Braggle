package com.github.simbre1.braggle

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager

@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity() {
    private lateinit var currentTheme: Theme
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = PreferenceManager
            .getDefaultSharedPreferences(this)
        currentTheme = Theme.fromCode(
            sharedPref.getString(
                "current_theme_preference",
                Theme.default().code)!!)
        setAppTheme(currentTheme)
    }

    override fun onResume() {
        super.onResume()
        val theme = Theme.fromCode(
            sharedPref.getString(
                "current_theme_preference",
                Theme.default().code)!!)
        if(currentTheme != theme)
            recreate()
    }

    private fun setAppTheme(theme: Theme) {
        setTheme(theme.themeId)
    }
}