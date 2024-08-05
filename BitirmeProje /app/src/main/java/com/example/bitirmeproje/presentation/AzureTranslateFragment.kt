package com.example.bitirmeproje.presentation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.bitirmeproje.R
import com.example.bitirmeproje.business.AzureTranslateService
import com.example.bitirmeproje.data.TranslationRequest
import com.example.bitirmeproje.data.TranslationResponse
import com.example.bitirmeproje.data.TranslationService
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AzureTranslateFragment : Fragment() {

    private lateinit var flaskService: TranslationService
    private lateinit var azureService: AzureTranslateService
    private lateinit var resultView: TextView
    private lateinit var textViewDogruCeviri: TextView
    private lateinit var textviewceviriskorlari: TextView
    private lateinit var textViewDogruCeviriSonuc: TextView
    private lateinit var txtSkorlar: TextView


    private lateinit var txttrnslttxt: TextView
    private val mainScope = MainScope()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_azure_translate, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(130, TimeUnit.SECONDS)
            .writeTimeout(130, TimeUnit.SECONDS)
            .readTimeout(130, TimeUnit.SECONDS)
            .build()

        // Flask Retrofit
        val flaskRetrofit = Retrofit.Builder()
            .baseUrl("http://192.168.1.100:5001")  // Flask sunucu IP adresi
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        flaskService = flaskRetrofit.create(TranslationService::class.java)

        // Azure Service
        val subscriptionKey = "8a924b90a9b54d39bda66f0ad95d299b"  // Azure abonelik anahtarınızı buraya ekleyin
        val endpoint = "https://api.cognitive.microsofttranslator.com/"  // Azure endpoint URL'inizi buraya ekleyin
        val region = "eastus"  // Azure bölgenizi buraya ekleyin
        azureService = AzureTranslateService(subscriptionKey, endpoint, region)

        textViewDogruCeviri = view.findViewById(R.id.textViewDogruCeviri)
        resultView = view.findViewById(R.id.txtTranslated)
        textviewceviriskorlari = view.findViewById(R.id.textviewceviriskorlari)
        txttrnslttxt = view.findViewById(R.id.txttrnslttxt)
        textViewDogruCeviriSonuc = view.findViewById(R.id.txtDogrucevirisonuc)
        txtSkorlar = view.findViewById(R.id.txtskorlar)



        val receivedText = arguments?.getString("capturedText") ?: ""
        txttrnslttxt.text = receivedText

        // İlk başlangıçta metni çevir
        translateWithAzure(receivedText)
        translateWithFlask(receivedText)
        btnGeminiAPITranslate(view)

        fun delayedTranslateText(receivedText: String) {
            GlobalScope.launch(Dispatchers.Main) {
                delay(5000) // 2000 milisaniye = 2 saniye
                translateWithFlaskDelayed(receivedText)
            }
        }
        // pythonrestapi
        delayedTranslateText(receivedText)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel() // Coroutine scope'u iptal edin
    }

    private fun translateWithAzure(textToTranslate: String) {
        azureService.translate(textToTranslate, "tr") { translation ->
            if (translation != null) {
                resultView.text = translation
            }
        }
    }

    private fun translateWithFlask(textToTranslate: String) {
        val request = TranslationRequest(
            sentence = textToTranslate,
            reference = ""
        )

        flaskService.translate(request).enqueue(object : Callback<TranslationResponse> {
            override fun onResponse(call: Call<TranslationResponse>, response: Response<TranslationResponse>) {
                if (response.isSuccessful) {
                }
            }

            override fun onFailure(call: Call<TranslationResponse>, t: Throwable) {

            }
        })
    }

    private fun translateWithFlaskDelayed(textToTranslate: String) {
        val request = TranslationRequest(
            sentence = textToTranslate,
            reference = textViewDogruCeviriSonuc.text.toString() // Referans çeviriyi dinamik olarak belirleyin
        )

        flaskService.translate(request).enqueue(object : Callback<TranslationResponse> {
            override fun onResponse(call: Call<TranslationResponse>, response: Response<TranslationResponse>) {
                if (response.isSuccessful) {
                    val translationResponse = response.body()
                    translationResponse?.let {
                        txtSkorlar.text = """
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

    fun btnGeminiAPITranslate(view: View) {
        val promptText = "bu cümlenin daha doğru Çevirisi nedir, türkçe yaz, eğer tek kelimeyse doğrudan aynı bırak, kelime: ${txttrnslttxt.text}"
        val generativeModel = GenerativeModel(
            modelName = "gemini-pro",
            apiKey = "AIzaSyBLFBgmjvkyIibmxYwefR6c7r-K3Kh5sfc"
        )

        mainScope.launch {
            try {
                val response = generativeModel.generateContent(prompt = promptText)
                if (response.text?.isEmpty() == true) {
                    Log.e("AzureTranslateFragment", "Response is empty.")
                } else {
                    Log.i("AzureTranslateFragment", "Response received: ${response.text}")
                    updateText(response.text ?: "Empty or null text", isCorrectTranslation = true)
                }
            } catch (e: Exception) {
                Log.e("AzureTranslateFragment", "Failed to generate content: ${e.message}")
            }
        }
    }

    private fun updateText(newText: String, isCorrectTranslation: Boolean) {
        activity?.runOnUiThread {
            if (isCorrectTranslation) {
                textViewDogruCeviriSonuc.text = newText
            } else {
                //resultView.text = newText
            }
        }
    }
}
