package de.hsrm.mi.stacs.pepjekt.model

data class Exchange(
    val stocks: List<Stock>,
    val symbol: String,
    val description: String
) {
}