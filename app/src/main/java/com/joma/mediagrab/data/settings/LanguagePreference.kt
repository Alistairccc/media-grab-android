package com.joma.mediagrab.data.settings

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LanguagePreference(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _language = MutableStateFlow(stored(context))
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    fun select(language: AppLanguage) {
        preferences.edit().putString(KEY_LANGUAGE, language.key).apply()
        _language.value = language
    }

    companion object {
        const val PREFS_NAME = "mediagrab_settings"
        const val KEY_LANGUAGE = "language"

        fun stored(context: Context): AppLanguage =
            AppLanguage.fromKey(
                context
                    .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .getString(KEY_LANGUAGE, null),
            )
    }
}
