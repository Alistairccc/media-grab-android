package com.joma.mediagrab

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.joma.mediagrab.di.AppContainer
import timber.log.Timber

class MediaGrabApp :
    Application(),
    SingletonImageLoader.Factory {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        container = AppContainer(this)
    }

    override fun newImageLoader(context: Context): ImageLoader =
        ImageLoader
            .Builder(context)
            .components { add(OkHttpNetworkFetcherFactory()) }
            .build()
}
