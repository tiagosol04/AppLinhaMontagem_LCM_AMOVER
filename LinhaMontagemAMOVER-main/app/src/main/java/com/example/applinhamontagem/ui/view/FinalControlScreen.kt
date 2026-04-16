package com.example.applinhamontagem.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.applinhamontagem.ui.navigation.Screen
import com.example.applinhamontagem.ui.viewmodel.ProductionViewModel
import com.example.applinhamontagem.ui.viewmodel.QcState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinalControlScreen(navController: NavController, prodViewModel: ProductionViewModel, motaId: Int, ordemId: Int) {
    val uiState by prodViewModel.uiState.collectAsState()
    val itens = uiState.checklists?.controlo ?: emptyList()
    val isReady = uiState.isFinalControlComplete

    fun getState(id: Int): QcState {
        val override = uiState.qcOverrides[id]
        if (override != null) return override
        val server = itens.find { it.idChecklist == id }?.controloFinal
        return if (server == 1) QcState.PASSOU else QcState.PENDENTE
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Controlo de Qualidade", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar") } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Button(
                onClick = { navController.navigate(Screen.Packaging.createRoute(motaId)) },
                enabled = isReady,
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if(isReady) Color(0xFF2E7D32) else Color.LightGray)
            ) {
                Text(if(isReady) "TUDO OK - SEGUIR P/ EMBALAGEM" else "RESOLVA AS FALHAS PENDENTES")
                if(isReady) {
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF0F2F5)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(itens) { item ->
                val state = getState(item.idChecklist)
                QcCard(
                    nome = item.nome,
                    state = state,
                    onPass = { prodViewModel.qcAprovar(item.idChecklist) },
                    onFail = { prodViewModel.qcReprovar(item.idChecklist) }
                )
            }
        }
    }
}

@Composable
fun QcCard(nome: String, state: QcState, onPass: () -> Unit, onFail: () -> Unit) {
    val (bgColor, icon, iconColor) = when(state) {
        QcState.PASSOU -> Triple(Color(0xFFE8F5E9), Icons.Default.CheckCircle, Color(0xFF2E7D32)) // Verde Suave
        QcState.CORRIGIDO -> Triple(Color(0xFFE3F2FD), Icons.Default.Build, Color(0xFF1565C0)) // Azul
        QcState.FALHOU -> Triple(Color(0xFFFFEBEE), Icons.Default.Warning, Color(0xFFC62828)) // Vermelho
        QcState.PENDENTE -> Triple(Color.White, null, Color.Gray)
    }

    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                }
                Text(nome, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))

                // Badge de Estado
                if (state != QcState.PENDENTE) {
                    Surface(
                        color = iconColor.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = state.name,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = iconColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Lógica de Botões Inteligente
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (state == QcState.FALHOU) {
                    // Se falhou, só mostra botão de CORRIGIR
                    Button(
                        onClick = onPass, // Passar depois de falhar = Corrigir
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                    ) {
                        Icon(Icons.Default.Build, null)
                        Spacer(Modifier.width(8.dp))
                        Text("CORRIGIR FALHA")
                    }
                } else {
                    // Estado Normal: Aprovar ou Reprovar
                    // Botão Reprovar (Outline)
                    OutlinedButton(
                        onClick = onFail,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F))
                    ) {
                        Icon(Icons.Default.ThumbDown, null)
                        Spacer(Modifier.width(8.dp))
                        Text("REPROVAR")
                    }

                    // Botão Aprovar (Filled)
                    Button(
                        onClick = onPass,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = if(state == QcState.PASSOU) Color(0xFF2E7D32) else Color(0xFF43A047))
                    ) {
                        Icon(Icons.Default.ThumbUp, null)
                        Spacer(Modifier.width(8.dp))
                        Text("APROVAR")
                    }
                }
            }
        }
    }
}