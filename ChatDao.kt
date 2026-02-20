package com.privateai.app
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ChatDao {
    @Insert
    suspend fun insertMessage(chatEntity: ChatEntity)

    @Query("SELECT * FROM chat_history ORDER BY timestamp ASC")
    suspend fun getAllMessages(): List<ChatEntity>

    @Query("DELETE FROM chat_history")    suspend fun clearAll()
}
