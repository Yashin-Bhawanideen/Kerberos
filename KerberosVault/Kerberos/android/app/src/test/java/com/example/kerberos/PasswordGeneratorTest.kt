package com.example.kerberos

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

//unit tests verify the password generator behaviour (JUnit, 2026)
class PasswordGeneratorTest {

    @Test
    fun generatedPasswordHasCorrectLength() {
        val password = PasswordGenerator.generate(
            length = 20,
            useUppercase = true,
            useLowercase = true,
            useNumbers = true,
            useSymbols = true
        )

        assertEquals(20, password.length)
    }

    @Test
    fun generatedPasswordContainsSelectedCharacterTypes() {
        val password = PasswordGenerator.generate(
            length = 16,
            useUppercase = true,
            useLowercase = true,
            useNumbers = true,
            useSymbols = true
        )

        assertTrue(password.any { it.isUpperCase() })
        assertTrue(password.any { it.isLowerCase() })
        assertTrue(password.any { it.isDigit() })
        assertTrue(password.any { it in "!@#$%^&*" })
    }

    @Test
    fun generatedPasswordOnlyUsesSelectedTypes() {
        val password = PasswordGenerator.generate(
            length = 16,
            useUppercase = false,
            useLowercase = false,
            useNumbers = true,
            useSymbols = false
        )

        assertTrue(password.all { it.isDigit() })
        assertFalse(password.any { it.isLetter() })
    }

    @Test(expected = IllegalArgumentException::class)
    fun generatorRejectsNoCharacterTypesSelected() {
        PasswordGenerator.generate(
            length = 16,
            useUppercase = false,
            useLowercase = false,
            useNumbers = false,
            useSymbols = false
        )
    }
}

/*
REFERENCE LIST

JUnit. 2026. JUnit 4. [Online].
Available at: https://junit.org/junit4/
[Accessed 22 September 2026].
*/