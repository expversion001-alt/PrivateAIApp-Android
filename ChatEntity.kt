package com.privateai.app
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_history")
data class ChatEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val message: String,
    val isUser: Boolean,
    val imageUrl: String? = null,
    val isImage: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
