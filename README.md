# Android Virtual Display Captor

## How to Use

Let's try to capture the webview on Presentation.

```kotlin
class WebViewPresentation(context: Context, display: Display) : Presentation(context, display) {

  var url : String? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.content)
    webView.settings.javaScriptEnabled = true
    webView.loadUrl(url)
  }
}
```

VirtualDisplayCaptor captures webview as bitmap.

```kotlin
vdc = VirtualDisplayCaptor(this@MainActivity).apply {
  fps = 30
  width = this@MainActivity.width
  height = this@MainActivity.height
}.invoke<WebViewPresentation>(object : VirtualDisplayCaptor.Callback {
  override fun onCaptured(bitmap: ByteArray) {
    val bytebuffer = ByteBuffer.allocate(bitmap.size)
    bytebuffer.put(bitmap, 0, bitmap.size)
    bytebuffer.rewind()
    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    bmp.copyPixelsFromBuffer(bytebuffer)

    val canvas = surfaceView.holder.lockCanvas()
    canvas.drawBitmap(bmp, 0f, 0f, null)
    surfaceView.holder.unlockCanvasAndPost(canvas)
  }
}) {
  url = "https://www.google.co.jp"
}
```

VirtualDisplayCaptor sends touch event to the presentation.

```kotlin
surfaceView.setOnTouchListener { v, event ->
  vdc?.inject(event)
  true
}
```

VirtualDisplayCaptor has a easy to use class for calling from Unity (WebView Only)

## Reference

### class VirtualDisplayCaptor

```
kotlin.Any
 `- com.xevo.argo.virtualDisplayCaptor.VirtualDisplayCaptor
```

Capture image of Android Presentation or Activity that is rendered on off-screen.

#### Nested classes

|           | Class                                                        |
| --------- | ------------------------------------------------------------ |
| interface | Callback<br /><br />Interface for receiving bitmap which is rendered on off screen. |

#### Constructors

| Constructors                           |      |
| -------------------------------------- | ---- |
| VirtualDisplayCaptor(context: Context) |      |

#### Methods

| Return | Method                                                       |
| ------ | :----------------------------------------------------------- |
| void   | \<reified T: Presentation> invoke(_callback: VirtualDisplayCaptor.Callback, initBlock: T.() -> Unit)<br /><br />Starts to capture the presentation on a virtual display. The presentation is instantiated when  a virtual display created and the initBlock will performed for the presentation. Captured bitmap data is sent via the _callback. |
| void   | \<reified T: Activity> invoke(_callback: VirtualDisplayCaptor.Callback, intent: Intent)<br /><br />Starts to capture the activity on a virtual display. The activity is lauched when  a virtual display created and the intent will be merged the intent to start the activity. Captured bitmap data is sent via the _callback.<br/>[NOTE]: Activity on Virtual Display feature is unstable on Android OS so far. |
| void   | inject(touch: MotionEvent)<br /><br />Notify the presentation/activity of touch events. |
| void   | exit()<br /><br />Finish capture and execution of presentation/activity.<br/>[NOTE]: Not implemented so far. |

#### Properties 

| Type | Property                                      |
| ---- | --------------------------------------------- |
| Int  | width<br/><br/>Width of the virtual display   |
| Int  | height<br/><br/>Height of the virtual display |
| Long | fps<br/><br/>Frame rate for screen capture    |

### inteface VirtualDisplayCaptor.Callback

```
kotlin.Any
 `- com.xevo.argo.virtualDisplayCaptor.VirtualDisplayCaptor.Callback
```

#### Methods

| Return | Method                        |
| ------ | ----------------------------- |
| void   | onCaptured(bitmap: ByteArray) |

## Reference for Unity

These classes are various Android-Native Views for Unity.

### class NativeView

```
kotlin.Any
 `- com.xevo.argo.virtualDisplayCaptor.unity.NativeView
```

#### Nested classes

| Type      | Class                                                        |
| --------- | ------------------------------------------------------------ |
| interface | Callback<br/>Interface for receiving bitmap which is rendered on off screen. |
| enum      | TouchAction<br/>Types of touch action.                       |
| class     | BoxedByteArray<br/>Wrapping `ByteArray` with class for handling byte data in Unity |

#### Methods

| Return | Method                                                       |
| ------ | ------------------------------------------------------------ |
| void   | \<reified T: Presentation> invoke(_width: Int, _height: Int, _fps: Long, _callback: Callback, crossinline _initBlock: T.() -> Unit) |
| void   | injectTouch(_id: Int, touchAction: TouchAction, _x: Float, _y: Float) |

### class WebView

```
kotlin.Any
 `- com.xevo.argo.virtualDisplayCaptor.unity.NativeView
     `- com.xevo.argo.virtualDisplayCaptor.unity.WebView
```

WebView for Unity

#### Methods

| Return | Method                                                       |
| ------ | ------------------------------------------------------------ |
| void   | invoke(width: Int, height: Int, fps: Long, callback: NativeView.Callback) |

#### Properties

| Type   | Property |
| ------ | -------- |
| String | url      |

### class RotatingTriangleView

```
kotlin.Any
 `- com.xevo.argo.virtualDisplayCaptor.unity.NativeView
     `- com.xevo.argo.virtualDisplayCaptor.unity.RotatingTriangleView
```

#### Methods

| Return | Method                                                       |
| ------ | ------------------------------------------------------------ |
| void   | invoke(width: Int, height: Int, fps: Long, callback: NativeView.Callback) |



