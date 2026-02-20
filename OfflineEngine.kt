package com.privateai.app

object OfflineEngine {

    fun reply(message: String): String {
        return when {
            message.contains("hello", true) -> "Hello! Offline mode active."
            message.contains("help", true) -> "Limited offline assistance available."
            else -> "Offline mode: limited response."
        }
    }
}
