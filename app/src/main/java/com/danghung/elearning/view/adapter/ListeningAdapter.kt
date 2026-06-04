package com.danghung.elearning.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.danghung.elearning.R
import com.danghung.elearning.api.req.AnswerRLPartReq
import com.danghung.elearning.api.res.QuestionRes
import androidx.media3.exoplayer.ExoPlayer

@SuppressLint("SetTextI18n")
class ListeningAdapter(
    items: List<QuestionRes>,
    private val listener: OnListeningListener
) : RecyclerView.Adapter<ListeningAdapter.ListeningHolder>() {

    companion object {
        const val LEVEL_IDLE = 0
        const val LEVEL_PLAY = 1
    }

    private var currentPlayingPosition = -1
    private var isCurrentlyPlaying = false

    private val questions = items.toMutableList()
    private val selectedAnswers = mutableMapOf<Long, Long>()

    /** * Cung cấp một lambda/callback để Adapter có thể chủ động truy vấn nhanh thông tin
     * thời gian thực (duration, currentPosition) từ playerPool nằm ngoài Fragment.
     */
    var playerProvider: ((String) -> ExoPlayer?)? = null

    interface OnListeningListener {
        fun onAnswerSelected(answer: AnswerRLPartReq)
        fun onPlayClicked(position: Int, audioUrl: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListeningHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_listening, parent, false)
        return ListeningHolder(view)
    }

    override fun getItemCount() = questions.size

    override fun onBindViewHolder(holder: ListeningHolder, position: Int) {
        holder.bind(questions[position], position)
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    fun setPlayingState(position: Int, playing: Boolean) {
        val old = currentPlayingPosition
        currentPlayingPosition = position
        isCurrentlyPlaying = playing
        if (old != -1 && old != position) notifyItemChanged(old)
        if (position != -1) notifyItemChanged(position)
    }

    fun resetAllPlayState() {
        currentPlayingPosition = -1
        isCurrentlyPlaying = false
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newItems: List<QuestionRes>) {
        questions.clear()
        questions.addAll(newItems)
        selectedAnswers.clear()
        notifyDataSetChanged()
    }

    // -----------------------------------------------------------------------
    // ViewHolder
    // -----------------------------------------------------------------------

    inner class ListeningHolder(v: View) : RecyclerView.ViewHolder(v) {

        private val tvQuestionNumber: TextView = v.findViewById(R.id.tvQuestionNumber)
        private val rgOptions: RadioGroup      = v.findViewById(R.id.rgOptions)
        private val ansA: RadioButton          = v.findViewById(R.id.ansA)
        private val ansB: RadioButton          = v.findViewById(R.id.ansB)
        private val ansC: RadioButton          = v.findViewById(R.id.ansC)
        private val ivPlay: ImageView          = v.findViewById(R.id.ivPlay)
        private val seekBar: SeekBar           = v.findViewById(R.id.seekBar)
        private val tvDuration: TextView       = v.findViewById(R.id.tvDuration)

        @SuppressLint("PrivateResource")
        fun bind(item: QuestionRes, position: Int) {
            tvQuestionNumber.text = "Question ${item.content}"

            // Trích xuất thông tin player đã preload từ pool dựa vào url
            val player = item.audioUrl?.let { playerProvider?.invoke(it) }
            val duration = player?.duration ?: 0L
            val current = player?.currentPosition ?: 0L

            if (duration > 0) {
                // Hiển thị chính xác tiến trình hiện tại / tổng thời lượng thực tế của file audio
                seekBar.progress = (current * 100 / duration).toInt()
                tvDuration.text = "${formatTime(current)}/${formatTime(duration)}"
            } else {
                seekBar.progress = 0
                tvDuration.text = "0:00/0:00"
            }

            ivPlay.setImageLevel(
                if (position == currentPlayingPosition && isCurrentlyPlaying) LEVEL_PLAY
                else LEVEL_IDLE
            )

            val options = item.optionRes ?: emptyList()
            fun bindOption(rb: RadioButton, index: Int) {
                if (index < options.size) {
                    rb.visibility = View.VISIBLE
                    rb.text = options[index].content
                    rb.tag = options[index].id
                } else {
                    rb.visibility = View.GONE
                }
            }
            bindOption(ansA, 0)
            bindOption(ansB, 1)
            bindOption(ansC, 2)

            rgOptions.setOnCheckedChangeListener(null)
            rgOptions.clearCheck()
            selectedAnswers[item.id]?.let { selectedId ->
                when (selectedId) {
                    ansA.tag -> ansA.isChecked = true
                    ansB.tag -> ansB.isChecked = true
                    ansC.tag -> ansC.isChecked = true
                }
            }
            rgOptions.setOnCheckedChangeListener { _, checkedId ->
                val rb = itemView.findViewById<RadioButton>(checkedId)
                val optionId = rb.tag as Long
                selectedAnswers[item.id] = optionId
                listener.onAnswerSelected(AnswerRLPartReq(item.id, optionId))
            }

            ivPlay.setOnClickListener {
                it.startAnimation(
                    AnimationUtils.loadAnimation(itemView.context, androidx.appcompat.R.anim.abc_fade_in)
                )
                listener.onPlayClicked(position, item.audioUrl ?: "")
            }
        }

        fun updateProgress(current: Long, duration: Long) {
            if (duration <= 0) return
            seekBar.progress = (current * 100 / duration).toInt()
            tvDuration.text = "${formatTime(current)}/${formatTime(duration)}"
        }
    }

    @SuppressLint("DefaultLocale")
    fun formatTime(ms: Long): String {
        val totalSec = ms / 1000
        return String.format("%d:%02d", totalSec / 60, totalSec % 60)
    }
}