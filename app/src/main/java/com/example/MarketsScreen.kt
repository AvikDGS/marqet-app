package com.example

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.CryptoPriceResponse
import com.example.model.YahooSparkItem
import com.example.viewmodel.MarketsUiState
import com.example.viewmodel.MarketsViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

val PositiveGreen = Color(0xFF00E676)
val NegativeRed = Color(0xFFFF5252)

@Composable
fun MarketsScreen(viewModel: MarketsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(AppBackgroundGradient)) {
        when (val state = uiState) {
            is MarketsUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
            }
            is MarketsUiState.Error -> {
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.fetchMarketsData() }) { Text("Retry") }
                }
            }
            is MarketsUiState.Success -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        MarketsTicker(state)
                    }

                    item {
                        val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                        val timeStr = sdf.format(Date(state.timestamp))
                        Text(
                            text = "Last updated: $timeStr",
                            color = TextMutedColor,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 24.dp).padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    
                    item {
                        SectionHeader("Indian Markets")
                        val indianSymbols = listOf(
                            Pair("^BSESN", "SENSEX"), Pair("^NSEI", "NIFTY 50"),
                            Pair("^NSEBANK", "NIFTY BANK"), Pair("^NSMIDCP", "MIDCAP")
                        )
                        MarketGrid(symbols = indianSymbols, data = state.yahooData)
                    }

                    item {
                        SectionHeader("Global Markets")
                        val globalSymbols = listOf(
                            Pair("^IXIC", "NASDAQ"), Pair("^GSPC", "S&P 500"),
                            Pair("^DJI", "DOW JONES"), Pair("^FTSE", "FTSE 100"),
                            Pair("^N225", "NIKKEI 225")
                        )
                        MarketGrid(symbols = globalSymbols, data = state.yahooData)
                    }

                    item {
                        SectionHeader("Crypto")
                        CryptoGrid(data = state.cryptoData)
                    }

                    item {
                        SectionHeader("Commodities")
                        val commoditySymbols = listOf(
                            Pair("GC=F", "Gold"), Pair("SI=F", "Silver"),
                            Pair("CL=F", "Crude Oil (WTI)"), Pair("BZ=F", "Crude Oil (Brent)"),
                            Pair("NG=F", "Natural Gas")
                        )
                        MarketGrid(symbols = commoditySymbols, data = state.yahooData, showGraph=false)
                    }

                    item {
                        SectionHeader("Forex")
                        val forexSymbols = listOf(
                            Pair("INR=X", "USD/INR"), Pair("EURINR=X", "EUR/INR"),
                            Pair("GBPINR=X", "GBP/INR"), Pair("JPYINR=X", "JPY/INR")
                        )
                        MarketGrid(symbols = forexSymbols, data = state.yahooData, showGraph=false)
                        Spacer(modifier = Modifier.height(100.dp)) // bottom padding
                    }
                }
            }
        }
    }
}

@Composable
fun MarketsTicker(state: MarketsUiState.Success) {
    val scrollState = rememberScrollState()
    LaunchedEffect(scrollState.maxValue) {
        if(scrollState.maxValue > 0) {
            while (true) {
                scrollState.animateScrollTo(scrollState.maxValue, animationSpec = androidx.compose.animation.core.tween(durationMillis = 20000, easing = androidx.compose.animation.core.LinearEasing))
                scrollState.scrollTo(0)
            }
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GlassBackground)
            .border(width = 1.dp, color = GlassBorder)
            .padding(vertical = 12.dp)
            .horizontalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.width(24.dp))
        val tickerItems = listOf(
            Pair("SENSEX", state.yahooData["^BSESN"]),
            Pair("NIFTY50", state.yahooData["^NSEI"]),
            Pair("NASDAQ", state.yahooData["^IXIC"]),
            Pair("S&P500", state.yahooData["^GSPC"]),
            Pair("BTC", state.cryptoData["bitcoin"]),
            Pair("ETH", state.cryptoData["ethereum"]),
            Pair("Gold", state.yahooData["GC=F"]),
            Pair("USD/INR", state.yahooData["INR=X"])
        )

        for ((name, data) in tickerItems) {
            val change = when(data) {
                is YahooSparkItem -> {
                    val prev = data.chartPreviousClose ?: (data.previousClose ?: 1.0)
                    val curr = data.close.lastOrNull() ?: prev
                    ((curr - prev) / prev) * 100
                }
                is CryptoPriceResponse -> data.usd_24h_change ?: 0.0
                else -> 0.0
            }
            val color = if (change >= 0) PositiveGreen else NegativeRed
            val arrow = if (change >= 0) "▲" else "▼"
            val displayChange = String.format(Locale.getDefault(), "%.2f%%", kotlin.math.abs(change))
            
            Row(modifier = Modifier.padding(end = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("$arrow $displayChange", color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        color = Color.White,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
    )
}

@Composable
fun MarketGrid(symbols: List<Pair<String,String>>, data: Map<String, YahooSparkItem>, showGraph:Boolean = true) {
    val itemsPerRow = 2
    val rows = (symbols.size + 1) / itemsPerRow
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        for (i in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                for (j in 0 until itemsPerRow) {
                    val idx = i * itemsPerRow + j
                    if (idx < symbols.size) {
                        val sym = symbols[idx]
                        val itemData = data[sym.first]
                        Box(modifier = Modifier.weight(1f).padding(end = if (j == 0) 12.dp else 0.dp)) {
                            MarketCard(title = sym.second, data = itemData, showGraph=showGraph)
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f).padding(end = 0.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MarketCard(title: String, data: YahooSparkItem?, showGraph: Boolean) {
    val prev = data?.chartPreviousClose ?: (data?.previousClose ?: 1.0)
    val curr = data?.close?.lastOrNull() ?: prev
    val change = if (prev != 0.0) ((curr - prev) / prev) * 100 else 0.0
    val color = if (change >= 0) PositiveGreen else NegativeRed
    val arrow = if (change >= 0) "▲" else "▼"

    val (isOpen, countdownStr) = com.example.utils.DateTimeUtils.getMarketStatusAndCountdown()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GlassBackground)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text(title, color = TextSecondaryColor, fontSize = 14.sp)
                Box(
                    modifier = Modifier
                        .background(if (isOpen) PositiveGreen.copy(alpha=0.2f) else NegativeRed.copy(alpha=0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        if (isOpen) "MARKET OPEN" else "MARKET CLOSED",
                        color = if (isOpen) PositiveGreen else NegativeRed,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                String.format(java.util.Locale.getDefault(), "%.2f", curr), 
                color = Color.White, 
                fontSize = 18.sp, 
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$arrow ${String.format(java.util.Locale.getDefault(), "%.2f%%", kotlin.math.abs(change))}", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                if (showGraph && data != null && data.close.isNotEmpty()) {
                    Spacer(modifier = Modifier.weight(1f))
                    val points = data.close.filterNotNull()
                    if (points.size > 1) {
                        Sparkline(points, color, modifier = Modifier.size(width = 60.dp, height = 30.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (!isOpen && countdownStr.isNotEmpty()) {
                Text(countdownStr, color = NegativeRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
            Text("Last updated: ${com.example.utils.DateTimeUtils.getCurrentIstTime()}", color = TextSecondaryColor.copy(alpha=0.5f), fontSize=9.sp)
        }
    }
}

@Composable
fun CryptoGrid(data: Map<String, CryptoPriceResponse>) {
    val cryptos = listOf(
        Pair("bitcoin", "Bitcoin (BTC)"), Pair("ethereum", "Ethereum (ETH)"),
        Pair("binancecoin", "BNB"), Pair("solana", "Solana"),
        Pair("ripple", "Ripple (XRP)"), Pair("dogecoin", "Dogecoin")
    )
    val itemsPerRow = 2
    val rows = (cryptos.size + 1) / itemsPerRow
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        for (i in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                for (j in 0 until itemsPerRow) {
                    val idx = i * itemsPerRow + j
                    if (idx < cryptos.size) {
                        val sym = cryptos[idx]
                        val itemData = data[sym.first]
                        Box(modifier = Modifier.weight(1f).padding(end = if (j == 0) 12.dp else 0.dp)) {
                            CryptoCard(title = sym.second, data = itemData)
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f).padding(end = 0.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CryptoCard(title: String, data: CryptoPriceResponse?) {
    val usd = data?.usd ?: 0.0
    val inr = data?.inr ?: 0.0
    val change = data?.usd_24h_change ?: 0.0
    val color = if (change >= 0) PositiveGreen else NegativeRed
    val arrow = if (change >= 0) "▲" else "▼"
    
    // Auto-generate some visual mockup points for a 7-day chart (sine-wave-like simulation based on change) 
    // Since CoinGecko simple api doesn't give 7d array, we just show a subtle mock graph that correlates with 24h trend
    val simulatedPoints = remember(change) {
        val base = 100.0
        val drift = change / 7.0
        List(7) { ix -> base + (ix * drift) + (Math.sin(ix.toDouble()) * 2) }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GlassBackground)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text(title, color = TextSecondaryColor, fontSize = 14.sp)
                Box(
                    modifier = Modifier
                        .background(PositiveGreen.copy(alpha=0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        "ALWAYS OPEN",
                        color = PositiveGreen,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            val formatStr = if (usd < 1.0) "%.4f" else "%.2f"
            Text("$${String.format(java.util.Locale.getDefault(), formatStr, usd)}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            val formatInr = if (inr < 10.0) "%.2f" else "%.0f"
            Text("₹${String.format(java.util.Locale.getDefault(), formatInr, inr)}", color = TextMutedColor, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$arrow ${String.format(java.util.Locale.getDefault(), "%.2f%%", kotlin.math.abs(change))}", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Sparkline(simulatedPoints, color, modifier = Modifier.size(width = 50.dp, height = 24.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Last updated: ${com.example.utils.DateTimeUtils.getCurrentIstTime()}", color = TextSecondaryColor.copy(alpha=0.5f), fontSize=9.sp)
        }
    }
}

@Composable
fun Sparkline(points: List<Double>, color: Color, modifier: Modifier = Modifier) {
    if (points.isEmpty()) return
    Canvas(modifier = modifier) {
        val minPrice = points.minOrNull() ?: 0.0
        val maxPrice = points.maxOrNull() ?: 1.0
        val range = max(maxPrice - minPrice, 0.0001)

        val width = size.width
        val height = size.height

        val stepX = width / max(points.size - 1, 1).toFloat()

        val path = Path()
        points.forEachIndexed { index, price ->
            val x = index * stepX
            val normalizedY = 1f - ((price - minPrice) / range).toFloat()
            val y = normalizedY * height

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
