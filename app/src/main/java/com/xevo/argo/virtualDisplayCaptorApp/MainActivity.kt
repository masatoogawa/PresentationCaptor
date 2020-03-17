package com.xevo.argo.virtualDisplayCaptorApp

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.SurfaceHolder
import com.xevo.argo.virtualDisplayCaptor.BoxedByteArrayForCSharp
import com.xevo.argo.virtualDisplayCaptor.VirtualDisplayCaptor
import com.xevo.argo.virtualDisplayCaptor.Listener

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity(), Listener {
    private var isSurfaceCreated = false
    private var width = 0
    private var height = 0

    override fun onRendered(bitmap: BoxedByteArrayForCSharp) {
        Log.d("MO", "onRendered")
        val bytebuffer = ByteBuffer.allocate(bitmap.byteArray.size)
        bytebuffer.put(bitmap.byteArray,0, bitmap.byteArray.size)
        bytebuffer.rewind()
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bmp.copyPixelsFromBuffer(bytebuffer)

        if (isSurfaceCreated) {
            val canvas = surfaceView.holder.lockCanvas()
            val left: Float = 0f
            val top: Float = 0f
            canvas.drawBitmap(bmp, left, top, null)
            surfaceView.holder.unlockCanvasAndPost(canvas)
        }

    }

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
            true
        }

        surfaceView.holder.addCallback(object: SurfaceHolder.Callback {
            override fun surfaceChanged(
                holder: SurfaceHolder?,
                format: Int,
                _width: Int,
                _height: Int
            ) {
                MainActivity@isSurfaceCreated = true
                MainActivity@width = _width
                MainActivity@height = _height
                Log.d("mogawa", "surfaceChanged w:$width, h:$height")
                val v = VirtualDisplayCaptor()
                v.startRender(this@MainActivity, width, height, 30, this@MainActivity)
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                Log.d("mogawa", "surfaceCreated")
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                Log.d("mogawa", "surfaceDestroyed")
            }
        })
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
