package com.danghung.elearning.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.danghung.elearning.R
import com.danghung.elearning.api.res.VocabularyRes
import com.danghung.elearning.record.AndroidAudioPlayer

class VocabularyAdapter(
    private var items: List<VocabularyRes>,
    private val listener: OnVocabularyClickListener,
    private val context: Context
) : RecyclerView.Adapter<VocabularyAdapter.VocabularyHolder>(), View.OnClickListener {

    private val audioPlayer = AndroidAudioPlayer(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VocabularyHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_vocabulary, parent, false)
        return VocabularyHolder(view)
    }

    interface OnVocabularyClickListener {
        fun onNext()
        fun onPrevious()
    }

    override fun onBindViewHolder(holder: VocabularyHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class VocabularyHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val tvVocabulary: TextView = v.findViewById(R.id.tvVocabulary)
        private val ivSound: ImageView = v.findViewById(R.id.ivSound)
        private val tvNoun: TextView = v.findViewById(R.id.tvNoun)
        private val tvMeaning: TextView = v.findViewById(R.id.tvMeaning)
        private val tvExample: TextView = v.findViewById(R.id.tvExample)
        private val btnPrevious: View = v.findViewById(R.id.btnPrevious)
        private val btnNext: View = v.findViewById(R.id.btnNext)
        private val btnFlip: View = v.findViewById(R.id.btnFlip)
        private val cardFront: View = v.findViewById(R.id.cardFront)
        private val cardBack: View = v.findViewById(R.id.cardBack)

        private var isFront = true

        fun speak(word: String) {
            val url =
                "https://translate.google.com/translate_tts?ie=UTF-8&q=$word&tl=en&client=tw-ob"
            audioPlayer.playUrl(url)
        }

        @SuppressLint("PrivateResource")
        fun bind(item: VocabularyRes) {
            tvVocabulary.text = item.word
            tvNoun.text = item.pronunciation
            tvMeaning.text = item.meaning
            tvExample.text = item.example
            btnNext.setOnClickListener(this@VocabularyAdapter)
            btnPrevious.setOnClickListener(this@VocabularyAdapter)

            ivSound.setOnClickListener {
                it.startAnimation(
                    AnimationUtils.loadAnimation(
                        itemView.context,
                        androidx.appcompat.R.anim.abc_fade_in
                    )
                )
                speak(item.word)
            }
            btnFlip.setOnClickListener {
                it.startAnimation(
                    AnimationUtils.loadAnimation(
                        itemView.context,
                        androidx.appcompat.R.anim.abc_fade_in
                    )
                )
                flipCard()
            }
        }

        fun flipCard() {
            val scale = itemView.resources.displayMetrics.density
            cardFront.cameraDistance = 8000 * scale
            cardBack.cameraDistance = 8000 * scale

            if (isFront) {
                val flipOut = android.animation.AnimatorInflater.loadAnimator(context, R.animator.flip_out)
                val flipIn = android.animation.AnimatorInflater.loadAnimator(context, R.animator.flip_in)

                // Set hardware layer khi animate để tránh bóng đen
                cardFront.setLayerType(View.LAYER_TYPE_HARDWARE, null)
                cardBack.setLayerType(View.LAYER_TYPE_HARDWARE, null)

                flipOut.setTarget(cardFront)
                flipOut.start()
                flipOut.addListener(object : android.animation.AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        cardFront.visibility = View.GONE
                        cardBack.visibility = View.VISIBLE
                        cardBack.rotationY = -90f
                        flipIn.setTarget(cardBack)
                        flipIn.start()
                        flipIn.addListener(object : android.animation.AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: android.animation.Animator) {
                                // Trả về NONE sau khi xong để tránh memory leak
                                cardFront.setLayerType(View.LAYER_TYPE_NONE, null)
                                cardBack.setLayerType(View.LAYER_TYPE_NONE, null)
                            }
                        })
                    }
                })
            } else {
                val flipOut = android.animation.AnimatorInflater.loadAnimator(context, R.animator.flip_out)
                val flipIn = android.animation.AnimatorInflater.loadAnimator(context, R.animator.flip_in)

                cardFront.setLayerType(View.LAYER_TYPE_HARDWARE, null)
                cardBack.setLayerType(View.LAYER_TYPE_HARDWARE, null)

                flipOut.setTarget(cardBack)
                flipOut.start()
                flipOut.addListener(object : android.animation.AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        cardBack.visibility = View.GONE
                        cardFront.visibility = View.VISIBLE
                        cardFront.rotationY = -90f
                        flipIn.setTarget(cardFront)
                        flipIn.start()
                        flipIn.addListener(object : android.animation.AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: android.animation.Animator) {
                                cardFront.setLayerType(View.LAYER_TYPE_NONE, null)
                                cardBack.setLayerType(View.LAYER_TYPE_NONE, null)
                            }
                        })
                    }
                })
            }

            isFront = !isFront
        }
    }

    @SuppressLint("PrivateResource")
    override fun onClick(v: View) {
        v.startAnimation(
            AnimationUtils.loadAnimation(
                context,
                androidx.appcompat.R.anim.abc_fade_in
            )
        )
        when (v.id) {
            R.id.btnNext -> {
                listener.onNext()
            }

            R.id.btnPrevious -> {
                listener.onPrevious()
            }
        }
    }
}
