package com.example.applinhamontagem.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.applinhamontagem.ui.components.DynamicCheckItem
import com.example.applinhamontagem.ui.navigation.Screen
import com.example.applinhamontagem.ui.viewmodel.ProductionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostAssemblyScreen(navController: NavController, viewModel: ProductionViewModel, motaId: Int, ordemId: Int) {
    val uiState by viewModel.uiState.collectAsState()
    val isComplete = uiState.isPostAssemblyComplete

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("2. Pós-Montagem") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { if (isComplete) navController.navigate(Screen.FinalControl.createRoute(motaId, ordemId)) },
                containerColor = if (isComplete) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant
            ) { Icon(Icons.AutoMirrored.Filled.ArrowForward, "Seguinte") }
        }
    ) { padding ->
        val list = uiState.checklists?.montagem ?: emptyList()
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
            items(list) { item ->
                DynamicCheckItem(
                    descricao = item.nome,
                    isChecked = item.verificado == 1,
                    onCheckedChange = { viewModel.toggleChecklist(item.idChecklist, "montagem", it) }
                )
            }
        }
    }
}