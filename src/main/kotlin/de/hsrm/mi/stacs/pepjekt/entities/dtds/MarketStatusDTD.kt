package de.hsrm.mi.stacs.pepjekt.entities.dtds

data class MarketStatusDTD(
    var exchange: String,
    var holiday: String? = null,
    var isOpen: Boolean,
    var session: String? = null,
    var timezone: String,
    var t: Long
)
