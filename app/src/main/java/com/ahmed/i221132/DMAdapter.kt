package com.ahmed.i221132

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DMAdapter(
    private val conversations: List<Conversation>,
    private val context: Context
) : RecyclerView.Adapter<DMAdapter.DMViewHolder>() {

    class DMViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: CircleImageView = itemView.findViewById(R.id.dm_profile_image)
        val usernameText: TextView = itemView.findViewById(R.id.dm_username_text)
        val lastMessageText: TextView = itemView.findViewById(R.id.dm_last_message_text)
        val timestampText: TextView = itemView.findViewById(R.id.dm_timestamp_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DMViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_dm, parent, false)
        return DMViewHolder(view)
    }

    override fun onBindViewHolder(holder: DMViewHolder, position: Int) {
        val conversation = conversations[position]

        holder.usernameText.text = conversation.username
        holder.profileImage.load(conversation.profileImageUrl)
        // 🔑 Display the actual last message and timestamp
        holder.lastMessageText.text = conversation.lastMessage
        holder.timestampText.text = formatTimestamp(conversation.timestamp)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("USER_ID", conversation.uid)
            intent.putExtra("USER_NAME", conversation.username)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = conversations.size

    // Helper function to format the timestamp
    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}