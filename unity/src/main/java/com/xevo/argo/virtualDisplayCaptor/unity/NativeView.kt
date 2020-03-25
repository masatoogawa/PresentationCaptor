package com.xevo.argo.virtualDisplayCaptor.unity

import android.app.Presentation
import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import com.unity3d.player.UnityPlayer
import com.xevo.argo.virtualDisplayCaptor.VirtualDisplayCaptor

open class NativeView {

    lateinit var vdc: VirtualDisplayCaptor
    class BoxedByteArray(ba: ByteArray) {
        val byteArray = ba
    }

    interface Callback {
        fun onCaptured(bitmap: BoxedByteArray)
    }

    enum class TouchAction {
        BEGIN, MOVE, END
    }

    var fingerIdSet = mutableSetOf<Int>()

    inline fun <reified T: Presentation> invoke(_width: Int, _height: Int, _fps: Long,
                                                _callback: Callback,
                                                crossinline _initBlock: T.() -> Unit) {
        vdc = VirtualDisplayCaptor(UnityPlayer.currentActivity).apply {
            fps = _fps
            width = _width
            height = _height
        }.invoke(object : VirtualDisplayCaptor.Callback {
            override fun onCaptured(bitmap: ByteArray) {
                _callback.onCaptured(BoxedByteArray(bitmap))
            }
        }, _initBlock)
    }

    open fun invoke(width: Int, height: Int, fps: Long, callback: Callback) {}

    fun injectTouch(_id: Int, touchAction: TouchAction, _x: Float, _y: Float) {
        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis()

        val properties = mutableListOf<MotionEvent.PointerProperties>()
        properties.add(MotionEvent.PointerProperties().apply {
            id = _id
            toolType = MotionEvent.TOOL_TYPE_FINGER
        })

        val coords = mutableListOf<MotionEvent.PointerCoords>()
        coords.add(MotionEvent.PointerCoords().apply {
            x = _x
            y = _y
            orientation = 0f
            pressure = 1.0f
            size = 1.0f
        })

        when(touchAction) {
            TouchAction.BEGIN -> {
                val action = if (fingerIdSet.size == 0) MotionEvent.ACTION_DOWN else MotionEvent.ACTION_POINTER_DOWN
                fingerIdSet.add(_id)
                val ev = MotionEvent.obtain(
                    downTime,
                    eventTime,
                    action,
                    1,
                    properties.toTypedArray(),
                    coords.toTypedArray(),
                    0,
                    0,
                    1.0f,
                    1.0f,
                    0,
                    0,
                    InputDevice.SOURCE_TOUCHSCREEN,
                    0
                )
                vdc.inject(ev)
            }
            TouchAction.END -> {
                fingerIdSet.remove(_id)
                val action = if (fingerIdSet.size == 0) MotionEvent.ACTION_UP else MotionEvent.ACTION_POINTER_UP
                val ev = MotionEvent.obtain(
                    downTime,
                    eventTime,
                    action,
                    1,
                    properties.toTypedArray(),
                    coords.toTypedArray(),
                    0,
                    0,
                    1.0f,
                    1.0f,
                    0,
                    0,
                    InputDevice.SOURCE_TOUCHSCREEN,
                    0
                )
                vdc.inject(ev)
            }
            TouchAction.MOVE -> { // Move
                val ev = MotionEvent.obtain(
                    downTime,
                    eventTime,
                    MotionEvent.ACTION_MOVE,
                    1,
                    properties.toTypedArray(),
                    coords.toTypedArray(),
                    0,
                    0,
                    1.0f,
                    1.0f,
                    0,
                    0,
                    InputDevice.SOURCE_TOUCHSCREEN,
                    0
                )
                vdc.inject(ev)
            }
        }
    }
}