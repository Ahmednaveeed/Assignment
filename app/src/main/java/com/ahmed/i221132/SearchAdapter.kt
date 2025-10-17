// SearchAdapter.kt
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

class SearchAdapter(
    private val context: Context,
    private val userList: List<User>,
    private val onItemClick: (User) -> Unit
) : RecyclerView.Adapter<SearchAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: CircleImageView = itemView.findViewById(R.id.user_search_profile_image)
        val usernameText: TextView = itemView.findViewById(R.id.user_search_username)
        val fullNameText: TextView = itemView.findViewById(R.id.user_search_full_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        // Inflate the layout for a single search result item
        val view = LayoutInflater.from(context).inflate(R.layout.item_user_search, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]

        holder.usernameText.text = user.username
        // Use the 'name' field for the full name as per your Firebase structure
        holder.fullNameText.text = user.name

        // Load profile picture from Base64 string
        val base64 = user.profileImageBase64
        if (!base64.isNullOrEmpty()) {
            try {
                val imageBytes = Base64.decode(base64, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.profileImage.setImageBitmap(decodedImage)
            } catch (e: Exception) {
                // Set default image if decoding fails
                holder.profileImage.setImageResource(R.drawable.user)
            }
        } else {
            // Set default image if no Base64 string exists
            holder.profileImage.setImageResource(R.drawable.user)
        }

        // Handle click event to open the profile
        holder.itemView.setOnClickListener { onItemClick(user) }
    }

    override fun getItemCount(): Int = userList.size
}