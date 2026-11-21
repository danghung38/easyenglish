package com.danghung.elearning.util

import android.net.Uri

object GoogleTranslateSpeech {
    fun englishUrl(text: String): String = Uri.Builder()
        .scheme("https")
        .authority("translate.google.com")
        .path("/translate_tts")
        .appendQueryParameter("ie", "UTF-8")
        .appendQueryParameter("q", text)
        .appendQueryParameter("tl", "en")
        .appendQueryParameter("client", "tw-ob")
        .build()
        .toString()
}
