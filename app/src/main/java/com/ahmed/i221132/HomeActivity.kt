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
import com.google.android.material.color.DynamicColors
import de.hdodenhof.circleimageview.CircleImageView

private lateinit var pickImageLauncher: ActivityResultLauncher<String>

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // NEW: RecyclerView Setup for Posts
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
            // more can be added
        )

        val adapter = PostAdapter(posts, this)
        recyclerView.adapter = adapter
        // END NEW

        val search_image = findViewById<ImageView>(R.id.search_image)
        val message_button = findViewById<ImageView>(R.id.message_button)
        val add_post = findViewById<ImageView>(R.id.add_post)
        val camera_button = findViewById<ImageView>(R.id.camera_button)
        val profile_image = findViewById<CircleImageView>(R.id.profile_image)  // FIXED: Cast to CircleImageView
        val heart_image = findViewById<ImageView>(R.id.heart_image)
        val your_story = findViewById<CircleImageView>(R.id.your_story)  // FIXED: Cast to CircleImageView
        val story1_image = findViewById<CircleImageView>(R.id.story1_image)  // FIXED: Cast to CircleImageView
        val story2_image = findViewById<CircleImageView>(R.id.story2_image)  // FIXED: Cast to CircleImageView
        val story3_image = findViewById<CircleImageView>(R.id.story3_image)  // FIXED: Cast to CircleImageView
        val story4_image = findViewById<CircleImageView>(R.id.story4_image)  // FIXED: Cast to CircleImageView

        pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                // Open Story Page with selected image
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
        your_story.setOnClickListener {
            val intent = Intent(this, yourstory::class.java)
            startActivity(intent)
        }
        story1_image.setOnClickListener {
            val intent = Intent(this, story1::class.java)
            startActivity(intent)
        }
        story2_image.setOnClickListener {
            val intent = Intent(this, story2::class.java)
            startActivity(intent)
        }
        story3_image.setOnClickListener {
            val intent = Intent(this, story3::class.java)
            startActivity(intent)
        }
        story4_image.setOnClickListener {
            val intent = Intent(this, story4::class.java)
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