package de.hsrm.mi.stacs.pepjekt.model

data class Stock(
    val symbol: String,
    val description: String,
    val figi: String,
    val currency: Currency
) {
}