plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.tanda.ndiplayer"
    compileSdk = 36
    // ndkVersion = "25.1.8937393" // 暫時註解避免版本問題

    defaultConfig {
        applicationId = "com.tanda.ndiplayer"
        minSdk = 21
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // 暫時移除 ABI 過濾器進行測試
        // ndk {
        //     abiFilters += listOf("arm64-v8a", "x86_64")
        // }

        // externalNativeBuild {
        //     cmake {
        //         cppFlags += "-std=c++17"
        //         arguments += "-DANDROID_STL=c++_shared"
        //         targets += "ndiplayer"
        //     }
        // }
    }
    
    // externalNativeBuild {
    //     cmake {
    //         path = file("src/main/cpp/CMakeLists.txt")
    //         version = "3.18.1"
    //     }
    // }

    packaging {
        jniLibs {
            pickFirsts += listOf("**/libc++_shared.so", "**/libndi.so")
            keepDebugSymbols += "**/libndi.so"
            excludes += listOf("**/mips/**", "**/mips64/**")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    
    // Android TV Leanback 支援
    implementation("androidx.leanback:leanback:1.0.0")
    implementation("androidx.leanback:leanback-preference:1.0.0")
    
    // UI 元件
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("androidx.activity:activity-ktx:1.9.3")
    
    // 生命週期和 ViewModel
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    
    // 協程支援
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}