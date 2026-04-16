package com.example.applinhamontagem.ui.view

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
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
import com.example.applinhamontagem.data.remote.dto.MotaAtribuidaDto
import com.example.applinhamontagem.ui.navigation.Screen
import com.example.applinhamontagem.ui.viewmodel.AuthViewModel
import com.example.applinhamontagem.ui.viewmodel.MotaStatus
import com.example.applinhamontagem.ui.viewmodel.ProductionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController, authViewModel: AuthViewModel, prodViewModel: ProductionViewModel) {
    val user = authViewModel.user
    val uiState by prodViewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(user?.userId) {
        user?.userId?.let { prodViewModel.loadMinhasMotas(it) }
    }

    Scaffold(
        containerColor = Color(0xFFF2F4F8),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("A-MOVER", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Operador: ${user?.username}", style = MaterialTheme.typography.labelSmall)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) { popUpTo(0) }
                    }) { Icon(Icons.AutoMirrored.Filled.ExitToApp, "Sair") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.IdentifyMoto.route) },
                icon = { Icon(Icons.Default.QrCodeScanner, null) },
                text = { Text("NOVA UNIDADE", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        if (uiState.minhasAtribuidas.isEmpty() && !uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nenhuma mota atribuída.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Secção Em Progresso
                item { SectionHeader("EM PRODUÇÃO") }
                items(uiState.minhasAtribuidas.filter { prodViewModel.getStatusMota(it) != MotaStatus.CONCLUIDA }) { mota ->
                    MotaCardReal(mota, prodViewModel.getStatusMota(mota)) {
                        prodViewModel.selectFromDashboard(mota)
                        navController.navigate(Screen.RegisterVin.createRoute(mota.motaId, mota.idOrdemProducao ?: 0))
                    }
                }

                // Secção Concluídos
                item { SectionHeader("FINALIZADAS") }
                items(uiState.minhasAtribuidas.filter { prodViewModel.getStatusMota(it) == MotaStatus.CONCLUIDA }) { mota ->
                    MotaCardReal(mota, MotaStatus.CONCLUIDA) {
                        Toast.makeText(context, "Mota Finalizada. Acesso bloqueado.", Toast.LENGTH_SHORT).show()
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = Color.Gray,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun MotaCardReal(mota: MotaAtribuidaDto, status: MotaStatus, onClick: () -> Unit) {
    val isDone = status == MotaStatus.CONCLUIDA
    val statusColor = if(isDone) Color(0xFF2E7D32) else Color(0xFFF9A825)

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(if(isDone) 0.dp else 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(Modifier.width(6.dp).fillMaxHeight().background(statusColor))
            Column(Modifier.padding(16.dp).weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("OP #${mota.idOrdemProducao}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Spacer(Modifier.weight(1f))
                    if(isDone) Icon(Icons.Default.CheckCircle, null, tint = statusColor, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.height(4.dp))
                Text(mota.numeroIdentificacao ?: "Sem VIN", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Modelo ${mota.idModelo} • ${mota.cor}", style = MaterialTheme.typography.bodyMedium)
            }
            if(!isDone) {
                Box(Modifier.align(Alignment.CenterVertically).padding(end=16.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.LightGray)
                }
            } else {
                Box(Modifier.align(Alignment.CenterVertically).padding(end=16.dp)) {
                    Icon(Icons.Default.Lock, null, tint = Color.LightGray)
                }
            }
        }
    }
}