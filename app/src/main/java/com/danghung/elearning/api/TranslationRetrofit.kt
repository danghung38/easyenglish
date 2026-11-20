package com.danghung.elearning.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object TranslationRetrofit {
    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.mymemory.translated.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun <T> api(cls: Class<T>): T = instance.create(cls)
}