package com.ahmed.i221132

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class message : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var conversationsRecyclerView: RecyclerView
    private lateinit var conversationAdapter: DMAdapter
    private val conversationList = mutableListOf<Conversation>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        // 🔑 1. INITIALIZE ALL VARIABLES FIRST
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        conversationsRecyclerView = findViewById(R.id.dms_recycler_view)
        val backBtn = findViewById<ImageView>(R.id.backBtn)
        val newMessageButton = findViewById<ImageView>(R.id.addbtn)

        // 🔑 2. SET UP THE RECYCLERVIEW AND ADAPTER
        conversationsRecyclerView.layoutManager = LinearLayoutManager(this)
        conversationAdapter = DMAdapter(conversationList, this)
        conversationsRecyclerView.adapter = conversationAdapter

        // 🔑 3. SET UP CLICK LISTENERS
        backBtn.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        newMessageButton.setOnClickListener {
            // Open the new activity that lists all users
            startActivity(Intent(this, NewMessageActivity::class.java))
        }

        // 🔑 4. FINALLY, LOAD THE DATA
        loadConversations()
    }

    private fun loadConversations() {
        val currentUserUid = auth.currentUser?.uid ?: return
        val conversationsRef = database.getReference("user-chats").child(currentUserUid)

        // Listen for changes in the user's conversation list
        conversationsRef.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                conversationList.clear()
                for (convoSnapshot in snapshot.children) {
                    val partnerId = convoSnapshot.key
                    val lastMessage = convoSnapshot.child("lastMessage").getValue(String::class.java)
                    val timestamp = convoSnapshot.child("timestamp").getValue(Long::class.java)

                    if (partnerId != null) {
                        // Now, fetch the partner's user details (username, profile pic)
                        database.getReference("users").child(partnerId).get().addOnSuccessListener { userSnapshot ->
                            val username = userSnapshot.child("username").getValue(String::class.java)
                            val profileImageUrl = userSnapshot.child("profileImageUrl").getValue(String::class.java)
                            val uid = userSnapshot.child("uid").getValue(String::class.java)

                            val conversation = Conversation(
                                uid = uid ?: partnerId,
                                username = username ?: "",
                                profileImageUrl = profileImageUrl ?: "",
                                lastMessage = lastMessage ?: "",
                                timestamp = timestamp ?: 0L
                            )
                            // To avoid adding duplicates during the async fetch
                            if (!conversationList.any { it.uid == conversation.uid }) {
                                conversationList.add(conversation)
                            }

                            // Sort the list to show the newest conversations first
                            conversationList.sortByDescending { it.timestamp }
                            conversationAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@message, "Failed to load conversations.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}