package com.danghung.elearning.view.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isVisible
import com.danghung.elearning.R
import com.danghung.elearning.databinding.ViewScoreResultBinding
import java.util.Locale

/** Shows the submitted band and optionally starts on-demand feedback generation. */
class ScoreResultDialog(
    context: Context,
    skillName: String,
    score: Double?,
    private val onRequestFeedback: (() -> Unit)? = null,
    scoreLabel: String = context.getString(R.string.txt_ielts_band_score),
    awaitingFinalScore: Boolean = false,
    private val onDone: () -> Unit
) : Dialog(context) {

    private val binding = ViewScoreResultBinding.inflate(layoutInflater)

    init {
        setContentView(binding.root)
        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        setCancelable(false)
        setCanceledOnTouchOutside(false)

        updateResult(skillName, score, scoreLabel, awaitingFinalScore)
        binding.btnRequestFeedback.setOnClickListener {
            dismiss()
            onRequestFeedback?.invoke()
        }
        binding.btnDone.setOnClickListener {
            dismiss()
            onDone()
        }
    }

    /** Rebinds a provisional result when the background reviewer publishes the final band. */
    fun updateResult(
        skillName: String,
        score: Double?,
        scoreLabel: String,
        awaitingFinalScore: Boolean = false
    ) {
        binding.tvResultTitle.text = context.getString(R.string.txt_score_result_title, skillName)
        binding.tvScoreLabel.text = scoreLabel
        binding.tvBandScore.text = score?.let { String.format(Locale.US, "%.1f", it) } ?: "--"
        setAwaitingFinalScore(awaitingFinalScore)
    }

    fun setAwaitingFinalScore(awaiting: Boolean) {
        binding.btnDone.isEnabled = !awaiting
        binding.btnDone.alpha = if (awaiting) 0.55f else 1f
        binding.btnDone.text = context.getString(
            if (awaiting) R.string.txt_confirming_final_score else R.string.txt_done
        )
        binding.btnRequestFeedback.isVisible = onRequestFeedback != null && !awaiting
    }
}
