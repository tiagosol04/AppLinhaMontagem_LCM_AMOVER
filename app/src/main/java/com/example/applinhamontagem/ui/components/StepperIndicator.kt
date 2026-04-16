package com.example.applinhamontagem.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.applinhamontagem.ui.theme.PrimaryBlue
import com.example.applinhamontagem.ui.theme.StatusSuccess
import com.example.applinhamontagem.ui.viewmodel.ProductionStep

@Composable
fun StepperIndicator(
    currentStep: ProductionStep,
    completedSteps: Set<ProductionStep> = emptySet(),
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProductionStep.entries.forEachIndexed { idx, step ->
            val isCurrent = step == currentStep
            val isCompleted = step in completedSteps || step.index < currentStep.index

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(if (isCurrent) 36.dp else 28.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCurrent -> PrimaryBlue
                                isCompleted -> StatusSuccess
                                else -> Color(0xFFE0E0E0)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isCompleted && !isCurrent) "\u2713" else "${step.index + 1}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = if (isCurrent) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = step.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCurrent) PrimaryBlue else Color.Gray
                )
            }

            if (idx < ProductionStep.entries.size - 1) {
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(2.dp)
                        .background(if (isCompleted) StatusSuccess else Color(0xFFE0E0E0))
                )
            }
        }
    }
}
