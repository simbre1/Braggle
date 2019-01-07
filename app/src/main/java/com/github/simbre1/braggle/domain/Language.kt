package com.github.simbre1.braggle.domain

enum class Language(val code: String,
                    val displayName: String,
                    val dictionaryIntentPackage: String?) {
    EN("en", "english", "livio.pack.lang.en_US"),
    FR("fr", "french", "livio.pack.lang.fr_FR"),
    NL("nl", "dutch", null);

    companion object {
        fun fromCode(code: String): Language?  {
            return values().find { l -> l.code == code }
        }
    }
}