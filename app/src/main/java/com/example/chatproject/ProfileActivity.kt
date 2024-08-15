package com.example.chatproject

// ProfileActivity.kt
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var uploadButton: Button
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null

    private lateinit var storageReference: StorageReference
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference // For Realtime Database

    // or
    private val firestore = FirebaseFirestore.getInstance() // For Firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profileImageView = findViewById(R.id.profileImageView)
        uploadButton = findViewById(R.id.uploadButton)

        storageReference = FirebaseStorage.getInstance().reference.child("profile_pictures")

        profileImageView.setOnClickListener {
            openFileChooser()
        }

        uploadButton.setOnClickListener {
            uploadImage()
        }
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
            && data != null && data.data != null
        ) {
            imageUri = data.data
            profileImageView.setImageURI(imageUri)
        }
    }

    private fun uploadImage() {
        if (imageUri != null) {
            val fileReference = storageReference.child("${auth.currentUser?.uid}.jpg")
            fileReference.putFile(imageUri!!)
                .addOnSuccessListener {
                    fileReference.downloadUrl.addOnSuccessListener { uri ->
                        val downloadUrl = uri.toString()
                        val user = auth.currentUser
                        val profileUpdates = userProfileChangeRequest {
                            photoUri = uri
                        }

                        user!!.updateProfile(profileUpdates).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val uid = user.uid

                                // Retrieve existing user profile data from Realtime Database
                                database.child("user").child(uid).get()
                                    .addOnSuccessListener { dataSnapshot ->
                                        val existingUserProfile =
                                            dataSnapshot.value as MutableMap<String, Any>?

                                        // Update profile image URL
                                        existingUserProfile?.put("profileImageUrl", downloadUrl)

                                        // Update Realtime Database
                                        database.child("user").child(uid)
                                            .updateChildren(existingUserProfile!!)

                                        // Optionally update Firestore as well
                                        firestore.collection("users").document(uid).get()
                                            .addOnSuccessListener { document ->
                                                val existingUserProfileFirestore =
                                                    document.data?.toMutableMap()

                                                // Update profile image URL
                                                existingUserProfileFirestore?.put(
                                                    "profileImageUrl",
                                                    downloadUrl
                                                )

                                                // Update Firestore
                                                firestore.collection("users").document(uid)
                                                    .set(existingUserProfileFirestore!!)
                                            }

                                        Toast.makeText(
                                            this,
                                            "Upload successful",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        }
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show()
        }
    }
}
