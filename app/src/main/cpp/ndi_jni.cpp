#include <jni.h>
#include <string>
#include <dlfcn.h>
#include <android/log.h>

#define LOG_TAG "NDI_JNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// NDI 函式庫句柄
static void* ndi_lib = nullptr;

// NDI 函數指標定義
typedef bool (*NDI_initialize_t)();
typedef void (*NDI_destroy_t)();
typedef void* (*NDI_find_create_v2_t)(const void* create_settings);
typedef void (*NDI_find_destroy_t)(void* find_instance);
typedef const void* (*NDI_find_get_current_sources_t)(void* find_instance, uint32_t* num_sources);
typedef void* (*NDI_recv_create_v3_t)(const void* recv_settings);
typedef void (*NDI_recv_destroy_t)(void* recv_instance);
typedef int (*NDI_recv_capture_v2_t)(void* recv_instance, void* video_frame, void* audio_frame, void* metadata_frame, uint32_t timeout_in_ms);
typedef void (*NDI_recv_free_video_v2_t)(void* recv_instance, void* video_frame);
typedef void (*NDI_recv_free_audio_v2_t)(void* recv_instance, void* audio_frame);

// NDI 資料結構定義
struct NDI_find_create_v2_t_struct {
    bool show_local_sources;
    const char* groups;
    const char* extra_ips;
};

struct NDI_source_t {
    const char* name;
    const char* url_address;
};

struct NDI_recv_create_v3_t_struct {
    const NDI_source_t* source;
    bool allow_video_fields;
    int bandwidth;
    bool color_format;
    const char* name_string;
};

// NDI 函數指標
static NDI_initialize_t NDI_initialize_func = nullptr;
static NDI_destroy_t NDI_destroy_func = nullptr;
static NDI_find_create_v2_t NDI_find_create_v2_func = nullptr;
static NDI_find_destroy_t NDI_find_destroy_func = nullptr;
static NDI_find_get_current_sources_t NDI_find_get_current_sources_func = nullptr;
static NDI_recv_create_v3_t NDI_recv_create_v3_func = nullptr;
static NDI_recv_destroy_t NDI_recv_destroy_func = nullptr;
static NDI_recv_capture_v2_t NDI_recv_capture_v2_func = nullptr;
static NDI_recv_free_video_v2_t NDI_recv_free_video_v2_func = nullptr;
static NDI_recv_free_audio_v2_t NDI_recv_free_audio_v2_func = nullptr;

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_tanda_ndiplayer_ndi_NDI_initialize(JNIEnv *env, jobject thiz) {
    LOGI("正在初始化 NDI SDK...");
    
    // 嘗試載入 NDI 函式庫
    ndi_lib = dlopen("libndi.so", RTLD_NOW);
    if (ndi_lib == nullptr) {
        LOGE("無法載入 NDI 函式庫: %s", dlerror());
        return JNI_FALSE;
    }
    
    LOGI("NDI 函式庫載入成功，正在載入函數指標...");
    
    // 載入所有必要的 NDI 函數
    NDI_initialize_func = (NDI_initialize_t) dlsym(ndi_lib, "NDIlib_initialize");
    NDI_destroy_func = (NDI_destroy_t) dlsym(ndi_lib, "NDIlib_destroy");
    NDI_find_create_v2_func = (NDI_find_create_v2_t) dlsym(ndi_lib, "NDIlib_find_create_v2");
    NDI_find_destroy_func = (NDI_find_destroy_t) dlsym(ndi_lib, "NDIlib_find_destroy");
    NDI_find_get_current_sources_func = (NDI_find_get_current_sources_t) dlsym(ndi_lib, "NDIlib_find_get_current_sources");
    NDI_recv_create_v3_func = (NDI_recv_create_v3_t) dlsym(ndi_lib, "NDIlib_recv_create_v3");
    NDI_recv_destroy_func = (NDI_recv_destroy_t) dlsym(ndi_lib, "NDIlib_recv_destroy");
    NDI_recv_capture_v2_func = (NDI_recv_capture_v2_t) dlsym(ndi_lib, "NDIlib_recv_capture_v2");
    NDI_recv_free_video_v2_func = (NDI_recv_free_video_v2_t) dlsym(ndi_lib, "NDIlib_recv_free_video_v2");
    NDI_recv_free_audio_v2_func = (NDI_recv_free_audio_v2_t) dlsym(ndi_lib, "NDIlib_recv_free_audio_v2");
    
    // 檢查關鍵函數是否載入成功
    if (!NDI_initialize_func || !NDI_find_create_v2_func || !NDI_recv_create_v3_func) {
        LOGE("無法載入關鍵 NDI 函數");
        dlclose(ndi_lib);
        ndi_lib = nullptr;
        return JNI_FALSE;
    }
    
    // 初始化 NDI
    if (!NDI_initialize_func()) {
        LOGE("NDI 初始化失敗");
        dlclose(ndi_lib);
        ndi_lib = nullptr;
        return JNI_FALSE;
    }
    
    LOGI("NDI SDK 初始化成功");
    return JNI_TRUE;
}

JNIEXPORT void JNICALL
Java_com_tanda_ndiplayer_ndi_NDI_destroy(JNIEnv *env, jobject thiz) {
    LOGI("正在清理 NDI SDK...");
    
    // 銷毀 NDI
    if (NDI_destroy_func) {
        NDI_destroy_func();
    }
    
    // 關閉動態載入的函式庫
    if (ndi_lib != nullptr) {
        dlclose(ndi_lib);
        ndi_lib = nullptr;
    }
    
    // 重置函數指標
    NDI_initialize_func = nullptr;
    NDI_destroy_func = nullptr;
    NDI_find_create_v2_func = nullptr;
    NDI_find_destroy_func = nullptr;
    NDI_find_get_current_sources_func = nullptr;
    NDI_recv_create_v3_func = nullptr;
    NDI_recv_destroy_func = nullptr;
    NDI_recv_capture_v2_func = nullptr;
    NDI_recv_free_video_v2_func = nullptr;
    NDI_recv_free_audio_v2_func = nullptr;
    
    LOGI("NDI SDK 清理完成");
}

JNIEXPORT jlong JNICALL
Java_com_tanda_ndiplayer_ndi_NDI_findCreate(JNIEnv *env, jobject thiz, jboolean show_local_sources) {
    LOGI("建立 NDI Find 實例 (show_local_sources: %s)", show_local_sources ? "true" : "false");
    
    if (!NDI_find_create_v2_func) {
        LOGE("NDI Find 函數未載入");
        return 0;
    }
    
    // 設定 NDI Find 建立參數
    NDI_find_create_v2_t_struct find_create = {};
    find_create.show_local_sources = show_local_sources;
    find_create.groups = nullptr; // 使用預設群組
    find_create.extra_ips = nullptr; // 使用自動 IP 偵測
    
    void* find_instance = NDI_find_create_v2_func(&find_create);
    if (!find_instance) {
        LOGE("建立 NDI Find 實例失敗");
        return 0;
    }
    
    LOGI("NDI Find 實例建立成功: %p", find_instance);
    return reinterpret_cast<jlong>(find_instance);
}

JNIEXPORT void JNICALL
Java_com_tanda_ndiplayer_ndi_NDI_findDestroy(JNIEnv *env, jobject thiz, jlong find_instance) {
    LOGI("銷毀 NDI Find 實例");
    
    if (find_instance == 0 || !NDI_find_destroy_func) {
        LOGE("無效的 Find 實例或函數未載入");
        return;
    }
    
    void* instance = reinterpret_cast<void*>(find_instance);
    NDI_find_destroy_func(instance);
    LOGI("NDI Find 實例已銷毀");
}

JNIEXPORT jobjectArray JNICALL
Java_com_tanda_ndiplayer_ndi_NDI_findGetSources(JNIEnv *env, jobject thiz, jlong find_instance, jint timeout) {
    LOGI("取得 NDI 訊號源列表 (timeout: %d)", timeout);
    
    if (find_instance == 0 || !NDI_find_get_current_sources_func) {
        LOGE("無效的 Find 實例或函數未載入");
        return nullptr;
    }
    
    void* instance = reinterpret_cast<void*>(find_instance);
    uint32_t num_sources = 0;
    
    // 取得 NDI 訊號源
    const NDI_source_t* sources = static_cast<const NDI_source_t*>(
        NDI_find_get_current_sources_func(instance, &num_sources));
    
    if (!sources || num_sources == 0) {
        LOGI("未發現 NDI 訊號源");
        // 回傳空陣列而非 null
        jclass sourceClass = env->FindClass("com/tanda/ndiplayer/ndi/NDI$NDISource");
        return env->NewObjectArray(0, sourceClass, nullptr);
    }
    
    LOGI("發現 %d 個 NDI 訊號源", num_sources);
    
    // 建立 Java 物件陣列
    jclass sourceClass = env->FindClass("com/tanda/ndiplayer/ndi/NDI$NDISource");
    if (!sourceClass) {
        LOGE("無法找到 NDISource 類別");
        return nullptr;
    }
    
    jmethodID constructor = env->GetMethodID(sourceClass, "<init>", "(Ljava/lang/String;Ljava/lang/String;)V");
    if (!constructor) {
        LOGE("無法找到 NDISource 建構子");
        return nullptr;
    }
    
    jobjectArray result = env->NewObjectArray(num_sources, sourceClass, nullptr);
    if (!result) {
        LOGE("無法建立結果陣列");
        return nullptr;
    }
    
    // 填入訊號源資料
    for (uint32_t i = 0; i < num_sources; i++) {
        jstring name = env->NewStringUTF(sources[i].name ? sources[i].name : "未知訊號源");
        jstring url = env->NewStringUTF(sources[i].url_address ? sources[i].url_address : "");
        
        jobject sourceObj = env->NewObject(sourceClass, constructor, name, url);
        env->SetObjectArrayElement(result, i, sourceObj);
        
        env->DeleteLocalRef(name);
        env->DeleteLocalRef(url);
        env->DeleteLocalRef(sourceObj);
    }
    
    LOGI("成功建立 %d 個訊號源物件", num_sources);
    return result;
}

JNIEXPORT jlong JNICALL
Java_com_tanda_ndiplayer_ndi_NDI_recvCreate(JNIEnv *env, jobject thiz, jobject source) {
    LOGI("建立 NDI 接收器");
    // TODO: 實現 NDI 接收器建立邏輯
    return 0;
}

JNIEXPORT void JNICALL
Java_com_tanda_ndiplayer_ndi_NDI_recvDestroy(JNIEnv *env, jobject thiz, jlong recv_instance) {
    LOGI("銷毀 NDI 接收器");
    // TODO: 實現 NDI 接收器銷毀邏輯
}

JNIEXPORT jobject JNICALL
Java_com_tanda_ndiplayer_ndi_NDI_recvCapture(JNIEnv *env, jobject thiz, jlong recv_instance, jint timeout) {
    LOGI("擷取 NDI 影格");
    // TODO: 實現 NDI 影格擷取邏輯
    return nullptr;
}

JNIEXPORT void JNICALL
Java_com_tanda_ndiplayer_ndi_NDI_recvFreeVideo(JNIEnv *env, jobject thiz, jlong recv_instance, jobject frame) {
    LOGI("釋放視訊影格");
    // TODO: 實現視訊影格釋放邏輯
}

JNIEXPORT void JNICALL
Java_com_tanda_ndiplayer_ndi_NDI_recvFreeAudio(JNIEnv *env, jobject thiz, jlong recv_instance, jobject frame) {
    LOGI("釋放音訊影格");
    // TODO: 實現音訊影格釋放邏輯
}

} // extern "C"