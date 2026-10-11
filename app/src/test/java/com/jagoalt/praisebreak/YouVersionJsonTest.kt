package com.jagoalt.praisebreak

import com.jagoalt.praisebreak.data.bible.YouVersionJson
import org.junit.Assert.assertEquals
import org.junit.Test

class YouVersionJsonTest {
    @Test
    fun readsVersionsAndStripsWrappedCopyrightQuotes() {
        val body = """
            {
              "data": [
                {
                  "id": 3034,
                  "abbreviation": "BSB",
                  "title": "Berean Standard Bible",
                  "language_tag": "en",
                  "copyright": "\"Public domain.\""
                }
              ],
              "next_page_token": "next"
            }
        """.trimIndent()
        val page = YouVersionJson.versions(body)
        assertEquals(3034, page.versions.single().id)
        assertEquals("BSB", page.versions.single().abbreviation)
        assertEquals("Public domain.", page.versions.single().copyright)
        assertEquals("next", page.nextToken)
    }

    @Test
    fun treatsANullPageTokenAsTheEnd() {
        val page = YouVersionJson.versions(
            """
            {
              "data": [
                {"id": 3844, "abbreviation": "turytc", "title": "Literal Turkish Bible Translation (YTC)", "language_tag": "tr", "copyright": null}
              ],
              "next_page_token": null
            }
            """.trimIndent()
        )
        assertEquals(null, page.nextToken)
        assertEquals("", page.versions.single().copyright)
        assertEquals("turytc", page.versions.single().abbreviation)
    }

    @Test
    fun readsPassageTextWithoutTags() {
        val verse = YouVersionJson.passage(
            """{"id":"JHN.3.16","content":"<p>For God so loved.</p>","reference":"John 3:16"}"""
        )
        assertEquals("JHN.3.16", verse.id)
        assertEquals("John 3:16", verse.reference)
        assertEquals("For God so loved.", verse.content)
    }
}
