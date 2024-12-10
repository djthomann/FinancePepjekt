package de.hsrm.mi.stacs.pepjekt.entities

data class Exchange(
    val stocks: List<Stock>,
    val symbol: String,
    val description: String
) {
}