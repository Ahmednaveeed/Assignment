package com.ahmed.i221132

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class NewMessageActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var userAdapter: DMAdapter // We can reuse the DMAdapter!
    private val userList = mutableListOf<Conversation>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val toolbar: Toolbar = findViewById(R.id.toolbar_new_message)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        usersRecyclerView = findViewById(R.id.users_recycler_view)
        usersRecyclerView.layoutManager = LinearLayoutManager(this)

        userAdapter = DMAdapter(userList, this)
        usersRecyclerView.adapter = userAdapter

        loadAllUsers()
    }

    private fun loadAllUsers() {
        val currentUserUid = auth.currentUser?.uid
        val usersRef = database.getReference("users")

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (userSnapshot in snapshot.children) {
                    // We can reuse the Conversation class to hold user data
                    val user = userSnapshot.getValue(Conversation::class.java)
                    // Add user to the list only if it's not the current user
                    if (user != null && user.uid != currentUserUid) {
                        userList.add(user)
                    }
                }
                userAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@NewMessageActivity, "Failed to load users.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}