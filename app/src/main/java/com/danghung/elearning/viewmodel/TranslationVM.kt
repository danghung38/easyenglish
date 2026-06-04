// viewmodel/TranslationVM.kt
package com.danghung.elearning.viewmodel

import com.danghung.elearning.api.TranslationApi
import com.danghung.elearning.api.TranslationRetrofit
import com.danghung.elearning.model.MyMemoryResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TranslationVM : BaseViewModel() {
    companion object {
        const val TRANSLATE = "TRANSLATE"
    }

    fun translate(text: String, langPair: String = "en|vi") {
        TranslationRetrofit.api(TranslationApi::class.java)
            .translate(text, langPair)
            .enqueue(object : Callback<MyMemoryResponse> {
                override fun onResponse(
                    call: Call<MyMemoryResponse>,
                    response: Response<MyMemoryResponse>
                ) {
                    if (response.isSuccessful && response.body()?.responseStatus == 200) {
                        callBack?.apiSuccess(TRANSLATE, response.body())
                    } else {
                        callBack?.apiError(TRANSLATE, response.code(), null)
                    }
                }

                override fun onFailure(call: Call<MyMemoryResponse>, t: Throwable) {
                    callBack?.apiError(TRANSLATE, 999, t.message)
                }
            })
    }

    override fun handleSuccess(key: String?, data: Any?) {}
}