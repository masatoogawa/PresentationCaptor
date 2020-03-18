package com.xevo.argo.webview

import android.app.Presentation
import android.content.Context
import android.os.Bundle
import android.view.Display
import kotlinx.android.synthetic.main.content.*

class WebViewPresentation(context: Context, display: Display) : Presentation(context, display) {

    var url : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content)
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(url)
    }
}