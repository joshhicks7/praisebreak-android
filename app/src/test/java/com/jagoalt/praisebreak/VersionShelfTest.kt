package com.jagoalt.praisebreak

import com.jagoalt.praisebreak.data.bible.BibleLanguages
import com.jagoalt.praisebreak.data.bible.VersionShelf
import java.util.Locale
import com.jagoalt.praisebreak.domain.model.BibleVersion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionShelfTest {
    @Test
    fun keepsASavedListForAWeek() {
        val now = 1_700_000_000_000L
        assertTrue(VersionShelf.fresh(now - VersionShelf.WEEK_MS + 1, now))
        assertFalse(VersionShelf.fresh(now - VersionShelf.WEEK_MS, now))
        assertFalse(VersionShelf.fresh(0L, now))
    }

    @Test
    fun roundTripsVersionFields() {
        val saved = listOf(
            BibleVersion(3034, "BSB", "Berean Standard Bible", "en", "Public domain.")
        )
        val loaded = VersionShelf.decode(VersionShelf.encode(saved))
        assertEquals(saved, loaded)
    }

    @Test
    fun usesTheDeviceLanguageAndFallsBackToEnglish() {
        assertEquals("es", BibleLanguages.code(Locale("es", "MX")))
        assertEquals("en", BibleLanguages.code(Locale.US))
        assertEquals("en", BibleLanguages.normalize(""))
        assertEquals("he", BibleLanguages.normalize("iw"))
    }
}
