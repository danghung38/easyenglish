package com.danghung.elearning.view.fragment

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import com.danghung.elearning.api.res.UserAnswerRes
import com.danghung.elearning.databinding.FragmentListeningHistoryBinding
import com.danghung.elearning.view.adapter.ListeningHistoryAdapter
import com.danghung.elearning.view.fragment.MenuFragment.Companion.TYPE_LEARN
import com.danghung.elearning.viewmodel.ListeningVM
import com.danghung.elearning.viewmodel.ListeningVM.Companion.GET_LISTENING_HISTORY
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("SetTextI18n")
class ListeningHistoryFragment : BaseFragment<FragmentListeningHistoryBinding, ListeningVM>(),
    ListeningHistoryAdapter.OnListeningHistoryListener {
    companion object {
        val TAG: String = ListeningHistoryFragment::class.java.name
    }

    private var userExamPartId: Long = 0L
    private lateinit var adapter: ListeningHistoryAdapter
    private lateinit var player: ExoPlayer

    private var currentPlayingPosition = -1
    private var job: Job? = null

    override fun getClassVM(): Class<ListeningVM> = ListeningVM::class.java

    override fun initViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentListeningHistoryBinding =
        FragmentListeningHistoryBinding.inflate(inflater, container, false)

    override fun initViews() {
        userExamPartId = data as? Long ?: 0L

        setupPlayer()
        adapter = ListeningHistoryAdapter(emptyList(), this)
        binding.rvListeningHistory.layoutManager = LinearLayoutManager(context)
        binding.rvListeningHistory.adapter = adapter

        callBack.showLoading()
        viewModel.getListeningHistory(userExamPartId)


        binding.ivBack.setOnClickListener {
            callBack.showFragment(
                MenuFragment.TAG, TYPE_LEARN, false
            )
        }
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

    @Suppress("UNCHECKED_CAST")
    override fun handleApiSuccess(key: String, data: Any?) {
        when (key) {
            GET_LISTENING_HISTORY -> {
                val res = data as? List<UserAnswerRes> ?: emptyList()
                adapter.submitList(res)
                binding.rvListeningHistory.adapter = adapter
            }
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

                    val viewHolder = binding.rvListeningHistory.findViewHolderForAdapterPosition(
                        currentPlayingPosition
                    ) as? ListeningHistoryAdapter.ListeningHistoryHolder

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

}