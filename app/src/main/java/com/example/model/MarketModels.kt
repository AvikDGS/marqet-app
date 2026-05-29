package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class CryptoPriceResponse(
    val usd: Double? = null,
    val usd_24h_change: Double? = null,
    val inr: Double? = null,
    val inr_24h_change: Double? = null
)

@Serializable
data class YahooSparkItem(
    val symbol: String? = null,
    val previousClose: Double? = null,
    val chartPreviousClose: Double? = null,
    val close: List<Double?> = emptyList()
)
