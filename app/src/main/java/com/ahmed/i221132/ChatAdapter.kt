package com.ahmed.i221132

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(
    private val messageList: List<ChatMessage>,
    private val currentUserId: String,
    private val onDelete: (ChatMessage) -> Unit, // Callback for delete
    private val onEdit: (ChatMessage) -> Unit    // Callback for edit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // --- Define constants for all four view types ---
    private val VIEW_TYPE_SENT_TEXT = 1
    private val VIEW_TYPE_RECEIVED_TEXT = 2
    private val VIEW_TYPE_SENT_IMAGE = 3
    private val VIEW_TYPE_RECEIVED_IMAGE = 4

    // --- Two separate ViewHolders for text and image messages ---
    class TextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.message_text)
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageImage: ImageView = itemView.findViewById(R.id.message_image)
    }

    // --- This function determines which layout to use ---
    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]
        val isSent = message.senderId == currentUserId
        val isText = !message.text.isNullOrEmpty()

        return when {
            isSent && isText -> VIEW_TYPE_SENT_TEXT
            !isSent && isText -> VIEW_TYPE_RECEIVED_TEXT
            isSent && !isText -> VIEW_TYPE_SENT_IMAGE
            else -> VIEW_TYPE_RECEIVED_IMAGE
        }
    }

    // --- This function inflates the correct layout ---
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SENT_TEXT -> TextViewHolder(inflater.inflate(R.layout.item_sent_message, parent, false))
            VIEW_TYPE_RECEIVED_TEXT -> TextViewHolder(inflater.inflate(R.layout.item_received_message, parent, false))
            VIEW_TYPE_SENT_IMAGE -> ImageViewHolder(inflater.inflate(R.layout.item_sent_image, parent, false))
            else -> ImageViewHolder(inflater.inflate(R.layout.item_received_image, parent, false))
        }
    }

    // --- This function binds the data and sets listeners ---
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messageList[position]
        val fiveMinutesInMillis = 5 * 60 * 1000 // 5 minutes

        when (holder.itemViewType) {
            VIEW_TYPE_SENT_TEXT, VIEW_TYPE_RECEIVED_TEXT -> {
                val textHolder = holder as TextViewHolder
                textHolder.messageText.text = message.text
            }
            VIEW_TYPE_SENT_IMAGE, VIEW_TYPE_RECEIVED_IMAGE -> {
                val imageHolder = holder as ImageViewHolder
                try {
                    val imageBytes = Base64.decode(message.imageBase64, Base64.DEFAULT)
                    val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    imageHolder.messageImage.setImageBitmap(decodedImage)
                } catch (e: Exception) {
                    imageHolder.messageImage.setImageResource(R.drawable.error)
                }
            }
        }

        // --- Add long-press listener for editing and deleting ---
        holder.itemView.setOnLongClickListener {
            // Only allow actions on messages sent by the current user within the time limit
            if (message.senderId == currentUserId && (System.currentTimeMillis() - message.timestamp) < fiveMinutesInMillis) {
                // Only allow editing for text messages
                if (!message.text.isNullOrEmpty()) {
                    onEdit(message)
                } else {
                    // For image messages, only allow deletion
                    onDelete(message)
                }
                true // Consume the long click
            } else {
                false
            }
        }
    }

    override fun getItemCount(): Int = messageList.size
}