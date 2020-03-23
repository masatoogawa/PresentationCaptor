package com.xevo.argo.webview

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.content.*
import java.lang.ref.WeakReference

class WebViewActivity : AppCompatActivity() {

    companion object {
        var activity: WeakReference<Activity>? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content)
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(intent.getStringExtra("url"))
        activity = WeakReference(this)
    }

}