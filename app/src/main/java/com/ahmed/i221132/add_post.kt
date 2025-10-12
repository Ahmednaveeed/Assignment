package com.ahmed.i221132

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class add_post : AppCompatActivity() {

    // --- FIREBASE INITIALIZATION ---
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage

    private var postImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        // --- VIEW INITIALIZATION ---
        val backArrow = findViewById<ImageView>(R.id.back_arrow)
        val postButton = findViewById<Button>(R.id.post_button)
        val imageView = findViewById<ImageView>(R.id.post_image_preview)
        val captionEditText = findViewById<EditText>(R.id.caption_edit_text)
        val locationEditText = findViewById<EditText>(R.id.location_edit_text)

        // --- RECEIVE AND DISPLAY IMAGE ---
        val postImageUriString = intent.getStringExtra("postImage")
        if (postImageUriString != null) {
            postImageUri = Uri.parse(postImageUriString)
            imageView.setImageURI(postImageUri)
        }

        // --- LISTENERS ---
        backArrow.setOnClickListener {
            finish() // Go back to the previous screen
        }

        postButton.setOnClickListener {
            val caption = captionEditText.text.toString().trim()
            val location = locationEditText.text.toString().trim()

            if (postImageUri == null) {
                Toast.makeText(this, "No image selected.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (caption.isEmpty()) {
                Toast.makeText(this, "Caption cannot be empty.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            uploadPost(caption, location)
        }
    }

    private fun uploadPost(caption: String, location: String) {
        val uid = auth.currentUser?.uid ?: return
        Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show()

        // 1. UPLOAD IMAGE TO FIREBASE STORAGE
        val postId = UUID.randomUUID().toString() // Create a unique ID for the post
        val storageRef = storage.reference.child("posts/$uid/$postId")

        storageRef.putFile(postImageUri!!)
            .addOnSuccessListener {
                // Image uploaded, now get the download URL
                storageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                    // 2. SAVE POST TO FIREBASE REALTIME DATABASE
                    savePostToDatabase(postId, uid, imageUrl.toString(), caption, location)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun savePostToDatabase(postId: String, uid: String, imageUrl: String, caption: String, location: String) {
        val timestamp = System.currentTimeMillis()

        val postMap = HashMap<String, Any>()
        postMap["postId"] = postId
        postMap["userId"] = uid
        postMap["imageUrl"] = imageUrl
        postMap["caption"] = caption
        postMap["location"] = location
        postMap["timestamp"] = timestamp
        postMap["likes"] = 0 // Initialize likes

        database.getReference("posts").child(postId).setValue(postMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Posted successfully!", Toast.LENGTH_SHORT).show()
                finish() // Close the add post screen and return to home
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to post: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}