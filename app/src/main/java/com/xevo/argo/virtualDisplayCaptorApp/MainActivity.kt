package com.xevo.argo.virtualDisplayCaptorApp

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.*
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.Surface
import android.view.SurfaceHolder
import android.widget.Toast
import com.xevo.argo.virtualDisplayCaptor.VirtualDisplayCaptor

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.util.*
import kotlin.concurrent.timer
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {
    private var isSurfaceCreated = false
    private var width = 0
    private var height = 0
    private var vdc: VirtualDisplayCaptor? = null
    private lateinit var timer: Timer
    private var pipSize = 0

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            var point = Point(0,0)
            windowManager.defaultDisplay.getRealSize(point)

            var builder = PictureInPictureParams.Builder()
            pipSize = if(point.x > point.y) point.x else point.y
            //enterPictureInPictureMode(builder.setAspectRatio(Rational(point.x, point.y)).build())
            enterPictureInPictureMode(builder.setAspectRatio(Rational(pipSize, pipSize)).build())
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
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
                Log.d("mogawa", "w:$width|h:$height")
                if (vdc == null) {
                    requestScreenCapturePermission()
                }
                else {

//                    val canvas = surfaceView.holder.lockCanvas()
//                    canvas?.let {cv ->
//                        cv.drawBitmap(blackBitmap(width,height), 0.0f, 0.0f, null)
//                        surfaceView.holder.unlockCanvasAndPost(cv)
//                    }



                    vdc?.resizeScreenCaptor(width, height)
                }
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                Log.d("mogawa", "surfaceCreated")
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                Log.d("mogawa", "surfaceDestroyed")
            }
        })

//        timer = timer(period = 1000L) {
//            val orientation = resources.configuration.orientation
//
//            launch {
//
//                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                    Log.d("mogawa", "landscape")
//                    //Toast.makeText(this@MainActivity, "landscape", Toast.LENGTH_SHORT).show()
//                }
//                else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
//                    Log.d("mogawa", "portrait")
//                    //Toast.makeText(this@MainActivity, "portrait", Toast.LENGTH_SHORT).show()
//
//                }
//            }
//        }

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
                        fps = 15
                        width = this@MainActivity.width
                        height = this@MainActivity.height
                    }.screenCapture(it, object: VirtualDisplayCaptor.Callback {
                        override fun onCaptured(bitmap: ByteArray) {
                            Log.d("MO", "onRendered w:${width},h:${height},bitmap.size:${bitmap.size}")
                            if (width * height * 4 != bitmap.size) {
                                return
                            }
                            val bytebuffer = ByteBuffer.allocate(bitmap.size)
                            bytebuffer.put(bitmap, 0, bitmap.size)
                            bytebuffer.rewind()
                            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                            bmp.copyPixelsFromBuffer(bytebuffer)

                            if (isSurfaceCreated) {
                                val canvas = surfaceView.holder.lockCanvas()
                                canvas?.let {cv ->
                                    cv.drawBitmap(bmp, 0.0f, 0.0f, null)
                                    surfaceView.holder.unlockCanvasAndPost(cv)
                                }
                            }
                        }
                    })
                    return
                }
            }
        }
        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show()
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show()

        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration?
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (isInPictureInPictureMode) {
            timer = timer(period = 1000L) {
                val rotation = windowManager.defaultDisplay.rotation
                val point = Point(0,0)
                windowManager.defaultDisplay.getRealSize(point)

                launch {

                    when (rotation) {
                        Surface.ROTATION_180, Surface.ROTATION_0 -> {
                            Log.d("mogawa", "ROTATION_180, ROTATION_0 $point")
                            //val builder = PictureInPictureParams.Builder()
                            //enterPictureInPictureMode(builder.build())
                            //enterPictureInPictureMode(builder.setAspectRatio(Rational(point.x, point.y)).build())
//                            val canvas = surfaceView.holder.lockCanvas()
//                            canvas?.let {cv ->
//                                cv.drawBitmap(blackBitmap(width,height), 0.0f, 0.0f, null)
//                                surfaceView.holder.unlockCanvasAndPost(cv)
//                            }
                        }
                        Surface.ROTATION_270, Surface.ROTATION_90  -> {
                            Log.d("mogawa", "ROTATION_270, ROTATION_90 $point")
                            //val builder = PictureInPictureParams.Builder()
                            //enterPictureInPictureMode(builder.build())
                            //enterPictureInPictureMode(builder.setAspectRatio(Rational(point.x, point.y)).build())
//                            val canvas = surfaceView.holder.lockCanvas()
//                            canvas?.let {cv ->
//                                cv.drawBitmap(blackBitmap(width,height), 0.0f, 0.0f, null)
//                                surfaceView.holder.unlockCanvasAndPost(cv)
//                            }
                        }
                        else -> {
                            Log.d("mogawa", "ROTATION_UNKNOWN")
                        }

                    }
                }
            }

        } else {
            timer.cancel()
        }
    }


}
