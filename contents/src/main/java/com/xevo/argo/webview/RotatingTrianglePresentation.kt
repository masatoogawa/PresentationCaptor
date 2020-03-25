package com.xevo.argo.webview

import android.app.Presentation
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Display
import kotlinx.android.synthetic.main.rotating_triangle.*

class RotatingTrianglePresentation(context: Context, display: Display) : Presentation(context, display) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rotating_triangle)

        Handler().postDelayed(runnable {
            simplesurfaceview.onInvalidate()
            Handler().postDelayed(this, 1000/30)
        },1000/30)

        reticleview.setOnTouchListener { v, event ->
            Log.d("reticle", event.toString())
            reticleview.inject(event)
            true
        }
    }

    inline fun runnable(crossinline body: Runnable.() -> Unit) = object : Runnable {
        override fun run() = body()
    }
}
