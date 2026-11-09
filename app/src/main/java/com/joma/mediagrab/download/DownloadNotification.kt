package com.joma.mediagrab.download

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.joma.mediagrab.MainActivity
import com.joma.mediagrab.R
import com.joma.mediagrab.core.util.Formatters

object DownloadNotification {
    const val CHANNEL_ID = "downloads"
    const val NOTIFICATION_ID = 1001

    fun ensureChannel(context: Context) {
        val channel =
            NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel),
                NotificationManager.IMPORTANCE_LOW,
            ).apply { setShowBadge(false) }
        NotificationManagerCompat.from(context).createNotificationChannel(channel)
    }

    fun build(
        context: Context,
        state: DownloadState,
    ): Notification {
        val builder =
            NotificationCompat
                .Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_link)
                .setContentIntent(contentIntent(context))
                .setOnlyAlertOnce(true)
                .setSilent(true)
        when (state) {
            is DownloadState.Running -> {
                val percent = state.progress.percent.coerceIn(0, 100)
                builder
                    .setContentTitle(state.media.title.ifBlank { context.getString(R.string.app_name) })
                    .setContentText(
                        context.getString(
                            R.string.notification_progress,
                            percent,
                            Formatters.speedMb(state.progress.bytesPerSecond),
                        ),
                    )
                    .setProgress(100, percent, false)
                    .setOngoing(true)
                    .addAction(
                        0,
                        context.getString(R.string.notification_cancel),
                        cancelIntent(context),
                    )
            }

            is DownloadState.Completed ->
                builder
                    .setContentTitle(context.getString(R.string.notification_done))
                    .setContentText(state.media.title.ifBlank { context.getString(R.string.app_name) })
                    .setAutoCancel(true)

            is DownloadState.Failed ->
                builder
                    .setContentTitle(context.getString(R.string.notification_failed))
                    .setContentText(state.media.title.ifBlank { context.getString(R.string.app_name) })
                    .setAutoCancel(true)

            is DownloadState.Cancelled, DownloadState.Idle ->
                builder
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(context.getString(R.string.notification_preparing))
                    .setOngoing(true)
        }
        return builder.build()
    }

    private fun contentIntent(context: Context): PendingIntent {
        val intent =
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun cancelIntent(context: Context): PendingIntent {
        val intent =
            Intent(context, DownloadService::class.java)
                .setAction(DownloadService.ACTION_CANCEL)
        return PendingIntent.getService(
            context,
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }
}
