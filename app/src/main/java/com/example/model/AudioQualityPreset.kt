package com.example.model

enum class AudioQualityPreset(
    val title: String,
    val subtitle: String,
    val dataRateEstimate: String,
    val targetBitrateKbps: Int,
    val bufferMultiplier: Float
) {
    HIGH(
        title = "High Fidelity",
        subtitle = "Highest acoustic detail, uncompressed soundstage",
        dataRateEstimate = "~140 MB / hr",
        targetBitrateKbps = 320,
        bufferMultiplier = 1.5f
    ),
    BALANCED(
        title = "Balanced Cosmic",
        subtitle = "Optimal balance of quality and streaming speed",
        dataRateEstimate = "~57 MB / hr",
        targetBitrateKbps = 128,
        bufferMultiplier = 1.0f
    ),
    DATA_SAVER(
        title = "Data Saver",
        subtitle = "Eco-bandwidth mode for mobile data & weak signals",
        dataRateEstimate = "~28 MB / hr",
        targetBitrateKbps = 64,
        bufferMultiplier = 0.6f
    )
}
