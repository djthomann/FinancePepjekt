package de.hsrm.mi.stacs.pepjekt.entities.dtds

//variables are defined by the finnhub api
data class StockQuoteDTD(
    var c: Float,   // current Price
    var d: Float,   // change
    var dp: Float,  // percent change
    var h: Float,   // high price
    var l: Float,   // low price
    var o: Float,   // open price
    var pc: Float,  // previous close price
    var t: Long,    // time
)
