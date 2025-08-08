# NDI Player for Android TV

Android TV 上的 NDI 網路視訊串流播放器，支援自動探索和即時播放 NDI 訊號源。

## 專案概述

本專案實作了一個專為 Android TV 設計的 NDI 播放器，能夠：

- 🔍 自動探索區域網路中的 NDI 訊號源
- 📺 以列表形式呈現所有可用的訊號源
- 🎮 支援 D-Pad 遙控器操作
- 📱 全螢幕即時播放 NDI 影音串流
- 🚀 針對 Android TV 平台最佳化

## 分支架構

專案採用多分支架構，提供兩種不同的 NDI 整合方案：

### 📂 main 分支
主分支，包含完整的專案文檔和基本架構。

### 🔧 NativeSDK 分支
使用官方 NDI Native SDK 的高效能實作。

**特色：**
- ✅ 最佳效能 - 直接使用原生 NDI SDK
- ✅ 完整功能 - 支援所有 NDI 特性  
- ✅ JNI 動態載入避免編譯問題
- ❌ 複雜建置設定
- ❌ 較大 APK 尺寸

**適用於：**
- 需要最佳串流效能的應用
- 可接受複雜建置流程的團隊
- 專業級 NDI 功能需求

### ☕ Devolay 分支
使用 Devolay Java 函式庫的簡化實作。

**特色：**
- ✅ 簡化開發 - 純 Java/Kotlin 實作
- ✅ 簡單建置 - 無需 NDK 或 JNI
- ✅ 自動記憶體管理
- ✅ 較小 APK 尺寸
- ❌ 效能稍低於原生實作

**適用於：**
- 快速原型開發
- 避免複雜 native 開發的團隊
- 一般 NDI 串流需求

## 快速開始

### 1. 選擇分支
```bash
# 使用 Native SDK (高效能)
git checkout NativeSDK

# 使用 Devolay (簡單開發)  
git checkout Devolay
```

### 2. 建置專案
```bash
# macOS 環境
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew assembleDebug
```

### 3. 部署到 Android TV
- 連接 Android TV 裝置或模擬器
- 使用 Android Studio 部署 APK
- 確保裝置與 NDI 訊號源在同一網路

## 技術規格

- **最低 Android 版本**：API 21 (Android 5.0)
- **目標 Android 版本**：API 36 (Android 14)
- **開發語言**：Kotlin
- **UI 框架**：Android TV Leanback
- **NDI SDK 版本**：v6.2 (NativeSDK 分支)
- **Devolay 版本**：2.1.0 (Devolay 分支)

## 開發狀態

| 功能 | NativeSDK | Devolay | 狀態 |
|-----|-----------|---------|------|
| 專案架構設定 | ✅ | ✅ | 完成 |
| NDI SDK 整合 | ✅ | ✅ | 完成 |
| 建置系統 | ✅ | ✅ | 完成 |
| Leanback UI | 🔄 | 🔄 | 開發中 |
| 訊號源探索 | ⏳ | ⏳ | 待實作 |
| 影音串流 | ⏳ | ⏳ | 待實作 |
| OpenGL 渲染 | ⏳ | ⏳ | 待實作 |

## 專案結構

```
NDIPlayer/
├── CLAUDE.md              # 詳細開發文檔和問題解決方案
├── README.md               # 本文件
├── README-NativeSDK.md    # NativeSDK 分支說明 (NativeSDK 分支)
├── README-Devolay.md      # Devolay 分支說明 (Devolay 分支)
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/.../MainActivity.kt
│   │   ├── java/.../ndi/NDI.kt    # NDI API 封裝
│   │   ├── cpp/ndi_jni.cpp        # JNI 實作 (NativeSDK 分支)
│   │   ├── jniLibs/               # Native 函式庫 (NativeSDK 分支)
│   │   └── res/                   # Android 資源
│   └── build.gradle.kts
├── NDI/                           # NDI SDK 檔案 (NativeSDK 分支)
└── gradle/
```

## 建置需求

### 通用需求
- Android Studio Arctic Fox 或更新版本
- Kotlin 1.9+
- Gradle 8.0+

### NativeSDK 分支額外需求
- Android NDK 27+
- CMake 3.18+
- C++17 支援

## 網路需求

NDI Player 需要以下網路設定：

- 🌐 與 NDI 訊號源在同一區域網路
- 📡 支援 UDP 多點傳播 (Multicast)
- 🔌 開放 UDP 端口範圍：5960-5999
- 📶 建議使用有線網路以獲得最佳效能

## 授權條款

本專案遵循以下授權：

- **專案程式碼**：MIT License
- **NDI SDK**：遵循 NDI SDK License Agreement
- **Devolay 函式庫**：Apache 2.0 License
- **第三方依賴**：各自的授權條款

## 貢獻指南

歡迎提交 Issue 和 Pull Request：

1. Fork 本專案
2. 選擇適當的分支進行開發
3. 遵循現有的程式碼風格
4. 提交詳細的變更說明
5. 確保所有測試通過

## 相關連結

- [NDI 官方網站](https://ndi.tv/)
- [Devolay GitHub](https://github.com/WalkerKnapp/devolay)
- [Android TV 開發指南](https://developer.android.com/tv)
- [專案 GitHub](https://github.com/dreamgen/NDIPlayer)

---

**最後更新：** 2025年8月8日  
**開發者：** dreamgen@gmail.com  
**協作：** Claude Code