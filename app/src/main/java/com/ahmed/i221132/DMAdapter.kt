package com.ahmed.i221132

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView

class DMAdapter(
    private val dms: List<DMMessage>,
    private val context: Context
) : RecyclerView.Adapter<DMAdapter.DMViewHolder>() {

    class DMViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: CircleImageView = itemView.findViewById(R.id.dm_profile_image)
        val usernameText: TextView = itemView.findViewById(R.id.dm_username_text)
        val lastMessageText: TextView = itemView.findViewById(R.id.dm_last_message_text)
        val timestampText: TextView = itemView.findViewById(R.id.dm_timestamp_text)
        val cameraImage: ImageView = itemView.findViewById(R.id.dm_camera_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DMViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_dm, parent, false)
        return DMViewHolder(view)
    }

    override fun onBindViewHolder(holder: DMViewHolder, position: Int) {
        val dm = dms[position]
        holder.profileImage.setImageResource(dm.profileImageRes)
        holder.usernameText.text = dm.username
        holder.lastMessageText.text = dm.lastMessage
        holder.timestampText.text = dm.timestamp

        // Click listener to open respective DM chats
        holder.itemView.setOnClickListener {
            when (dm.username) {
                "hammad_yasin" -> context.startActivity(Intent(context, DMhammad::class.java))
                "abdullah_malik309" -> context.startActivity(Intent(context, DMabdullah::class.java))
                "zohaib_shafqat" -> context.startActivity(Intent(context, DMzohaib::class.java))
                "saulehnaveed" -> context.startActivity(Intent(context, DMsauleh::class.java))
                "faizan_naveed" -> context.startActivity(Intent(context, DMfaizan::class.java))
                "umair.asghar" -> context.startActivity(Intent(context, DMumair::class.java))
            }
        }
    }

    override fun getItemCount(): Int = dms.size
}