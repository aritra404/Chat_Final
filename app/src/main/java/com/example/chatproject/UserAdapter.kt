import android.content.Context
import android.content.Intent
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.chatproject.R
import com.example.project.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserAdapter(
    private val context: Context,
    private var userList: ArrayList<User>,
    private val onItemClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>(), Filterable {

    private var filteredUserList: ArrayList<User> = ArrayList(userList)

    init {
        filteredUserList = ArrayList(userList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_chat_item, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = filteredUserList[position]
        holder.bind(currentUser)
        holder.itemView.setOnClickListener {
            onItemClick(currentUser)
        }
        holder.lastMessageText.text = currentUser.lastMessage ?: "No messages yet"

        // Format the last message time
        if (currentUser.lastMessageTime != 0L) {
            val simpleDateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val formattedTime = simpleDateFormat.format(Date(currentUser.lastMessageTime))
            holder.lastMessageTimeTextView.text = formattedTime
        } else {
            holder.lastMessageTimeTextView.text = ""
        }

        // Display unread messages count
        if (currentUser.unreadMessages > 0) {
            holder.unreadMessagesTextView.visibility = View.VISIBLE
            holder.unreadMessagesTextView.text = currentUser.unreadMessages.toString()
        } else {
            holder.unreadMessagesTextView.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return filteredUserList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = ArrayList<User>()
                if (constraint.isNullOrEmpty()) {
                    filteredList.addAll(userList)
                } else {
                    val filterPattern = constraint.toString().lowercase().trim()
                    for (user in userList) {
                        if (user.name?.lowercase()?.contains(filterPattern) == true) {
                            filteredList.add(user)
                        }
                    }
                }
                val results = FilterResults()
                results.values = filteredList
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredUserList.clear()
                filteredUserList.addAll(results?.values as List<User>)
                notifyDataSetChanged()
            }
        }
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textName: TextView = itemView.findViewById(R.id.sender_name)
        private val profileImage: ImageView = itemView.findViewById(R.id.profile_image)
        val unreadMessagesTextView: TextView = itemView.findViewById(R.id.unreadMessagesTextView)
        val lastMessageTimeTextView: TextView = itemView.findViewById(R.id.time)
         val lastMessageText: TextView = itemView.findViewById(R.id.last_message)

        fun bind(user: User) {
            textName.text = user.name

            lastMessageText.text = user.lastMessage

            // Load profile image using Glide
            Glide.with(context)
                .load(user.profileImageUrl)
                .placeholder(R.drawable.img) // Placeholder image
                .into(profileImage)
        }
    }
}

