# Presentation Captor

## Reference

### public class PresentationCaptor

```
kotlin.Any
 `- com.xevo.argo.PresentationCaptor
```

Capture image of Android Presentation that is rendered on off-screen.

#### Nested classes

|          | Class                                                        |
| -------- | ------------------------------------------------------------ |
| abstract | Callback<br /><br />Interface for receiving bitmap which is rendered on off screen. |
|          | BoxedByteArray<br /><br />バイト配列を渡すためのclass        |

#### Public methods

| Return | Method                                                       |
| ------ | :----------------------------------------------------------- |
| void   | invoke(presentationClassName: String, displaySize: Size, fps: Long, callback: PresentationCaptor.Callback)<br /><br />Starts to render a virtual display and passes bitmap through the callback. |
| void   | invoke(presentationClassName: String, activity: Activity, displaySize: Size, fps: Long, callback: PresentationCaptor.Callback)<br /><br />Starts to render a virtual display and passes bitmap through the callback. |
| void   | inject(presentation: Presentation, touch: MotionEvent)<br /><br />Notify the presentation of touch events. |
| void   | injectTouch(presentation: Presentation, x1: Int, y1: Int, x2: Int, y2: Int)<br /><br />Notify the presentation of touch events. |
| void   | exit(presentation: Presentation)<br /><br />Finish capture and execution of presentation. |

### public inteface Callback

```
kotlin.Any
 `- com.xevo.argo.PresentationCaptor.Callback
```

#### Public Methods

| Return | Method                                                       |
| ------ | ------------------------------------------------------------ |
| void   | onCreated(presentation: Presentation)                        |
| void   | onCaptured(presentation: Presentation, bitmap: PresentationCaptor.BoxedByteArray) |

### public class BoxedByteArray

```
kotlin.Any
 `- com.xevo.argo.PresentationCaptor.BoxedByteArray
```

#### Public Properties

| Type                        | Property  |
| --------------------------- | --------- |
| ByteArray (byte [] in Java) | byteArray |

