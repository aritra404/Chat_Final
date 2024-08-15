package com.example.chatproject

import Message
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageBox: EditText
    private lateinit var sendButton: ImageView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var mDbRef: DatabaseReference
    private lateinit var userNameTextView: TextView
    private lateinit var profilePictureImageView: ImageView

    var receiverRoom: String? = null
    var senderRoom: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Retrieve the name, UID, and profile image URL from the intent
        val name = intent.getStringExtra("name")
        val profileImageUrl = intent.getStringExtra("profileImageUrl")

        // Find the views
        userNameTextView = findViewById(R.id.userNameTextView)
        profilePictureImageView = findViewById(R.id.profileImageView)

        // Set the user name and profile picture
        userNameTextView.text = name
        Glide.with(this)
            .load(profileImageUrl)
            .placeholder(R.drawable.img) // Placeholder image
            .into(profilePictureImageView)

        val receiverUid = intent.getStringExtra("uid")
        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
        mDbRef = FirebaseDatabase.getInstance().reference

        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid

        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageBox = findViewById(R.id.messageBox)
        sendButton = findViewById(R.id.sendButton)
        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList)
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = messageAdapter

        mDbRef.child("chats").child(senderRoom!!).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()

                    for (postSnapshot in snapshot.children) {
                        val message = postSnapshot.getValue(Message::class.java)
                        message?.let { messageList.add(it) }
                    }
                    messageAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatActivity", "Error: ${error.message}")
                }
            })

        sendButton.setOnClickListener {
            val message = messageBox.text.toString()
            val messageObject = Message(message, senderUid, receiverUid)

            // Update the receiver's chat room
            mDbRef.child("chats").child(receiverRoom!!).child("messages").push()
                .setValue(messageObject).addOnCompleteListener {
                    if (it.isSuccessful) {
                        // Update the sender's chat room with received status true
                        val senderMessageObject =
                            Message(message, senderUid, receiverUid, received = true)
                        mDbRef.child("chats").child(senderRoom!!).child("messages").push()
                            .setValue(senderMessageObject)

                        mDbRef.child("users").child(receiverUid!!).child("token")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    val receiverToken = dataSnapshot.getValue(String::class.java)
                                    if (!receiverToken.isNullOrEmpty()) {
                                        sendNotificationToUser(receiverToken, senderUid!!, message)
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    Log.e(
                                        "ChatActivity",
                                        "Failed to retrieve FCM token: ${databaseError.message}"
                                    )
                                }
                            })
                    }
                }
            messageBox.setText("")
        }
    }

    // Mark all received messages as seen when the receiver views the chat
    override fun onResume() {
        super.onResume()

        // Mark all received messages as seen when the chat is opened
        mDbRef.child("chats").child(receiverRoom!!).child("messages")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (postSnapshot in snapshot.children) {
                        val message = postSnapshot.getValue(Message::class.java)

                        // Update only if the message is from the other user and not already seen
                        if (message?.senderId != FirebaseAuth.getInstance().currentUser?.uid && message?.seen == false) {
                            postSnapshot.ref.child("seen").setValue(true)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatActivity", "Error: ${error.message}")
                }
            })
    }
}

