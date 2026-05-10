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
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Warning
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
import com.example.applinhamontagem.data.utils.ScannerInputHelper
import com.example.applinhamontagem.ui.navigation.Screen
import com.example.applinhamontagem.ui.theme.StatusSuccess
import com.example.applinhamontagem.ui.viewmodel.ProductionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicAssemblyScreen(
    navController: NavController,
    viewModel: ProductionViewModel,
    motaId: Int
) {
    val uiState by viewModel.uiState.collectAsState()
    val mota = uiState.currentMota
    val isComplete = uiState.isAssemblyComplete
    val context = LocalContext.current
    var scanDialogPecaId by remember { mutableStateOf<Int?>(null) }
    var scanInput by remember { mutableStateOf("") }
    var scanError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(motaId) {
        if (uiState.currentMota?.idMota != motaId) {
            uiState.minhasAtribuidas.find { it.motaId == motaId }
                ?.let { viewModel.selectFromDashboard(it) }
        }
    }

    // Diálogo para introduzir S/N da peça (manual ou via leitor HID)
    scanDialogPecaId?.let { pecaId ->
        AlertDialog(
            onDismissRequest = {
                scanDialogPecaId = null
                scanInput = ""
                scanError = null
            },
            title = { Text("Número de Série da Peça", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Leia o código de barras ou introduza o S/N manualmente.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    OutlinedTextField(
                        value = scanInput,
                        onValueChange = { raw ->
                            // Sanitizar input ao digitar (remove CR, LF, tabs)
                            scanInput = ScannerInputHelper.sanitize(raw).uppercase()
                            scanError = null
                        },
                        label = { Text("S/N") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.titleMedium,
                        isError = scanError != null,
                        supportingText = if (scanError != null) {
                            { Text(scanError!!, color = MaterialTheme.colorScheme.error) }
                        } else null
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val snSanitizado = ScannerInputHelper.sanitize(scanInput)
                        when {
                            snSanitizado.isBlank() -> {
                                scanError = "O número de série não pode estar vazio."
                            }
                            !ScannerInputHelper.isValidSn(snSanitizado) -> {
                                scanError = "S/N inválido (mín. 3, máx. 60 caracteres)."
                            }
                            else -> {
                                viewModel.registarPeca(pecaId, snSanitizado) { ok ->
                                    if (ok) {
                                        HapticHelper.success(context)
                                        scanDialogPecaId = null
                                        scanInput = ""
                                        scanError = null
                                    } else {
                                        HapticHelper.error(context)
                                        // Erro já está em uiState.errorMessage
                                    }
                                }
                            }
                        }
                    },
                    enabled = scanInput.isNotBlank()
                ) {
                    Text("CONFIRMAR")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    scanDialogPecaId = null
                    scanInput = ""
                    scanError = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Montagem de Peças",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "VIN: ${mota?.numeroIdentificacao ?: "—"}",
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
                val total = uiState.listaPecasCombinada.size
                val done = uiState.listaPecasCombinada.count { it.isConcluido }
                val p = if (total > 0) done / total.toFloat() else 0f
                LinearProgressIndicator(
                    progress = { p },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                )
                Text(
                    "$done / $total peças",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        },
        bottomBar = {
            // Avançar só é possível com todas as peças montadas E ordem iniciada
            val podeAvancar = isComplete && uiState.ordemIniciada
            Button(
                onClick = {
                    if (podeAvancar && mota != null) {
                        navController.navigate(
                            Screen.PostAssembly.createRoute(mota.idMota, mota.idOrdemProducao)
                        )
                    }
                },
                enabled = podeAvancar,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(64.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("AVANÇAR PARA VERIFICAÇÃO", fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF5F7FA)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // Banner de erro geral (API, duplicado, etc.)
            uiState.errorMessage?.let { msg ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            msg,
                            modifier = Modifier.padding(12.dp),
                            color = Color(0xFFC62828),
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Banner: ordem ainda não iniciada
            if (uiState.isOrdemPorIniciar) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Warning,
                                    null,
                                    tint = Color(0xFFE65100),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Esta ordem ainda não foi iniciada",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "É necessário iniciar a ordem para criar as checklists e poder avançar no fluxo de produção.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            Spacer(Modifier.height(12.dp))

                            val ordemBloqueada = uiState.currentOrdem?.estado == 3
                            val ordemConcluida = uiState.currentOrdem?.estado == 2

                            when {
                                ordemBloqueada -> Text(
                                    "Ordem bloqueada. Contacte o supervisor.",
                                    color = Color(0xFFC62828),
                                    fontWeight = FontWeight.Medium
                                )
                                ordemConcluida -> Text(
                                    "Esta ordem já está concluída.",
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                                else -> Button(
                                    onClick = {
                                        val ordemId = mota?.idOrdemProducao ?: return@Button
                                        viewModel.iniciarOrdem(ordemId)
                                    },
                                    enabled = !uiState.isLoading,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFE65100)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("INICIAR ORDEM", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Lista de peças
            if (uiState.listaPecasCombinada.isEmpty() && !uiState.isLoading && !uiState.isOrdemPorIniciar) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Nenhuma peça definida para este modelo.",
                            color = Color.Gray
                        )
                    }
                }
            }

            items(uiState.listaPecasCombinada) { item ->
                val isDone = item.isConcluido
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDone) Color(0xFFE8F5E9) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(if (isDone) 0.dp else 3.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isDone) Icons.Default.CheckCircle else Icons.Default.QrCodeScanner,
                            null,
                            tint = if (isDone) StatusSuccess else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                item.defModelo.descricao ?: "Peça",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "PN: ${item.defModelo.partNumber ?: "—"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            if (isDone) {
                                Text(
                                    "S/N: ${item.montado?.numeroSerie ?: "—"}",
                                    color = StatusSuccess,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        if (!isDone) {
                            Button(
                                onClick = {
                                    scanDialogPecaId = item.defModelo.idPeca
                                    scanInput = ""
                                    scanError = null
                                },
                                modifier = Modifier.height(56.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) {
                                Icon(
                                    Icons.Default.QrCodeScanner,
                                    null,
                                    modifier = Modifier.size(20.dp)
                                )
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
