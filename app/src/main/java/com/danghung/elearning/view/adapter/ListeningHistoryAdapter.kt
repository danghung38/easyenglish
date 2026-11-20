package com.danghung.elearning.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.danghung.elearning.R
import com.danghung.elearning.api.res.UserAnswerRes
import androidx.media3.exoplayer.ExoPlayer

@SuppressLint("SetTextI18n")
class ListeningHistoryAdapter(
    private var items: List<UserAnswerRes>,
    private val listener: OnListeningHistoryListener
) : RecyclerView.Adapter<ListeningHistoryAdapter.ListeningHistoryHolder>() {

    companion object {
        const val LEVEL_IDLE = 0
        const val LEVEL_PLAY = 1
    }

    private var currentPlayingPosition = -1
    private var isCurrentlyPlaying = false

    /**
     * Callback kết nối trực tiếp đến playerPool của Fragment để lấy thông tin audio thời gian thực
     */
    var playerProvider: ((String) -> ExoPlayer?)? = null

    interface OnListeningHistoryListener {
        fun onPlayClicked(position: Int, audioUrl: String)
    }

    // -----------------------------------------------------------------------
    // RecyclerView
    // -----------------------------------------------------------------------

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListeningHistoryHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_listening_history, parent, false)
        return ListeningHistoryHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ListeningHistoryHolder, position: Int) {
        holder.bind(items[position], position)
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /** Chỉ refresh 2 item bị ảnh hưởng, giữ nguyên trạng thái các item còn lại */
    fun setPlayingState(position: Int, playing: Boolean) {
        val old = currentPlayingPosition
        currentPlayingPosition = position
        isCurrentlyPlaying = playing
        if (old != -1 && old != position) notifyItemChanged(old)
        if (position != -1) notifyItemChanged(position)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newItems: List<UserAnswerRes>) {
        items = newItems
        notifyDataSetChanged()
    }

    // -----------------------------------------------------------------------
    // ViewHolder
    // -----------------------------------------------------------------------

    inner class ListeningHistoryHolder(v: View) : RecyclerView.ViewHolder(v) {

        private val tvCorrectWrong: TextView = v.findViewById(R.id.tvCorrectWrong)
        private val tvExplain: TextView      = v.findViewById(R.id.tvExplain)
        private val ansA: RadioButton        = v.findViewById(R.id.ansA)
        private val ansB: RadioButton        = v.findViewById(R.id.ansB)
        private val ansC: RadioButton        = v.findViewById(R.id.ansC)
        private val ivPlay: ImageView        = v.findViewById(R.id.ivPlay)
        private val seekBar: SeekBar         = v.findViewById(R.id.seekBar)
        private val tvDuration: TextView     = v.findViewById(R.id.tvDuration)

        @SuppressLint("PrivateResource")
        fun bind(item: UserAnswerRes, position: Int) {
            val question = item.question ?: return
            val options  = question.optionRes ?: emptyList()

            // Icon play/pause tương ứng với trạng thái hiện tại
            ivPlay.setImageLevel(
                if (position == currentPlayingPosition && isCurrentlyPlaying) LEVEL_PLAY
                else LEVEL_IDLE
            )

            // Lấy thông tin player đã qua cơ chế preload ngầm
            val player = question.audioUrl?.let { playerProvider?.invoke(it) }
            val duration = player?.duration ?: 0L
            val current = player?.currentPosition ?: 0L

            if (duration > 0) {
                // Hiển thị chính xác mốc tiến trình và thời lượng thật (ví dụ: 0:00/0:21 hoặc vị trí đang pause)
                seekBar.progress = (current * 100 / duration).toInt()
                tvDuration.text  = "${formatTime(current)}/${formatTime(duration)}"
            } else {
                seekBar.progress = 0
                tvDuration.text  = "0:00/0:00"
            }

            // Options (read-only, highlight đúng/sai)
            fun bindOption(rb: RadioButton, index: Int) {
                if (index < options.size) {
                    val option = options[index]
                    rb.visibility = View.VISIBLE
                    rb.text       = option.content
                    rb.isChecked  = option.id == item.selectedOptionId
                    rb.isEnabled  = false
                    rb.setBackgroundResource(
                        when {
                            option.id == question.correctOptionId  -> R.drawable.bg_answer_correct
                            option.id == item.selectedOptionId     -> R.drawable.bg_answer_wrong
                            else                                   -> android.R.color.transparent
                        }
                    )
                } else {
                    rb.visibility = View.GONE
                }
            }
            bindOption(ansA, 0)
            bindOption(ansB, 1)
            bindOption(ansC, 2)

            // Correct / Wrong badge
            val correct = item.score == 1.0
            tvCorrectWrong.text = if (correct) "CORRECT" else "WRONG"
            tvCorrectWrong.backgroundTintList = ContextCompat.getColorStateList(
                itemView.context,
                if (correct) R.color.color_correct else R.color.color_wrong
            )

            tvExplain.text = "Explain: ${question.explain}"

            ivPlay.setOnClickListener {
                it.startAnimation(
                    AnimationUtils.loadAnimation(itemView.context, androidx.appcompat.R.anim.abc_fade_in)
                )
                listener.onPlayClicked(position, question.audioUrl ?: "")
            }
        }

        fun updateProgress(current: Long, duration: Long) {
            if (duration <= 0) return
            seekBar.progress = (current * 100 / duration).toInt()
            tvDuration.text  = "${formatTime(current)}/${formatTime(duration)}"
        }
    }

    @SuppressLint("DefaultLocale")
    fun formatTime(ms: Long): String {
        val totalSec = ms / 1000
        return String.format("%d:%02d", totalSec / 60, totalSec % 60)
    }
}