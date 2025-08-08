package com.tanda.ndiplayer.ndi

/**
 * NDI SDK 的 JNI 封裝類別
 * 提供 NDI 核心功能的 Kotlin 介面
 */
object NDI {
    
    // 暫時停用 JNI 載入，直到 NDK 問題解決
    // init {
    //     System.loadLibrary("ndiplayer")
    // }
    
    // NDI 初始化和清理 - 暫時使用模擬實作
    fun initialize(): Boolean {
        // 模擬成功初始化
        return true
    }
    
    fun destroy() {
        // 模擬清理動作
    }
    
    // NDI 探索相關 - 暫時使用模擬實作
    fun findCreate(showLocalSources: Boolean): Long {
        // 回傳假的實例 ID
        return System.currentTimeMillis()
    }
    
    fun findDestroy(findInstance: Long) {
        // 模擬銷毀動作
    }
    
    fun findGetSources(findInstance: Long, timeout: Int): Array<NDISource>? {
        // 回傳測試用的 NDI 訊號源
        return arrayOf(
            NDISource("測試訊號源 1", "192.168.1.100:5961"),
            NDISource("測試訊號源 2", "192.168.1.101:5961")
        )
    }
    
    // NDI 接收相關 - 暫時使用模擬實作
    fun recvCreate(source: NDISource): Long {
        return System.currentTimeMillis()
    }
    
    fun recvDestroy(recvInstance: Long) {
        // 模擬銷毀動作
    }
    
    fun recvCapture(recvInstance: Long, timeout: Int): NDIFrame? {
        // 回傳無影格狀態
        return NDIFrame.NoFrame
    }
    
    fun recvFreeVideo(recvInstance: Long, frame: NDIVideoFrame) {
        // 模擬釋放動作
    }
    
    fun recvFreeAudio(recvInstance: Long, frame: NDIAudioFrame) {
        // 模擬釋放動作
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