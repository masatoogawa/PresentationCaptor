package com.xevo.argo.virtualDisplayCaptor

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder

class ScreenCaptor(_context: Context) {
    val context = _context
    var width = 0
    var height = 0
    var fps = 0L
    var service : ScreenCaptorService.Service? = null

    interface Callback {
        fun onCaptured(bitmap: ByteArray)
    }

    fun invoke(intentForMediaProjection: Intent, callback: Callback) {
        context.run {
            val intent = Intent(context, ScreenCaptorService::class.java).apply {
                putExtra("intentForMediaProjection", intentForMediaProjection)
                putExtra("width", width)
                putExtra("height", height)
                putExtra("fps", fps)
                putExtra("dpi", context.resources.displayMetrics.density)
            }
            startForegroundService(intent)
            bindService(intent, object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, _service: IBinder?) {
                    (_service as ScreenCaptorService.Service).let {
                        it.callback = callback
                    }
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    service = null
                }

            }, Service.BIND_AUTO_CREATE)

        }


    }
}