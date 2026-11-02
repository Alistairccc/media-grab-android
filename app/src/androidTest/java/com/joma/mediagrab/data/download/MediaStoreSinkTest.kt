package com.joma.mediagrab.data.download

import android.provider.MediaStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MediaStoreSinkTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun publishesCompletedVideoIntoMoviesMediaGrab() {
        val name = "mediagrab_probe_${System.currentTimeMillis()}.mp4"
        val sink = MediaStoreSink(context)
        val file = sink.create(name, "video/mp4")
        file.output().use { it.write(ByteArray(2048)) }
        file.publish()

        val row = query(name)
        assertEquals("Movies/MediaGrab", row?.relativePath?.trimEnd('/'))
        assertEquals(0, row?.pending)
        assertEquals(2048L, row?.size)
        delete(row?.id)
    }

    @Test
    fun discardsUnfinishedVideo() {
        val name = "mediagrab_cancel_${System.currentTimeMillis()}.mp4"
        val sink = MediaStoreSink(context)
        sink.create(name, "video/mp4").discard()

        assertNull(query(name))
    }

    private fun query(name: String): Row? {
        val projection =
            arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.RELATIVE_PATH,
                MediaStore.Video.Media.IS_PENDING,
                MediaStore.Video.Media.SIZE,
            )
        context.contentResolver
            .query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                "${MediaStore.Video.Media.DISPLAY_NAME}=?",
                arrayOf(name),
                null,
            ).use { cursor ->
                if (cursor == null || !cursor.moveToFirst()) {
                    return null
                }
                return Row(
                    id = cursor.getLong(0),
                    relativePath = cursor.getString(1),
                    pending = cursor.getInt(2),
                    size = cursor.getLong(3),
                )
            }
    }

    private fun delete(id: Long?) {
        if (id != null) {
            context.contentResolver.delete(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                "${MediaStore.Video.Media._ID}=?",
                arrayOf(id.toString()),
            )
        }
    }

    private data class Row(
        val id: Long,
        val relativePath: String?,
        val pending: Int,
        val size: Long,
    )
}
