package com.ahmed.i221132

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class ProfilePostAdapter(
    private val posts: List<ProfilePost>,
    private val context: Context // 👈 Added context
) : RecyclerView.Adapter<ProfilePostAdapter.ProfilePostViewHolder>() {

    class ProfilePostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postImage: ImageView = itemView as ImageView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfilePostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profile_post, parent, false)
        return ProfilePostViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfilePostViewHolder, position: Int) {
        val post = posts[position]
        // 🔑 Decode the Base64 string to display the image
        try {
            val imageBytes = Base64.decode(post.imageBase64, Base64.DEFAULT)
            val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            holder.postImage.setImageBitmap(decodedImage)
        } catch (e: Exception) {
            // Set a fallback image if decoding fails
            holder.postImage.setImageResource(R.drawable.error)
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int = posts.size
}