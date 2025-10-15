package com.ahmed.i221132

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.util.UUID

class AddHighlightActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val selectedImageUris = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_highlight)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val highlightNameInput = findViewById<EditText>(R.id.highlight_name_input)
        val selectImagesButton = findViewById<Button>(R.id.select_images_button)
        val createHighlightButton = findViewById<Button>(R.id.create_highlight_button)

        val imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.clipData?.let { clipData ->
                    for (i in 0 until clipData.itemCount) {
                        selectedImageUris.add(clipData.getItemAt(i).uri.toString())
                    }
                } ?: result.data?.data?.let { uri ->
                    selectedImageUris.add(uri.toString())
                }
                Toast.makeText(this, "${selectedImageUris.size} images selected", Toast.LENGTH_SHORT).show()
                // You would update a RecyclerView here to show previews
            }
        }

        selectImagesButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            imagePickerLauncher.launch(intent)
        }

        createHighlightButton.setOnClickListener {
            val highlightName = highlightNameInput.text.toString().trim()
            if (highlightName.isEmpty() || selectedImageUris.isEmpty()) {
                Toast.makeText(this, "Please provide a name and select at least one image.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            createHighlight(highlightName)
        }
    }

    private fun createHighlight(title: String) {
        val uid = auth.currentUser?.uid ?: return
        val highlightId = database.reference.push().key ?: UUID.randomUUID().toString()

        val highlightItems = mutableMapOf<String, Any>()
        val coverImageBase64 = encodeUriToBase64(selectedImageUris.first()) // Use the first image as cover

        if (coverImageBase64 == null) {
            Toast.makeText(this, "Failed to process cover image.", Toast.LENGTH_SHORT).show()
            return
        }

        selectedImageUris.forEach { uriString ->
            val imageBase64 = encodeUriToBase64(uriString)
            if (imageBase64 != null) {
                val itemId = database.reference.push().key!!
                highlightItems[itemId] = mapOf(
                    "imageBase64" to imageBase64,
                    "timestamp" to System.currentTimeMillis()
                )
            }
        }

        val highlightData = mapOf(
            "highlightId" to highlightId,
            "title" to title,
            "coverImageBase64" to coverImageBase64,
            "items" to highlightItems
        )

        database.getReference("highlights").child(uid).child(highlightId).setValue(highlightData)
            .addOnSuccessListener {
                Toast.makeText(this, "Highlight created!", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun encodeUriToBase64(uriString: String): String? {
        return try {
            val imageUri = android.net.Uri.parse(uriString)
            val inputStream = contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
            Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
        } catch (e: Exception) {
            null
        }
    }
}