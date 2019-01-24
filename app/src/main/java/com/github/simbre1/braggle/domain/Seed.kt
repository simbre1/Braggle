package com.github.simbre1.braggle.domain

import kotlin.random.Random

const val QR_PREFIX = "braggle:seed:"

class Seed(val seed: Long,
           val seedString: String?) {

    fun marshal(): String {
        if (seedString != null) {
            return seed.toString() + "," + seedString
        } else {
            return seed.toString()
        }
    }

    fun toQr(): String {
        return QR_PREFIX + marshal()
    }

    companion object {

        fun fromQr(str: String?): Seed? {
            if (str.isNullOrBlank()
                || !str.startsWith(QR_PREFIX)
                || str.length <= QR_PREFIX.length) {
                return null
            } else {
                return unmarshal(str.substring(QR_PREFIX.length))
            }
        }

        fun unmarshal(str: String?): Seed? {
            if (str.isNullOrBlank()) {
                return null
            }

            val i = str.indexOf(",")
            val seed: Long
            try {
                seed = (if(i == -1) str else str.substring(0, i)).toLong()
            } catch (e: NumberFormatException) {
                return null
            }

            if (i == -1 || i == str.length - 1) {
                return create(seed)
            } else {
                return Seed(seed, str.substring(i + 1))
            }
        }

        fun create(seedString: String?) =
            if(seedString.isNullOrBlank()) create()
            else Seed(stringToSeed(seedString), seedString)

        fun create(seed: Long) = Seed(seed, null)

        fun create() = Seed(Random.nextLong(), null)

        private fun stringToSeed(s: String?): Long {
            val trimmed = s?.trim() ?: ""
            return if (trimmed.isEmpty()) {
                kotlin.random.Random.nextLong()
            } else {
                trimmed.hashCode().toLong()
            }
        }
    }
}