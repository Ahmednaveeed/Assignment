package com.ahmed.i221132

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
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
    private val currentUserId: String // 🔑 Pass the user's ID
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
            holder.highlightImage.visibility = View.GONE
            holder.newHighlightContainer.visibility = View.VISIBLE
            holder.itemView.setOnClickListener {
                context.startActivity(Intent(context, AddHighlightActivity::class.java))
            }
        } else {
            holder.highlightImage.visibility = View.VISIBLE
            holder.newHighlightContainer.visibility = View.GONE
            // Decode the cover image
            try {
                val imageBytes = Base64.decode(highlight.coverImageBase64, Base64.DEFAULT)
                holder.highlightImage.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))
            } catch (e: Exception) { /* Use placeholder */ }

            holder.itemView.setOnClickListener {
                val intent = Intent(context, HighlightViewActivity::class.java)
                intent.putExtra("USER_ID", currentUserId)
                intent.putExtra("HIGHLIGHT_ID", highlight.highlightId)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = highlights.size
}