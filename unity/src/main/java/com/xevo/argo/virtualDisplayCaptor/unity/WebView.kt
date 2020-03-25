package com.xevo.argo.virtualDisplayCaptor.unity

import com.xevo.argo.webview.WebViewPresentation

class WebView : NativeView() {

    var url: String? = null

    override fun invoke(width: Int, height: Int, fps: Long, callback: Callback) {
        invoke<WebViewPresentation>(width,height,fps,callback) {
            url = this@WebView.url
        }
    }
}