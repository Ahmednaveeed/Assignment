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

    // Variables for other profile fields
    private lateinit var nameEditText: EditText
    private lateinit var usernameEditText: EditText

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

        // Initialize username
        usernameEditText = findViewById<EditText>(R.id.etUsername)

        // Disable keyboard input for Date of Birth
        etDob.isFocusable = false
        etDob.isClickable = true

        var isPasswordVisible = false

        profileImageButton.setOnClickListener {
            Toast.makeText(this, "Profile picture upload feature is currently disabled.", Toast.LENGTH_SHORT).show()
        }

        // --- Date of Birth Picker Click Listener ---
        etDob.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                    etDob.setText(formattedDate)
                },
                year, month, day
            )
            datePicker.show()
        }

        // --- Back Button Click Listener  ---
        backBtn.setOnClickListener {
            val intent = Intent(this, savedacc::class.java)
            finish()
        }

        // --- Password Toggle Click Listener ---
        toggleIcon.setOnClickListener {
            if (isPasswordVisible) {
                // Hide Password
                passwordEditText.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleIcon.setImageResource(R.drawable.view)
            } else {
                // Show Password
                passwordEditText.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                toggleIcon.setImageResource(R.drawable.noview)
            }
            // Move cursor to end
            passwordEditText.setSelection(passwordEditText.text.length)
            isPasswordVisible = !isPasswordVisible
        }

        // --- REVISED Firebase Registration and Profile Saving Logic ---
        registerBtn.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val username = usernameEditText.text.toString().trim()
            val name = nameEditText.text.toString().trim() // Getting name here

            // Input Validation (Simplified: removed filePath check)
            if (email.isEmpty() || password.isEmpty() || username.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Please fill all required profile fields.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // 1. Create User with Firebase Auth
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(baseContext, "Authentication successful. Saving profile...", Toast.LENGTH_SHORT).show()

                        val user = auth.currentUser

                        // 2. CRITICAL CHANGE: Directly call the Realtime DB save function
                        if (user != null) {
                            saveProfileToDatabase(user.uid, email)
                        } else {
                            // Fallback
                            startActivity(Intent(this, HomeActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            })
                            finish()
                        }

                    } else {
                        // If sign in fails
                        Toast.makeText(baseContext, "Authentication failed: ${task.exception?.message}",
                            Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
    // --- REVISED Save Profile Data to Realtime Database and Navigate ---
    private fun saveProfileToDatabase(uid: String, email: String) {
        val username = usernameEditText.text.toString()
        val name = nameEditText.text.toString()

        // Using a static URL since Firebase Storage is removed
        val defaultImageUrl = "https://default-image-url.com/profile.png"

        val userMap = HashMap<String, Any>()
        userMap["email"] = email
        userMap["username"] = username
        userMap["name"] = name
        userMap["profileImageUrl"] = defaultImageUrl
        userMap["dateOfBirth"] = findViewById<EditText>(R.id.etDob).text.toString()
        userMap["isOnline"] = true
        userMap["posts"] = 0
        userMap["followers"] = 0
        userMap["following"] = 0

        // Save to Realtime Database under /users/{UID}
        database.getReference("users").child(uid).setValue(userMap)
            .addOnSuccessListener {
                // Profile saved successfully. Navigate to Home.
                Toast.makeText(this, "Profile setup complete!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save profile data: ${e.message}", Toast.LENGTH_LONG).show()
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
    }
}