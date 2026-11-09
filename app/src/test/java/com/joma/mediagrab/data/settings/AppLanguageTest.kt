package com.joma.mediagrab.data.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AppLanguageTest {
    @Test
    fun `unknown key falls back to system`() {
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromKey("fr"))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromKey(null))
    }

    @Test
    fun `keys round trip`() {
        AppLanguage.entries.forEach { language ->
            assertEquals(language, AppLanguage.fromKey(language.key))
        }
    }

    @Test
    fun `system has no locale while explicit choices do`() {
        assertNull(AppLanguage.SYSTEM.locale)
        assertNotNull(AppLanguage.ENGLISH.locale)
        assertNotNull(AppLanguage.CHINESE.locale)
    }
}
