package com.xevo.virtualdisplayunityplugin

import android.app.Activity
import android.app.Presentation
import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.Image
import android.media.ImageReader
import android.os.Bundle
import android.os.Debug
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import java.nio.ByteBuffer
import kotlin.concurrent.timer
import com.unity3d.player.UnityPlayer
import kotlinx.android.synthetic.main.presentation_main.*
import kotlinx.android.synthetic.main.presentation_webview.*

const val TAG = "VDP"

class BoxedByteArrayForCSharp(ba: ByteArray) {
    val byteArray = ba
}

interface listener {
    fun onRendered(bitmap: BoxedByteArrayForCSharp)
}

class VirtualDisplayPlugin : DisplayManager.DisplayListener {

    private lateinit var imageReader: ImageReader;
    private var mainPresentation: MainPresentation? = null
    inner class MainPresentation(context: Context, display: Display) : Presentation(context, display) {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.presentation_webview)
            //webView.loadUrl("https://example.com/")
            webView.loadUrl("https://google.com/")
            //webView.loadUrl("https://www.lear.com/")

            //val rotation = AnimationUtils.loadAnimation(activity, R.anim.rotator)
            //textView.startAnimation(rotation)
        }
    }

    override fun onDisplayChanged(displayId: Int) {
        if (this.displayId == displayId) {
            val display = displayManager.getDisplay(this.displayId)
            mainPresentation = MainPresentation(activity, display)
            mainPresentation?.show()
        }

        //val handler = Looper.getMainLooper()
        timer(period = 1000 / _fps) {

            //activity.runOnUiThread {
            val image: Image? = imageReader.acquireLatestImage()
            image?.let {
                Log.d(TAG,it.toString())
                val planes = it.planes
                val buffer = planes[0].buffer
                val pixelStrive = planes[0].pixelStride
                val rowStride = planes[0].rowStride
                val rowPadding = rowStride - pixelStrive * 800

                bitmap = Bitmap.createBitmap(800 + rowPadding / pixelStrive, 480, Bitmap.Config.ARGB_8888)
                bitmap?.copyPixelsFromBuffer(buffer)


                val bytebuffer = ByteBuffer.allocate(bitmap!!.byteCount)
                bitmap?.copyPixelsToBuffer(bytebuffer)



                _listener?.onRendered(BoxedByteArrayForCSharp(bytebuffer.array()))
                it.close()

            }
        }
    }

    override fun onDisplayAdded(displayId: Int) {
        Log.d(TAG, "onDisplayAdded")
        this.displayId = displayId
    }

    override fun onDisplayRemoved(displayId: Int) {
        Log.d(TAG, "onDisplayRemoved")
    }

    private var displayId: Int = 0
    private var _listener: listener? = null
    private var _fps: Long = 0
    private var count = 0
    private var bitmap: Bitmap? = null
    private var activity = UnityPlayer.currentActivity
    //private lateinit var activity : Activity
    private var metrics = DisplayMetrics()
    private lateinit var displayManager: DisplayManager
    private lateinit var windowManager: WindowManager
    //private var displayManager =
    //    activity.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    //private var windowManager =
    //    activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    init {
        //windowManager.defaultDisplay.getMetrics(metrics)
        //displayManager.registerDisplayListener(this, null)
    }

    fun startRender(context: Activity, fps: Long, listener: listener) {
        activity = context
        startRender(fps, listener)

    }
    fun startRender(fps: Long, listener: listener) {

        displayManager =
            activity.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        windowManager =
            activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(metrics)
        displayManager.registerDisplayListener(this, null)
        imageReader = ImageReader.newInstance(800, 480, PixelFormat.RGBA_8888, 2)

        displayManager.createVirtualDisplay("DisplayManagerVirtualDisplay",
            800, 480, metrics.densityDpi, imageReader.surface,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION, null, null)


        _listener = listener
        _fps = fps

//        bitmap = Bitmap.createBitmap(800, 480, Bitmap.Config.ARGB_8888)
//        bitmap?.let {
//            timer(period = 1000 / _fps) {
//                updateBitmap()
//                val bytebuffer = ByteBuffer.allocate(it.byteCount)
//                it.copyPixelsToBuffer(bytebuffer)
//                _listener?.onRendered(BoxedByteArrayForCSharp(bytebuffer.array()))
//            }
//        }
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