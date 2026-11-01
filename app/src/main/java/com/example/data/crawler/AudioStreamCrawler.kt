package com.example.data.crawler

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.net.URI
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class CrawledAudioStream(
    val resolvedStreamUrl: String,
    val stationName: String,
    val genre: String,
    val bitrate: Int,
    val faviconUrl: String?,
    val isFromWebpage: Boolean,
    val contentType: String
)

object AudioStreamCrawler {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private const val USER_AGENT =
        "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36 GalaxyRadio/2.0"

    suspend fun crawlForAudioFeed(rawUrl: String): Result<CrawledAudioStream> = withContext(Dispatchers.IO) {
        val trimmedUrl = rawUrl.trim()
        if (!trimmedUrl.startsWith("http://", ignoreCase = true) && !trimmedUrl.startsWith("https://", ignoreCase = true)) {
            return@withContext Result.failure(IllegalArgumentException("URL must start with http:// or https://"))
        }

        try {
            // Step 1: Probe the initial URL
            val initialProbe = probeUrl(trimmedUrl)
            if (initialProbe != null && isAudioResponse(initialProbe.contentType, initialProbe.icyName)) {
                return@withContext Result.success(
                    CrawledAudioStream(
                        resolvedStreamUrl = initialProbe.finalUrl,
                        stationName = cleanStationName(initialProbe.icyName, trimmedUrl),
                        genre = initialProbe.icyGenre ?: "Online Radio",
                        bitrate = initialProbe.icyBitrate ?: 128,
                        faviconUrl = null,
                        isFromWebpage = false,
                        contentType = initialProbe.contentType
                    )
                )
            }

            // Step 2: Check if it's a playlist file (.pls, .m3u, .asx, .xspf)
            val playlistStream = tryParsePlaylist(trimmedUrl)
            if (playlistStream != null) {
                return@withContext Result.success(playlistStream)
            }

            // Step 3: Fetch HTML webpage and aggressively crawl for streams
            val htmlContent = fetchHtmlContent(trimmedUrl)
            if (!htmlContent.isNullOrBlank()) {
                val webpageStream = crawlHtmlForStreams(trimmedUrl, htmlContent)
                if (webpageStream != null) {
                    return@withContext Result.success(webpageStream)
                }
            }

            // Step 4: Fallback to common mountpoints for this host
            val mountpointStream = probeCommonMountpoints(trimmedUrl)
            if (mountpointStream != null) {
                return@withContext Result.success(mountpointStream)
            }

            Result.failure(Exception("No active audio feed or streaming player found at this URL."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private data class ProbeResult(
        val finalUrl: String,
        val contentType: String,
        val icyName: String?,
        val icyGenre: String?,
        val icyBitrate: Int?
    )

    private fun probeUrl(url: String): ProbeResult? {
        try {
            val request = Request.Builder()
                .url(url)
                .addHeader("Icy-MetaData", "1")
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Range", "bytes=0-2048")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val finalUrl = response.request.url.toString()
            val contentType = response.header("Content-Type")?.lowercase() ?: ""
            val icyName = response.header("icy-name")
            val icyGenre = response.header("icy-genre")
            val icyBr = response.header("icy-br")?.toIntOrNull()

            response.close()

            return ProbeResult(
                finalUrl = finalUrl,
                contentType = contentType,
                icyName = icyName,
                icyGenre = icyGenre,
                icyBitrate = icyBr
            )
        } catch (_: Exception) {
            return null
        }
    }

    private fun isAudioResponse(contentType: String, icyName: String?): Boolean {
        if (!icyName.isNullOrBlank()) return true
        val ct = contentType.lowercase()
        return ct.startsWith("audio/") ||
                ct.contains("application/ogg") ||
                ct.contains("application/x-mpegurl") ||
                ct.contains("application/vnd.apple.mpegurl") ||
                ct.contains("audio/x-scpls") ||
                ct.contains("audio/mpeg") ||
                ct.contains("audio/aac")
    }

    private fun tryParsePlaylist(url: String): CrawledAudioStream? {
        val lower = url.lowercase()
        val isLikelyPlaylist = lower.contains(".pls") ||
                lower.contains(".m3u8") ||
                lower.contains(".m3u") ||
                lower.contains(".asx") ||
                lower.contains(".xspf")

        try {
            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", USER_AGENT)
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string().orEmpty()
            val ct = response.header("Content-Type")?.lowercase().orEmpty()
            response.close()

            if (!isLikelyPlaylist && !ct.contains("mpegurl") && !ct.contains("scpls")) {
                return null
            }

            // Extract candidate URLs from playlist text
            val extractedUrls = extractUrlsFromPlaylist(body, url)
            for (candidate in extractedUrls) {
                val probe = probeUrl(candidate)
                if (probe != null && isAudioResponse(probe.contentType, probe.icyName)) {
                    return CrawledAudioStream(
                        resolvedStreamUrl = probe.finalUrl,
                        stationName = cleanStationName(probe.icyName, candidate),
                        genre = probe.icyGenre ?: "Online Radio",
                        bitrate = probe.icyBitrate ?: 128,
                        faviconUrl = null,
                        isFromWebpage = false,
                        contentType = probe.contentType
                    )
                }
            }
        } catch (_: Exception) {}
        return null
    }

    private fun extractUrlsFromPlaylist(body: String, baseUrl: String): List<String> {
        val urls = mutableListOf<String>()

        // 1. PLS format: File1=http://...
        val plsMatcher = Pattern.compile("File\\d+\\s*=\\s*(https?://\\S+)", Pattern.CASE_INSENSITIVE).matcher(body)
        while (plsMatcher.find()) {
            plsMatcher.group(1)?.let { urls.add(it.trim()) }
        }

        // 2. M3U format: lines starting with http://
        body.lines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
                urls.add(trimmed)
            }
        }

        // 3. XML ASX / XSPF format
        val xmlMatcher = Pattern.compile("<(?:ref\\s+href|location)>[\"']?(https?://[^\"'<>\\s]+)[\"']?</", Pattern.CASE_INSENSITIVE).matcher(body)
        while (xmlMatcher.find()) {
            xmlMatcher.group(1)?.let { urls.add(it.trim()) }
        }

        return urls.distinct()
    }

    private fun fetchHtmlContent(url: String): String? {
        return try {
            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()
            response.close()
            body
        } catch (_: Exception) {
            null
        }
    }

    private fun crawlHtmlForStreams(baseUrl: String, html: String): CrawledAudioStream? {
        val stationTitle = extractHtmlTitle(html) ?: "Online Radio"
        val faviconUrl = extractHtmlFavicon(html, baseUrl)

        val candidateUrls = mutableListOf<String>()

        // Pattern A: HTML5 audio/source elements
        val mediaPattern = Pattern.compile("<(?:audio|source|video)[^>]+src=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE)
        val mediaMatcher = mediaPattern.matcher(html)
        while (mediaMatcher.find()) {
            mediaMatcher.group(1)?.let { candidateUrls.add(resolveUrl(baseUrl, it)) }
        }

        // Pattern B: JavaScript streaming player configs (streamUrl, file, audio, etc.)
        val jsPatterns = listOf(
            Pattern.compile("[\"']?(?:stream(?:Url|URL|Source)?|audioUrl|stream)[\"']?\\s*[:=]\\s*[\"'](https?://[^\"'\\s]+)[\"']", Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\"']?(?:file|src|url|source)[\"']?\\s*[:=]\\s*[\"'](https?://[^\"'\\s]+\\.(?:mp3|aac|ogg|m3u8|m3u|pls)[^\"'\\s]*)[\"']", Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\"'](https?://[^\"'\\s]*(?:icecast|shoutcast|zeno\\.fm|radio\\.co|stream|listen|live)[^\"'\\s]*)[\"']", Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\"'](https?://[^\"'\\s]*:(?:8000|8002|8004|8006|8080|8443|8888)/[^\"'\\s]*)[\"']", Pattern.CASE_INSENSITIVE)
        )

        for (pattern in jsPatterns) {
            val matcher = pattern.matcher(html)
            while (matcher.find()) {
                matcher.group(1)?.let {
                    val resolved = resolveUrl(baseUrl, it)
                    if (isValidStreamCandidate(resolved)) {
                        candidateUrls.add(resolved)
                    }
                }
            }
        }

        // Pattern C: Embedded iFrame player src
        val iframePattern = Pattern.compile("<iframe[^>]+src=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE)
        val iframeMatcher = iframePattern.matcher(html)
        val iframeUrls = mutableListOf<String>()
        while (iframeMatcher.find()) {
            iframeMatcher.group(1)?.let { iframeUrls.add(resolveUrl(baseUrl, it)) }
        }

        // Pattern D: Anchor links to streams or playlists
        val linkPattern = Pattern.compile("<a[^>]+href=[\"']([^\"']+\\.(?:mp3|aac|m3u8|m3u|pls|ogg))[\"']", Pattern.CASE_INSENSITIVE)
        val linkMatcher = linkPattern.matcher(html)
        while (linkMatcher.find()) {
            linkMatcher.group(1)?.let { candidateUrls.add(resolveUrl(baseUrl, it)) }
        }

        // Prioritize distinct candidates
        val rankedCandidates = candidateUrls.distinct().filter {
            !it.endsWith(".js") && !it.endsWith(".css") && !it.endsWith(".png") && !it.endsWith(".jpg")
        }

        // Test each candidate
        for (candidate in rankedCandidates) {
            // If it's a playlist, parse it
            if (candidate.endsWith(".pls") || candidate.endsWith(".m3u")) {
                val plStream = tryParsePlaylist(candidate)
                if (plStream != null) {
                    return plStream.copy(
                        stationName = if (plStream.stationName == "Online Radio") stationTitle else plStream.stationName,
                        faviconUrl = faviconUrl,
                        isFromWebpage = true
                    )
                }
            }

            val probe = probeUrl(candidate)
            if (probe != null && isAudioResponse(probe.contentType, probe.icyName)) {
                return CrawledAudioStream(
                    resolvedStreamUrl = probe.finalUrl,
                    stationName = if (!probe.icyName.isNullOrBlank()) probe.icyName else stationTitle,
                    genre = probe.icyGenre ?: "Online Radio",
                    bitrate = probe.icyBitrate ?: 128,
                    faviconUrl = faviconUrl,
                    isFromWebpage = true,
                    contentType = probe.contentType
                )
            }
        }

        // If no candidate from main HTML, check embedded player iframes
        for (iframe in iframeUrls.take(3)) {
            val iframeHtml = fetchHtmlContent(iframe)
            if (!iframeHtml.isNullOrBlank()) {
                val iframeStream = crawlHtmlForStreams(iframe, iframeHtml)
                if (iframeStream != null) {
                    return iframeStream.copy(
                        stationName = if (iframeStream.stationName == "Online Radio") stationTitle else iframeStream.stationName,
                        faviconUrl = faviconUrl,
                        isFromWebpage = true
                    )
                }
            }
        }

        return null
    }

    private fun probeCommonMountpoints(baseUrl: String): CrawledAudioStream? {
        val httpUrl = baseUrl.toHttpUrlOrNull() ?: return null
        val origin = "${httpUrl.scheme}://${httpUrl.host}"
        val port = httpUrl.port

        val guesses = mutableListOf(
            "$origin:$port/stream",
            "$origin:$port/live",
            "$origin:$port/listen",
            "$origin:$port/;",
            "$origin/stream",
            "$origin/live",
            "$origin/radio.mp3",
            "$origin:8000/stream",
            "$origin:8000/live",
            "$origin:8000/;"
        ).distinct()

        for (mountpoint in guesses) {
            val probe = probeUrl(mountpoint)
            if (probe != null && isAudioResponse(probe.contentType, probe.icyName)) {
                return CrawledAudioStream(
                    resolvedStreamUrl = probe.finalUrl,
                    stationName = cleanStationName(probe.icyName, mountpoint),
                    genre = probe.icyGenre ?: "Online Radio",
                    bitrate = probe.icyBitrate ?: 128,
                    faviconUrl = null,
                    isFromWebpage = true,
                    contentType = probe.contentType
                )
            }
        }

        return null
    }

    private fun resolveUrl(base: String, relative: String): String {
        return try {
            val baseUri = URI(base)
            baseUri.resolve(relative.trim()).toString()
        } catch (_: Exception) {
            relative.trim()
        }
    }

    private fun isValidStreamCandidate(url: String): Boolean {
        val lower = url.lowercase()
        return lower.startsWith("http://") || lower.startsWith("https://")
    }

    private fun extractHtmlTitle(html: String): String? {
        val ogMatcher = Pattern.compile("<meta\\s+property=[\"']og:title[\"']\\s+content=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE).matcher(html)
        if (ogMatcher.find()) {
            return ogMatcher.group(1)?.trim()
        }
        val titleMatcher = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE).matcher(html)
        if (titleMatcher.find()) {
            val title = titleMatcher.group(1)?.replace(Regex("\\s+"), " ")?.trim()
            if (!title.isNullOrBlank()) {
                return title.split(" - ", " | ", " • ").firstOrNull()?.trim() ?: title
            }
        }
        return null
    }

    private fun extractHtmlFavicon(html: String, baseUrl: String): String? {
        val iconMatcher = Pattern.compile("<link\\s+[^>]*rel=[\"'](?:shortcut )?icon[\"'][^>]*href=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE).matcher(html)
        if (iconMatcher.find()) {
            iconMatcher.group(1)?.let { return resolveUrl(baseUrl, it) }
        }
        val ogImageMatcher = Pattern.compile("<meta\\s+property=[\"']og:image[\"']\\s+content=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE).matcher(html)
        if (ogImageMatcher.find()) {
            ogImageMatcher.group(1)?.let { return resolveUrl(baseUrl, it) }
        }
        return null
    }

    private fun cleanStationName(icyName: String?, fallbackUrl: String): String {
        if (!icyName.isNullOrBlank() && icyName != "Online Radio") {
            return icyName.trim()
        }
        val httpUrl = fallbackUrl.toHttpUrlOrNull()
        if (httpUrl != null) {
            val host = httpUrl.host.removePrefix("www.").removePrefix("stream.")
            return host.replaceFirstChar { it.uppercase() }
        }
        return "Online Radio"
    }
}
