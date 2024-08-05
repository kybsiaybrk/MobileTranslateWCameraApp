package com.example.bitirmeproje.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.bitirmeproje.R
import kotlinx.android.synthetic.main.activity_ai.*

class AiActivity : AppCompatActivity() {

    private lateinit var googleTranslateFragment: GoogleTranslateFragment
    private lateinit var azureTranslateFragment: AzureTranslateFragment
    private lateinit var DeepLTranslateFragment: DeepLTranslateFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai)

        val receivedText = intent.getStringExtra("capturedText") ?: ""

        // Fragment'ları oluştur
        googleTranslateFragment = GoogleTranslateFragment().apply {
            arguments = Bundle().apply {
                putString("capturedText", receivedText)
            }
        }
        DeepLTranslateFragment = DeepLTranslateFragment().apply {
            arguments = Bundle().apply {
                putString("capturedText", receivedText)
            }
        }

        azureTranslateFragment = AzureTranslateFragment().apply {
            arguments = Bundle().apply {
                putString("capturedText", receivedText)
            }
        }
        //yandexTranslateFragment = YandexTranslateFragment()
        //bingTranslateFragment = BingTranslateFragment()

        // Fragment'ları ekle ve göster
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, googleTranslateFragment, "GoogleTranslateFragment")
            .add(R.id.fragment_container, azureTranslateFragment, "AzureTranslateFragment")
            .add(R.id.fragment_container, DeepLTranslateFragment, "DeepLTranslateFragment")
            .hide(azureTranslateFragment)
            .hide(DeepLTranslateFragment)
            .commit()

        btnGoogleTranslate.setOnClickListener {
            showFragment(googleTranslateFragment)
        }

        btnAzureTranslate.setOnClickListener {
            showFragment(azureTranslateFragment)
        }


        btnDeepLTranslate.setOnClickListener {
            showFragment(DeepLTranslateFragment)
        }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .hide(googleTranslateFragment)
            //.hide(yandexTranslateFragment)
            .hide(azureTranslateFragment)
            .hide(DeepLTranslateFragment)
            .show(fragment)
            .commit()
    }
}
