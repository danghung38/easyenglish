package com.danghung.elearning.view.fragment

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.danghung.elearning.R
import com.danghung.elearning.databinding.FragmentStoriesBinding
import com.danghung.elearning.model.StoryLevel
import com.danghung.elearning.view.adapter.StoriesAdapter
import com.danghung.elearning.viewmodel.StoriesVM
import com.danghung.elearning.viewmodel.StoriesVM.State

class StoriesFragment : BaseFragment<FragmentStoriesBinding, StoriesVM>() {
    companion object {
        val TAG: String = StoriesFragment::class.java.name
    }

    private lateinit var storiesAdapter: StoriesAdapter

    override fun getClassVM(): Class<StoriesVM> = StoriesVM::class.java

    override fun initViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentStoriesBinding = FragmentStoriesBinding.inflate(inflater, container, false)

    override fun initViews() {
        storiesAdapter = StoriesAdapter { story ->
            callBack.showFragment(StoryReaderFragment.TAG, story.id, true)
        }
        binding.rvStories.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStories.adapter = storiesAdapter
        styleLevelChips()

        binding.storyLevels.check(when (viewModel.selectedLevel) {
            StoryLevel.BEGINNER -> R.id.chipStoryBeginner
            StoryLevel.INTERMEDIATE -> R.id.chipStoryIntermediate
            StoryLevel.ADVANCED -> R.id.chipStoryAdvanced
            null -> R.id.chipStoryAll
        })
        binding.storyLevels.setOnCheckedStateChangeListener { _, selected ->
            viewModel.selectedLevel = when (selected.firstOrNull()) {
                R.id.chipStoryBeginner -> StoryLevel.BEGINNER
                R.id.chipStoryIntermediate -> StoryLevel.INTERMEDIATE
                R.id.chipStoryAdvanced -> StoryLevel.ADVANCED
                else -> null
            }
            renderLibrary()
        }

        binding.btnRetryStories.setOnClickListener { viewModel.loadLibrary() }
        viewModel.library.observe(viewLifecycleOwner) { renderLibrary() }
        viewModel.loadLibrary()
        setupBackPress()
    }

    private fun renderLibrary() {
        val state = viewModel.library.value
        val level = viewModel.selectedLevel
        val items = if (state is State.Ready) {
            state.data.filter { level == null || it.level == level }
        } else {
            emptyList()
        }
        storiesAdapter.submitList(items)
        binding.pbStories.isVisible = state == State.Loading
        binding.lnStoriesStatus.isVisible = state == State.Failed || (state is State.Ready && items.isEmpty())
        binding.btnRetryStories.isVisible = state == State.Failed
        binding.tvStoriesStatus.setText(
            if (state == State.Failed) R.string.txt_story_list_error else R.string.txt_story_empty
        )
    }

    private fun styleLevelChips() {
        val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
        listOf(
            binding.chipStoryBeginner to StoryLevel.BEGINNER,
            binding.chipStoryIntermediate to StoryLevel.INTERMEDIATE,
            binding.chipStoryAdvanced to StoryLevel.ADVANCED
        ).forEach { (chip, level) ->
            val surface = chip.context.getColor(level.surface)
            val ink = chip.context.getColor(level.ink)
            chip.setTextColor(ink)
            chip.chipBackgroundColor = ColorStateList(
                states, intArrayOf(surface, chip.context.getColor(R.color.white))
            )
            chip.chipStrokeColor = ColorStateList(states, intArrayOf(ink, surface))
        }
    }

    override fun onDestroyView() {
        binding.rvStories.adapter = null
        super.onDestroyView()
    }
}
