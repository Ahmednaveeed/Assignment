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

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        conversationsRecyclerView = findViewById(R.id.dms_recycler_view)
        conversationsRecyclerView.layoutManager = LinearLayoutManager(this)
        conversationAdapter = DMAdapter(conversationList, this)
        conversationsRecyclerView.adapter = conversationAdapter

        loadConversations()

        val backBtn = findViewById<ImageView>(R.id.backBtn)
        val newMessageButton = findViewById<ImageView>(R.id.addbtn)

        backBtn.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
        newMessageButton.setOnClickListener {
            startActivity(Intent(this, NewMessageActivity::class.java))
        }
    }

    private fun loadConversations() {
        val currentUserUid = auth.currentUser?.uid ?: return
        val conversationsRef = database.getReference("user-chats").child(currentUserUid)

        conversationsRef.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                conversationList.clear()
                for (convoSnapshot in snapshot.children) {
                    val partnerId = convoSnapshot.key
                    val lastMessage = convoSnapshot.child("lastMessage").getValue(String::class.java)
                    val timestamp = convoSnapshot.child("timestamp").getValue(Long::class.java)

                    if (partnerId != null) {
                        database.getReference("users").child(partnerId).get().addOnSuccessListener { userSnapshot ->
                            val username = userSnapshot.child("username").getValue(String::class.java)
                            // 🔑 CHANGE: Fetched 'profileImageBase64' to pass to the adapter
                            val profileImageBase64 = userSnapshot.child("profileImageBase64").getValue(String::class.java)
                            val uid = userSnapshot.child("uid").getValue(String::class.java)

                            val conversation = Conversation(
                                uid = uid ?: partnerId,
                                username = username ?: "",
                                profileImageBase64 = profileImageBase64 ?: "",
                                lastMessage = lastMessage ?: "",
                                timestamp = timestamp ?: 0L
                            )
                            if (!conversationList.any { it.uid == conversation.uid }) {
                                conversationList.add(conversation)
                            }

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