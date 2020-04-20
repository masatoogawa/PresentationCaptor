package com.xevo.argo.virtualDisplayCaptor

import android.app.*
import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjectionManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.view.Display
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import java.nio.ByteBuffer
import kotlin.concurrent.timer

class ScreenCaptorService : Service() {

    lateinit var imageReader: ImageReader
    var displayId = 0
    var fps = 0L

    inner class Service: Binder() {
        var callback : ScreenCaptor.Callback? = null
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
            val width = getIntExtra("width", -1)
            val height = getIntExtra("height", -1)
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
                                    this@ScreenCaptorService.
                                    service.callback?.onCaptured(
                                        byteBuffer.array()
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

            intentForMediaProjection?.let {
                val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                val mediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, it)

                imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
                mediaProjection.createVirtualDisplay("ScreenCaptorService",
                    width, height, density.toInt(), DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    imageReader.surface, null, null)
            }
        }

        return START_NOT_STICKY
    }
}