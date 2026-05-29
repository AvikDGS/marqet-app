package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.Article
import com.example.model.YahooSparkItem
import com.example.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SearchResult(
    val news: List<Article> = emptyList(),
    val stocks: Map<String, YahooSparkItem> = emptyMap()
)

class SearchViewModel(private val allNews: List<Article>) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _results = MutableStateFlow(SearchResult())
    val results: StateFlow<SearchResult> = _results.asStateFlow()

    private val _filter = MutableStateFlow("All")
    val filter: StateFlow<String> = _filter.asStateFlow()
    
    private var searchJob: Job? = null

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        searchJob?.cancel()
        
        if (newQuery.isBlank()) {
            _isSearching.value = false
            _results.value = SearchResult()
            return
        }

        searchJob = viewModelScope.launch(Dispatchers.IO) {
            _isSearching.value = true
            delay(300) // debounce
            
            // 1. Search local news
            val filteredNews = allNews.filter {
                val titleMatch = it.title?.contains(newQuery, ignoreCase = true) == true
                val descMatch = it.description?.contains(newQuery, ignoreCase = true) == true
                titleMatch || descMatch
            }

            // 2. Search Yahoo Finance symbol
            var stockMap = emptyMap<String, YahooSparkItem>()
            try {
                // Not ideal, doing network directly in ViewModel for brevity
                val requestUrl = "https://query2.finance.yahoo.com/v1/finance/search?q=$newQuery"
                val response = okhttp3.OkHttpClient().newCall(
                    okhttp3.Request.Builder().url(requestUrl).build()
                ).execute()

                if (response.isSuccessful) {
                    val body = response.body?.string()
                    val quotesArray = org.json.JSONObject(body ?: "").optJSONArray("quotes")
                    val symbols = mutableListOf<String>()
                    if (quotesArray != null) {
                        for (i in 0 until Math.min(quotesArray.length(), 3)) {
                            val item = quotesArray.optJSONObject(i)
                            if (item != null) {
                                symbols.add(item.optString("symbol"))
                            }
                        }
                    }
                    if (symbols.isNotEmpty()) {
                        val joined = symbols.joinToString(",")
                        stockMap = RetrofitClient.yahooApi.getSpark(symbols = joined)
                    }
                }
            } catch (e: Exception) {
                // Ignore network failure for Yahoo
            }
            
            _results.value = SearchResult(news = filteredNews, stocks = stockMap)
            _isSearching.value = false
        }
    }

    fun setFilter(newFilter: String) {
        _filter.value = newFilter
    }
}
