package com.xevo.argo.virtualDisplayCaptor

import android.app.Activity
import android.app.ActivityOptions
import android.app.Presentation
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.Image
import android.media.ImageReader
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Display
import android.view.MotionEvent
import android.view.WindowManager
import com.xevo.argo.webview.WebViewActivity
import java.nio.ByteBuffer
import kotlin.concurrent.timer

class VirtualDisplayCaptor(var context: Context) {

    interface Callback {
        fun onCaptured(bitmap: BoxedByteArray)
    }

    class BoxedByteArray(ba: ByteArray) {
        val byteArray = ba
    }

    var presentation: Presentation? = null
    var isLaunched = false
    var width = 0
    var height = 0
    var fps: Long = 0
    var displayId = 0
    lateinit var callback: Callback
    lateinit var imageReader: ImageReader

    inline fun <reified T: Activity> invoke(_callback: Callback, bundle: Bundle) : VirtualDisplayCaptor {
        callback = _callback
        val displayManager =
            context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        displayManager.registerDisplayListener(object : DisplayManager.DisplayListener {
            override fun onDisplayAdded(_displayId: Int) {
                displayId = _displayId
            }
            override fun onDisplayChanged(_displayId: Int) {
                if (displayId == _displayId && !isLaunched) {
                    context.startActivity(
                        Intent(context, T::class.java).apply {
                            putExtras(bundle)
                            addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        },
                        ActivityOptions.makeBasic().apply {
                            launchDisplayId = _displayId
                        }.toBundle()
                    )
                    isLaunched = true
                    timer(period = 1000 / fps) {
                        val image: Image? = imageReader.acquireLatestImage()
                        image?.let {
                            val planes = it.planes
                            val buffer = planes[0].buffer
                            val pixelStride = planes[0].pixelStride
                            val rowStride = planes[0].rowStride
                            val rowPadding = rowStride - pixelStride * width
                            val bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888)
                            bitmap?.let { bitmap ->
                                bitmap.copyPixelsFromBuffer(buffer)
                                val croppedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height)
                                val byteBuffer = ByteBuffer.allocate(pixelStride * width * height)
                                croppedBitmap.copyPixelsToBuffer(byteBuffer)
                                callback.onCaptured(
                                    BoxedByteArray(byteBuffer.array())
                                )
                            }
                            it.close()
                        }
                    }
                }
            }
            override fun onDisplayRemoved(displayId: Int) {
            }
        }, null)
        imageReader = ImageReader.newInstance(this.width, this.height, PixelFormat.RGBA_8888, 1)
        val windowManager =
            context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        displayManager.createVirtualDisplay("DisplayManagerVirtualDisplay",
            this.width, this.height, metrics.densityDpi, imageReader.surface,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION)
        return this
    }

    inline fun <reified T: Presentation> invoke(_callback: Callback, crossinline initBlock: T.() -> Unit) : VirtualDisplayCaptor {
        callback = _callback
        val displayManager =
            context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        displayManager.registerDisplayListener(object : DisplayManager.DisplayListener {
            override fun onDisplayAdded(_displayId: Int) {
                displayId = _displayId
            }
            override fun onDisplayChanged(_displayId: Int) {
                if (displayId == _displayId && presentation == null) {
                    this@VirtualDisplayCaptor.presentation =
                        T::class.java.getConstructor(Context::class.java, Display::class.java)
                            .newInstance(context, displayManager.getDisplay(displayId))
                            .apply(initBlock)
                    presentation?.show()
                    isLaunched = true
                    timer(period = 1000 / fps) {
                        val image: Image? = imageReader.acquireLatestImage()
                        image?.let {
                            val planes = it.planes
                            val buffer = planes[0].buffer
                            val pixelStride = planes[0].pixelStride
                            val rowStride = planes[0].rowStride
                            val rowPadding = rowStride - pixelStride * width
                            val bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888)
                            bitmap?.let { bitmap ->
                                bitmap.copyPixelsFromBuffer(buffer)
                                val croppedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height)
                                val byteBuffer = ByteBuffer.allocate(pixelStride * width * height)
                                croppedBitmap.copyPixelsToBuffer(byteBuffer)
                                callback.onCaptured(
                                    BoxedByteArray(byteBuffer.array())
                                )
                            }
                            it.close()
                        }
                    }
                }
            }
            override fun onDisplayRemoved(displayId: Int) {
            }
        }, null)
        imageReader = ImageReader.newInstance(this.width, this.height, PixelFormat.RGBA_8888, 1)
        val windowManager =
            context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        displayManager.createVirtualDisplay("DisplayManagerVirtualDisplay",
            this.width, this.height, metrics.densityDpi, imageReader.surface,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION)
        return this
    }

    fun inject(ev: MotionEvent) {
        presentation?.dispatchTouchEvent(ev)
        WebViewActivity.activity?.get()?.dispatchTouchEvent(ev)
    }
}