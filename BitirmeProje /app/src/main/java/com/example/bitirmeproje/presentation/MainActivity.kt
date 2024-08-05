package com.example.bitirmeproje.presentation

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bitirmeproje.R

class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val backgroundColor = getColor(R.color.arkaplan)
        val statusbarcolor = getColor(R.color.button)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = statusbarcolor
            window.navigationBarColor = backgroundColor
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        val btnKameraSayfa = findViewById<Button>(R.id.btnOkutCevir)
        val btnDilSec = findViewById<Button>(R.id.btnDilSec)
        val btnAyarlar = findViewById<Button>(R.id.btnAyarlar)
        val btnRealTime = findViewById<Button>(R.id.btnGercekZamanli)

        btnKameraSayfa.setOnClickListener{
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)

        }

        btnRealTime.setOnClickListener{
            val intent = Intent(this, RealTimeTranslationActivity::class.java)
            startActivity(intent)

        }



        btnAyarlar.setOnClickListener{
            val intent = Intent(this, AiActivity::class.java)
            startActivity(intent)
        }





    }
}