package com.ahmed.i221132

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream
import android.graphics.Bitmap

class EditProfile : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private lateinit var profileImage: CircleImageView
    private lateinit var nameValue: EditText
    private lateinit var usernameValue: EditText
    private lateinit var bioValue: EditText
    private lateinit var emailValue: EditText

    private var imageUri: Uri? = null
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // --- Initialization ---
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        profileImage = findViewById(R.id.profile_image)
        nameValue = findViewById(R.id.name_value)
        usernameValue = findViewById(R.id.username_value)
        bioValue = findViewById(R.id.bio_value) // Make sure this ID matches your XML
        emailValue = findViewById(R.id.email_value)

        val cancel = findViewById<TextView>(R.id.cancel)
        val done = findViewById<TextView>(R.id.done)
        val changePhoto = findViewById<TextView>(R.id.change_photo)
        val logoutButton = findViewById<Button>(R.id.logout_button)

        loadUserData()

        imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                imageUri = result.data?.data
                profileImage.setImageURI(imageUri)
            }
        }

        cancel.setOnClickListener { finish() }
        done.setOnClickListener { saveProfileChanges() }
        changePhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
            imagePickerLauncher.launch(intent)
        }
        logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, savedacc::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return
        val userRef = database.getReference("users").child(uid)

        userRef.get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                val name = dataSnapshot.child("name").getValue(String::class.java)
                val username = dataSnapshot.child("username").getValue(String::class.java)
                val email = dataSnapshot.child("email").getValue(String::class.java)
                // 🔑 GET BIO: Fetch the bio from Firebase
                val bio = dataSnapshot.child("bio").getValue(String::class.java)
                val profileImageBase64 = dataSnapshot.child("profileImageBase64").getValue(String::class.java)

                nameValue.setText(name)
                usernameValue.setText(username)
                emailValue.setText(email)
                // 🔑 SET BIO: Display the bio (will be empty if it's the first time)
                bioValue.setText(bio)

                if (!profileImageBase64.isNullOrEmpty()) {
                    try {
                        val imageBytes = Base64.decode(profileImageBase64, Base64.DEFAULT)
                        profileImage.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))
                    } catch (e: Exception) { /* Use default icon */ }
                }
            }
        }
    }

    private fun saveProfileChanges() {
        val uid = auth.currentUser?.uid ?: return
        val userRef = database.getReference("users").child(uid)

        val newName = nameValue.text.toString().trim()
        val newUsername = usernameValue.text.toString().trim()
        // 🔑 GET NEW BIO: Read the text from the bio EditText
        val newBio = bioValue.text.toString().trim()

        if (newName.isEmpty() || newUsername.isEmpty()) {
            Toast.makeText(this, "Name and Username cannot be empty.", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Saving...", Toast.LENGTH_SHORT).show()

        val updates = mutableMapOf<String, Any>(
            "name" to newName,
            "username" to newUsername,
            // 🔑 SAVE BIO: Add the bio to the data being saved
            "bio" to newBio
        )

        val newImageBase64 = encodeImageToBase64()
        if (newImageBase64 != null) {
            updates["profileImageBase64"] = newImageBase64
        }

        userRef.updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update profile.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun encodeImageToBase64(): String? {
        if (imageUri == null) return null
        return try {
            val drawable = profileImage.drawable
            val bitmap = (drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
            val byteArray = baos.toByteArray()
            Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            null
        }
    }
}