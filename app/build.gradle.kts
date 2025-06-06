plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    compileSdk = 35

    defaultConfig {
        applicationId = "com.madsam.compose_icon_pack"
        minSdk = 28
        targetSdk = 35
        versionCode = 17103000
        versionName = "4.0.0"
        namespace = "com.madsam.compose_icon_pack"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    lint {
        disable += "QueryAllPackagesPermission"
    }
}

composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
}

dependencies {
    // Preferences DataStore
    implementation(libs.datastore.preferences)

    // Compose 依赖
    implementation(libs.compose.compiler)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.navigation.compose)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.icons.extended)

    // Android官方库
    implementation(libs.material)
    implementation(libs.preference)
    implementation(libs.recyclerview)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)

    // 更现代的图片加载库
    implementation(libs.coil.compose)

    // 快速滚动条
    implementation(libs.fastscroll)

    // 网络请求框架
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)

    // 图片加载
    implementation(libs.glide)

    // 新手引导提示
    implementation(libs.material.tap.target)

    // 测试依赖
    testImplementation(libs.junit)
}