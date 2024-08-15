package com.example.chatproject.stories

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.chatproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class UploadStoryActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null
    private lateinit var imagePreview: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_story)

        imagePreview = findViewById(R.id.imagePreview)
        val selectImageButton: Button = findViewById(R.id.selectImageButton)
        val uploadImageButton: Button = findViewById(R.id.uploadImageButton)

        selectImageButton.setOnClickListener {
            openFileChooser()
        }

        uploadImageButton.setOnClickListener {
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
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            Glide.with(this).load(imageUri).into(imagePreview)
        }
    }

    private fun uploadImage() {
        if (imageUri != null) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val userId = currentUser?.uid ?: ""
            val storageReference =
                FirebaseStorage.getInstance().reference.child("stories/$userId/${System.currentTimeMillis()}.jpg")

            storageReference.putFile(imageUri!!).addOnSuccessListener {
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()

                    // Create a Story object
                    val story = Story(
                        imageUrl = downloadUrl,
                        timeStamp = System.currentTimeMillis()
                    )

                    // Save the story under the user's stories node
                    FirebaseDatabase.getInstance().reference.child("stories")
                        .child(userId)
                        .child("userStories")
                        .push()
                        .setValue(story)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this@UploadStoryActivity,
                                "Story uploaded successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                        .addOnFailureListener { error ->
                            Toast.makeText(
                                this@UploadStoryActivity,
                                "Failed to upload story: ${error.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }
}

