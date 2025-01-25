package de.hsrm.mi.stacs.pepjekt.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("latest_crypto_quote")
class CryptoQuoteLatest(
    @Id
    var cryptoSymbol: String,
    var quote_id: Long,
)