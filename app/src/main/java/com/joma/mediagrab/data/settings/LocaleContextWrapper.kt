package com.joma.mediagrab.data.settings

import android.content.Context
import android.content.res.Configuration

object LocaleContextWrapper {
    fun wrap(context: Context): Context {
        val locale = LanguagePreference.stored(context).locale ?: return context
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }
}
