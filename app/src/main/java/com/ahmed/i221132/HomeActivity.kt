package com.ahmed.i221132

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

private lateinit var pickImageLauncher: ActivityResultLauncher<String>
class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val search_image = findViewById<ImageView>(R.id.search_image)
        val message_button = findViewById<ImageView>(R.id.message_button)
        val profile_image = findViewById<ImageView>(R.id.profile_image)
        val add_post = findViewById<ImageView>(R.id.add_post)
        val camera_button = findViewById<ImageView>(R.id.camera_button)

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