package com.danghung.elearning.view.fragment

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.danghung.elearning.R
import com.danghung.elearning.api.res.StoryDetailRes
import com.danghung.elearning.databinding.FragmentStoryReaderBinding
import com.danghung.elearning.databinding.ItemStoryParagraphBinding
import com.danghung.elearning.viewmodel.StoriesVM
import com.danghung.elearning.viewmodel.StoriesVM.State

class StoryReaderFragment : BaseFragment<FragmentStoryReaderBinding, StoriesVM>() {
    companion object {
        val TAG: String = StoryReaderFragment::class.java.name
        private const val STATE_STORY_ID = "story_id"
        private val paragraphBreak = Regex("""\n(?:[ \t]*\n)+""")
    }

    private var storyId = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storyId = savedInstanceState?.getLong(STATE_STORY_ID) ?: (data as? Long) ?: 0L
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(STATE_STORY_ID, storyId)
        super.onSaveInstanceState(outState)
    }

    override fun getClassVM(): Class<StoriesVM> = StoriesVM::class.java

    override fun initViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentStoryReaderBinding = FragmentStoryReaderBinding.inflate(inflater, container, false)

    override fun initViews() {
        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        if (storyId <= 0L) {
            binding.lnStoryError.isVisible = true
            binding.tvStoryError.setText(R.string.txt_story_not_found)
            binding.btnRetry.isVisible = false
            return
        }

        binding.btnRetry.setOnClickListener { viewModel.loadStory(storyId) }
        viewModel.reader.observe(viewLifecycleOwner) { state ->
            binding.lnStoryLoading.isVisible = state == State.Loading
            binding.lnStoryError.isVisible = state == State.Failed
            binding.storyScroll.isVisible = state is State.Ready
            if (state is State.Ready) showStory(state.data)
        }
        viewModel.loadStory(storyId)
    }

    private fun showStory(detail: StoryDetailRes) = with(binding) {
        tvTitle.text = detail.story.title
        tvTranslatedTitle.text = detail.story.titleVi
        ivStoryCover.backgroundTintList =
            ColorStateList.valueOf(requireContext().getColor(detail.story.level.surface))
        Glide.with(this@StoryReaderFragment)
            .load(detail.story.imageUrl)
            .placeholder(R.drawable.ic_story_cover_default)
            .fallback(R.drawable.ic_story_cover_default)
            .error(R.drawable.ic_story_cover_default)
            .into(ivStoryCover)

        showReadingText(detail)
    }

    private fun String.paragraphs(): List<String> = replace("\r\n", "\n")
        .replace('\r', '\n')
        .split(paragraphBreak)
        .map(String::trim)
        .filter(String::isNotEmpty)

    private fun showReadingText(detail: StoryDetailRes) = with(binding) {
        val english = detail.content.paragraphs()
        val vietnamese = detail.translation.paragraphs()
        val canPair = english.size > 1 && english.size == vietnamese.size
        lnStoryParagraphs.removeAllViews()
        lnStoryParagraphs.isVisible = canPair
        tvStoryText.isVisible = !canPair
        btnTranslate.isVisible = vietnamese.isNotEmpty()

        if (canPair) {
            showParagraphTranslations(english, vietnamese)
        } else {
            fun renderLanguage() {
                val translated = viewModel.readingVietnamese && vietnamese.isNotEmpty()
                tvStoryText.text = if (translated) detail.translation else detail.content
                tvReadingLanguage.setText(
                    if (translated) R.string.txt_story_vietnamese else R.string.txt_story_english
                )
                btnTranslate.setText(
                    if (translated) R.string.txt_story_show_english else R.string.txt_story_show_translation
                )
            }
            btnTranslate.setOnClickListener {
                viewModel.readingVietnamese = !viewModel.readingVietnamese
                renderLanguage()
            }
            renderLanguage()
        }
    }

    private fun showParagraphTranslations(english: List<String>, vietnamese: List<String>) = with(binding) {
        tvReadingLanguage.setText(R.string.txt_story_english)
        val expanded = viewModel.expandedTranslations
        val rows = english.zip(vietnamese).map { (original, translated) ->
            ItemStoryParagraphBinding.inflate(layoutInflater, lnStoryParagraphs, true).apply {
                tvParagraph.text = original
                tvParagraphTranslation.text = translated
            }
        }

        fun renderToggle() {
            btnTranslate.setText(
                if (rows.indices.all { it in expanded }) R.string.txt_story_hide_all_translations
                else R.string.txt_story_show_all_translations
            )
        }

        rows.forEachIndexed { index, row ->
            row.renderTranslation(index in expanded)
            row.btnParagraphTranslation.setOnClickListener {
                if (!expanded.add(index)) expanded.remove(index)
                row.renderTranslation(index in expanded)
                renderToggle()
            }
        }
        btnTranslate.setOnClickListener {
            val showAll = rows.indices.any { it !in expanded }
            expanded.clear()
            if (showAll) expanded.addAll(rows.indices)
            rows.forEach { it.renderTranslation(showAll) }
            renderToggle()
        }
        renderToggle()
    }

    private fun ItemStoryParagraphBinding.renderTranslation(shown: Boolean) {
        tvParagraphTranslation.isVisible = shown
        btnParagraphTranslation.setText(
            if (shown) R.string.txt_story_hide_translation else R.string.txt_story_show_translation
        )
    }
}
