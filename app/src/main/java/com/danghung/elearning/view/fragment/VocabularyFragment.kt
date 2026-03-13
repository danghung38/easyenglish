package com.danghung.elearning.view.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import com.danghung.elearning.api.res.VocabularyRes
import com.danghung.elearning.databinding.FragmentVocabularyBinding
import com.danghung.elearning.view.adapter.VocabularyAdapter
import com.danghung.elearning.viewmodel.VocabularyVM
import com.danghung.elearning.viewmodel.VocabularyVM.Companion.GET_VOCABULARY


class VocabularyFragment : BaseFragment<FragmentVocabularyBinding, VocabularyVM>(),
    VocabularyAdapter.OnVocabularyClickListener {
    companion object {
        val TAG: String = VocabularyFragment::class.java.name
    }

    private lateinit var title: String
    private lateinit var vocabularyAdapter: VocabularyAdapter

    override fun getClassVM(): Class<VocabularyVM> = VocabularyVM::class.java

    override fun initViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentVocabularyBinding = FragmentVocabularyBinding.inflate(inflater, container, false)

    override fun initViews() {
        title = data as? String ?: ""
        binding.tvTitle.text = title
        callBack.showLoading()
        viewModel.getVocabularies(title)

        binding.ivBack.setOnClickListener { callBack.showFragment(MenuFragment.TAG, null, false) }
    }

    @Suppress("UNCHECKED_CAST")
    override fun handleApiSuccess(key: String, data: Any?) {
        when (key) {
            GET_VOCABULARY -> {
                val list = data as? List<VocabularyRes> ?: emptyList()
                vocabularyAdapter = VocabularyAdapter(list, this, requireContext())
                binding.vpVocabulary.setAdapter(vocabularyAdapter)
            }
        }
    }

    override fun onNext() {
        val current = binding.vpVocabulary.currentItem
        if (current < vocabularyAdapter.itemCount - 1) {
            binding.vpVocabulary.setCurrentItem(current + 1, true)
        }
    }

    override fun onPrevious() {
        val current = binding.vpVocabulary.currentItem
        if (current > 0) {
            binding.vpVocabulary.setCurrentItem(current - 1, true)
        }
    }

}