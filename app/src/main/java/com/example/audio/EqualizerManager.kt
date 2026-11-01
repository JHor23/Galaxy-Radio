package com.example.audio

import android.content.Context
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.util.Log
import com.example.model.EqualizerBand
import com.example.model.EqPresetType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class EqualizerUiState(
    val isEnabled: Boolean = true,
    val activePreset: EqPresetType = EqPresetType.FLAT,
    val bands: List<EqualizerBand> = emptyList(),
    val bassBoostSupported: Boolean = false,
    val bassBoostStrength: Int = 0, // 0..1000
    val virtualizerSupported: Boolean = false,
    val virtualizerStrength: Int = 0 // 0..1000
)

class EqualizerManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("galaxy_radio_eq", Context.MODE_PRIVATE)

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var currentSessionId: Int = 0

    private val _state = MutableStateFlow(EqualizerUiState())
    val state: StateFlow<EqualizerUiState> = _state.asStateFlow()

    init {
        // Load default mock/preset bands before any audio session is created
        initializeDefaultBands()
    }

    private fun initializeDefaultBands() {
        val defaultFrequencies = listOf(60_000, 230_000, 910_000, 3_600_000, 14_000_000)
        val bands = defaultFrequencies.mapIndexed { index, freq ->
            val savedLevel = prefs.getInt("eq_band_$index", 0)
            EqualizerBand(
                bandIndex = index,
                centerFreqHz = freq,
                currentLevelMb = savedLevel,
                minLevelMb = -1500,
                maxLevelMb = 1500
            )
        }
        val savedPresetName = prefs.getString("active_preset", EqPresetType.FLAT.name)
        val preset = try {
            EqPresetType.valueOf(savedPresetName ?: EqPresetType.FLAT.name)
        } catch (_: Exception) {
            EqPresetType.FLAT
        }
        val bassStrength = prefs.getInt("bass_strength", 0)
        val virtStrength = prefs.getInt("virt_strength", 0)

        _state.update {
            it.copy(
                bands = bands,
                activePreset = preset,
                bassBoostStrength = bassStrength,
                virtualizerStrength = virtStrength
            )
        }
    }

    fun attachAudioSession(sessionId: Int) {
        if (sessionId <= 0 || sessionId == currentSessionId) return
        releaseAudioFx()
        currentSessionId = sessionId

        try {
            val eq = Equalizer(0, sessionId).apply {
                enabled = _state.value.isEnabled
            }
            equalizer = eq

            val numBands = eq.numberOfBands.toInt()
            val minLevel = eq.bandLevelRange[0].toInt()
            val maxLevel = eq.bandLevelRange[1].toInt()

            val bands = (0 until numBands).map { bandIndex ->
                val freq = eq.getCenterFreq(bandIndex.toShort())
                val savedLevel = prefs.getInt("eq_band_$bandIndex", 0)
                    .coerceIn(minLevel, maxLevel)
                eq.setBandLevel(bandIndex.toShort(), savedLevel.toShort())

                EqualizerBand(
                    bandIndex = bandIndex,
                    centerFreqHz = freq,
                    currentLevelMb = savedLevel,
                    minLevelMb = minLevel,
                    maxLevelMb = maxLevel
                )
            }

            // BassBoost
            val bb = try {
                BassBoost(0, sessionId).apply {
                    enabled = _state.value.isEnabled
                    if (strengthSupported) {
                        setStrength(_state.value.bassBoostStrength.toShort())
                    }
                }
            } catch (e: Exception) {
                null
            }
            bassBoost = bb

            // Virtualizer
            val virt = try {
                Virtualizer(0, sessionId).apply {
                    enabled = _state.value.isEnabled
                    if (strengthSupported) {
                        setStrength(_state.value.virtualizerStrength.toShort())
                    }
                }
            } catch (e: Exception) {
                null
            }
            virtualizer = virt

            _state.update {
                it.copy(
                    bands = bands,
                    bassBoostSupported = bb?.strengthSupported == true,
                    virtualizerSupported = virt?.strengthSupported == true
                )
            }

            // Re-apply active preset
            applyPresetInternal(_state.value.activePreset)
        } catch (e: Exception) {
            Log.e("EqualizerManager", "Failed to attach audio session $sessionId", e)
        }
    }

    fun toggleEnabled(enabled: Boolean) {
        equalizer?.enabled = enabled
        bassBoost?.enabled = enabled
        virtualizer?.enabled = enabled
        _state.update { it.copy(isEnabled = enabled) }
        prefs.edit().putBoolean("eq_enabled", enabled).apply()
    }

    fun applyPreset(preset: EqPresetType) {
        applyPresetInternal(preset)
        prefs.edit().putString("active_preset", preset.name).apply()
    }

    private fun applyPresetInternal(preset: EqPresetType) {
        if (preset == EqPresetType.CUSTOM) {
            _state.update { it.copy(activePreset = preset) }
            return
        }

        val eq = equalizer
        val currentBands = _state.value.bands
        val updatedBands = currentBands.mapIndexed { index, band ->
            val gainDb = preset.gainsDb.getOrElse(index) { 0f }
            val levelMb = (gainDb * 100).toInt().coerceIn(band.minLevelMb, band.maxLevelMb)
            try {
                eq?.setBandLevel(index.toShort(), levelMb.toShort())
            } catch (_: Exception) {}
            prefs.edit().putInt("eq_band_$index", levelMb).apply()
            band.copy(currentLevelMb = levelMb)
        }

        _state.update {
            it.copy(
                activePreset = preset,
                bands = updatedBands
            )
        }
    }

    fun setBandLevel(bandIndex: Int, levelMb: Int) {
        val eq = equalizer
        val currentBands = _state.value.bands.toMutableList()
        if (bandIndex in currentBands.indices) {
            val band = currentBands[bandIndex]
            val clamped = levelMb.coerceIn(band.minLevelMb, band.maxLevelMb)
            try {
                eq?.setBandLevel(bandIndex.toShort(), clamped.toShort())
            } catch (_: Exception) {}
            currentBands[bandIndex] = band.copy(currentLevelMb = clamped)
            prefs.edit().putInt("eq_band_$bandIndex", clamped).apply()

            _state.update {
                it.copy(
                    bands = currentBands,
                    activePreset = EqPresetType.CUSTOM
                )
            }
            prefs.edit().putString("active_preset", EqPresetType.CUSTOM.name).apply()
        }
    }

    fun setBassBoost(strength: Int) {
        val clamped = strength.coerceIn(0, 1000)
        try {
            if (bassBoost?.strengthSupported == true) {
                bassBoost?.setStrength(clamped.toShort())
            }
        } catch (_: Exception) {}
        _state.update { it.copy(bassBoostStrength = clamped) }
        prefs.edit().putInt("bass_strength", clamped).apply()
    }

    fun setVirtualizer(strength: Int) {
        val clamped = strength.coerceIn(0, 1000)
        try {
            if (virtualizer?.strengthSupported == true) {
                virtualizer?.setStrength(clamped.toShort())
            }
        } catch (_: Exception) {}
        _state.update { it.copy(virtualizerStrength = clamped) }
        prefs.edit().putInt("virt_strength", clamped).apply()
    }

    fun releaseAudioFx() {
        try { equalizer?.release() } catch (_: Exception) {}
        try { bassBoost?.release() } catch (_: Exception) {}
        try { virtualizer?.release() } catch (_: Exception) {}
        equalizer = null
        bassBoost = null
        virtualizer = null
        currentSessionId = 0
    }
}
