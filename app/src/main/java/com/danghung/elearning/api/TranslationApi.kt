package com.danghung.elearning.api

import com.danghung.elearning.model.MyMemoryResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface TranslationApi {
    // en|vi = English sang Tiếng Việt
    @GET("get")
    fun translate(
        @Query("q") query: String,
        @Query("langpair") langPair: String = "en|vi"
    ): Call<MyMemoryResponse>
}