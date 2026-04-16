package com.example.applinhamontagem.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.applinhamontagem.ui.components.DynamicCheckItem
import com.example.applinhamontagem.ui.navigation.Screen
import com.example.applinhamontagem.ui.viewmodel.ProductionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicQualityScreen(
    navController: NavController,
    viewModel: ProductionViewModel,
    motaId: Int,
    ordemId: Int
) {
    val uiState by viewModel.uiState.collectAsState()
    val checklists = uiState.checklists

    val montagemOk = checklists?.montagem?.all { it.verificado == 1 } ?: false
    val controloOk = checklists?.controlo?.all { it.controloFinal == 1 } ?: false
    val podeEmbalar = montagemOk && controloOk

    // Cores para simular "desativado" sem usar enabled=
    val fabContainer = if (podeEmbalar) MaterialTheme.colorScheme.secondary
    else MaterialTheme.colorScheme.surfaceVariant

    val fabContent = if (podeEmbalar) MaterialTheme.colorScheme.onSecondary
    else MaterialTheme.colorScheme.onSurfaceVariant

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Controlo de Qualidade (OP #$ordemId)") }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (podeEmbalar) {
                        navController.navigate(Screen.Packaging.createRoute(motaId))
                    }
                },
                containerColor = fabContainer,
                contentColor = fabContent,
                icon = { Icon(Icons.Default.Inventory, contentDescription = "Embalar") },
                text = { Text(if (podeEmbalar) "Embalar" else "Falta validar") }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading && checklists == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) { Text(uiState.errorMessage ?: "Erro desconhecido.") }
            }

            checklists == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {

                    if (!podeEmbalar) {
                        Text(
                            text = "⚠️ Para avançar para Embalagem, valida toda a Montagem e os Testes Finais.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Secção Montagem
                    Text(
                        "Verificação de Montagem",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    checklists.montagem.forEach { task ->
                        DynamicCheckItem(
                            descricao = task.nome,
                            isChecked = task.verificado == 1,
                            onCheckedChange = { isChecked ->
                                viewModel.toggleChecklist(task.idChecklist, "montagem", isChecked)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Secção Controlo Final
                    Text(
                        "Testes Finais",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    checklists.controlo.forEach { task ->
                        DynamicCheckItem(
                            descricao = task.nome,
                            isChecked = task.controloFinal == 1,
                            onCheckedChange = { isChecked ->
                                viewModel.toggleChecklist(task.idChecklist, "controlo", isChecked)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}
