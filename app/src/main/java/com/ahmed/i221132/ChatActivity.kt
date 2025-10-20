package com.ahmed.i221132

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.ByteArrayOutputStream

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

    private lateinit var attachButton: ImageView
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    // 🔑 NEW: Call Buttons
    private lateinit var videoCallButton: ImageView
    private lateinit var audioCallButton: ImageView

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
        listenForOnlineStatus()
        chatRoomId = getChatRoomId(senderId!!, receiverId!!)

        messagesRecyclerView = findViewById(R.id.messages_recycler_view)
        messageInput = findViewById(R.id.message_input)
        sendButton = findViewById(R.id.send_button)
        attachButton = findViewById(R.id.attach_button)

        // 🔑 NEW: Initialize Call Buttons (Ensure these IDs exist in your XML)
        videoCallButton = findViewById(R.id.video_call_button)
        audioCallButton = findViewById(R.id.audio_call_button)


        // CHANGED: Pass listeners for edit and delete actions to the adapter
        chatAdapter = ChatAdapter(
            messageList,
            senderId!!,
            onDelete = { message -> showDeleteConfirmation(message) },
            onEdit = { message -> showEditDeleteDialog(message) }
        )
        messagesRecyclerView.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        messagesRecyclerView.adapter = chatAdapter

        // Setup image picker
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { imageUri -> sendImageMessage(imageUri) }
            }
        }

        attachButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
            imagePickerLauncher.launch(intent)
        }

        sendButton.setOnClickListener {
            val messageText = messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
            }
        }

        // 🔑 NEW: Call Button Listeners
        videoCallButton.setOnClickListener { startCall(receiverId!!, "VIDEO") }
        audioCallButton.setOnClickListener { startCall(receiverId!!, "AUDIO") }

        listenForMessages()
    }

    // 🔑 MOVED and MODIFIED: Call Signaling Logic
    private fun startCall(targetUserId: String, callType: String) {
        val currentUserId = auth.currentUser?.uid ?: return

        // 1. Generate a unique channel name
        val sortedUids = listOf(currentUserId, targetUserId).sorted()
        val channelName = "${sortedUids[0]}_${sortedUids[1]}"

        // 2. Create a signaling node in Firebase to notify the recipient
        val callRef = database.getReference("calls").child(targetUserId)
        val callData = mapOf(
            "callerId" to currentUserId,
            "callType" to callType, // "VIDEO" or "AUDIO"
            "channelName" to channelName,
            "status" to "ringing"
        )

        callRef.setValue(callData)
            .addOnSuccessListener {
                // 3. Launch the local Call Activity
                val intent = Intent(this, CallActivity::class.java)
                intent.putExtra("CHANNEL_NAME", channelName)
                intent.putExtra("CALL_TYPE", callType)
                intent.putExtra("IS_CALLER", true)
                intent.putExtra("TARGET_USER_UID", targetUserId) // For cleanup
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to initiate call via Firebase.", Toast.LENGTH_SHORT).show()
            }
    }


    private fun listenForOnlineStatus() {
        val receiverRef = database.getReference("users").child(receiverId!!)

        receiverRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isOnline = snapshot.child("isOnline").getValue(Boolean::class.java) ?: false
                val lastSeen = snapshot.child("lastSeen").getValue(Long::class.java)

                val toolbarTitle = supportActionBar?.title?.toString() ?: "User"

                if (isOnline) {
                    supportActionBar?.subtitle = "Online"
                } else if (lastSeen != null) {
                    // Format the timestamp into a readable "Last seen X minutes ago" string
                    val timeDifference = System.currentTimeMillis() - lastSeen
                    // Simple version:
                    supportActionBar?.subtitle = "Last seen offline"
                    // Advanced version: Use a library or helper function to format time difference
                } else {
                    supportActionBar?.subtitle = "Offline"
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }







    private fun sendMessage(messageText: String) {
        val timestamp = System.currentTimeMillis()
        val messageKey = database.getReference("chats").child(chatRoomId!!).child("messages").push().key ?: ""
        val message = ChatMessage(messageKey, senderId!!, receiverId!!, messageText, null, timestamp)

        database.getReference("chats").child(chatRoomId!!).child("messages").child(messageKey).setValue(message)
            .addOnSuccessListener {
                messageInput.setText("")
                updateConversationIndex(messageText, timestamp)
            }
    }

    private fun sendImageMessage(imageUri: Uri) {
        val imageBase64 = encodeImageToBase64(imageUri)
        if (imageBase64 == null) {
            Toast.makeText(this, "Failed to process image.", Toast.LENGTH_SHORT).show()
            return
        }

        val timestamp = System.currentTimeMillis()
        val messageKey = database.getReference("chats").child(chatRoomId!!).child("messages").push().key ?: ""
        val message = ChatMessage(messageKey, senderId!!, receiverId!!, null, imageBase64, timestamp)

        database.getReference("chats").child(chatRoomId!!).child("messages").child(messageKey).setValue(message)
            .addOnSuccessListener {
                // NEW: Update the conversation index for image messages
                updateConversationIndex("📷 Photo", timestamp)
            }
    }


    // 🔑 NEW: Helper function to send the FCM notification payload (Targeting the receiver)
    private fun sendFCMNotification(targetUserId: String, title: String, body: String) {
        // We assume the receiver's token is saved in the database
        database.getReference("users").child(targetUserId).child("fcmToken").get()
            .addOnSuccessListener { snapshot ->
                val token = snapshot.getValue(String::class.java)
                if (!token.isNullOrEmpty()) {
                    // In a real app, this Toast confirms the readiness to send the payload.
                    Toast.makeText(this, "Notification triggered for user: $targetUserId", Toast.LENGTH_LONG).show()
                }
            }
    }
    // NEW: A reusable function to update the last message for both users
    private fun updateConversationIndex(lastMessage: String, timestamp: Long) {
        val conversationUpdate = mapOf(
            "lastMessage" to lastMessage,
            "timestamp" to timestamp
        )
        database.getReference("user-chats").child(senderId!!).child(receiverId!!).setValue(conversationUpdate)
        database.getReference("user-chats").child(receiverId!!).child(senderId!!).setValue(conversationUpdate)
    }

    private fun listenForMessages() {
        val messagesRef = database.getReference("chats").child(chatRoomId!!).child("messages")
        messagesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear()
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.getValue(ChatMessage::class.java)
                    if (message != null) {
                        // CHANGED: Store the unique ID of the message
                        message.messageId = messageSnapshot.key ?: ""
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

    private fun showEditDeleteDialog(message: ChatMessage) {
        val options = arrayOf("Edit Message", "Delete Message")
        AlertDialog.Builder(this)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditDialog(message) // Call the existing edit dialog
                    1 -> showDeleteConfirmation(message) // Call the delete confirmation
                }
            }
            .show()
    }

    // NEW: Function to show a confirmation dialog before deleting
    private fun showDeleteConfirmation(message: ChatMessage) {
        AlertDialog.Builder(this)
            .setTitle("Delete Message")
            .setMessage("Are you sure you want to delete this message?")
            .setPositiveButton("Delete") { _, _ ->
                deleteMessage(message)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun triggerScreenshotAlert() {
        // Determine the user whose chat was screenshotted (the target of the notification)
        val currentUserName = supportActionBar?.title?.toString() ?: "A User"

        // Notify the user you are chatting with (the receiver)
        sendFCMNotification(
            receiverId!!,
            "SECURITY ALERT",
            "${currentUserName} took a screenshot of your chat."
        )
        Toast.makeText(this, "MOCK ALERT SENT to receiver.", Toast.LENGTH_LONG).show()
    }

    // NEW: Function to delete the message from Firebase
    private fun deleteMessage(message: ChatMessage) {
        database.getReference("chats").child(chatRoomId!!).child("messages")
            .child(message.messageId).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Message deleted.", Toast.LENGTH_SHORT).show()
            }
    }

    // NEW: Function to show a dialog for editing a message
    private fun showEditDialog(message: ChatMessage) {
        val editText = EditText(this).apply { setText(message.text) }
        AlertDialog.Builder(this)
            .setTitle("Edit Message")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newText = editText.text.toString().trim()
                if (newText.isNotEmpty()) {
                    editMessage(message, newText)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // NEW: Function to update the message text in Firebase
    private fun editMessage(message: ChatMessage, newText: String) {
        database.getReference("chats").child(chatRoomId!!).child("messages")
            .child(message.messageId).child("text").setValue(newText)
            .addOnSuccessListener {
                Toast.makeText(this, "Message updated.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getChatRoomId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
    }

    private fun encodeImageToBase64(imageUri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
            val byteArray = baos.toByteArray()
            Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            null
        }
    }
}