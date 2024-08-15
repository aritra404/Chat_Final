package com.example.chatproject.stories

import StoryActivity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatproject.R


class StoryAdapter(
    private val context: Context,
    private var userStories: List<UserStory>,
    private val onStoryClick: (UserStory) -> Unit
) : RecyclerView.Adapter<StoryAdapter.UserStoryViewHolder>() {

    inner class UserStoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userProfilePic: ImageView = itemView.findViewById(R.id.storyImageView)
        val userName: TextView = itemView.findViewById(R.id.userNameTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserStoryViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.story_item, parent, false)
        return UserStoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserStoryViewHolder, position: Int) {
        val userStory = userStories[position]
        holder.userName.text = userStory.userName

        Glide.with(context)
            .load(userStory.userProfilePic)
            .into(holder.userProfilePic)

        holder.itemView.setOnClickListener {
            onStoryClick(userStory)
        }
    }

    override fun getItemCount(): Int {
        return userStories.size
    }
    fun updateStories(newStories: List<UserStory>) {
        userStories = newStories
        notifyDataSetChanged()
    }
}

