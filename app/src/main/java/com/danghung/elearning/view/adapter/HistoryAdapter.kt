package com.danghung.elearning.view.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.danghung.elearning.R
import com.danghung.elearning.api.res.TestHistoryRes
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HistoryAdapter(
    private var items: List<TestHistoryRes>, private val listener: OnHistoryClickListener
) : RecyclerView.Adapter<HistoryAdapter.HistoryHolder>() {

    companion object {
        const val KEY_FULL_TEST = "KEY_FULL_TEST"
        const val KEY_SINGLE_SKILL = "KEY_SINGLE_SKILL"
    }

    interface OnHistoryClickListener {
        fun onHistoryClick(item: TestHistoryRes, key: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_history_test, parent, false)
        return HistoryHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size


    inner class HistoryHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val tvSkill: TextView = v.findViewById(R.id.tvSkillName)
        private val tvExamName: TextView = v.findViewById(R.id.tvNameExam)
        private val tvDate: TextView = v.findViewById(R.id.tvDateExam)
        private val tvScore: TextView = v.findViewById(R.id.tvBandScore)
        private val btnDetail: TextView = v.findViewById(R.id.btnViewDetails)

        @SuppressLint("SetTextI18n", "DefaultLocale", "PrivateResource")
        fun bind(item: TestHistoryRes) {
            tvSkill.text = getSkillText(item.skillType, item.isFullTest)
            val bg = tvSkill.background as? GradientDrawable
            val color = ContextCompat.getColor(tvSkill.context, getSkillColorRes(item.skillType, item.isFullTest))
            bg?.setColor(color)
            tvExamName.text = item.examName
            tvDate.text = formatTestDate(item.testDate)
            tvScore.text = if (item.isFullTest) {
                String.format("%.1f", item.score)
            } else {
                item.score.toString()
            }

            btnDetail.setOnClickListener {
                it.startAnimation(
                    AnimationUtils.loadAnimation(
                        itemView.context,
                        androidx.appcompat.R.anim.abc_fade_in
                    )
                )
                val key = if (item.isFullTest) KEY_FULL_TEST else KEY_SINGLE_SKILL
                listener.onHistoryClick(item, key)
            }
        }
    }

    fun getSkillColorRes(skill: String?, isFullTest: Boolean): Int {
        if (isFullTest) return R.color.color_bg_fulltest
        return when (skill) {
            "LISTENING" -> R.color.color_bg_listening
            "READING" -> R.color.color_bg_reading
            "WRITING" -> R.color.color_bg_writing
            "SPEAKING" -> R.color.color_bg_speaking
            else -> R.color.color_app
        }
    }

    fun getSkillText(skill: String?, isFullTest: Boolean): String {
        return if (isFullTest) "FULL TEST" else skill ?: ""
    }

    fun formatTestDate(date: String): String {
        val input = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val output = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return LocalDateTime.parse(date, input).format(output)
    }
}
