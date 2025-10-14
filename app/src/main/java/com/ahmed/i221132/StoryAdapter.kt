package com.ahmed.i221132

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView

class StoryAdapter(
    private val stories: List<Story>,
    private val context: Context,
    private val onStoryClick: (String) -> Unit // 🔑 Changed to pass userId (String)
) : RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

    class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val storyImage: CircleImageView = itemView.findViewById(R.id.story_image)
        val storyText: TextView = itemView.findViewById(R.id.story_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_story, parent, false)
        return StoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = stories[position]

        if (position == 0){
            holder.storyText.text = "Your Story"
        } else{
            holder.storyText.text = story.username
        }

        // 🔑 Decode the user's profile picture
        val imageBase64 = story.profileImageBase64
        if (imageBase64.isNotEmpty()) {
            try {
                val imageBytes = Base64.decode(imageBase64, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.storyImage.setImageBitmap(decodedImage)
            } catch (e: Exception) {
                holder.storyImage.setImageResource(R.drawable.user)
            }
        } else {
            holder.storyImage.setImageResource(R.drawable.user)
        }

        holder.itemView.setOnClickListener {
            onStoryClick(story.uid) // Pass the user's ID
        }
    }

    override fun getItemCount(): Int = stories.size
}