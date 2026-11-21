package com.danghung.elearning.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.danghung.elearning.api.ExamApi
import com.danghung.elearning.api.StoryApi
import com.danghung.elearning.api.res.ApiRes
import com.danghung.elearning.api.res.ExamRes
import com.danghung.elearning.api.res.PageRes
import com.danghung.elearning.model.BilingualStory
import com.danghung.elearning.model.StoryLevel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class HomeVM : BaseViewModel() {
    private val _exams = MutableLiveData<List<ExamRes>>()
    val exams: LiveData<List<ExamRes>> = _exams

    private val _featuredStory = MutableLiveData<BilingualStory?>()
    val featuredStory: LiveData<BilingualStory?> = _featuredStory
    private var storyRequest: Job? = null

    private var examRequest: Call<ApiRes<PageRes<List<ExamRes>>>>? = null
    private var answerDay: Long? = null
    private var answerWord: Int? = null

    fun loadFeaturedStory() {
        if (storyRequest?.isActive == true || _featuredStory.value != null) return
        storyRequest = viewModelScope.launch {
            _featuredStory.value = try {
                api(StoryApi::class.java).getStories(StoryLevel.BEGINNER).result?.firstOrNull()
            } catch (_: IOException) {
                null
            } catch (_: HttpException) {
                null
            }
        }
    }

    fun loadExams() {
        if (examRequest != null || _exams.value != null) return

        val request = api(ExamApi::class.java).getExamList(sortBy = "id:desc")
        examRequest = request
        request.enqueue(object : Callback<ApiRes<PageRes<List<ExamRes>>>> {
            override fun onResponse(
                call: Call<ApiRes<PageRes<List<ExamRes>>>>,
                response: Response<ApiRes<PageRes<List<ExamRes>>>>
            ) {
                examRequest = null
                if (response.isSuccessful) {
                    _exams.value = response.body()?.result?.items.orEmpty()
                        .filter { !it.title.isNullOrBlank() && !it.examParts.isNullOrEmpty() }
                }
            }

            override fun onFailure(call: Call<ApiRes<PageRes<List<ExamRes>>>>, t: Throwable) {
                examRequest = null
            }
        })
    }

    fun answerFor(day: Long): Int? = answerWord.takeIf { answerDay == day }

    fun selectAnswer(day: Long, word: Int) {
        answerDay = day
        answerWord = word
    }

    override fun onCleared() {
        examRequest?.cancel()
        super.onCleared()
    }
}
