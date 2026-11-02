package com.joma.mediagrab.download

import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import timber.log.Timber

class PendingMediaCleaner(
    private val context: Context,
) {
    fun cleanup() {
        val cutoff = (System.currentTimeMillis() - MAX_AGE_MS) / 1000
        val selection =
            "${MediaStore.Video.Media.RELATIVE_PATH} LIKE ? AND " +
                "${MediaStore.Video.Media.IS_PENDING} = 1 AND " +
                "${MediaStore.Video.Media.DATE_ADDED} < ?"
        try {
            context.contentResolver.delete(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                selection,
                arrayOf("${Environment.DIRECTORY_MOVIES}/MediaGrab%", cutoff.toString()),
            )
        } catch (error: SecurityException) {
            Timber.w(error, "pending MediaStore cleanup skipped")
        }
    }

    private companion object {
        const val MAX_AGE_MS = 24L * 60 * 60 * 1000
    }
}
