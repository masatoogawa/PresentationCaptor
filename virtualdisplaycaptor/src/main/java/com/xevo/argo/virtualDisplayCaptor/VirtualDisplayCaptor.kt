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
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.nio.ByteBuffer
import kotlin.concurrent.timer
import com.unity3d.player.UnityPlayer
import kotlinx.android.synthetic.main.presentation_webview.*

const val TAG = "VDP"

class BoxedByteArrayForCSharp(ba: ByteArray) {
    val byteArray = ba
}

interface Listener {
    fun onRendered(bitmap: BoxedByteArrayForCSharp)
}

class VirtualDisplayCaptor : DisplayManager.DisplayListener {
    // User Settings
    private lateinit var activity: Activity
    private var width = 0
    private var height = 0
    private var fps: Long = 0
    private lateinit var listener: Listener

    private var mainPresentation: MainPresentation? = null
    private lateinit var displayManager: DisplayManager
    private var displayId = 0
    private lateinit var imageReader: ImageReader
    //private var url = "https://google.com/"
    private var url = "https://www.youtube.com/watch?v=GSeRKL895WA"
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun startRender(width: Int, height: Int, fps: Long, listener: Listener) {
        startRender(UnityPlayer.currentActivity, width, height, fps, listener)
    }

    fun setURL(url: String) {
        this.url = url
    }

    fun startRender(activity: Activity, width: Int, height: Int, fps: Long, listener: Listener) {
        this.activity = activity
        this.width = width
        this.height = height
        this.listener = listener
        this.fps = fps

        displayManager =
            activity.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        displayManager.registerDisplayListener(this, null)

        imageReader = ImageReader.newInstance(this.width, this.height, PixelFormat.RGBA_8888, 1)

        val windowManager =
            activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)

        displayManager.createVirtualDisplay("DisplayManagerVirtualDisplay",
            this.width, this.height, metrics.densityDpi, imageReader.surface,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION)
    }

    private inline fun <reified T> launchActivity(context: Context, displayId: Int) {
        context.startActivity(
            Intent(context, T::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            },
            ActivityOptions.makeBasic().apply {
                launchDisplayId = displayId
            }.toBundle())
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onDisplayChanged(displayId: Int) {
        if (this.displayId == displayId && mainPresentation == null) {
            mainPresentation = MainPresentation(activity, displayManager.getDisplay(this.displayId))
            //mainPresentation?.show()

            launchActivity<TestActivity>(activity, displayId)

            timer(period = 1000 / this.fps) {
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
                        listener.onRendered(
                            BoxedByteArrayForCSharp(
                                byteBuffer.array()
                            )
                        )
                    }
                    it.close()
                }
            }
        }
    }

    override fun onDisplayAdded(displayId: Int) {
        this.displayId = displayId
    }

    override fun onDisplayRemoved(displayId: Int) {
    }

    inner class MainPresentation(context: Context, display: Display) : Presentation(context, display) {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.presentation_webview)
            //setContentView(R.layout.presentation_main)
            //val rotation = AnimationUtils.loadAnimation(activity, R.anim.rotator)
            //textView.startAnimation(rotation)
            webView.settings.javaScriptEnabled = true
            webView.loadUrl(url)
        }
    }

    class TestActivity: AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.presentation_main)
        }
    }
}