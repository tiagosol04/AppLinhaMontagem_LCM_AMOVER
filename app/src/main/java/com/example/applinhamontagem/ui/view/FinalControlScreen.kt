package com.example.applinhamontagem.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
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
import com.example.applinhamontagem.ui.viewmodel.ProductionStep
import com.example.applinhamontagem.ui.viewmodel.ProductionViewModel
import com.example.applinhamontagem.ui.viewmodel.QcState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinalControlScreen(navController: NavController, prodViewModel: ProductionViewModel, motaId: Int, ordemId: Int) {
    val uiState by prodViewModel.uiState.collectAsState()
    val itens = uiState.checklists?.controlo ?: emptyList()
    val isReady = uiState.isFinalControlComplete
    val context = LocalContext.current

    LaunchedEffect(Unit) { prodViewModel.setStep(ProductionStep.CONTROLO) }

    fun getState(id: Int): QcState {
        val override = uiState.qcOverrides[id]
        if (override != null) return override
        val server = itens.find { it.idChecklist == id }?.controloFinal
        return if (server == 1) QcState.PASSOU else QcState.PENDENTE
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Controlo de Qualidade", fontWeight = FontWeight.Bold) },
                    navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar") } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
                StepperIndicator(currentStep = ProductionStep.CONTROLO)
            }
        },
        bottomBar = {
            Button(
                onClick = {
                    if (isReady) {
                        HapticHelper.success(context)
                        navController.navigate(Screen.Packaging.createRoute(motaId))
                    }
                },
                enabled = isReady,
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isReady) StatusSuccess else Color.LightGray)
            ) {
                Text(if (isReady) "TUDO OK - AVANCAR P/ EMBALAGEM" else "RESOLVA AS FALHAS PENDENTES", fontWeight = FontWeight.Bold)
                if (isReady) { Spacer(Modifier.width(12.dp)); Icon(Icons.AutoMirrored.Filled.ArrowForward, null) }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF0F2F5)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(itens) { item ->
                QcCard(
                    nome = item.nome,
                    state = getState(item.idChecklist),
                    onPass = { prodViewModel.qcAprovar(item.idChecklist); HapticHelper.success(context) },
                    onFail = { prodViewModel.qcReprovar(item.idChecklist) }
                )
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun QcCard(nome: String, state: QcState, onPass: () -> Unit, onFail: () -> Unit) {
    val (bgColor, icon, iconColor) = when (state) {
        QcState.PASSOU -> Triple(Color(0xFFE8F5E9), Icons.Default.CheckCircle, Color(0xFF2E7D32))
        QcState.CORRIGIDO -> Triple(Color(0xFFE3F2FD), Icons.Default.Build, Color(0xFF1565C0))
        QcState.FALHOU -> Triple(Color(0xFFFFEBEE), Icons.Default.Warning, Color(0xFFC62828))
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
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(12.dp))
                }
                Text(nome, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                if (state != QcState.PENDENTE) {
                    Surface(color = iconColor.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
                        Text(state.name, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, color = iconColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (state == QcState.FALHOU) {
                    Button(onClick = onPass, modifier = Modifier.weight(1f).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)), shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Default.Build, null); Spacer(Modifier.width(8.dp)); Text("CORRIGIR FALHA", fontWeight = FontWeight.Bold)
                    }
                } else {
                    OutlinedButton(onClick = onFail, modifier = Modifier.weight(1f).height(56.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)), shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Default.ThumbDown, null); Spacer(Modifier.width(8.dp)); Text("REPROVAR", fontWeight = FontWeight.Bold)
                    }
                    Button(onClick = onPass, modifier = Modifier.weight(1f).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = if (state == QcState.PASSOU) Color(0xFF2E7D32) else Color(0xFF43A047)), shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Default.ThumbUp, null); Spacer(Modifier.width(8.dp)); Text("APROVAR", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
