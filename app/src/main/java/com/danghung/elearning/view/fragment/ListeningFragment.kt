package com.danghung.elearning.view.fragment

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import com.danghung.elearning.R
import com.danghung.elearning.api.req.AnswerRLPartReq
import com.danghung.elearning.api.res.QuestionRes
import com.danghung.elearning.api.res.SubmitRLPartRes
import com.danghung.elearning.databinding.FragmentListeningBinding
import com.danghung.elearning.model.StartPracticeData
import com.danghung.elearning.view.adapter.ListeningAdapter
import com.danghung.elearning.view.dialog.SubmitSuccessDialog
import com.danghung.elearning.view.fragment.MenuFragment.Companion.TYPE_LEARN
import com.danghung.elearning.viewmodel.ListeningVM
import com.danghung.elearning.viewmodel.ListeningVM.Companion.GET_QUESTIONS_LISTENING
import com.danghung.elearning.viewmodel.ListeningVM.Companion.SUBMIT_LISTENING
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("SetTextI18n")
class ListeningFragment : BaseFragment<FragmentListeningBinding, ListeningVM>(),
    ListeningAdapter.OnListeningListener, SubmitSuccessDialog.OnResultCallBack {
    companion object {
        val TAG: String = ListeningFragment::class.java.name
    }

    private lateinit var startData: StartPracticeData
    private lateinit var adapter: ListeningAdapter
    private lateinit var player: ExoPlayer

    private var currentPlayingPosition = -1
    private var job: Job? = null

    override fun getClassVM(): Class<ListeningVM> = ListeningVM::class.java

    override fun initViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentListeningBinding = FragmentListeningBinding.inflate(inflater, container, false)

    override fun initViews() {
        startData = data as StartPracticeData

        setupPlayer()
        adapter = ListeningAdapter(emptyList(), this)
        binding.rvListening.layoutManager = LinearLayoutManager(context)
        binding.rvListening.adapter = adapter

        callBack.showLoading()
        viewModel.getQuestions(startData.examPartRes?.id ?: 0)

        viewModel.formattedTime.observe(viewLifecycleOwner) { time ->
            binding.tvTime.text = time
            if (time == "00:00") {
                onTimeUp()
            }
        }
        binding.btnSubmit.setOnClickListener(this)
    }

    private fun setupPlayer() {
        player = ExoPlayer.Builder(requireContext()).build().apply {
            repeatMode = Player.REPEAT_MODE_OFF
            playWhenReady = false
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (currentPlayingPosition != -1) {
                        adapter.updatePlayingState(currentPlayingPosition, isPlaying)
                    }
                    if (isPlaying) startUpdating()
                    else stopUpdating()
                }

                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_ENDED) {
                        pause()
                        seekTo(0)
                    }
                }
            })
        }
    }

    private fun onTimeUp() {
        if (viewModel.isAutoSubmitted()) return
        disableSubmitButton()
        viewModel.markAutoSubmitted()
        notify("Time is up! Auto submitting...")
        viewModel.submit(startData.userExamPartId)
    }

    override fun handleApiSuccess(key: String, data: Any?) {
        when (key) {
            GET_QUESTIONS_LISTENING -> {
                val questions = data as List<QuestionRes>
                adapter.submitList(questions)
                binding.tvProcess.text = "0/${questions.size}"
                viewModel.startCountdown(startData.examPartRes?.duration ?: 15)
            }

            SUBMIT_LISTENING -> {
                data as SubmitRLPartRes
                notify("Submit listening part successfully!")
                var dialog = SubmitSuccessDialog(requireContext())
                dialog.setCallBack(this)
                dialog.show()
            }
        }
    }

    override fun onAnswerSelected(answer: AnswerRLPartReq) {
        viewModel.addOrUpdateAnswer(answer)

        val answered = viewModel.getAnsweredCount()
        val total = adapter.itemCount
        binding.tvProcess.text = "$answered/$total"
    }

    override fun clickView(v: View) {
        when (v.id) {
            binding.btnSubmit.id -> handleSubmit()
        }
    }

    private fun handleSubmit() {
        if (viewModel.isTimeUp()) {
            notify("Time is up!")
            return
        }
        val error = viewModel.validateBeforeSubmit()
        if (error != null) {
            notify(error)
            return
        }
        viewModel.stopCountdown()
        binding.tvTime.text = "00:00"
        disableSubmitButton()
        callBack.showLoading()
        viewModel.submit(startData.userExamPartId)
    }

    private fun disableSubmitButton() {
        binding.btnSubmit.apply {
            isEnabled = false
            isClickable = false
            backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.color_splash_des)
        }
    }


    override fun onPlayClicked(position: Int, audioUrl: String) {
        playAudio(position, audioUrl)
    }

    private fun playAudio(position: Int, url: String) {
        if (currentPlayingPosition == position) {
            if (player.isPlaying) player.pause()
            else player.play()
            return
        }

        currentPlayingPosition = position

        player.stop()
        player.clearMediaItems()

        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
        player.play()
    }

    private fun startUpdating() {

        stopUpdating()

        job = viewLifecycleOwner.lifecycleScope.launch {

            while (player.isPlaying) {

                val duration = player.duration
                val current = player.currentPosition

                if (duration > 0 && currentPlayingPosition != -1) {

                    val viewHolder =
                        binding.rvListening.findViewHolderForAdapterPosition(currentPlayingPosition) as? ListeningAdapter.ListeningHolder

                    viewHolder?.updateProgress(current, duration)
                }

                delay(300)
            }
        }
    }


    private fun stopUpdating() {
        job?.cancel()
        job = null
    }


    override fun onDestroy() {
        stopUpdating()
        player.release()
        super.onDestroy()
    }

    override fun onResult() {
        callBack.showFragment(MenuFragment.TAG, TYPE_LEARN, false)
    }

}