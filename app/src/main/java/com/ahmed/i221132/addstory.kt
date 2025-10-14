package com.ahmed.i221132

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.util.UUID

class addstory : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storyImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addstory)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        storyImageView = findViewById(R.id.storyImage)
        val imageUri = intent.getStringExtra("storyImage")
        val closeBtn = findViewById<ImageView>(R.id.closeBtn)
        val yourStoryBtn = findViewById<Button>(R.id.yourStoryBtn)

        if (imageUri != null) {
            storyImageView.setImageURI(Uri.parse(imageUri))
        }

        closeBtn.setOnClickListener {
            finish()
        }

        // 🔑 NEW: Add functionality to the "Your Story" button
        yourStoryBtn.setOnClickListener {
            uploadStory()
        }
    }

    private fun uploadStory() {
        val imageBase64 = encodeImageToBase64()
        if (imageBase64 == null) {
            Toast.makeText(this, "Could not process image.", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUserUid = auth.currentUser?.uid
        if (currentUserUid == null) {
            Toast.makeText(this, "You must be logged in to post a story.", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Posting story...", Toast.LENGTH_SHORT).show()

        val storyId = database.reference.push().key ?: UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()

        val storyData = mapOf(
            "storyId" to storyId,
            "userId" to currentUserUid,
            "imageBase64" to imageBase64,
            "timestamp" to timestamp
        )

        // Save the story under a new 'stories' node, grouped by the user's ID
        database.getReference("stories").child(currentUserUid).child(storyId).setValue(storyData)
            .addOnSuccessListener {
                Toast.makeText(this, "Story posted!", Toast.LENGTH_SHORT).show()
                finish() // Close the screen after posting
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to post story.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun encodeImageToBase64(): String? {
        return try {
            val bitmap = (storyImageView.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
            val byteArray = baos.toByteArray()
            Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}