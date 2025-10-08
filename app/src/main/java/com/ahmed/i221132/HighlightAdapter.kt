package com.ahmed.i221132

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView

class HighlightAdapter(
    private val highlights: List<Highlight>,
    private val context: Context
) : RecyclerView.Adapter<HighlightAdapter.HighlightViewHolder>() {

    class HighlightViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val highlightImage: CircleImageView = itemView.findViewById(R.id.highlight_image)
        val highlightText: TextView = itemView.findViewById(R.id.highlight_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HighlightViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_highlight, parent, false)
        return HighlightViewHolder(view)
    }

    override fun onBindViewHolder(holder: HighlightViewHolder, position: Int) {
        val highlight = highlights[position]
        holder.highlightImage.setImageResource(highlight.imageResource)
        holder.highlightText.text = highlight.title
    }

    override fun getItemCount(): Int = highlights.size
}