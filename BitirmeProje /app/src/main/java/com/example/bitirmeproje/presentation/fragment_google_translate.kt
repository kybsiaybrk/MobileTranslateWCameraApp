package com.example.bitirmeproje.presentation

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.bitirmeproje.R
import com.example.bitirmeproje.data.API
import com.example.bitirmeproje.data.ServiceBuilder
import com.example.bitirmeproje.data.TranslationRequest as DataTranslationRequest
import com.example.bitirmeproje.data.TranslationResponse
import com.example.bitirmeproje.data.TranslationService
import com.example.bitirmeproje.domain.TranslateRequestBodyV2
import com.example.bitirmeproje.domain.TranslationResultV2
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.android.synthetic.main.fragment_google_translate.txttrnslttxt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class GoogleTranslateFragment : Fragment() {

    private lateinit var service: TranslationService
    private val apiService = ServiceBuilder.buildService(API::class.java)
    private lateinit var resultView: TextView
    private lateinit var textView3: TextView
    private lateinit var textviewDogruCeviri: TextView
    private lateinit var txtskorlar: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_google_translate, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(130, TimeUnit.SECONDS)
            .writeTimeout(130, TimeUnit.SECONDS)
            .readTimeout(130, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.1.100:5001")  // Flask sunucu IP adresi
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        service = retrofit.create(TranslationService::class.java)

        textviewDogruCeviri = view.findViewById(R.id.txtDogrucevirisonuc)
        resultView = view.findViewById(R.id.txtTranslated)
        txtskorlar = view.findViewById(R.id.txtskorlar)

        val textViewtxt = view.findViewById<TextView>(R.id.txttrnslttxt)

        // Fragment'a gelen veriyi al
        val receivedText = arguments?.getString("capturedText") ?: ""
        textViewtxt.text = receivedText

        // İlk başlangıçta metni çevir
        translateText(receivedText)
        btnGeminiAPITranslate(view)



        fun delayedTranslateText(receivedText: String) {
            GlobalScope.launch(Dispatchers.Main) {
                delay(5000) // 2000 milisaniye = 2 saniye
                translateText2(receivedText)
            }
        }
        // pythonrestapi
        delayedTranslateText(receivedText)
    }

    private fun translateText(textToTranslate: String) {
        val requestBody = TranslateRequestBodyV2(
            q = textToTranslate,
            target = "tr"
        )
        apiService.translateText(requestBody).enqueue(object : Callback<TranslationResultV2> {

            override fun onResponse(
                call: Call<TranslationResultV2>,
                response: Response<TranslationResultV2>
            ) {
                if (response.isSuccessful) {
                    response.body()?.data?.translations?.firstOrNull()?.let {
                        resultView.text = it.translatedText
                    }
                } else {
                    val errorResponse = response.errorBody()?.string()
                    resultView.text = "Translation failed: $errorResponse"
                    Log.e("Translation Error", "Error response: $errorResponse")
                }
            }

            override fun onFailure(call: Call<TranslationResultV2>, t: Throwable) {
                Log.e("API Error", "Failed to fetch translation", t)
                resultView.text = "Error: ${t.message}"
            }
        })
    }

    fun btnGeminiAPITranslate(view: View) {
        val promptText = "bu cümlenin daha doğru Çevirisi nedir, türkçe yaz, eğer tek kelimeyse doğrudan aynı bırak, kelime: ${txttrnslttxt.text}"
        val generativeModel = GenerativeModel(
            modelName = "gemini-pro",
            apiKey = "AIzaSyBLFBgmjvkyIibmxYwefR6c7r-K3Kh5sfc"
        )

        MainScope().launch {
            try {
                val response = generativeModel.generateContent(prompt = promptText)
                if (response.text?.isEmpty() == true) {
                    Log.e("GoogleTranslateFragment", "Response is empty.")
                } else {
                    Log.i("GoogleTranslateFragment", "Response received: ${response.text}")
                    updateText(response.text ?: "Empty or null text", isCorrectTranslation = true)
                }
            } catch (e: Exception) {
                Log.e("GoogleTranslateFragment", "Failed to generate content: ${e.message}")
            }
        }
    }

    private fun updateText(newText: String, isCorrectTranslation: Boolean) {
        activity?.runOnUiThread {
            if (isCorrectTranslation) {
                textviewDogruCeviri.text = newText
            } else {
                textView3.text = newText
            }
        }
    }

    private fun translateText2(textToTranslate: String) {
        val request = DataTranslationRequest(
            sentence = textToTranslate,
            reference = textviewDogruCeviri.text.toString() // Burada referans çeviriyi dinamik olarak belirleyebilirsiniz
        )

        val call = service.translate(request)
        call.enqueue(object : Callback<TranslationResponse> {
            override fun onResponse(call: Call<TranslationResponse>, response: Response<TranslationResponse>) {
                if (response.isSuccessful) {
                    val translationResponse = response.body()
                    translationResponse?.let {
                        txtskorlar.text = """
                            BLEU Score: ${it.bleu_score}
                            METEOR Score: ${it.meteor_score}
                            chrF Score: ${it.chrf_score}
                            OMBC Score: ${it.ombc}
                        """.trimIndent()
                        Log.d("Translation", "Original: ${it.original}")
                        Log.d("Translation", "Translated: ${it.translated}")
                        Log.d("Translation", "Reference: ${it.reference}")
                        Log.d("Translation", "BLEU Score: ${it.bleu_score}")
                        Log.d("Translation", "METEOR Score: ${it.meteor_score}")
                        Log.d("Translation", "chrF Score: ${it.chrf_score}")
                    }
                } else {
                    Log.e("Translation Error", "Request failed with status: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<TranslationResponse>, t: Throwable) {
                Log.e("Translation Error", "Failed to fetch translation", t)
            }
        })
    }
}
