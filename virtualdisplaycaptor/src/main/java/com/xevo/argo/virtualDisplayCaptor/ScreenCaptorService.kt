package com.xevo.argo.virtualDisplayCaptor

import android.app.*
import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjectionManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Display
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import kotlin.concurrent.thread
import kotlin.concurrent.timer
import kotlin.coroutines.CoroutineContext

class ScreenCaptorService : Service(), CoroutineScope {

    private val singleThreadContext = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val job = Job()
    override val coroutineContext: CoroutineContext
        //get() = Dispatchers.Main + job
        get() = singleThreadContext + job

    lateinit var imageReader: ImageReader
    var displayId = 0
    var fps = 0L
    private var width = 0
    private var height = 0

    var virtualDisplay: VirtualDisplay? = null


    inner class Service: Binder() {
        var callback : ScreenCaptor.Callback? = null
        fun resize(_width: Int, _height: Int, dpi: Int) {
            launch {
                Log.d("mogawa", "resize in coroutine")
                width = _width
                height = _height
                virtualDisplay?.surface = null
                imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
                virtualDisplay?.resize(width, height, dpi)
                virtualDisplay?.surface = imageReader.surface
            }
        }
    }

    lateinit var service: Service

    override fun onBind(intent: Intent?): IBinder? {
        service = Service()
        return service
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startAsForegroundService(id: String, name: String, desc: String, title: String, text: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.getNotificationChannel(id) == null) {
            notificationManager.createNotificationChannel(NotificationChannel(
                id, name, NotificationManager.IMPORTANCE_HIGH).apply {
                description = desc
            })
        }

        startForeground(1, NotificationCompat.Builder(this, id).apply {
            setContentTitle(title)
            setContentText(text)
        }.build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startAsForegroundService(
            "NotificationOfScreenCaptorService",
            "Notification Of ScreenCaptorService",
            "None",
            "TITLE: Notification of ScreenCaptorService",
            "TEXT: Notification of ScreenCaptorService")

        intent?.run {
            val intentForMediaProjection = getParcelableExtra<Intent>("intentForMediaProjection")
            width = getIntExtra("width", -1)
            height = getIntExtra("height", -1)
            fps = getLongExtra("fps", -1)
            val density = getFloatExtra("dpi", -1f)

            val displayManager =
                getSystemService(Context.DISPLAY_SERVICE) as DisplayManager

            displayManager.registerDisplayListener(object : DisplayManager.DisplayListener {
                override fun onDisplayAdded(_displayId: Int) {
                    displayId = _displayId
                }
                override fun onDisplayChanged(_displayId: Int) {
                    if (displayId == _displayId) {
                        timer(period = 1000 / fps) {
                            launch {
                                val image: Image? = imageReader.acquireLatestImage()
                                image?.let {
                                    val planes = it.planes
                                    val buffer = planes[0].buffer
                                    val pixelStride = planes[0].pixelStride
                                    val rowStride = planes[0].rowStride
                                    val rowPadding = rowStride - pixelStride * width
                                    val bitmap = Bitmap.createBitmap(
                                        width + rowPadding / pixelStride,
                                        height,
                                        Bitmap.Config.ARGB_8888
                                    )
                                    bitmap?.let { bitmap ->
                                        bitmap.copyPixelsFromBuffer(buffer)
                                        val croppedBitmap =
                                            Bitmap.createBitmap(bitmap, 0, 0, width, height)
                                        val byteBuffer =
                                            ByteBuffer.allocate(pixelStride * width * height)

                                        val bbb = blackBitmap(width, height)
                                        Canvas(bbb).drawBitmap(croppedBitmap, 0f,0f,null)




                                        //croppedBitmap.copyPixelsToBuffer(byteBuffer)
                                        bbb.copyPixelsToBuffer(byteBuffer)
                                        this@ScreenCaptorService.service.callback?.onCaptured(
                                            byteBuffer.array()
                                        )
                                    }
                                    it.close()
                                }
                            }
                        }
                    }
                }
                override fun onDisplayRemoved(displayId: Int) {
                }
            }, null)

            intentForMediaProjection?.let {
                val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                val mediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, it)

                imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
                virtualDisplay = mediaProjection.createVirtualDisplay("ScreenCaptorService",
                    width, height, density.toInt(), DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    imageReader.surface, object : VirtualDisplay.Callback() {
                        override fun onPaused() {
                            super.onPaused()
                            Log.d("mogawa", "VD onPaused")
                        }

                        override fun onResumed() {
                            super.onResumed()
                            Log.d("mogawa", "VD onResumed")
                        }

                        override fun onStopped() {
                            super.onStopped()
                            Log.d("mogawa", "VD onStopped")
                        }
                    }, null)
            }
        }

        return START_NOT_STICKY
    }

    fun blackBitmap(width: Int, height: Int) : Bitmap {
        val bitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888)
        Canvas(bitmap).drawRect(0f, 0f, width.toFloat(), height.toFloat(),
            Paint().apply {
                color = Color.BLACK
                alpha = 255
            })
        return bitmap
    }
}