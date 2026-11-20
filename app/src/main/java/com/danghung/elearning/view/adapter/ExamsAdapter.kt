package com.danghung.elearning.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TableRow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.danghung.elearning.R
import com.danghung.elearning.api.res.ExamRes

class ExamsAdapter(
    items: List<ExamRes>, private val listener: OnExamClickListener
) : RecyclerView.Adapter<ExamsAdapter.ExamsHolder>() {

    private var fullList: List<ExamRes> = items
    private var displayList: MutableList<ExamRes> = items.toMutableList()

    interface OnExamClickListener {
        fun onExamClick(exam: ExamRes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamsHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exam, parent, false)
        return ExamsHolder(view)
    }

    override fun getItemCount(): Int = displayList.size

    override fun onBindViewHolder(holder: ExamsHolder, position: Int) {
        holder.bind(displayList[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newItems: List<ExamRes>) {
        fullList = newItems
        displayList = newItems.toMutableList()
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filter(keyword: String) {
        displayList.clear()

        if (keyword.isBlank()) {
            displayList.addAll(fullList)
        } else {
            val key = keyword.lowercase()
            displayList.addAll(
                fullList.filter {
                    it.title.lowercase().contains(key) == true
                }
            )
        }

        notifyDataSetChanged()
    }

    inner class ExamsHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val tvTitleExam: TextView = v.findViewById(R.id.tvTitleExam)
        private val ivAvtExam: ImageView = v.findViewById(R.id.ivAvtExam)
        private val tvDesExam: TextView = v.findViewById(R.id.tvDesExam)
        private val tvDurationExam: TextView = v.findViewById(R.id.tvDurationExam)
        private val btnStartExam: TableRow = v.findViewById(R.id.btnStartExam)

        @SuppressLint("SetTextI18n", "PrivateResource")
        fun bind(item: ExamRes) {
            tvTitleExam.text = item.title
            tvDesExam.text = item.description
            tvDurationExam.text = "Duration: ${item.totalDuration} minutes"
            Glide.with(itemView.context).load(item.imageUrl).placeholder(R.drawable.ic_avt_exam)
                .error(R.drawable.ic_avt_exam).into(ivAvtExam)

            btnStartExam.setOnClickListener {
                it.startAnimation(AnimationUtils.loadAnimation(itemView.context, androidx.appcompat.R.anim.abc_fade_in))
                listener.onExamClick(item)
            }
        }
    }
}