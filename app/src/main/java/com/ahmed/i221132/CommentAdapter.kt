package com.ahmed.i221132

import android.content.Context
import android.graphics.BitmapFactory
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView

class CommentAdapter(
    private val comments: List<Comment>,
    private val context: Context
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: CircleImageView = itemView.findViewById(R.id.comment_profile_image)
        val commentText: TextView = itemView.findViewById(R.id.comment_text)
        val timestampText: TextView = itemView.findViewById(R.id.comment_timestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.timestampText.text = "Just now" // Can be improved with a time formatting function
        fetchUserInfo(comment.userId, holder, comment.text)
    }

    override fun getItemCount(): Int = comments.size

    private fun fetchUserInfo(userId: String, holder: CommentViewHolder, commentText: String) {
        FirebaseDatabase.getInstance().getReference("users").child(userId).get().addOnSuccessListener {
            val username = it.child("username").getValue(String::class.java)
            val profileImageBase64 = it.child("profileImageBase64").getValue(String::class.java)

            // Set profile image
            if (!profileImageBase64.isNullOrEmpty()) {
                try {
                    val imageBytes = Base64.decode(profileImageBase64, Base64.DEFAULT)
                    holder.profileImage.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))
                } catch (e: Exception) { /* Use default */ }
            }

            // Set formatted comment text
            val builder = SpannableStringBuilder()
            builder.append(username, StyleSpan(android.graphics.Typeface.BOLD), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            builder.append(" $commentText")
            holder.commentText.text = builder
        }
    }
}