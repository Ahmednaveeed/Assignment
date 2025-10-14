package com.ahmed.i221132

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageView

    private var receiverId: String? = null
    private var senderId: String? = null
    private var chatRoomId: String? = null

    private lateinit var chatAdapter: ChatAdapter
    private val messageList = mutableListOf<ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        receiverId = intent.getStringExtra("USER_ID")
        val receiverName = intent.getStringExtra("USER_NAME")
        senderId = auth.currentUser?.uid

        val toolbar: Toolbar = findViewById(R.id.chat_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = receiverName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        if (receiverId == null || senderId == null) {
            Toast.makeText(this, "Error: Chat could not be initialized.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        chatRoomId = getChatRoomId(senderId!!, receiverId!!)

        messagesRecyclerView = findViewById(R.id.messages_recycler_view)
        messageInput = findViewById(R.id.message_input)
        sendButton = findViewById(R.id.send_button)

        chatAdapter = ChatAdapter(messageList, senderId!!)
        messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        messagesRecyclerView.adapter = chatAdapter

        sendButton.setOnClickListener {
            val messageText = messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
            }
        }

        listenForMessages()
    }

    private fun sendMessage(messageText: String) {
        val timestamp = System.currentTimeMillis()
        val message = ChatMessage(senderId!!, receiverId!!, messageText, timestamp)

        // 1. Push the message to the main chat room
        database.getReference("chats").child(chatRoomId!!).child("messages").push().setValue(message)
            .addOnSuccessListener {
                messageInput.setText("") // Clear the input field

                // 🔑 NEW: Update the conversation index for both users
                val conversationUpdate = mapOf(
                    "lastMessage" to messageText,
                    "timestamp" to timestamp
                )

                // Update for the sender
                database.getReference("user-chats").child(senderId!!).child(receiverId!!).setValue(conversationUpdate)

                // Update for the receiver
                database.getReference("user-chats").child(receiverId!!).child(senderId!!).setValue(conversationUpdate)
            }
    }

    private fun listenForMessages() {
        val messagesRef = database.getReference("chats").child(chatRoomId!!).child("messages")
        messagesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear()
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.getValue(ChatMessage::class.java)
                    if (message != null) {
                        messageList.add(message)
                    }
                }
                chatAdapter.notifyDataSetChanged()
                messagesRecyclerView.scrollToPosition(messageList.size - 1)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ChatActivity, "Failed to load messages.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getChatRoomId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
    }
}