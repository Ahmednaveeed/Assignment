package com.ahmed.i221132

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class register : AppCompatActivity() {
    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val backBtn = findViewById<ImageView>(R.id.backBtn)

        backBtn.setOnClickListener {
            val intent = Intent(this, savedacc::class.java)
            finish()
        }

        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val toggleIcon = findViewById<ImageView>(R.id.ivTogglePassword)
        val etDob = findViewById<EditText>(R.id.etDob)

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

    }
}