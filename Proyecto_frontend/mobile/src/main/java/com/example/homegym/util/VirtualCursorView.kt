package com.example.homegym.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class VirtualCursorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = Color.parseColor("#E67447") // Color principal de la paleta
        style = Paint.Style.FILL
        isAntiAlias = true
        setShadowLayer(10f, 0f, 0f, Color.BLACK)
    }

    private val borderPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    private var cursorX = 100f
    private var cursorY = 100f
    private val radius = 20f

    init {
        // Asegurarse de que el cursor esté por encima de todo
        translationZ = 100f
    }

    fun updatePosition(x: Float, y: Float) {
        cursorX = x
        cursorY = y
        postInvalidateOnAnimation()
    }

    fun getCursorX() = cursorX
    fun getCursorY() = cursorY

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(cursorX, cursorY, radius, paint)
        canvas.drawCircle(cursorX, cursorY, radius, borderPaint)
    }
}
