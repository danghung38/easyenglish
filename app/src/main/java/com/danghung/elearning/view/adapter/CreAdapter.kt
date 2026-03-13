package com.danghung.elearning.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.danghung.elearning.R
import com.danghung.elearning.model.CreItem

class CreAdapter(
    private var items: List<CreItem>
) : RecyclerView.Adapter<CreAdapter.CreHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cre, parent, false)
        return CreHolder(view)
    }

    override fun onBindViewHolder(holder: CreHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size


    inner class CreHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val tvTitle: TextView = v.findViewById(R.id.tvTitleCre)
        private val tvAuthor: TextView = v.findViewById(R.id.tvAuthor)

        fun bind(item: CreItem) {
            tvTitle.text = item.title
            tvAuthor.text = item.author
        }
    }
}
