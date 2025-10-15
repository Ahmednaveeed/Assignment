package com.ahmed.i221132

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.util.UUID

class add_post : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var imageView: ImageView // Made this a class property

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val backArrow = findViewById<ImageView>(R.id.back_arrow)
        val postButton = findViewById<Button>(R.id.post_button)
        imageView = findViewById(R.id.post_image_preview) // Initialized here
        val captionEditText = findViewById<EditText>(R.id.caption_edit_text)
        val locationEditText = findViewById<EditText>(R.id.location_edit_text)

        // Receive and display the selected image preview
        val postImageUriString = intent.getStringExtra("postImage")
        if (postImageUriString != null) {
            val postImageUri = Uri.parse(postImageUriString)
            imageView.setImageURI(postImageUri)
        }

        backArrow.setOnClickListener { finish() }

        postButton.setOnClickListener {
            val caption = captionEditText.text.toString().trim()
            val location = locationEditText.text.toString().trim()

            if (imageView.drawable == null) {
                Toast.makeText(this, "No image selected.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (caption.isEmpty()) {
                Toast.makeText(this, "Caption cannot be empty.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔑 1. Encode the image into a Base64 string
            val imageBase64 = encodeImageToBase64()
            if (imageBase64 != null) {
                // 🔑 2. Save the post with the encoded string to the database
                savePostToDatabase(caption, location, imageBase64)
            } else {
                Toast.makeText(this, "Failed to process image.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun encodeImageToBase64(): String? {
        return try {
            val bitmap = (imageView.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            // Compress the image to reduce its size before encoding
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
            val byteArray = baos.toByteArray()
            Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun savePostToDatabase(caption: String, location: String, imageBase64: String) {
        val uid = auth.currentUser?.uid ?: return
        Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show()

        val postId = database.getReference("posts").push().key ?: UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()

        val postMap = HashMap<String, Any>()
        postMap["postId"] = postId
        postMap["userId"] = uid
        postMap["imageBase64"] = imageBase64
        postMap["caption"] = caption
        postMap["location"] = location
        postMap["timestamp"] = timestamp

        // 🔑 FIXED: Initialize 'likes' as an empty map, not the number 0
        postMap["likes"] = emptyMap<String, Boolean>()

        database.getReference("posts").child(postId).setValue(postMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Posted successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to post: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}