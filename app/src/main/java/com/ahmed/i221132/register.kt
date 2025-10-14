package com.ahmed.i221132

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class register : AppCompatActivity() {
    @SuppressLint("WrongViewCast")
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    // 🔑 FIXED: Removed the unused 'nameEditText'
    private lateinit var usernameEditText: EditText
    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // --- Firebase Initialization ---
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // --- View Initialization ---
        val profileImageButton = findViewById<ImageView>(R.id.profile_photo)
        val backBtn = findViewById<ImageView>(R.id.backBtn)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val toggleIcon = findViewById<ImageView>(R.id.ivTogglePassword)
        val etDob = findViewById<EditText>(R.id.etDob)
        val registerBtn = findViewById<Button>(R.id.btnSignUp)
        val emailEditText = findViewById<EditText>(R.id.etEmail)

        // Your XML file has these IDs, so this is correct.
        usernameEditText = findViewById(R.id.etUsername)
        firstNameEditText = findViewById(R.id.etFirstName)
        lastNameEditText = findViewById(R.id.etLastName)

        // ... (The rest of your listeners for profile image, date picker, etc. are fine) ...

        // --- REVISED Firebase Registration and Profile Saving Logic ---
        registerBtn.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val username = usernameEditText.text.toString().trim()
            // 🔑 FIXED: Get text from the correct first and last name fields
            val firstName = firstNameEditText.text.toString().trim()
            val lastName = lastNameEditText.text.toString().trim()

            // 🔑 FIXED: Updated validation to check first and last name
            if (email.isEmpty() || password.isEmpty() || username.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
                Toast.makeText(this, "Please fill all required profile fields.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // 1. Create User with Firebase Auth
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(baseContext, "Authentication successful. Saving profile...", Toast.LENGTH_SHORT).show()

                        val user = auth.currentUser

                        if (user != null) {
                            // 🔑 FIXED: Combine first and last name and pass to the save function
                            val fullName = "$firstName $lastName"
                            saveProfileToDatabase(user.uid, email, username, fullName)
                        } else {
                            // Fallback
                            startActivity(Intent(this, HomeActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            })
                            finish()
                        }

                    } else {
                        Toast.makeText(baseContext, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    // 🔑 FIXED: Updated function to accept all necessary parameters
    private fun saveProfileToDatabase(uid: String, email: String, username: String, fullName: String) {
        val defaultImageUrl = "https://firebasestorage.googleapis.com/v0/b/socially-12a42.appspot.com/o/profile_images%2Fdefault_user_icon.png?alt=media&token=d2ff861f-2565-4423-8637-79736417937a"

        val userMap = HashMap<String, Any>()
        userMap["uid"] = uid // It's good practice to also store the uid in the record
        userMap["email"] = email
        userMap["username"] = username
        userMap["name"] = fullName // Saving the combined full name
        userMap["profileImageUrl"] = defaultImageUrl
        userMap["dateOfBirth"] = findViewById<EditText>(R.id.etDob).text.toString()
        userMap["isOnline"] = true
        userMap["posts"] = 0
        userMap["followers"] = 0
        userMap["following"] = 0

        database.getReference("users").child(uid).setValue(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile setup complete!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save profile data: ${e.message}", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
    }
}