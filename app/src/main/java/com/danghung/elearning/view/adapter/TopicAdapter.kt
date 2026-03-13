package com.danghung.elearning.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TableRow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.danghung.elearning.R
import com.danghung.elearning.api.res.TopicRes

class TopicAdapter(
    private var items: List<TopicRes>, private val listener: OnTopicClickListener
) : RecyclerView.Adapter<TopicAdapter.TopicHolder>() {

    interface OnTopicClickListener {
        fun onTopicClick(topic: TopicRes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_topic, parent, false)
        return TopicHolder(view)
    }

    override fun onBindViewHolder(holder: TopicHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size


    inner class TopicHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val tvTitle: TextView = v.findViewById(R.id.tvVocabularyTitle)
        private val tvTotalWords: TextView = v.findViewById(R.id.tvToTalWords)
        private val btnLearnNow: TableRow = v.findViewById(R.id.btnLearnNow)

        @SuppressLint("SetTextI18n", "PrivateResource")
        fun bind(item: TopicRes) {
            tvTitle.text = item.title
            tvTotalWords.text = "${item.wordCount} WORDS"
            btnLearnNow.setOnClickListener {
                it.startAnimation(
                    AnimationUtils.loadAnimation(
                        itemView.context,
                        androidx.appcompat.R.anim.abc_fade_in
                    )
                )
                listener.onTopicClick(item)
            }
        }
    }
}
