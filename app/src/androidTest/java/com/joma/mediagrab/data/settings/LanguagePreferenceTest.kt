package com.joma.mediagrab.data.settings

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LanguagePreferenceTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val preferences =
        context.getSharedPreferences(LanguagePreference.PREFS_NAME, Context.MODE_PRIVATE)

    @Before
    fun clearBefore() {
        preferences.edit().clear().commit()
    }

    @After
    fun clearAfter() {
        preferences.edit().clear().commit()
    }

    @Test
    fun defaultsToSystemWithoutASavedChoice() {
        assertEquals(AppLanguage.SYSTEM, LanguagePreference(context).language.value)
    }

    @Test
    fun selectionSurvivesANewInstance() {
        LanguagePreference(context).select(AppLanguage.CHINESE)

        assertEquals(AppLanguage.CHINESE, LanguagePreference(context).language.value)
    }

    @Test
    fun wrapAppliesTheSavedLanguage() {
        LanguagePreference(context).select(AppLanguage.CHINESE)

        val wrapped = LocaleContextWrapper.wrap(context)

        assertEquals("zh", wrapped.resources.configuration.locales[0].language)
    }
}
