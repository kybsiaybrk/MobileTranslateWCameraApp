package com.example.bitirmeproje.business

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class OverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val left = (width * 0.15).toFloat()
        val top = (height * 0.3).toFloat()
        val right = (width * 0.85).toFloat()
        val bottom = (height * 0.46).toFloat()
        canvas.drawRect(left, top, right, bottom, paint)
    }
}
