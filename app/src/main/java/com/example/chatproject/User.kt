package com.example.project

class User {
    var name: String? = null
    var email: String? = null
    var uid: String? = null
    var profileImageUrl: String? = null
    var unreadMessages: Int = 0
    var lastMessage: String? = null // Add this field to store the last message
    var lastMessageTime: Long = 0L

    constructor() {}

    constructor(name: String?, email: String?, uid: String?, pUrl: String?, lastMessage: String?) {
        this.name = name
        this.email = email
        this.uid = uid
        this.profileImageUrl = pUrl
        this.unreadMessages = 0
        this.lastMessage = lastMessage
        this.lastMessageTime = lastMessageTime
    }
}