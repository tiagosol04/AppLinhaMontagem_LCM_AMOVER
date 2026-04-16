package com.example.applinhamontagem.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.applinhamontagem.ui.viewmodel.PecaUiItem

@Composable
fun DynamicPartItem(
    item: PecaUiItem,
    onScanClick: () -> Unit
) {
    val isDone = item.isConcluido
    val cardColor = if (isDone) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surface
    val borderColor = if (isDone) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error

    val desc = item.defModelo.descricao ?: "—"
    val pn = item.defModelo.partNumber ?: "—"
    val sn = item.montado?.numeroSerie ?: "—"

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = if (!isDone) BorderStroke(1.dp, borderColor) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(desc, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("PN: $pn", style = MaterialTheme.typography.bodySmall)

                if (isDone) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("SN: $sn", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                }
            }

            if (isDone) {
                Icon(Icons.Default.CheckCircle, "Concluído", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(32.dp))
            } else {
                Button(
                    onClick = onScanClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, "Scan", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SCAN")
                }
            }
        }
    }
}