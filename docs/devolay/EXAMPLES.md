# Devolay 實作範例

## 基本範例：NDI 訊號源探索

### 簡單探索範例

```kotlin
package com.tanda.ndiplayer.examples

import me.walkerknapp.devolay.*
import android.content.Context
import android.net.nsd.NsdManager
import android.util.Log

class BasicDiscoveryExample(private val context: Context) {
    private val TAG = "BasicDiscovery"
    private lateinit var nsdManager: NsdManager
    
    fun discoverNDISources(): List<DevolaySource> {
        // 1. 設定 NsdManager (Android 必需)
        nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
        
        var finder: DevolayFinder? = null
        
        return try {
            // 2. 建立探索器
            finder = DevolayFinder()
            Log.i(TAG, "NDI Finder 已啟動")
            
            // 3. 等待探索完成
            Thread.sleep(3000) // 等待 3 秒
            
            // 4. 取得發現的訊號源
            val sources = finder.currentSources
            Log.i(TAG, "發現 ${sources.size} 個 NDI 訊號源")
            
            sources.forEach { source ->
                Log.i(TAG, "訊號源: ${source.sourceName} @ ${source.urlAddress}")
            }
            
            sources.toList()
            
        } catch (e: Exception) {
            Log.e(TAG, "探索過程發生錯誤", e)
            emptyList()
        } finally {
            // 5. 清理資源
            finder?.close()
        }
    }
}
```

## 中級範例：協程式探索管理器

```kotlin
package com.tanda.ndiplayer.examples

import me.walkerknapp.devolay.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import android.content.Context
import android.net.nsd.NsdManager
import android.util.Log

class CoroutineNDIDiscovery(private val context: Context) {
    private val TAG = "CoroutineNDI"
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    
    private var finder: DevolayFinder? = null
    private val discoveryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // StateFlow 用於發送探索結果
    private val _sourcesFlow = MutableStateFlow<List<DevolaySource>>(emptyList())
    val sourcesFlow: StateFlow<List<DevolaySource>> = _sourcesFlow.asStateFlow()
    
    fun startDiscovery() {
        discoveryScope.launch {
            try {
                finder = DevolayFinder()
                Log.i(TAG, "開始 NDI 探索...")
                
                // 定期更新訊號源列表
                while (isActive) {
                    val sources = finder?.currentSources?.toList() ?: emptyList()
                    _sourcesFlow.value = sources
                    
                    Log.d(TAG, "更新訊號源列表: ${sources.size} 個源")
                    delay(2000) // 每 2 秒更新一次
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "探索過程錯誤", e)
                _sourcesFlow.value = emptyList()
            }
        }
    }
    
    fun stopDiscovery() {
        discoveryScope.cancel()
        finder?.close()
        finder = null
        _sourcesFlow.value = emptyList()
        Log.i(TAG, "NDI 探索已停止")
    }
}
```

## 高級範例：完整 NDI 接收器

```kotlin
package com.tanda.ndiplayer.examples

import me.walkerknapp.devolay.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import java.nio.ByteBuffer

class NDIReceiverExample(private val context: Context) {
    private val TAG = "NDIReceiver"
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    
    private var receiver: DevolayReceiver? = null
    private val receiverScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // 影格資料通道
    private val frameChannel = Channel<VideoFrameData>(Channel.UNLIMITED)
    
    data class VideoFrameData(
        val width: Int,
        val height: Int,
        val data: ByteArray,
        val timestamp: Long
    )
    
    fun connectToSource(source: DevolaySource): Boolean {
        return try {
            receiver = DevolayReceiver(
                source,
                DevolayReceiver.ColorFormat.BGRX_BGRA,
                DevolayReceiver.RECEIVE_BANDWIDTH_HIGHEST,
                false, // 不允許場掃描
                "NDIPlayer接收器"
            )
            
            Log.i(TAG, "已連線到訊號源: ${source.sourceName}")
            startReceiving()
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "連線失敗", e)
            false
        }
    }
    
    private fun startReceiving() {
        receiverScope.launch {
            val videoFrame = DevolayVideoFrame()
            val audioFrame = DevolayAudioFrame()
            
            while (isActive && receiver != null) {
                try {
                    val frameType = receiver!!.receiveCapture(
                        videoFrame, 
                        audioFrame, 
                        null, 
                        1000 // 1 秒超時
                    )
                    
                    when (frameType) {
                        DevolayFrameType.VIDEO -> {
                            processVideoFrame(videoFrame)
                        }
                        DevolayFrameType.AUDIO -> {
                            processAudioFrame(audioFrame)
                        }
                        DevolayFrameType.NONE -> {
                            // 超時，繼續等待
                            delay(16) // ~60fps
                        }
                        DevolayFrameType.ERROR -> {
                            Log.w(TAG, "接收錯誤，嘗試重新連線")
                            delay(1000)
                        }
                        else -> {
                            // 其他類型的影格
                            delay(16)
                        }
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "接收循環錯誤", e)
                    delay(1000)
                }
            }
        }
    }
    
    private suspend fun processVideoFrame(frame: DevolayVideoFrame) {
        try {
            val frameData = VideoFrameData(
                width = frame.width,
                height = frame.height,
                data = ByteArray(frame.data.remaining()).also { 
                    frame.data.get(it) 
                },
                timestamp = frame.timestamp
            )
            
            frameChannel.trySend(frameData)
            Log.d(TAG, "處理視訊影格: ${frame.width}x${frame.height}")
            
        } catch (e: Exception) {
            Log.w(TAG, "視訊影格處理錯誤", e)
        }
    }
    
    private fun processAudioFrame(frame: DevolayAudioFrame) {
        try {
            Log.d(TAG, "處理音訊影格: ${frame.sampleRate}Hz, ${frame.channels}聲道, ${frame.samples}樣本")
            // TODO: 實作音訊播放邏輯
            
        } catch (e: Exception) {
            Log.w(TAG, "音訊影格處理錯誤", e)
        }
    }
    
    // 取得最新的視訊影格
    suspend fun getLatestVideoFrame(): VideoFrameData? {
        return try {
            frameChannel.tryReceive().getOrNull()
        } catch (e: Exception) {
            Log.w(TAG, "取得影格失敗", e)
            null
        }
    }
    
    fun disconnect() {
        receiverScope.cancel()
        receiver?.close()
        receiver = null
        frameChannel.close()
        Log.i(TAG, "NDI 接收器已中斷連線")
    }
}
```

## Android TV 整合範例

```kotlin
package com.tanda.ndiplayer.examples

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.walkerknapp.devolay.*
import android.app.Application
import android.util.Log

class NDIViewModel(private val application: Application) : ViewModel() {
    private val TAG = "NDIViewModel"
    
    private val discovery = CoroutineNDIDiscovery(application)
    private val receiver = NDIReceiverExample(application)
    
    // UI 狀態
    private val _uiState = MutableStateFlow(NDIUiState())
    val uiState: StateFlow<NDIUiState> = _uiState.asStateFlow()
    
    data class NDIUiState(
        val isDiscovering: Boolean = false,
        val sources: List<DevolaySource> = emptyList(),
        val selectedSource: DevolaySource? = null,
        val isConnected: Boolean = false,
        val errorMessage: String? = null
    )
    
    init {
        // 監聽探索結果
        viewModelScope.launch {
            discovery.sourcesFlow.collect { sources ->
                _uiState.value = _uiState.value.copy(
                    sources = sources
                )
            }
        }
    }
    
    fun startDiscovery() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDiscovering = true,
                errorMessage = null
            )
            
            try {
                discovery.startDiscovery()
                Log.i(TAG, "開始探索 NDI 訊號源")
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDiscovering = false,
                    errorMessage = "探索失敗: ${e.message}"
                )
                Log.e(TAG, "探索啟動失敗", e)
            }
        }
    }
    
    fun stopDiscovery() {
        discovery.stopDiscovery()
        _uiState.value = _uiState.value.copy(
            isDiscovering = false
        )
        Log.i(TAG, "停止探索")
    }
    
    fun connectToSource(source: DevolaySource) {
        viewModelScope.launch {
            try {
                val success = receiver.connectToSource(source)
                
                _uiState.value = _uiState.value.copy(
                    selectedSource = source,
                    isConnected = success,
                    errorMessage = if (success) null else "連線失敗"
                )
                
                if (success) {
                    Log.i(TAG, "成功連線到: ${source.sourceName}")
                } else {
                    Log.w(TAG, "連線失敗: ${source.sourceName}")
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isConnected = false,
                    errorMessage = "連線錯誤: ${e.message}"
                )
                Log.e(TAG, "連線過程錯誤", e)
            }
        }
    }
    
    fun disconnect() {
        receiver.disconnect()
        _uiState.value = _uiState.value.copy(
            selectedSource = null,
            isConnected = false
        )
        Log.i(TAG, "已中斷連線")
    }
    
    override fun onCleared() {
        super.onCleared()
        stopDiscovery()
        disconnect()
        Log.i(TAG, "ViewModel 清理完成")
    }
    
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return NDIViewModel(application) as T
        }
    }
}
```

## 錯誤處理和恢復範例

```kotlin
package com.tanda.ndiplayer.examples

import me.walkerknapp.devolay.*
import kotlinx.coroutines.*
import android.content.Context
import android.util.Log

class ResilientNDIManager(private val context: Context) {
    private val TAG = "ResilientNDI"
    
    private var currentReceiver: DevolayReceiver? = null
    private var retryJob: Job? = null
    private val maxRetries = 3
    private val retryDelayMs = 5000L
    
    // 帶重試的連線功能
    suspend fun connectWithRetry(source: DevolaySource): Boolean {
        repeat(maxRetries) { attempt ->
            try {
                currentReceiver?.close()
                currentReceiver = DevolayReceiver(source)
                
                Log.i(TAG, "連線成功 (第 ${attempt + 1} 次嘗試): ${source.sourceName}")
                return true
                
            } catch (e: Exception) {
                Log.w(TAG, "連線失敗 (第 ${attempt + 1} 次嘗試): ${e.message}")
                
                if (attempt < maxRetries - 1) {
                    delay(retryDelayMs)
                }
            }
        }
        
        Log.e(TAG, "所有重試都失敗了")
        return false
    }
    
    // 自動重連機制
    fun startAutoReconnect(source: DevolaySource) {
        retryJob?.cancel()
        retryJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    // 檢查連線狀態
                    if (!isConnectionHealthy()) {
                        Log.w(TAG, "檢測到連線問題，嘗試重連...")
                        connectWithRetry(source)
                    }
                    
                    delay(10000) // 每 10 秒檢查一次
                    
                } catch (e: Exception) {
                    Log.e(TAG, "自動重連檢查錯誤", e)
                    delay(retryDelayMs)
                }
            }
        }
    }
    
    private fun isConnectionHealthy(): Boolean {
        return try {
            val receiver = currentReceiver ?: return false
            
            // 嘗試接收一個影格來測試連線
            val result = receiver.receiveCapture(null, null, null, 100)
            result != DevolayFrameType.ERROR
            
        } catch (e: Exception) {
            Log.d(TAG, "連線健康檢查失敗: ${e.message}")
            false
        }
    }
    
    fun stopAutoReconnect() {
        retryJob?.cancel()
        retryJob = null
        currentReceiver?.close()
        currentReceiver = null
        Log.i(TAG, "自動重連已停止")
    }
}
```

## 效能監控範例

```kotlin
package com.tanda.ndiplayer.examples

import me.walkerknapp.devolay.*
import android.util.Log
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicLong

class NDIPerformanceMonitor {
    private val TAG = "NDIPerformance"
    
    private val frameCount = AtomicLong(0)
    private val droppedFrames = AtomicLong(0)
    private val totalBytes = AtomicLong(0)
    private var startTime = System.currentTimeMillis()
    
    fun onFrameReceived(frame: DevolayVideoFrame) {
        frameCount.incrementAndGet()
        totalBytes.addAndGet(frame.data.remaining().toLong())
    }
    
    fun onFrameDropped() {
        droppedFrames.incrementAndGet()
    }
    
    fun startPerformanceReporting() {
        CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                delay(5000) // 每 5 秒報告一次
                reportPerformance()
            }
        }
    }
    
    private fun reportPerformance() {
        val currentTime = System.currentTimeMillis()
        val elapsedSeconds = (currentTime - startTime) / 1000.0
        
        val fps = frameCount.get() / elapsedSeconds
        val dropRate = droppedFrames.get().toDouble() / frameCount.get() * 100
        val mbps = (totalBytes.get() * 8) / (elapsedSeconds * 1024 * 1024)
        
        Log.i(TAG, "效能統計:")
        Log.i(TAG, "  FPS: %.2f".format(fps))
        Log.i(TAG, "  丟幀率: %.2f%%".format(dropRate))
        Log.i(TAG, "  頻寬: %.2f Mbps".format(mbps))
        Log.i(TAG, "  總影格數: ${frameCount.get()}")
        Log.i(TAG, "  丟失影格: ${droppedFrames.get()}")
    }
    
    fun reset() {
        frameCount.set(0)
        droppedFrames.set(0)
        totalBytes.set(0)
        startTime = System.currentTimeMillis()
    }
}
```

這些範例展示了從基本探索到高級效能監控的完整 Devolay 使用模式，涵蓋了 Android TV NDI 應用開發的各個層面。