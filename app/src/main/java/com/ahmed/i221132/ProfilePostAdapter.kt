package com.ahmed.i221132

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class ProfilePostAdapter(
    private val posts: List<ProfilePost>
) : RecyclerView.Adapter<ProfilePostAdapter.ProfilePostViewHolder>() {

    class ProfilePostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // The itemView itself is the ImageView, as defined in item_profile_post.xml
        val postImage: ImageView = itemView as ImageView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfilePostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profile_post, parent, false)
        return ProfilePostViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfilePostViewHolder, position: Int) {
        val post = posts[position]
        // Set the image resource on the ImageView from the ViewHolder
        holder.postImage.setImageResource(post.imageRes)
        // Add click later: holder.itemView.setOnClickListener { /* open post detail */ }
    }

    override fun getItemCount(): Int = posts.size
}
