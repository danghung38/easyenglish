package com.danghung.elearning.view.fragment

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
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
import java.util.concurrent.atomic.AtomicInteger

@SuppressLint("SetTextI18n")
class ListeningHistoryFragment : BaseFragment<FragmentListeningHistoryBinding, ListeningVM>(),
    ListeningHistoryAdapter.OnListeningHistoryListener {

    companion object {
        val TAG: String = ListeningHistoryFragment::class.java.name
    }

    private var userExamPartId: Long = 0L
    private lateinit var adapter: ListeningHistoryAdapter

    /** Mỗi URL có 1 ExoPlayer đã prepare() sẵn — phát gần như tức thì */
    private val playerPool = mutableMapOf<String, ExoPlayer>()

    private var activePlayer: ExoPlayer? = null
    private var activeUrl: String = ""
    private var activePosition: Int = -1

    private var progressJob: Job? = null


    override fun getClassVM(): Class<ListeningVM> = ListeningVM::class.java

    override fun initViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentListeningHistoryBinding =
        FragmentListeningHistoryBinding.inflate(inflater, container, false)

    override fun initViews() {
        userExamPartId = data as? Long ?: 0L

        adapter = ListeningHistoryAdapter(emptyList(), this)

        // Đăng ký provider giúp đồng bộ hóa dữ liệu thời gian thực từ Fragment pool vào Adapter
        adapter.playerProvider = { url -> playerPool[url] }

        binding.rvListeningHistory.layoutManager = LinearLayoutManager(context)
        binding.rvListeningHistory.adapter = adapter
        binding.rvListeningHistory.itemAnimator = null

        // Hiện loading ngay từ đầu, ẩn tạm danh sách câu hỏi lịch sử đi để tránh nhấp nháy UI
        callBack.showLoad()
        //callBack.showLoading()
        binding.rvListeningHistory.visibility = View.INVISIBLE

        viewModel.getListeningHistory(userExamPartId)

        binding.ivBack.setOnClickListener {
            callBack.showFragment(MenuFragment.TAG, TYPE_LEARN, false)
        }
        setUpOnBackMenu(TYPE_LEARN)
    }

    // Preload — prepare() toàn bộ player ngay khi có dữ liệu

    private fun preloadAllPlayers(items: List<UserAnswerRes>) {
        val urls = items.mapNotNull { it.question?.audioUrl }.distinct()
        if (urls.isEmpty()) {
            startHistoryFlow()
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
                    startHistoryFlow()
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
                prepare()   // buffer ngầm, chưa play
            }

            // Chờ player chuẩn bị cấu trúc stream âm thanh hoàn tất
            p.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_READY || state == Player.STATE_BUFFERING) {
                        p.removeListener(this)
                        if (readyCounter.incrementAndGet() == totalUrls) {
                            startHistoryFlow()
                        }
                    }
                }
            })

            playerPool[url] = p
        }
    }

    /**
     * Tắt loading hoàn toàn và hiển thị danh sách lịch sử làm bài mượt mà lên UI
     */
    private fun startHistoryFlow() {
        activity?.runOnUiThread {
            callBack.hideLoading()
            // Đảm bảo Adapter cập nhật lại một lần để hiển thị đầy đủ thời lượng (0:00/Tổng thời gian)
            adapter.notifyDataSetChanged()
            binding.rvListeningHistory.visibility = View.VISIBLE
        }
    }


    @Suppress("UNCHECKED_CAST")
    override fun handleApiSuccess(key: String, data: Any?) {
        when (key) {
            GET_LISTENING_HISTORY -> {
                // Giữ vững loading hoạt động liên tục đè lên view khi nhận phản hồi từ API
                callBack.showLoading()
                binding.rvListeningHistory.visibility = View.INVISIBLE

                val res = data as? List<UserAnswerRes> ?: emptyList()
                adapter.submitList(res)

                // Bắt đầu chuẩn bị nạp dữ liệu âm thanh ngầm
                preloadAllPlayers(res)
            }
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

        // ── Track khác: dừng cũ ──
        activePlayer?.pause()
        adapter.setPlayingState(activePosition, false)
        stopProgressUpdating()

        activePlayer   = target
        activeUrl      = audioUrl
        activePosition = position

        // Listener theo dõi ended
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
                    (binding.rvListeningHistory
                        .findViewHolderForAdapterPosition(position)
                            as? ListeningHistoryAdapter.ListeningHistoryHolder)
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

    // Lifecycle

    override fun onDestroy() {
        stopProgressUpdating()
        playerPool.values.forEach { it.release() }
        playerPool.clear()
        super.onDestroy()
    }
}