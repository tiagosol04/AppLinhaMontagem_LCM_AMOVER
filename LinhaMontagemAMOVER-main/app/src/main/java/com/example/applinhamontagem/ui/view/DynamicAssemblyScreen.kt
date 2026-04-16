package com.example.applinhamontagem.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.applinhamontagem.ui.navigation.Screen
import com.example.applinhamontagem.ui.viewmodel.ProductionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicAssemblyScreen(navController: NavController, viewModel: ProductionViewModel, motaId: Int) {
    val uiState by viewModel.uiState.collectAsState()
    val mota = uiState.currentMota
    val isComplete = uiState.isAssemblyComplete

    // Recupera estado se necessário
    LaunchedEffect(motaId) {
        if (uiState.currentMota?.idMota != motaId) {
            uiState.minhasAtribuidas.find { it.motaId == motaId }?.let { viewModel.selectFromDashboard(it) }
        }
    }

    Scaffold(
        topBar = {
            Column {
                // ✅ CORREÇÃO: TopAppBar padrão não tem subtitle.
                // Criamos um layout customizado no title ou usamos CenterAlignedTopAppBar
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                            Text("1. Montagem", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = "VIN: ${mota?.numeroIdentificacao ?: "..."}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
                )

                // Barra de Progresso no topo
                val total = uiState.listaPecasCombinada.size
                val done = uiState.listaPecasCombinada.count { it.isConcluido }
                val p = if (total > 0) done / total.toFloat() else 0f
                LinearProgressIndicator(
                    progress = { p },
                    modifier = Modifier.fillMaxWidth(),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
        },
        bottomBar = {
            // Botão de Avançar Fixo no fundo
            Button(
                onClick = { if (isComplete && mota != null) navController.navigate(Screen.PostAssembly.createRoute(mota.idMota, mota.idOrdemProducao)) },
                enabled = isComplete,
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("AVANÇAR PARA PÓS-MONTAGEM")
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF5F7FA)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.listaPecasCombinada) { item ->
                val isDone = item.isConcluido

                ListItem(
                    modifier = Modifier.fillMaxWidth().background(Color.White, MaterialTheme.shapes.medium),
                    headlineContent = { Text(item.defModelo.descricao ?: "Peça", fontWeight = FontWeight.Bold) },
                    supportingContent = {
                        Column {
                            Text("PN: ${item.defModelo.partNumber}", style = MaterialTheme.typography.bodySmall)
                            if (isDone) Text("SN: ${item.montado?.numeroSerie}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    },
                    leadingContent = {
                        Icon(
                            if(isDone) Icons.Default.CheckCircle else Icons.Default.QrCodeScanner,
                            null,
                            tint = if(isDone) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingContent = {
                        if (!isDone) {
                            Button(
                                onClick = { viewModel.registarPeca(item.defModelo.idPeca, "SN-AUTO") },
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) { Text("SCAN") }
                        }
                    }
                )
            }
        }
    }
}