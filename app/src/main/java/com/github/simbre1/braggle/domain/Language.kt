package com.github.simbre1.braggle.domain

enum class Language(val code: String,
                    val displayName: String,
                    val dictionaryIntentPackage: String?,
                    val dictionaryUrl: String?) {
    EN(
        "en",
        "english",
        "livio.pack.lang.en_US",
        "https://en.wiktionary.org/wiki/%s"),
    FR(
        "fr",
        "french",
        "livio.pack.lang.fr_FR",
        "https://fr.wiktionary.org/wiki/%s"),
    NL(
        "nl",
        "dutch",
        null,
        "https://nl.wiktionary.org/wiki/%s");

    companion object {
        fun fromCode(code: String): Language?  {
            return values().find { l -> l.code == code }
        }
    }
}