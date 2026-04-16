package com.example.applinhamontagem.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.applinhamontagem.ui.navigation.Screen
import com.example.applinhamontagem.ui.viewmodel.ProductionViewModel

@Composable
fun IdentifyMotoScreen(navController: NavController, viewModel: ProductionViewModel) {
    var vinInput by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    var navigated by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.currentMota) {
        val mota = uiState.currentMota
        if (mota != null && !navigated) {
            navigated = true
            navController.navigate(Screen.Assembly.createRoute(mota.idMota))
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Identificar Mota", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
        Text("Leia o QR Code do Chassis")
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = vinInput,
            onValueChange = { vinInput = it; navigated = false },
            label = { Text("VIN") },
            leadingIcon = { Icon(Icons.Default.QrCodeScanner, null) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { viewModel.loadMotaByVin(vinInput) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            else {
                Icon(Icons.Default.Search, null)
                Spacer(Modifier.width(8.dp))
                Text("BUSCAR")
            }
        }

        uiState.errorMessage?.let {
            Spacer(Modifier.height(16.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}