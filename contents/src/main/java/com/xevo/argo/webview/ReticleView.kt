package com.xevo.argo.webview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class ReticleView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
): View(context, attrs, defStyleAttr) {

    val paint = Paint().apply {
        color = Color.YELLOW
        strokeWidth = 5.0f
        style = Paint.Style.STROKE
    }

    data class Reticle (var x: Float,
                        var y: Float)
    var reticleMap = mutableMapOf<Int, Reticle>()

    @Synchronized
    override fun onDraw(canvas: Canvas?) {
        reticleMap.forEach { (k, v) ->
            val pts = floatArrayOf(
                v.x, 0.0f, v.x, height.toFloat(),
                0.0f, v.y, width.toFloat(), v.y)
            canvas?.drawLines(pts, paint)
            canvas?.drawCircle(v.x,v.y,50.0f, paint)
            canvas?.drawCircle(v.x,v.y,100.0f, paint)
        }
    }

    @Synchronized
    fun inject(event: MotionEvent) {
        for (index in 0 until event.pointerCount) {
            reticleMap[event.getPointerId(index)] = Reticle(
                event.getX(index), event.getY(index))
        }
        invalidate()
    }
}

