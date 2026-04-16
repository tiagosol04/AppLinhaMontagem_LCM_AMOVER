package com.example.applinhamontagem.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import com.example.applinhamontagem.data.utils.HapticHelper
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.applinhamontagem.ui.components.DynamicCheckItem
import com.example.applinhamontagem.ui.components.StepperIndicator
import com.example.applinhamontagem.ui.navigation.Screen
import com.example.applinhamontagem.ui.theme.StatusSuccess
import com.example.applinhamontagem.ui.viewmodel.ProductionStep
import com.example.applinhamontagem.ui.viewmodel.ProductionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostAssemblyScreen(navController: NavController, viewModel: ProductionViewModel, motaId: Int, ordemId: Int) {
    val uiState by viewModel.uiState.collectAsState()
    val isComplete = uiState.isPostAssemblyComplete
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.setStep(ProductionStep.POS_MONTAGEM) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Verificacao Pos-Montagem", fontWeight = FontWeight.Bold) },
                    navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar") } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
                StepperIndicator(currentStep = ProductionStep.POS_MONTAGEM)
            }
        },
        bottomBar = {
            Button(
                onClick = {
                    if (isComplete) {
                        HapticHelper.success(context)
                        navController.navigate(Screen.FinalControl.createRoute(motaId, ordemId))
                    }
                },
                enabled = isComplete,
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isComplete) StatusSuccess else Color.LightGray)
            ) {
                Text("AVANCAR PARA CONTROLO", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.width(12.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
            }
        }
    ) { padding ->
        val list = uiState.checklists?.montagem ?: emptyList()

        if (list.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator()
                        Text("A carregar checklists...", color = Color.Gray)
                    } else {
                        Text("Sem itens de verificação.", color = Color.Gray)
                        Button(
                            onClick = { uiState.currentMota?.let { viewModel.selectMota(it) } },
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("RECARREGAR") }
                    }
                }
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Text("Valide cada ponto antes de avancar:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
                }
                items(list) { item ->
                    DynamicCheckItem(
                        descricao = item.nome,
                        isChecked = (item.verificado ?: 0) == 1,
                        onCheckedChange = { viewModel.toggleChecklist(item.idChecklist, "montagem", it) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}
