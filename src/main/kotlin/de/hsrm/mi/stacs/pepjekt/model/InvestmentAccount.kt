package de.hsrm.mi.stacs.pepjekt.model

class InvestmentAccount(val bankAccount: BankAccount) {
    val portfolio: MutableMap<Stock, Float> = mutableMapOf()
}