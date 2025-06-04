plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("plugin.serialization")  // ← enable kotlinx.serialization
}

android {
    namespace = "com.example.waffle_front"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.waffle_front"
        minSdk = 24
        //noinspection EditedTargetSdkVersion
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    packagingOptions {
        resources {
            excludes += setOf(
                "META-INF/spring.schemas",
                "META-INF/spring.handlers",
                "META-INF/spring.tooling",
                "META-INF/license.txt",
                "META-INF/notice.txt"
            )
        }
    }
}

//dependencies {
//
//    // Retrofit2
//    implementation("com.squareup.retrofit2:retrofit:2.9.0")
////    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
//    //For WebSocket STOMP
//    implementation("org.springframework:spring-websocket:5.3.20") // Для STOMP клиента
//    implementation("org.springframework:spring-messaging:5.3.20")
////    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3") // Для JSON
//    // json
//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
//    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0")
//
//
//
//}
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // retrofit + kotlinx.serialization
    implementation(libs.retrofit)
    implementation(libs.retrofit2.kotlinx.serialization.converter)
    implementation(libs.kotlinx.serialization.json)

    // STOMP-клиент для Android
    implementation("org.springframework:spring-websocket:5.3.20") // Для STOMP клиента
    implementation("org.springframework:spring-messaging:5.3.20")
    implementation("com.github.NaikSoftware:StompProtocolAndroid:1.6.6")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation (libs.glide)
    // 1. в build.gradle (обычно уже есть зависимость на retrofit и converter-gson), добавим:
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")

}