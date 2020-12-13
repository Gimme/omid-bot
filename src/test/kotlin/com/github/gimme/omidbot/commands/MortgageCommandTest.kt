package com.github.gimme.omidbot.commands

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class MortgageCommandTest {

    private val command = MortgageCommand()

    companion object {
        @JvmStatic
        private fun inputProvider() = Stream.of(
            Input(1000, expectedPayment = 12000),
            Input(8500, expectedPayment = 102000),
            Input(8500, 2, expectedPayment = 204000),

            Input(0, 1, 1000000, 8, 8, 100, 10, 0, expectedPayment = 100000),

            Input(3, 0, 1000, 0, 8, 100, 0, expectedPayment = 0),
            Input(3, 1, 1000, 0, 8, 100, 0, 1, expectedPayment = 46),
            Input(3, 3, 1000, 0, 8, 100, 0, 1, expectedPayment = 138),

            Input(0, 0.08, 1000000, 0, 8, 100, 0, 1.5, expectedPayment = 1250),
            Input(0, 0.17, 1000000, 0, 8, 100, 0, 1.5, expectedPayment = 2500),

            Input(0, 0.08, 1000000, 0, 8, 100, 3, 0, expectedPayment = 2500),
            Input(0, 0.17, 1000000, 0, 8, 100, 3, 0, expectedPayment = 5000),

            Input(0, 0.08, 1000000, 0, 8, 100, 3, 1.5, expectedPayment = 3750.0),
            Input(0, 0.17, 1000000, 0, 8, 100, 3, 1.5, expectedPayment = 7496.875),
            Input(1000, 0.17, 1000000, 0, 8, 100, 3, 1.5, expectedPayment = 9496.875),

            Input(0, 0.17, 240000, 0, 8, 100, 10, 5, expectedPayment = 5991.6667),


            // Investment Loss

            Input(0, loanPercent = 1000000, loanInterestPercent = 0, expectedRevenue = 0),
            Input(0, 0.08, 1000000, 0, 10, 100, 12, 12, expectedRevenue = -10000),
            Input(0, 0.17, 1000000, 0, 10, 100, 12, 0, expectedRevenue = -79.7414),

            Input(1000, 0.08, 0, 0, 10, 0, 0, 0, expectedRevenue = -1007.97414),
            Input(1000, 0.08, 1000000, 0, 10, 100, 0, 12, expectedRevenue = -11007.97414),
            Input(1000, 0.08, 1000000, 0, 10, 100, 12, 12, expectedRevenue = -11007.97414),
            Input(1000, 0.17, 1000000, 0, 10, 100, 12, 12, expectedRevenue = (-21007.97414) * 1.00797414 - 1007.97414 - 9900 + 10000),

            Input(0, 0.08, 1000, 10, 0, 0, 0, 0, expectedRevenue = -992.02586)
        )
    }

    data class Input(
        val rent: Number,
        val durationYears: Number = 1,
        val propertyValue: Number = 0,
        val propertyReturnPercent: Number = 0,
        val marketReturnPercent: Number = 8,
        val loanPercent: Number = 85,
        val loanAmortizationPercent: Number = 2,
        val loanInterestPercent: Number = 1.5,
        val expectedPayment: Number? = null,
        val expectedRevenue: Number? = null,
    )

    @ParameterizedTest
    @MethodSource("inputProvider")
    fun `should calculate mortgage costs`(data: Input) {
        val body = command.execute(
            data.rent.toDouble(),
            data.durationYears.toDouble(),
            data.propertyValue.toDouble(),
            data.propertyReturnPercent.toDouble(),
            data.marketReturnPercent.toDouble(),
            data.loanPercent.toDouble(),
            data.loanAmortizationPercent.toDouble(),
            data.loanInterestPercent.toDouble(),
        ).body as MortgageCommand.Result

        assert(data.expectedPayment != null || data.expectedRevenue != null)

        val delta = 0.0001
        data.expectedPayment?.let { assertEquals(it.toDouble(), body.totalPayment, delta) }
        data.expectedRevenue?.let { assertEquals(it.toDouble(), body.totalRevenue, delta) }
    }

    @Test
    fun `given total cost should calculate average monthly cost`() {
        val result = command.execute(1000.0, 1.0, marketReturnPercent = 0.0).body as MortgageCommand.Result

        assertEquals(12000.0, result.totalPayment)
        assertEquals(-12000.0, result.totalRevenue)
        assertEquals(1000.0, result.averageMonthlyPayment)
        assertEquals(-1000.0, result.averageMonthlyRevenue)
    }

    @Test
    fun `test input should have same defaults as command`() {
        val data1 = Input(0)
        assertEquals(command.execute(0.0),
            command.execute(
                data1.rent.toDouble(),
                data1.durationYears.toDouble(),
                data1.propertyValue.toDouble(),
                data1.propertyReturnPercent.toDouble(),
                data1.marketReturnPercent.toDouble(),
                data1.loanPercent.toDouble(),
                data1.loanAmortizationPercent.toDouble(),
                data1.loanInterestPercent.toDouble(),
            ))

        val data2 = Input(100, loanPercent = 1000)
        assertEquals(command.execute(100.0, loanPercent = 1000.0),
            command.execute(
                data2.rent.toDouble(),
                data2.durationYears.toDouble(),
                data2.propertyValue.toDouble(),
                data2.propertyReturnPercent.toDouble(),
                data2.marketReturnPercent.toDouble(),
                data2.loanPercent.toDouble(),
                data2.loanAmortizationPercent.toDouble(),
                data2.loanInterestPercent.toDouble(),
            ))
    }
}
