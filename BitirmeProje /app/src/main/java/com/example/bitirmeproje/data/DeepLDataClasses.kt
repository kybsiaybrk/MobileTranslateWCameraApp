package com.example.bitirmeproje.data

data class DeepLResponse(
    val translations: List<Translation>
)

data class Translation(
    val detected_source_language: String,
    val text: String
)
