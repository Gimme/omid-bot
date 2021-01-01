package com.github.gimme.omidbot

import kotlin.math.pow

class Simulation(startingBalance: Double, stockInterest: Double = 0.0, propertyInterest: Double = 0.0) {
    private val loans: MutableList<Loan> = mutableListOf()

    var monthlyIncome = 0.0
    var rent = 0.0
    var stockInterestMonthly = (1 + stockInterest).pow(1 / 12.0) - 1
    var propertyInterestMonthly = (1 + propertyInterest).pow(1 / 12.0) - 1
    var totalPayed = 0.0
        private set
    var totalAmortized = 0.0
        private set
    var ghostSavings = 0.0 // Money that could have been saved instead of paying off the property
        private set
    var investMoneyInTheBank = false

    private var totalPayedThisMonth = 0.0
    var propertyValue = 0.0
    var moneyInTheBank = startingBalance

    fun fastForward(months: Int): Simulation {
        ghostSavings += (propertyValue - loans.sumOf(Loan::debt)).coerceAtLeast(0.0)
        repeat(months) { doMonth() }
        return this
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
        if (investMoneyInTheBank) {
            moneyInTheBank *= 1 + stockInterestMonthly
        }

        payLoanRates()
        amortizeLoans()

        ghostSavings += totalPayedThisMonth - payedAtTheBeginningOfMonth
        totalPayed += totalPayedThisMonth
        moneyInTheBank -= totalPayedThisMonth
        moneyInTheBank += monthlyIncome
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

    fun addIncome(monthlyIncome: Double): Simulation {
        this.monthlyIncome += monthlyIncome
        return this
    }

    fun addRent(rent: Double): Simulation {
        this.rent += rent
        return this
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
}
