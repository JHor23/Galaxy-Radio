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
}
