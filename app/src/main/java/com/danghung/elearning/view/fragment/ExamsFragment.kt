package com.danghung.elearning.view.fragment

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.danghung.elearning.api.res.ExamRes
import com.danghung.elearning.api.res.PageRes
import com.danghung.elearning.databinding.FragmentExamsBinding
import com.danghung.elearning.view.adapter.ExamsAdapter
import com.danghung.elearning.viewmodel.ExamsVM
import com.danghung.elearning.viewmodel.ExamsVM.Companion.GET_EXAM_LIST


class ExamsFragment : BaseFragment<FragmentExamsBinding, ExamsVM>(),
    ExamsAdapter.OnExamClickListener {
    companion object {
        val TAG: String = ExamsFragment::class.java.name
    }

    private lateinit var adapter: ExamsAdapter

    override fun getClassVM(): Class<ExamsVM> = ExamsVM::class.java

    override fun initViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentExamsBinding = FragmentExamsBinding.inflate(inflater, container, false)

    override fun initViews() {
        adapter = ExamsAdapter(emptyList(), this)

        binding.rvAllExams.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@ExamsFragment.adapter
        }

        if (viewModel.examList == null) {
            callBack.showLoading()
            viewModel.getExamList()
        } else {
            adapter.updateData(viewModel.examList!!)
        }

        handleSearchExam()
        setUpOnBackPressed()
    }

    private fun handleSearchExam() {
        binding.edtSearchExams.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onExamClick(exam: ExamRes) {
        callBack.showFragment(PracticeFragment.TAG, exam, false)
    }

    @Suppress("UNCHECKED_CAST")
    override fun handleApiSuccess(key: String, data: Any?) {
        when (key) {
            GET_EXAM_LIST -> {
                val page = data as PageRes<List<ExamRes>>
                adapter.updateData(page.items)
            }
        }
    }

    private fun setUpOnBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, object : androidx.activity.OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {

                }
            })
    }

}