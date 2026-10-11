package com.jagoalt.praisebreak.data.bible

import com.jagoalt.praisebreak.domain.model.BibleVersion
import com.jagoalt.praisebreak.domain.model.Scripture
import org.json.JSONObject

data class BiblePage(
    val versions: List<BibleVersion>,
    val nextToken: String?
)

object YouVersionJson {
    fun versions(body: String): BiblePage {
        val root = JSONObject(body)
        val data = root.optJSONArray("data") ?: return BiblePage(emptyList(), null)
        val versions = buildList {
            for (index in 0 until data.length()) {
                val item = data.optJSONObject(index) ?: continue
                val id = item.optInt("id", 0)
                if (id == 0) continue
                add(
                    BibleVersion(
                        id = id,
                        abbreviation = text(item, "localized_abbreviation").ifBlank { text(item, "abbreviation") },
                        title = text(item, "localized_title").ifBlank { text(item, "title") },
                        language = text(item, "language_tag"),
                        copyright = clean(text(item, "copyright"))
                    )
                )
            }
        }
        return BiblePage(versions, text(root, "next_page_token").ifBlank { null })
    }

    fun passage(body: String): Scripture {
        val root = JSONObject(body)
        return Scripture(
            id = root.optString("id"),
            reference = root.optString("reference"),
            content = plain(root.optString("content"))
        )
    }

    private fun text(item: JSONObject, key: String): String {
        if (!item.has(key) || item.isNull(key)) return ""
        return item.optString(key)
    }

    fun clean(value: String): String {
        val trimmed = value.trim()
        if (trimmed.length >= 2 && trimmed.first() == '"' && trimmed.last() == '"') {
            return trimmed.substring(1, trimmed.lastIndex).trim()
        }
        return trimmed
    }

    private fun plain(value: String): String {
        return value
            .replace(Regex("<[^>]+>"), " ")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&#39;", "'")
            .replace("&quot;", "\"")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
