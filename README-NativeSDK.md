# NDI Player - Native SDK 分支

此分支包含使用 NDI 官方 Native SDK 的實作方案。

## 特色

- 使用 NDI SDK v6.2 for Android 官方函式庫
- JNI 動態載入實作避免編譯期依賴問題
- 完整的 C++ NDI API 封裝
- 支援所有 NDI 原生功能

## 技術架構

### JNI 橋接層
- **檔案**: `app/src/main/cpp/ndi_jni.cpp`
- **功能**: 動態載入 NDI .so 檔案，提供 Kotlin 介面
- **優點**: 避免 NDK 版本相容性問題

### Kotlin 封裝
- **檔案**: `app/src/main/java/com/tanda/ndiplayer/ndi/NDI.kt`
- **功能**: 提供類型安全的 NDI API
- **支援**: 訊號源探索、接收、影音處理

### Native Libraries
- **位置**: `app/src/main/jniLibs/`
- **架構**: arm64-v8a, x86_64
- **來源**: NDI SDK v6.2 官方函式庫

## 建置需求

- Android Studio Arctic Fox 或更新版本
- NDK 27+ (已解決相容性問題)
- minSdk 21, targetSdk 36

## NDK 問題解決

此分支已解決以下問題：
- ✅ NDK 27 mips 架構不支援問題
- ✅ llvm-strip 工具鏈錯誤
- ✅ 動態載入 NDI 函式庫

## 優點

- 🚀 最佳效能 - 直接使用原生 NDI SDK
- 🔧 完整功能 - 支援所有 NDI 特性
- 📱 Android TV 最佳化
- ⚡ 低延遲影音串流

## 缺點

- 🛠️ 複雜的建置設定
- 📦 較大的 APK 尺寸 (包含 .so 檔案)
- 🔧 需要 NDK 和 JNI 知識

## 使用情境

適合需要以下需求的專案：
- 最佳的串流效能
- 完整的 NDI 功能支援  
- 可接受較複雜的建置流程

---

**分支狀態**: ✅ 可建置，基本功能完成  
**下一步**: 實作 Leanback UI 和實際 NDI 探索功能