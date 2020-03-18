package com.xevo.argo.webview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.content.*

class WebViewActivity : AppCompatActivity() {
    var url : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content)
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(intent.getStringExtra("url"))
    }

}