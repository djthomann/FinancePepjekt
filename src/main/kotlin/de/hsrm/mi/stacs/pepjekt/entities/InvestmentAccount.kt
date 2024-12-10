package de.hsrm.mi.stacs.pepjekt.entities

class InvestmentAccount(val bankAccount: BankAccount) {
    val portfolio: MutableMap<Stock, Float> = mutableMapOf()
    val owner: User? = null
}