package com.ahmed.i221132

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView

class PostAdapter(
    private val posts: List<Post>,
    private val context: Context
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: CircleImageView = itemView.findViewById(R.id.post_profile_image)
        val usernameText: TextView = itemView.findViewById(R.id.post_username_text)
        val locationText: TextView = itemView.findViewById(R.id.post_location_text)
        val optionsBar: ImageView = itemView.findViewById(R.id.post_options_bar)
        val postImageView: ImageView = itemView.findViewById(R.id.post_image_view)
        val likeImage: ImageView = itemView.findViewById(R.id.post_like_image)
        val commentImage: ImageView = itemView.findViewById(R.id.post_comment_image)
        val shareImage: ImageView = itemView.findViewById(R.id.post_share_image)
        val optionsImage: ImageView = itemView.findViewById(R.id.post_options_image)
        val saveImage: ImageView = itemView.findViewById(R.id.post_save_image)
        val likedText: TextView = itemView.findViewById(R.id.post_liked_text)
        val captionText: TextView = itemView.findViewById(R.id.post_caption_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.profileImage.setImageResource(post.profileImageRes)
        holder.usernameText.text = post.username
        holder.locationText.text = post.location
        holder.postImageView.setImageResource(post.postImageRes)
        holder.likedText.text = post.likedByText
        holder.captionText.text = post.caption
        // Add click listeners here later, e.g., for likes
    }

    override fun getItemCount(): Int = posts.size
}