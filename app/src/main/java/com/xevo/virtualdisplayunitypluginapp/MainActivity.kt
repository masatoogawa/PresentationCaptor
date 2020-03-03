package com.xevo.virtualdisplayunitypluginapp

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.xevo.virtualdisplayunityplugin.BoxedByteArrayForCSharp
import com.xevo.virtualdisplayunityplugin.VirtualDisplayPlugin
import com.xevo.virtualdisplayunityplugin.Listener

import kotlinx.android.synthetic.main.activity_main.*
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity(), Listener {
    override fun onRendered(bitmap: BoxedByteArrayForCSharp) {
        Log.d("MO", "onRendered")
        val bytebuffer = ByteBuffer.allocate(bitmap.byteArray.size)
        bytebuffer.put(bitmap.byteArray,0, bitmap.byteArray.size)
        bytebuffer.rewind()
        val bmp = Bitmap.createBitmap(800, 480, Bitmap.Config.ARGB_8888)
        bmp.copyPixelsFromBuffer(bytebuffer)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        val v = VirtualDisplayPlugin()
        v.startRender(this, 800, 480, 1, this)
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
