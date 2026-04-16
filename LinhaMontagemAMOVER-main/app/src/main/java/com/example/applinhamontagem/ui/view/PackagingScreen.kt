package com.example.applinhamontagem.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.applinhamontagem.ui.navigation.Screen
import com.example.applinhamontagem.ui.viewmodel.AuthViewModel
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

    // ✅ LÓGICA DE FINALIZAÇÃO
    fun onFinalizarClick() {
        val uid = authViewModel.user?.userId ?: return
        prodViewModel.finalizarMotaEVoltar(uid) {
            // Callback: Só volta à dashboard depois do servidor confirmar
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Dashboard.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("4. Embalagem", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Verifique o conteúdo:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom=16.dp))

            if (uiState.errorMessage != null) {
                Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(itens) { item ->
                    val isChecked = item.incluido == 1
                    Card(
                        modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = if(isChecked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                    ) {
                        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Text(item.nome, fontWeight = FontWeight.SemiBold)
                            Switch(
                                checked = isChecked,
                                onCheckedChange = { prodViewModel.toggleChecklist(item.idChecklist, "embalagem", it) },
                                thumbContent = if(isChecked) {{ Icon(Icons.Default.Check, null, Modifier.size(12.dp)) }} else null
                            )
                        }
                    }
                }
            }

            // ✅ BOTÃO FINAL
            Button(
                onClick = { onFinalizarClick() },
                enabled = isComplete && !isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("A FINALIZAR...")
                } else {
                    Text("CONCLUIR PRODUÇÃO", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}