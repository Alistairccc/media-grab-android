package com.joma.mediagrab.data.download

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import java.io.IOException
import java.io.OutputStream

interface DownloadSink {
    fun create(
        displayName: String,
        mime: String,
    ): DownloadFile
}

interface DownloadFile {
    fun output(): OutputStream

    fun publish()

    fun discard()
}

/**
 * Writes an in-progress download into `Movies/MediaGrab`.
 *
 * The row stays pending until the bytes are complete, so a failed or cancelled
 * download never shows up in the gallery.
 */
class MediaStoreSink(
    private val context: Context,
) : DownloadSink {
    override fun create(
        displayName: String,
        mime: String,
    ): DownloadFile {
        val values =
            ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Video.Media.MIME_TYPE, mime)
                put(
                    MediaStore.Video.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_MOVIES}/MediaGrab",
                )
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        val uri =
            context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
                ?: throw IOException("MediaStore insert failed")
        return object : DownloadFile {
            override fun output(): OutputStream =
                context.contentResolver.openOutputStream(uri)
                    ?: throw IOException("MediaStore open failed")

            override fun publish() {
                val ready = ContentValues().apply { put(MediaStore.Video.Media.IS_PENDING, 0) }
                context.contentResolver.update(uri, ready, null, null)
            }

            override fun discard() {
                context.contentResolver.delete(uri, null, null)
            }
        }
    }
}
