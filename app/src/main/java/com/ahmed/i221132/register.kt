package com.ahmed.i221132

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import java.io.ByteArrayOutputStream
import java.util.Calendar

class register : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    // --- Views needed across functions ---
    private lateinit var profileImageButton: ImageView
    private lateinit var usernameEditText: EditText
    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var etDob: EditText

    // --- Launcher for picking an image from the gallery ---
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // --- Firebase Initialization ---
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // --- View Initialization ---
        profileImageButton = findViewById(R.id.profile_photo)
        val backBtn = findViewById<ImageView>(R.id.backBtn)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val toggleIcon = findViewById<ImageView>(R.id.ivTogglePassword)
        etDob = findViewById(R.id.etDob)
        val registerBtn = findViewById<Button>(R.id.btnSignUp)
        val emailEditText = findViewById<EditText>(R.id.etEmail)
        usernameEditText = findViewById(R.id.etUsername)
        firstNameEditText = findViewById(R.id.etFirstName)
        lastNameEditText = findViewById(R.id.etLastName)

        // --- Initialize the image picker launcher ---
        imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val imageUri = data?.data
                if (imageUri != null) {
                    // Set the selected image as a preview in the ImageView
                    profileImageButton.setImageURI(imageUri)
                }
            }
        }

        // --- Listener to open the gallery ---
        profileImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            imagePickerLauncher.launch(intent)
        }

        // --- Date of Birth Picker Logic ---
        etDob.isFocusable = false
        etDob.isClickable = true
        etDob.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                etDob.setText(formattedDate)
            }, year, month, day).show()
        }

        // --- Back Button Logic ---
        backBtn.setOnClickListener {
            finish() // Closes the current activity and goes back
        }

        // --- Password Toggle Logic ---
        var isPasswordVisible = false
        toggleIcon.setOnClickListener {
            if (isPasswordVisible) {
                // Hide Password
                passwordEditText.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleIcon.setImageResource(R.drawable.view) // Your icon for 'hidden'
            } else {
                // Show Password
                passwordEditText.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                toggleIcon.setImageResource(R.drawable.noview) // Your icon for 'visible'
            }
            passwordEditText.setSelection(passwordEditText.text.length) // Move cursor to end
            isPasswordVisible = !isPasswordVisible
        }

        // --- Registration Logic ---
        registerBtn.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val username = usernameEditText.text.toString().trim()
            val firstName = firstNameEditText.text.toString().trim()
            val lastName = lastNameEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || username.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(baseContext, "Authentication successful. Saving profile...", Toast.LENGTH_SHORT).show()
                        val user = auth.currentUser
                        if (user != null) {
                            val fullName = "$firstName $lastName"
                            saveProfileToDatabase(user.uid, email, username, fullName)
                        }
                    } else {
                        Toast.makeText(baseContext, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun encodeImageToBase64(): String? {
        return try {
            val drawable = profileImageButton.drawable ?: return null
            val bitmap = (drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            // Compress to reduce size and prevent database overload
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
            val byteArray = baos.toByteArray()
            Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null // Return null if there's no image or an error occurs
        }
    }

    private fun saveProfileToDatabase(uid: String, email: String, username: String, fullName: String) {
        val profileImageBase64 = encodeImageToBase64() ?: ""

        val userMap = HashMap<String, Any>()
        userMap["uid"] = uid
        userMap["email"] = email
        userMap["username"] = username
        userMap["name"] = fullName
        userMap["profileImageBase64"] = profileImageBase64
        userMap["dateOfBirth"] = etDob.text.toString()
        userMap["isOnline"] = false
        userMap["lastSeen"] = ServerValue.TIMESTAMP
        userMap["posts"] = 0
        userMap["followers"] = 0
        userMap["following"] = 0
        userMap["pendingAlertsCount"] = 0

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
            }
    }
}