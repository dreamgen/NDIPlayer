# NDI Player - Devolay 分支

此分支使用 Devolay Java 函式庫來實作 NDI 功能，避免複雜的 JNI 和 NDK 設定。

## 特色

- 純 Java/Kotlin 實作，無需 JNI 或 NDK
- 使用 Devolay 2.1.0 函式庫
- 簡化的建置流程
- 自動 native 函式庫管理

## 技術架構

### Devolay 整合
- **函式庫**: `me.walkerknapp:devolay:2.1.0`
- **功能**: 提供完整的 NDI Java API
- **優點**: 自動處理 native 函式庫載入和記憶體管理

### Kotlin 封裝
- **檔案**: `app/src/main/java/com/tanda/ndiplayer/ndi/NDI.kt`
- **功能**: Devolay API 的 Kotlin 封裝
- **特色**: 協程支援，Android 生命週期整合

### 網路探索
- **函式庫**: `org.jmdns:jmdns:3.5.8`
- **用途**: mDNS 服務探索支援
- **功能**: 增強 NDI 訊號源探索能力

## 建置需求

- Android Studio Arctic Fox 或更新版本
- 無需 NDK 或 JNI 設定
- minSdk 21, targetSdk 36
- Kotlin 1.9+

## Devolay 優點

### 簡化開發
- ✅ 無需 NDK/JNI 知識
- ✅ 自動 native 函式庫管理
- ✅ 純 Java API，類型安全
- ✅ 簡單的建置設定

### 功能完整
- 🔍 NDI 訊號源探索
- 📺 影音串流接收
- 🎵 多聲道音訊支援
- 📱 Android 平台最佳化

### 記憶體管理
- 🚀 自動記憶體回收
- 📦 較小的 APK 尺寸
- ⚡ 減少記憶體洩漏風險

## 與 Native SDK 比較

| 特色 | Devolay | Native SDK |
|-----|---------|------------|
| 建置複雜度 | 簡單 | 複雜 |
| APK 大小 | 較小 | 較大 |
| 效能 | 良好 | 最佳 |
| 功能覆蓋 | 完整 | 完整 |
| 開發難度 | 簡單 | 困難 |
| 記憶體管理 | 自動 | 手動 |

## 適用情境

Devolay 分支適合以下需求：
- 🚀 快速原型開發
- 📱 一般 NDI 串流需求
- 👨‍💻 避免複雜的 native 開發
- 🔄 需要頻繁迭代的專案

## 實作狀態

- ✅ Devolay 函式庫整合
- ✅ 基本 NDI 探索框架
- ✅ Android TV UI 設定
- 🔄 實際訊號源探索 (開發中)
- ⏳ 影音串流接收 (待實作)
- ⏳ OpenGL 渲染 (待實作)

## 下一步開發

1. 完成 NDI 訊號源探索功能
2. 實作 DevolayReceiver 影音接收
3. 加入 OpenGL ES 視訊渲染
4. 實作 AudioTrack 音訊播放

---

**分支狀態**: 🔄 開發中，基本框架完成  
**建議使用**: 適合大多數 NDI 應用需求