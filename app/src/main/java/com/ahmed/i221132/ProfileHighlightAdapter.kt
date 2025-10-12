package com.ahmed.i221132

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView

class ProfileHighlightAdapter(
    private val highlights: List<ProfileHighlight>,
    private val context: Context,
    private val onHighlightClick: (Int) -> Unit
) : RecyclerView.Adapter<ProfileHighlightAdapter.ProfileHighlightViewHolder>() {

    class ProfileHighlightViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val highlightImage: CircleImageView = itemView.findViewById(R.id.profile_highlight_image)
        val newHighlightContainer: FrameLayout = itemView.findViewById(R.id.new_highlight_container)
        val highlightText: TextView = itemView.findViewById(R.id.profile_highlight_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileHighlightViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_profile_highlight, parent, false)
        return ProfileHighlightViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileHighlightViewHolder, position: Int) {
        val highlight = highlights[position]
        holder.highlightText.text = highlight.title

        if (highlight.isNewButton) {
            // Show "New" button style
            holder.highlightImage.visibility = View.GONE
            holder.newHighlightContainer.visibility = View.VISIBLE
        } else {
            // Show regular highlight
            holder.highlightImage.visibility = View.VISIBLE
            holder.newHighlightContainer.visibility = View.GONE
            holder.highlightImage.setImageResource(highlight.imageResource)
        }

        holder.itemView.setOnClickListener {
            onHighlightClick(position)
        }
    }

    override fun getItemCount(): Int = highlights.size
}