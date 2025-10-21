package com.ahmed.i221132

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ahmed.i221132.R // CRITICAL IMPORT

class FollowListActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var listTitleText: TextView
    private lateinit var followListRecyclerView: RecyclerView
    private lateinit var emptyListMessage: TextView
    private lateinit var userListAdapter: UserListAdapter
    private val userList = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow_list)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // 1. Get data from Intent
        val targetUserId = intent.getStringExtra("TARGET_USER_UID")
        val listType = intent.getStringExtra("LIST_TYPE")

        if (targetUserId == null || listType == null) {
            Toast.makeText(this, "Error: Invalid user data.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 2. Initialize UI
        // If the app crashes here, the ID names in activity_follow_list.xml are WRONG.
        listTitleText = findViewById(R.id.list_title_text)
        followListRecyclerView = findViewById(R.id.follow_list_recycler_view)
        emptyListMessage = findViewById(R.id.empty_list_message)

        listTitleText.text = listType

        // 3. Setup RecyclerView
        userListAdapter = UserListAdapter(userList, this)
        followListRecyclerView.layoutManager = LinearLayoutManager(this)
        followListRecyclerView.adapter = userListAdapter

        // 4. Load the list
        loadUserList(targetUserId, listType)
    }

    // ... (loadUserList and fetchUserDetails functions remain as they were)

    private fun loadUserList(targetUserId: String, listType: String) {
        val listNode = if (listType == "Followers") "followers" else "following"

        database.getReference(listNode).child(targetUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userUids = mutableListOf<String>()

                    for (childSnapshot in snapshot.children) {
                        childSnapshot.key?.let { userUids.add(it) }
                    }

                    if (userUids.isEmpty()) {
                        showEmptyState(listType)
                        return
                    }

                    fetchUserDetails(userUids)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@FollowListActivity, "Failed to load list.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun fetchUserDetails(uids: List<String>) {
        database.getReference("users").addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()

                for (uid in uids) {
                    snapshot.child(uid).getValue(User::class.java)?.let { user ->
                        userList.add(user.copy(uid = uid))
                    }
                }

                if (userList.isEmpty()) {
                    showEmptyState(listTitleText.text.toString())
                } else {
                    followListRecyclerView.visibility = View.VISIBLE
                    emptyListMessage.visibility = View.GONE
                    userListAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FollowListActivity, "Failed to fetch user details.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showEmptyState(listType: String) {
        followListRecyclerView.visibility = View.GONE
        emptyListMessage.text = "This user currently has no ${listType.toLowerCase()}."
        emptyListMessage.visibility = View.VISIBLE
    }
}