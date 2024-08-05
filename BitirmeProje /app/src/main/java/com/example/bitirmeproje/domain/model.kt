package com.example.bitirmeproje.domain


data class TranslateRequestBodyV3(
    val contents: List<String>,
    val targetLanguageCode: String,
    val sourceLanguageCode: String? = null
)

data class TranslationResultV3(
    val data: TranslationDataV3
)

data class TranslationDataV3(
    val translations: List<TranslationTextV3>
)

data class TranslationTextV3(
    val translatedText: String
)

data class TranslateRequestBodyV2(
    val q: String,  // Çevrilecek metin veya metinler
    val target: String,  // Hedef dil kodu
    val source: String? = null,  // Kaynak dil kodu, opsiyonel
    val format: String = "text"  // Metin formatı, "text" veya "html"
)
data class TranslationResultV2(
    val data: TranslationsListV2
)

data class TranslationsListV2(
    val translations: List<TranslatedTextV2>
)

data class TranslatedTextV2(
    val translatedText: String,
    val detectedSourceLanguage: String? = null  // Otomatik algılanan kaynak dil, opsiyonel
)


