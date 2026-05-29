package com.example.network

import com.example.model.GeminiGenerateContentRequest
import com.example.model.GeminiGenerateContentResponse
import com.example.model.NewsResponse
import com.example.model.CryptoPriceResponse
import com.example.model.YahooSparkItem
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Header

import java.util.concurrent.TimeUnit

interface NewsApiService {
    @GET("v2/top-headlines")
    suspend fun getNewsByCategory(
        @Query("category") category: String,
        @Query("apiKey") apiKey: String,
        @Query("country") country: String = "us"
    ): NewsResponse
}

interface GroqApiService {
    @POST("v1/chat/completions")
    suspend fun getSummary(
        @Header("Authorization") authHeader: String,
        @Body request: com.example.model.GroqChatRequest
    ): com.example.model.GroqChatResponse
}

interface HuggingFaceApiService {
    @POST("models/facebook/bart-large-cnn")
    suspend fun getSummary(
        @Header("Authorization") authHeader: String,
        @Body request: com.example.model.HuggingFaceRequest
    ): List<com.example.model.HuggingFaceResponseItem>
}

interface GNewsApiService {
    @GET("top-headlines")
    suspend fun getNewsByCategory(
        @Query("topic") topic: String,
        @Query("lang") lang: String = "en",
        @Query("token") token: String
    ): com.example.model.GNewsResponse
}

interface MediastackApiService {
    @GET("news")
    suspend fun getNewsByCategory(
        @Query("categories") categories: String,
        @Query("languages") languages: String = "en",
        @Query("access_key") token: String
    ): com.example.model.MediastackResponse
}

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiGenerateContentRequest
    ): GeminiGenerateContentResponse
}

interface CoinGeckoApiService {
    @GET("api/v3/simple/price")
    suspend fun getPrices(
        @Query("ids") ids: String,
        @Query("vs_currencies") vsCurrencies: String = "usd,inr",
        @Query("include_24hr_change") include24hChange: Boolean = true
    ): Map<String, CryptoPriceResponse>
}

interface YahooFinanceApiService {
    @GET("v8/finance/spark")
    suspend fun getSpark(
        @Query("symbols") symbols: String,
        @Query("range") range: String = "5d",
        @Query("interval") interval: String = "1d"
    ): Map<String, YahooSparkItem>
}

object RetrofitClient {
    private val json = Json { 
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
    }
    private val contentType = "application/json".toMediaType()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    val newsApi: NewsApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://newsapi.org/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(NewsApiService::class.java)
    }

    val gnewsApi: GNewsApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://gnews.io/api/v4/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(GNewsApiService::class.java)
    }

    val mediastackApi: MediastackApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://api.mediastack.com/v1/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(MediastackApiService::class.java)
    }

    val geminiApi: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(GeminiApiService::class.java)
    }

    val coinGeckoApi: CoinGeckoApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.coingecko.com/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(CoinGeckoApiService::class.java)
    }

    val yahooApi: YahooFinanceApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://query1.finance.yahoo.com/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(YahooFinanceApiService::class.java)
    }

    val groqApi: GroqApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.groq.com/openai/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(GroqApiService::class.java)
    }

    val huggingFaceApi: HuggingFaceApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api-inference.huggingface.co/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(HuggingFaceApiService::class.java)
    }
}
