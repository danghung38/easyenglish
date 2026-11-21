package com.danghung.elearning.view.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.danghung.elearning.R
import com.danghung.elearning.databinding.ItemStoryBinding
import com.danghung.elearning.model.BilingualStory

class StoriesAdapter(
    private val onStoryClick: (BilingualStory) -> Unit
) : ListAdapter<BilingualStory, StoriesAdapter.StoryHolder>(StoryDiff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryHolder =
        StoryHolder(ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: StoryHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class StoryHolder(private val binding: ItemStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(story: BilingualStory) = with(binding) {
            val levelSurface = ColorStateList.valueOf(root.context.getColor(story.level.surface))
            tvStoryEnglishTitle.text = story.title
            tvStoryVietnameseTitle.text = story.titleVi
            tvStoryLevel.setText(story.level.label)
            tvStoryLevel.setTextColor(root.context.getColor(story.level.ink))
            tvStoryLevel.backgroundTintList = levelSurface
            ivCover.backgroundTintList = levelSurface
            tvStorySummary.text = story.summary
            tvStorySummary.isVisible = !story.summary.isNullOrBlank()

            Glide.with(ivCover)
                .load(story.imageUrl)
                .placeholder(R.drawable.ic_story_cover_default)
                .fallback(R.drawable.ic_story_cover_default)
                .error(R.drawable.ic_story_cover_default)
                .into(ivCover)

            root.setOnClickListener { onStoryClick(story) }
        }
    }

    private object StoryDiff : DiffUtil.ItemCallback<BilingualStory>() {
        override fun areItemsTheSame(oldItem: BilingualStory, newItem: BilingualStory): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: BilingualStory, newItem: BilingualStory): Boolean =
            oldItem == newItem
    }
}
