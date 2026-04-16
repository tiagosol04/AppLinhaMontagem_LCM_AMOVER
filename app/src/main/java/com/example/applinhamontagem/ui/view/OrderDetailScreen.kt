package com.example.applinhamontagem.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.applinhamontagem.ui.viewmodel.OrdersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(navController: NavController, viewModel: OrdersViewModel, orderId: Int) {
    var ordem by remember { mutableStateOf(viewModel.ordens.find { it.idOrdemProducao == orderId }) }

    LaunchedEffect(orderId) {
        if (ordem == null) viewModel.getOrdemById(orderId).onSuccess { ordem = it }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Detalhes da Ordem") }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            ordem?.let { o ->
                Text("Número: ${o.numeroOrdem}", style = MaterialTheme.typography.headlineMedium)
                Text("Cliente ID: ${o.idCliente}")
                Text("País: ${o.paisDestino}")
            } ?: CircularProgressIndicator()

            Spacer(Modifier.height(16.dp))
            Button(onClick = { navController.popBackStack() }) { Text("Voltar") }
        }
    }
}