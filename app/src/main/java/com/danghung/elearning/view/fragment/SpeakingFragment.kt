package com.danghung.elearning.view.fragment

import android.annotation.SuppressLint
import android.os.SystemClock
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.danghung.elearning.R
import com.danghung.elearning.api.res.ApiErrorRes
import com.danghung.elearning.api.res.IELTSSpeakingRes
import com.danghung.elearning.api.res.QuestionRes
import com.danghung.elearning.databinding.FragmentSpeakingBinding
import com.danghung.elearning.model.StartPracticeData
import com.danghung.elearning.record.AndroidAudioPlayer
import com.danghung.elearning.record.AndroidAudioRecorder
import com.danghung.elearning.record.AudioPlayer
import com.danghung.elearning.record.AudioRecorder
import com.danghung.elearning.record.SpeakingAudioImporter
import com.danghung.elearning.view.dialog.FullTestNextDialog
import com.danghung.elearning.view.dialog.ErrorDialog
import com.danghung.elearning.view.dialog.ExitDialog
import com.danghung.elearning.view.dialog.SubmitSuccessDialog
import com.danghung.elearning.view.fragment.MenuFragment.Companion.TYPE_EXAMS
import com.danghung.elearning.viewmodel.SpeakingVM
import com.danghung.elearning.viewmodel.SpeakingVM.Companion.EVALUATE_SPEAKING
import com.danghung.elearning.viewmodel.SpeakingVM.Companion.GET_QUESTIONS_SPEAKING
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import com.danghung.elearning.api.res.EvaluationJobRes
import com.danghung.elearning.evaluation.SpeakingParts
import com.danghung.elearning.view.dialog.ScoreResultDialog
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings

@SuppressLint("SetTextI18n")
class SpeakingFragment : BaseFragment<FragmentSpeakingBinding, SpeakingVM>(),
    SubmitSuccessDialog.OnResultCallBack {
    companion object {
        val TAG: String = SpeakingFragment::class.java.name
        private const val MIN_RECORDING_DURATION_MS = 3_000L
    }

    private lateinit var startData: StartPracticeData
    private lateinit var tempFile: File
    private lateinit var player: AudioPlayer
    private lateinit var recorder: AudioRecorder
    private lateinit var audioImporter: SpeakingAudioImporter
    private var answerAudioFile: File? = null
    private var answerAudioMimeType = "audio/mp4"
    private var currentQuestionId: Long = 0
    private var isPlaying = false
    private var isRecording = false
    private var recordingStartedAtMs = 0L
    private var parts: List<List<QuestionRes>> = emptyList()
    private var partIndex = 0
    private var scoring = false
    private var pollCount = 0
    private var resultPollJob: Job? = null
    private var scoreResultDialog: ScoreResultDialog? = null
    private val duration: Int
        get() = startData.examPartRes?.duration ?: 0

    private val pickAudioLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let(::importSelectedAudio)
    }

    private val requestAudioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Nếu user vừa cấp quyền sau khi bấm nút Start Recording thì tự động thu âm luôn
            if (pendingRecordAfterPermission) {
                pendingRecordAfterPermission = false
                startRecording()
            }
        } else {
            pendingRecordAfterPermission = false
            if (!shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                // User đã chọn "Don't ask again" -> mở Cài đặt ứng dụng để user tự bật
                notify("Microphone permission is permanently denied. Please enable it in App Settings.")
                openAppSettings()
            } else {
                notify("Microphone permission is required to record your speaking answer.")
            }
        }
    }

    private var pendingRecordAfterPermission = false

    override fun getClassVM(): Class<SpeakingVM> = SpeakingVM::class.java

    override fun initViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentSpeakingBinding = FragmentSpeakingBinding.inflate(inflater, container, false)

    override fun initViews() {
        startData = data as StartPracticeData
        setupRecording()
        requestAudioPermissionOnEnter() // <-- Thêm dòng này

        callBack.showLoad()
        viewModel.getQuestions(startData.examPartRes?.id ?: 0)


        startCountdown()

        binding.btnStartRecording.setOnClickListener(this)
        binding.btnStopRecording.setOnClickListener(this)
        binding.btnImportAudio.setOnClickListener(this)
        binding.btnSubmitSpeaking.setOnClickListener(this)
        binding.ivRecordAnswer.setOnClickListener(this)

        onBack()
    }

    private fun onBack() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, object : androidx.activity.OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val dialog = ExitDialog(requireContext())
                    dialog.setCallBack(object : ExitDialog.OnExitCallBack {
                        override fun onExit() {
                            // 1. Dừng đếm ngược thời gian làm bài
                            viewModel.stopCountdown()

                            // 2. Tắt âm thanh phát nếu đang nghe lại ghi âm
                            if (isPlaying) {
                                try {
                                    player.stop()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                isPlaying = false
                            }

                            // 3. Sử dụng Coroutine Scope của Fragment để dừng ghi âm an toàn (nếu đang thu)
                            if (isRecording) {
                                lifecycleScope.launch(Dispatchers.IO) {
                                    try {
                                        recorder.stop()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                    isRecording = false

                                    // Sau khi hạ hết các cờ phần cứng ở background, quay về Main thread để chuyển màn
                                    withContext(Dispatchers.Main) {
                                        // Xóa file tạm nếu cần thiết khi thoát ngang bài làm
                                        if (tempFile.exists()) tempFile.delete()

                                        // Điều hướng thoát ra màn hình Menu
                                        callBack.showFragment(MenuFragment.TAG, TYPE_EXAMS, false)
                                    }
                                }
                            } else {
                                // Trường hợp không ghi âm, chỉ cần xóa file tạm và thoát ngay
                                if (tempFile.exists()) tempFile.delete()
                                callBack.showFragment(MenuFragment.TAG, TYPE_EXAMS, false)
                            }
                        }
                    })

                    dialog.show()
                }
            })
    }

    private fun setupRecording() {
        recorder = AndroidAudioRecorder(requireContext())
        player = AndroidAudioPlayer(requireContext())
        audioImporter = SpeakingAudioImporter(requireContext())
        tempFile = File(requireContext().filesDir, "speaking_temp.m4a")
        binding.ivRecordAnswer.isEnabled = false
        disableButton(binding.btnStopRecording)
        disableButton(binding.btnSubmitSpeaking)
    }

    override fun clickView(v: View) {
        when (v.id) {
            binding.ivRecordAnswer.id -> handlePlayRecording()
            binding.btnStartRecording.id -> checkPermissionAndRecord()
            binding.btnStopRecording.id -> stopRecording()
            binding.btnImportAudio.id -> chooseAudioFile()
            binding.btnSubmitSpeaking.id -> handleSubmit()
        }
    }

    private fun handlePlayRecording() {
        val audioFile = currentAnswerAudio()
        if (audioFile == null) {
            notify("No recording or imported audio found!")
            return
        }

        if (isPlaying) {
            player.stop()
            isPlaying = false
            enableButton(binding.btnStartRecording)
            enableButton(binding.btnImportAudio)
            enableButton(binding.btnSubmitSpeaking)
            binding.tvRecordingStatus.text = "You can play again or submit your answer."
            notify("Playback stopped.")
        } else {
            disableButton(binding.btnStartRecording)
            disableButton(binding.btnImportAudio)
            disableButton(binding.btnSubmitSpeaking)
            player.playFile(audioFile)
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
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    deleteImportedAudio()
                    if (tempFile.exists()) tempFile.delete()
                    recorder.start(tempFile)
                }
            }
            result.onSuccess {
                answerAudioFile = tempFile
                answerAudioMimeType = "audio/mp4"
                isRecording = true
                recordingStartedAtMs = SystemClock.elapsedRealtime()
                binding.ivRecordAnswer.isEnabled = false
                enableButton(binding.btnStopRecording)
                disableButton(binding.btnStartRecording)
                disableButton(binding.btnImportAudio)
                disableButton(binding.btnSubmitSpeaking)
                binding.tvRecordingStatus.text = "Recording started. Please speak now!"
                notify(binding.tvRecordingStatus.text.toString())
            }.onFailure { error ->
                isRecording = false
                recordingStartedAtMs = 0L
                answerAudioFile = null
                if (tempFile.exists()) tempFile.delete()
                enableButton(binding.btnStartRecording)
                enableButton(binding.btnImportAudio)
                disableButton(binding.btnStopRecording)
                disableButton(binding.btnSubmitSpeaking)
                binding.tvRecordingStatus.text = getString(R.string.txt_recording_failed)
                notify(error.message ?: getString(R.string.txt_recording_failed))
            }
        }
    }


    private fun stopRecording() {
        lifecycleScope.launch {
            val recordedDurationMs = SystemClock.elapsedRealtime() - recordingStartedAtMs
            val result = withContext(Dispatchers.IO) {
                runCatching { recorder.stop() }
            }
            isRecording = false
            recordingStartedAtMs = 0L

            if (result.isFailure || !tempFile.exists() || tempFile.length() == 0L) {
                answerAudioFile = null
                if (tempFile.exists()) tempFile.delete()
                enableButton(binding.btnStartRecording)
                enableButton(binding.btnImportAudio)
                disableButton(binding.btnStopRecording)
                disableButton(binding.btnSubmitSpeaking)
                binding.ivRecordAnswer.isEnabled = false
                binding.tvRecordingStatus.text = getString(R.string.txt_recording_failed)
                notify(result.exceptionOrNull()?.message ?: getString(R.string.txt_recording_failed))
                return@launch
            }

            if (recordedDurationMs < MIN_RECORDING_DURATION_MS) {
                answerAudioFile = null
                tempFile.delete()
                enableButton(binding.btnStartRecording)
                enableButton(binding.btnImportAudio)
                disableButton(binding.btnStopRecording)
                disableButton(binding.btnSubmitSpeaking)
                binding.ivRecordAnswer.isEnabled = false
                binding.tvRecordingStatus.text = getString(R.string.txt_recording_too_short)
                notify(getString(R.string.txt_recording_too_short))
                return@launch
            }

            // Chỉ chạy khi stop xong
            enableButton(binding.btnStartRecording)
            enableButton(binding.btnImportAudio)
            enableButton(binding.btnSubmitSpeaking)
            disableButton(binding.btnStopRecording)
            binding.ivRecordAnswer.isEnabled = true
            binding.tvRecordingStatus.text =
                "Recording stopped. You can play or submit your answer."
            notify(binding.tvRecordingStatus.text.toString())
        }
    }

    private fun chooseAudioFile() {
        if (viewModel.isTimeUp()) {
            notify("Time is up!")
            return
        }
        if (isRecording) {
            notify("Stop the current recording before choosing a file.")
            return
        }
        pickAudioLauncher.launch(arrayOf("audio/*"))
    }

    private fun importSelectedAudio(uri: android.net.Uri) {
        lifecycleScope.launch {
            binding.tvRecordingStatus.text = "Importing audio..."
            disableButton(binding.btnImportAudio)
            disableButton(binding.btnSubmitSpeaking)

            val result = withContext(Dispatchers.IO) {
                runCatching { audioImporter.import(uri) }
            }
            result.onSuccess { imported ->
                stopPlaybackIfNeeded()
                if (tempFile.exists()) tempFile.delete()
                answerAudioFile = imported.file
                answerAudioMimeType = imported.mimeType
                binding.ivRecordAnswer.isEnabled = true
                enableButton(binding.btnStartRecording)
                enableButton(binding.btnImportAudio)
                enableButton(binding.btnSubmitSpeaking)
                disableButton(binding.btnStopRecording)
                binding.tvRecordingStatus.text =
                    "Selected: ${imported.displayName}. Tap the icon to preview."
                notify("Audio file is ready for evaluation.")
            }.onFailure { error ->
                enableButton(binding.btnImportAudio)
                if (currentAnswerAudio() != null) enableButton(binding.btnSubmitSpeaking)
                binding.tvRecordingStatus.text = "Choose another audio file or record an answer."
                notify(error.message ?: "Cannot import the selected audio file.")
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun onTimeUp() {
        if (scoring) return
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
        val audioFile = currentAnswerAudio()
        if (audioFile == null) {
            if (!isAuto) notify("Please record or choose an audio file!")
            return
        }

        disableButton(binding.btnSubmitSpeaking)
        disableButton(binding.btnStartRecording)
        disableButton(binding.btnImportAudio)
        binding.ivRecordAnswer.isEnabled = false
        callBack.showLoading()

        if (isAuto) notify("Time is up! Auto submitting...")

        viewModel.evaluateSpeaking(
            file = audioFile,
            questionId = currentQuestionId,
            userExamPartId = startData.userExamPartId,
            mimeType = answerAudioMimeType
        )
    }

    @SuppressLint("SetTextI18n")
    private fun handleSubmit() {
        if (viewModel.isTimeUp()) {
            notify("Time is up!")
            return
        }
        if (currentAnswerAudio() == null) {
            notify("Please record or choose an audio file!")
            return
        }

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
            R.id.btnImportAudio -> R.color.color_app
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
                parts = SpeakingParts.group(questions)
                if (parts.size != 3) notify("Đề Speaking chưa đủ Part 1, 2, 3.")
                showCurrentPart()
                //callBack.hideLoading()
                callBack.hideLoad()
            }

            EVALUATE_SPEAKING -> {
                clearWorkingAudio()
                if (partIndex < parts.lastIndex && !viewModel.isTimeUp()) {
                    partIndex++
                    showCurrentPart()
                    callBack.hideLoading()
                } else if (partIndex == parts.lastIndex) {
                    scoring = true
                    viewModel.stopCountdown()
                    viewModel.completeSpeaking(startData.userExamPartId)
                } else {
                    callBack.hideLoading()
                    notify("Chưa đủ 3 part. Hãy hoàn thành các part còn lại.")
                }
            }

            SpeakingVM.SPEAKING_RESULT -> handleSessionJob(data as EvaluationJobRes)
            SpeakingVM.SPEAKING_FEEDBACK -> {
                callBack.hideLoading()
                callBack.showFragment(SpeakingHistoryFragment.TAG, startData.userExamPartId, false)
            }

        }
    }

    private fun showCurrentPart() {
        val questions = parts.getOrNull(partIndex) ?: return
        currentQuestionId = questions.first().id
        binding.tvSpeakingQuestion.text = Html.fromHtml(
            "<b>Speaking Part ${partIndex + 1} / 3</b><br>" +
                questions.mapIndexed { index, question -> "${index + 1}. ${question.content.orEmpty()}" }.joinToString("<br><br>"),
            Html.FROM_HTML_MODE_COMPACT
        )
        binding.tvSubmitSpeaking.text = if (partIndex == parts.lastIndex) "SUBMIT & GET SCORE" else "SAVE PART & CONTINUE"
        binding.tvRecordingStatus.text = "Record or import one audio answering all questions of this part."
        binding.ivRecordAnswer.isEnabled = false
        enableButton(binding.btnStartRecording)
        enableButton(binding.btnImportAudio)
        disableButton(binding.btnSubmitSpeaking)
    }

    private fun handleSessionJob(job: EvaluationJobRes) {
        if (!job.isTerminal()) {
            scheduleResultPoll()
            return
        }
        callBack.hideLoading()
        if (!job.hasDisplayableScore()) {
            notify(job.errorMessage ?: "Không thể chấm audio. Hãy kiểm tra lại chất lượng bản ghi.")
            return
        }
        val result = Gson().fromJson(Gson().toJson(job.result), IELTSSpeakingRes::class.java)
        showOrUpdateScoreResult(result)

        if (job.shouldPollForFinalScore()) {
            scheduleResultPoll()
        } else {
            resultPollJob?.cancel()
            resultPollJob = null
        }
    }

    private fun scheduleResultPoll() {
        if (++pollCount > 300) {
            callBack.hideLoading()
            scoreResultDialog?.setAwaitingFinalScore(false)
            notify("Chấm bài chưa hoàn tất. Mở lại Detail để kiểm tra.")
            return
        }
        resultPollJob?.cancel()
        resultPollJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(1500)
            viewModel.getSpeakingResult(startData.userExamPartId)
        }
    }

    private fun showOrUpdateScoreResult(result: IELTSSpeakingRes) {
        val skillName = if (result.scoreStatus == "PROVISIONAL") {
            "Speaking (provisional)"
        } else {
            "Speaking (AI-estimated)"
        }
        val score = result.overallBand ?: result.languageBand
        val scoreLabel = if (result.overallBand == null) {
            "Language estimate (3 criteria)"
        } else {
            "AI-estimated Speaking band"
        }
        val awaitingFinalScore =
            result.scoreStatus == "PROVISIONAL" && result.reviewError == null

        val currentDialog = scoreResultDialog
        if (currentDialog?.isShowing == true) {
            currentDialog.updateResult(
                skillName,
                score,
                scoreLabel,
                awaitingFinalScore,
            )
            return
        }

        val dialog = ScoreResultDialog(
            requireContext(),
            skillName,
            score,
            scoreLabel = scoreLabel,
            awaitingFinalScore = awaitingFinalScore,
            onRequestFeedback = {
                callBack.showLoading()
                viewModel.requestSpeakingFeedback(startData.userExamPartId)
            },
            onDone = { if (startData.userExamRes != null) showFullTestProgressDialog() else onResult() }
        )
        dialog.setOnDismissListener {
            if (scoreResultDialog === dialog) scoreResultDialog = null
        }
        scoreResultDialog = dialog
        dialog.show()
    }

    override fun handleApiError(key: String, code: Int, error: ApiErrorRes?) {
        callBack.hideLoading()
        enableButton(binding.btnImportAudio)
        if (currentAnswerAudio() != null) enableButton(binding.btnSubmitSpeaking)
        if (key == EVALUATE_SPEAKING && (code == 400 || code == 422)) {
            binding.tvRecordingStatus.text =
                error?.message ?: getString(R.string.txt_speaking_audio_rejected)
            return
        }
        val dialog = ErrorDialog(requireContext())
        dialog.setCallBack(this)
        dialog.show()
    }

    override fun onResult() {
        if (startData.userExamRes != null) {
            callBack.showFragment(MenuFragment.TAG, TYPE_EXAMS, false)
        } else {
            callBack.showFragment(SpeakingHistoryFragment.TAG, startData.userExamPartId, false)
        }
    }

    override fun onDestroyView() {
        resultPollJob?.cancel()
        scoreResultDialog?.dismiss()
        scoreResultDialog = null
        super.onDestroyView()
    }

    private fun showFullTestProgressDialog() {
        val nextData = FullTestFlow.getNextStartData(startData)
        if (nextData == null) {
            notify("Full test completed!")
            val dialog = SubmitSuccessDialog(requireContext())
            dialog.setCallBack(this)
            dialog.show()
            return
        }

        val dialog = FullTestNextDialog(
            requireContext(),
            title = "Continue full test?",
            nextLabel = "NEXT",
            exitLabel = "EXIT"
        )
        dialog.setCallBack(object : FullTestNextDialog.OnFullTestNextCallBack {
            override fun onNext() {
                val nextTag = FullTestFlow.getFragmentTag(nextData.examPartRes?.skillType)
                if (nextTag == null) {
                    notify("Unsupported skill type")
                    callBack.showFragment(MenuFragment.TAG, TYPE_EXAMS, false)
                    return
                }
                callBack.showFragment(nextTag, nextData, false)
            }

            override fun onExit() {
                callBack.showFragment(MenuFragment.TAG, TYPE_EXAMS, false)
            }
        })
        dialog.show()
    }

    private fun hasAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestAudioPermissionOnEnter() {
        if (!hasAudioPermission()) {
            pendingRecordAfterPermission = false
            requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun checkPermissionAndRecord() {
        if (hasAudioPermission()) {
            startRecording()
        } else {
            pendingRecordAfterPermission = true
            requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", requireContext().packageName, null)
        }
        startActivity(intent)
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

        stopPlaybackIfNeeded()
        clearWorkingAudio()
        super.onDestroy()
    }

    private fun currentAnswerAudio(): File? = answerAudioFile?.takeIf(File::exists)

    private fun stopPlaybackIfNeeded() {
        if (!isPlaying) return
        runCatching { player.stop() }
        isPlaying = false
    }

    private fun deleteImportedAudio() {
        answerAudioFile
            ?.takeIf { it != tempFile && it.exists() }
            ?.delete()
        answerAudioFile = null
    }

    private fun clearWorkingAudio() {
        deleteImportedAudio()
        if (tempFile.exists()) tempFile.delete()
        answerAudioFile = null
    }
}
