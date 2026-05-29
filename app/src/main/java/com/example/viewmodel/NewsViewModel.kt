package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.model.Article
import com.example.model.GeminiContent
import com.example.model.GeminiGenerateContentRequest
import com.example.model.GeminiPart
import com.example.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.data.SettingsRepository
import kotlinx.coroutines.flow.collectLatest
import com.example.utils.DateTimeUtils
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.net.URL
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader

import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.CachedArticle

sealed class NewsUiState {
    object Loading : NewsUiState()
    data class Success(val articles: List<Article>, val isRefreshing: Boolean = false, val isOffline: Boolean = false) : NewsUiState()
    data class Error(val message: String) : NewsUiState()
}

sealed class SummaryUiState {
    object Idle : SummaryUiState()
    object Loading : SummaryUiState()
    data class Success(val summary: String) : SummaryUiState()
    data class Error(val message: String) : SummaryUiState()
}

class NewsViewModel(application: Application) : AndroidViewModel(application) {
    val settingsRepository = SettingsRepository(application)
    private val database = Room.databaseBuilder(application, AppDatabase::class.java, "news_database").build()

    private val _newsState = MutableStateFlow<NewsUiState>(NewsUiState.Loading)
    val newsState: StateFlow<NewsUiState> = _newsState.asStateFlow()

    private val _summaryState = MutableStateFlow<SummaryUiState>(SummaryUiState.Idle)
    val summaryState: StateFlow<SummaryUiState> = _summaryState.asStateFlow()

    private val _newStoriesAvailable = MutableStateFlow(false)
    val newStoriesAvailable: StateFlow<Boolean> = _newStoriesAvailable.asStateFlow()

    private var pendingArticles: List<Article> = emptyList()
    private var allArticles: MutableList<Article> = mutableListOf()

    init {
        viewModelScope.launch {
            settingsRepository.currentCategoryId.collectLatest { categoryId ->
                fetchNews()
            }
        }
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(10_000) // 10s auto-refresh for fast APIs
                fetchFastApis(isSilent = true)
            }
        }
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(60_000) // 60s auto-refresh for RSS
                fetchRss(isSilent = true)
            }
        }
    }

    private var fetchJob: kotlinx.coroutines.Job? = null

    fun loadPendingStories() {
        if (pendingArticles.isNotEmpty()) {
            _newsState.value = NewsUiState.Success(pendingArticles, isRefreshing = false)
            pendingArticles = emptyList()
            _newStoriesAvailable.value = false
        }
    }

    private suspend fun fetchRssFeed(url: String): List<Article> {
        return withContext(Dispatchers.IO) {
            try {
                val feed = SyndFeedInput().build(XmlReader(URL(url)))
                feed.entries.map { entry ->
                    Article(
                        source = com.example.model.Source(name = feed.title ?: "RSS"),
                        title = entry.title,
                        description = entry.description?.value ?: "",
                        url = entry.link,
                        publishedAt = DateTimeUtils.formatIsoDate(entry.publishedDate ?: java.util.Date()),
                        urlToImage = entry.enclosures?.firstOrNull()?.url ?: ""
                    )
                }
            } catch(e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    private fun deduplicateAndSort(articles: List<Article>): List<Article> {
        val unique = mutableListOf<Article>()
        for (article in articles) {
            val isDuplicate = unique.any { 
                val t1 = it.title?.lowercase() ?: ""
                val t2 = article.title?.lowercase() ?: ""
                t1.isNotBlank() && t2.isNotBlank() && (t1 == t2 || t1.contains(t2) || t2.contains(t1))
            }
            if (!isDuplicate) {
                unique.add(article)
            }
        }
        
        return unique.sortedWith(Comparator { a1, a2 ->
            val isBreaking1 = a1.title?.contains("BREAKING", true) == true || (a1.title?.hashCode()?.rem(100) ?: 0) > 90
            val isBreaking2 = a2.title?.contains("BREAKING", true) == true || (a2.title?.hashCode()?.rem(100) ?: 0) > 90
            
            if (isBreaking1 != isBreaking2) {
                isBreaking2.compareTo(isBreaking1)
            } else {
                val date1 = DateTimeUtils.parseIsoDate(a1.publishedAt)?.time ?: 0L
                val date2 = DateTimeUtils.parseIsoDate(a2.publishedAt)?.time ?: 0L
                if (date1 != date2) {
                    date2.compareTo(date1)
                } else {
                    val score1 = a1.title?.hashCode()?.rem(100) ?: 0
                    val score2 = a2.title?.hashCode()?.rem(100) ?: 0
                    score2.compareTo(score1)
                }
            }
        })
    }

    fun fetchNews(isRefresh: Boolean = false, isSilent: Boolean = false) {
        if (fetchJob?.isActive == true) return

        fetchJob = viewModelScope.launch {
            val currentState = _newsState.value
            if (isRefresh && currentState is NewsUiState.Success) {
                if (!isSilent) _newsState.value = currentState.copy(isRefreshing = true)
            } else {
                if (!isSilent) _newsState.value = NewsUiState.Loading
            }

            try {
                val newsApiFuture = async(Dispatchers.IO) {
                    try {
                        val key = try { BuildConfig.NEWS_API_KEY } catch (e: Exception) {""}
                        if (key.isNotBlank()) RetrofitClient.newsApi.getNewsByCategory("business", key, "us").articles
                        else emptyList()
                    } catch(e: Exception) { emptyList() }
                }

                val gnewsFuture = async(Dispatchers.IO) {
                    try {
                        val key = try { BuildConfig.GNEWS_API_KEY } catch (e:Exception) {""}
                        if (key.isNotBlank()) {
                            RetrofitClient.gnewsApi.getNewsByCategory("business", "en", key).articles.map {
                                Article(
                                    source = it.source,
                                    title = it.title,
                                    description = it.description,
                                    url = it.url,
                                    urlToImage = it.image,
                                    publishedAt = it.publishedAt,
                                    content = it.content
                                )
                            }
                        } else emptyList()
                    } catch(e: Exception) { emptyList() }
                }

                val mediastackFuture = async(Dispatchers.IO) {
                    try {
                        val key = try { BuildConfig.MEDIASTACK_API_KEY } catch (e:Exception) {""}
                        if (key.isNotBlank()) {
                            RetrofitClient.mediastackApi.getNewsByCategory("business", "en", key).data.map {
                                Article(
                                    source = com.example.model.Source(name = it.source),
                                    title = it.title,
                                    description = it.description,
                                    url = it.url,
                                    urlToImage = it.image,
                                    publishedAt = it.published_at
                                )
                            }
                        } else emptyList()
                    } catch(e: Exception) { emptyList() }
                }

                val rssUrls = listOf(
                    "https://feeds.reuters.com/reuters/businessNews",
                    "http://feeds.bbci.co.uk/news/business/rss.xml",
                    "https://www.moneycontrol.com/rss/business.xml",
                    "http://feeds.bbci.co.uk/news/world/rss.xml",
                    "https://www.espn.com/espn/rss/news",
                    "https://economictimes.indiatimes.com/rssfeedstopstories.cms",
                    "https://feeds.feedburner.com/ndtvnews-top-stories",
                    "https://timesofindia.indiatimes.com/rssfeedstopstories.cms",
                    "https://www.cricbuzz.com/cricket-rss-feeds"
                )
                
                val rssFutures = rssUrls.map { url ->
                    async(Dispatchers.IO) { fetchRssFeed(url) }
                }

                val combined = mutableListOf<Article>()
                combined.addAll(newsApiFuture.await())
                combined.addAll(gnewsFuture.await())
                combined.addAll(mediastackFuture.await())
                rssFutures.awaitAll().forEach { combined.addAll(it) }

                val validArticles = combined.filter { !it.title.isNullOrBlank() }
                
                if (validArticles.isEmpty()) {
                     if (!isSilent) _newsState.value = NewsUiState.Error("No news articles found.")
                } else {
                     allArticles.addAll(validArticles)
                     val newSorted = deduplicateAndSort(allArticles).take(500)
                     allArticles = newSorted.toMutableList()

                     // Cache to Room
                     withContext(Dispatchers.IO) {
                         database.articleDao().clearAll()
                         database.articleDao().insertArticles(newSorted.map {
                             CachedArticle(
                                 url = it.url ?: "",
                                 sourceName = it.source?.name,
                                 author = it.author,
                                 title = it.title,
                                 description = it.description,
                                 urlToImage = it.urlToImage,
                                 publishedAt = it.publishedAt,
                                 content = it.content
                             )
                         })
                     }

                     if (isSilent) {
                         pendingArticles = newSorted
                         _newStoriesAvailable.value = true
                     } else {
                         _newsState.value = NewsUiState.Success(newSorted, isRefreshing = false)
                         _newStoriesAvailable.value = false
                         pendingArticles = emptyList()
                     }
                }
            } catch (e: Exception) {
                // Fallback to cache on error
                val cached = withContext(Dispatchers.IO) {
                    database.articleDao().getArticles()
                }
                
                if (cached.isNotEmpty()) {
                    val restored = cached.map { 
                        Article(
                            source = com.example.model.Source(name = it.sourceName),
                            author = it.author,
                            title = it.title,
                            description = it.description,
                            url = it.url,
                            urlToImage = it.urlToImage,
                            publishedAt = it.publishedAt,
                            content = it.content
                        )
                    }
                    _newsState.value = NewsUiState.Success(restored, isRefreshing = false, isOffline = true)
                } else if (!isSilent) {
                    if (currentState is NewsUiState.Success && isRefresh) {
                        _newsState.value = currentState.copy(isRefreshing = false)
                    } else {
                        _newsState.value = NewsUiState.Error("Offline and no cached news available.")
                    }
                }
            }
        }
    }

    fun fetchFastApis(isSilent: Boolean = true) {
        viewModelScope.launch {
            try {
                val newsApiFuture = async(Dispatchers.IO) {
                    try {
                        val key = try { BuildConfig.NEWS_API_KEY } catch (e: Exception) {""}
                        if (key.isNotBlank()) RetrofitClient.newsApi.getNewsByCategory("business", key, "us").articles
                        else emptyList()
                    } catch(e: Exception) { emptyList() }
                }

                val gnewsFuture = async(Dispatchers.IO) {
                    try {
                        val key = try { BuildConfig.GNEWS_API_KEY } catch (e:Exception) {""}
                        if (key.isNotBlank()) RetrofitClient.gnewsApi.getNewsByCategory("business", "en", key).articles.map {
                            Article(source = it.source, title = it.title, description = it.description, url = it.url, urlToImage = it.image, publishedAt = it.publishedAt, content = it.content)
                        } else emptyList()
                    } catch(e: Exception) { emptyList() }
                }

                val mediastackFuture = async(Dispatchers.IO) {
                    try {
                        val key = try { BuildConfig.MEDIASTACK_API_KEY } catch (e:Exception) {""}
                        if (key.isNotBlank()) RetrofitClient.mediastackApi.getNewsByCategory("business", "en", key).data.map {
                            Article(source = com.example.model.Source(name=it.source), title = it.title, description = it.description, url = it.url, urlToImage = it.image, publishedAt = it.published_at)
                        } else emptyList()
                    } catch(e: Exception) { emptyList() }
                }

                val combined = mutableListOf<Article>()
                combined.addAll(newsApiFuture.await())
                combined.addAll(gnewsFuture.await())
                combined.addAll(mediastackFuture.await())

                val validArticles = combined.filter { !it.title.isNullOrBlank() }
                if (validArticles.isNotEmpty()) {
                    allArticles.addAll(validArticles)
                    val newSorted = deduplicateAndSort(allArticles).take(500)
                    allArticles = newSorted.toMutableList()

                    if (isSilent) {
                        pendingArticles = newSorted
                        _newStoriesAvailable.value = true
                    } else {
                        _newsState.value = NewsUiState.Success(newSorted, isRefreshing = false)
                    }
                }
            } catch(e: Exception) { }
        }
    }

    fun fetchRss(isSilent: Boolean = true) {
        viewModelScope.launch {
            try {
                val rssUrls = listOf(
                    "https://feeds.reuters.com/reuters/businessNews",
                    "http://feeds.bbci.co.uk/news/business/rss.xml",
                    "https://www.moneycontrol.com/rss/business.xml",
                    "http://feeds.bbci.co.uk/news/world/rss.xml",
                    "https://www.espn.com/espn/rss/news",
                    "https://economictimes.indiatimes.com/rssfeedstopstories.cms",
                    "https://feeds.feedburner.com/ndtvnews-top-stories",
                    "https://timesofindia.indiatimes.com/rssfeedstopstories.cms",
                    "https://www.cricbuzz.com/cricket-rss-feeds"
                )
                
                val rssFutures = rssUrls.map { url ->
                    async(Dispatchers.IO) { fetchRssFeed(url) }
                }
                
                val combined = mutableListOf<Article>()
                rssFutures.awaitAll().forEach { combined.addAll(it) }

                val validArticles = combined.filter { !it.title.isNullOrBlank() }
                if (validArticles.isNotEmpty()) {
                    allArticles.addAll(validArticles)
                    val newSorted = deduplicateAndSort(allArticles).take(500)
                    allArticles = newSorted.toMutableList()

                    if (isSilent) {
                        pendingArticles = newSorted
                        _newStoriesAvailable.value = true
                    } else {
                        _newsState.value = NewsUiState.Success(newSorted, isRefreshing = false)
                    }
                }
            } catch(e: Exception) {}
        }
    }

    fun clearSummary() {
        _summaryState.value = SummaryUiState.Idle
    }

    fun summarizeArticle(article: com.example.model.Article?) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY.trim() } catch(e:Exception) {""}
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            _summaryState.value = SummaryUiState.Error(
                "Gemini API Key is missing!\n" +
                "To see AI summarization:\n" +
                "1. Open the 'Secrets' panel in AI Studio.\n" +
                "2. Add a new secret named GEMINI_API_KEY.\n" +
                "3. Paste your valid Gemini API key as the value."
            )
            return
        }

        if (article == null) {
            _summaryState.value = SummaryUiState.Error("No content available to summarize.")
            return
        }

        val title = article.title ?: "Unknown Title"
        val description = article.description ?: ""
        val content = article.content ?: ""

        if (title.isBlank() && description.isBlank() && content.isBlank()) {
            _summaryState.value = SummaryUiState.Error("No content available to summarize.")
            return
        }

        viewModelScope.launch {
            _summaryState.value = SummaryUiState.Loading
            try {
                val prompt = """
                    You are an expert news summarizer AI.
                    Please provide a highly-structured summary of the following news story based on the available information.
                    Since you might only have snippets, use your knowledge to provide context if needed, but focus on the core information provided.

                    News Title: $title
                    Description: $description
                    Content Snippet: $content
                    
                    You MUST format your response strictly using markdown with the following structure:
                    1. A proper main headline starting with `# `
                    2. A sub-headline or brief context starting with `## ` or `### `
                    3. Key takeaways in a bulleted list starting with `- ` or `* `
                    4. Use **bold** text for important names, entities, or key facts.
                    5. Use *italics* for emphasis, source names, or publication names.
                    6. Include at least one relevant quote formatted as a blockquote starting with `> ` (synthesize a realistic quote based on the content if a direct quote isn't available, but mark it as a summary quote).
                """.trimIndent()
                val request = GeminiGenerateContentRequest(
                    contents = listOf(
                        GeminiContent(parts = listOf(GeminiPart(text = prompt)))
                    )
                )

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.geminiApi.generateContent(apiKey, request)
                }

                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "Could not generate summary."

                _summaryState.value = SummaryUiState.Success(text)
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string() ?: e.message()
                _summaryState.value = SummaryUiState.Error("API Error: $errorBody")
            } catch (e: Throwable) {
                _summaryState.value = SummaryUiState.Error(e.message ?: "Failed to generate summary")
            }
        }
    }
}
