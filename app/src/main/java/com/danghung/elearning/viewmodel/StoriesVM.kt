package com.danghung.elearning.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.danghung.elearning.api.StoryApi
import com.danghung.elearning.api.res.StoryDetailRes
import com.danghung.elearning.model.BilingualStory
import com.danghung.elearning.model.StoryLevel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class StoriesVM : BaseViewModel() {
    sealed interface State<out T> {
        data object Loading : State<Nothing>
        data object Failed : State<Nothing>
        data class Ready<T>(val data: T) : State<T>
    }

    private val _library = MutableLiveData<State<List<BilingualStory>>>()
    val library: LiveData<State<List<BilingualStory>>> = _library

    private val _reader = MutableLiveData<State<StoryDetailRes>>()
    val reader: LiveData<State<StoryDetailRes>> = _reader

    var selectedLevel: StoryLevel? = null
    val expandedTranslations = mutableSetOf<Int>()
    var readingVietnamese = false

    fun loadLibrary() = load(_library) { api(StoryApi::class.java).getStories().result }

    fun loadStory(id: Long) = load(_reader) { api(StoryApi::class.java).getStory(id).result }

    private fun <T : Any> load(state: MutableLiveData<State<T>>, fetch: suspend () -> T?) {
        if (state.value == State.Loading || state.value is State.Ready) return
        state.value = State.Loading
        viewModelScope.launch {
            state.value = try {
                val data = fetch()
                if (data == null) State.Failed else State.Ready(data)
            } catch (_: IOException) {
                State.Failed
            } catch (_: HttpException) {
                State.Failed
            }
        }
    }
}
