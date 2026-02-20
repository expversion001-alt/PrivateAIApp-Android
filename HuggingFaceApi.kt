package com.privateai.app

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import okhttp3.ResponseBody
import retrofit2.http.Streaming

interface HuggingFaceApi {
    @POST("v1/chat/completions")
    suspend fun chatV1(
        @Header("Authorization") token: String,
        @Body request: ChatRequest
    ): ChatCompletionResponse

    @Streaming
    @POST("hf/models/{modelId}")
    suspend fun generateImage(
        @Path("modelId") modelId: String,
        @Header("Authorization") token: String,
        @Body request: Map<String, Any>
    ): ResponseBody
}
