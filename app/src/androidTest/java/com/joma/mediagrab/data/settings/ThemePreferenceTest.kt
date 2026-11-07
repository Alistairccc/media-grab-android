package com.joma.mediagrab.data.settings

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.joma.mediagrab.ui.theme.AppTheme
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThemePreferenceTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val preferences = context.getSharedPreferences("mediagrab_settings", Context.MODE_PRIVATE)

    @Before
    fun clearBefore() {
        preferences.edit().clear().commit()
    }

    @After
    fun clearAfter() {
        preferences.edit().clear().commit()
    }

    @Test
    fun defaultsToCyanWithoutASavedChoice() {
        assertEquals(AppTheme.CYAN, ThemePreference(context).theme.value)
    }

    @Test
    fun selectionSurvivesANewInstance() {
        ThemePreference(context).select(AppTheme.VIOLET)

        assertEquals(AppTheme.VIOLET, ThemePreference(context).theme.value)
    }
}
