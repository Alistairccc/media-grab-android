package com.joma.mediagrab.download

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import com.joma.mediagrab.MediaGrabApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber

class DownloadService : Service() {
    private val manager: DownloadManager
        get() = (application as MediaGrabApp).container.downloadManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var collecting = false

    override fun onCreate() {
        super.onCreate()
        Timber.d("DownloadService created")
        DownloadNotification.ensureChannel(this)
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        if (intent?.action == ACTION_CANCEL) {
            Timber.d("DownloadService cancel action")
            manager.cancel()
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(
            DownloadNotification.NOTIFICATION_ID,
            DownloadNotification.build(this, manager.state.value),
        )
        manager.execute()
        if (!collecting) {
            collecting = true
            scope.launch { observe() }
        }
        return START_NOT_STICKY
    }

    private suspend fun observe() {
        var lastNotificationMs = 0L
        manager.state.collect { state ->
            when (state) {
                is DownloadState.Running -> {
                    val now = SystemClock.elapsedRealtime()
                    if (now - lastNotificationMs >= NOTIFICATION_INTERVAL_MS) {
                        notify(state)
                        lastNotificationMs = now
                    }
                }

                is DownloadState.Completed, is DownloadState.Failed -> {
                    notify(state)
                    ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_DETACH)
                    stopSelf()
                }

                is DownloadState.Cancelled -> {
                    ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }

                DownloadState.Idle -> Unit
            }
        }
    }

    private fun notify(state: DownloadState) {
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            return
        }
        NotificationManagerCompat
            .from(this)
            .notify(DownloadNotification.NOTIFICATION_ID, DownloadNotification.build(this, state))
    }

    override fun onDestroy() {
        Timber.d("DownloadService destroyed")
        scope.cancel()
        if (manager.state.value is DownloadState.Running) {
            manager.cancel()
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_CANCEL = "com.joma.mediagrab.action.CANCEL_DOWNLOAD"
        private const val NOTIFICATION_INTERVAL_MS = 800L
    }
}
