package com.ahmed.i221132

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView

class HighlightViewActivity : AppCompatActivity() {

    // --- UI Elements ---
    private lateinit var highlightImageView: ImageView
    private lateinit var highlightProgress: ProgressBar
    private lateinit var highlightCoverImage: CircleImageView
    private lateinit var highlightTitleText: TextView
    private lateinit var closeButton: ImageView

    // --- Firebase & Data ---
    private lateinit var database: FirebaseDatabase
    private val highlightItems = mutableListOf<DataSnapshot>()
    private var currentItemIndex = 0

    // --- Playback Controls ---
    private val handler = Handler(Looper.getMainLooper())
    private var storyDuration = 5000L // 5 seconds per image

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_highlight_view)

        // Initialize UI
        highlightImageView = findViewById(R.id.highlight_image_view)
        highlightProgress = findViewById(R.id.highlight_progress)
        highlightCoverImage = findViewById(R.id.highlight_cover_image)
        highlightTitleText = findViewById(R.id.highlight_title_text)
        closeButton = findViewById(R.id.close_button)

        database = FirebaseDatabase.getInstance()

        val userId = intent.getStringExtra("USER_ID")
        val highlightId = intent.getStringExtra("HIGHLIGHT_ID")

        if (userId == null || highlightId == null) {
            finish()
            return
        }

        closeButton.setOnClickListener { finish() }

        // Add click listener to advance to the next story
        findViewById<FrameLayout>(R.id.root_layout).setOnClickListener {
            handler.removeCallbacksAndMessages(null) // Stop the current timer
            showNextItem() // Show the next image immediately
        }

        fetchHighlightInfo(userId, highlightId)
    }

    private fun fetchHighlightInfo(userId: String, highlightId: String) {
        val highlightRef = database.getReference("highlights").child(userId).child(highlightId)

        // 1. Fetch the highlight's title and cover image
        highlightRef.get().addOnSuccessListener {
            val title = it.child("title").getValue(String::class.java)
            val coverImageBase64 = it.child("coverImageBase64").getValue(String::class.java)

            highlightTitleText.text = title
            if (!coverImageBase64.isNullOrEmpty()) {
                try {
                    val imageBytes = Base64.decode(coverImageBase64, Base64.DEFAULT)
                    highlightCoverImage.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))
                } catch (e: Exception) { /* Use default */ }
            }
        }

        // 2. Fetch all the items (images) within the highlight
        val itemsRef = highlightRef.child("items")
        itemsRef.orderByChild("timestamp").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(this@HighlightViewActivity, "No items in this highlight.", Toast.LENGTH_SHORT).show()
                    finish()
                    return
                }
                snapshot.children.forEach { highlightItems.add(it) }
                startPlayback()
            }
            override fun onCancelled(error: DatabaseError) { finish() }
        })
    }

    private fun startPlayback() {
        if (highlightItems.isEmpty()) {
            finish()
            return
        }
        showNextItem()
    }

    private fun showNextItem() {
        // Stop any previous timer
        handler.removeCallbacksAndMessages(null)

        if (currentItemIndex >= highlightItems.size) {
            finish() // All items have been viewed
            return
        }

        val itemSnapshot = highlightItems[currentItemIndex]
        val imageBase64 = itemSnapshot.child("imageBase64").getValue(String::class.java)

        // Decode and display the image
        if (imageBase64 != null) {
            try {
                val imageBytes = Base64.decode(imageBase64, Base64.DEFAULT)
                highlightImageView.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))
            } catch (e: Exception) {
                // Skip this item if image is corrupt
                currentItemIndex++
                showNextItem()
                return
            }
        }

        startProgressBar()
        currentItemIndex++
    }

    private fun startProgressBar() {
        highlightProgress.max = storyDuration.toInt()
        highlightProgress.progress = 0

        handler.post(object : Runnable {
            var progress = 0
            override fun run() {
                progress += 100 // Update every 100ms
                highlightProgress.progress = progress
                if (progress < storyDuration) {
                    handler.postDelayed(this, 100)
                } else {
                    // When the timer finishes, automatically show the next item
                    showNextItem()
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        // Crucial: Stop the handler when the activity is destroyed to prevent memory leaks
        handler.removeCallbacksAndMessages(null)
    }
}