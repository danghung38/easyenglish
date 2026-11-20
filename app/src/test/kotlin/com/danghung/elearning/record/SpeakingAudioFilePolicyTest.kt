package com.danghung.elearning.record

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SpeakingAudioFilePolicyTest {

    @Test
    fun acceptsSupportedAudioFormats() {
        assertNull(SpeakingAudioFilePolicy.validationError("answer.mp3", "audio/mpeg", 1024))
        assertNull(SpeakingAudioFilePolicy.validationError("answer.wav", null, 1024))
        assertNull(SpeakingAudioFilePolicy.validationError(null, "audio/x-m4a", null))
    }

    @Test
    fun rejectsUnsupportedFile() {
        val error = SpeakingAudioFilePolicy.validationError(
            displayName = "notes.pdf",
            mimeType = "application/pdf",
            sizeBytes = 1024
        )

        assertTrue(error.orEmpty().contains("Unsupported audio format"))
    }

    @Test
    fun rejectsFileLargerThanBackendLimit() {
        val error = SpeakingAudioFilePolicy.validationError(
            displayName = "answer.mp3",
            mimeType = "audio/mpeg",
            sizeBytes = SpeakingAudioFilePolicy.MAX_FILE_SIZE_BYTES + 1
        )

        assertTrue(error.orEmpty().contains("10 MB"))
    }

    @Test
    fun derivesExtensionAndNormalizedMimeType() {
        assertEquals("wav", SpeakingAudioFilePolicy.extension(null, "audio/x-wav"))
        assertEquals(
            "audio/mp4",
            SpeakingAudioFilePolicy.normalizedMimeType("sample.m4a", "audio/x-m4a")
        )
        assertEquals(
            "audio/flac",
            SpeakingAudioFilePolicy.normalizedMimeType("sample.flac", null)
        )
    }
}
