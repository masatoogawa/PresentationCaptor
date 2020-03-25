package com.xevo.argo.virtualDisplayCaptor.unity

import com.xevo.argo.webview.RotatingTrianglePresentation

class RotatingTriangleView : NativeView() {

    override fun invoke(width: Int, height: Int, fps: Long, callback: Callback) {
        invoke<RotatingTrianglePresentation>(width,height,fps,callback) {}
    }
}
