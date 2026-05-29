package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.ui.graphics.vector.ImageVector

data class NewsCategory(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val group: String
)

object CategoryData {
    val allCategories = listOf(
        // BUSINESS & MARKETS
        NewsCategory("stock_markets", "Stock Markets", Icons.AutoMirrored.Filled.TrendingUp, "BUSINESS & MARKETS"),
        NewsCategory("banking_finance", "Banking & Finance", Icons.Default.AccountBalance, "BUSINESS & MARKETS"),
        NewsCategory("commodities", "Commodities", Icons.Default.Layers, "BUSINESS & MARKETS"),
        NewsCategory("forex", "Forex", Icons.Default.CurrencyExchange, "BUSINESS & MARKETS"),
        NewsCategory("crypto_web3", "Crypto & Web3", Icons.Default.CurrencyBitcoin, "BUSINESS & MARKETS"),
        NewsCategory("macro_economy", "Macro & Economy", Icons.Default.Public, "BUSINESS & MARKETS"),
        NewsCategory("tech_startups", "Tech & Startups", Icons.Default.Business, "BUSINESS & MARKETS"),
        NewsCategory("real_estate", "Real Estate", Icons.Default.Home, "BUSINESS & MARKETS"),
        NewsCategory("esg_sustainability", "ESG & Sustainability", Icons.Default.Eco, "BUSINESS & MARKETS"),

        // SPORTS
        NewsCategory("football_soccer", "Football/Soccer", Icons.Default.SportsSoccer, "SPORTS"),
        NewsCategory("cricket", "Cricket", Icons.Default.SportsCricket, "SPORTS"),
        NewsCategory("basketball", "Basketball", Icons.Default.SportsBasketball, "SPORTS"),
        NewsCategory("tennis", "Tennis", Icons.Default.SportsTennis, "SPORTS"),
        NewsCategory("athletics_olympics", "Athletics & Olympics", Icons.AutoMirrored.Filled.DirectionsRun, "SPORTS"),
        NewsCategory("baseball", "Baseball", Icons.Default.SportsBaseball, "SPORTS"),
        NewsCategory("american_football", "American Football", Icons.Default.SportsFootball, "SPORTS"),
        NewsCategory("motorsports", "Motorsports", Icons.Default.SportsMotorsports, "SPORTS"),
        NewsCategory("combat_sports", "Combat Sports", Icons.Default.SportsMartialArts, "SPORTS"),
        NewsCategory("badminton", "Badminton", Icons.Default.SportsTennis, "SPORTS"),
        NewsCategory("volleyball", "Volleyball", Icons.Default.SportsVolleyball, "SPORTS"),
        NewsCategory("aquatics", "Aquatics", Icons.Default.Pool, "SPORTS"),
        NewsCategory("kabaddi_local", "Kabaddi & Local Sports", Icons.Default.SportsKabaddi, "SPORTS"),
        NewsCategory("esports", "Esports", Icons.Default.SportsEsports, "SPORTS"),
        NewsCategory("hockey_rugby_golf", "Hockey & Rugby & Golf", Icons.Default.SportsGolf, "SPORTS"),

        // WORLD NEWS
        NewsCategory("geopolitics", "Geopolitics", Icons.Default.Public, "WORLD NEWS"),
        NewsCategory("politics_elections", "Politics & Elections", Icons.Default.HowToVote, "WORLD NEWS"),
        NewsCategory("defence_security", "Defence & Security", Icons.Default.Security, "WORLD NEWS"),
        NewsCategory("climate_environment", "Climate & Environment", Icons.Default.Park, "WORLD NEWS"),
        NewsCategory("health_science", "Health & Science", Icons.Default.Science, "WORLD NEWS"),

        // TECH & INNOVATION
        NewsCategory("artificial_intelligence", "Artificial Intelligence", Icons.Default.SmartToy, "TECH & INNOVATION"),
        NewsCategory("consumer_tech", "Consumer Tech", Icons.Default.Devices, "TECH & INNOVATION"),
        NewsCategory("cloud_enterprise", "Cloud & Enterprise", Icons.Default.Cloud, "TECH & INNOVATION"),
        NewsCategory("space_tech", "Space Tech", Icons.Default.RocketLaunch, "TECH & INNOVATION"),

        // ENTERTAINMENT
        NewsCategory("movies_ott", "Movies & OTT", Icons.Default.Movie, "ENTERTAINMENT"),
        NewsCategory("music", "Music", Icons.Default.MusicNote, "ENTERTAINMENT"),
        NewsCategory("tv_web_series", "TV & Web Series", Icons.Default.Tv, "ENTERTAINMENT"),
        NewsCategory("celebrity_culture", "Celebrity & Culture", Icons.Default.Star, "ENTERTAINMENT")
    )
    
    fun getById(id: String): NewsCategory? = allCategories.find { it.id == id }
}
