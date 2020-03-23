package com.xevo.argo.virtualDisplayCaptor.unity

import android.util.Log
import com.unity3d.player.UnityPlayer
import com.xevo.argo.virtualDisplayCaptor.VirtualDisplayCaptor
import com.xevo.argo.webview.WebViewPresentation


class WebView {

    class BoxedByteArray(ba: ByteArray) {
        val byteArray = ba
    }

    interface Callback {
        fun onCaptured(bitmap: BoxedByteArray)
    }

    var url: String? = null

    fun invoke(width: Int, height: Int, fps: Long, callback: Callback) {

        VirtualDisplayCaptor(UnityPlayer.currentActivity).apply {
            this.fps = fps
            this.width = width
            this.height = height
        }.invoke<WebViewPresentation>(object : VirtualDisplayCaptor.Callback {
            override fun onCaptured(bitmap: ByteArray) {
                callback.onCaptured(BoxedByteArray(bitmap))
            }
        }) {
            url = this@WebView.url
        }
    }
}