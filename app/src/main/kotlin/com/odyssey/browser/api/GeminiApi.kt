package com.odyssey.browser.api

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import com.squareup.moshi.Json

data class GeminiRequest(@Json(name = "contents") val contents: List<Content>)
data class Content(@Json(name = "parts") val parts: List<Part>)
data class Part(@Json(name = "text") val text: String)
data class GeminiResponse(@Json(name = "candidates") val candidates: List<Candidate>?)
data class Candidate(@Json(name = "content") val content: Content?)

interface GeminiService {
    @POST("v1/models/gemini-pro:generateContent")
    suspend fun generateContent(@Query("key") apiKey: String, @Body request: GeminiRequest): GeminiResponse
}

class GeminiApi {
    private val service: GeminiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GeminiService::class.java)
    }

    suspend fun query(text: String): String {
        val apiKey = System.getenv("GEMINI_API_KEY") ?: ""
        if (apiKey.isEmpty()) return "Gemini API key not configured"
        val request = GeminiRequest(contents = listOf(Content(parts = listOf(Part(text = text)))))
        return try {
            val response = service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No response from Gemini"
        } catch (e: Exception) { "Error: ${e.message}" }
    }
}
