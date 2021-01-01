package com.github.gimme.omidbot.commands

import com.github.gimme.gimmebot.core.command.BaseCommand
import com.github.gimme.gimmebot.core.command.executor.CommandExecutor
import com.github.gimme.omidbot.Simulation
import kotlin.math.roundToInt

class MortgageCommand : BaseCommand<MortgageCommand.Result>("mortgage") {

    @CommandExecutor("", "", "0", "0", "8", "85", "2", "1.5")
    fun execute(
        durationYears: Double,
        rent: Double,
        propertyValue: Double = 0.0,
        propertyReturnPercent: Double = 0.0,
        marketReturnPercent: Double = 8.0,
        loanPercent: Double = 85.0,
        loanAmortizationPercent: Double = 2.0,
        loanInterestPercent: Double = 1.5,
    ): Result {
        val durationMonths = (durationYears * 12).roundToInt()

        val simulation = Simulation(0.0, marketReturnPercent / 100, propertyReturnPercent / 100)

        simulation.addRent(rent)
        simulation.buyProperty(propertyValue)
        simulation.takeLoan((loanPercent / 100) * propertyValue,
            loanInterestPercent / 100,
            loanAmortizationPercent / 100)
        simulation.fastForward(durationMonths)

        return Result(
            simulation.totalPayed,
            if (durationMonths == 0) 0.0 else simulation.totalPayed / durationMonths,
            simulation.getTotalRevenue(),
            if (durationMonths == 0) 0.0 else simulation.getTotalRevenue() / durationMonths,
        )
    }

    data class Result(
        val totalPayment: Double,
        val averageMonthlyPayment: Double,
        val totalRevenue: Double,
        val averageMonthlyRevenue: Double,
    ) {
        override fun toString(): String { // TODO: display real monthly expense and theoretical money lost based on market
            return """
                RESULT - Monthly average (does not account for taxes):
                |  Payment: ${averageMonthlyPayment.roundToInt()} (total: ${totalPayment.roundToInt()}) 
                |  Revenue: ${averageMonthlyRevenue.roundToInt()} (total: ${totalRevenue.roundToInt()}) 
                """.trimIndent()
        }
    }

}
