package com.example.audio

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

object IcyMetadataReader {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val titlePattern = Pattern.compile("StreamTitle='(.*?)';", Pattern.CASE_INSENSITIVE)

    suspend fun fetchLiveMetadata(streamUrl: String): Pair<String?, String?> = withContext(Dispatchers.IO) {
        var inputStream: InputStream? = null
        try {
            val request = Request.Builder()
                .url(streamUrl)
                .addHeader("Icy-MetaData", "1")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .addHeader("Accept", "*/*")
                .build()

            val response = client.newCall(request).execute()
            val metaIntHeader = response.header("icy-metaint")
            val metaInt = metaIntHeader?.toIntOrNull()

            if (metaInt != null && metaInt > 0) {
                inputStream = response.body?.byteStream()
                if (inputStream != null) {
                    // Skip audio payload bytes up to metaInt
                    var bytesToSkip = metaInt.toLong()
                    while (bytesToSkip > 0) {
                        val skipped = inputStream.skip(bytesToSkip)
                        if (skipped <= 0) break
                        bytesToSkip -= skipped
                    }

                    // Read 1-byte metadata length indicator (length in blocks of 16 bytes)
                    val lengthByte = inputStream.read()
                    if (lengthByte > 0) {
                        val metaLength = lengthByte * 16
                        val metaBuffer = ByteArray(metaLength)
                        var readSoFar = 0
                        while (readSoFar < metaLength) {
                            val r = inputStream.read(metaBuffer, readSoFar, metaLength - readSoFar)
                            if (r <= 0) break
                            readSoFar += r
                        }
                        val metaString = String(metaBuffer, 0, readSoFar, Charsets.UTF_8)
                        val matcher = titlePattern.matcher(metaString)
                        if (matcher.find()) {
                            val fullTitle = matcher.group(1)?.trim()
                            if (!fullTitle.isNullOrBlank()) {
                                response.close()
                                return@withContext parseArtistAndTitle(fullTitle)
                            }
                        }
                    }
                }
            }
            response.close()
        } catch (_: Exception) {
            // Silently return null on network hiccup
        } finally {
            try { inputStream?.close() } catch (_: Exception) {}
        }
        null to null
    }

    fun parseArtistAndTitle(rawString: String): Pair<String, String> {
        val clean = rawString.trim()
        val parts = clean.split(" - ", limit = 2)
        return if (parts.size == 2) {
            val artist = parts[0].trim()
            val track = parts[1].trim()
            artist to track
        } else {
            "" to clean
        }
    }
}
