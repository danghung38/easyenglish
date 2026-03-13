package com.danghung.elearning.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.danghung.elearning.R
import com.danghung.elearning.model.OnboardingItem

class OnboardingAdapter(
    private var items: List<OnboardingItem>
) : RecyclerView.Adapter<OnboardingAdapter.OnboardingHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_onboarding, parent, false)
        return OnboardingHolder(view)
    }

    override fun onBindViewHolder(holder: OnboardingHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size


    inner class OnboardingHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val tvTitle: TextView = v.findViewById(R.id.tv_title_onboarding)
        private val ivImage: ImageView = v.findViewById(R.id.iv_onboarding)
        private val tvDesc: TextView = v.findViewById(R.id.tv_desc_onboarding)

        fun bind(item: OnboardingItem) {
            tvTitle.text = item.title
            tvDesc.text = item.description
            tvDesc.visibility = View.VISIBLE
            ivImage.setImageResource(item.imageRes)
        }
    }
}
