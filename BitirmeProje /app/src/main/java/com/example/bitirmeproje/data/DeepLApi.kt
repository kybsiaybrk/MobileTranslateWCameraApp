package com.example.bitirmeproje.data

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface DeepLApi {
    @FormUrlEncoded
    @POST("/v2/translate")
    fun translate(
        @Field("auth_key") authKey: String,
        @Field("text") text: String,
        @Field("target_lang") targetLang: String
    ): Call<DeepLResponse>
}
