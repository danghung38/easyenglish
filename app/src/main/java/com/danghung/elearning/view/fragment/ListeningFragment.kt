package com.danghung.elearning.view.fragment

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.recyclerview.widget.LinearLayoutManager
import com.danghung.elearning.R
import com.danghung.elearning.api.req.AnswerRLPartReq
import com.danghung.elearning.api.res.QuestionRes
import com.danghung.elearning.api.res.SubmitRLPartRes
import com.danghung.elearning.databinding.FragmentListeningBinding
import com.danghung.elearning.model.StartPracticeData
import com.danghung.elearning.view.adapter.ListeningAdapter
import com.danghung.elearning.view.dialog.ExitDialog
import com.danghung.elearning.view.dialog.FullTestNextDialog
import com.danghung.elearning.view.dialog.ScoreResultDialog
import com.danghung.elearning.view.fragment.MenuFragment.Companion.TYPE_EXAMS
import com.danghung.elearning.viewmodel.ListeningVM
import com.danghung.elearning.viewmodel.ListeningVM.Companion.GET_QUESTIONS_LISTENING
import com.danghung.elearning.viewmodel.ListeningVM.Companion.SUBMIT_LISTENING
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

@SuppressLint("SetTextI18n")
class ListeningFragment : BaseFragment<FragmentListeningBinding, ListeningVM>(),
    ListeningAdapter.OnListeningListener {

    companion object {
        val TAG: String = ListeningFragment::class.java.name
    }

    private lateinit var startData: StartPracticeData
    private lateinit var adapter: ListeningAdapter

    private val playerPool = mutableMapOf<String, ExoPlayer>()

    private var activePlayer: ExoPlayer? = null
    private var activeUrl: String = ""
    private var activePosition: Int = -1

    private var progressJob: Job? = null


    override fun getClassVM(): Class<ListeningVM> = ListeningVM::class.java

    override fun initViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentListeningBinding = FragmentListeningBinding.inflate(inflater, container, false)

    override fun initViews() {
        startData = data as StartPracticeData

        adapter = ListeningAdapter(emptyList(), this)

        // Kết nối Provider giúp Adapter chủ động đọc duration/position thật từ Pool của Fragment
        adapter.playerProvider = { url -> playerPool[url] }

        binding.rvListening.layoutManager = LinearLayoutManager(context)
        binding.rvListening.adapter = adapter
        binding.rvListening.itemAnimator = null

        callBack.showLoading()
        //callBack.showLoad()
        binding.rvListening.visibility = View.INVISIBLE

        viewModel.getQuestions(startData.examPartRes?.id ?: 0)

        viewModel.formattedTime.observe(viewLifecycleOwner) { time ->
            binding.tvTime.text = time
            if (time == "00:00") onTimeUp()
        }

        binding.btnSubmit.setOnClickListener(this)

        onBack()
    }

    private fun onBack() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, object : androidx.activity.OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val dialog = ExitDialog(requireContext())
                    dialog.setCallBack(object : ExitDialog.OnExitCallBack {
                        override fun onExit() {
                            stopAllPlayback()
                            viewModel.stopCountdown()
                            callBack.showFragment(MenuFragment.TAG, TYPE_EXAMS, false)

                            // Hoặc nếu muốn pop fragment thông thường:
                            // parentFragmentManager.popBackStack()
                        }
                    })

                    dialog.show()
                }
            })
    }

    // Preload — tạo, prepare() và chờ toàn bộ player READY mới vào bài học
    private fun preloadAllPlayers(questions: List<QuestionRes>) {
        val urls = questions.mapNotNull { it.audioUrl }.distinct()
        if (urls.isEmpty()) {
            startQuizFlow()
            return
        }

        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setConnectTimeoutMs(8_000)
            .setReadTimeoutMs(8_000)
            .setAllowCrossProtocolRedirects(true)

        val readyCounter = AtomicInteger(0)
        val totalUrls = urls.size

        urls.forEach { url ->
            if (playerPool.containsKey(url)) {
                if (readyCounter.incrementAndGet() == totalUrls) {
                    startQuizFlow()
                }
                return@forEach
            }

            val source: MediaSource = ProgressiveMediaSource
                .Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(url))

            val p = ExoPlayer.Builder(requireContext()).build().apply {
                repeatMode    = Player.REPEAT_MODE_OFF
                playWhenReady = false
                setMediaSource(source)
                prepare()
            }

            p.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_READY || state == Player.STATE_BUFFERING) {
                        p.removeListener(this)
                        if (readyCounter.incrementAndGet() == totalUrls) {
                            startQuizFlow()
                        }
                    }
                }
            })

            playerPool[url] = p
        }
    }

    private fun startQuizFlow() {
        activity?.runOnUiThread {
            callBack.hideLoading()
            // Tại đây adapter tiến hành render lại, hiển thị chuẩn mốc thời gian preload (0:00/0:21)
            adapter.notifyDataSetChanged()
            binding.rvListening.visibility = View.VISIBLE
            viewModel.startCountdown(startData.examPartRes?.duration ?: 15)
        }
    }

    // Playback

    override fun onPlayClicked(position: Int, audioUrl: String) {
        if (audioUrl.isEmpty()) return
        val target = playerPool[audioUrl] ?: return

        // ── Cùng track: toggle ──
        if (activeUrl == audioUrl) {
            if (target.isPlaying) {
                target.pause()
                adapter.setPlayingState(position, false)
                stopProgressUpdating()
            } else {
                if (target.playbackState == Player.STATE_ENDED) target.seekTo(0)
                target.play()
                adapter.setPlayingState(position, true)
                startProgressUpdating(position, target)
            }
            return
        }
        activePlayer?.pause()
        adapter.setPlayingState(activePosition, false)
        stopProgressUpdating()

        activePlayer   = target
        activeUrl      = audioUrl
        activePosition = position

        target.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    target.removeListener(this)
                    target.seekTo(0)
                    adapter.setPlayingState(position, false)
                    stopProgressUpdating()
                }
            }
        })

        when (target.playbackState) {
            Player.STATE_READY -> {
                target.play()
                adapter.setPlayingState(position, true)
                startProgressUpdating(position, target)
            }
            Player.STATE_BUFFERING -> {
                target.playWhenReady = true
                adapter.setPlayingState(position, true)
                startProgressUpdating(position, target)
            }
            else -> {
                target.prepare()
                target.playWhenReady = true
                target.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        if (state == Player.STATE_READY) {
                            target.removeListener(this)
                            adapter.setPlayingState(position, true)
                            startProgressUpdating(position, target)
                        }
                    }
                })
            }
        }
    }

    // Progress
    private fun startProgressUpdating(position: Int, p: ExoPlayer) {
        stopProgressUpdating()
        progressJob = viewLifecycleOwner.lifecycleScope.launch {
            while (true) {
                val duration = p.duration
                val current  = p.currentPosition
                if (duration > 0) {
                    (binding.rvListening
                        .findViewHolderForAdapterPosition(position)
                            as? ListeningAdapter.ListeningHolder)
                        ?.updateProgress(current, duration)
                }
                delay(200)
            }
        }
    }

    private fun stopProgressUpdating() {
        progressJob?.cancel()
        progressJob = null
    }

    // Stop all
    private fun stopAllPlayback() {
        stopProgressUpdating()
        activePlayer?.pause()
        adapter.setPlayingState(activePosition, false)
        activePlayer   = null
        activeUrl      = ""
        activePosition = -1
    }

    override fun handleApiSuccess(key: String, data: Any?) {
        when (key) {
            GET_QUESTIONS_LISTENING -> {
                callBack.showLoading()
                binding.rvListening.visibility = View.INVISIBLE

                val questions = data as List<QuestionRes>
                adapter.submitList(questions)
                binding.tvProcess.text = "0/${adapter.answerableCount}"

                preloadAllPlayers(questions)
            }
            SUBMIT_LISTENING -> {
                val result = data as SubmitRLPartRes
                if (startData.userExamRes != null) {
                    showFullTestProgressDialog(result.score)
                } else {
                    showScoreResult(result.score)
                }
            }
        }
    }

    // Answer
    override fun onAnswerSelected(answer: AnswerRLPartReq) {
        viewModel.addOrUpdateAnswer(answer)
        binding.tvProcess.text = "${viewModel.getAnsweredCount()}/${adapter.answerableCount}"
    }

    override fun clickView(v: View) {
        when (v.id) {
            binding.btnSubmit.id -> handleSubmit()
        }
    }

    private fun handleSubmit() {
        if (viewModel.isTimeUp()) { notify("Time is up!"); return }
        val error = viewModel.validateBeforeSubmit()
        if (error != null) { notify(error); return }
        stopAllPlayback()
        viewModel.stopCountdown()
        binding.tvTime.text = "00:00"
        disableSubmitButton()
        callBack.showLoad()
        //callBack.showLoading()
        viewModel.submit(startData.userExamPartId)
    }

    private fun onTimeUp() {
        if (viewModel.isAutoSubmitted()) return
        stopAllPlayback()
        disableSubmitButton()
        viewModel.markAutoSubmitted()
        notify("Time is up! Auto submitting...")
        viewModel.submit(startData.userExamPartId)
    }

    private fun disableSubmitButton() {
        binding.btnSubmit.apply {
            isEnabled   = false
            isClickable = false
            backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.color_splash_des)
        }
    }

    // Full test
    private fun showFullTestProgressDialog(score: Double?) {
        val nextData = FullTestFlow.getNextStartData(startData)
        if (nextData == null) {
            showScoreResult(score)
            return
        }
        val dialog = FullTestNextDialog(
            requireContext(),
            title     = "Listening band: ${formatBand(score)}\nContinue full test?",
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

    private fun showScoreResult(score: Double?) {
        ScoreResultDialog(requireContext(), "Listening", score) {
            callBack.showFragment(MenuFragment.TAG, TYPE_EXAMS, false)
        }.show()
    }

    private fun formatBand(score: Double?): String =
        score?.let { String.format(Locale.US, "%.1f", it) } ?: "--"


    override fun onDestroy() {
        stopProgressUpdating()
        playerPool.values.forEach { it.release() }
        playerPool.clear()
        super.onDestroy()
    }
}
