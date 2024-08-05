package com.example.bitirmeproje.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DeepLService(private val authKey: String) {
    private val api: DeepLApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api-free.deepl.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(DeepLApi::class.java)
    }

    fun translate(text: String, targetLang: String, callback: (String?) -> Unit) {
        val call = api.translate(authKey, text, targetLang)
        call.enqueue(object : Callback<DeepLResponse> {
            override fun onResponse(call: Call<DeepLResponse>, response: Response<DeepLResponse>) {
                if (response.isSuccessful) {
                    val translation = response.body()?.translations?.firstOrNull()?.text
                    callback(translation)
                } else {
                    callback(null)
                }
            }

            override fun onFailure(call: Call<DeepLResponse>, t: Throwable) {
                callback(null)
            }
        })
    }
}
