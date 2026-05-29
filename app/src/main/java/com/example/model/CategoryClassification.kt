package com.example.model

fun Article.matchesCategory(categoryId: String): Boolean {
    val titleStr = (this.title ?: "").lowercase()
    val descStr = (this.description ?: "").lowercase()
    val contentStr = (this.content ?: "").lowercase()
    val sourceName = (this.source?.name ?: "").lowercase()
    val urlStr = (this.url ?: "").lowercase()

    val text = "$titleStr $descStr $contentStr $sourceName $urlStr"

    return when (categoryId) {
        "stock_markets" -> text.anyOf("stock", "market", "nasdaq", "dow jo", "s&p", "nifty", "shares", "equity", "bse", "nse", "sensex", "trading", "nikkei", "hang seng")
        "banking_finance" -> text.anyOf("bank", "finance", "fed ", "reserve", "loan", "interest rate", "inflation", "audit", "tax", "banking", "treasury", "fiscal", "lending")
        "commodities" -> text.anyOf("gold", "oil", "crude", "silver", "commodity", "steel", "copper", "gas", "fuel", "zinc", "aluminum", "brent")
        "forex" -> text.anyOf("forex", "exchange rate", "currency", "usd", "eur", "inr", "rupee", "pound", "yen", "dinar", "shekel", "forex")
        "crypto_web3" -> text.anyOf("crypto", "bitcoin", "ethereum", "btc", "eth", "solana", "nft", "blockchain", "web3", "coin", "ledger", "binance", "coinbase")
        "macro_economy" -> text.anyOf("economy", "gdp", "growth", "recession", "economic", "trade", "fiscal", "deficit", "unemployment", "tariff", "g7")
        "tech_startups" -> text.anyOf("startup", "venture", "funding", "ipo", "acquisition", "fintech", "saas", "tech company", "unicorn", "series a", "series b", "series c", "seed round", "founder")
        "real_estate" -> text.anyOf("real estate", "housing", "property", "apartment", "building", "home price", "mortgage", "developer", "sublease", "renting")
        "esg_sustainability" -> text.anyOf("esg", "sustainability", "carbon", "renewable", "green energy", "solar", "wind power", "climate change", "eco-friendly")
        
        "football_soccer" -> text.anyOf("football", "soccer", "laliga", "premier league", "chelsea", "arsenal", "liverpool", "manchester", "real madrid", "barcelona", "bayern", "messi", "ronaldo", "fifa", "uefa", "penalty", "stadium", "champions league")
        "cricket" -> text.anyOf("cricket", "cricbuzz", "ipl", "t20", "odi", "test match", "dhoni", "kohli", "wickets", "runs", "innings", "bowling", "batting", "bcci", "icc")
        "basketball" -> text.anyOf("basketball", "nba", "lakers", "lebron", "curry", "dunk", "hoops")
        "tennis" -> text.anyOf("tennis", "wimbledon", "nadal", "federer", "djokovic", "grand slam", "open", "atp", "wta")
        "athletics_olympics" -> text.anyOf("olympics", "athletics", "run", "marathon", "sprint", "relay", "shot put", "javelin")
        "baseball" -> text.anyOf("baseball", "mlb", "yankees", "home run", "batter", "pitcher")
        "american_football" -> text.anyOf("nfl", "super bowl", "quarterback", "touchdown", "gridiron")
        "motorsports" -> text.anyOf("f1", "formula 1", "grand prix", "racing", "nascar", "motogp", "hamilton", "verstappen", "ferrari")
        "combat_sports" -> text.anyOf("ufc", "boxing", "mma", "wrestling", "fight", "champion", "knockout", "tko")
        "badminton" -> text.anyOf("badminton", "shuttlecock", "racket", "smash")
        "volleyball" -> text.anyOf("volleyball", "spike", "block")
        "aquatics" -> text.anyOf("swimming", "pool", "phelps", "diving")
        "kabaddi_local" -> text.anyOf("kabaddi", "pro kabaddi", "raid", "tackle")
        "esports" -> text.anyOf("esports", "gaming", "streamer", "twitch", "fortnite", "pubg", "cod", "valorant", "dota", "cs:go", "league of legends")
        "hockey_rugby_golf" -> text.anyOf("hockey", "rugby", "golf", "pga", "nhl", "puck", "try", "scrum", "green", "putter")

        "geopolitics" -> text.anyOf("geopolitics", "treaty", "summit", "nato", "un ", "unilateral", "sanctions", "discussions", "china-us", "diplomatic")
        "politics_elections" -> text.anyOf("politics", "election", "biden", "trump", "government", "parliament", "senate", "congress", "democrat", "republican", "voting", "bill", "prime minister", "president")
        "defence_security" -> text.anyOf("defence", "defense", "military", "army", "navy", "weapons", "missile", "security", "pentagon", "nuclear", "war", "border")
        "climate_environment" -> text.anyOf("climate", "environment", "global warming", "nature", "forest", "emissions", "pollution", "wildfire", "earthquake", "hurricane")
        "health_science" -> text.anyOf("health", "science", "covid", "vaccine", "cancer", "space", "study", "research", "medical", "doctor", "hospital", "discovery", "dna", "biology")

        "artificial_intelligence" -> text.anyOf("ai", "artificial intelligence", "gemini", "chatgpt", "openai", "nvidia", "llm", "machine learning", "copilot", "anthropic", "claude")
        "consumer_tech" -> text.anyOf("iphone", "apple", "samsung", "pixel", "android", "gadget", "laptop", "smartphone", "smartwatch", "ipad", "macbook", "device")
        "cloud_enterprise" -> text.anyOf("cloud", "aws", "azure", "enterprise", "software", "infrastructure", "server", "cybersecurity", "saas")
        "space_tech" -> text.anyOf("space", "nasa", "spacex", "rocket", "satellite", "orbit", "mars", "moon", "galaxy", "telescope", "artemis")

        "movies_ott" -> text.anyOf("movie", "netflix", "prime video", "disney", "hollywood", "bollywood", "cinema", "trailer", "oscar", "actor", "furious", "film", "series")
        "music" -> text.anyOf("music", "song", "album", "spotify", "singer", "concert", "grammy", "lyrics", "billboard", "tour")
        "tv_web_series" -> text.anyOf("tv series", "show", "episode", "season", "drama", "sitcom")
        "celebrity_culture" -> text.anyOf("celebrity", "gossip", "star", "avatar", "rumor", "influence", "fashion")

        else -> false
    }
}

fun Article.isSportsArticle(): Boolean {
    val sportsCategoryIds = listOf(
        "football_soccer", "cricket", "basketball", "tennis", "athletics_olympics", "baseball",
        "american_football", "motorsports", "combat_sports", "badminton", "volleyball", "aquatics",
        "kabaddi_local", "esports", "hockey_rugby_golf"
    )
    if (sportsCategoryIds.any { matchesCategory(it) }) return true
    
    val sourceName = (this.source?.name ?: "").lowercase()
    val urlStr = (this.url ?: "").lowercase()
    val titleStr = (this.title ?: "").lowercase()
    val descStr = (this.description ?: "").lowercase()
    val text = "$titleStr $descStr $sourceName $urlStr"
    
    return text.anyOf("espn", "cricbuzz", "sport", "score", "game", "match", "cup", "stadium", "athlete", "championship", "tournament", "ipl")
}

private fun String.anyOf(vararg keywords: String): Boolean {
    return keywords.any { this.contains(it) }
}
