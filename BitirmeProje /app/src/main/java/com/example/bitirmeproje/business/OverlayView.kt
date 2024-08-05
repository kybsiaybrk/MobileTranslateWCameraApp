package com.example.bitirmeproje.business

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class OverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = ContextCompat.getColor(context, android.R.color.black)
        alpha = 150 // Karanlıklaştırma seviyesi
    }

    private val screenWidth = resources.displayMetrics.widthPixels
    private val screenHeight = resources.displayMetrics.heightPixels

    // Ekranın ortasında 16:9 oranında bir dikdörtgen
    private val rectWidth = screenWidth * 0.8 // Ekran genişliğinin %80'i
    private val rectHeight = rectWidth * 9 / 16 // 16:9 oranında yükseklik
    private val left = (screenWidth - rectWidth) / 2
    private val top = (screenHeight - rectHeight) / 2
    private val right = left + rectWidth
    private val bottom = top + rectHeight

    private val rect = Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())

    override fun onDraw(canvas: Canvas) {
        if (canvas != null) {
            super.onDraw(canvas)
        }
        canvas?.let {
            // Dikdörtgenin dışındaki alanı karart
            it.drawRect(0f, 0f, width.toFloat(), rect.top.toFloat(), paint)
            it.drawRect(0f, rect.bottom.toFloat(), width.toFloat(), height.toFloat(), paint)
            it.drawRect(0f, rect.top.toFloat(), rect.left.toFloat(), rect.bottom.toFloat(), paint)
            it.drawRect(rect.right.toFloat(), rect.top.toFloat(), width.toFloat(), rect.bottom.toFloat(), paint)
        }
    }

    fun getRectangle(): Rect {
        return rect
    }
}
