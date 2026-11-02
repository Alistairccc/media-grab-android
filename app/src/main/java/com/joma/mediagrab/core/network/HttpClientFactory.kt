package com.joma.mediagrab.core.network

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object HttpClientFactory {
    fun create(): OkHttpClient =
        OkHttpClient
            .Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
}
