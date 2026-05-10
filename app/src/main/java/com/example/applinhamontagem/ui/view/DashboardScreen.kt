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
import com.example.applinhamontagem.data.remote.dto.MotaAtribuidaDto
import com.example.applinhamontagem.ui.navigation.Screen
import com.example.applinhamontagem.ui.viewmodel.AuthViewModel
import com.example.applinhamontagem.ui.viewmodel.MotaUiStatus
import com.example.applinhamontagem.ui.viewmodel.ProductionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    prodViewModel: ProductionViewModel
) {
    val user = authViewModel.user
    val uiState by prodViewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(user?.userId) {
        // idUtilizador é o Int operacional (tabela Utilizadores) — distinto do GUID userId do Identity
        prodViewModel.loadMinhasMotas(user?.idUtilizador?.toString() ?: "")
    }

    Scaffold(
        containerColor = Color(0xFFF2F4F8),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "A-MOVER",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Operador: ${user?.username ?: "—"}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        authViewModel.logout(context)
                        navController.navigate(Screen.Login.route) { popUpTo(0) }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sair")
                    }
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

        uiState.errorMessage?.let { msg ->
            if (uiState.minhasAtribuidas.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(msg, color = MaterialTheme.colorScheme.error)
                }
                return@Scaffold
            }
        }

        if (uiState.minhasAtribuidas.isEmpty() && !uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Nenhuma mota atribuída.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Motas em curso (Em Produção e Ativas)
                val emCurso = uiState.minhasAtribuidas.filter {
                    val s = prodViewModel.getStatusMota(it)
                    s == MotaUiStatus.EM_PRODUCAO || s == MotaUiStatus.ATIVA
                }
                if (emCurso.isNotEmpty()) {
                    item { SectionHeader("EM CURSO") }
                    items(emCurso) { mota ->
                        val status = prodViewModel.getStatusMota(mota)
                        MotaCardReal(mota, status) {
                            prodViewModel.selectFromDashboard(mota)
                            navController.navigate(
                                Screen.RegisterVin.createRoute(mota.motaId, mota.idOrdemProducao ?: 0)
                            )
                        }
                    }
                }

                // Motas com estados especiais (manutenção, descontinuadas, desconhecido)
                val outras = uiState.minhasAtribuidas.filter {
                    val s = prodViewModel.getStatusMota(it)
                    s != MotaUiStatus.EM_PRODUCAO && s != MotaUiStatus.ATIVA
                }
                if (outras.isNotEmpty()) {
                    item { SectionHeader("OUTRAS") }
                    items(outras) { mota ->
                        val status = prodViewModel.getStatusMota(mota)
                        MotaCardReal(mota, status) {
                            Toast.makeText(
                                context,
                                "Estado: ${status.label}. Contacte o supervisor se necessário.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
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
fun MotaCardReal(mota: MotaAtribuidaDto, status: MotaUiStatus, onClick: () -> Unit) {
    val isInteractive = status == MotaUiStatus.EM_PRODUCAO || status == MotaUiStatus.ATIVA
    val statusColor = when (status) {
        MotaUiStatus.EM_PRODUCAO -> Color(0xFFF9A825)
        MotaUiStatus.ATIVA -> Color(0xFF1976D2)
        MotaUiStatus.EM_MANUTENCAO -> Color(0xFFE65100)
        MotaUiStatus.DESCONTINUADA -> Color(0xFF616161)
        MotaUiStatus.DESCONHECIDO -> Color(0xFF9E9E9E)
    }

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(if (isInteractive) 4.dp else 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(
                Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(statusColor)
            )
            Column(
                Modifier
                    .padding(16.dp)
                    .weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "OP #${mota.idOrdemProducao}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Spacer(Modifier.weight(1f))
                    Surface(
                        color = statusColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            status.label,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    mota.numeroIdentificacao ?: "Sem VIN",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Modelo ${mota.idModelo} · ${mota.cor ?: "—"}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (isInteractive) {
                Box(
                    Modifier
                        .align(Alignment.CenterVertically)
                        .padding(end = 16.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.LightGray)
                }
            } else {
                Box(
                    Modifier
                        .align(Alignment.CenterVertically)
                        .padding(end = 16.dp)
                ) {
                    Icon(Icons.Default.Warning, null, tint = statusColor.copy(alpha = 0.5f))
                }
            }
        }
    }
}
