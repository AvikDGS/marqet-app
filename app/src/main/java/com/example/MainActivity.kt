package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.viewmodel.MarketsViewModel
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.model.Article
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.NewsUiState
import com.example.viewmodel.NewsViewModel
import com.example.viewmodel.SummaryUiState

// Colors for Glassmorphism
val GlassBackground = Color.White.copy(alpha = 0.08f)
val GlassBorder = Color.White.copy(alpha = 0.15f)
fun showNotification(context: Context, title: String, body: String) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelId = "story_updates_channel"
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Story Updates",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
    }

    val notification = NotificationCompat.Builder(context, channelId)
        .setContentTitle(title)
        .setContentText(body)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(System.currentTimeMillis().toInt(), notification)
}

val AppBackgroundGradient = Brush.verticalGradient(listOf(Color(0xFF000000), Color(0xFF0D0D1A)))
val OnboardingBackgroundGradient = Brush.verticalGradient(listOf(Color(0xFF000000), Color(0xFF1A1A2E)))
val AccentGradient = Brush.linearGradient(listOf(Color(0xFFFFFFFF), Color(0xFFA0A0A0)))
val TextSecondaryColor = Color.White.copy(alpha = 0.7f)
val TextMutedColor = Color.White.copy(alpha = 0.4f)
val BottomNavBg = Color.Black.copy(alpha = 0.8f)

class MainActivity : ComponentActivity() {
    private val viewModel: NewsViewModel by viewModels()
    private val marketsViewModel: MarketsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContent {
                MyApplicationTheme {
                    NewsApp(viewModel, marketsViewModel)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            setContent {
                MyApplicationTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(16.dp)) {
                            Text("Fatal Error during initialization:\n${e.message}", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NewsApp(viewModel: NewsViewModel, marketsViewModel: MarketsViewModel) {
    val navController = rememberNavController()
    var selectedArticle by remember { mutableStateOf<Article?>(null) }
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (currentRoute == "home" || currentRoute == "markets") {
                NavigationBar(
                    containerColor = BottomNavBg,
                    tonalElevation = 0.dp
                ) {
                    val tabs = listOf(
                        Triple("Home", Icons.Default.Home, "home"),
                        Triple("Markets", Icons.Default.ShoppingCart, "markets"), // Using ShoppingCart as it's the current icon
                        Triple("Sports", Icons.Default.PlayArrow, "home"),
                        Triple("Saved", Icons.Default.Star, "home"),
                        Triple("Profile", Icons.Default.Person, "home")
                    )
                    
                    tabs.forEach { triple ->
                        val (label, icon, route) = triple
                        val isSelected = currentRoute == route && (label == "Home" || label == "Markets")
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { 
                                if (route != currentRoute) {
                                    navController.navigate(route) {
                                        popUpTo("home") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                indicatorColor = Color.Transparent,
                                unselectedIconColor = TextMutedColor,
                                unselectedTextColor = TextMutedColor
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        val currentContext = LocalContext.current
        val authViewModel = androidx.lifecycle.viewmodel.compose.viewModel<com.example.viewmodel.AuthViewModel>(
            factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    return com.example.viewmodel.AuthViewModel(currentContext.applicationContext as android.app.Application) as T
                }
            }
        )
        val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
        val startDest = if (currentUser != null) "home" else "welcome"
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(currentUser) {
            currentUser?.uid?.let { userId ->
                try {
                    val channel = supabase.channel("story-updates")
                    channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                        table = "story_updates"
                        filter = "user_id=eq.\$userId"
                    }.onEach { change ->
                        // New story update found - send local notification
                        showNotification(
                            context = currentContext,
                            title = "Update on your watched story",
                            body = change.record["headline"].toString()
                        )
                    }.launchIn(this)
                    
                    try {
                        supabase.realtime.connect()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    channel.subscribe()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        Box(modifier = Modifier.padding(paddingValues)) {
            NavHost(navController = navController, startDestination = startDest) {
                composable("welcome") {
                    WelcomeScreen(
                        onSignInClick = { navController.navigate("auth_signin") },
                        onSignUpClick = { navController.navigate("auth_signup") }
                    )
                }
                composable("auth_signin") {
                    AuthScreen(
                        viewModel = authViewModel,
                        isSignUp = false,
                        onBack = { navController.popBackStack() },
                        onSuccess = {
                            navController.navigate("home") {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onToggleMode = {
                            navController.navigate("auth_signup") {
                                popUpTo("welcome")
                            }
                        }
                    )
                }
                composable("auth_signup") {
                    AuthScreen(
                        viewModel = authViewModel,
                        isSignUp = true,
                        onBack = { navController.popBackStack() },
                        onSuccess = {
                            navController.navigate("category_selection") {
                                popUpTo("welcome") { inclusive = true }
                            }
                        },
                        onToggleMode = {
                            navController.navigate("auth_signin") {
                                popUpTo("welcome")
                            }
                        }
                    )
                }
                composable("category_selection") {
                    CategorySelectionScreen(
                        viewModel = authViewModel,
                        onContinue = {
                            navController.navigate("home") {
                                popUpTo("category_selection") { inclusive = true }
                            }
                        },
                        onSkip = {
                            navController.navigate("home") {
                                popUpTo("category_selection") { inclusive = true }
                            }
                        }
                    )
                }
                composable("onboarding") {
                    OnboardingFlow(onFinish = {
                        navController.navigate("welcome") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    })
                }
                composable("profile") {
                    ProfileScreen(
                        viewModel = authViewModel,
                        onBackClick = { navController.popBackStack() },
                        onSignOutComplete = {
                            navController.navigate("welcome") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
                composable("home") {
                    HomeScreen(
                        viewModel = viewModel,
                        onArticleClick = { article ->
                            selectedArticle = article
                            viewModel.clearSummary()
                            viewModel.summarizeArticle(article)
                            navController.navigate("detail")
                        },
                        onAllCategoriesClick = {
                            navController.navigate("categories")
                        },
                        onSearchClick = {
                            navController.navigate("search")
                        },
                        onProfileClick = {
                            navController.navigate("profile")
                        }
                    )
                }
                composable("categories") {
                    CategoriesScreen(
                        viewModel = viewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }
                composable("search") {
                    val allNews = if (viewModel.newsState.collectAsStateWithLifecycle().value is NewsUiState.Success) 
                        (viewModel.newsState.collectAsStateWithLifecycle().value as NewsUiState.Success).articles 
                    else 
                        emptyList()
                    val searchViewModel = androidx.lifecycle.viewmodel.compose.viewModel<com.example.viewmodel.SearchViewModel>(
                        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                return com.example.viewmodel.SearchViewModel(allNews) as T
                            }
                        }
                    )
                    SearchScreen(
                        viewModel = searchViewModel,
                        onBackClick = { navController.popBackStack() },
                        onArticleClick = { article ->
                            selectedArticle = article
                            viewModel.clearSummary()
                            viewModel.summarizeArticle(article)
                            navController.navigate("detail")
                        },
                        onVoiceSearchClick = {
                            val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            }
                            try {
                                // this is mock, normally we'd launch for result
                            } catch (e: Exception) {}
                        },
                        onSearch = { query ->
                            val user = authViewModel.currentUser.value
                            if (user != null) {
                                coroutineScope.launch {
                                    authViewModel.dbRepository.addSearchHistory(user.uid, query)
                                }
                            }
                        }
                    )
                }
                composable("markets") {
                    MarketsScreen(viewModel = marketsViewModel)
                }
                composable("detail") {
                    selectedArticle?.let { article ->
                        DetailScreen(
                            article = article,
                            viewModel = viewModel,
                            authViewModel = authViewModel,
                            onBackClick = { navController.popBackStack() },
                            onSuggestedArticleClick = { suggested ->
                                 selectedArticle = suggested
                                 viewModel.clearSummary()
                                 viewModel.summarizeArticle(suggested)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingFlow(onFinish: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    Box(modifier = Modifier.fillMaxSize().background(OnboardingBackgroundGradient)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(3f),
                contentAlignment = Alignment.Center
            ) {
                // Circular Image placeholder for onboarding
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .clip(CircleShape)
                        .background(GlassBackground)
                        .border(1.dp, GlassBorder, CircleShape)
                ) {
                    if (step == 0) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(80.dp).align(Alignment.Center))
                    } else if (step == 1) {
                        Icon(Icons.Default.Menu, contentDescription = null, tint = Color.White, modifier = Modifier.size(80.dp).align(Alignment.Center))
                    } else {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(80.dp).align(Alignment.Center))
                    }
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
            ) {
                val title = when(step) {
                    0 -> "Stay Ahead of Markets"
                    1 -> "Your News, Your Way"
                    else -> "Never Miss a Story"
                }
                
                Text(title, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Get real-time updates and deep AI summaries for the news that matters to you.",
                    color = TextSecondaryColor,
                    fontSize = 16.sp
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (step < 2) {
                        TextButton(onClick = onFinish) {
                            Text("Skip", color = TextSecondaryColor)
                        }
                        
                        Button(
                            onClick = { step++ },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Next", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Button(
                            onClick = onFinish,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Text("Get Started", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: NewsViewModel, onArticleClick: (Article) -> Unit, onAllCategoriesClick: () -> Unit, onSearchClick: () -> Unit, onProfileClick: () -> Unit) {
    val uiState by viewModel.newsState.collectAsStateWithLifecycle()
    val newStoriesAvailable by viewModel.newStoriesAvailable.collectAsStateWithLifecycle()
    val selectedCategoryIds by viewModel.settingsRepository.selectedCategoryIds.collectAsStateWithLifecycle()
    val currentCategoryId by viewModel.settingsRepository.currentCategoryId.collectAsStateWithLifecycle()

    val categories = com.example.model.CategoryData.allCategories.sortedWith(
        compareByDescending<com.example.model.NewsCategory> { selectedCategoryIds.contains(it.id) }
            .thenBy { it.name }
    )

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val isRefreshing = uiState is NewsUiState.Success && (uiState as NewsUiState.Success).isRefreshing
    val pullToRefreshState = androidx.compose.material3.pulltorefresh.rememberPullToRefreshState()
    var wasRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(isRefreshing) {
        if (wasRefreshing && !isRefreshing) {
            Toast.makeText(context, "Feed updated • just now", Toast.LENGTH_SHORT).show()
        }
        wasRefreshing = isRefreshing
    }

    Scaffold(
        containerColor = Color.Transparent
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
                    containerColor = Color(0xFF1A1A2E), // dark background
                    color = Color.White // white circular loading spinner
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
                            Text("New stories available ↑ Tap to load", color = Color.White, fontSize = 14.sp)
                        }
                    }
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
                                    text = "Marqet", 
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
                        text = com.example.utils.DateTimeUtils.getGreeting(),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Sticky search bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 12.dp)
                        .background(GlassBackground, RoundedCornerShape(50))
                        .border(1.dp, GlassBorder, RoundedCornerShape(50))
                        .clickable { onSearchClick() }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
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
                
                // Categories
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = category.id == currentCategoryId
                        Box(
                            modifier = Modifier
                                .border(1.dp, if (isSelected) Color.White else GlassBorder, RoundedCornerShape(50))
                                .background(if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(50))
                                .clickable {
                                    viewModel.settingsRepository.setCurrentCategory(category.id)
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
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
                                Button(onClick = { viewModel.fetchNews() }, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)) {
                                    Text("Retry")
                                }
                            }
                        }
                        is NewsUiState.Success -> {
                            val articles = state.articles
                            if (articles.isNotEmpty()) {
                                val now = System.currentTimeMillis()
                                val last24h = mutableListOf<com.example.model.Article>()
                                val earlierStories = mutableListOf<com.example.model.Article>()
                                val fromThisWeek = mutableListOf<com.example.model.Article>()
                                
                                articles.forEach { art ->
                                    val time = com.example.utils.DateTimeUtils.parseIsoDate(art.publishedAt)?.time ?: 0L
                                    val diffHours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(now - time)
                                    when {
                                        diffHours < 24 -> last24h.add(art)
                                        diffHours < 24 * 7 -> earlierStories.add(art)
                                        else -> fromThisWeek.add(art)
                                    }
                                }

                                // If last24h is oddly empty but we have articles, just fallback to making the first one featured anyway.
                                val feedArticles = if (last24h.isNotEmpty()) last24h else articles.take(1)
                                val fallbackUsed = last24h.isEmpty()
                                
                                Column(modifier = Modifier.fillMaxSize()) {
                                    if (state.isOffline) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().background(Color(0xFFE53935)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("Offline Mode. Showing cached news.", color = Color.White, modifier = Modifier.padding(8.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    LazyColumn(
                                    contentPadding = PaddingValues(bottom = 24.dp),
                                    modifier = Modifier.fillMaxSize()
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
                                                "Earlier stories",
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
                                                "From this week",
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
                                    Text("No news available", color = Color.White)
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

@Composable
fun FeaturedArticleCard(article: Article, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .background(Brush.radialGradient(listOf(Color.White.copy(alpha = 0.15f), Color.Transparent)))
        )

        Column {
            // Large circular image at top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = 40.dp)
                    .zIndex(1f),
                contentAlignment = Alignment.Center
            ) {
                if (!article.urlToImage.isNullOrBlank()) {
                    AsyncImage(
                        model = article.urlToImage,
                        contentDescription = "Featured Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .border(2.dp, GlassBorder, CircleShape)
                    )
                } else {
                    Box(modifier = Modifier.size(160.dp).clip(CircleShape).background(GlassBackground))
                }
            }
            
            // Glass card below, slightly overlapping
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GlassBackground, RoundedCornerShape(24.dp))
                    .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
            ) {
                // shimmer simulation overlay
                Box(modifier = Modifier.matchParentSize().background(Brush.linearGradient(listOf(Color.White.copy(0.05f), Color.Transparent)), RoundedCornerShape(24.dp)))
                
                // subtle grey-to-transparent bottom gradient
                Box(modifier = Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.White.copy(alpha = 0.08f))), RoundedCornerShape(24.dp)))
                
                Column(modifier = Modifier.padding(top = 64.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)) {
                    val publishTime = com.example.utils.DateTimeUtils.parseIsoDate(article.publishedAt)?.time ?: 0L
                    val diffInMillis = System.currentTimeMillis() - publishTime
                    val isNew = diffInMillis in 0..(60 * 60 * 1000L)
                    val isBreaking = article.title?.contains("BREAKING", true) == true || (article.title?.hashCode()?.rem(100) ?: 0) > 90

                    if (isBreaking) {
                        Text(
                            text = "BREAKING", 
                            color = Color(0xFFFF4B4B), 
                            fontSize = 11.sp, 
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp).border(1.dp, Color(0xFFFF4B4B).copy(alpha=0.5f), RoundedCornerShape(4.dp)).padding(horizontal=6.dp, vertical=2.dp)
                        )
                    } else if (isNew) {
                        Text(
                            text = "NEW", 
                            color = Color(0xFF4CAF50), 
                            fontSize = 11.sp, 
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp).border(1.dp, Color(0xFF4CAF50).copy(alpha=0.5f), RoundedCornerShape(4.dp)).padding(horizontal=6.dp, vertical=2.dp)
                        )
                    } else {
                        Text(
                            text = "FEATURED",
                            color = Color.White.copy(alpha=0.5f),
                            fontSize = 11.sp,
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    Text(
                        text = article.title ?: "No Title",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = article.source?.name?.uppercase() ?: "BUSINESS",
                                    color = TextSecondaryColor,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = " • ",
                                    color = TextSecondaryColor,
                                    fontSize = 14.sp
                                )
                                Icon(Icons.Default.Schedule, contentDescription=null, tint=TextSecondaryColor, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = com.example.utils.DateTimeUtils.getRelativeTime(article.publishedAt),
                                    color = TextSecondaryColor,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Read More", tint = Color.Black, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GlassArticleCard(article: Article, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(GlassBackground, RoundedCornerShape(24.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
            .clickable { onClick() }
    ) {
        // subtle grey-to-transparent bottom gradient
        Box(modifier = Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.White.copy(alpha = 0.08f))), RoundedCornerShape(24.dp)))
        
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (!article.urlToImage.isNullOrBlank()) {
                AsyncImage(
                    model = article.urlToImage,
                    contentDescription = "Thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(1.dp, GlassBorder, CircleShape)
                )
            } else {
                Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(GlassBackground))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                val publishTime = com.example.utils.DateTimeUtils.parseIsoDate(article.publishedAt)?.time ?: 0L
                val diffInMillis = System.currentTimeMillis() - publishTime
                val isNew = diffInMillis in 0..(60 * 60 * 1000L)
                val isBreaking = article.title?.contains("BREAKING", true) == true || (article.title?.hashCode()?.rem(100) ?: 0) > 90

                if (isBreaking) {
                    Text(
                        text = "BREAKING", 
                        color = Color(0xFFFF4B4B), 
                        fontSize = 10.sp, 
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp).border(1.dp, Color(0xFFFF4B4B).copy(alpha=0.5f), RoundedCornerShape(4.dp)).padding(horizontal=4.dp, vertical=2.dp)
                    )
                } else if (isNew) {
                    Text(
                        text = "NEW", 
                        color = Color(0xFF4CAF50), 
                        fontSize = 10.sp, 
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp).border(1.dp, Color(0xFF4CAF50).copy(alpha=0.5f), RoundedCornerShape(4.dp)).padding(horizontal=4.dp, vertical=2.dp)
                    )
                }
                
                Text(
                    text = article.title ?: "No Title",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = article.source?.name?.uppercase() ?: "NEWS",
                            color = TextSecondaryColor,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = " • ",
                            color = TextSecondaryColor,
                            fontSize = 11.sp
                        )
                        Icon(Icons.Default.Schedule, contentDescription=null, tint=TextSecondaryColor, modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = com.example.utils.DateTimeUtils.getRelativeTime(article.publishedAt),
                            color = TextSecondaryColor,
                            fontSize = 11.sp
                        )
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Read More", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun DetailScreen(
    article: Article,
    viewModel: NewsViewModel,
    authViewModel: com.example.viewmodel.AuthViewModel,
    onBackClick: () -> Unit,
    onSuggestedArticleClick: (Article) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val summaryState by viewModel.summaryState.collectAsStateWithLifecycle()
    val allArticles = if (viewModel.newsState.collectAsStateWithLifecycle().value is NewsUiState.Success) 
        (viewModel.newsState.collectAsStateWithLifecycle().value as NewsUiState.Success).articles 
    else emptyList()

    val context = LocalContext.current
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    LaunchedEffect(article.url) {
        val user = authViewModel.currentUser.value
        if (user != null) {
            authViewModel.dbRepository.addReadingHistory(user.uid, article)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(AppBackgroundGradient)) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 80.dp) // space for bottom actions
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                        .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                ) {
                    if (!article.urlToImage.isNullOrBlank()) {
                        AsyncImage(
                            model = article.urlToImage,
                            contentDescription = "News Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha=0.9f)))))
                    } else {
                        Box(modifier = Modifier.fillMaxSize().background(GlassBackground))
                    }
                    
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(24.dp)
                    ) {
                        // Category Chip
                        Box(
                            modifier = Modifier
                                .background(GlassBackground, RoundedCornerShape(16.dp))
                                .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("BUSINESS • TECHNOLOGY", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = article.title ?: "No Title",
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 32.sp
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Column {
                            Text(
                                text = com.example.utils.DateTimeUtils.getFullDateTime(article.publishedAt),
                                color = TextSecondaryColor,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(24.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
                                    Text((article.source?.name?.take(1) ?: "N").uppercase(), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = article.source?.name ?: "Unknown Source",
                                    color = Color.White.copy(alpha=0.9f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(" • ", color = TextSecondaryColor, fontSize = 14.sp)
                                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription=null, tint=TextSecondaryColor, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = com.example.utils.DateTimeUtils.getReadingTime(article.content ?: article.description),
                                    color = TextSecondaryColor,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // Top actions (Back, Save, Share)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.background(BottomNavBg, CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    
                    Row {
                        IconButton(
                            onClick = { 
                                val user = authViewModel.currentUser.value
                                if (user != null) {
                                    coroutineScope.launch {
                                        authViewModel.dbRepository.saveArticle(user.uid, article)
                                        Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show() 
                                    }
                                } else {
                                    Toast.makeText(context, "Please login to save", Toast.LENGTH_SHORT).show() 
                                }
                            },
                            modifier = Modifier.background(BottomNavBg, CircleShape)
                        ) {
                            Icon(Icons.Default.BookmarkBorder, contentDescription = "Save", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { /* Share */ },
                            modifier = Modifier.background(BottomNavBg, CircleShape)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                // AI Summary Label
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = Color(0xFF6B4EE6), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Summary", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Summary Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GlassBackground, RoundedCornerShape(24.dp))
                        .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    when (val state = summaryState) {
                        is SummaryUiState.Idle, is SummaryUiState.Loading -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Generating AI Analysis...", color = Color.White)
                            }
                        }
                        is SummaryUiState.Error -> {
                            Text(state.message, color = MaterialTheme.colorScheme.error)
                        }
                        is SummaryUiState.Success -> {
                            MarkdownRenderer(text = state.summary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                // Read full article
                Button(
                    onClick = {
                        if (!article.url.isNullOrBlank()) {
                            try {
                                uriHandler.openUri(article.url)
                            } catch (e: Exception) {}
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GlassBackground),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                ) {
                    Text("Read full article", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.OpenInBrowser, contentDescription = "Open", tint = Color.White, modifier = Modifier.size(18.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Follow story button
                Button(
                    onClick = {
                        val user = authViewModel.currentUser.value
                        if (user != null) {
                            coroutineScope.launch {
                                authViewModel.dbRepository.watchStory(
                                    userId = user.uid,
                                    headline = article.title ?: "Unknown",
                                    url = article.url,
                                    keywords = emptyList(), // Can add actual keyword extraction later
                                    entities = emptyList() // Can add actual entity extraction later
                                )
                                Toast.makeText(context, "We'll notify you of updates to this story", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Please login to follow stories", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B4EE6)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Follow this story 🔔", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Related Stories
                val shuffled = allArticles.shuffled()
                val related = shuffled.take(3)
                val moreFromSource = shuffled.drop(3).take(3)
                val suggestions = shuffled.drop(6).take(10)

                if (related.isNotEmpty()) {
                    Text("Related Stories", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(related) { rel ->
                            Box(modifier = Modifier.width(280.dp)) {
                                GlassArticleCard(article = rel, onClick = { onSuggestedArticleClick(rel) })
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                if (moreFromSource.isNotEmpty()) {
                    Text("More from ${article.source?.name ?: "this source"}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(moreFromSource) { rel ->
                            Box(modifier = Modifier.width(280.dp)) {
                                GlassArticleCard(article = rel, onClick = { onSuggestedArticleClick(rel) })
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                if (suggestions.isNotEmpty()) {
                    Text("You might also like", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    suggestions.forEach { sugg ->
                        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).clickable { onSuggestedArticleClick(sugg) }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (!sugg.urlToImage.isNullOrBlank()) {
                                    AsyncImage(
                                        model = sugg.urlToImage,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp))
                                    )
                                } else {
                                    Box(modifier = Modifier.size(80.dp).background(GlassBackground, RoundedCornerShape(12.dp)))
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = sugg.title ?: "",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(sugg.source?.name ?: "", color = TextSecondaryColor, fontSize = 12.sp)
                                        Text(" • ", color = TextSecondaryColor, fontSize = 12.sp)
                                        Text(com.example.utils.DateTimeUtils.getRelativeTime(sugg.publishedAt), color = TextSecondaryColor, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Bottom Actions Reaction Bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color(0xFF0F0F1A).copy(alpha=0.9f))
                .border(1.dp, GlassBorder, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(vertical = 12.dp, horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ReactionItem(icon = Icons.Default.ThumbUp, label = "Helpful", count = "2.4k")
                ReactionItem(icon = Icons.Default.BookmarkBorder, label = "Save", count = "")
                ReactionItem(icon = Icons.Default.Share, label = "Share", count = "")
                ReactionItem(icon = Icons.Default.ChatBubbleOutline, label = "483", count = "")
            }
        }
    }
}

@Composable
fun ReactionItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, count: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(20.dp))
        if (label.isNotEmpty() || count.isNotEmpty()) {
            Spacer(modifier = Modifier.width(6.dp))
            val text = if (count.isNotEmpty() && label != count) "$label • $count" else if (count.isNotEmpty()) count else label
            Text(text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun MarkdownRenderer(text: String) {
    val lines = text.split("\n")
    Column(modifier = Modifier.fillMaxWidth()) {
        for (line in lines) {
            when {
                line.startsWith("# ") -> {
                    Text(
                        text = parseInlineMarkdown(line.substring(2)),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                line.startsWith("## ") -> {
                    Text(
                        text = parseInlineMarkdown(line.substring(3)),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                    )
                }
                line.startsWith("### ") -> {
                    Text(
                        text = parseInlineMarkdown(line.substring(4)),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                line.startsWith("- ") || line.startsWith("* ") -> {
                    Row(modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)) {
                        Text("•", modifier = Modifier.padding(end = 8.dp), color = TextSecondaryColor)
                        Text(
                            text = parseInlineMarkdown(line.substring(2)),
                            color = TextSecondaryColor,
                            fontSize = 16.sp
                        )
                    }
                }
                line.startsWith("> ") -> {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth()
                            .background(GlassBackground, RoundedCornerShape(4.dp))
                            .border(1.dp, GlassBorder, RoundedCornerShape(4.dp))
                    ) {
                        Text(
                            text = parseInlineMarkdown(line.substring(2)),
                            color = Color.White,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                line.isNotBlank() -> {
                    Text(
                        text = parseInlineMarkdown(line),
                        color = TextSecondaryColor,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }
    }
}

fun parseInlineMarkdown(text: String): androidx.compose.ui.text.AnnotatedString {
    val builder = androidx.compose.ui.text.AnnotatedString.Builder()
    var currentIndex = 0
    val regex = Regex("\\*\\*(.*?)\\*\\*|\\*(.*?)\\*")
    val matches = regex.findAll(text)

    for (match in matches) {
        builder.append(text.substring(currentIndex, match.range.first))
        if (match.groups[1] != null) {
            builder.pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold))
            builder.append(match.groups[1]!!.value)
            builder.pop()
        } else if (match.groups[2] != null) {
            builder.pushStyle(androidx.compose.ui.text.SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic))
            builder.append(match.groups[2]!!.value)
            builder.pop()
        }
        currentIndex = match.range.last + 1
    }
    builder.append(text.substring(currentIndex))
    return builder.toAnnotatedString()
}
