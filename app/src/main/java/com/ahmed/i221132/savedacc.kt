package com.ahmed.i221132

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView

class savedacc : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var lastSavedAccount: SavedAccountInfo? = null // Holds the info loaded from storage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_savedacc)
        auth = FirebaseAuth.getInstance()

        val profileContainer = findViewById<LinearLayout>(R.id.profileContainer)
        val profileImage = findViewById<CircleImageView>(R.id.profile_image) // Check ID
        val usernameText = findViewById<TextView>(R.id.username_text) // Check ID
        val loginBtn = findViewById<Button>(R.id.loginBtn)
        val switchAccounts = findViewById<TextView>(R.id.switchAccounts)
        val signUp = findViewById<TextView>(R.id.signUp)

        // 🔑 Load the last saved account info
        lastSavedAccount = AccountStorageHelper.getLastAccount(this)

        if (lastSavedAccount != null) {
            // Display saved user's info
            usernameText.text = lastSavedAccount?.username
            if (!lastSavedAccount?.profileImageBase64.isNullOrEmpty()) {
                try {
                    val imageBytes = Base64.decode(lastSavedAccount?.profileImageBase64, Base64.DEFAULT)
                    profileImage.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))
                } catch (e: Exception) { profileImage.setImageResource(R.drawable.user) }
            } else { profileImage.setImageResource(R.drawable.user) }
            profileContainer.visibility = View.VISIBLE
            loginBtn.visibility = View.VISIBLE
        } else {
            // No saved account, hide profile section
            profileContainer.visibility = View.GONE
            loginBtn.visibility = View.GONE
        }

        // Login button for the SAVED account shown
        loginBtn.setOnClickListener {
            // Check if Firebase *still* has an active session for THIS specific user
            val currentUser = auth.currentUser
            if (currentUser != null && currentUser.uid == lastSavedAccount?.uid) {
                // If yes, the token is likely still valid, go straight home
                Toast.makeText(this, "Welcome back, ${lastSavedAccount?.username}!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                // If no active session or different user, go to login screen and pre-fill email
                val intent = Intent(this, login::class.java)
                intent.putExtra("PREFILL_EMAIL", lastSavedAccount?.email)
                startActivity(intent)
            }
        }

        switchAccounts.setOnClickListener {
            val intent = Intent(this, login::class.java) // Go to login without pre-fill
            startActivity(intent)
        }

        signUp.setOnClickListener {
            val intent = Intent(this, register::class.java)
            startActivity(intent)
        }
    }
}