package com.odyssey.browser.api

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import com.squareup.moshi.Json

data class OllamaRequest(@Json(name = "model") val model: String, @Json(name = "prompt") val prompt: String, @Json(name = "stream") val stream: Boolean = false)
data class OllamaResponse(@Json(name = "response") val response: String?)

interface OllamaService {
    @POST("api/generate")
    suspend fun generate(@Body request: OllamaRequest): OllamaResponse
}

class OllamaApi {
    private var service: OllamaService? = null

    private fun getService(): OllamaService {
        if (service == null) {
            val baseUrl = System.getenv("OLLAMA_HOST") ?: "http://localhost:11434/"
            service = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(OllamaService::class.java)
        }
        return service!!
    }

    suspend fun query(text: String): String {
        val model = System.getenv("OLLAMA_MODEL") ?: "mistral"
        if (model.isEmpty()) return "Ollama model not configured"
        val request = OllamaRequest(model = model, prompt = text)
        return try {
            val response = getService().generate(request)
            response.response ?: "No response from Ollama"
        } catch (e: Exception) { "Error: ${e.message}" }
    }
}
