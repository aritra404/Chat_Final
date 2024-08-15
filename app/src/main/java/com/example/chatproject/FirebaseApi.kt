package com.example.chatproject
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

fun sendNotificationToUser(receiverToken: String, senderName: String, message: String) {
    val FCM_API = "https://fcm.googleapis.com/fcm/send"
    val serverKey = ""
    val contentType = "application/json"

    val notificationBody = JSONObject()
    val notification = JSONObject()

    try {
        notification.put("title", senderName)
        notification.put("body", message)
        notification.put("sound", "default")

        notificationBody.put("to", receiverToken)
        notificationBody.put("notification", notification)

        val requestBody = RequestBody.create(contentType.toMediaTypeOrNull(), notificationBody.toString())
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(FCM_API)
            .post(requestBody)
            .addHeader("Authorization", serverKey)
            .addHeader("Content-Type", contentType)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                // Handle failure
                Log.d("FCM", "Failed to send notification: ${e.message}")
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                // Handle success
                Log.d("FCM", "Notification sent successfully: ${response.body?.string()}")
            }
        })

    } catch (e: Exception) {
        Log.e("FCM", "Error in sending notification: ${e.message}")
    }
}
