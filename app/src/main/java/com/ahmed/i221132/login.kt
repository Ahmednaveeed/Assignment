package com.ahmed.i221132

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val loginBtn = findViewById<Button>(R.id.loginBtn)
        val signUp = findViewById<TextView>(R.id.signUp)
        val backBtn = findViewById<ImageView>(R.id.backBtn)
        val emailEditText = findViewById<EditText>(R.id.etEmail)
        val passwordEditText = findViewById<EditText>(R.id.etPassword)

        val prefilledEmail = intent.getStringExtra("PREFILL_EMAIL")
        if (!prefilledEmail.isNullOrEmpty()) {
            emailEditText.setText(prefilledEmail)
        }

        loginBtn.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) { /* ... error toast ... */ return@setOnClickListener }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        task.result?.user?.let { user ->
                            // 🔑 Fetch and save account info on successful login
                            fetchAndSaveAccountInfo(user.uid, email)
                        } ?: navigateToHome() // Fallback if user is null
                    } else { /* ... error toast ... */ }
                }
        }
        signUp.setOnClickListener { startActivity(Intent(this, register::class.java)) }
        backBtn.setOnClickListener { finish() } // Use finish() instead of starting savedacc
    }

    // Fetches user details and saves them as the 'last account'
    private fun fetchAndSaveAccountInfo(uid: String, email: String) {
        database.getReference("users").child(uid).get().addOnSuccessListener { snapshot ->
            val username = snapshot.child("username").getValue(String::class.java) ?: email
            val profileImageBase64 = snapshot.child("profileImageBase64").getValue(String::class.java)
            val accountInfo = SavedAccountInfo(uid, email, username, profileImageBase64)
            // 🔑 Save using the new helper
            AccountStorageHelper.saveLastAccount(this, accountInfo)
            navigateToHome()
        }.addOnFailureListener {
            // Save basic info even if fetch fails
            val accountInfo = SavedAccountInfo(uid, email, email, null)
            AccountStorageHelper.saveLastAccount(this, accountInfo)
            navigateToHome()
        }
    }

    private fun navigateToHome() {
        Toast.makeText(baseContext, "Login Successful!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}