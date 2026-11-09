package com.joma.mediagrab.data.settings

import androidx.annotation.StringRes
import com.joma.mediagrab.R
import java.util.Locale

enum class AppLanguage(
    val key: String,
    val locale: Locale?,
    @param:StringRes val labelRes: Int,
) {
    SYSTEM("system", null, R.string.language_system),
    ENGLISH("en", Locale.ENGLISH, R.string.language_english),
    CHINESE("zh", Locale.SIMPLIFIED_CHINESE, R.string.language_chinese),
    ;

    companion object {
        fun fromKey(key: String?): AppLanguage = entries.firstOrNull { it.key == key } ?: SYSTEM
    }
}
