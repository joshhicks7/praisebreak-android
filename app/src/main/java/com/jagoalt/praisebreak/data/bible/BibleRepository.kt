package com.jagoalt.praisebreak.data.bible

import com.jagoalt.praisebreak.domain.model.BibleVersion
import com.jagoalt.praisebreak.domain.model.Scripture
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BibleRepository(private val api: YouVersionApi = YouVersionApi()) {
    suspend fun versions(apiKey: String, language: String): List<BibleVersion> = withContext(Dispatchers.IO) {
        val code = BibleLanguages.normalize(language)
        if (code != BibleLanguages.ENGLISH) {
            val local = runCatching { pages(apiKey, code) }.getOrNull().orEmpty()
            if (local.isNotEmpty()) return@withContext local
        }
        pages(apiKey, BibleLanguages.ENGLISH)
    }

    private fun pages(apiKey: String, language: String): List<BibleVersion> {
        val all = mutableListOf<BibleVersion>()
        var token: String? = null
        repeat(8) {
            val url = buildString {
                append(YouVersionApi.ROOT)
                append("/bibles?page_size=99&language_ranges%5B%5D=")
                append(URLEncoder.encode(language, Charsets.UTF_8.name()))
                if (!token.isNullOrBlank()) {
                    append("&page_token=")
                    append(URLEncoder.encode(token, Charsets.UTF_8.name()))
                }
            }
            val page = YouVersionJson.versions(api.get(url, apiKey))
            all += page.versions
            token = page.nextToken
            if (token.isNullOrBlank()) return all
        }
        return all
    }

    suspend fun passage(apiKey: String, bibleId: Int, passageId: String): Scripture {
        return withContext(Dispatchers.IO) {
            val safeId = if (passageId.matches(PASSAGE)) passageId else "JHN.3.16"
            val url = "${YouVersionApi.ROOT}/bibles/$bibleId/passages/$safeId?format=text"
            YouVersionJson.passage(api.get(url, apiKey))
        }
    }

    private companion object {
        val PASSAGE = Regex("[A-Z0-9.]+")
    }
}
