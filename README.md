# Android Virtual Display Captor

## Reference

### public class VirtualDisplayCaptor

```
kotlin.Any
 `- com.xevo.argo.virtualDisplayCaptor.VirtualDisplayCaptor
```

Capture image of Android Presentation or Activity that is rendered on off-screen.

#### Nested classes

|          | Class                                                        |
| -------- | ------------------------------------------------------------ |
| abstract | Callback<br /><br />Interface for receiving bitmap which is rendered on off screen. |
|          | BoxedByteArray<br />v<br />バイト配列を渡すためのclass       |

#### Public constructors

| Constructors                           |      |
| -------------------------------------- | ---- |
| VirtualDisplayCaptor(context: Context) |      |

#### Public methods

| Return | Method                                                       |
| ------ | :----------------------------------------------------------- |
| void   | \<reified T: Presentation> invoke(_callback: VirtualDisplayCaptor.Callback, initBlock: T.() -> Unit)<br /><br />Starts to render a virtual display and passes bitmap through the callback. virtual display を作り、そこで指定された Presentation を instantiate する。fps の間隔で display から bitmap を取得して callback する。 |
| void   | \<reified T: Activity> invoke(_callback: VirtualDisplayCaptor.Callback, intent: Intent)<br /><br />Starts to render a virtual display and passes bitmap through the callback. |
| void   | inject(touch: MotionEvent)<br /><br />Notify the presentation or activity of touch events. |
| void   | exit()<br /><br />Finish capture and execution of presentation. |

### public inteface Callback

```
kotlin.Any
 `- com.xevo.argo.virtualDisplayCaptor.VirtualDisplayCaptor.Callback
```

#### Public Methods

| Return | Method                                                  |
| ------ | ------------------------------------------------------- |
| void   | onCaptured(bitmap: VirtualDisplayCaptor.BoxedByteArray) |

### public class BoxedByteArray

```
kotlin.Any
 `- com.xevo.argo.virtualDisplayCaptor.VirtualDisplayCaptor.BoxedByteArray
```

#### Public Properties

| Type                        | Property  |
| --------------------------- | --------- |
| ByteArray (byte [] in Java) | byteArray |

