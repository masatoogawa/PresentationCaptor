package com.xevo.argo.virtualDisplayCaptor.unity

import android.util.Log
import com.unity3d.player.UnityPlayer
import com.xevo.argo.virtualDisplayCaptor.VirtualDisplayCaptor
import com.xevo.argo.webview.WebViewPresentation


class WebView {

    interface Callback {
        fun onRendered(bitmap: VirtualDisplayCaptor.BoxedByteArray)
    }

    var url: String? = null

    fun invoke(width: Int, height: Int, fps: Long, callback: Callback) {

        VirtualDisplayCaptor(UnityPlayer.currentActivity).apply {
            this.fps = fps
            this.width = width
            this.height = height
        }.invoke<WebViewPresentation>(object : VirtualDisplayCaptor.Callback {
            override fun onCaptured(bitmap: VirtualDisplayCaptor.BoxedByteArray) {
                Log.d("MO", "onRendered")
                callback.onRendered(bitmap)
            }
        }) {
            url = this@WebView.url
        }
    }
}