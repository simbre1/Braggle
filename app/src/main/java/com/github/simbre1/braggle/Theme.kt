package com.github.simbre1.braggle

enum class Theme(val code: String,
                 val themeId: Int) {
    LIGHT("light", R.style.Theme_App_Light),
    DARK("dark", R.style.Theme_App_Dark);

    companion object {
        fun fromCode(code: String): Theme  {
            return values().find { l -> l.code == code } ?: default()
        }

        fun default() = LIGHT
    }
}