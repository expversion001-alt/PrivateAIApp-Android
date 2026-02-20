package com.privateai.app

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var repository: ChatRepository
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var typingIndicator: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val chatDao = ChatDatabase.getDatabase(this).chatDao()
        repository = ChatRepository(chatDao)
        
        chatAdapter = ChatAdapter()
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        chatRecyclerView.adapter = chatAdapter
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        chatRecyclerView.layoutManager = layoutManager

        typingIndicator = findViewById(R.id.typingIndicator)
        val promptInput = findViewById<EditText>(R.id.promptInput)
        val btnChat = findViewById<MaterialButton>(R.id.btnChat)

        // Load history
        lifecycleScope.launch {
            val history = withContext(Dispatchers.IO) {
                repository.getAllMessages()
            }
            chatAdapter.setMessages(history)
            if (history.isNotEmpty()) {
                chatRecyclerView.scrollToPosition(history.size - 1)
            }
        }

        btnChat.setOnClickListener {
            val message = promptInput.text.toString()
            if (message.isNotBlank()) {
                promptInput.text.clear()
                
                // Add user message to UI and DB
                val userChat = ChatEntity(message = message, isUser = true)
                chatAdapter.addMessage(userChat)
                chatRecyclerView.smoothScrollToPosition(chatAdapter.itemCount - 1)
                
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        repository.saveMessage(message, true)
                    }
                    
                    // Show typing indicator
                    typingIndicator.visibility = View.VISIBLE
                    
                    try {
                        val reply = withContext(Dispatchers.IO) {
                            repository.sendMessage(message)
                        }
                        
                        // Hide typing indicator
                        typingIndicator.visibility = View.GONE
                        
                        // Add AI message to UI and DB
                        val aiChat = ChatEntity(message = reply, isUser = false)
                        chatAdapter.addMessage(aiChat)
                        chatRecyclerView.smoothScrollToPosition(chatAdapter.itemCount - 1)
                        
                        withContext(Dispatchers.IO) {
                            repository.saveMessage(reply, false)
                        }
                    } catch (e: Exception) {
                        typingIndicator.visibility = View.GONE
                        val errorChat = ChatEntity(message = "Error: Unable to process request.", isUser = false)
                        chatAdapter.addMessage(errorChat)
                        chatRecyclerView.smoothScrollToPosition(chatAdapter.itemCount - 1)
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear_chat -> {
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        repository.clearChat()
                    }
                    chatAdapter.clearMessages()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
