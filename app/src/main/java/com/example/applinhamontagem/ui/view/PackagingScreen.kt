package com.example.applinhamontagem.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
    val ordemConcluida = uiState.currentOrdem?.estado == 2
    val context = LocalContext.current
    var showConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { prodViewModel.setStep(ProductionStep.EMBALAGEM) }

    // Diálogo de confirmação antes de marcar como embalada
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text("Confirmar Embalagem", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Confirma que esta unidade está embalada e pronta?",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "A API irá validar que todos os requisitos estão cumpridos (peças, checklists, VIN).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Text(
                        "Nota: esta ação marca a unidade como embalada. A ordem de produção só ficará concluída quando todas as unidades estiverem prontas.",
                        color = Color(0xFFE65100),
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        val uid = authViewModel.user?.idUtilizador?.toString() ?: return@Button
                        prodViewModel.concluirEtapaEmbalagem(uid) {
                            HapticHelper.success(context)
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Dashboard.route) { inclusive = true }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
                    modifier = Modifier.height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("SIM, CONFIRMAR", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showConfirmDialog = false },
                    modifier = Modifier.height(52.dp)
                ) {
                    Text("CANCELAR")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text("Embalagem e Expedição", fontWeight = FontWeight.Bold)
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
                StepperIndicator(currentStep = ProductionStep.EMBALAGEM)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {

            // Erros da API (ex: peças em falta, VIN não registado)
            uiState.errorMessage?.let { msg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(8.dp)
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

            if (itens.isEmpty() && !isLoading) {
                // Checklist de embalagem não configurada
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Checklist de embalagem não configurada para este modelo.\nContacte o supervisor.",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Text(
                    "Verifique o conteúdo da embalagem:",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(itens) { item ->
                        val isChecked = (item.incluido ?: 0) == 1
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isChecked) Color(0xFFE8F5E9)
                                else MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    item.nome,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                Switch(
                                    checked = isChecked,
                                    onCheckedChange = {
                                        prodViewModel.toggleChecklist(
                                            item.idChecklist, "embalagem", it
                                        )
                                        if (it) HapticHelper.success(context)
                                    },
                                    modifier = Modifier.size(width = 52.dp, height = 32.dp),
                                    thumbContent = if (isChecked) {
                                        { Icon(Icons.Default.Check, null, Modifier.size(14.dp)) }
                                    } else null
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Aviso: checklist concluída mas ordem ainda não está no estado Concluída (2)
            // A API marcar-embalada requer ordem CONCLUÍDA — bloquear até existir endpoint de conclusão por unidade
            if (isComplete && !ordemConcluida) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Checklist de embalagem concluída.\n\nA API atual requer que a ordem esteja Concluída para registar a expedição, mas a ordem ainda está Em Produção. Para evitar fechar a ordem inteira por engano, a conclusão final está bloqueada.\n\nContacte o supervisor ou aguarde que o sistema de gestão conclua a ordem.",
                        modifier = Modifier.padding(12.dp),
                        color = Color(0xFFE65100),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Botão MARCAR COMO EMBALADA
            // Só ativo se todos os itens estiverem concluídos E a ordem já estiver em estado Concluída (2)
            Button(
                onClick = { showConfirmDialog = true },
                enabled = isComplete && ordemConcluida && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isComplete && ordemConcluida) StatusSuccess else Color.LightGray
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "A PROCESSAR...",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                } else {
                    Text(
                        when {
                            !isComplete -> "COMPLETE TODOS OS ITENS DE EMBALAGEM"
                            !ordemConcluida -> "AGUARDA CONCLUSÃO DA ORDEM"
                            else -> "MARCAR COMO EMBALADA"
                        },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
