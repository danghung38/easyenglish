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
    private var isPlaying = false

    interface OnListeningHistoryListener {
        fun onPlayClicked(position: Int, audioUrl: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListeningHistoryHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_listening_history, parent, false)
        return ListeningHistoryHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ListeningHistoryHolder, position: Int) {
        holder.bind(items[position], position)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newItems: List<UserAnswerRes>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ListeningHistoryHolder(v: View) : RecyclerView.ViewHolder(v) {

        private val tvCorrectWrong: TextView = v.findViewById(R.id.tvCorrectWrong)
        private val tvExplain: TextView = v.findViewById(R.id.tvExplain)

        private val ansA: RadioButton = v.findViewById(R.id.ansA)
        private val ansB: RadioButton = v.findViewById(R.id.ansB)
        private val ansC: RadioButton = v.findViewById(R.id.ansC)

        private val ivPlay: ImageView = v.findViewById(R.id.ivPlay)
        private val seekBar: SeekBar = v.findViewById(R.id.seekBar)
        private val tvDuration: TextView = v.findViewById(R.id.tvDuration)

        @SuppressLint("PrivateResource")
        fun bind(item: UserAnswerRes, position: Int) {

            val question = item.question ?: return
            val options = question.optionRes ?: emptyList()

            ivPlay.setImageLevel(
                if (position == currentPlayingPosition && isPlaying)
                    LEVEL_PLAY else LEVEL_IDLE
            )

            fun bindOption(rb: RadioButton, index: Int) {

                if (index < options.size) {

                    val option = options[index]

                    rb.visibility = View.VISIBLE
                    rb.text = option.content
                    rb.isChecked = option.id == item.selectedOptionId
                    rb.isEnabled = false

                    // highlight
                    if (option.id == question.correctOptionId) {
                        rb.setBackgroundResource(R.drawable.bg_answer_correct)
                    } else if (option.id == item.selectedOptionId) {
                        rb.setBackgroundResource(R.drawable.bg_answer_wrong)
                    } else {
                        rb.setBackgroundResource(android.R.color.transparent)
                    }

                } else {
                    rb.visibility = View.GONE
                }
            }

            bindOption(ansA, 0)
            bindOption(ansB, 1)
            bindOption(ansC, 2)

            tvCorrectWrong.text = if (item.score == 1.0) "CORRECT" else "WRONG"

            val color =
                if (item.score == 1.0) R.color.color_correct
                else R.color.color_wrong

            tvCorrectWrong.backgroundTintList =
                ContextCompat.getColorStateList(itemView.context, color)

            tvExplain.text = "Explain: ${question.explain}"

            ivPlay.setOnClickListener {
                it.startAnimation(AnimationUtils.loadAnimation(itemView.context, androidx.appcompat.R.anim.abc_fade_in))
                listener.onPlayClicked(position, question.audioUrl ?: "")
            }

            seekBar.progress = 0
            tvDuration.text = "0:00"
        }

        fun updateProgress(current: Long, duration: Long) {

            if (duration <= 0) return

            val percent = (current * 100 / duration).toInt()
            seekBar.progress = percent
            tvDuration.text = "${formatTime(current)}/${formatTime(duration)}"
        }
    }

    @SuppressLint("DefaultLocale")
    private fun formatTime(ms: Long): String {

        val totalSec = ms / 1000
        val min = totalSec / 60
        val sec = totalSec % 60

        return String.format("%d:%02d", min, sec)
    }

    fun updatePlayingState(position: Int, playing: Boolean) {

        val old = currentPlayingPosition
        currentPlayingPosition = position
        isPlaying = playing

        if (old != -1) notifyItemChanged(old)
        if (position != -1) notifyItemChanged(position)
    }
}