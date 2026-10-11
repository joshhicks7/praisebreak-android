package com.jagoalt.praisebreak.data.bible

import java.net.HttpURLConnection
import java.net.URL

class BibleRequestException(val code: Int) : Exception(messageFor(code))

class YouVersionApi {
    fun get(url: String, apiKey: String): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8_000
            readTimeout = 8_000
            setRequestProperty("X-YVP-App-Key", apiKey)
            setRequestProperty("Accept", "application/json")
        }
        return try {
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) throw BibleRequestException(code)
            body
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        const val ROOT = "https://api.youversion.com/v1"
    }
}

private fun messageFor(code: Int): String {
    return when (code) {
        429 -> "Please wait a minute, then try again."
        else -> "Couldn't load Bible versions."
    }
}
