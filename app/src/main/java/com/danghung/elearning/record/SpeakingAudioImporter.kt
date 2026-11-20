package com.danghung.elearning.record

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream

class SpeakingAudioImporter(private val context: Context) {

    fun import(uri: Uri): ImportedSpeakingAudio {
        val resolver = context.contentResolver
        val metadata = resolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
            null,
            null,
            null
        )?.use { cursor ->
            if (!cursor.moveToFirst()) return@use AudioMetadata(null, null)
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            AudioMetadata(
                displayName = nameIndex.takeIf { it >= 0 }?.let(cursor::getString),
                sizeBytes = sizeIndex.takeIf { it >= 0 && !cursor.isNull(it) }
                    ?.let(cursor::getLong)
            )
        } ?: AudioMetadata(null, null)

        val mimeType = resolver.getType(uri)
        SpeakingAudioFilePolicy.validationError(
            metadata.displayName,
            mimeType,
            metadata.sizeBytes
        )?.let { throw IllegalArgumentException(it) }

        val importDirectory = File(context.cacheDir, "speaking_audio").apply { mkdirs() }
        importDirectory.listFiles()?.forEach(File::delete)
        val extension = SpeakingAudioFilePolicy.extension(metadata.displayName, mimeType)
        val destination = File(importDirectory, "selected_audio.$extension")

        try {
            resolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destination).use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var totalBytes = 0L
                    while (true) {
                        val bytesRead = input.read(buffer)
                        if (bytesRead < 0) break
                        totalBytes += bytesRead
                        if (totalBytes > SpeakingAudioFilePolicy.MAX_FILE_SIZE_BYTES) {
                            throw IllegalArgumentException("Audio file must be 10 MB or smaller.")
                        }
                        output.write(buffer, 0, bytesRead)
                    }
                }
            } ?: throw IllegalArgumentException("Cannot read the selected audio file.")
        } catch (error: Exception) {
            destination.delete()
            throw error
        }

        if (destination.length() == 0L) {
            destination.delete()
            throw IllegalArgumentException("The selected audio file is empty.")
        }

        return ImportedSpeakingAudio(
            file = destination,
            displayName = metadata.displayName ?: destination.name,
            mimeType = SpeakingAudioFilePolicy.normalizedMimeType(metadata.displayName, mimeType)
        )
    }

    data class ImportedSpeakingAudio(
        val file: File,
        val displayName: String,
        val mimeType: String
    )

    private data class AudioMetadata(
        val displayName: String?,
        val sizeBytes: Long?
    )
}
