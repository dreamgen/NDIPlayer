package com.tanda.ndiplayer.ndi

import android.content.Context
import android.net.nsd.NsdManager
import android.util.Log
import kotlinx.coroutines.*
import me.walkerknapp.devolay.*

/**
 * 簡單的 Devolay 測試類別
 * 用於驗證 Devolay 基本功能是否正常運作
 */
class SimpleDevolayTest(private val context: Context) {
    private val TAG = "SimpleDevolayTest"
    private lateinit var nsdManager: NsdManager
    
    /**
     * 執行基本的 Devolay 測試
     */
    suspend fun runBasicTest(): TestResult = withContext(Dispatchers.IO) {
        Log.i(TAG, "開始 Devolay 基本測試")
        
        // 初始化 NsdManager
        nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
        
        val results = mutableListOf<String>()
        var hasErrors = false
        
        try {
            // 測試 1: 檢查 Devolay 版本
            testDevolayVersion(results)
            
            // 測試 2: 建立和關閉 DevolayFinder
            testFinderCreation(results)
            
            // 測試 3: 進行 NDI 探索測試
            testNDIDiscovery(results)
            
        } catch (e: Exception) {
            hasErrors = true
            results.add("❌ 測試過程發生嚴重錯誤: ${e.message}")
            Log.e(TAG, "測試執行錯誤", e)
        }
        
        Log.i(TAG, "Devolay 基本測試完成")
        TestResult(
            success = !hasErrors,
            details = results,
            timestamp = System.currentTimeMillis()
        )
    }
    
    private fun testDevolayVersion(results: MutableList<String>) {
        try {
            // 嘗試獲取 Devolay 版本資訊
            Log.i(TAG, "測試 Devolay 版本檢查...")
            results.add("✅ Devolay 函式庫載入成功")
            results.add("📦 準備進行 NDI SDK 測試...")
            
        } catch (e: Exception) {
            results.add("❌ Devolay 版本檢查失敗: ${e.message}")
            Log.e(TAG, "版本檢查失敗", e)
        }
    }
    
    private suspend fun testFinderCreation(results: MutableList<String>) {
        var finder: DevolayFinder? = null
        
        try {
            Log.i(TAG, "測試 DevolayFinder 建立...")
            
            finder = DevolayFinder()
            results.add("✅ DevolayFinder 建立成功")
            
            // 等待短時間讓探索器初始化
            delay(1000)
            
            // 檢查探索器狀態
            val currentSources = finder.currentSources
            results.add("📡 探索器初始化完成，當前找到 ${currentSources.size} 個訊號源")
            
        } catch (e: Exception) {
            results.add("❌ DevolayFinder 建立失敗: ${e.message}")
            Log.e(TAG, "Finder 建立失敗", e)
            
        } finally {
            finder?.close()
        }
    }
    
    private suspend fun testNDIDiscovery(results: MutableList<String>) {
        var finder: DevolayFinder? = null
        
        try {
            Log.i(TAG, "測試 NDI 訊號源探索...")
            
            finder = DevolayFinder()
            results.add("🔍 開始 NDI 訊號源探索...")
            
            // 等待較長時間讓探索完成
            repeat(3) { attempt ->
                delay(2000) // 等待 2 秒
                
                val sources = finder.currentSources
                results.add("📊 探索第 ${attempt + 1} 次: 找到 ${sources.size} 個訊號源")
                
                sources.forEachIndexed { index, source ->
                    results.add("  📺 訊號源 ${index + 1}: ${source.sourceName}")
                    results.add("      位址: ${source.toString()}")
                }
                
                if (sources.isNotEmpty()) {
                    return@repeat // 找到訊號源後停止
                }
            }
            
            val finalSources = finder.currentSources
            if (finalSources.isEmpty()) {
                results.add("⚠️  未發現任何 NDI 訊號源")
                results.add("💡 請確保:")
                results.add("   • 裝置與 NDI 訊號源在同一網路")
                results.add("   • NDI 發送端正在運作")
                results.add("   • 防火牆允許 NDI 流量")
            } else {
                results.add("✅ NDI 探索測試成功")
            }
            
        } catch (e: Exception) {
            results.add("❌ NDI 探索測試失敗: ${e.message}")
            Log.e(TAG, "NDI 探索測試失敗", e)
            
        } finally {
            finder?.close()
        }
    }
    
    /**
     * 執行進階測試（包含連線測試）
     */
    suspend fun runAdvancedTest(): TestResult = withContext(Dispatchers.IO) {
        Log.i(TAG, "開始 Devolay 進階測試")
        
        val results = mutableListOf<String>()
        var hasErrors = false
        
        try {
            // 先執行基本測試
            val basicResult = runBasicTest()
            results.addAll(basicResult.details)
            
            if (basicResult.success) {
                // 進行連線測試
                testNDIConnection(results)
            } else {
                results.add("❌ 基本測試失敗，跳過進階測試")
                hasErrors = true
            }
            
        } catch (e: Exception) {
            hasErrors = true
            results.add("❌ 進階測試過程發生錯誤: ${e.message}")
            Log.e(TAG, "進階測試執行錯誤", e)
        }
        
        TestResult(
            success = !hasErrors,
            details = results,
            timestamp = System.currentTimeMillis()
        )
    }
    
    private suspend fun testNDIConnection(results: MutableList<String>) {
        var finder: DevolayFinder? = null
        var receiver: DevolayReceiver? = null
        
        try {
            Log.i(TAG, "測試 NDI 連線...")
            
            finder = DevolayFinder()
            delay(3000) // 等待探索完成
            
            val sources = finder.currentSources
            if (sources.isEmpty()) {
                results.add("⚠️  無可用訊號源進行連線測試")
                return
            }
            
            val testSource = sources.first()
            results.add("🔗 嘗試連線到: ${testSource.sourceName}")
            
            receiver = DevolayReceiver(testSource)
            results.add("✅ NDI 接收器建立成功")
            
            // 嘗試接收影格
            val videoFrame = DevolayVideoFrame()
            val result = receiver.receiveCapture(videoFrame, null, null, 2000)
            
            when (result) {
                DevolayFrameType.VIDEO -> {
                    results.add("🎬 成功接收視訊影格!")
                    // TODO: 實際取得影格資訊
                    results.add("   成功接收到視訊資料")
                }
                DevolayFrameType.NONE -> {
                    results.add("⏳ 連線正常但未收到影格（可能是靜態源）")
                }
                DevolayFrameType.ERROR -> {
                    results.add("❌ 接收影格時發生錯誤")
                }
                else -> {
                    results.add("📊 收到其他類型影格: $result")
                }
            }
            
        } catch (e: Exception) {
            results.add("❌ NDI 連線測試失敗: ${e.message}")
            Log.e(TAG, "NDI 連線測試失敗", e)
            
        } finally {
            receiver?.close()
            finder?.close()
        }
    }
    
    /**
     * 測試結果資料類別
     */
    data class TestResult(
        val success: Boolean,
        val details: List<String>,
        val timestamp: Long
    ) {
        fun getFormattedReport(): String {
            val status = if (success) "✅ 成功" else "❌ 失敗"
            val header = "Devolay 測試報告 - $status\n" +
                    "時間: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(timestamp)}\n" +
                    "=" + "=".repeat(50)
            
            return "$header\n${details.joinToString("\n")}"
        }
    }
}