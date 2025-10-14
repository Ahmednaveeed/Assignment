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
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView

class PostAdapter(
    private val posts: List<Post>,
    private val context: Context
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: CircleImageView = itemView.findViewById(R.id.post_profile_image)
        val usernameText: TextView = itemView.findViewById(R.id.post_username_text)
        val locationText: TextView = itemView.findViewById(R.id.post_location_text)
        val postImageView: ImageView = itemView.findViewById(R.id.post_image_view)
        val likedText: TextView = itemView.findViewById(R.id.post_liked_text)
        val captionText: TextView = itemView.findViewById(R.id.post_caption_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        // 1. Decode the Base64 string for the main post image
        try {
            if (post.imageBase64.isNotEmpty()) {
                val imageBytes = Base64.decode(post.imageBase64, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.postImageView.setImageBitmap(decodedImage)
            } else {
                holder.postImageView.setImageResource(R.drawable.loading)
            }
        } catch (e: Exception) {
            holder.postImageView.setImageResource(R.drawable.error)
            e.printStackTrace()
        }

        // 2. Set simple text fields
        holder.locationText.text = post.location
        holder.likedText.text = "${post.likes} likes"

        // 3. Fetch the user's info to populate the header and caption
        fetchUserInfo(post, holder)
    }

    override fun getItemCount(): Int = posts.size

    private fun fetchUserInfo(post: Post, holder: PostViewHolder) {
        if (post.userId.isEmpty()) return

        val userRef = FirebaseDatabase.getInstance().getReference("users").child(post.userId)

        userRef.get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                val username = dataSnapshot.child("username").getValue(String::class.java)

                // 🔑 CHANGE: Fetched 'profileImageBase64'
                val profileImageBase64 = dataSnapshot.child("profileImageBase64").getValue(String::class.java)

                // Update post header username
                holder.usernameText.text = username

                // 🔑 CHANGE: Replaced Coil with Base64 decoding for the profile picture
                if (!profileImageBase64.isNullOrEmpty()) {
                    try {
                        val imageBytes = Base64.decode(profileImageBase64, Base64.DEFAULT)
                        val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        holder.profileImage.setImageBitmap(decodedImage)
                    } catch (e: Exception) {
                        holder.profileImage.setImageResource(R.drawable.user)
                    }
                } else {
                    holder.profileImage.setImageResource(R.drawable.user)
                }

                // Format and set the caption with the username
                if (username != null && post.caption.isNotEmpty()) {
                    val builder = SpannableStringBuilder()
                    builder.append(username, StyleSpan(android.graphics.Typeface.BOLD), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    builder.append(" ${post.caption}")
                    holder.captionText.text = builder
                } else {
                    holder.captionText.text = post.caption
                }
            }
        }.addOnFailureListener {
            holder.usernameText.text = "Unknown User"
            holder.captionText.text = post.caption
        }
    }
}