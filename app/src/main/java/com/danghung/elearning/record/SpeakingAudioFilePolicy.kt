package com.danghung.elearning.record

object SpeakingAudioFilePolicy {
    const val MAX_FILE_SIZE_BYTES: Long = 10L * 1024L * 1024L

    private val extensionToMimeType = mapOf(
        "mp3" to "audio/mpeg",
        "m4a" to "audio/mp4",
        "mp4" to "audio/mp4",
        "wav" to "audio/wav",
        "ogg" to "audio/ogg",
        "aac" to "audio/aac",
        "flac" to "audio/flac",
        "webm" to "audio/webm"
    )

    private val supportedMimeTypes = extensionToMimeType.values.toSet() + setOf(
        "audio/x-m4a",
        "audio/x-wav"
    )

    fun validationError(displayName: String?, mimeType: String?, sizeBytes: Long?): String? {
        if (sizeBytes != null && sizeBytes > MAX_FILE_SIZE_BYTES) {
            return "Audio file must be 10 MB or smaller."
        }

        val normalizedMimeType = mimeType?.lowercase()?.substringBefore(';')?.trim()
        val extension = displayName
            ?.substringAfterLast('.', missingDelimiterValue = "")
            ?.lowercase()

        val supportedByMime = normalizedMimeType in supportedMimeTypes
        val supportedByExtension = extension in extensionToMimeType
        if (!supportedByMime && !supportedByExtension) {
            return "Unsupported audio format. Choose MP3, M4A, WAV, OGG, AAC, FLAC or WEBM."
        }
        return null
    }

    fun extension(displayName: String?, mimeType: String?): String {
        val fileExtension = displayName
            ?.substringAfterLast('.', missingDelimiterValue = "")
            ?.lowercase()
            ?.takeIf(extensionToMimeType::containsKey)
        if (fileExtension != null) return fileExtension

        val normalizedMimeType = mimeType?.lowercase()?.substringBefore(';')?.trim()
        return when (normalizedMimeType) {
            "audio/mpeg" -> "mp3"
            "audio/mp4", "audio/x-m4a" -> "m4a"
            "audio/wav", "audio/x-wav" -> "wav"
            "audio/ogg" -> "ogg"
            "audio/aac" -> "aac"
            "audio/flac" -> "flac"
            "audio/webm" -> "webm"
            else -> "m4a"
        }
    }

    fun normalizedMimeType(displayName: String?, mimeType: String?): String {
        val normalized = mimeType?.lowercase()?.substringBefore(';')?.trim()
        if (normalized in supportedMimeTypes) {
            return when (normalized) {
                "audio/x-m4a" -> "audio/mp4"
                "audio/x-wav" -> "audio/wav"
                else -> normalized.orEmpty()
            }
        }
        return extensionToMimeType[extension(displayName, mimeType)] ?: "audio/mp4"
    }
}
