package com.example.bitirmeproje.data

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface TranslationService {
    @POST("/translate")
    fun translate(@Body request: TranslationRequest): Call<TranslationResponse>
}
