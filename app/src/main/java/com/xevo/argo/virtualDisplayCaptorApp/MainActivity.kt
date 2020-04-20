package com.xevo.argo.virtualDisplayCaptorApp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.SurfaceHolder
import android.widget.Toast
import com.xevo.argo.virtualDisplayCaptor.VirtualDisplayCaptor

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {
    private var isSurfaceCreated = false
    private var width = 0
    private var height = 0
    private var vdc: VirtualDisplayCaptor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        surfaceView.setOnTouchListener { v, event ->
            Log.d("mogawa", "touch $event")
            vdc?.inject(event)
            true
        }

        surfaceView.holder.addCallback(object: SurfaceHolder.Callback {
            override fun surfaceChanged(
                holder: SurfaceHolder?,
                format: Int,
                _width: Int,
                _height: Int
            ) {
                isSurfaceCreated = true
                width = _width
                height = _height
                requestScreenCapturePermission()
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                Log.d("mogawa", "surfaceCreated")
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                Log.d("mogawa", "surfaceDestroyed")
            }
        })

    }

    private fun requestScreenCapturePermission() {
        val mediaProjectionManager: MediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intentForMediaProjection: Intent?) {
        super.onActivityResult(requestCode, resultCode, intentForMediaProjection)
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                intentForMediaProjection?.let {
                    vdc = VirtualDisplayCaptor(this@MainActivity).apply {
                        fps = 30
                        width = this@MainActivity.width
                        height = this@MainActivity.height
                    }.screenCapture(it, object: VirtualDisplayCaptor.Callback {
                        override fun onCaptured(bitmap: ByteArray) {
                            Log.d("MO", "onRendered")
                            val bytebuffer = ByteBuffer.allocate(bitmap.size)
                            bytebuffer.put(bitmap, 0, bitmap.size)
                            bytebuffer.rewind()
                            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                            bmp.copyPixelsFromBuffer(bytebuffer)

                            if (isSurfaceCreated) {
                                val canvas = surfaceView.holder.lockCanvas()
                                canvas.drawBitmap(bmp, 0.0f, 0.0f, null)
                                surfaceView.holder.unlockCanvasAndPost(canvas)
                            }
                        }
                    })
                    return
                }
            }
        }
        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
