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
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SECTION = 0
        private const val VIEW_TYPE_QUESTION = 1
    }

    interface OnAnswerSelectedListener {
        fun onAnswerSelected(answer: AnswerRLPartReq)
    }

    private val questions = QuestionListPresentation.order(items).toMutableList()
    private val selectedAnswers = mutableMapOf<Long, Long>()

    val answerableCount: Int
        get() = QuestionListPresentation.answerableCount(questions)

    override fun getItemViewType(position: Int): Int =
        if (questions[position].isSection) VIEW_TYPE_SECTION else VIEW_TYPE_QUESTION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SECTION -> SectionHolder(
                inflater.inflate(R.layout.item_question_section, parent, false)
            )
            else -> ReadingHolder(inflater.inflate(R.layout.item_reading, parent, false))
        }
    }

    override fun getItemCount(): Int = questions.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = questions[position]
        when (holder) {
            is SectionHolder -> holder.bind(item)
            is ReadingHolder -> holder.bind(
                item = item,
                questionNumber = QuestionListPresentation.questionNumberAt(questions, position)
            )
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newItems: List<QuestionRes>) {
        questions.clear()
        questions.addAll(QuestionListPresentation.order(newItems))
        selectedAnswers.clear()
        notifyDataSetChanged()
    }

    inner class ReadingHolder(v: View) : RecyclerView.ViewHolder(v) {

        private val tvQuestionNumber: TextView = v.findViewById(R.id.tvQuestionNumber)
        private val tvQuestion: TextView = v.findViewById(R.id.tvQuestion)
        private val rgOptions: RadioGroup = v.findViewById(R.id.rgOptions)
        private val ansA: RadioButton = v.findViewById(R.id.ansA)
        private val ansB: RadioButton = v.findViewById(R.id.ansB)
        private val ansC: RadioButton = v.findViewById(R.id.ansC)
        private val ansD: RadioButton = v.findViewById(R.id.ansD)

        @SuppressLint("SetTextI18n")
        fun bind(item: QuestionRes, questionNumber: Int) {
            tvQuestionNumber.text = "Question $questionNumber"
            tvQuestion.text = item.content.orEmpty()

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

    class SectionHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val tvSectionTitle: TextView = v.findViewById(R.id.tvSectionTitle)
        private val tvSectionContent: TextView = v.findViewById(R.id.tvSectionContent)

        fun bind(item: QuestionRes) {
            tvSectionTitle.text = item.section?.takeIf { it.isNotBlank() } ?: "Section"
            val content = item.content.orEmpty().trim()
            tvSectionContent.text = content
            tvSectionContent.visibility = if (content.isBlank()) View.GONE else View.VISIBLE
        }
    }
}
