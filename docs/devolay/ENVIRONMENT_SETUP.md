# Devolay 開發環境設定完整指南

## 環境準備狀態 ✅

本專案（Devolay 分支）已完成 NDI 開發環境的基礎設定，可以開始進行小型 NDI 測試和實作。

### 已完成的設定項目

#### 1. Gradle 依賴配置 ✅
```kotlin
// app/build.gradle.kts
dependencies {
    // Devolay - Java NDI 函式庫（Android 版本）
    implementation("me.walkerknapp:devolay:2.1.0")
    
    // 網路探索支援
    implementation("org.jmdns:jmdns:3.5.8")
    
    // Android TV Leanback 支援
    implementation("androidx.leanback:leanback:1.0.0")
    implementation("androidx.leanback:leanback-preference:1.0.0")
    
    // 其他必要依賴已配置...
}
```

#### 2. Android 權限設定 ✅
```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_MULTICAST_STATE" />

<uses-feature
    android:name="android.software.leanback"
    android:required="true" />
<uses-feature
    android:name="android.hardware.touchscreen"
    android:required="false" />
```

#### 3. 核心類別架構 ✅
- **NDI.kt** - Devolay 封裝類別，提供統一的 NDI API
- **SimpleDevolayTest.kt** - 基礎功能測試工具
- **MainActivity.kt** - 整合測試和初始化邏輯

#### 4. 文檔系統 ✅
- **DEVOLAY_SETUP.md** - 詳細設定指南
- **API_REFERENCE.md** - 完整 API 參考文檔
- **EXAMPLES.md** - 實用範例程式碼集
- **ENVIRONMENT_SETUP.md** - 本文件

## 目前功能狀態

### ✅ 已實作功能
1. **Devolay 函式庫整合** - AAR 依賴正確配置
2. **基本 NDI 封裝** - 統一的 Kotlin API
3. **測試工具** - 自動化環境驗證
4. **錯誤處理** - 完整的異常處理機制
5. **文檔系統** - 詳細的開發指南

### 🔄 部分實作功能
1. **NDI 探索** - 基礎框架完成，需要實際測試
2. **連線管理** - 架構就緒，需要實際 NDI 源測試
3. **生命週期管理** - Android 整合完成

### ⏳ 待實作功能
1. **Leanback UI** - Android TV 使用者介面
2. **影音渲染** - OpenGL ES 視訊顯示
3. **音訊播放** - AudioTrack 整合
4. **效能最佳化** - 緩衝和同步機制

## 測試環境需求

### 網路環境
- 📡 WiFi 或有線網路連接
- 🌐 與 NDI 發送端在同一區域網路
- 🔓 防火牆允許 UDP 多播流量（端口 5960-5999）

### NDI 發送端選項
為了測試 NDI 接收功能，需要準備一個 NDI 發送端：

#### 選項 1：NDI Tools（推薦）
```
1. 下載 NDI Tools: https://ndi.tv/tools/
2. 安裝並執行 "NDI Screen Capture"
3. 開始螢幕捕獲，創建一個測試訊號源
```

#### 選項 2：OBS Studio + NDI Plugin
```
1. 安裝 OBS Studio
2. 安裝 OBS NDI Plugin
3. 設定 NDI Output
```

#### 選項 3：NDI Test Patterns
```
1. 使用 NDI Tools 中的 "NDI Test Patterns"
2. 產生標準測試圖案訊號
```

### 測試裝置
- ✅ **實體 Android 裝置**（推薦）
- ✅ **ARM Android 模擬器**
- ✅ **x86 Android 模擬器**
- ❌ **x86_64 Android 模擬器**（已知不支援）

## 快速測試步驟

### 1. 建置專案
```bash
# 確保 Java 環境正確
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"

# 建置 Debug 版本
./gradlew assembleDebug
```

### 2. 部署和測試
```bash
# 連接 Android 裝置或模擬器
adb devices

# 安裝 APK
./gradlew installDebug

# 檢視 NDI 測試日誌
adb logcat | grep -E "(MainActivity|SimpleDevolayTest|NDI_Devolay)"
```

### 3. 預期測試結果
應用程式啟動後會自動執行以下測試：

```
✅ Devolay 函式庫載入成功
📦 準備進行 NDI SDK 測試...
✅ DevolayFinder 建立成功
📡 探索器初始化完成，當前找到 X 個訊號源
🔍 開始 NDI 訊號源探索...
📊 探索第 N 次: 找到 X 個訊號源
  📺 訊號源 1: [源名稱]
      位址: [IP:端口]
```

## 常見問題解決

### 問題 1: 找不到任何 NDI 訊號源
**症狀**: 探索結果顯示 0 個訊號源

**解決方案**:
```
1. 確認 NDI 發送端正在運作
2. 檢查網路連接 - 確保在同一區域網路
3. 檢查防火牆設定
4. 嘗試重啟應用程式
5. 檢查 Android 網路權限
```

### 問題 2: Devolay 函式庫載入失敗
**症狀**: UnsatisfiedLinkError 或類似錯誤

**解決方案**:
```
1. 確認使用實體裝置或支援的模擬器
2. 檢查 Gradle 依賴配置
3. 清理並重建專案: ./gradlew clean build
4. 檢查 NDK 版本相容性
```

### 問題 3: 應用程式崩潰
**症狀**: 應用程式啟動後立即關閉

**解決方案**:
```
1. 檢查 Logcat 錯誤訊息
2. 確認所有權限已正確設定
3. 檢查 NsdManager 初始化
4. 嘗試在背景執行緒中進行 NDI 操作
```

## 開發建議

### 1. 除錯最佳實務
```kotlin
// 啟用詳細日誌
private const val TAG = "NDI_Debug"
Log.setProperty("log.tag.$TAG", "VERBOSE")

// 使用結構化日誌
Log.d(TAG, "NDI 狀態: finder=${finder != null}, sources=${sources.size}")
```

### 2. 錯誤處理模式
```kotlin
try {
    // NDI 操作
} catch (e: Exception) {
    Log.e(TAG, "NDI 操作失敗: ${e.message}", e)
    // 提供使用者友善的錯誤訊息
    showUserError("NDI 連線問題，請檢查網路設定")
}
```

### 3. 資源管理
```kotlin
// 使用 try-with-resources 模式
DevolayFinder().use { finder ->
    // 使用 finder
} // 自動關閉
```

## 下一步開發計劃

### Phase 1: 核心功能驗證
- [ ] 在真實 NDI 環境中測試探索功能
- [ ] 驗證 DevolayReceiver 連線能力
- [ ] 實作基本的影格接收循環

### Phase 2: UI 整合
- [ ] 建立 Leanback Fragment 顯示訊號源列表
- [ ] 加入 D-Pad 導覽支援
- [ ] 實作訊號源選擇邏輯

### Phase 3: 影音播放
- [ ] 整合 OpenGL ES 進行視訊渲染
- [ ] 加入 AudioTrack 音訊播放
- [ ] 實作同步播放機制

---

**環境設定完成度: 85%**  
**準備開始小型 NDI 測試: ✅**  
**建議下一步**: 在實際 NDI 環境中測試探索和連線功能