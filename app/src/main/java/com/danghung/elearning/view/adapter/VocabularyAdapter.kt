package com.danghung.elearning.view.adapter

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.danghung.elearning.R
import com.danghung.elearning.api.res.VocabularyRes
import com.danghung.elearning.record.AndroidAudioPlayer

class VocabularyAdapter(
    private val items: List<VocabularyRes>,
    private val listener: OnVocabularyClickListener,
    private val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_FLASHCARD = 0
        private const val TYPE_QUIZ = 1
    }

    interface OnVocabularyClickListener {
        fun onNext()
        fun onPrevious()
    }

    private val audioPlayer = AndroidAudioPlayer(context)
    private val selectedAnswers = mutableMapOf<Int, String>()
    private val checkedQuizIds = mutableSetOf<Int>()

    override fun getItemViewType(position: Int): Int =
        if (items[position].chatbotQuiz == true) TYPE_QUIZ else TYPE_FLASHCARD

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_QUIZ) {
            QuizHolder(inflater.inflate(R.layout.item_vocabulary_quiz, parent, false))
        } else {
            FlashcardHolder(inflater.inflate(R.layout.item_vocabulary, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FlashcardHolder -> holder.bind(items[position])
            is QuizHolder -> holder.bind(items[position])
        }
    }

    override fun getItemCount(): Int = items.size

    private fun animateAndRun(view: View, action: () -> Unit) {
        view.startAnimation(
            AnimationUtils.loadAnimation(context, androidx.appcompat.R.anim.abc_fade_in)
        )
        action()
    }

    inner class FlashcardHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvVocabulary: TextView = view.findViewById(R.id.tvVocabulary)
        private val ivSound: ImageView = view.findViewById(R.id.ivSound)
        private val tvNoun: TextView = view.findViewById(R.id.tvNoun)
        private val tvMeaning: TextView = view.findViewById(R.id.tvMeaning)
        private val tvExample: TextView = view.findViewById(R.id.tvExample)
        private val btnPrevious: View = view.findViewById(R.id.btnPrevious)
        private val btnNext: View = view.findViewById(R.id.btnNext)
        private val btnFlip: View = view.findViewById(R.id.btnFlip)
        private val cardFront: View = view.findViewById(R.id.cardFront)
        private val cardBack: View = view.findViewById(R.id.cardBack)
        private var isFront = true

        fun bind(item: VocabularyRes) {
            tvVocabulary.text = item.word
            tvNoun.text = item.pronunciation.orEmpty()
            tvMeaning.text = item.meaning
            tvExample.text = item.example.orEmpty()
            cardFront.visibility = View.VISIBLE
            cardBack.visibility = View.GONE
            isFront = true

            btnNext.setOnClickListener { animateAndRun(it, listener::onNext) }
            btnPrevious.setOnClickListener { animateAndRun(it, listener::onPrevious) }
            ivSound.setOnClickListener { view ->
                animateAndRun(view) {
                    val url = "https://translate.google.com/translate_tts?ie=UTF-8&q=${item.word}&tl=en&client=tw-ob"
                    audioPlayer.playUrl(url)
                }
            }
            btnFlip.setOnClickListener { view -> animateAndRun(view, ::flipCard) }
        }

        private fun flipCard() {
            val outgoing = if (isFront) cardFront else cardBack
            val incoming = if (isFront) cardBack else cardFront
            val scale = itemView.resources.displayMetrics.density
            outgoing.cameraDistance = 8000 * scale
            incoming.cameraDistance = 8000 * scale
            outgoing.setLayerType(View.LAYER_TYPE_HARDWARE, null)
            incoming.setLayerType(View.LAYER_TYPE_HARDWARE, null)

            val flipOut = AnimatorInflater.loadAnimator(context, R.animator.flip_out)
            val flipIn = AnimatorInflater.loadAnimator(context, R.animator.flip_in)
            flipOut.setTarget(outgoing)
            flipOut.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    outgoing.visibility = View.GONE
                    incoming.visibility = View.VISIBLE
                    incoming.rotationY = -90f
                    flipIn.setTarget(incoming)
                    flipIn.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            outgoing.setLayerType(View.LAYER_TYPE_NONE, null)
                            incoming.setLayerType(View.LAYER_TYPE_NONE, null)
                        }
                    })
                    flipIn.start()
                }
            })
            flipOut.start()
            isFront = !isFront
        }
    }

    inner class QuizHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvQuestion: TextView = view.findViewById(R.id.tvQuizQuestion)
        private val optionsGroup: RadioGroup = view.findViewById(R.id.rgQuizOptions)
        private val optionButtons = listOf<RadioButton>(
            view.findViewById(R.id.quizOptionA),
            view.findViewById(R.id.quizOptionB),
            view.findViewById(R.id.quizOptionC),
            view.findViewById(R.id.quizOptionD)
        )
        private val btnCheck: View = view.findViewById(R.id.btnCheckAnswer)
        private val btnPrevious: View = view.findViewById(R.id.btnPrevious)
        private val btnNext: View = view.findViewById(R.id.btnNext)
        private val tvResult: TextView = view.findViewById(R.id.tvQuizResult)
        private val tvExplanation: TextView = view.findViewById(R.id.tvQuizExplanation)

        @SuppressLint("SetTextI18n")
        fun bind(item: VocabularyRes) {
            tvQuestion.text = item.quizQuestion.orEmpty()
            optionsGroup.setOnCheckedChangeListener(null)
            optionsGroup.clearCheck()

            val options = item.quizOptions.orEmpty()
            optionButtons.forEachIndexed { index, button ->
                if (index < options.size) {
                    button.visibility = View.VISIBLE
                    button.text = options[index]
                    button.tag = options[index]
                } else {
                    button.visibility = View.GONE
                    button.tag = null
                }
            }

            selectedAnswers[item.id]?.let { selected ->
                optionButtons.firstOrNull { it.tag == selected }?.isChecked = true
            }
            renderResult(item)

            optionsGroup.setOnCheckedChangeListener { _, checkedId ->
                val selected = itemView.findViewById<RadioButton>(checkedId)?.tag as? String
                if (selected != null) selectedAnswers[item.id] = selected
                checkedQuizIds.remove(item.id)
                renderResult(item)
            }
            btnCheck.setOnClickListener {
                checkedQuizIds.add(item.id)
                renderResult(item)
            }
            btnPrevious.setOnClickListener { animateAndRun(it, listener::onPrevious) }
            btnNext.setOnClickListener { animateAndRun(it, listener::onNext) }
        }

        private fun renderResult(item: VocabularyRes) {
            if (item.id !in checkedQuizIds) {
                tvResult.visibility = View.GONE
                tvExplanation.visibility = View.GONE
                return
            }

            val selected = selectedAnswers[item.id]
            tvResult.visibility = View.VISIBLE
            if (selected == null) {
                tvResult.setText(R.string.txt_choose_answer)
                tvResult.setTextColor(Color.DKGRAY)
                tvExplanation.visibility = View.GONE
                return
            }

            val correct = VocabularyQuizEvaluator.isCorrect(selected, item.correctAnswer)
            tvResult.text = if (correct) "Correct!" else "Incorrect. Answer: ${item.correctAnswer.orEmpty()}"
            tvResult.setTextColor(Color.parseColor(if (correct) "#16803B" else "#C62828"))
            tvExplanation.text = item.explanation.orEmpty()
            tvExplanation.visibility = if (item.explanation.isNullOrBlank()) View.GONE else View.VISIBLE
        }
    }
}
