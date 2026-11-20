package com.danghung.elearning.view.adapter

import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.recyclerview.widget.RecyclerView
import com.danghung.elearning.databinding.ItemChatBotBinding
import android.animation.ObjectAnimator
import android.view.View
import com.bumptech.glide.Glide
import com.danghung.elearning.R
import com.danghung.elearning.databinding.ItemChatUserBinding
import com.danghung.elearning.model.ChatMessage

class ChatAdapter(private val onAnimationEnd: () -> Unit,
                  private var avatarUrl: String? = null ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val messages = mutableListOf<ChatMessage>()

    companion object {
        private const val TYPE_BOT = 0
        private const val TYPE_USER = 1
    }

    fun updateAvatarUrl(url: String?) {
        avatarUrl = url
        // refresh các item user đang hiển thị
        val userPositions = messages.indices.filter { messages[it].isUser }
        userPositions.forEach { notifyItemChanged(it) }
    }

    fun submitList(newList: List<ChatMessage>) {
        messages.clear()
        messages.addAll(newList)
        notifyDataSetChanged()
    }

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    fun removeTypingAndAddReply(botMessage: ChatMessage) {
        // Tìm và xóa typing bubble
        val typingIndex = messages.indexOfFirst { it.isTyping }
        if (typingIndex != -1) {
            messages.removeAt(typingIndex)
            notifyItemRemoved(typingIndex)
        }
        // Thêm tin nhắn thật
        messages.add(botMessage)
        notifyItemInserted(messages.size - 1)
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) TYPE_USER else TYPE_BOT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_USER) {
            UserViewHolder(ItemChatUserBinding.inflate(inflater, parent, false))
        } else {
            BotViewHolder(ItemChatBotBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is UserViewHolder) {
            holder.bind(message)
        } else if (holder is BotViewHolder) {
            holder.bind(message)
        }
    }

    override fun getItemCount(): Int = messages.size

    inner class UserViewHolder(private val binding: ItemChatUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chatMessage: ChatMessage) {
            binding.tvUserMessage.text = chatMessage.message

            // Load avatar
            Glide.with(binding.root.context)
                .load(avatarUrl)
                .placeholder(R.drawable.ic_user)
                .error(R.drawable.ic_user)
                .into(binding.imgItemUserAvatar)
        }
    }

    inner class BotViewHolder(private val binding: ItemChatBotBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var animator: ValueAnimator? = null
        private var dotAnimators: List<ObjectAnimator> = emptyList()

        fun bind(chatMessage: ChatMessage) {
            animator?.cancel()
            dotAnimators.forEach { it.cancel() }

            if (chatMessage.isTyping) {
                // Hiển thị typing dots, ẩn text
                binding.tvBotMessage.visibility = View.GONE
                binding.layoutTyping.visibility = View.VISIBLE
                startDotsAnimation()
            } else {
                // Ẩn typing dots, hiển thị text
                binding.layoutTyping.visibility = View.GONE
                binding.tvBotMessage.visibility = View.VISIBLE

                if (chatMessage.isAnimating) {
                    val fullText = chatMessage.message
                    val durationTime = (fullText.length * 30).toLong().coerceIn(300, 3000)

                    animator = ValueAnimator.ofInt(0, fullText.length).apply {
                        duration = durationTime
                        interpolator = LinearInterpolator()
                        addUpdateListener { animation ->
                            val index = animation.animatedValue as Int
                            binding.tvBotMessage.text = fullText.substring(0, index)
                        }
                        addListener(object : android.animation.AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: android.animation.Animator) {
                                chatMessage.isAnimating = false
                                onAnimationEnd()
                            }
                        })
                        start()
                    }
                } else {
                    binding.tvBotMessage.text = chatMessage.message
                }
            }
        }

        private fun startDotsAnimation() {
            val dots = listOf(binding.dot1, binding.dot2, binding.dot3)
            dotAnimators = dots.mapIndexed { index, dot ->
                ObjectAnimator.ofFloat(dot, View.ALPHA, 0.2f, 1f, 0.2f).apply {
                    duration = 900
                    repeatCount = ObjectAnimator.INFINITE
                    interpolator = LinearInterpolator()
                    startDelay = (index * 200).toLong()  // mỗi chấm delay 200ms so với chấm trước
                    start()
                }
            }
        }
    }
}