package com.tanda.ndiplayer

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.tanda.ndiplayer.ndi.NDI
import com.tanda.ndiplayer.ndi.SimpleDevolayTest
import kotlinx.coroutines.launch

/**
 * NDI Player 主要活動
 * 負責初始化 Devolay NDI SDK 和管理應用程式生命週期
 */
class MainActivity : FragmentActivity() {
    private val TAG = "MainActivity"
    private lateinit var devolayTest: SimpleDevolayTest
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // 初始化 Devolay 測試工具
        devolayTest = SimpleDevolayTest(this)
        
        // 初始化和測試 NDI SDK
        initializeAndTestNDI()
    }
    
    private fun initializeAndTestNDI() {
        lifecycleScope.launch {
            try {
                Log.i(TAG, "開始初始化 Devolay NDI...")
                
                // 使用 NDI 封裝類別初始化
                val success = NDI.initialize()
                if (success) {
                    Log.i(TAG, "NDI 封裝類別初始化成功")
                    showToast("NDI 初始化成功")
                    
                    // 執行基本 Devolay 測試
                    runDevolayTest()
                    
                } else {
                    Log.e(TAG, "NDI 封裝類別初始化失敗")
                    showToast("NDI 初始化失敗")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "NDI 初始化過程發生錯誤", e)
                showToast("NDI 初始化錯誤: ${e.message}")
            }
        }
    }
    
    private suspend fun runDevolayTest() {
        try {
            Log.i(TAG, "執行 Devolay 基本功能測試...")
            showToast("開始 NDI 功能測試...")
            
            val testResult = devolayTest.runBasicTest()
            
            // 輸出詳細測試結果到 Log
            Log.i(TAG, "Devolay 測試完成:")
            testResult.details.forEach { detail ->
                Log.i(TAG, detail)
            }
            
            // 顯示測試結果
            val message = if (testResult.success) {
                "NDI 測試成功! 找到 ${countDiscoveredSources(testResult.details)} 個訊號源"
            } else {
                "NDI 測試完成，部分功能可能有問題"
            }
            
            showToast(message)
            
            // 如果基本測試成功，可以繼續進行進階測試
            if (testResult.success) {
                Log.i(TAG, "基本測試成功，準備進行探索功能測試...")
                startNDIDiscovery()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Devolay 測試執行錯誤", e)
            showToast("NDI 測試失敗: ${e.message}")
        }
    }
    
    private fun startNDIDiscovery() {
        lifecycleScope.launch {
            try {
                Log.i(TAG, "開始 NDI 訊號源持續探索...")
                
                // 使用 NDI 封裝類別進行探索
                val findInstance = NDI.findCreate(true)
                if (findInstance != 0L) {
                    // 等待探索結果
                    kotlinx.coroutines.delay(3000)
                    
                    val sources = NDI.findGetSources(findInstance, 1000)
                    if (sources != null && sources.isNotEmpty()) {
                        Log.i(TAG, "發現 ${sources.size} 個 NDI 訊號源:")
                        sources.forEach { source ->
                            Log.i(TAG, "  - ${source.name} @ ${source.urlAddress}")
                        }
                        showToast("發現 ${sources.size} 個 NDI 訊號源")
                    } else {
                        Log.i(TAG, "未發現任何 NDI 訊號源")
                        showToast("未發現 NDI 訊號源")
                    }
                    
                    // 清理探索器
                    NDI.findDestroy(findInstance)
                } else {
                    Log.w(TAG, "建立 NDI 探索器失敗")
                    showToast("建立探索器失敗")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "NDI 探索過程錯誤", e)
                showToast("探索錯誤: ${e.message}")
            }
        }
    }
    
    private fun countDiscoveredSources(details: List<String>): Int {
        return details.count { it.contains("📺 訊號源") }
    }
    
    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 清理 NDI 資源
        try {
            NDI.destroy()
            Log.i(TAG, "NDI 資源清理完成")
        } catch (e: Exception) {
            Log.w(TAG, "NDI 清理時發生錯誤", e)
        }
    }
}