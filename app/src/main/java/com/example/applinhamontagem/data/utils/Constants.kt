package com.example.applinhamontagem.data.utils

import com.example.applinhamontagem.BuildConfig

object Constants {
    // URL definida por flavor em build.gradle.kts:
    // debug  → http://10.0.2.2:5137/
    // release → https://CONFIGURAR_URL_PRODUCAO/
    val BASE_URL: String get() = BuildConfig.API_BASE_URL
}