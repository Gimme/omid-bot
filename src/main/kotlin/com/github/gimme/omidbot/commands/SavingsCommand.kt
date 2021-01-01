package com.github.gimme.omidbot.commands

import com.github.gimme.gimmebot.core.command.BaseCommand
import com.github.gimme.gimmebot.core.command.executor.CommandExecutor
import com.github.gimme.omidbot.Simulation
import kotlin.math.roundToInt

class SavingsCommand : BaseCommand<SavingsCommand.Result>("savings") {

    @CommandExecutor("", "", "0", "8")
    fun execute(durationYears: Double, startingAmount: Double, monthlySavings: Double = 0.0, interestPercent: Double = 8.0): Result {
        val durationMonths = (durationYears * 12).roundToInt()

        val simulation = Simulation(startingAmount, interestPercent / 100)
            .apply { investMoneyInTheBank = true }
            .addIncome(monthlySavings)
            .fastForward(durationMonths)

        return Result(durationYears, simulation.moneyInTheBank)
    }

    data class Result(
        val durationYears: Double,
        val newBalance: Double,
    )
}
