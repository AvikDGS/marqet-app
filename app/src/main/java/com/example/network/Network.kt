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
import java.util.concurrent.TimeUnit

interface NewsApiService {
    @GET("top-headlines/category/business/us.json")
    suspend fun getBusinessNews(): NewsResponse

    @GET("top-headlines/category/{category}/us.json")
    suspend fun getNewsByCategory(@retrofit2.http.Path("category") category: String): NewsResponse
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
            .baseUrl("https://saurav.tech/NewsAPI/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(NewsApiService::class.java)
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
}
