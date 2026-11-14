package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.IcyMetadataReader
import com.example.model.AudioQualityPreset
import com.example.model.EqPresetType
import com.example.model.RadioStation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Galaxy Radio", appName)
  }

  @Test
  fun `parse artist and title from icy metadata`() {
    val raw = "Solar Fields - Starlight Echoes"
    val (artist, title) = IcyMetadataReader.parseArtistAndTitle(raw)
    assertEquals("Solar Fields", artist)
    assertEquals("Starlight Echoes", title)
  }

  @Test
  fun `verify audio quality presets`() {
    assertEquals(3, AudioQualityPreset.values().size)
    assertEquals(320, AudioQualityPreset.HIGH.targetBitrateKbps)
    assertEquals(64, AudioQualityPreset.DATA_SAVER.targetBitrateKbps)
  }

  @Test
  fun `verify equalizer presets`() {
    assertTrue(EqPresetType.values().isNotEmpty())
    assertEquals("Cosmic Flat", EqPresetType.FLAT.displayName)
  }

  @Test
  fun `verify sleep timer formatting`() {
    val state = com.example.sleep.SleepTimerState(
      isActive = true,
      selectedMinutes = 30,
      remainingSeconds = 125L
    )
    assertEquals("02:05", state.formattedRemaining)
    assertEquals("3m", state.displayShort)
  }

  @Test
  fun `verify playlist url extraction`() {
    val pls = """
      [playlist]
      NumberOfEntries=1
      File1=http://stream.somafm.com:80/spacestation-128-mp3
      Title1=Space Station Soma
      Length1=-1
    """.trimIndent()
    val matcher = java.util.regex.Pattern.compile("File\\d+\\s*=\\s*(https?://\\S+)", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(pls)
    assertTrue(matcher.find())
    assertEquals("http://stream.somafm.com:80/spacestation-128-mp3", matcher.group(1))
  }

  @Test
  fun `verify wix json audioStreamLink extraction for happyradio`() {
    val sampleWixHtml = """
      {"audioStreamLink":"https:\/\/stream.zeno.fm\/nkeaps48xg0uv","station":"Happy Radio"}
    """.trimIndent()
    val unescaped = sampleWixHtml.replace("\\/", "/").replace("\\\"", "\"")
    val matcher = java.util.regex.Pattern.compile(
      "[\"'](?:audioStreamLink|audioUrl|streamUrl|streamURL|liveStream)[\"']\\s*:\\s*[\"'](https?://[^\"'\\s]+)[\"']",
      java.util.regex.Pattern.CASE_INSENSITIVE
    ).matcher(unescaped)
    assertTrue(matcher.find())
    assertEquals("https://stream.zeno.fm/nkeaps48xg0uv", matcher.group(1))
  }

  @Test
  fun `verify song history entity creation and fields`() {
    val history = com.example.data.local.SongHistoryEntity(
      id = 1L,
      stationId = "curated_space_station",
      stationName = "Space Station Soma",
      streamUrl = "http://stream.somafm.com:80/spacestation-128-mp3",
      trackTitle = "Solar Fields - Starlight",
      artist = "Solar Fields",
      playedAtTimestamp = 1700000000000L
    )
    assertEquals("Space Station Soma", history.stationName)
    assertEquals("Solar Fields - Starlight", history.trackTitle)
    assertEquals("Solar Fields", history.artist)
    assertEquals(1700000000000L, history.playedAtTimestamp)
  }

  @Test
  fun `verify about menu and creator strings`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    assertEquals("About", context.getString(R.string.menu_about))
    assertEquals("JHor", context.getString(R.string.about_creator_handle))
    assertEquals("ABOUT ME", context.getString(R.string.about_me_heading))
    assertEquals("https://ko-fi.com/jhor23", com.example.ui.components.CREATOR_KOFI_URL)
    assertEquals("https://github.com/JHor23/Galaxy-Radio", com.example.ui.components.CREATOR_SOURCE_URL)
    assertEquals("https://github.com/JHor23", com.example.ui.components.CREATOR_PROJECTS_URL)
  }
}
