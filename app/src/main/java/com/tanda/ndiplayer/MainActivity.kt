package com.tanda.ndiplayer

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.tanda.ndiplayer.ndi.NDI

/**
 * NDI Player 主要活動
 * 負責初始化 NDI SDK 和管理應用程式生命週期
 */
class MainActivity : FragmentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // 初始化 NDI SDK
        initializeNDI()
    }
    
    private fun initializeNDI() {
        try {
            val success = NDI.initialize()
            if (success) {
                // NDI 初始化成功
                // TODO: 開始探索 NDI 訊號源
            } else {
                // NDI 初始化失敗
                // TODO: 顯示錯誤訊息
            }
        } catch (e: UnsatisfiedLinkError) {
            // NDI 函式庫載入失敗
            // TODO: 顯示錯誤訊息
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 清理 NDI 資源
        try {
            NDI.destroy()
        } catch (e: Exception) {
            // 忽略清理時的錯誤
        }
    }
}