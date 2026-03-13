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
    private var isPlaying = false

    private val questions = items.toMutableList()
    private val selectedAnswers = mutableMapOf<Long, Long>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListeningHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_listening, parent, false)
        return ListeningHolder(view)
    }

    override fun getItemCount() = questions.size

    override fun onBindViewHolder(holder: ListeningHolder, position: Int) {
        holder.bind(questions[position], position)
    }

    interface OnListeningListener {
        fun onAnswerSelected(answer: AnswerRLPartReq)
        fun onPlayClicked(position: Int, audioUrl: String)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newItems: List<QuestionRes>) {
        questions.clear()
        questions.addAll(newItems)
        selectedAnswers.clear()
        notifyDataSetChanged()
    }

    inner class ListeningHolder(v: View) : RecyclerView.ViewHolder(v) {

        private val tvQuestionNumber: TextView = v.findViewById(R.id.tvQuestionNumber)
        private val rgOptions: RadioGroup = v.findViewById(R.id.rgOptions)
        private val ansA: RadioButton = v.findViewById(R.id.ansA)
        private val ansB: RadioButton = v.findViewById(R.id.ansB)
        private val ansC: RadioButton = v.findViewById(R.id.ansC)
        private val ivPlay: ImageView = v.findViewById(R.id.ivPlay)
        private val seekBar: SeekBar = v.findViewById(R.id.seekBar)
        private val tvDuration: TextView = v.findViewById(R.id.tvDuration)

        @SuppressLint("PrivateResource")
        fun bind(item: QuestionRes, position: Int) {

            tvQuestionNumber.text = "Question ${item.content}"

            ivPlay.setImageLevel(
                if (position == currentPlayingPosition && isPlaying)
                    LEVEL_PLAY else LEVEL_IDLE
            )

            val options = item.optionRes ?: emptyList()

            fun bindOption(rb: RadioButton, index: Int) {
                if (index < options.size) {
                    rb.visibility = View.VISIBLE
                    rb.text = options[index].content
                    rb.tag = options[index].id
                } else rb.visibility = View.GONE
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
                it.startAnimation(AnimationUtils.loadAnimation(itemView.context, androidx.appcompat.R.anim.abc_fade_in))
                listener.onPlayClicked(position, item.audioUrl ?: "")
            }
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