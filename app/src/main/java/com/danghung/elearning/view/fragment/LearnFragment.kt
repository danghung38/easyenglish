package com.danghung.elearning.view.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.danghung.elearning.api.res.TestHistoryRes
import com.danghung.elearning.api.res.TopicRes
import com.danghung.elearning.api.res.UserExamPartRes
import com.danghung.elearning.api.res.UserExamRes
import com.danghung.elearning.databinding.FragmentLearnBinding
import com.danghung.elearning.view.adapter.HistoryAdapter
import com.danghung.elearning.view.adapter.HistoryAdapter.Companion.KEY_FULL_TEST
import com.danghung.elearning.view.adapter.TopicAdapter
import com.danghung.elearning.view.dialog.FullTestSkillDialog
import com.danghung.elearning.view.fragment.PracticeFragment.Companion.LISTENING
import com.danghung.elearning.view.fragment.PracticeFragment.Companion.READING
import com.danghung.elearning.view.fragment.PracticeFragment.Companion.SPEAKING
import com.danghung.elearning.view.fragment.PracticeFragment.Companion.WRITING
import com.danghung.elearning.viewmodel.LearnVM
import com.danghung.elearning.viewmodel.LearnVM.Companion.GET_HISTORY
import com.danghung.elearning.viewmodel.LearnVM.Companion.GET_TOPICS
import com.danghung.elearning.viewmodel.LearnVM.Companion.GET_USER_EXAM


class LearnFragment : BaseFragment<FragmentLearnBinding, LearnVM>(),
    TopicAdapter.OnTopicClickListener, HistoryAdapter.OnHistoryClickListener {
    companion object {
        val TAG: String = LearnFragment::class.java.name
    }

    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var topicAdapter: TopicAdapter
    private var pendingFullTestName: String? = null

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

        setupBackPress()

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

            GET_USER_EXAM -> {
                val exam = data as? UserExamRes
                if (exam == null) {
                    notify("No exam data")
                    return
                }
                showFullTestSkillPicker(exam)
            }
        }
    }

    override fun onTopicClick(topic: TopicRes) {
        notify("Selected topic: ${topic.title}")
        callBack.showFragment(VocabularyFragment.TAG, topic.title, false)
    }

    override fun onHistoryClick(item: TestHistoryRes, key: String) {
        //notify("Selected history: key=$key")
        if (key == KEY_FULL_TEST) {
            pendingFullTestName = item.examName
            //callBack.showLoading()
            viewModel.getUserExam(item.userExamId)
            return
        }

        when (item.skillType) {
            READING -> callBack.showFragment(ReadingHistoryFragment.TAG, item.userExamPartId, false)
            LISTENING -> callBack.showFragment(ListeningHistoryFragment.TAG, item.userExamPartId, false)
            SPEAKING -> callBack.showFragment(SpeakingHistoryFragment.TAG, item.userExamPartId, false)
            WRITING -> callBack.showFragment(WritingHistoryFragment.TAG, item.userExamPartId, false)
            else -> notify("Unknown skill type: ${item.skillType}")
        }
    }

    private fun showFullTestSkillPicker(exam: UserExamRes) {
        val submittedParts = exam.parts.filter { it.submitted }
        if (submittedParts.isEmpty()) {
            notify("No submitted parts")
            return
        }

        val ordered = listOf(LISTENING, READING, SPEAKING, WRITING)
        val partsBySkill = submittedParts.associateBy { it.skillType }
        val items = ordered.mapNotNull { skill -> partsBySkill[skill]?.let { it to skill } }

        if (items.isEmpty()) {
            notify("No submitted parts")
            return
        }

        val title = pendingFullTestName?.let { "Select skill - $it" } ?: "Select skill"
        val dialog = FullTestSkillDialog(requireContext(), title, items.map { it.first })
        dialog.setCallBack(object : FullTestSkillDialog.OnSkillSelectedCallBack {
            override fun onSkillSelected(item: UserExamPartRes) {
                openHistoryForSkill(item.skillType, item)
            }
        })
        dialog.show()
    }

    private fun openHistoryForSkill(skill: String, part: UserExamPartRes) {
        when (skill) {
            READING -> callBack.showFragment(ReadingHistoryFragment.TAG, part.id, false)
            LISTENING -> callBack.showFragment(ListeningHistoryFragment.TAG, part.id, false)
            SPEAKING -> callBack.showFragment(SpeakingHistoryFragment.TAG, part.id, false)
            WRITING -> callBack.showFragment(WritingHistoryFragment.TAG, part.id, false)
            else -> notify("Unknown skill type: $skill")
        }
    }

}