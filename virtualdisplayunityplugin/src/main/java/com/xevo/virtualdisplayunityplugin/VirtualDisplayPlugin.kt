package com.xevo.virtualdisplayunityplugin

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.nio.ByteBuffer
import kotlin.concurrent.timer

class BoxedByteArrayForCSharp(ba: ByteArray) {
    val byteArray = ba
}

interface listener {
    fun onRendered(bitmap: BoxedByteArrayForCSharp)
}

class VirtualDisplayPlugin {

    private var _listener: listener? = null
    private var _fps: Long = 0
    private var count = 0
    private var bitmap: Bitmap? = null

    fun startRender(fps: Long, listener: listener) {
        _listener = listener
        _fps = fps

        bitmap = Bitmap.createBitmap(800, 480, Bitmap.Config.ARGB_8888)
        bitmap?.let {
            timer(period = 1000 / _fps) {
                updateBitmap()
                val bytebuffer = ByteBuffer.allocate(it.byteCount)
                it.copyPixelsToBuffer(bytebuffer)
                _listener?.onRendered(BoxedByteArrayForCSharp(bytebuffer.array()))
            }
        }
    }

    fun updateBitmap() {
        bitmap?.let {
            when (count % 3) {
                0 -> {
                    it.eraseColor(android.graphics.Color.RED)
                }
                1 -> {
                    it.eraseColor(android.graphics.Color.GREEN)

                }
                2 -> {
                    it.eraseColor(android.graphics.Color.BLUE)

                }
                else -> {

                }

            }
            count++
        }
    }
}