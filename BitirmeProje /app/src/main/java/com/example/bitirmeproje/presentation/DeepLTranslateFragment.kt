package com.example.bitirmeproje.presentation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.bitirmeproje.R
import com.example.bitirmeproje.data.DeepLService
import com.example.bitirmeproje.data.TranslationRequest
import com.example.bitirmeproje.data.TranslationResponse
import com.example.bitirmeproje.data.TranslationService
import com.google.ai.client.generativeai.GenerativeModel
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

class DeepLTranslateFragment : Fragment() {

    private lateinit var flaskService: TranslationService
    private lateinit var deepLService: DeepLService
    private lateinit var resultView: TextView
    private lateinit var textViewDogruCeviri: TextView
    private lateinit var textViewDogruCeviriSonuc: TextView
    private lateinit var textviewceviriskorlari: TextView
    private lateinit var txtskorlar: TextView
    private lateinit var txttrnslttxt: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_deep_l_translate, container, false)
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

        // DeepL Service
        val deepLAuthKey = "8eab2b27-f8e0-429f-8264-95dc6df95df8:fx"
        deepLService = DeepLService(deepLAuthKey)

        textViewDogruCeviri = view.findViewById(R.id.textViewDogruCeviri)
        textViewDogruCeviriSonuc = view.findViewById(R.id.txtDogrucevirisonuc)
        resultView = view.findViewById(R.id.txtTranslated)
        txtskorlar = view.findViewById(R.id.txtskorlar)
        txttrnslttxt = view.findViewById(R.id.txttrnslttxt)
        textviewceviriskorlari = view.findViewById(R.id.textviewceviriskorlari)

        val receivedText = arguments?.getString("capturedText") ?: ""
        txttrnslttxt.text = receivedText

        // İlk başlangıçta metni çevir
        translateWithFlask(receivedText)
        translateWithDeepL(receivedText)
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

    private fun translateWithFlask(textToTranslate: String) {
        val request = TranslationRequest(
            sentence = textToTranslate,
            reference = "" // Referansı uygun şekilde ayarlayın
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

    private fun translateWithDeepL(textToTranslate: String) {
        deepLService.translate(textToTranslate, "TR") { translation ->
            if (translation != null) {
                resultView.text = translation
            } else {
            }
        }
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
            apiKey = "AIzaSyBLFBgmjvkyIibmxYwefR6c7r-K3Kh5sfc"  // Google Gemini API anahtarınızı buraya ekleyin
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
                textViewDogruCeviriSonuc.text = newText
            } else {
                textViewDogruCeviriSonuc.text = newText
            }
        }
    }
}
