package com.ahmed.i221132

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.net.Uri
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class EditProfile : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val cancel = findViewById<TextView>(R.id.cancel)
        val done = findViewById<TextView>(R.id.done)
        val change_photo = findViewById<TextView>(R.id.change_photo)
        val profileImage = findViewById<ImageView>(R.id.profile_image)

        cancel.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            finish()
        }

        done.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            finish()
        }


        val launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult())
        {
            if(it.resultCode == RESULT_OK){
                val uri = it.data?.data
                profileImage.setImageURI(uri)
            }
        }

        change_photo.setOnClickListener {
            var intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            launcher.launch(intent)
        }

    }
}