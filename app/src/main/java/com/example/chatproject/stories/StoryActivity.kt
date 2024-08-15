
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.ProgressBar

import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.chatproject.R
import com.example.chatproject.stories.Story
import com.example.chatproject.stories.UserStory



class StoryActivity : AppCompatActivity() {

    private lateinit var storyImageView: ImageView
    private lateinit var storyProgressBar: ProgressBar
    private var currentStoryIndex = 0
    private lateinit var userStory: UserStory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story)

        storyImageView = findViewById(R.id.storyImageView)
        storyProgressBar = findViewById(R.id.storyProgressBar)

        userStory = intent.getParcelableExtra("userStory")!!

        loadStory(userStory.stories[currentStoryIndex])
    }

    private fun loadStory(story: Story) {
        Glide.with(this).load(story.imageUrl).into(storyImageView)

        Handler(Looper.getMainLooper()).postDelayed({
            currentStoryIndex++
            if (currentStoryIndex < userStory.stories.size) {
                loadStory(userStory.stories[currentStoryIndex])
            } else {
                finish()
            }
        }, 3000) // Display each story for 3 seconds
    }
}


