package com.example.model

data class EqualizerBand(
    val bandIndex: Int,
    val centerFreqHz: Int,
    val currentLevelMb: Int,
    val minLevelMb: Int = -1500,
    val maxLevelMb: Int = 1500
) {
    val frequencyLabel: String
        get() = when {
            centerFreqHz >= 1_000_000 -> "${centerFreqHz / 1_000_000} kHz"
            centerFreqHz >= 1_000 -> "${centerFreqHz / 1_000} Hz"
            else -> "$centerFreqHz mHz"
        }

    val displayFreq: String
        get() = when {
            centerFreqHz in 50_000..70_000 -> "60 Hz"
            centerFreqHz in 200_000..260_000 -> "230 Hz"
            centerFreqHz in 800_000..1_000_000 -> "910 Hz"
            centerFreqHz in 3_000_000..4_000_000 -> "3.6 kHz"
            centerFreqHz in 12_000_000..16_000_000 -> "14 kHz"
            centerFreqHz >= 1_000_000 -> "${centerFreqHz / 1_000_000} kHz"
            centerFreqHz >= 1_000 -> "${centerFreqHz / 1_000} Hz"
            else -> "$centerFreqHz Hz"
        }
}

enum class EqPresetType(val displayName: String, val gainsDb: List<Float>) {
    FLAT("Cosmic Flat", listOf(0f, 0f, 0f, 0f, 0f)),
    BASS_BOOST("Deep Space Bass", listOf(7f, 5f, 1f, -1f, -2f)),
    TREBLE_BOOST("Starlight Treble", listOf(-2f, 0f, 2f, 6f, 8f)),
    NEBULA_CHILL("Nebula Chill", listOf(4f, 2f, -1f, 3f, 2f)),
    VOCAL_ORBIT("Vocal Orbit", listOf(-1f, 1f, 6f, 4f, 0f)),
    ELECTRONIC("Cosmic Synth", listOf(5f, 3f, 0f, 4f, 6f)),
    CUSTOM("Custom Calibration", listOf(0f, 0f, 0f, 0f, 0f))
}
