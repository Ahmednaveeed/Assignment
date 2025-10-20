package com.ahmed.i221132

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SociallyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // The notification will usually contain data payloads for handling specific types of alerts
        remoteMessage.notification?.let {
            // Display the notification content from the FCM payload
            sendNotification(it.title ?: "New Alert", it.body ?: "Check your Socially app.")
        }
    }

    override fun onNewToken(token: String) {
        // This is called when the device receives a new token (e.g., after initial install)
        super.onNewToken(token)
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (token != null) {
            // Save the token to the user's profile in the database
            FirebaseDatabase.getInstance().getReference("users").child(uid)
                .child("fcmToken").setValue(token)
        }
    }

    private fun sendNotification(title: String, messageBody: String) {
        val channelId = "socially_alerts"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.notification) // Use a notification icon you have
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create the NotificationChannel for Android 8.0 (Oreo) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Socially Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Send the notification
        notificationManager.notify(0, notificationBuilder.build())
    }
}
