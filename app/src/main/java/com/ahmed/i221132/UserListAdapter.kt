package com.ahmed.i221132

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ahmed.i221132.R
import de.hdodenhof.circleimageview.CircleImageView

class UserListAdapter(
    private val userList: List<User>,
    private val context: Context
) : RecyclerView.Adapter<UserListAdapter.UserListViewHolder>() {

    class UserListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // NOTE: These IDs must exist in item_user_list.xml
        val profileImage: CircleImageView = itemView.findViewById(R.id.user_list_profile_image)
        val usernameText: TextView = itemView.findViewById(R.id.user_list_username)
        val nameText: TextView = itemView.findViewById(R.id.user_list_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserListViewHolder {
        // NOTE: item_user_list.xml must exist in your res/layout folder
        val view = LayoutInflater.from(context).inflate(R.layout.item_user_list, parent, false)
        return UserListViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserListViewHolder, position: Int) {
        val user = userList[position]

        holder.usernameText.text = user.username
        holder.nameText.text = user.name

        // Load image from Base64 string
        user.profileImageBase64?.let { base64 ->
            if (base64.isNotEmpty()) {
                try {
                    val imageBytes = Base64.decode(base64, Base64.DEFAULT)
                    val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    holder.profileImage.setImageBitmap(decodedImage)
                } catch (e: Exception) {
                    holder.profileImage.setImageResource(R.drawable.user)
                }
            } else {
                holder.profileImage.setImageResource(R.drawable.user)
            }
        }

        // Clicking a user in the list should open their friend profile
        holder.itemView.setOnClickListener {
            val intent = Intent(context, friendprofile::class.java)
            intent.putExtra("TARGET_USER_UID", user.uid)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = userList.size
}