package com.example.repository

import android.util.Log
import com.example.model.Article
import com.example.supabase
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SavedArticle(
    val id: Int? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("article_url") val articleUrl: String,
    val headline: String,
    val summary: String? = null,
    val source: String? = null,
    val category: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("published_at") val publishedAt: String? = null,
    @SerialName("saved_at") val savedAt: String? = null
)

@Serializable
data class WatchedStory(
    val id: Int? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("original_headline") val originalHeadline: String,
    @SerialName("original_url") val originalUrl: String? = null,
    val keywords: List<String> = emptyList(),
    val entities: List<String> = emptyList(),
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("update_count") val updateCount: Int = 0,
    @SerialName("last_updated") val lastUpdated: String? = null,
    @SerialName("saved_at") val savedAt: String? = null
)

@Serializable
data class ReadingHistory(
    val id: Int? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("article_url") val articleUrl: String,
    val headline: String,
    val category: String? = null,
    @SerialName("read_at") val readAt: String? = null
)

@Serializable
data class SearchHistory(
    val id: Int? = null,
    @SerialName("user_id") val userId: String,
    val query: String,
    @SerialName("searched_at") val searchedAt: String? = null
)

class DatabaseRepository {

    suspend fun saveArticle(userId: String, article: Article) = withContext(Dispatchers.IO) {
        try {
            val savedArticle = SavedArticle(
                userId = userId,
                articleUrl = article.url ?: "",
                headline = article.title ?: "Unknown",
                summary = article.description,
                source = article.source?.name,
                category = null,
                imageUrl = article.urlToImage,
                publishedAt = article.publishedAt
            )
            supabase.postgrest["saved_articles"].insert(savedArticle)
        } catch (e: Exception) {
            Log.e("DatabaseRepository", "Error saving article", e)
        }
    }

    suspend fun getSavedArticles(userId: String): List<SavedArticle> = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["saved_articles"]
                .select(Columns.ALL) {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<SavedArticle>()
        } catch (e: Exception) {
            Log.e("DatabaseRepository", "Error getting saved articles", e)
            emptyList()
        }
    }

    suspend fun watchStory(userId: String, headline: String, url: String? = null, keywords: List<String>, entities: List<String>) = withContext(Dispatchers.IO) {
        try {
            val watchedStory = WatchedStory(
                userId = userId,
                originalHeadline = headline,
                originalUrl = url,
                keywords = keywords,
                entities = entities
            )
            supabase.postgrest["watched_stories"].insert(watchedStory)
        } catch (e: Exception) {
            Log.e("DatabaseRepository", "Error watching story", e)
        }
    }
    
    suspend fun getWatchedStories(userId: String): List<WatchedStory> = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["watched_stories"]
                .select(Columns.ALL) {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<WatchedStory>()
        } catch (e: Exception) {
            Log.e("DatabaseRepository", "Error getting watched stories", e)
            emptyList()
        }
    }

    suspend fun addReadingHistory(userId: String, article: Article) = withContext(Dispatchers.IO) {
        try {
            val history = ReadingHistory(
                userId = userId,
                articleUrl = article.url ?: "",
                headline = article.title ?: "Unknown",
                category = null
            )
            supabase.postgrest["reading_history"].insert(history)
        } catch (e: Exception) {
            Log.e("DatabaseRepository", "Error adding reading history", e)
        }
    }

    suspend fun getReadingHistory(userId: String): List<ReadingHistory> = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["reading_history"]
                .select(Columns.ALL) {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<ReadingHistory>()
        } catch (e: Exception) {
            Log.e("DatabaseRepository", "Error getting reading history", e)
            emptyList()
        }
    }

    suspend fun addSearchHistory(userId: String, query: String) = withContext(Dispatchers.IO) {
        try {
            val history = SearchHistory(
                userId = userId,
                query = query
            )
            supabase.postgrest["search_history"].insert(history)
        } catch (e: Exception) {
            Log.e("DatabaseRepository", "Error adding search history", e)
        }
    }

    suspend fun updateCategories(userId: String, categoryIds: List<String>) = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["profiles"]
                .update(
                    {
                        set("selected_categories", categoryIds)
                    }
                ) {
                    filter {
                        eq("id", userId)
                    }
                }
        } catch (e: Exception) {
            Log.e("DatabaseRepository", "Error updating categories", e)
        }
    }
}
