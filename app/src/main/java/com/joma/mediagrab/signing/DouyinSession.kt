package com.joma.mediagrab.signing

import com.joma.mediagrab.core.network.UserAgents
import com.joma.mediagrab.core.result.AppResult

data class DouyinSession(
    val uifid: String,
    val verifyFp: String,
    val userAgent: String,
    val browserInfo: String = ABogus.DEFAULT_BROWSER_INFO,
    val cookies: Map<String, String> = emptyMap(),
) {
    companion object {
        const val DEFAULT_USER_AGENT: String = UserAgents.DESKTOP_CHROME
    }
}

interface DouyinSessionProvider {
    suspend fun get(): AppResult<DouyinSession>
}

class StaticDouyinSessionProvider(
    private val session: DouyinSession,
) : DouyinSessionProvider {
    override suspend fun get(): AppResult<DouyinSession> = AppResult.Ok(session)
}
