plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.applinhamontagem"
    compileSdk = 35 // Ajustado para versão estável (Android 15)

    defaultConfig {
        applicationId = "com.example.applinhamontagem"
        minSdk = 26 // Recomendado API 26 (Android 8.0) para apps industriais modernas
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
    buildFeatures {
        compose = true
    }
}

dependencies {
    // --- BIBLIOTECAS PADRÃO (Mantidas do teu projeto) ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // --- NAVEGAÇÃO ENTRE ECRÃS (Login -> Montagem) ---
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // --- REDE / API (Conectar ao Backend) ---
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // Logging Interceptor: Permite ver os JSONs que vêm da API no Logcat
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // --- CÂMARA E CÓDIGO DE BARRAS (Scanner Real) ---
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.5.1")

    // --- IMAGENS (Carregar fotos das peças via URL) ---
    implementation("io.coil-kt:coil-compose:2.5.0")

    // --- ÍCONES (Pack completo de ícones Material Design) ---
    implementation("androidx.compose.material:material-icons-extended:1.7.6")
    implementation(libs.mediation.test.suite)

    // --- TESTES (Padrão) ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}