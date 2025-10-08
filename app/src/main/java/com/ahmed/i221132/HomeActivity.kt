package com.ahmed.i221132

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView

private lateinit var pickImageLauncher: ActivityResultLauncher<String>

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // NEW: Stories RecyclerView Setup
        val storiesRecyclerView = findViewById<RecyclerView>(R.id.stories_recycler_view)
        storiesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val stories = listOf(
            Story("Your story", R.drawable.ahmed),
            Story("abdullah", R.drawable.abdullah),
            Story("hammad", R.drawable.hammad),
            Story("zohaibbb", R.drawable.zohaib),
            Story("saulehh", R.drawable.sauleh)
        )

        val storyAdapter = StoryAdapter(stories, this) { position ->
            // Handle story clicks based on position
            val intent = when (position) {
                0 -> Intent(this, yourstory::class.java)
                1 -> Intent(this, story1::class.java)
                2 -> Intent(this, story2::class.java)
                3 -> Intent(this, story3::class.java)
                4 -> Intent(this, story4::class.java)
                else -> return@StoryAdapter
            }
            startActivity(intent)
        }
        storiesRecyclerView.adapter = storyAdapter
        // END NEW

        // Posts RecyclerView Setup
        val recyclerView = findViewById<RecyclerView>(R.id.posts_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        val posts = listOf(
            Post(
                "abdullah_malik309",
                "ISB, pakistan",
                R.drawable.abdullah,
                R.drawable.abdullah2,
                "Liked by hammad__yasin and 44686 others",
                "abdullah_malik309 The game in Japan was amazing and I want to share some photos"
            ),
            Post(
                "zohaib_shafqat",
                "home, RWP",
                R.drawable.zohaib,
                R.drawable.zohaib2,
                "Liked by hammad__yasin and 2686 others",
                "zohaib_shafqat Snooker is my passion"
            )
        )

        val adapter = PostAdapter(posts, this)
        recyclerView.adapter = adapter

        val search_image = findViewById<ImageView>(R.id.search_image)
        val message_button = findViewById<ImageView>(R.id.message_button)
        val add_post = findViewById<ImageView>(R.id.add_post)
        val camera_button = findViewById<ImageView>(R.id.camera_button)
        val profile_image = findViewById<CircleImageView>(R.id.profile_image)
        val heart_image = findViewById<ImageView>(R.id.heart_image)

        pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                val intent = Intent(this, addstory::class.java)
                intent.putExtra("storyImage", uri.toString())
                startActivity(intent)
            }
        }

        search_image.setOnClickListener {
            val intent = Intent(this, search::class.java)
            startActivity(intent)
        }
        message_button.setOnClickListener {
            val intent = Intent(this, message::class.java)
            startActivity(intent)
        }
        profile_image.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }
        heart_image.setOnClickListener {
            val intent = Intent(this, heart_following::class.java)
            startActivity(intent)
        }
        camera_button.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        var launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult())
        {
            if(it.resultCode == RESULT_OK){
                var uri = it.data?.data
                add_post.setImageURI(uri)
            }
        }

        add_post.setOnClickListener {
            var intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            launcher.launch(intent)
        }
    }
}