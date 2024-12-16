package com.nat.finalstoryapp.ui.stories

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nat.finalstoryapp.data.api.response.Story
import com.nat.finalstoryapp.databinding.ItemStoryBinding
import com.nat.finalstoryapp.ui.details.DetailsActivity

class StoryAdapter : ListAdapter<Story, StoryAdapter.StoryViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = getItem(position)
        holder.bind(story)
    }

    class StoryViewHolder(private val binding: ItemStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(story: Story) {
            binding.tvItemName.text = story.title
            binding.tvItemDescription.text = story.description
            binding.createdAtText.text = story.createdAt
            Glide.with(binding.ivItemPhoto.context).load(story.photoUrl).into(binding.ivItemPhoto)

            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, DetailsActivity::class.java).apply {
                    putExtra(DetailsActivity.EXTRA_STORY_ID, story.id)
                    putExtra(DetailsActivity.EXTRA_STORY_NAME, story.title)
                    putExtra(DetailsActivity.EXTRA_STORY_DESCRIPTION, story.description)
                    putExtra(DetailsActivity.EXTRA_STORY_PHOTO_URL, story.photoUrl)
                    putExtra(DetailsActivity.EXTRA_STORY_CREATED_AT, story.createdAt)
                }
                context.startActivity(intent)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Story>() {
            override fun areItemsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem == newItem
            }
        }
    }
}