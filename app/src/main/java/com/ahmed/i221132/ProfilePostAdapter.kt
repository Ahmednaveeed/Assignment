package com.ahmed.i221132

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import android.widget.Toast
import com.ahmed.i221132.R

/**
 * Adapter for displaying user posts in a 3-column grid.
 * NOTE: Requires item_profile_post.xml layout file with R.id.post_grid_image ImageView.
 */
class ProfilePostAdapter(
    private val posts: List<Post>,
    private val context: Context
) : RecyclerView.Adapter<ProfilePostAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postImage: ImageView = itemView.findViewById(R.id.post_grid_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profile_post, parent, false)

        // CRITICAL FOR 3-COLUMN GRID: Dynamically calculate and set width/height
        val layoutParams = view.layoutParams
        val columnWidth = parent.measuredWidth / 3

        layoutParams.width = columnWidth
        layoutParams.height = columnWidth // Ensures posts are square
        view.layoutParams = layoutParams

        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfilePostAdapter.PostViewHolder, position: Int) {
        val post = posts[position]

        // Load image from Base64 string
        if (post.imageBase64.isNotEmpty()) {
            try {
                val imageBytes = Base64.decode(post.imageBase64, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.postImage.setImageBitmap(decodedImage)
            } catch (e: Exception) {
//                holder.postImage.setImageResource(R.drawable.default_post)
            }
        } else {
//            holder.postImage.setImageResource(R.drawable.default_post)
        }

        holder.itemView.setOnClickListener {
            Toast.makeText(context, "Viewing Post: ${post.caption.take(15)}...", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = posts.size
}
