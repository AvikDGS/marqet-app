package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class NewsResponse(
    val status: String? = null,
    val totalResults: Int? = null,
    val articles: List<Article> = emptyList()
)

@Serializable
data class GNewsResponse(
    val totalArticles: Int? = null,
    val articles: List<GNewsArticle> = emptyList()
)

@Serializable
data class GNewsArticle(
    val title: String? = null,
    val description: String? = null,
    val content: String? = null,
    val url: String? = null,
    val image: String? = null,
    val publishedAt: String? = null,
    val source: Source? = null
)

@Serializable
data class MediastackResponse(
    val data: List<MediastackArticle> = emptyList()
)

@Serializable
data class MediastackArticle(
    val title: String? = null,
    val description: String? = null,
    val source: String? = null,
    val url: String? = null,
    val image: String? = null,
    val published_at: String? = null
)

@Serializable
data class Article(
    val source: Source? = null,
    val author: String? = null,
    val title: String? = null,
    val description: String? = null,
    val url: String? = null,
    val urlToImage: String? = null,
    val publishedAt: String? = null,
    val content: String? = null
)

@Serializable
data class Source(
    val id: String? = null,
    val name: String? = null
)

@Serializable
data class GeminiGenerateContentRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null
)

@Serializable
data class GeminiContent(
    val parts: List<GeminiPart>
)

@Serializable
data class GeminiPart(
    val text: String
)

@Serializable
data class GeminiGenerateContentResponse(
    val candidates: List<GeminiCandidate>? = null
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null
)

@Serializable
data class GroqChatRequest(
    val model: String = "llama-3.1-8b-instant",
    val messages: List<GroqMessage>,
    val max_tokens: Int = 200,
    val temperature: Double = 0.3
)

@Serializable
data class GroqMessage(
    val role: String,
    val content: String
)

@Serializable
data class GroqChatResponse(
    val choices: List<GroqChoice>? = null
)

@Serializable
data class GroqChoice(
    val message: GroqMessage? = null
)

@Serializable
data class HuggingFaceRequest(
    val inputs: String
)

@Serializable
data class HuggingFaceResponseItem(
    val summary_text: String? = null
)
