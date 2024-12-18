package com.nat.finalstoryapp.ui.stories

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nat.finalstoryapp.data.api.response.ListStoryItem
import com.nat.finalstoryapp.databinding.ItemStoryBinding
import com.nat.finalstoryapp.ui.details.DetailsActivity

class StoryListAdapter : PagingDataAdapter<ListStoryItem, StoryListAdapter.StoryViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = getItem(position)
        if (story != null) {
            holder.bind(story)
        }
    }

    class StoryViewHolder(private val binding: ItemStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(story: ListStoryItem) {
            binding.tvItemName.text = story.name
            binding.tvItemDescription.text = story.description
            binding.createdAtText.text = story.createdAt
            Glide.with(binding.ivItemPhoto.context).load(story.photoUrl).into(binding.ivItemPhoto)

            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, DetailsActivity::class.java).apply {
                    putExtra(DetailsActivity.EXTRA_STORY_ID, story.id)
                    putExtra(DetailsActivity.EXTRA_STORY_NAME, story.name)
                    putExtra(DetailsActivity.EXTRA_STORY_DESCRIPTION, story.description)
                    putExtra(DetailsActivity.EXTRA_STORY_PHOTO_URL, story.photoUrl)
                    putExtra(DetailsActivity.EXTRA_STORY_CREATED_AT, story.createdAt)
                }
                context.startActivity(intent)
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}