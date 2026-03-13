package com.danghung.elearning.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.danghung.elearning.R
import com.danghung.elearning.api.req.AnswerRLPartReq
import com.danghung.elearning.api.res.QuestionRes

class ReadingAdapter(
    items: List<QuestionRes>,
    private val listener: OnAnswerSelectedListener
) : RecyclerView.Adapter<ReadingAdapter.ReadingHolder>() {

    interface OnAnswerSelectedListener {
        fun onAnswerSelected(answer: AnswerRLPartReq)
    }

    private val questions = items.toMutableList()
    private val selectedAnswers = mutableMapOf<Long, Long>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReadingHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reading, parent, false)
        return ReadingHolder(view)
    }

    override fun getItemCount(): Int = questions.size

    override fun onBindViewHolder(holder: ReadingHolder, position: Int) {
        holder.bind(questions[position], position)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newItems: List<QuestionRes>) {
        questions.clear()
        questions.addAll(newItems)
        selectedAnswers.clear()
        notifyDataSetChanged()
    }

    inner class ReadingHolder(v: View) : RecyclerView.ViewHolder(v) {

        private val tvQuestion: TextView = v.findViewById(R.id.tvQuestion)
        private val rgOptions: RadioGroup = v.findViewById(R.id.rgOptions)
        private val ansA: RadioButton = v.findViewById(R.id.ansA)
        private val ansB: RadioButton = v.findViewById(R.id.ansB)
        private val ansC: RadioButton = v.findViewById(R.id.ansC)
        private val ansD: RadioButton = v.findViewById(R.id.ansD)

        @SuppressLint("SetTextI18n")
        fun bind(item: QuestionRes, position: Int) {
            tvQuestion.text = "${position + 1}. ${item.content}"

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
            bindOption(ansD, 3)

            // reset do recycle
            rgOptions.setOnCheckedChangeListener(null)
            rgOptions.clearCheck()

            // restore answer
            selectedAnswers[item.id]?.let { selectedId ->
                when (selectedId) {
                    ansA.tag -> ansA.isChecked = true
                    ansB.tag -> ansB.isChecked = true
                    ansC.tag -> ansC.isChecked = true
                    ansD.tag -> ansD.isChecked = true
                }
            }

            rgOptions.setOnCheckedChangeListener { _, checkedId ->
                val rb = itemView.findViewById<RadioButton>(checkedId)
                val optionId = rb.tag as Long
                selectedAnswers[item.id] = optionId
                listener.onAnswerSelected(AnswerRLPartReq(item.id,optionId))
            }
        }
    }
}
