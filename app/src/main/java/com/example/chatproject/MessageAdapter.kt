package com.example.chatproject

import Message
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter(val context: Context, val messageList: ArrayList<Message>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val ITEM_RECEIVE = 1
    val ITEM_SENT = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_RECEIVE) {
            // Inflate receive layout
            val view: View = LayoutInflater.from(context).inflate(R.layout.receive, parent, false)
            ReceiveViewHolder(view)
        } else {
            // Inflate sent layout
            val view: View = LayoutInflater.from(context).inflate(R.layout.sent, parent, false)
            SentViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = messageList[position]

        // Create a simple date format to display the time
        val simpleDateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val formattedTime = simpleDateFormat.format(Date(currentMessage.timestamp))

        if (holder is SentViewHolder) {
            // Handle sent message
            holder.sentMessage.text = currentMessage.message
            holder.sentTime.text = formattedTime

            // Set the status for sent messages
            if (currentMessage.seen) {
                holder.sentStatus.text = "Seen"
            } else if (currentMessage.received) {
                holder.sentStatus.text = "Received"
            } else {
                holder.sentStatus.text = "Sent"
            }

        } else if (holder is ReceiveViewHolder) {
            // Handle received message
            holder.receiveMessage.text = currentMessage.message
            holder.receiveTime.text = formattedTime
        }
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]
        return if (FirebaseAuth.getInstance().currentUser?.uid == currentMessage.senderId) {
            ITEM_SENT
        } else {
            ITEM_RECEIVE
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    class SentViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val sentMessage: TextView = itemView.findViewById(R.id.txt_sent_message)
        val sentStatus: TextView = itemView.findViewById(R.id.txt_seen_status) // Status TextView
        val sentTime: TextView = itemView.findViewById(R.id.txt_sent_time) // Time TextView
    }

    class ReceiveViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val receiveMessage: TextView = itemView.findViewById(R.id.txt_recieve_message)
        val receiveTime: TextView = itemView.findViewById(R.id.txt_receive_time) // Time TextView
    }
}

