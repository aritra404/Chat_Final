package com.example.chatproject.stories

import java.io.Serializable

data class UserStory(
    val userId: String = "",
    val userName: String = "",
    val userProfilePic: String = "",
    val stories: List<Story> = listOf()
): Serializable
