package com.example.bitirmeproje.data

data class TranslationResponse(
    val original: String,
    val translated: String,
    val reference: String,
    val bleu_score: Float,
    val meteor_score: Float,
    val chrf_score: Float,
    val ombc: Float

)
