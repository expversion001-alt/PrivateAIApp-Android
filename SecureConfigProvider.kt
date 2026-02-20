package com.privateai.app

object SecureConfigProvider {
    // In a real production app, this should be obfuscated or stored securely
    fun getHuggingFaceToken(): String {
        return "Bearer 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" 
    }

    const val DEFAULT_MODEL = "mistralai/Mistral-7B-Instruct-v0.2"
    const val IMAGE_MODEL = "runwayml/stable-diffusion-v1-5"
}
