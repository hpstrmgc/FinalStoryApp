package com.nat.finalstoryapp.ui.details

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.nat.finalstoryapp.databinding.ActivityDetailsBinding

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val storyName = intent.getStringExtra(EXTRA_STORY_NAME)
        val storyDescription = intent.getStringExtra(EXTRA_STORY_DESCRIPTION)
        val storyPhotoUrl = intent.getStringExtra(EXTRA_STORY_PHOTO_URL)
        val storyCreatedAt = intent.getStringExtra(EXTRA_STORY_CREATED_AT)

        binding.tvAuthorName.text = storyName
        binding.tvDescriptionDetails.text = storyDescription
        binding.tvCreatedAt.text = storyCreatedAt
        Glide.with(this).load(storyPhotoUrl).into(binding.ivDetailsPhoto)
    }

    companion object {
        const val EXTRA_STORY_ID = "extra_story_id"
        const val EXTRA_STORY_NAME = "extra_story_name"
        const val EXTRA_STORY_DESCRIPTION = "extra_story_description"
        const val EXTRA_STORY_PHOTO_URL = "extra_story_photo_url"
        const val EXTRA_STORY_CREATED_AT = "extra_story_created_at"
    }
}