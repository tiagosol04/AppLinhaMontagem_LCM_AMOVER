package com.example.applinhamontagem.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import com.example.applinhamontagem.data.utils.HapticHelper
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.applinhamontagem.ui.components.StepperIndicator
import com.example.applinhamontagem.ui.navigation.Screen
import com.example.applinhamontagem.ui.theme.StatusSuccess
import com.example.applinhamontagem.ui.viewmodel.AuthViewModel
import com.example.applinhamontagem.ui.viewmodel.ProductionStep
import com.example.applinhamontagem.ui.viewmodel.ProductionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackagingScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    prodViewModel: ProductionViewModel,
    motaId: Int
) {
    val uiState by prodViewModel.uiState.collectAsState()
    val itens = uiState.checklists?.embalagem ?: emptyList()
    val isComplete = uiState.isPackagingComplete
    val isLoading = uiState.isLoading
    val context = LocalContext.current
    var showConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { prodViewModel.setStep(ProductionStep.EMBALAGEM) }

    // Dialogo de confirmacao final
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Finalizar Producao", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tem a certeza que quer finalizar esta unidade?", style = MaterialTheme.typography.bodyLarge)
                    Text("A API vai validar que todos os requisitos estao cumpridos (pecas, checklists, VIN).", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    Text("Esta acao nao pode ser revertida.", color = Color(0xFFC62828), fontWeight = FontWeight.Medium)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        val uid = authViewModel.user?.userId ?: return@Button
                        prodViewModel.finalizarProducao(uid) {
                            HapticHelper.success(context)
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Dashboard.route) { inclusive = true }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
                    modifier = Modifier.height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("SIM, FINALIZAR", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmDialog = false }, modifier = Modifier.height(52.dp)) {
                    Text("CANCELAR")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Embalagem e Expedicao", fontWeight = FontWeight.Bold) },
                    navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar") } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
                StepperIndicator(currentStep = ProductionStep.EMBALAGEM)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Verifique o conteudo da embalagem:", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 16.dp))

            // Erro da API (ex: pecas em falta, VIN nao registado)
            uiState.errorMessage?.let { msg ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(msg, modifier = Modifier.padding(12.dp), color = Color(0xFFC62828), fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                }
            }

            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(itens) { item ->
                    val isChecked = (item.incluido ?: 0) == 1
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = if (isChecked) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.nome, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            Switch(
                                checked = isChecked,
                                onCheckedChange = {
                                    prodViewModel.toggleChecklist(item.idChecklist, "embalagem", it)
                                    if (it) HapticHelper.success(context)
                                },
                                modifier = Modifier.size(width = 52.dp, height = 32.dp),
                                thumbContent = if (isChecked) {{ Icon(Icons.Default.Check, null, Modifier.size(14.dp)) }} else null
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Botao FINALIZAR - chama POST /ordens/{id}/finalizar via API
            Button(
                onClick = { showConfirmDialog = true },
                enabled = isComplete && !isLoading,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isComplete) StatusSuccess else Color.LightGray
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("A FINALIZAR...", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                } else {
                    Text(
                        if (isComplete) "CONCLUIR PRODUCAO" else "COMPLETE TODOS OS ITENS",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
