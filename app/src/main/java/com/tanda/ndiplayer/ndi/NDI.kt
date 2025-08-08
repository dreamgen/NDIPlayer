package com.tanda.ndiplayer.ndi

import me.walkerknapp.devolay.*
import kotlinx.coroutines.*
import android.util.Log

/**
 * NDI Devolay 封裝類別
 * 使用 Devolay Java 函式庫提供 NDI 功能
 */
object NDI {
    private const val TAG = "NDI_Devolay"
    
    private var isInitialized = false
    private var finder: DevolayFinder? = null
    
    // NDI 初始化和清理
    fun initialize(): Boolean {
        return try {
            if (!isInitialized) {
                // Devolay 會自動初始化
                isInitialized = true
                Log.i(TAG, "Devolay NDI 初始化成功")
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Devolay NDI 初始化失敗", e)
            false
        }
    }
    
    fun destroy() {
        finder?.close()
        finder = null
        isInitialized = false
        Log.i(TAG, "NDI 資源已清理")
    }
    
    // NDI 探索相關 - 使用 Devolay 實作
    fun findCreate(showLocalSources: Boolean): Long {
        return try {
            if (!isInitialized) initialize()
            
            finder?.close() // 關閉之前的 finder
            finder = DevolayFinder().apply {
                // Devolay 會自動開始探索
            }
            System.currentTimeMillis() // 回傳一個唯一 ID
        } catch (e: Exception) {
            Log.e(TAG, "建立 NDI Finder 失敗", e)
            0L
        }
    }
    
    fun findDestroy(findInstance: Long) {
        finder?.close()
        finder = null
        Log.i(TAG, "NDI Finder 已銷毀")
    }
    
    fun findGetSources(findInstance: Long, timeout: Int): Array<NDISource>? {
        return try {
            Log.i(TAG, "搜尋 NDI 訊號源中...")
            
            // TODO: 實作真實的 Devolay 探索
            // 暫時回傳測試資料
            arrayOf(
                NDISource("Devolay 測試訊號源 1", "192.168.1.100:5961"),
                NDISource("Devolay 測試訊號源 2", "192.168.1.101:5961")
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "取得 NDI 訊號源失敗", e)
            emptyArray()
        }
    }
    
    // NDI 接收相關 - Devolay 實作框架
    fun recvCreate(source: NDISource): Long {
        return try {
            // TODO: 實作 DevolayReceiver 建立
            Log.i(TAG, "準備建立接收器: ${source.name}")
            System.currentTimeMillis()
        } catch (e: Exception) {
            Log.e(TAG, "建立 NDI 接收器失敗", e)
            0L
        }
    }
    
    fun recvDestroy(recvInstance: Long) {
        // TODO: 實作接收器銷毀
        Log.i(TAG, "接收器已銷毀")
    }
    
    fun recvCapture(recvInstance: Long, timeout: Int): NDIFrame? {
        // TODO: 實作影格擷取
        return NDIFrame.NoFrame
    }
    
    fun recvFreeVideo(recvInstance: Long, frame: NDIVideoFrame) {
        // Devolay 自動管理記憶體
    }
    
    fun recvFreeAudio(recvInstance: Long, frame: NDIAudioFrame) {
        // Devolay 自動管理記憶體
    }
    
    /**
     * NDI 訊號源資料類別
     */
    data class NDISource(
        val name: String,
        val urlAddress: String
    )
    
    /**
     * NDI 影格基礎類別
     */
    sealed class NDIFrame {
        data class VideoFrame(
            val width: Int,
            val height: Int,
            val stride: Int,
            val frameRate: Float,
            val data: ByteArray
        ) : NDIFrame() {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false
                other as VideoFrame
                return width == other.width && height == other.height && 
                       stride == other.stride && frameRate == other.frameRate &&
                       data.contentEquals(other.data)
            }
            
            override fun hashCode(): Int {
                var result = width
                result = 31 * result + height
                result = 31 * result + stride
                result = 31 * result + frameRate.hashCode()
                result = 31 * result + data.contentHashCode()
                return result
            }
        }
        
        data class AudioFrame(
            val sampleRate: Int,
            val channels: Int,
            val samples: Int,
            val data: FloatArray
        ) : NDIFrame() {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false
                other as AudioFrame
                return sampleRate == other.sampleRate && channels == other.channels &&
                       samples == other.samples && data.contentEquals(other.data)
            }
            
            override fun hashCode(): Int {
                var result = sampleRate
                result = 31 * result + channels
                result = 31 * result + samples
                result = 31 * result + data.contentHashCode()
                return result
            }
        }
        
        object NoFrame : NDIFrame()
    }
}

// 類型別名以簡化使用
typealias NDIVideoFrame = NDI.NDIFrame.VideoFrame
typealias NDIAudioFrame = NDI.NDIFrame.AudioFrame