class Message {
    var message: String? = null
    var senderId: String? = null
    var receiverId: String? = null
    var seen: Boolean = false
    var received: Boolean = false
    var timestamp: Long = System.currentTimeMillis()

    constructor(){}

    constructor(message: String?, senderId: String?, receiverId: String?, seen: Boolean = false, received: Boolean = false){
        this.message = message
        this.senderId = senderId
        this.receiverId = receiverId
        this.seen = seen
        this.received = received
        this.timestamp = timestamp
    }
}