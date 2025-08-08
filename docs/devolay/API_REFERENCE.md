# Devolay API 參考文檔

## 核心類別概述

### DevolayFinder
用於探索網路上可用的 NDI 訊號源。

```kotlin
class DevolayFinder : AutoCloseable
```

#### 建構子
```kotlin
DevolayFinder()  // 預設建構子，開始自動探索
```

#### 主要方法
```kotlin
// 取得當前探索到的訊號源列表
val currentSources: Array<DevolaySource>

// 等待指定時間後取得訊號源
fun waitForSources(timeoutMs: Int): Array<DevolaySource>

// 關閉探索器並釋放資源
override fun close()
```

#### 使用範例
```kotlin
val finder = DevolayFinder()
try {
    // 等待 2 秒讓探索完成
    val sources = finder.waitForSources(2000)
    sources.forEach { source ->
        println("發現訊號源: ${source.sourceName}")
    }
} finally {
    finder.close()
}
```

### DevolayReceiver
用於接收 NDI 訊號源的影音資料。

```kotlin
class DevolayReceiver : AutoCloseable
```

#### 建構子
```kotlin
// 基本建構子
DevolayReceiver(source: DevolaySource)

// 完整建構子
DevolayReceiver(
    source: DevolaySource,
    colorFormat: DevolayReceiver.ColorFormat = ColorFormat.BGRX_BGRA,
    receiveBandwidth: Int = DevolayReceiver.RECEIVE_BANDWIDTH_HIGHEST,
    allowVideoFields: Boolean = true,
    name: String? = null
)
```

#### 主要方法
```kotlin
// 擷取影格（視訊、音訊、元資料）
fun receiveCapture(
    videoFrame: DevolayVideoFrame?,
    audioFrame: DevolayAudioFrame?,
    metadataFrame: DevolayMetadataFrame?,
    timeoutMs: Int
): DevolayFrameType

// 設定接收點 (Tally)
fun sendTally(tally: DevolayTally)

// 關閉接收器
override fun close()
```

#### 接收頻寬選項
```kotlin
companion object {
    const val RECEIVE_BANDWIDTH_METADATA_ONLY = -10
    const val RECEIVE_BANDWIDTH_AUDIO_ONLY = 10
    const val RECEIVE_BANDWIDTH_LOWEST = 0
    const val RECEIVE_BANDWIDTH_HIGHEST = 100
}
```

### DevolaySource
代表一個 NDI 訊號源的資訊。

```kotlin
data class DevolaySource(
    val sourceName: String,     // 訊號源名稱
    val urlAddress: String      // 網路位址
)
```

#### 主要屬性
```kotlin
val sourceName: String    // 顯示名稱，如 "電腦名稱 (應用程式名稱)"
val urlAddress: String    // IP 位址和端口，如 "192.168.1.100:5961"
```

## 影格類別

### DevolayVideoFrame
包含視訊影格資料。

```kotlin
class DevolayVideoFrame
```

#### 主要屬性
```kotlin
val width: Int              // 影像寬度
val height: Int             // 影像高度
val stride: Int             // 每行位元組數
val frameRateN: Int         // 影格率分子
val frameRateD: Int         // 影格率分母
val fourCC: Int             // 像素格式識別碼
val data: ByteBuffer        // 影像資料緩衝區
val timestamp: Long         // 時間戳記
```

#### 常用像素格式
```kotlin
companion object {
    const val FOURCC_BGRX = 0x58524742    // 32位元 BGRX
    const val FOURCC_BGRA = 0x41524742    // 32位元 BGRA  
    const val FOURCC_RGBX = 0x58424752    // 32位元 RGBX
    const val FOURCC_RGBA = 0x41424752    // 32位元 RGBA
    const val FOURCC_UYVY = 0x59565955    // YUV 4:2:2
}
```

### DevolayAudioFrame
包含音訊影格資料。

```kotlin
class DevolayAudioFrame
```

#### 主要屬性
```kotlin
val sampleRate: Int         // 取樣率 (Hz)
val channels: Int           // 聲道數
val samples: Int            // 樣本數
val channelStride: Int      // 聲道間距
val data: ByteBuffer        // 音訊資料 (32位元浮點數)
val timestamp: Long         // 時間戳記
```

### DevolayFrameType
表示擷取到的影格類型。

```kotlin
enum class DevolayFrameType {
    NONE,           // 無影格
    VIDEO,          // 視訊影格
    AUDIO,          // 音訊影格
    METADATA,       // 元資料影格
    ERROR,          // 錯誤
    STATUS_CHANGE   // 狀態變更
}
```

## Android 專用考量

### NsdManager 需求
在 Android 上使用 Devolay 時，需要提供 NsdManager 實例：

```kotlin
class NDIManager(private val context: Context) {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    
    fun initializeDevolay() {
        // NsdManager 會自動被 Devolay 使用
    }
}
```

### 執行緒安全性
Devolay 的大部分操作都不是執行緒安全的，建議：

```kotlin
class NDIHandler {
    private val ndiExecutor = Executors.newSingleThreadExecutor()
    
    fun performNDIOperation(operation: () -> Unit) {
        ndiExecutor.execute(operation)
    }
}
```

### 記憶體管理最佳實務

```kotlin
class NDIResourceManager {
    private var finder: DevolayFinder? = null
    private var receiver: DevolayReceiver? = null
    
    fun cleanup() {
        // 正確順序：先關閉接收器，再關閉探索器
        receiver?.close()
        receiver = null
        
        finder?.close()
        finder = null
    }
}
```

## 錯誤處理模式

### 連線錯誤處理
```kotlin
fun connectWithRetry(source: DevolaySource, maxRetries: Int = 3): Boolean {
    repeat(maxRetries) { attempt ->
        try {
            receiver = DevolayReceiver(source)
            return true
        } catch (e: Exception) {
            Log.w(TAG, "連線失敗，嘗試 ${attempt + 1}/$maxRetries", e)
            Thread.sleep(1000) // 等待後重試
        }
    }
    return false
}
```

### 影格接收錯誤處理
```kotlin
fun captureFrameSafely(): DevolayVideoFrame? {
    return try {
        val videoFrame = DevolayVideoFrame()
        val result = receiver?.receiveCapture(videoFrame, null, null, 1000)
        
        when (result) {
            DevolayFrameType.VIDEO -> videoFrame
            DevolayFrameType.NONE -> null
            DevolayFrameType.ERROR -> {
                Log.e(TAG, "接收影格時發生錯誤")
                null
            }
            else -> null
        }
    } catch (e: Exception) {
        Log.e(TAG, "影格擷取異常", e)
        null
    }
}
```

## 效能最佳化

### 影格緩衝策略
```kotlin
class FrameBuffer {
    private val frameQueue = ArrayDeque<DevolayVideoFrame>()
    private val maxBufferSize = 3
    
    fun addFrame(frame: DevolayVideoFrame) {
        if (frameQueue.size >= maxBufferSize) {
            frameQueue.removeFirst() // 丟棄舊影格
        }
        frameQueue.addLast(frame)
    }
    
    fun getLatestFrame(): DevolayVideoFrame? {
        return frameQueue.lastOrNull()
    }
}
```

### 資源池模式
```kotlin
class VideoFramePool {
    private val pool = ConcurrentLinkedQueue<DevolayVideoFrame>()
    
    fun borrowFrame(): DevolayVideoFrame {
        return pool.poll() ?: DevolayVideoFrame()
    }
    
    fun returnFrame(frame: DevolayVideoFrame) {
        // 重置影格狀態後歸還到池中
        pool.offer(frame)
    }
}
```

## 常數和設定值

### 網路設定
```kotlin
object NDIConstants {
    const val DEFAULT_DISCOVERY_TIMEOUT = 5000    // 5 秒探索超時
    const val DEFAULT_RECEIVE_TIMEOUT = 1000      // 1 秒接收超時
    const val DEFAULT_PORT_RANGE = 5960..5999     // NDI 使用的端口範圍
}
```

### 影格率常數
```kotlin
object FrameRates {
    const val FPS_23_98 = Pair(24000, 1001)
    const val FPS_24 = Pair(24, 1)
    const val FPS_25 = Pair(25, 1)
    const val FPS_29_97 = Pair(30000, 1001)
    const val FPS_30 = Pair(30, 1)
    const val FPS_50 = Pair(50, 1)
    const val FPS_59_94 = Pair(60000, 1001)
    const val FPS_60 = Pair(60, 1)
}
```

這份 API 參考文檔提供了 Devolay 在 Android 環境中使用的完整指南，包括所有重要類別、方法、錯誤處理和效能最佳化策略。