package com.danghung.elearning.view.fragment

import android.annotation.SuppressLint
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.danghung.elearning.R
import com.danghung.elearning.api.req.SubmitSpeakingExamReq
import com.danghung.elearning.api.res.ApiErrorRes
import com.danghung.elearning.api.res.IELTSSpeakingRes
import com.danghung.elearning.api.res.QuestionRes
import com.danghung.elearning.databinding.FragmentSpeakingBinding
import com.danghung.elearning.model.StartPracticeData
import com.danghung.elearning.record.AndroidAudioPlayer
import com.danghung.elearning.record.AndroidAudioRecorder
import com.danghung.elearning.record.AudioPlayer
import com.danghung.elearning.record.AudioRecorder
import com.danghung.elearning.view.dialog.ErrorDialog
import com.danghung.elearning.view.dialog.SubmitSuccessDialog
import com.danghung.elearning.view.fragment.MenuFragment.Companion.TYPE_LEARN
import com.danghung.elearning.viewmodel.SpeakingVM
import com.danghung.elearning.viewmodel.SpeakingVM.Companion.EVALUATE_SPEAKING
import com.danghung.elearning.viewmodel.SpeakingVM.Companion.GET_QUESTIONS_SPEAKING
import com.danghung.elearning.viewmodel.SpeakingVM.Companion.SUBMIT_SPEAKING
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@SuppressLint("SetTextI18n")
class SpeakingFragment : BaseFragment<FragmentSpeakingBinding, SpeakingVM>(),
    SubmitSuccessDialog.OnResultCallBack {
    companion object {
        val TAG: String = SpeakingFragment::class.java.name
    }

    private lateinit var startData: StartPracticeData
    private lateinit var tempFile: File
    private lateinit var player: AudioPlayer
    private lateinit var recorder: AudioRecorder
    private var currentQuestionId: Long = 0
    private var isPlaying = false
    private var isRecording = false
    private val duration: Int
        get() = startData.examPartRes?.duration ?: 0

    override fun getClassVM(): Class<SpeakingVM> = SpeakingVM::class.java

    override fun initViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentSpeakingBinding = FragmentSpeakingBinding.inflate(inflater, container, false)

    override fun initViews() {
        startData = data as StartPracticeData
        setupRecording()
        callBack.showLoading()
        viewModel.getQuestions(startData.examPartRes?.id ?: 0)

        startCountdown()

        binding.btnStartRecording.setOnClickListener(this)
        binding.btnStopRecording.setOnClickListener(this)
        binding.btnSubmitSpeaking.setOnClickListener(this)
        binding.ivRecordAnswer.setOnClickListener(this)
    }

    private fun setupRecording() {
        recorder = AndroidAudioRecorder(requireContext())
        player = AndroidAudioPlayer(requireContext())
        tempFile = File(requireContext().filesDir, "speaking_temp.m4a")
        binding.ivRecordAnswer.isEnabled = false
        disableButton(binding.btnStopRecording)
        disableButton(binding.btnSubmitSpeaking)
    }

    override fun clickView(v: View) {
        when (v.id) {
            binding.ivRecordAnswer.id -> handlePlayRecording()
            binding.btnStartRecording.id -> startRecording()
            binding.btnStopRecording.id -> stopRecording()
            binding.btnSubmitSpeaking.id -> handleSubmit()
        }
    }

    private fun handlePlayRecording() {
        if (!tempFile.exists()) {
            notify("No recording found!")
            return
        }

        if (isPlaying) {
            player.stop()
            isPlaying = false
            enableButton(binding.btnStartRecording)
            enableButton(binding.btnSubmitSpeaking)
            binding.tvRecordingStatus.text = "You can play again or submit your answer."
            notify("Playback stopped.")
        } else {
            disableButton(binding.btnStartRecording)
            disableButton(binding.btnSubmitSpeaking)
            player.playFile(tempFile)
            isPlaying = true
            binding.tvRecordingStatus.text = "Click to stop or record again if needed."
            notify("Playing your recorded answer...")
        }
    }

    private fun startCountdown() {
        viewModel.startCountdown(duration)
        viewModel.formattedTime.observe(viewLifecycleOwner) { time ->
            binding.tvTime.text = time
            if (time == "00:00") onTimeUp()
        }
    }

    private fun startRecording() {
        lifecycleScope.launch(Dispatchers.IO) {
            if (tempFile.exists()) tempFile.delete()
            recorder.start(tempFile)
            isRecording = true

            withContext(Dispatchers.Main) {
                binding.ivRecordAnswer.isEnabled = false
                enableButton(binding.btnStopRecording)
                disableButton(binding.btnStartRecording)
                disableButton(binding.btnSubmitSpeaking)
                binding.tvRecordingStatus.text = "Recording started. Please speak now!"
                notify(binding.tvRecordingStatus.text.toString())
            }
        }
    }


    private fun stopRecording() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                recorder.stop()
                isRecording = false
            }

            // Chỉ chạy khi stop xong
            enableButton(binding.btnStartRecording)
            enableButton(binding.btnSubmitSpeaking)
            disableButton(binding.btnStopRecording)
            binding.ivRecordAnswer.isEnabled = true
            binding.tvRecordingStatus.text =
                "Recording stopped. You can play or submit your answer."
            notify(binding.tvRecordingStatus.text.toString())
        }
    }


    @SuppressLint("SetTextI18n")
    private fun onTimeUp() {
        if (viewModel.isAutoSubmitted()) return
        viewModel.markAutoSubmitted()
        viewModel.stopCountdown()
        binding.tvTime.text = "00:00"

        lifecycleScope.launch {
            // Nếu đang record thì stop trước
            if (isRecording) {
                withContext(Dispatchers.IO) {
                    try {
                        recorder.stop()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                isRecording = false
            }

            submitSpeaking(isAuto = true)
        }
    }

    private fun submitSpeaking(isAuto: Boolean) {
        if (!tempFile.exists()) {
            if (!isAuto) notify("Please record your answer!")
            return
        }

        disableButton(binding.btnSubmitSpeaking)
        callBack.showLoading()

        if (isAuto) notify("Time is up! Auto submitting...")

        viewModel.evaluateSpeaking(
            file = tempFile,
            questionId = currentQuestionId,
            userExamPartId = startData.userExamPartId
        )
    }

    @SuppressLint("SetTextI18n")
    private fun handleSubmit() {
        if (viewModel.isTimeUp()) {
            notify("Time is up!")
            return
        }
        if (!tempFile.exists()) {
            notify("Please record your answer!")
            return
        }

        viewModel.stopCountdown()
        binding.tvTime.text = "00:00"

        submitSpeaking(isAuto = false)
    }

    private fun disableButton(view: View) {
        view.apply {
            isEnabled = false
            isClickable = false
            backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.color_splash_des)
        }
    }

    private fun enableButton(view: View) {
        view.isEnabled = true
        view.isClickable = true

        val colorRes = when (view.id) {
            R.id.btnStopRecording -> R.color.color_stop
            R.id.btnStartRecording -> R.color.color_start
            R.id.btnSubmitSpeaking -> R.color.color_submit
            else -> return
        }
        view.backgroundTintList = ContextCompat.getColorStateList(requireContext(), colorRes)
    }

    @Suppress("UNCHECKED_CAST")
    override fun apiSuccess(key: String, data: Any?) {
        when (key) {

            GET_QUESTIONS_SPEAKING -> {
                val questions = data as List<QuestionRes>
                val firstQuestion = questions.firstOrNull()
                currentQuestionId = firstQuestion?.id ?: 0

                binding.tvSpeakingQuestion.text = Html.fromHtml(
                    firstQuestion?.content ?: "No question available", Html.FROM_HTML_MODE_COMPACT
                )
                callBack.hideLoading()
            }

            EVALUATE_SPEAKING -> {
                val result = data as IELTSSpeakingRes
                handleEvaluationResult(result)
            }

            SUBMIT_SPEAKING -> {
                callBack.hideLoading()
                notify("Submit speaking part successfully!")
                if (tempFile.exists()) tempFile.delete()
                var dialog = SubmitSuccessDialog(requireContext())
                dialog.setCallBack(this)
                dialog.show()
            }
        }
    }

    override fun handleApiError(key: String, code: Int, error: ApiErrorRes?) {
        var dialog = ErrorDialog(requireContext())
        dialog.setCallBack(this)
        dialog.show()
    }

    override fun onResult() {
        callBack.showFragment(MenuFragment.TAG, TYPE_LEARN, false)
    }

    private fun handleEvaluationResult(result: IELTSSpeakingRes) {
        val criteria = result.criteriaScores
        val request = SubmitSpeakingExamReq(
            userExamPartId = startData.userExamPartId,
            overallBand = result.overallBand ?: 0.0,
            fluencyCoherence = criteria?.fluencyCoherence,
            lexicalResource = criteria?.lexicalResource,
            grammaticalRange = criteria?.grammaticalRange,
            pronunciation = criteria?.pronunciation,
            overallFeedback = result.examinerFeedback
        )

        viewModel.submitSpeaking(request)
    }

    override fun onDestroy() {
        viewModel.stopCountdown()

        if (isRecording) {
            try {
                recorder.stop()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isRecording = false
        }

        if (tempFile.exists()) tempFile.delete()
        super.onDestroy()
    }
}