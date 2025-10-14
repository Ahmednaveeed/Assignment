package com.ahmed.i221132

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class StoryViewActivity : AppCompatActivity() {

    private lateinit var storyImageView: ImageView
    private lateinit var storyProgress: ProgressBar
    private lateinit var database: FirebaseDatabase

    private val stories = mutableListOf<DataSnapshot>()
    private var currentStoryIndex = 0
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_view)

        storyImageView = findViewById(R.id.story_image_view)
        storyProgress = findViewById(R.id.story_progress)
        database = FirebaseDatabase.getInstance()

        val userId = intent.getStringExtra("USER_ID")
        if (userId == null) {
            finish()
            return
        }

        fetchStories(userId)
    }

    private fun fetchStories(userId: String) {
        val twentyFourHoursAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
        val storiesRef = database.getReference("stories").child(userId)

        // Query for stories posted in the last 24 hours
        storiesRef.orderByChild("timestamp").startAt(twentyFourHoursAgo.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        Toast.makeText(this@StoryViewActivity, "No active stories.", Toast.LENGTH_SHORT).show()
                        finish()
                        return
                    }
                    snapshot.children.forEach { stories.add(it) }
                    startStoryPlayback()
                }

                override fun onCancelled(error: DatabaseError) {
                    finish()
                }
            })
    }

    private fun startStoryPlayback() {
        if (stories.isEmpty()) {
            finish()
            return
        }
        showNextStory()
    }

    private fun showNextStory() {
        if (currentStoryIndex >= stories.size) {
            finish() // All stories have been viewed
            return
        }

        val storySnapshot = stories[currentStoryIndex]
        val imageBase64 = storySnapshot.child("imageBase64").getValue(String::class.java)

        if (imageBase64 != null) {
            try {
                val imageBytes = Base64.decode(imageBase64, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                storyImageView.setImageBitmap(decodedImage)
            } catch (e: Exception) {
                // Skip this story if image is corrupt
                currentStoryIndex++
                showNextStory()
                return
            }
        }

        // Simulate story duration (e.g., 5 seconds)
        storyProgress.max = 5000
        storyProgress.progress = 0
        handler.post(object : Runnable {
            var progress = 0
            override fun run() {
                progress += 100
                storyProgress.progress = progress
                if (progress < 5000) {
                    handler.postDelayed(this, 100)
                } else {
                    currentStoryIndex++
                    showNextStory()
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null) // Stop handler to prevent memory leaks
    }
}