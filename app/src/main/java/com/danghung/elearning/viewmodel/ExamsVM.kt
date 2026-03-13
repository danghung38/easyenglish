package com.danghung.elearning.viewmodel

import com.danghung.elearning.api.ExamApi
import com.danghung.elearning.api.res.ApiRes
import com.danghung.elearning.api.res.ExamRes
import com.danghung.elearning.api.res.PageRes

class ExamsVM : BaseViewModel() {
    companion object {
        const val GET_EXAM_LIST = "GET_EXAM_LIST"
    }

    var examList: List<ExamRes>? = null

    fun getExamList(
        pageNo: Int = 1, pageSize: Int = 20, sortBy: String? = null
    ) {
        executeApi {
            api(ExamApi::class.java).getExamList(pageNo, pageSize, sortBy)
                .enqueue(initHandleResponse(GET_EXAM_LIST))
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun handleSuccess(key: String?, data: Any?) {
        when (key) {
            GET_EXAM_LIST -> {
                val res = data as ApiRes<PageRes<List<ExamRes>>>
                callBack?.apiSuccess(GET_EXAM_LIST, res.result)
                examList = res.result?.items
            }
        }
    }
}