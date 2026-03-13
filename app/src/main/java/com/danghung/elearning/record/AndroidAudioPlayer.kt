package com.danghung.elearning.record

import android.content.Context
import android.media.MediaPlayer
import androidx.core.net.toUri
import java.io.File

class AndroidAudioPlayer(
    private val context: Context
): AudioPlayer {

    private var player: MediaPlayer? = null

    override fun playFile(file: File) {
        MediaPlayer.create(context, file.toUri()).apply {
            player = this
            start()
        }
    }

    override fun playUrl(url: String) {
        stop()
        player = MediaPlayer().apply {
            setDataSource(url)
            prepare()
            start()
        }
    }

    override fun stop() {
        player?.stop()
        player?.release()
        player = null
    }
}