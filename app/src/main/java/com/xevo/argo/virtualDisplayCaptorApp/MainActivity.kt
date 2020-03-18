package com.xevo.argo.virtualDisplayCaptorApp

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.SurfaceHolder
import com.xevo.argo.virtualDisplayCaptor.VirtualDisplayCaptor
import com.xevo.argo.webview.WebViewActivity
import com.xevo.argo.webview.WebViewPresentation

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {
    private var isSurfaceCreated = false
    private var width = 0
    private var height = 0

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
                isSurfaceCreated = true
                width = _width
                height = _height

                val bundle = Bundle()
                bundle.putString("url", "https://www.xevo.com/")

                VirtualDisplayCaptor(this@MainActivity).apply {
                    fps = 30
                    width = this@MainActivity.width
                    height = this@MainActivity.height
                }.invoke<WebViewActivity>(object : VirtualDisplayCaptor.Callback {
                    override fun onCaptured(bitmap: VirtualDisplayCaptor.BoxedByteArray) {
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
                }, bundle)

//
//                VirtualDisplayCaptor(this@MainActivity).apply {
//                    fps = 30
//                    width = this@MainActivity.width
//                    height = this@MainActivity.height
//                }.invoke<WebViewPresentation>(object : VirtualDisplayCaptor.Callback {
//                    override fun onCaptured(bitmap: VirtualDisplayCaptor.BoxedByteArray) {
//                        Log.d("MO", "onRendered")
//                        val bytebuffer = ByteBuffer.allocate(bitmap.byteArray.size)
//                        bytebuffer.put(bitmap.byteArray,0, bitmap.byteArray.size)
//                        bytebuffer.rewind()
//                        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//                        bmp.copyPixelsFromBuffer(bytebuffer)
//
//                        if (isSurfaceCreated) {
//                            val canvas = surfaceView.holder.lockCanvas()
//                            val left: Float = 0f
//                            val top: Float = 0f
//                            canvas.drawBitmap(bmp, left, top, null)
//                            surfaceView.holder.unlockCanvasAndPost(canvas)
//                        }
//                    }
//                }) {
//                    //url = "https://www.youtube.com"
//                    url = "https://www.youtube.com/watch?v=GSeRKL895WA"
//                }
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
