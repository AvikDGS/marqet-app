package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.border
import androidx.compose.ui.focus.focusRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Article
import com.example.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel, 
    onBackClick: () -> Unit,
    onArticleClick: (Article) -> Unit,
    onVoiceSearchClick: () -> Unit
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()

    var recentSearches by remember { mutableStateOf(listOf("Tata", "Apple event", "Federal reserve", "Bitcoin rally")) }
    val trendingSearches = listOf("NVIDIA earnings", "Tesla", "T20 World Cup", "US Elections", "Gold price")
    
    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = query,
                        onValueChange = viewModel::onQueryChange,
                        placeholder = { Text("Search news, markets, sports...", color = Color.Gray) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onQueryChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White)
                                }
                            } else {
                                IconButton(onClick = onVoiceSearchClick) {
                                    Icon(Icons.Default.Mic, contentDescription = "Voice Search", tint = Color.White)
                                }
                            }
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (query.isEmpty()) {
                // Show trending and recent
                LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    item {
                        Text("Trending searches", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 16.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(trendingSearches) { trend ->
                                AssistChip(
                                    onClick = { viewModel.onQueryChange(trend) },
                                    label = { Text(trend, color = Color.White) },
                                    colors = AssistChipDefaults.assistChipColors(containerColor = Color.White.copy(alpha = 0.1f)),
                                    border = null
                                )
                            }
                        }
                    }

                    item {
                        Text("Recent searches", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 16.dp))
                    }
                    
                    items(recentSearches) { recent ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { viewModel.onQueryChange(recent) }.padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = "History", tint = Color.Gray, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(recent, color = Color.LightGray)
                            }
                            IconButton(onClick = { recentSearches = recentSearches.filter { it != recent } }) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.Gray, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            } else {
                // Show search results
                if (isSearching) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                } else if (results.news.isEmpty() && results.stocks.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No results found", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Try searching for different keywords", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                } else {
                    // Filters
                    LazyRow(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                    val filters = listOf("All", "Business", "Sports", "Tech", "Entertainment", "Last 24hrs", "This Week")
                        items(filters) { f ->
                            val isSelected = filter == f
                            Box(
                                modifier = Modifier
                                    .background(if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(16.dp))
                                    .border(1.dp, if (isSelected) Color.White else Color.Gray, RoundedCornerShape(16.dp))
                                    .clickable { viewModel.setFilter(f) }
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text(f, color = if (isSelected) Color.Black else Color.White, fontSize = 14.sp)
                            }
                        }
                    }

                    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
                        val displayNews = if (filter == "All" || filter == "Last 24hrs" || filter == "This Week") {
                            results.news
                        } else {
                            results.news.filter { it.title?.contains(filter.take(4), ignoreCase = true) == true || it.description?.contains(filter.take(4), ignoreCase = true) == true }
                        }.ifEmpty { results.news } // fallback to all if empty

                        if (filter == "All" || filter == "Business") {
                            if (results.stocks.isNotEmpty()) {
                                item {
                                    Text("Market Data", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
                                }
                                items(results.stocks.toList()) { (symbol, item) ->
                                    MarketCard(title = symbol, data = item, showGraph = true)
                                }
                            }
                        }
                        
                        if (displayNews.isNotEmpty()) {
                            item {
                                Text("News Articles", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
                            }
                            items(displayNews) { article ->
                                GlassArticleCard(article = article, onClick = { onArticleClick(article) })
                            }
                        }
                    }
                }
            }
        }
    }
}
