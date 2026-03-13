package com.danghung.elearning.view.fragment

import android.content.Context
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.danghung.elearning.R
import com.danghung.elearning.databinding.HomeFragmentBinding
import com.danghung.elearning.model.CreItem
import com.danghung.elearning.view.adapter.CreAdapter
import com.danghung.elearning.view.fragment.MenuFragment.Companion.TYPE_EXAMS
import com.danghung.elearning.viewmodel.CommomVM


class HomeFragment : BaseFragment<HomeFragmentBinding, CommomVM>() {
    companion object {
        val TAG: String = HomeFragment::class.java.name
    }

    private lateinit var creAdapter: CreAdapter
    private val autoScrollHandler = Handler(Looper.getMainLooper())
    private var autoScrollRunnable: Runnable? = null
    private val autoScrollDelay = 3000L
    private var isScrollingForward = true  // Track hướng scroll

    override fun getClassVM(): Class<CommomVM> = CommomVM::class.java

    override fun initViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): HomeFragmentBinding = HomeFragmentBinding.inflate(inflater, container, false)

    override fun initViews() {
        setupCreViewPager()
        binding.lnReading.setOnClickListener(this)
        binding.lnListening.setOnClickListener(this)
        binding.lnWriting.setOnClickListener(this)
        binding.lnSpeaking.setOnClickListener(this)
    }

    override fun clickView(v: View) {
        callBack.showFragment(MenuFragment.TAG, TYPE_EXAMS, false)
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