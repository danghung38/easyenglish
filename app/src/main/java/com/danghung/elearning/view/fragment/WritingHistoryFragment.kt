package com.danghung.elearning.view.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.danghung.elearning.api.res.UserAnswerRes
import com.danghung.elearning.databinding.FragmentWritingHistoryBinding
import com.danghung.elearning.view.adapter.WritingHistoryAdapter
import com.danghung.elearning.view.fragment.MenuFragment.Companion.TYPE_LEARN
import com.danghung.elearning.viewmodel.WritingVM
import com.danghung.elearning.viewmodel.WritingVM.Companion.GET_WRITING_HISTORY

class WritingHistoryFragment : BaseFragment<FragmentWritingHistoryBinding, WritingVM>() {
    companion object {
        val TAG: String = WritingHistoryFragment::class.java.name
    }

    private var userExamPartId: Long = 0L
    private val historyAdapter = WritingHistoryAdapter()

    override fun getClassVM(): Class<WritingVM> = WritingVM::class.java

    override fun initViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentWritingHistoryBinding =
        FragmentWritingHistoryBinding.inflate(inflater, container, false)

    override fun initViews() {
        userExamPartId = data as? Long ?: 0L
        binding.rvWritingResults.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }

        callBack.showLoad()
        viewModel.getWritingHistory(userExamPartId)

        binding.ivBack.setOnClickListener {
            callBack.showFragment(MenuFragment.TAG, TYPE_LEARN, false)
        }
        setUpOnBackMenu(TYPE_LEARN)
    }

    @Suppress("UNCHECKED_CAST")
    override fun handleApiSuccess(key: String, data: Any?) {
        if (key != GET_WRITING_HISTORY) return

        callBack.hideLoad()
        val results = data as? List<UserAnswerRes> ?: emptyList()
        historyAdapter.submitList(results)
        binding.rvWritingResults.visibility = if (results.isEmpty()) View.GONE else View.VISIBLE
        binding.tvEmptyWritingHistory.visibility = if (results.isEmpty()) View.VISIBLE else View.GONE
    }
}
