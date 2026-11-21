package com.danghung.elearning.view.fragment

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.danghung.elearning.R
import com.danghung.elearning.api.res.ExamRes
import com.danghung.elearning.databinding.FragmentHomeBinding
import com.danghung.elearning.model.BilingualStory
import com.danghung.elearning.model.CreItem
import com.danghung.elearning.model.HomeDailyContent
import com.danghung.elearning.model.HomeWord
import com.danghung.elearning.view.adapter.CreAdapter
import com.danghung.elearning.view.fragment.MenuFragment.Companion.TYPE_EXAMS
import com.danghung.elearning.view.fragment.MenuFragment.Companion.TYPE_LEARN
import com.danghung.elearning.view.fragment.MenuFragment.Companion.TYPE_STORIES
import com.danghung.elearning.viewmodel.HomeVM
import com.google.android.material.chip.Chip
import java.time.LocalDate
import kotlin.math.abs


class HomeFragment : BaseFragment<FragmentHomeBinding, HomeVM>() {
    companion object {
        val TAG: String = HomeFragment::class.java.name
    }

    private lateinit var creAdapter: CreAdapter
    private val autoScrollHandler = Handler(Looper.getMainLooper())
    private var autoScrollRunnable: Runnable? = null
    private val autoScrollDelay = 3000L
    private var isScrollingForward = true  // Track hướng scroll

    override fun getClassVM(): Class<HomeVM> = HomeVM::class.java

    override fun initViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHomeBinding = FragmentHomeBinding.inflate(inflater, container, false)

    override fun initViews() {
        // search
        binding.btnSearch.setOnClickListener(this)  // click icon search
        setupCreViewPager()
        binding.lnReading.setOnClickListener(this)
        binding.lnListening.setOnClickListener(this)
        binding.lnWriting.setOnClickListener(this)
        binding.lnSpeaking.setOnClickListener(this)

        // Gọi hàm setup cho Chatbot
        setupChatbotFloatingButton()
        setupDiscovery()
    }

    override fun clickView(v: View) {
        when (v.id) {
            binding.btnSearch.id -> {
                val query = binding.edtSearch.text.toString().trim()

                if (query.isEmpty()) {
                    notify("No empty search allowed!!")
                    return
                }

                callBack.showFragment(TranslationFragment.TAG, query, true)
            }
            else -> callBack.showFragment(MenuFragment.TAG, TYPE_EXAMS, false)
        }
    }

    private fun setupDiscovery() {
        binding.homeDiscovery.btnLearn.setOnClickListener {
            callBack.showFragment(MenuFragment.TAG, TYPE_LEARN, false)
        }
        bindDailyPractice()
        renderFeaturedStory(null)
        viewModel.featuredStory.observe(viewLifecycleOwner) { renderFeaturedStory(it) }
        viewModel.loadFeaturedStory()
        renderFeaturedExam(null)
        viewModel.exams.observe(viewLifecycleOwner) { renderFeaturedExam(it.firstOrNull()) }
        viewModel.loadExams()
    }

    private fun renderFeaturedStory(story: BilingualStory?) = with(binding.homeDiscovery) {
        tvFeaturedStoryTitle.text = story?.title ?: getString(R.string.txt_stories)
        tvFeaturedStoryTranslation.text = story?.titleVi ?: getString(R.string.home_story_preview)
        tvFeaturedStoryDetails.isVisible = story != null
        if (story != null) {
            tvFeaturedStoryDetails.setText(story.level.label)
            tvFeaturedStoryDetails.setTextColor(ContextCompat.getColor(requireContext(), story.level.ink))
        }
        ivFeaturedStoryCover.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(requireContext(), story?.level?.surface ?: R.color.story_panel)
        )

        Glide.with(this@HomeFragment)
            .load(story?.imageUrl)
            .placeholder(R.drawable.ic_story_cover_default)
            .fallback(R.drawable.ic_story_cover_default)
            .error(R.drawable.ic_story_cover_default)
            .into(ivFeaturedStoryCover)

        btnFeaturedStory.setOnClickListener {
            if (story != null) {
                callBack.showFragment(StoryReaderFragment.TAG, story.id, true)
            } else {
                callBack.showFragment(MenuFragment.TAG, TYPE_STORIES, false)
            }
        }
        btnBrowseStories.setOnClickListener {
            callBack.showFragment(MenuFragment.TAG, TYPE_STORIES, false)
        }
    }

    private fun renderFeaturedExam(exam: ExamRes?) = with(binding.homeDiscovery) {
        if (exam == null) {
            tvFeaturedTitle.setText(R.string.home_featured_default_title)
            tvFeaturedDetails.setText(R.string.home_featured_default_details)
            btnFeatured.setText(R.string.home_browse_exams)
            btnFeatured.setOnClickListener { callBack.showFragment(MenuFragment.TAG, TYPE_EXAMS, false) }
            return@with
        }

        val skillCount = exam.examParts.map { it.skillType }.distinct().size
        val skills = resources.getQuantityString(R.plurals.home_featured_skills, skillCount, skillCount)
        val duration = exam.totalDuration
        tvFeaturedTitle.text = exam.title
        tvFeaturedDetails.text = if (duration > 0) {
            val minutes = resources.getQuantityString(R.plurals.home_featured_minutes, duration, duration)
            getString(R.string.home_featured_details, skills, minutes)
        } else {
            skills
        }
        btnFeatured.setText(R.string.home_featured_open)
        btnFeatured.setOnClickListener { callBack.showFragment(PracticeFragment.TAG, exam, false) }
    }

    private fun bindDailyPractice() = with(binding.homeDiscovery) {
        val day = LocalDate.now().toEpochDay()
        val lesson = HomeDailyContent.forDay(day)
        tvDailyWord.setText(lesson.word.word)
        tvDailyDefinition.setText(lesson.word.definition)
        tvDailyExample.setText(lesson.word.example)
        btnTranslateWord.setOnClickListener {
            callBack.showFragment(TranslationFragment.TAG, getString(lesson.word.word), true)
        }
        tvQuizPrompt.text = getString(R.string.home_quiz_prompt, getString(lesson.answer.prompt))

        val selectedWord = viewModel.answerFor(day)
        quizChoices.removeAllViews()
        for (choice in lesson.choices) {
            val chip = createChoiceChip(choice)
            quizChoices.addView(chip)
            chip.isChecked = choice.word == selectedWord
            chip.setOnClickListener {
                chip.isChecked = true
                viewModel.selectAnswer(day, choice.word)
                renderQuizFeedback(choice.word, lesson.answer)
            }
        }
        renderQuizFeedback(selectedWord, lesson.answer)
    }

    private fun createChoiceChip(choice: HomeWord): Chip {
        val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
        val accent = ContextCompat.getColor(requireContext(), R.color.home_quiz_ink)
        val pale = ContextCompat.getColor(requireContext(), R.color.home_quiz_choice)
        return Chip(requireContext()).apply {
            id = View.generateViewId()
            setText(choice.word)
            isCheckable = true
            isCheckedIconVisible = false
            setEnsureMinTouchTargetSize(true)
            chipBackgroundColor = ColorStateList(states, intArrayOf(accent, pale))
            setTextColor(ColorStateList(states, intArrayOf(android.graphics.Color.WHITE, accent)))
        }
    }

    private fun renderQuizFeedback(selectedWord: Int?, answer: HomeWord) {
        val feedback = binding.homeDiscovery.tvQuizFeedback
        feedback.isVisible = selectedWord != null
        if (selectedWord == null) return

        val isCorrect = selectedWord == answer.word
        feedback.text = if (isCorrect) {
            getString(R.string.home_quiz_correct, getString(answer.definition))
        } else {
            getString(R.string.home_quiz_incorrect)
        }
        feedback.setTextColor(
            ContextCompat.getColor(requireContext(), if (isCorrect) R.color.home_correct else R.color.home_incorrect)
        )
    }

    private fun setupChatbotFloatingButton() {
        var dX = 0f
        var dY = 0f
        var initialX = 0f
        var initialY = 0f
        var isMoveAction = false

        // Lấy khoảng cách tối thiểu để hệ thống coi là hành động "Kéo" (nhỏ hơn mức này là Click)
        val touchSlop = ViewConfiguration.get(requireContext()).scaledTouchSlop

        binding.btnChatbot.setOnTouchListener { view, event ->
            // Lấy kích thước của layout cha (FrameLayout) để giới hạn chatbot không bị kéo ra ngoài màn hình
            val parentView = binding.rootView // id của FrameLayout gốc
            val parentWidth = parentView.width
            val parentHeight = parentView.height

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                    initialX = view.x
                    initialY = view.y
                    isMoveAction = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    var newX = event.rawX + dX
                    var newY = event.rawY + dY

                    // Giới hạn không cho kéo chatbot vượt quá biên màn hình (trái, phải, trên, dưới)
                    newX = newX.coerceIn(0f, (parentWidth - view.width).toFloat())
                    newY = newY.coerceIn(0f, (parentHeight - view.height).toFloat())

                    view.x = newX
                    view.y = newY

                    // Nếu khoảng cách dịch chuyển lớn hơn touchSlop thì xác định là đang kéo (Drag)
                    if (abs(view.x - initialX) > touchSlop || abs(view.y - initialY) > touchSlop) {
                        isMoveAction = true
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isMoveAction) {
                        // Nếu không phải kéo kéo thả thả -> Đây là một cú CLICK
                        // Gọi hàm chuyển sang ChatbotFragment của bạn ở đây
                        goToChatbotFragment()
                    } else {
                        // Nếu là hành động kéo thả -> Xử lý tự động hít về mép trái hoặc phải gần nhất
                        val screenCenterX = parentWidth / 2
                        val currentX = view.x
                        val viewWidth = view.width

                        // Xác định vị trí đích (Hít vào mép trái cách 20dp hoặc mép phải cách 20dp)
                        val marginPx = resources.getDimensionPixelSize(R.dimen.viewpager_current_item_horizontal_margin) // tận dụng tạm margin định nghĩa sẵn hoặc điền số px cụ thể
                        val targetX = if (currentX + viewWidth / 2 < screenCenterX) {
                            0f + 30f // Cách mép trái một chút
                        } else {
                            (parentWidth - viewWidth).toFloat() - 30f // Cách mép phải một chút
                        }

                        // Thực hiện hiệu ứng mượt mà (Animation) di chuyển về mép
                        view.animate()
                            .x(targetX)
                            .setDuration(250) // Thời gian hít (250 mili giây)
                            .start()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun goToChatbotFragment() {
        // Xử lý chuyển fragment của bạn tại đây, ví dụ:
        callBack.showFragment(ChatbotFragment.TAG, null, true)
    }

    private fun setupCreViewPager() {
        val creItems = listOf(
                CreItem(getString(R.string.txt_cre1), getString(R.string.txt_author1)),
                CreItem(getString(R.string.txt_cre2), getString(R.string.txt_author2)),
                CreItem(getString(R.string.txt_cre3), getString(R.string.txt_author3)),
                CreItem(getString(R.string.txt_cre4), getString(R.string.txt_author4)),
                CreItem(getString(R.string.txt_cre5), getString(R.string.txt_author5)),
                CreItem(getString(R.string.txt_cre6), getString(R.string.txt_author6)),
                CreItem(getString(R.string.txt_cre7), getString(R.string.txt_author7)),
                CreItem(getString(R.string.txt_cre8), getString(R.string.txt_author8)),
                CreItem(getString(R.string.txt_cre9), getString(R.string.txt_author9)),
                CreItem(getString(R.string.txt_cre10), getString(R.string.txt_author10)),
                CreItem(getString(R.string.txt_cre11), getString(R.string.txt_author11))
            )
        creAdapter = CreAdapter(creItems)
        binding.vpCre.adapter = creAdapter
        binding.dotCre.attachTo(binding.vpCre)
        setupCarousel()
        startAutoScroll()
        setupBackPress()
    }

    private fun setupCarousel() {
        binding.vpCre.offscreenPageLimit = 1
        val recyclerView = binding.vpCre.getChildAt(0) as? RecyclerView
        recyclerView?.overScrollMode = View.OVER_SCROLL_NEVER
        val nextItemVisiblePx = resources.getDimension(R.dimen.viewpager_next_item_visible)
        val currentItemHorizontalMarginPx = resources.getDimension(R.dimen.viewpager_current_item_horizontal_margin)
        val pageTranslationX = nextItemVisiblePx + currentItemHorizontalMarginPx
        val pageTransformer = androidx.viewpager2.widget.ViewPager2.PageTransformer { page, position ->
            page.translationX = -pageTranslationX * position
            page.scaleY = 1 - (0.25f * kotlin.math.abs(position))
            page.alpha = 0.25f + (1 - kotlin.math.abs(position))
        }
        binding.vpCre.setPageTransformer(pageTransformer)
        val itemDecoration = HorizontalMarginItemDecoration(
            requireContext(),
            R.dimen.viewpager_current_item_horizontal_margin
        )
        binding.vpCre.addItemDecoration(itemDecoration)
    }

    private class HorizontalMarginItemDecoration(context: Context, @DimenRes horizontalMarginInDp: Int) : RecyclerView.ItemDecoration() {
        private val horizontalMarginInPx: Int = context.resources.getDimensionPixelSize(horizontalMarginInDp)
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            outRect.right = horizontalMarginInPx
            outRect.left = horizontalMarginInPx
        }
    }

    private fun startAutoScroll() {
        autoScrollRunnable = object : Runnable {
            override fun run() {
                val itemCount = binding.vpCre.adapter?.itemCount ?: 0
                if (itemCount <= 1) return
                smoothScrollToNext()
                autoScrollHandler.postDelayed(this, autoScrollDelay)
            }
        }
        autoScrollHandler.postDelayed(autoScrollRunnable!!, autoScrollDelay)
    }

    private fun smoothScrollToNext() {
        val vp = binding.vpCre
        val itemCount = vp.adapter?.itemCount ?: 0
        if (itemCount == 0) return

        val currentItem = vp.currentItem
        val nextItem = if (isScrollingForward) {
            if (currentItem < itemCount - 1) {
                currentItem + 1  // Tiếp tục đi xuôi
            } else {
                isScrollingForward = false  // Đổi hướng khi đến cuối
                currentItem - 1  // Bắt đầu đi ngược
            }
        } else {
            if (currentItem > 0) {
                currentItem - 1  // Tiếp tục đi ngược
            } else {
                isScrollingForward = true  // Đổi hướng khi về đầu
                currentItem + 1  // Bắt đầu đi xuôi
            }
        }
        // Lấy RecyclerView bên trong ViewPager2
        val recyclerView = vp.getChildAt(0) as? RecyclerView
        val layoutManager = recyclerView?.layoutManager
        // Tạo SmoothScroller với tốc độ chậm
        val smoothScroller = object : LinearSmoothScroller(requireContext()) {
            override fun calculateSpeedPerPixel(displayMetrics: android.util.DisplayMetrics): Float {
                return 100f / displayMetrics.densityDpi
            }
        }
        smoothScroller.targetPosition = nextItem
        // Chạy smooth scroll
        layoutManager?.startSmoothScroll(smoothScroller)
        // Đảm bảo currentItem được cập nhật sau khi scroll xong
        vp.postDelayed({
            vp.setCurrentItem(nextItem, false)
        }, 800)
    }

    private fun stopAutoScroll() {
        autoScrollRunnable?.let {
            autoScrollHandler.removeCallbacks(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopAutoScroll()
    }

}
