package com.danghung.elearning.view.fragment

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.danghung.elearning.api.req.UserExamReq
import com.danghung.elearning.api.res.ExamRes
import com.danghung.elearning.databinding.FragmentPracticeBinding
import com.danghung.elearning.model.StartPracticeData
import com.danghung.elearning.view.dialog.StartTestDialog
import com.danghung.elearning.view.fragment.MenuFragment.Companion.TYPE_EXAMS
import com.danghung.elearning.viewmodel.PracticeVM
import com.danghung.elearning.viewmodel.PracticeVM.Companion.START_FULL
import com.danghung.elearning.viewmodel.PracticeVM.Companion.START_SINGLE


class PracticeFragment : BaseFragment<FragmentPracticeBinding, PracticeVM>(), StartTestDialog.OnStartTestCallBack {
    companion object {
        val TAG: String = PracticeFragment::class.java.name
        const val READING = "READING"
        const val LISTENING = "LISTENING"
        const val SPEAKING = "SPEAKING"
        const val WRITING = "WRITING"
        const val FULL = "FULL"
    }

    lateinit var exam: ExamRes
    private var currentSkill: String? = null

    override fun getClassVM(): Class<PracticeVM> = PracticeVM::class.java

    override fun initViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentPracticeBinding = FragmentPracticeBinding.inflate(inflater, container, false)

    override fun initViews() {
        exam = data as ExamRes
        updateUI(exam)

        binding.ivClose.setOnClickListener(this)
        binding.btnStartFullTest.setOnClickListener(this)
        binding.btnStartReading.setOnClickListener(this)
        binding.btnStartWriting.setOnClickListener(this)
        binding.btnStartSpeaking.setOnClickListener(this)
        binding.btnStartListening.setOnClickListener(this)
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(item: ExamRes?) {
        binding.tvTitleExam.text = item?.title
        binding.tvDurationExam.text = "Duration: ${item?.totalDuration} minutes"
    }

    override fun clickView(v: View) {
        when(v.id){
            binding.ivClose.id -> callBack.showFragment(MenuFragment.TAG, TYPE_EXAMS, false)
            binding.btnStartReading.id -> handleStartPractice(READING)
            binding.btnStartWriting.id -> handleStartPractice(WRITING)
            binding.btnStartSpeaking.id -> handleStartPractice(SPEAKING)
            binding.btnStartListening.id -> handleStartPractice(LISTENING)
            binding.btnStartFullTest.id -> handleStartPractice(FULL)
        }
    }

    private fun handleStartPractice(skill: String) {
        val dialog = StartTestDialog(requireContext(), skill)
        dialog.setCallBack(this)
        dialog.show()
    }

    override fun onStart(skill: String) {
        currentSkill = skill
        val typeSkill = if (skill == "FULL") null else skill
        val data = UserExamReq((data as ExamRes).id, typeSkill)
        if(skill == FULL) viewModel.startFullTest(data) else viewModel.startSingleSkill(data)

       // notify("Start practice $skill")
    }

    override fun handleApiSuccess(key: String, data: Any?) {
        when (key) {
            START_SINGLE -> {
                val userExamPartId = data as Long
                val examPart = viewModel.getExamPartBySkill(exam, currentSkill!!)
                if (examPart == null) return
                val startData = StartPracticeData(userExamPartId, exam, examPart)
                when(currentSkill){
                    READING -> callBack.showFragment(ReadingFragment.TAG, startData, false)
                    LISTENING -> callBack.showFragment(ListeningFragment.TAG, startData, false)
                    SPEAKING -> callBack.showFragment(SpeakingFragment.TAG, startData, false)
                    WRITING -> callBack.showFragment(WritingFragment.TAG, startData, false)
                }
            }

            START_FULL -> {
                //val exam = data as UserExamRes
                notify("Function is under development, please try single skill practice")
            }
        }
    }

}