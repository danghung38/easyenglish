package com.danghung.elearning.view.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.danghung.elearning.api.res.TestHistoryRes
import com.danghung.elearning.api.res.TopicRes
import com.danghung.elearning.databinding.FragmentLearnBinding
import com.danghung.elearning.view.adapter.HistoryAdapter
import com.danghung.elearning.view.adapter.TopicAdapter
import com.danghung.elearning.view.fragment.PracticeFragment.Companion.LISTENING
import com.danghung.elearning.view.fragment.PracticeFragment.Companion.READING
import com.danghung.elearning.viewmodel.LearnVM
import com.danghung.elearning.viewmodel.LearnVM.Companion.GET_HISTORY
import com.danghung.elearning.viewmodel.LearnVM.Companion.GET_TOPICS


class LearnFragment : BaseFragment<FragmentLearnBinding, LearnVM>(),
    TopicAdapter.OnTopicClickListener, HistoryAdapter.OnHistoryClickListener {
    companion object {
        val TAG: String = LearnFragment::class.java.name
    }

    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var topicAdapter: TopicAdapter

    override fun getClassVM(): Class<LearnVM> = LearnVM::class.java

    override fun initViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentLearnBinding = FragmentLearnBinding.inflate(inflater, container, false)

    override fun initViews() {
        // History
        historyAdapter = HistoryAdapter(emptyList(), this)
        binding.rvHistoryExams.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = historyAdapter
        }

        // Topics
        topicAdapter = TopicAdapter(emptyList(), this)
        binding.rvVocabulary.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = topicAdapter
        }

        // CALL API SONG SONG
        callBack.showLoading()
        viewModel.getHistory()

        callBack.showLoading()
        viewModel.getTopics()

    }

    @Suppress("UNCHECKED_CAST")
    override fun handleApiSuccess(key: String, data: Any?) {
        when (key) {
            GET_HISTORY -> {
                val list = data as List<TestHistoryRes>
                historyAdapter = HistoryAdapter(list, this)
                binding.rvHistoryExams.adapter = historyAdapter
            }

            GET_TOPICS -> {
                val topics = data as List<TopicRes>
                topicAdapter = TopicAdapter(topics, this)
                binding.rvVocabulary.adapter = topicAdapter
            }
        }
    }

    override fun onTopicClick(topic: TopicRes) {
        notify("Selected topic: ${topic.title}")
        callBack.showFragment(VocabularyFragment.TAG, topic.title, false)
    }

    override fun onHistoryClick(item: TestHistoryRes, key: String) {
        //notify("Selected history: key=$key")
        when(item.skillType){
            READING -> callBack.showFragment(ReadingHistoryFragment.TAG, item.userExamPartId, false)
            LISTENING -> callBack.showFragment(ListeningHistoryFragment.TAG, item.userExamPartId, false)
             else -> notify("Unknown skill type: ${item.skillType}")
        }
    }


}