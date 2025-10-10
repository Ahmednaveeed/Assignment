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
import java.util.Calendar

class register : AppCompatActivity() {
    @SuppressLint("WrongViewCast")
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        auth = FirebaseAuth.getInstance()

        val backBtn = findViewById<ImageView>(R.id.backBtn)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val toggleIcon = findViewById<ImageView>(R.id.ivTogglePassword)
        val etDob = findViewById<EditText>(R.id.etDob)
        val registerBtn = findViewById<Button>(R.id.btnSignUp)
        val emailEditText = findViewById<EditText>(R.id.etEmail)

        // Disable keyboard input
        etDob.isFocusable = false
        etDob.isClickable = true

        var isPasswordVisible = false

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

        backBtn.setOnClickListener {
            val intent = Intent(this, savedacc::class.java)
            finish()
        }

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

        // firebase registration
        registerBtn.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Registration success. User is automatically logged in.
                        Toast.makeText(baseContext, "Registration Successful!", Toast.LENGTH_SHORT).show()


                        val intent = Intent(this, HomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        // If sign in fails
                        Toast.makeText(baseContext, "Authentication failed: ${task.exception?.message}",
                            Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}