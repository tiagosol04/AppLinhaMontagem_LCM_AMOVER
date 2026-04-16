package com.example.applinhamontagem.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.applinhamontagem.data.utils.HapticHelper
import com.example.applinhamontagem.ui.navigation.Screen
import com.example.applinhamontagem.ui.viewmodel.ProductionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterVinScreen(navController: NavController, viewModel: ProductionViewModel, motaId: Int, ordemId: Int) {
    val uiState by viewModel.uiState.collectAsState()
    val ctx = LocalContext.current
    var vinInput by remember { mutableStateOf(uiState.currentMota?.numeroIdentificacao ?: "") }
    val jaTemVin = !uiState.currentMota?.numeroIdentificacao.isNullOrBlank()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Registar Quadro / VIN", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("OP #$ordemId", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar") } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (jaTemVin) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Quadro já registado", style = MaterialTheme.typography.titleMedium, color = Color(0xFF2E7D32))
                        Spacer(Modifier.height(8.dp))
                        Text(uiState.currentMota?.numeroIdentificacao ?: "", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { navController.navigate(Screen.Assembly.createRoute(motaId)) },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("AVANÇAR PARA MONTAGEM", fontWeight = FontWeight.Bold) }
            } else {
                Text("Leia o código do quadro ou introduza manualmente", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = Color.Gray)
                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = vinInput,
                    onValueChange = { vinInput = it.uppercase().trim() },
                    label = { Text("Número de Quadro / VIN") },
                    leadingIcon = { Icon(Icons.Default.QrCodeScanner, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        vinInput = "AJP${System.currentTimeMillis().toString().takeLast(11)}"
                        HapticHelper.tick(ctx)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, null)
                    Spacer(Modifier.width(8.dp))
                    Text("SIMULAR LEITURA DE CÓDIGO")
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        viewModel.registarVin(vinInput) {
                            HapticHelper.success(ctx)
                            navController.navigate(Screen.Assembly.createRoute(motaId))
                        }
                    },
                    enabled = vinInput.length >= 5 && !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else Text("REGISTAR E AVANÇAR", fontWeight = FontWeight.Bold)
                }
                uiState.errorMessage?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
