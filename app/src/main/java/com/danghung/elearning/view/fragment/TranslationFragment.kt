package com.danghung.elearning.view.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.OnBackPressedCallback
import com.danghung.elearning.api.res.ApiErrorRes
import com.danghung.elearning.databinding.FragmentTranslationBinding
import com.danghung.elearning.model.MyMemoryResponse
import com.danghung.elearning.view.fragment.MenuFragment.Companion.TYPE_HOME
import com.danghung.elearning.view.fragment.MenuFragment.Companion.TYPE_LEARN
import com.danghung.elearning.viewmodel.TranslationVM

class TranslationFragment : BaseFragment<FragmentTranslationBinding, TranslationVM>() {
    companion object {
        val TAG: String = TranslationFragment::class.java.name
    }

    private lateinit var title: String

    override fun getClassVM(): Class<TranslationVM> = TranslationVM::class.java

    override fun initViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentTranslationBinding = FragmentTranslationBinding.inflate(inflater, container, false)

    override fun initViews() {

        title = data as? String ?: ""
        binding.edtSearch.setText(title)
        doTranslate()

        binding.btnSearch.setOnClickListener(this)
        binding.btnSwapLang.setOnClickListener(this)
        binding.edtSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                doTranslate(); true
            } else false
        }
        setUpOnBackMenu(TYPE_HOME)
    }

    private var isEnToVi = true  // toggle hướng dịch

    override fun clickView(v: View) {
        when (v.id) {
            binding.btnSearch.id -> doTranslate()
            binding.btnSwapLang.id -> {
                isEnToVi = !isEnToVi
                binding.tvLangFrom.text = if (isEnToVi) "EN" else "VI"
                binding.tvLangTo.text = if (isEnToVi) "VI" else "EN"
                // swap nội dung 2 ô
                val temp = binding.edtSearch.text.toString()
                binding.edtSearch.setText(binding.tvResult.text)
                binding.tvResult.text = temp
            }
        }
    }

    private fun doTranslate() {
        val text = binding.edtSearch.text.toString().trim()
        if (text.isEmpty()) return
        binding.tvResult.text = ""
        binding.progressBar.visibility = View.VISIBLE
        val langPair = if (isEnToVi) "en|vi" else "vi|en"
        viewModel.translate(text, langPair)
    }

    override fun handleApiSuccess(key: String, data: Any?) {
        binding.progressBar.visibility = View.GONE
        if (key == TranslationVM.TRANSLATE) {
            val res = data as MyMemoryResponse
            binding.tvResult.text = res.responseData.translatedText

            // Hiển thị các kết quả gợi ý từ matches nếu có
            val suggestions = res.matches
                ?.filter {
                    it.segment.isNotBlank() &&
                            it.translation.isNotBlank() &&
                            !it.translation.contains("[object", true)
                }
                ?.distinctBy { it.translation.lowercase() }
                ?.filter {
                    it.translation != res.responseData.translatedText
                }
                ?.take(3)
            if (!suggestions.isNullOrEmpty()) {

                binding.tvSuggestions.visibility = View.VISIBLE

                binding.tvSuggestions.text = suggestions.joinToString("\n\n") {
                    "• ${it.segment}\n  → ${it.translation}"
                }

            } else {
                binding.tvSuggestions.visibility = View.GONE
            }
        }
    }

    override fun handleApiError(key: String, code: Int, error: ApiErrorRes?) {
        binding.progressBar.visibility = View.GONE
        notify("Translation failed, please try again.")
    }
}