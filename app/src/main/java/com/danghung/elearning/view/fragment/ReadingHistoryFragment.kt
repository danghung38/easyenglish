package com.danghung.elearning.view.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.danghung.elearning.api.res.UserAnswerRes
import com.danghung.elearning.databinding.FragmentReadingHistoryBinding
import com.danghung.elearning.view.adapter.ReadingHistoryAdapter
import com.danghung.elearning.view.fragment.MenuFragment.Companion.TYPE_LEARN
import com.danghung.elearning.viewmodel.ReadingVM
import com.danghung.elearning.viewmodel.ReadingVM.Companion.GET_READING_HISTORY


class ReadingHistoryFragment : BaseFragment<FragmentReadingHistoryBinding, ReadingVM>() {
    companion object {
        val TAG: String = ReadingHistoryFragment::class.java.name
    }

    private lateinit var historyAdapter: ReadingHistoryAdapter
    private var userExamPartId: Long = 0L

    override fun getClassVM(): Class<ReadingVM> = ReadingVM::class.java

    override fun initViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentReadingHistoryBinding =
        FragmentReadingHistoryBinding.inflate(inflater, container, false)

    override fun initViews() {
        userExamPartId = data as? Long ?: 0L
        callBack.showLoading()
        viewModel.getReadingHistory(userExamPartId)

        binding.ivBack.setOnClickListener {
            callBack.showFragment(
                MenuFragment.TAG, TYPE_LEARN, false
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun handleApiSuccess(key: String, data: Any?) {
        when (key) {
            GET_READING_HISTORY -> {
                val res = data as? List<UserAnswerRes> ?: emptyList()
                historyAdapter = ReadingHistoryAdapter(res)
                binding.rvReadingHistory.apply {
                    layoutManager = LinearLayoutManager(context)
                    adapter = historyAdapter
                }
            }
        }
    }

}