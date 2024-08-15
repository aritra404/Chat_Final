package com.example.chatproject

import Message
import StoryActivity
import UserAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button

import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatproject.stories.Story
import com.example.chatproject.stories.StoryAdapter
import com.example.chatproject.stories.UploadStoryActivity
import com.example.chatproject.stories.UserStory
import com.example.project.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private lateinit var userRecyclerView: RecyclerView
    private lateinit var storiesRecyclerView: RecyclerView
    private lateinit var userList: ArrayList<User>
    private lateinit var storyList: ArrayList<Story>
    private lateinit var userAdapter: UserAdapter
    private lateinit var storyAdapter: StoryAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private lateinit var uploadStoryButton: Button
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check if the user is logged in
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            // Retrieve and store the FCM token
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    if (token != null) {
                        FirebaseDatabase.getInstance().reference.child("users").child(userId).child("token").setValue(token)
                    }
                } else {
                    Log.e("FCM", "Failed to retrieve FCM token")
                }
            }
        }


        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        searchView = findViewById(R.id.searchBar)

        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference

        userList = ArrayList()
        storyList = ArrayList()

        userAdapter = UserAdapter(this, userList) { selectedUser ->
            val userName = selectedUser.name
            val userUid = selectedUser.uid
            val profileImageUrl = selectedUser.profileImageUrl

            // Pass the details to ChatActivity
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("name", userName)
            intent.putExtra("uid", userUid)
            intent.putExtra("profileImageUrl", profileImageUrl)
            startActivity(intent)
        }

        // Set up User RecyclerView
        userRecyclerView = findViewById(R.id.userRecyclerView)
        userRecyclerView.layoutManager = LinearLayoutManager(this)
        userRecyclerView.adapter = userAdapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Not used
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                userAdapter.filter.filter(newText)
                return true
            }
        })

        // Set up Stories RecyclerView
        storiesRecyclerView = findViewById(R.id.storiesRecyclerView)
        storiesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Initialize the adapter with an empty list
        storyAdapter = StoryAdapter(this, listOf()) { userStory ->
            val intent = Intent(this, StoryActivity::class.java)
            intent.putExtra("userStory", userStory)
            startActivity(intent)
        }

        storiesRecyclerView.adapter = storyAdapter

        uploadStoryButton = findViewById(R.id.uploadStoryButton)
        uploadStoryButton.setOnClickListener {
            val intent = Intent(this, UploadStoryActivity::class.java)
            startActivity(intent)
        }

        // Load Users from Firebase
        loadUsers()

        // Fetch user stories and update the adapter
        fetchUserStories { userStories ->
            storyAdapter.updateStories(userStories)
        }
    }

    private fun fetchUserStories(onComplete: (List<UserStory>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("stories")
            .get()
            .addOnSuccessListener { snapshot ->
                val userStories = snapshot.documents.mapNotNull { userDoc ->
                    val userId = userDoc.id
                    val stories = userDoc.reference.collection("userStories").get().result?.map { storyDoc ->
                        Story(
                            imageUrl = storyDoc.getString("imageUrl") ?: "",
                            timeStamp = storyDoc.getLong("timeStamp") ?: 0L
                        )
                    } ?: listOf()

                    val userData = db.collection("users").document(userId).get().result
                    UserStory(
                        userId = userId,
                        userName = userData?.getString("userName") ?: "",
                        userProfilePic = userData?.getString("userProfilePic") ?: "",
                        stories = stories
                    )
                }
                Log.d("Firestore", "Fetched user stories: $userStories")
                onComplete(userStories)
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error fetching stories: ", exception)
            }
    }


    private fun loadUsers() {
        mDbRef.child("user").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (postSnapshot in snapshot.children) {
                    val currentUser = postSnapshot.getValue(User::class.java)
                    if (mAuth.currentUser?.uid != currentUser?.uid) {
                        currentUser?.let {
                            userList.add(it)
                            fetchLastMessage(it) // Fetch the last message for each user
                            fetchUnreadMessagesCount(it)
                        }
                    }
                }
                userAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if needed
            }
        })
    }

    private fun fetchLastMessage(user: User) {
        val senderRoom = mAuth.currentUser?.uid + user.uid
        mDbRef.child("chats").child(senderRoom).child("messages").limitToLast(1)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (postSnapshot in snapshot.children) {
                        val message = postSnapshot.getValue(Message::class.java)
                        message?.let {
                            user.lastMessage = it.message
                            user.lastMessageTime = it.timestamp
                        }
                    }
                    userAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error if needed
                }
            })
    }

    private fun fetchUnreadMessagesCount(user: User) {
        val senderRoom = mAuth.currentUser?.uid + user.uid
        mDbRef.child("chats").child(senderRoom).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var unreadCount = 0
                    for (postSnapshot in snapshot.children) {
                        val message = postSnapshot.getValue(Message::class.java)
                        // If the message is unread and was sent to the current user
                        if (message != null && !message.seen && message.receiverId == mAuth.currentUser?.uid) {
                            unreadCount++
                        }
                    }
                    user.unreadMessages = unreadCount
                    userAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error if needed
                }
            })
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                mAuth.signOut()
                val intent = Intent(this@MainActivity, LogIn::class.java)
                finish()
                startActivity(intent)
                return true
            }
            R.id.settings -> {
                val intent = Intent(this@MainActivity, ProfileActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

