# Devolay Android 設定指南

## Devolay 概述

Devolay 是 NewTek NDI SDK 的 Java 封裝函式庫，提供跨平台的 NDI 視訊串流功能。

### 核心特色
- ☕ 純 Java API，遵循 Java 慣例
- 📱 完整 Android 支援（包含 NDI SDK 二進制檔案）
- 🔄 接近原生 NDI SDK 的功能對應
- 🚀 自動記憶體管理
- 🌐 跨平台支援（Windows、Linux、macOS、Android）

## Android 專案設定

### 1. Gradle 依賴設定

在 `app/build.gradle.kts` 中加入：

```kotlin
dependencies {
    // Devolay - Java NDI 函式庫（Android 版本）
    implementation("me.walkerknapp:devolay:2.1.1") {
        artifact {
            name = "devolay"
            type = "aar"
        }
    }
    
    // 網路探索支援
    implementation("org.jmdns:jmdns:3.5.8")
}
```

### 2. Android 權限設定

在 `AndroidManifest.xml` 中加入必要權限：

```xml
<!-- NDI 網路通訊權限 -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_MULTICAST_STATE" />

<!-- Android TV 支援 -->
<uses-feature
    android:name="android.software.leanback"
    android:required="true" />
<uses-feature
    android:name="android.hardware.touchscreen"
    android:required="false" />
```

### 3. NsdManager 設定

Devolay 在 Android 上需要 NsdManager 實例：

```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var nsdManager: NsdManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 設定 NsdManager
        nsdManager = getSystemService(Context.NSD_SERVICE) as NsdManager
        
        // 在建立 DevolayFinder/DevolayReceiver 之前設定
        setupNDI()
    }
}
```

## 基本使用模式

### 1. NDI 訊號源探索

```kotlin
class NDIManager {
    private var finder: DevolayFinder? = null
    
    fun startDiscovery(): List<DevolaySource> {
        finder = DevolayFinder()
        
        // 等待探索完成
        Thread.sleep(1000)
        
        // 取得當前可用的訊號源
        return finder?.currentSources?.toList() ?: emptyList()
    }
    
    fun stopDiscovery() {
        finder?.close()
        finder = null
    }
}
```

### 2. NDI 影音接收

```kotlin
class NDIReceiver {
    private var receiver: DevolayReceiver? = null
    
    fun connectToSource(source: DevolaySource) {
        receiver = DevolayReceiver(source)
    }
    
    fun captureFrame(): DevolayVideoFrame? {
        receiver?.let { recv ->
            val videoFrame = DevolayVideoFrame()
            val result = recv.receiveCapture(videoFrame, null, null, 0)
            
            return if (result == DevolayFrameType.VIDEO) {
                videoFrame
            } else {
                null
            }
        }
        return null
    }
    
    fun disconnect() {
        receiver?.close()
        receiver = null
    }
}
```

## 重要注意事項

### Android 模擬器限制
- ❌ **不支援 x86_64 模擬器**：NDI 在 x86_64 Android 模擬器上有已知問題
- ✅ **可用替代方案**：
  - x86 模擬器
  - ARM 模擬器
  - 實體裝置（推薦）

### 記憶體管理
Devolay 提供自動記憶體管理，但仍需正確關閉資源：

```kotlin
// 正確的資源清理
finder?.close()
receiver?.close()
```

### 執行緒考量
- NDI 探索和接收應在背景執行緒進行
- UI 更新必須在主執行緒執行

```kotlin
// 使用協程進行 NDI 操作
class NDIViewModel : ViewModel() {
    fun discoverSources() {
        viewModelScope.launch(Dispatchers.IO) {
            val sources = ndiManager.startDiscovery()
            
            withContext(Dispatchers.Main) {
                // 更新 UI
                sourcesLiveData.value = sources
            }
        }
    }
}
```

## 授權考量

### NDI SDK 授權
使用 Devolay Android 建置版本需遵循 NDI SDK 文檔第 5.2 節的指導原則：

1. 產品中必須包含適當的 NDI 商標聲明
2. 需要連結到 NDI 官方網站
3. 遵循 NDI 品牌使用規範

### 範例聲明
```
此應用程式使用 NDI® 技術。NDI® 是 Vizrt Group 的註冊商標。
更多資訊請訪問：https://ndi.tv/
```

## 故障排除

### 常見問題

1. **UnsatisfiedLinkError**
   - 確認使用正確的 AAR 依賴格式
   - 檢查 Android 架構相容性

2. **訊號源探索失敗**
   - 確認網路權限已設定
   - 檢查 NsdManager 是否正確初始化
   - 確保裝置與 NDI 源在同一網路

3. **接收影格失敗**
   - 檢查訊號源是否仍在線上
   - 確認網路頻寬足夠
   - 驗證防火牆設定

### 除錯技巧

```kotlin
// 啟用詳細日誌
private const val TAG = "NDI_Devolay"

Log.d(TAG, "Devolay 版本: ${DevolayVersion.version}")
Log.d(TAG, "找到 ${sources.size} 個 NDI 訊號源")
```

## 效能最佳化

1. **適當的執行緒池**：為 NDI 操作使用專門的執行緒
2. **資源重用**：避免頻繁建立/銷毀 DevolayReceiver
3. **影格緩衝**：實作適當的影格緩衝機制
4. **網路最佳化**：使用有線網路以獲得最佳效能

## 下一步

完成基本設定後，可以繼續實作：

1. Leanback UI 整合
2. 即時影像渲染
3. 音訊播放支援
4. 錯誤恢復機制