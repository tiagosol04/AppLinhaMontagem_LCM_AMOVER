package com.example.applinhamontagem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.applinhamontagem.ui.navigation.AppNavigation
import com.example.applinhamontagem.ui.theme.AppLinhaMontagemTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Aplica o tema (Cores, Tipografia)
            AppLinhaMontagemTheme {
                // Inicia a navegação
                AppNavigation()
            }
        }
    }
}