package com.example.applinhamontagem.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.applinhamontagem.data.utils.HapticHelper
import com.example.applinhamontagem.ui.navigation.Screen
import com.example.applinhamontagem.ui.theme.StatusSuccess
import com.example.applinhamontagem.ui.viewmodel.ProductionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicAssemblyScreen(navController: NavController, viewModel: ProductionViewModel, motaId: Int) {
    val uiState by viewModel.uiState.collectAsState()
    val mota = uiState.currentMota
    val isComplete = uiState.isAssemblyComplete
    val context = LocalContext.current
    var scanDialogPecaId by remember { mutableStateOf<Int?>(null) }
    var scanInput by remember { mutableStateOf("") }

    LaunchedEffect(motaId) {
        if (uiState.currentMota?.idMota != motaId) {
            uiState.minhasAtribuidas.find { it.motaId == motaId }?.let { viewModel.selectFromDashboard(it) }
        }
    }

    // Dialog para introduzir SN (manual ou simulado)
    scanDialogPecaId?.let { pecaId ->
        AlertDialog(
            onDismissRequest = { scanDialogPecaId = null; scanInput = "" },
            title = { Text("Número de Série da Peça") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = scanInput,
                        onValueChange = { scanInput = it.uppercase().trim() },
                        label = { Text("S/N") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.titleMedium
                    )
                    OutlinedButton(
                        onClick = {
                            scanInput = "SN-${System.currentTimeMillis().toString().takeLast(8)}"
                            HapticHelper.tick(context)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.QrCodeScanner, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("SIMULAR SCAN")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (scanInput.isNotBlank()) {
                            viewModel.registarPeca(pecaId, scanInput) { ok ->
                                if (ok) HapticHelper.success(context) else HapticHelper.error(context)
                            }
                            scanDialogPecaId = null; scanInput = ""
                        }
                    },
                    enabled = scanInput.length >= 3
                ) { Text("CONFIRMAR") }
            },
            dismissButton = {
                TextButton(onClick = { scanDialogPecaId = null; scanInput = "" }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Montagem de Peças", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("VIN: ${mota?.numeroIdentificacao ?: "..."}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    },
                    navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar") } },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
                )
                val total = uiState.listaPecasCombinada.size
                val done = uiState.listaPecasCombinada.count { it.isConcluido }
                val p = if (total > 0) done / total.toFloat() else 0f
                LinearProgressIndicator(progress = { p }, modifier = Modifier.fillMaxWidth().height(6.dp))
                Text("$done / $total peças", modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        },
        bottomBar = {
            Button(
                onClick = { if (isComplete && mota != null) navController.navigate(Screen.PostAssembly.createRoute(mota.idMota, mota.idOrdemProducao)) },
                enabled = isComplete,
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(64.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("AVANÇAR PARA VERIFICAÇÃO", fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF5F7FA)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(uiState.listaPecasCombinada) { item ->
                val isDone = item.isConcluido
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDone) Color(0xFFE8F5E9) else Color.White),
                    elevation = CardDefaults.cardElevation(if (isDone) 0.dp else 3.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isDone) Icons.Default.CheckCircle else Icons.Default.QrCodeScanner,
                            null,
                            tint = if (isDone) StatusSuccess else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.defModelo.descricao ?: "Peça", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            Text("PN: ${item.defModelo.partNumber ?: "—"}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            if (isDone) {
                                Text("S/N: ${item.montado?.numeroSerie ?: "—"}", color = StatusSuccess, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (!isDone) {
                            Button(
                                onClick = { scanDialogPecaId = item.defModelo.idPeca },
                                modifier = Modifier.height(56.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) {
                                Icon(Icons.Default.QrCodeScanner, null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("SCAN", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
