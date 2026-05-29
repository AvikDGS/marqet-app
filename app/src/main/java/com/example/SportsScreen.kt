package com.example

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Article
import com.example.model.CategoryData
import com.example.model.isSportsArticle
import com.example.model.matchesCategory
import com.example.viewmodel.NewsUiState
import com.example.viewmodel.NewsViewModel
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SportsScreen(
    viewModel: NewsViewModel,
    onArticleClick: (Article) -> Unit,
    onAllCategoriesClick: () -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val uiState by viewModel.newsState.collectAsStateWithLifecycle()
    val newStoriesAvailable by viewModel.newStoriesAvailable.collectAsStateWithLifecycle()

    val sportsCategories = CategoryData.allCategories.filter { it.group == "SPORTS" }.sortedBy { it.name }
    var activeSportsCategoryId by remember { mutableStateOf(sportsCategories.firstOrNull()?.id ?: "") }

    val context = LocalContext.current
    val isRefreshing = uiState is NewsUiState.Success && (uiState as NewsUiState.Success).isRefreshing
    val pullToRefreshState = rememberPullToRefreshState()
    var wasRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(isRefreshing) {
        if (wasRefreshing && !isRefreshing) {
            Toast.makeText(context, "Sports feed updated • just now", Toast.LENGTH_SHORT).show()
        }
        wasRefreshing = isRefreshing
    }

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.testTag("sports_screen_scaffold")
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                viewModel.fetchNews(isRefresh = true)
            },
            state = pullToRefreshState,
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            indicator = {
                PullToRefreshDefaults.Indicator(
                    isRefreshing = isRefreshing,
                    state = pullToRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    containerColor = Color(0xFF1A1A2E),
                    color = Color.White
                )
            }
        ) {
            Box(modifier = Modifier.fillMaxSize().background(AppBackgroundGradient)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    if (newStoriesAvailable) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(GlassBackground)
                                .clickable { viewModel.loadPendingStories() }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("New sports stories available ↑ Tap to load", color = Color.White, fontSize = 14.sp)
                        }
                    }

                    // Header Section
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = onAllCategoriesClick, modifier = Modifier.offset(x = (-12).dp)) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                                }
                                Column {
                                    Text(
                                        text = "Marqet Sports",
                                        style = androidx.compose.ui.text.TextStyle(
                                            brush = Brush.linearGradient(listOf(Color.White, Color.LightGray)),
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = com.example.utils.DateTimeUtils.getTodayHeaderDate(),
                                        color = TextSecondaryColor,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = onProfileClick,
                                    modifier = Modifier.background(GlassBackground, CircleShape).border(1.dp, GlassBorder, CircleShape)
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { viewModel.fetchNews(isRefresh = true) },
                                    modifier = Modifier.background(GlassBackground, CircleShape).border(1.dp, GlassBorder, CircleShape)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Top Championships & Match Coverage",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Sticky Search bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 12.dp)
                            .background(GlassBackground, RoundedCornerShape(50))
                            .border(1.dp, GlassBorder, RoundedCornerShape(50))
                            .clickable { onSearchClick() }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("sports_search_bar"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Search news, markets, sports...",
                            color = TextSecondaryColor,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                    }

                    if (uiState is NewsUiState.Success && (uiState as NewsUiState.Success).isRefreshing) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Color.White, trackColor = GlassBackground)
                    }

                    // Sports Categories Horizontal Row
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = 24.dp).testTag("sports_categories_row")
                    ) {
                        items(sportsCategories) { category ->
                            val isSelected = category.id == activeSportsCategoryId
                            Box(
                                modifier = Modifier
                                    .border(1.dp, if (isSelected) Color.White else GlassBorder, RoundedCornerShape(50))
                                    .background(if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(50))
                                    .clickable {
                                        activeSportsCategoryId = category.id
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .testTag("sports_category_tab_${category.id}")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        category.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.Black else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = category.name.uppercase(),
                                        color = if (isSelected) Color.Black else Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        when (val state = uiState) {
                            is NewsUiState.Loading -> {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
                            }
                            is NewsUiState.Error -> {
                                Column(
                                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(state.message, color = MaterialTheme.colorScheme.error)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { viewModel.fetchNews() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                                    ) {
                                        Text("Retry")
                                    }
                                }
                            }
                            is NewsUiState.Success -> {
                                val allArticles = state.articles
                                val sportsArticles = allArticles.filter { it.isSportsArticle() }

                                val filteredSportsArticles = sportsArticles.filter { it.matchesCategory(activeSportsCategoryId) }
                                val finalSportsArticles = if (filteredSportsArticles.isNotEmpty()) filteredSportsArticles else sportsArticles

                                if (finalSportsArticles.isNotEmpty()) {
                                    val now = System.currentTimeMillis()
                                    val last24h = mutableListOf<Article>()
                                    val earlierStories = mutableListOf<Article>()
                                    val fromThisWeek = mutableListOf<Article>()

                                    finalSportsArticles.forEach { art ->
                                        val time = com.example.utils.DateTimeUtils.parseIsoDate(art.publishedAt)?.time ?: 0L
                                        val diffHours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(now - time)
                                        when {
                                            diffHours < 24 -> last24h.add(art)
                                            diffHours < 24 * 7 -> earlierStories.add(art)
                                            else -> fromThisWeek.add(art)
                                        }
                                    }

                                    val feedArticles = if (last24h.isNotEmpty()) last24h else finalSportsArticles.take(1)
                                    val fallbackUsed = last24h.isEmpty()

                                    Column(modifier = Modifier.fillMaxSize()) {
                                        if (state.isOffline) {
                                            Box(
                                                modifier = Modifier.fillMaxWidth().background(Color(0xFFE53935)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "Offline Mode. Showing cached sports news.",
                                                    color = Color.White,
                                                    modifier = Modifier.padding(8.dp),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        LazyColumn(
                                            contentPadding = PaddingValues(bottom = 24.dp),
                                            modifier = Modifier.fillMaxSize().testTag("sports_news_list")
                                        ) {
                                            if (feedArticles.isNotEmpty()) {
                                                item {
                                                    Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                                                        FeaturedArticleCard(feedArticles.first(), onClick = { onArticleClick(feedArticles.first()) })
                                                    }
                                                }
                                                items(feedArticles.drop(1)) { article ->
                                                    Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                                                        GlassArticleCard(article, onClick = { onArticleClick(article) })
                                                    }
                                                }
                                            }

                                            val finalEarlier = if (fallbackUsed) earlierStories.drop(1) else earlierStories
                                            if (finalEarlier.isNotEmpty()) {
                                                item {
                                                    Text(
                                                        text = "Earlier sports news",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 20.sp,
                                                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                                                    )
                                                }
                                                items(finalEarlier) { article ->
                                                    Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                                                        GlassArticleCard(article, onClick = { onArticleClick(article) })
                                                    }
                                                }
                                            }

                                            val finalWeekly = if (fallbackUsed && earlierStories.isEmpty()) fromThisWeek.drop(1) else fromThisWeek
                                            if (finalWeekly.isNotEmpty()) {
                                                item {
                                                    Text(
                                                        text = "From this week",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 20.sp,
                                                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                                                    )
                                                }
                                                items(finalWeekly) { article ->
                                                    Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                                                        GlassArticleCard(article, onClick = { onArticleClick(article) })
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "No sports news available. Connect to a network or refresh.",
                                            color = Color.White,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
