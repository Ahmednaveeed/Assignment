package com.ahmed.i221132

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView

class StoryAdapter(
    private val stories: List<Story>,
    private val context: Context,
    private val onStoryClick: (Int) -> Unit
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
        holder.storyImage.setImageResource(story.profileImage)
        holder.storyText.text = story.username

        holder.itemView.setOnClickListener {
            onStoryClick(position)
        }
    }

    override fun getItemCount(): Int = stories.size
}