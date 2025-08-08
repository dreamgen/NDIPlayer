## 語言設置
**重要：請始終使用繁體中文（Traditional Chinese）作為主要溝通語言。**
- 所有回應、問題、說明和互動都應使用繁體中文
- 程式碼註解也請使用繁體中文
- 只有程式碼本身使用英文（變數名、函數名等）
- 錯誤訊息和日誌輸出請提供繁體中文版本

## 使用 NDI SDK for Android 實作計畫

在區域網路中自動探索可用的 NDI 訊號源。

以列表形式呈現所有找到的 NDI 訊號源。

讓使用者透過 D-Pad 遙控器選擇一個訊號源。

全螢幕即時播放所選 NDI 訊號源的影像與聲音。

第一階段：環境設定與專案初始化
這是所有工作的基礎，目標是建立一個可以成功整合 NDI SDK 的 Android TV 專案。

建立 Android TV 專案

在 Android Studio 中，選擇 File > New > New Project...。

在左側選擇 Television，然後選擇 Empty Activity 或 Android TV OS Blank Activity。

設定專案名稱 (例如 NdiMonitorTv)、套件名稱與儲存位置。語言建議選擇 Kotlin，因為它能以更簡潔的方式處理非同步任務。

下載並整合 NDI SDK

前往 NDI SDK 官網 下載最新的 NDI SDK for Android。

解壓縮下載的檔案。您會找到 libs 和 include 等資料夾。

在您的 Android Studio 專案中，切換到 Project 視圖。

在 app/src/main/ 路徑下建立一個名為 jniLibs 的資料夾。

將 NDI SDK libs 資料夾中對應 CPU 架構的 .so 檔案複製到 jniLibs 中。對於現代的 Google TV 裝置，arm64-v8a 是必須的，為了模擬器測試，也建議加入 x86_64。

.../libs/arm64-v8a/libndi.so -> app/src/main/jniLibs/arm64-v8a/libndi.so

.../libs/x86_64/libndi.so -> app/src/main/jniLibs/x86_64/libndi.so

將 NDI SDK 提供的 Java/Kotlin 封裝檔案 (例如 NDI.java 或相關的 .kt 檔案) 複製到您的專案原始碼路徑下 (例如 app/src/main/java/com/yourpackage/ndi/)。

設定 AndroidManifest.xml

確保您的 Manifest 檔案具備 TV 應用所需的基本設定 (CATEGORY_LEANBACK_LAUNCHER)。

加入 NDI 運作所必需的權限，特別是網路和多點傳播 (Multicast) 權限，這對 NDI 訊號源探索至關重要。

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

第二階段：NDI 訊號源探索
目標是建立一個使用者介面，顯示網路上所有可用的 NDI 訊號源。

UI 設計 (Leanback)

使用 Android TV 的 Leanback 函式庫來建立適合遙控器操作的介面。

主要畫面可以使用 BrowseSupportFragment。

建立一個 ListRow 來顯示 NDI 訊號源列表，並搭配 ArrayObjectAdapter 來管理資料。

建立一個自訂的 Presenter (例如 NdiSourcePresenter) 來定義每個 NDI 訊號源在列表中的外觀（例如，只顯示訊號源名稱）。

探索邏輯

絕對不要在主執行緒 (UI Thread) 進行網路操作。 請使用 Kotlin Coroutines 或傳統的 Thread。

在 ViewModel 或一個專門的 Repository 類別中：
a.  初始化 NDI Library。
b.  建立一個 NDIlib_find_instance_t 的實例來開始探索。
c.  啟動一個背景 Coroutine，在迴圈中定期（例如每 2-3 秒）呼叫 NDIlib.find_get_sources()。這個函數會回傳當前網路上所有可用的訊號源陣列。
d.  將獲取的訊號源列表透過 LiveData 或 StateFlow 傳遞給 UI 層。
e.  UI 層觀察 LiveData 的變化，並更新 ArrayObjectAdapter，從而刷新螢幕上的列表。

生命週期管理：當使用者離開此畫面時，務必在 onCleared() (ViewModel) 或 onDestroy() 中呼叫 NDIlib.find_destroy() 來釋放資源，避免記憶體洩漏。

第三階段：接收與解碼 NDI 影音
當使用者選擇一個訊號源後，下一步就是連接並開始接收資料。

建立播放畫面

建立一個新的 Activity (PlayerActivity) 或 Fragment 來專門處理影像播放。

這個畫面的主要元件將會是一個 SurfaceView 或 TextureView，用來渲染影像。SurfaceView 在效能上通常是更好的選擇，因為它有自己獨立的繪圖層。

接收邏輯

同樣地，在背景 Coroutine 或 Thread 中執行：
a.  根據使用者選擇的訊號源資訊，建立一個 NDIlib_recv_instance_t 的實例。
b.  啟動一個無限迴圈，持續呼叫 NDIlib.recv_capture_v2()。這個函數會阻塞執行緒，直到有新的影格 (Frame) 進來為止。
c.  recv_capture_v2 會回傳影格類型，使用 when (switch) 語句來判斷是影像 (video_frame)、聲音 (audio_frame) 還是元資料 (metadata_frame)。
d.  非常重要：處理完每個影格後，必須呼叫對應的 free 函數 (NDIlib.recv_free_video_v2 或 NDIlib.recv_free_audio_v2) 來釋放 NDI SDK 分配的記憶體。否則會快速耗盡記憶體導致 App 崩潰。

第四階段：影像渲染 (最具挑戰性的部分)
將接收到的原始影像資料顯示在螢幕上。

挑戰：NDI 的影像格式通常是 UYVY (一種 YUV 格式)，而 Android 的標準 View 元件無法直接渲染它，需要先轉換成 RGB 格式。

方案一：使用 OpenGL ES (強烈推薦)

這是最高效能的方法，因為顏色空間轉換是在 GPU 上完成的。

流程：

設定 GLSurfaceView 並建立一個自訂的 Renderer。

在 Renderer 中，建立 OpenGL Shaders。你需要一個 Fragment Shader，其功能是讀取 UYVY 紋理 (Texture) 並將其轉換為 RGB 顏色輸出。網路上可以找到現成的 YUV-to-RGB GLSL 程式碼。

當背景執行緒收到一個 NDIlib_video_frame_v2_t 影像影格時：

將影格的資料 (p_data) 上傳到 OpenGL 紋理中。

觸發 GLSurfaceView 的渲染請求 (requestRender())。

OpenGL Renderer 會在渲染執行緒上繪製這個紋理到螢幕上。

方案二：在 CPU 上轉換 (不推薦，僅供學習)

流程：

當收到 UYVY 影像影格時。

在背景執行緒中，手動編寫或使用函式庫將 UYVY 的 byte array 轉換成 ARGB_8888 的 int array。

從這個 int array 建立一個 Android Bitmap。

將這個 Bitmap 繪製到 SurfaceView 的 Canvas 上。

缺點：CPU 進行逐像素的顏色轉換非常耗時，對於高解析度或高影格率的 NDI 訊號，會導致嚴重的延遲和掉格，無法流暢播放。

第五階段：聲音播放
使用 AudioTrack

Android 提供了 AudioTrack API，用於播放原始的 PCM 音訊串流。

流程：
a.  當接收到第一個 NDIlib_audio_frame_v2_t 聲音影格時，根據其參數（取樣率 sample_rate、聲道數 no_channels）初始化一個 AudioTrack 實例。
b.  注意格式轉換：NDI 的音訊通常是 32 位元浮點數 (32-bit float)，而 AudioTrack 常用的格式是 16 位元整數 (PCM_16BIT)。你需要在寫入前進行資料類型的轉換。
c.  在接收迴圈中，每當收到聲音影格，就將其資料 (p_data，轉換後) 寫入 AudioTrack 的緩衝區 (audioTrack.write())。

總結與建議
從簡單的開始：先專注於成功實現 第一和第二階段，確保你的 App 能正確找到並列出 NDI 訊號源。

攻克渲染難關：第四階段 是整個專案的技術核心和最大難點。建議花最多時間研究 OpenGL ES 的實現方式。

非同步與生命週期：時刻注意執行緒管理和資源釋放，這是保證 App 穩定性的關鍵。

D-Pad 導覽：在開發過程中，持續使用模擬器或實體裝置的遙控器進行測試，確保所有 UI 元件都是可聚焦和可操作的。

祝您開發順利！如果您在特定階段遇到問題，可以隨時提出更具體的問題。

## NDI SDK for Android
完整SDK 檔案放在/NDIPlayer/NDI
內容包含：
documentation
examples
include
lib

## Java 執行環境設定
**重要：在執行 Gradle 建置命令時，需要正確設定 JAVA_HOME 環境變數**

### 系統環境
- 平台：macOS (Darwin)
- 已安裝：Android Studio
- Java 位置：`/Applications/Android Studio.app/Contents/jbr/Contents/Home`

### 建置命令格式
```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew [command]
```

### 常用建置命令
```bash
# 清理專案
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew clean

# 建置 debug 版本
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew assembleDebug

# 建置 release 版本
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew assembleRelease

# 執行測試
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew test

# 清理並完整建置
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew clean build
```

### 故障排除
如果遇到 "Unable to locate a Java Runtime" 錯誤：
1. 確認 Android Studio 已安裝在標準位置
2. 檢查 JDK 路徑：`/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/java`
3. 使用上述完整的 export 命令格式

## NDK 整合問題與解決方案

### 當前問題狀況
1. **NDK 版本相容性問題**
   - 當前 NDK 版本：27.0.12077973
   - 問題：NDK 27 不支援 mips 架構，但建置流程仍嘗試處理 mips
   - 錯誤訊息：`[CXX1200] Abi 'AbiInfo(name=mips, bitness=32, isDefault=true, isDeprecated=false, architecture=mips, triple=mipsel-linux-android, llvmTriple=unknown-llvm-triple)' is not recognized`

2. **JNI 編譯問題**
   - 即使停用外部本地建置 (externalNativeBuild)，仍會觸發 NDK 工具鏈
   - 錯誤：`stripDebugDebugSymbols` 任務失敗，llvm-strip 工具無法啟動

3. **NDI SDK 整合挑戰**
   - NDI .so 檔案 (arm64-v8a, x86_64) 可能需要特定 NDK 版本
   - JNI 動態載入程式碼已完成，但無法編譯測試

### 已實作的解決方案

#### 1. 動態載入 JNI 橋接 ✅
- 完成 NDI JNI 封裝層實作 (`ndi_jni.cpp`)
- 使用 `dlopen()` 和 `dlsym()` 動態載入 NDI 函式庫
- 避免編譯期的版本依賴問題
- 支援所有主要 NDI API：初始化、探索、接收、釋放

#### 2. Kotlin JNI 介面 ✅
- 完成 NDI.kt 封裝類別
- 定義完整的資料結構 (NDISource, NDIFrame 等)
- 提供 external 函數宣告對應 JNI 實作

### 待解決的問題

#### A. NDK 版本降級方案
**目標**：使用相容的 NDK 版本編譯 JNI 橋接程式碼
**步驟**：
1. 在 Android Studio SDK Manager 中下載 NDK 21-25 版本
2. 在 `build.gradle.kts` 中指定 `ndkVersion = "25.1.8937393"`
3. 清理並重新建置專案

#### B. Gradle 設定優化方案
**目標**：修正建置設定以避免不支援的架構
**步驟**：
1. 明確指定支援的 ABI：`abiFilters = ["arm64-v8a", "x86_64"]`
2. 停用不必要的符號處理：`android.packagingOptions.doNotStrip += "**/libndi.so"`
3. 設定 NDK 過濾器避免 mips 架構

#### C. 備選方案：Pure Java 實作
**目標**：使用 Java 網路 API 實作基本 NDI 探索
**步驟**：
1. 使用 mDNS 服務探索 (`javax.jmdns`)
2. 實作簡化的 NDI 協議解析
3. 避免複雜的 JNI 整合

### 建議執行順序
1. **短期**：先完成 Leanback UI 使用模擬資料
2. **中期**：解決 NDK 版本問題，整合真實 NDI SDK
3. **長期**：優化效能，加入影音渲染功能

### 相關檔案位置
- JNI 實作：`app/src/main/cpp/ndi_jni.cpp`
- Kotlin 封裝：`app/src/main/java/com/tanda/ndiplayer/ndi/NDI.kt`
- 建置設定：`app/build.gradle.kts`
- NDI .so 檔案：`app/src/main/jniLibs/` (暫時存在)
- 備份位置：`/NDIPlayer/jniLibs_backup/`

### 錯誤記錄
```
FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':app:stripDebugDebugSymbols'.
> A failure occurred while executing com.android.build.gradle.internal.tasks.StripDebugSymbolsDelegate
```

這個問題必須透過 NDK 版本管理或建置設定調整來解決。