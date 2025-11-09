package com.ahmed.i221132

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView
//import io.agora.rtc.IRtcEngineEventHandler
//import io.agora.rtc.RtcEngine
//import io.agora.rtc.RtcEngine.CHANNEL_PROFILE_COMMUNICATION
//import io.agora.rtc.video.VideoCanvas
import io.agora.rtc2.Constants.CHANNEL_PROFILE_COMMUNICATION
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.video.VideoCanvas


class CallActivity : AppCompatActivity() {

    // 🔑 YOUR ACTUAL AGORA APP ID
    private val AGORA_APP_ID = "144893b496ce4114a08505f7d681e137"

    private lateinit var rtcEngine: RtcEngine
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    private var isMuted = false
    private var isVideoDisabled = false
    private var isCaller = false
    private lateinit var channelName: String
    private lateinit var targetUserId: String
    private lateinit var callType: String

    private val PERMISSION_REQUEST_ID = 22
    // NOTE: These drawables must exist in your res/drawable folder
    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)

    // UI elements
    private lateinit var remoteVideoContainer: FrameLayout
    private lateinit var localVideoContainer: FrameLayout
    private lateinit var endCallButton: ImageView
    private lateinit var muteAudioButton: ImageView
    private lateinit var toggleVideoButton: ImageView
    private lateinit var callStatusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()

        // Get intent data
        channelName = intent.getStringExtra("CHANNEL_NAME") ?: run { finish(); return }
        callType = intent.getStringExtra("CALL_TYPE") ?: run { finish(); return }
        isCaller = intent.getBooleanExtra("IS_CALLER", false)
        // targetUserId is the ID of the person we are signalling to, or the person who called us.
        targetUserId = intent.getStringExtra("CALLER_ID") ?: intent.getStringExtra("TARGET_USER_UID") ?: auth.currentUser?.uid ?: ""

        initViews()

        // 1. Check for permissions before starting call setup
        if (checkSelfPermission(REQUIRED_PERMISSIONS)) {
            // NOTE: initAgoraEngine() will fail without the dependency, but this is the correct structure
            initAgoraEngine()
            setupButtons()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_ID)
        }
    }


    private fun initViews() {
        remoteVideoContainer = findViewById(R.id.remote_video_view_container)
        localVideoContainer = findViewById(R.id.local_video_view_container)
        endCallButton = findViewById(R.id.end_call_button)
        muteAudioButton = findViewById(R.id.mute_audio_button)
        toggleVideoButton = findViewById(R.id.toggle_video_button)
        callStatusText = findViewById(R.id.call_status_text)

        // Hide local video view if it's an audio call
        if (callType == "AUDIO") {
            localVideoContainer.visibility = View.GONE
            toggleVideoButton.visibility = View.GONE
            callStatusText.text = if (isCaller) "Audio Call Ringing..." else "Incoming Audio Call..."
        } else {
            callStatusText.text = if (isCaller) "Video Call Ringing..." else "Incoming Video Call..."
        }
    }

    private fun checkSelfPermission(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_ID) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                initAgoraEngine()
                setupButtons()
            } else {
                Toast.makeText(this, "Permissions are required for calling.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    // --- Agora Engine Setup (Will fail without dependency) ---
    private fun initAgoraEngine() {
        try {
            // NOTE: This RtcEngine.create call will cause a ClassNotFoundException without the dependency
            rtcEngine = RtcEngine.create(baseContext, AGORA_APP_ID, rtcEventHandler)

            rtcEngine.setChannelProfile(CHANNEL_PROFILE_COMMUNICATION)

            if (callType == "VIDEO") {
                rtcEngine.enableVideo()
                rtcEngine.enableAudio()
                setupLocalVideo()
            } else {
                rtcEngine.enableAudio()
            }

            // Join the channel
            rtcEngine.joinChannel(null, channelName, "Extra Info", 0)
        } catch (e: Exception) {
            Toast.makeText(this, "Agora init failed (missing dependency?).", Toast.LENGTH_LONG).show()
            // To prevent crash loop, we can just finish
            finish()
        }
    }

    private fun setupLocalVideo() {
        // NOTE: These methods will fail without the dependency
        val surfaceView = RtcEngine.CreateRendererView(baseContext)
        surfaceView.setZOrderMediaOverlay(true)
        localVideoContainer.addView(surfaceView, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ))
        rtcEngine.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0))
        localVideoContainer.visibility = View.VISIBLE
    }

    private fun setupRemoteVideo(uid: Int) {
        val surfaceView = RtcEngine.CreateRendererView(baseContext)
        remoteVideoContainer.addView(surfaceView, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ))
        rtcEngine.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid))
    }

    // --- Button Handlers ---
    private fun setupButtons() {
        endCallButton.setOnClickListener {
            endCall()
        }

        muteAudioButton.setOnClickListener {
            isMuted = !isMuted
            rtcEngine.muteLocalAudioStream(isMuted)
            // NOTE: Drawables need to exist
            muteAudioButton.setImageResource(if (isMuted) R.drawable.audio_phone else R.drawable.audio_phone)
        }

        toggleVideoButton.setOnClickListener {
            isVideoDisabled = !isVideoDisabled
            rtcEngine.enableLocalVideo(!isVideoDisabled)
            toggleVideoButton.setImageResource(if (isVideoDisabled) R.drawable.video_call else R.drawable.video_call)
        }
    }

    // --- Call Cleanup and Signaling ---
    private fun endCall() {
        // NOTE: This rtcEngine.leaveChannel() will fail without the dependency
        try {
            rtcEngine.leaveChannel()
        } catch (e: Exception) {
            // Handle cleanup even if Agora fails
        }

        // 🚀 FIREBASE CLEANUP (Crucial for signaling)
        // If the caller hangs up, delete the node under the recipient's UID.
        // If the recipient hangs up, delete the node under their own UID.
        val cleanupUid = if (isCaller) targetUserId else auth.currentUser?.uid

        if (cleanupUid != null) {
            firebaseDatabase.getReference("calls").child(cleanupUid).removeValue()
        }
        finish()
    }

    // --- Agora Event Handler (Will fail without dependency) ---
    private val rtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            runOnUiThread {
                callStatusText.text = "Connected: $channel"
            }
        }
        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread {
                callStatusText.text = "User Joined: $uid"
                setupRemoteVideo(uid)

                // 🔑 ADD THESE LINES: Hide the placeholder UI
                findViewById<CircleImageView>(R.id.profile_image_large).visibility = View.GONE
                callStatusText.visibility = View.GONE
            }
        }
        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread {
                endCall()
            }
        }
        override fun onFirstRemoteVideoDecoded(uid: Int, width: Int, height: Int, elapsed: Int) {
            runOnUiThread {
                callStatusText.text = "Video Started"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        RtcEngine.destroy()
    }

//    override fun onBackPressed() {
//        // Prevent accidental back press during a call
//        Toast.makeText(this, "Use the red button to end the call.", Toast.LENGTH_SHORT).show()
//    }
}