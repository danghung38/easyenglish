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
import com.danghung.elearning.api.res.UserExamPartRes

class FullTestSkillAdapter(
    private val items: List<UserExamPartRes>,
    private val listener: OnSkillClickListener
) : RecyclerView.Adapter<FullTestSkillAdapter.SkillHolder>() {

    interface OnSkillClickListener {
        fun onSkillClick(item: UserExamPartRes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fulltest_skill, parent, false)
        return SkillHolder(view)
    }

    override fun onBindViewHolder(holder: SkillHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class SkillHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val tvSkill: TextView = v.findViewById(R.id.tvSkillName)
        private val tvSubtitle: TextView = v.findViewById(R.id.tvSubtitle)
        private val tvScore: TextView = v.findViewById(R.id.tvBandScore)

        @SuppressLint("SetTextI18n", "DefaultLocale", "PrivateResource")
        fun bind(item: UserExamPartRes) {
            tvSkill.text = item.skillType
            tvSubtitle.text = "Full test"

            val bg = tvSkill.background as? GradientDrawable
            val color = ContextCompat.getColor(tvSkill.context, getSkillColorRes(item.skillType))
            bg?.setColor(color)

            val score = item.score
            tvScore.text = if (score == null) "-" else String.format("%.1f", score)

            itemView.setOnClickListener {
                it.startAnimation(
                    AnimationUtils.loadAnimation(
                        itemView.context,
                        androidx.appcompat.R.anim.abc_fade_in
                    )
                )
                listener.onSkillClick(item)
            }
        }
    }

    private fun getSkillColorRes(skill: String?): Int {
        return when (skill) {
            "LISTENING" -> R.color.color_bg_listening
            "READING" -> R.color.color_bg_reading
            "WRITING" -> R.color.color_bg_writing
            "SPEAKING" -> R.color.color_bg_speaking
            else -> R.color.color_app
        }
    }
}
