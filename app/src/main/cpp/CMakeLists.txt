cmake_minimum_required(VERSION 3.18.1)

project("ndiplayer")

# 建立 JNI 函式庫
add_library(
    ndiplayer
    SHARED
    ndi_jni.cpp
)

# 查找並連結必要的函式庫
find_library(
    log-lib
    log
)

find_library(
    dl-lib
    dl
)

# 設定包含目錄
target_include_directories(ndiplayer PRIVATE 
    ${CMAKE_CURRENT_SOURCE_DIR}/../../../NDI/include
)

# 連結函式庫
target_link_libraries(
    ndiplayer
    ${log-lib}
    ${dl-lib}
)

# 設定編譯選項
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
target_compile_options(ndiplayer PRIVATE 
    -Wall 
    -Wextra 
    -fPIC
)