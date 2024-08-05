package com.example.bitirmeproje.data


import com.example.bitirmeproje.domain.TranslateRequestBodyV2
import com.example.bitirmeproje.domain.TranslationResultV2
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface API {
    @POST("language/translate/v2")
    fun translateText(@Body requestBody: TranslateRequestBodyV2): Call<TranslationResultV2>
}






