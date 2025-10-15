package com.ahmed.i221132

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.UUID

class CommentsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var postId: String? = null

    private lateinit var commentAdapter: CommentAdapter
    private val commentList = mutableListOf<Comment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        postId = intent.getStringExtra("POST_ID")

        val toolbar: Toolbar = findViewById(R.id.comments_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val commentsRecyclerView = findViewById<RecyclerView>(R.id.comments_recycler_view)
        val commentEditText = findViewById<EditText>(R.id.comment_edit_text)
        val postCommentButton = findViewById<TextView>(R.id.post_comment_button)

        commentAdapter = CommentAdapter(commentList, this)
        commentsRecyclerView.adapter = commentAdapter
        commentsRecyclerView.layoutManager = LinearLayoutManager(this)

        postCommentButton.setOnClickListener {
            val commentText = commentEditText.text.toString().trim()
            if (commentText.isNotEmpty()) {
                postComment(commentText)
                commentEditText.text.clear()
            }
        }

        loadComments()
    }

    private fun postComment(text: String) {
        val userId = auth.currentUser?.uid
        if (userId == null || postId == null) return

        val commentId = database.reference.push().key ?: UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val comment = Comment(commentId, postId!!, userId, text, timestamp)

        database.getReference("comments").child(postId!!).child(commentId).setValue(comment)
    }

    private fun loadComments() {
        if (postId == null) return

        val commentsRef = database.getReference("comments").child(postId!!)
        commentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                commentList.clear()
                for (commentSnapshot in snapshot.children) {
                    val comment = commentSnapshot.getValue(Comment::class.java)
                    if (comment != null) {
                        commentList.add(comment)
                    }
                }
                commentAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CommentsActivity, "Failed to load comments.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}