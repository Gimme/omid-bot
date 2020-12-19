package com.github.gimme.omidbot.commands

import com.github.gimme.gimmebot.core.command.BaseCommand
import com.github.gimme.gimmebot.core.command.CommandResponse
import com.github.gimme.gimmebot.core.command.executor.CommandExecutor
import kotlin.math.pow
import kotlin.math.roundToInt

private const val USAGE = "mortgage <durationYears> <rent> <propertyValue=0> <propertyReturnPercent=0>" +
        " <marketReturnPercent=8> <loanPercent=85> <loanAmortizationPercent=2> <loanInterestPercent=1.5>"

class MortgageCommand : BaseCommand("mortgage") {

    @CommandExecutor
    fun execute(
        durationYears: Double,
        rent: Double,
        propertyValue: Double = 0.0,
        propertyReturnPercent: Double = 0.0,
        marketReturnPercent: Double = 8.0,
        loanPercent: Double = 85.0,
        loanAmortizationPercent: Double = 2.0,
        loanInterestPercent: Double = 1.5,
    ): CommandResponse {
        val durationMonths = (durationYears * 12).roundToInt()

        val simulation = Simulation(marketReturnPercent / 100, propertyReturnPercent / 100)

        simulation.addRent(rent)
        simulation.buyProperty(propertyValue)
        simulation.takeLoan((loanPercent / 100) * propertyValue,
            loanInterestPercent / 100,
            loanAmortizationPercent / 100)
        simulation.fastForward(durationMonths)

        val responseBody = Result(
            simulation.totalPayed,
            if (durationMonths == 0) 0.0 else simulation.totalPayed / durationMonths,
            simulation.getTotalRevenue(),
            if (durationMonths == 0) 0.0 else simulation.getTotalRevenue() / durationMonths,
        )

        return CommandResponse(responseBody.toString(), responseBody)
    }

    /**
     * Returns how to use the command.
     *
     * This is a temporary override before default values are fully supported. It can be removed when the default
     * implementation shows default values.
     */
    override fun getUsage(): String {
        return USAGE
    }

    internal data class Result(
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

    private class Loan(amount: Double, val interestRate: Double, amortizationRate: Double) {
        var debt: Double = amount
        val minAmortizationPerMonth: Double = (amortizationRate / 12) * amount

        fun amortize(amount: Double = minAmortizationPerMonth): Double {
            debt -= amount

            if (debt < 0) {
                val overpaid = -debt
                debt = 0.0
                return amount - overpaid
            }

            return amount
        }

        fun getCurrentMonthlyInterestCost(): Double = (interestRate / 12) * debt
    }

    private class Simulation(stockInterest: Double, propertyInterest: Double) {
        val loans: MutableList<Loan> = mutableListOf()

        var rent = 0.0
        var stockInterestMonthly = (1 + stockInterest).pow(1 / 12.0) - 1
        var propertyInterestMonthly = (1 + propertyInterest).pow(1 / 12.0) - 1
        var totalPayed = 0.0
            private set
        var totalAmortized = 0.0
            private set
        var ghostSavings = 0.0 // Money that could have been saved instead of paying off the property
            private set

        private var totalPayedThisMonth = 0.0
        var propertyValue = 0.0
        var moneyInTheBank = 0.0

        fun fastForward(months: Int) {
            ghostSavings += (propertyValue - loans.sumOf(Loan::debt)).coerceAtLeast(0.0)
            repeat(months) { doMonth() }
        }

        private fun doMonth() {
            // BEGINNING OF MONTH

            resetMonth()

            payRent()

            val payedAtTheBeginningOfMonth = totalPayedThisMonth
            ghostSavings += payedAtTheBeginningOfMonth

            // END OF MONTH

            ghostSavings *= 1 + stockInterestMonthly
            propertyValue *= 1 + propertyInterestMonthly

            payLoanRates()
            amortizeLoans()

            ghostSavings += totalPayedThisMonth - payedAtTheBeginningOfMonth
            totalPayed += totalPayedThisMonth
            moneyInTheBank -= totalPayedThisMonth
        }

        private fun resetMonth() {
            totalPayedThisMonth = 0.0
        }

        private fun payRent() {
            totalPayedThisMonth += rent
        }

        private fun payLoanRates() {
            for (loan in loans) {
                totalPayedThisMonth += loan.getCurrentMonthlyInterestCost()
            }
        }

        private fun amortizeLoans() {
            for (loan in loans) {
                val amortized = loan.amortize()
                totalPayedThisMonth += amortized
                totalAmortized += amortized
            }
        }

        fun addRent(rent: Double) {
            this.rent += rent
        }

        fun takeLoan(amount: Number, interestRate: Double, amortizationRate: Double): Simulation {
            moneyInTheBank += amount.toDouble()
            loans.add(Loan(amount.toDouble(), interestRate, amortizationRate))
            return this
        }

        fun buyProperty(propertyValue: Double): Simulation {
            moneyInTheBank -= propertyValue
            this.propertyValue += propertyValue
            return this
        }

        fun getTotalRevenue(): Double =
            -(ghostSavings - totalPayed) + moneyInTheBank + propertyValue - loans.sumOf(Loan::debt)
    }
}
