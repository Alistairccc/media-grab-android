package com.joma.mediagrab.data.settings

import android.content.Context
import com.joma.mediagrab.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemePreference(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _theme = MutableStateFlow(AppTheme.fromKey(preferences.getString(KEY_THEME, null)))
    val theme: StateFlow<AppTheme> = _theme.asStateFlow()

    fun select(theme: AppTheme) {
        preferences.edit().putString(KEY_THEME, theme.key).apply()
        _theme.value = theme
    }

    private companion object {
        const val PREFS_NAME = "mediagrab_settings"
        const val KEY_THEME = "theme"
    }
}
