package com.example.bitirmeproje.data

import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Header

interface AzureTranslateApi {

    @Headers("Content-Type: application/json")
    @POST("/translate")
    fun translate(
        @Query("api-version") apiVersion: String,
        @Query("to") targetLang: String,
        @Header("Ocp-Apim-Subscription-Key") subscriptionKey: String,
        @Header("Ocp-Apim-Subscription-Region") region: String,
        @Body requestBody: RequestBody
    ): Call<List<AzureTranslationResponse>>

}

data class AzureTranslationResponse(
    val translations: List<AzureTranslation>
)

data class AzureTranslation(
    val text: String
)
