package com.example.kerberos

import java.security.SecureRandom

object PasswordGenerator {

    private const val UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
    private const val NUMBERS = "0123456789"
    private const val SYMBOLS = "!@#$%^&*"

    private val secureRandom = SecureRandom()

    //uses SecureRandom so generated passwords use a stronger random source (Oracle, 2026)
    fun generate(
        length: Int,
        useUppercase: Boolean,
        useLowercase: Boolean,
        useNumbers: Boolean,
        useSymbols: Boolean
    ): String {
        require(length > 0)

        val selectedSets = mutableListOf<String>()

        if (useUppercase) selectedSets.add(UPPERCASE)
        if (useLowercase) selectedSets.add(LOWERCASE)
        if (useNumbers) selectedSets.add(NUMBERS)
        if (useSymbols) selectedSets.add(SYMBOLS)

        require(selectedSets.isNotEmpty())

        val password = mutableListOf<Char>()

        //puts at least one character from each selected type in the password
        selectedSets.forEach { set ->
            password.add(set[secureRandom.nextInt(set.length)])
        }

        val allCharacters = selectedSets.joinToString("")

        while (password.size < length) {
            password.add(
                allCharacters[secureRandom.nextInt(allCharacters.length)]
            )
        }

        password.shuffle(secureRandom)

        return password.joinToString("")
    }

    private fun <T> MutableList<T>.shuffle(random: SecureRandom) {
        for (i in lastIndex downTo 1) {
            val j = random.nextInt(i + 1)
            val temp = this[i]
            this[i] = this[j]
            this[j] = temp
        }
    }
}

/*
REFERENCE LIST

Oracle. 2026. SecureRandom. [Online].
Available at: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/security/SecureRandom.html
[Accessed 22 September 2026].
*/