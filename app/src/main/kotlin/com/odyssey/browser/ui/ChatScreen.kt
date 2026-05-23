package com.odyssey.browser.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.odyssey.browser.api.ApiProvider
import kotlinx.coroutines.launch

data class ChatMessage(val text: String, val isUser: Boolean)

@Composable
fun ChatScreen() {
    val apiProvider = remember { ApiProvider() }
    val messages = remember { mutableStateOf(listOf<ChatMessage>()) }
    val inputValue = remember { mutableStateOf(TextFieldValue("")) }
    val selectedApi = remember { mutableStateOf("Gemini") }
    val isLoading = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Gemini", "Ollama").forEach { api ->
                Button(
                    onClick = { selectedApi.value = api },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedApi.value == api) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(api)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages.value) { message ->
                ChatBubble(message)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = inputValue.value,
                onValueChange = { inputValue.value = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Message...") },
                enabled = !isLoading.value
            )
            IconButton(
                onClick = {
                    if (inputValue.value.text.isNotEmpty()) {
                        val userMessage = inputValue.value.text
                        messages.value = messages.value + ChatMessage(userMessage, true)
                        inputValue.value = TextFieldValue("")
                        isLoading.value = true
                        scope.launch {
                            try {
                                val response = when (selectedApi.value) {
                                    "Gemini" -> apiProvider.queryGemini(userMessage)
                                    "Ollama" -> apiProvider.queryOllama(userMessage)
                                    else -> "Unknown API"
                                }
                                messages.value = messages.value + ChatMessage(response, false)
                            } catch (e: Exception) {
                                messages.value = messages.value + ChatMessage("Error: ${e.message}", false)
                            } finally {
                                isLoading.value = false
                            }
                        }
                    }
                },
                enabled = !isLoading.value && inputValue.value.text.isNotEmpty()
            ) {
                Icon(Icons.Filled.Send, contentDescription = "Send")
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val backgroundColor = if (message.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (message.isUser) Color.White else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier.align(alignment).widthIn(max = 300.dp).background(backgroundColor, RoundedCornerShape(12.dp)).padding(12.dp)
    ) {
        Text(message.text, color = textColor)
    }
}
