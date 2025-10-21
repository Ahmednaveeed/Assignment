package com.ahmed.i221132

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ahmed.i221132.R
import de.hdodenhof.circleimageview.CircleImageView

class FollowRequestAdapter(
    private val requests: List<Request>,
    private val context: Context,
    private val onAccept: (String) -> Unit, // Callback for accepting request (takes requesting UID)
    private val onReject: (String) -> Unit // Callback for rejecting request (takes requesting UID)
) : RecyclerView.Adapter<FollowRequestAdapter.RequestViewHolder>() {

    class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: CircleImageView = itemView.findViewById(R.id.request_profile_image)
        val usernameText: TextView = itemView.findViewById(R.id.request_username)
        val nameText: TextView = itemView.findViewById(R.id.request_name)
        val acceptButton: Button = itemView.findViewById(R.id.accept_button)
        val rejectButton: Button = itemView.findViewById(R.id.reject_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        // NOTE: You need a layout file named 'item_follow_request.xml' with the IDs above.
        val view = LayoutInflater.from(context).inflate(R.layout.item_follow_request, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requests[position]

        holder.usernameText.text = request.username
        holder.nameText.text = request.name

        // Load image from Base64 string
        request.profileImageBase64?.let { base64 ->
            if (base64.isNotEmpty()) {
                try {
                    val imageBytes = Base64.decode(base64, Base64.DEFAULT)
                    val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    holder.profileImage.setImageBitmap(decodedImage)
                } catch (e: Exception) {
                    holder.profileImage.setImageResource(R.drawable.user) // Fallback
                }
            } else {
                holder.profileImage.setImageResource(R.drawable.user)
            }
        }

        // Setup button listeners to call the defined callbacks
        holder.acceptButton.setOnClickListener { onAccept(request.uid) }
        holder.rejectButton.setOnClickListener { onReject(request.uid) }

        // Optional: Clicking the profile row navigates to the profile screen
        holder.itemView.setOnClickListener {
            // Implement navigation to friendprofile.kt using request.uid
        }
    }

    override fun getItemCount(): Int = requests.size
}