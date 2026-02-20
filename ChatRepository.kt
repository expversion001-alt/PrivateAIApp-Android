package com.privateai.app

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.CertificatePinner
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.InputStream
import androidx.annotation.Keep
import java.util.concurrent.TimeUnit

@Keep
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val parameters: Map<String, Any> = mapOf("wait_for_model" to true)
)

@Keep
data class ChatMessage(
    val role: String,
    val content: String
)

@Keep
data class ChatCompletionResponse(
    val choices: List<ChatChoice>
)

@Keep
data class ChatChoice(
    val message: ChatMessage
)

class ChatRepository(private val chatDao: ChatDao? = null) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val api: HuggingFaceApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://router.huggingface.co/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HuggingFaceApi::class.java)
    }

    suspend fun getAllMessages(): List<ChatEntity> {
        return chatDao?.getAllMessages() ?: emptyList()
    }

    suspend fun saveMessage(message: String, isUser: Boolean) {
        chatDao?.insertMessage(ChatEntity(message = message, isUser = isUser))
    }

    suspend fun clearChat() {
        chatDao?.clearAll()
    }

    suspend fun sendMessage(message: String): String {
        return try {
            val messages = listOf(ChatMessage(role = "user", content = message))
            val request = ChatRequest(
                model = SecureConfigProvider.DEFAULT_MODEL,
                messages = messages
            )
            
            val response = api.chatV1(
                token = SecureConfigProvider.getHuggingFaceToken(),
                request = request
            )
            
            if (response.choices.isNotEmpty()) {
                response.choices[0].message.content.trim()
            } else {
                throw Exception("Empty response from API")
            }
        } catch (e: Exception) {
            OfflineEngine.reply(message)
        }
    }

    suspend fun generateImage(prompt: String): Bitmap? {
        return try {
            val responseBody = api.generateImage(
                modelId = SecureConfigProvider.IMAGE_MODEL,
                token = SecureConfigProvider.getHuggingFaceToken(),
                request = mapOf("inputs" to prompt, "parameters" to mapOf("wait_for_model" to true))
            )
            val inputStream: InputStream = responseBody.byteStream()
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            null
        }
    }
}
