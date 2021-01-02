package com.github.gimme.omidbot.commands

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class SavingsCommandTest {

    private val command = SavingsCommand()

    @ParameterizedTest
    @CsvSource(
        "1, 1000000, 0, 0, 1000000",
        "1, 1000000, 0, 8, 1080000",
        "2, 1000000, 0, 8, 1166400",
        "1, 0, 10000, 0, 120000",
        "1, 0, 10000, 8, 124339",
        "2, 1000000, 10000, 8, 1425025",
    )
    fun `should calculate FIRE`(
        durationYears: Double,
        startingAmount: Double,
        monthlySavings: Double,
        interestPercent: Double,
        expected: Double,
    ) {
        val result = command.execute(durationYears, startingAmount, monthlySavings, interestPercent)

        assertEquals(expected, result.newBalance, 0.2)
    }
}
