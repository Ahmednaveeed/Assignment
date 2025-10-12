package com.ahmed.i221132

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView

class PostAdapter(
    private val posts: List<Post>,
    private val context: Context
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    // The ViewHolder
    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: CircleImageView = itemView.findViewById(R.id.post_profile_image)
        val usernameText: TextView = itemView.findViewById(R.id.post_username_text)
        val locationText: TextView = itemView.findViewById(R.id.post_location_text)
        val postImageView: ImageView = itemView.findViewById(R.id.post_image_view)
        val likedText: TextView = itemView.findViewById(R.id.post_liked_text)
        val captionText: TextView = itemView.findViewById(R.id.post_caption_text)
        // You can keep the other views for future features like likes, comments, etc.
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }


    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        // 1. Load the main post image using Coil
        holder.postImageView.load(post.imageUrl) {
            placeholder(R.drawable.default_post_placeholder) // Optional: a placeholder image
            error(R.drawable.error_placeholder) // Optional: an error image
        }

        // 2. Bind the simple text data
        holder.captionText.text = post.caption
        holder.locationText.text = post.location
        holder.likedText.text = "${post.likes} likes" // Format the likes count

        // 3. Fetch the user's info (username and profile pic) from the database
        //    This is necessary because the Post object only stores the userId.
        fetchUserInfo(post.userId, holder.profileImage, holder.usernameText)
    }

    override fun getItemCount(): Int = posts.size

    // This function fetches the username and profile image for the user who made the post.
    private fun fetchUserInfo(userId: String, profileImageView: CircleImageView, usernameTextView: TextView) {
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

        userRef.get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                val username = dataSnapshot.child("username").getValue(String::class.java)
                val profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String::class.java)

                usernameTextView.text = username
                profileImageView.load(profileImageUrl) {
                    placeholder(R.drawable.user)
                    error(R.drawable.user)
                }
            }
        }.addOnFailureListener {
            // Handle error, e.g., user not found
            usernameTextView.text = "Unknown User"
        }
    }
}