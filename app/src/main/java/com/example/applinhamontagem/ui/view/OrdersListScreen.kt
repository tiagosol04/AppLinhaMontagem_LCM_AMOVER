package com.example.applinhamontagem.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.applinhamontagem.ui.navigation.Screen
import com.example.applinhamontagem.ui.viewmodel.OrdersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersListScreen(navController: NavController, viewModel: OrdersViewModel) {
    LaunchedEffect(Unit) { viewModel.loadOrdens() }

    Scaffold(topBar = { TopAppBar(title = { Text("Ordens de Produção") }) }) { padding ->
        if (viewModel.isLoading) LinearProgressIndicator()
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
            items(viewModel.ordens) { ordem ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                        navController.navigate(Screen.OrderDetail.createRoute(ordem.idOrdemProducao))
                    }
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Ordem #${ordem.numeroOrdem}", style = MaterialTheme.typography.titleMedium)
                        Text("Estado: ${ordem.estado}")
                    }
                }
            }
        }
    }
}