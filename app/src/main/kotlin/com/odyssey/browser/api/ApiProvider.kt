package com.odyssey.browser.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ApiProvider {
    private val geminiApi = GeminiApi()
    private val ollamaApi = OllamaApi()

    suspend fun queryGemini(query: String): String = withContext(Dispatchers.IO) {
        return@withContext try { geminiApi.query(query) } catch (e: Exception) { "Gemini Error: ${e.message}" }
    }

    suspend fun queryOllama(query: String): String = withContext(Dispatchers.IO) {
        return@withContext try { ollamaApi.query(query) } catch (e: Exception) { "Ollama Error: ${e.message}" }
    }
}
