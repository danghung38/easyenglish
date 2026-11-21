package com.danghung.elearning.api

import com.danghung.elearning.api.res.ApiRes
import com.danghung.elearning.api.res.StoryDetailRes
import com.danghung.elearning.model.BilingualStory
import com.danghung.elearning.model.StoryLevel
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface StoryApi {
    @GET("stories")
    suspend fun getStories(@Query("level") level: StoryLevel? = null): ApiRes<List<BilingualStory>>

    @GET("stories/{id}")
    suspend fun getStory(@Path("id") id: Long): ApiRes<StoryDetailRes>
}
