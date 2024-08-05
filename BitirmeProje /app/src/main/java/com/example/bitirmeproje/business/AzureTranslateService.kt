package com.example.bitirmeproje.business

import com.example.bitirmeproje.data.AzureTranslateApi
import com.example.bitirmeproje.data.AzureTranslationResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import org.json.JSONArray
import org.json.JSONObject

class AzureTranslateService(private val subscriptionKey: String, private val endpoint: String, private val region: String) {

    private val api: AzureTranslateApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(endpoint)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(AzureTranslateApi::class.java)
    }

    fun translate(text: String, targetLang: String, callback: (String?) -> Unit) {
        val requestBody = JSONArray().put(JSONObject().put("Text", text)).toString()
        val body = requestBody.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val call = api.translate("3.0", targetLang, subscriptionKey, region, body)

        call.enqueue(object : Callback<List<AzureTranslationResponse>> {
            override fun onResponse(call: Call<List<AzureTranslationResponse>>, response: Response<List<AzureTranslationResponse>>) {
                if (response.isSuccessful) {
                    val translation = response.body()?.firstOrNull()?.translations?.firstOrNull()?.text
                    callback(translation)
                } else {
                    callback(null)
                }
            }

            override fun onFailure(call: Call<List<AzureTranslationResponse>>, t: Throwable) {
                callback(null)
            }
        })
    }
}
