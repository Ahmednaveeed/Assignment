package com.ahmed.i221132


import Highlight
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView
import com.ahmed.i221132.R
// 🚀 THE CRITICAL FIX: Explicitly import your application's R class



/**
 * Adapter for displaying user story highlights.
 */
class HighlightAdapter(
    private val highlights: List<Highlight>,
    private val context: Context
) : RecyclerView.Adapter<HighlightAdapter.HighlightViewHolder>() {

    class HighlightViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // These IDs are now correctly resolved due to the import above
        val highlightImage: CircleImageView = itemView.findViewById(R.id.highlight_image)
        val highlightTitle: TextView = itemView.findViewById(R.id.highlight_text)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HighlightViewHolder {
        // NOTE: item_highlight.xml must exist in your res/layout folder
        val view = LayoutInflater.from(context).inflate(R.layout.item_highlight, parent, false)
        return HighlightViewHolder(view)
    }

    override fun onBindViewHolder(holder: HighlightViewHolder, position: Int) {
        val highlight = highlights[position]
        holder.highlightTitle.text = highlight.title

        // Load image from Base64 string
        // The property is assumed to be 'coverImageBase64' based on your Firebase structure
        highlight.coverImageBase64.let { base64 ->
            if (base64.isNotEmpty()) {
                try {
                    val imageBytes = Base64.decode(base64, Base64.DEFAULT)
                    val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    holder.highlightImage.setImageBitmap(decodedImage)
                } catch (e: Exception) {
                    // Fallback to a generic user drawable if decoding fails
                    holder.highlightImage.setImageResource(R.drawable.user)
                }
            } else {
                holder.highlightImage.setImageResource(R.drawable.user)
            }
        }

        holder.itemView.setOnClickListener {
            Toast.makeText(context, "Viewing highlight: ${highlight.title}", Toast.LENGTH_SHORT).show()
            // Implement navigation to your HighlightViewActivity
        }
    }

    override fun getItemCount(): Int = highlights.size
}