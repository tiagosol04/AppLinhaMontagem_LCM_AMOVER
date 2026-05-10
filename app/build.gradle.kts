plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.applinhamontagem"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.applinhamontagem"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // Emulador Android Studio: 10.0.2.2 redireciona para localhost do PC
            buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:5137/\"")
        }
        release {
            isMinifyEnabled = false
            // IMPORTANTE: substituir pela URL HTTPS real antes de distribuir
            buildConfigField("String", "API_BASE_URL", "\"https://CONFIGURAR_URL_PRODUCAO/\"")
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
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Navegação entre ecrãs
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // Rede / API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Câmara e código de barras (integração real em fases posteriores)
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.5.1")

    // Imagens
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Persistência de sessão
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Ícones Material Design
    implementation("androidx.compose.material:material-icons-extended:1.7.6")

    // Testes
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}