package com.danghung.elearning.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.danghung.elearning.R
import com.danghung.elearning.api.res.UserAnswerRes

class ReadingHistoryAdapter(
    private var items: List<UserAnswerRes>
) : RecyclerView.Adapter<ReadingHistoryAdapter.ReadingHistoryHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReadingHistoryHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reading_history, parent, false)
        return ReadingHistoryHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ReadingHistoryHolder, position: Int) {
        holder.bind(items[position], position)
    }

    inner class ReadingHistoryHolder(v: View) : RecyclerView.ViewHolder(v) {

        private val tvQuestion: TextView = v.findViewById(R.id.tvQuestion)
        private val tvExplain: TextView = v.findViewById(R.id.tvExplain)
        private val tvCorrectWrong: TextView = v.findViewById(R.id.tvCorrectWrong)

        private val ansA: RadioButton = v.findViewById(R.id.ansA)
        private val ansB: RadioButton = v.findViewById(R.id.ansB)
        private val ansC: RadioButton = v.findViewById(R.id.ansC)
        private val ansD: RadioButton = v.findViewById(R.id.ansD)

        @SuppressLint("SetTextI18n")
        fun bind(item: UserAnswerRes, position: Int) {

            val question = item.question ?: return

            tvQuestion.text = "${position + 1}. ${question.content}"

            val options = question.optionRes ?: emptyList()

            fun bindOption(rb: RadioButton, index: Int) {
                if (index < options.size) {
                    val option = options[index]

                    rb.visibility = View.VISIBLE
                    rb.text = option.content

                    rb.isChecked = option.id == item.selectedOptionId

                    rb.isEnabled = false // không cho click

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
            bindOption(ansD, 3)

            // Correct / Wrong
            tvCorrectWrong.text = if (item.score == 1.0) "CORRECT" else "WRONG"
            val color = if (item.score == 1.0) R.color.color_correct else R.color.color_wrong
            tvCorrectWrong.backgroundTintList = ContextCompat.getColorStateList(itemView.context, color)
            // Explain
            tvExplain.text = "Explain: ${question.explain}"
        }
    }
}