package com.danghung.elearning.record

import java.io.File

interface AudioPlayer {
    fun playFile(file: File)
    fun playUrl(url: String)
    fun stop()
}