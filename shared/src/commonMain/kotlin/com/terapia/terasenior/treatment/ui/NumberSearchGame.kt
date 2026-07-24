package com.terapia.terasenior.treatment.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

data class Cell(
    val number: Int,
    val isFound: Boolean = false,
    val isWrong: Boolean = false
)

@Composable
fun NumberSearchGame(
    onBack: () -> Unit
) {
    var targetNumber by remember { mutableIntStateOf(Random.nextInt(10)) }
    var grid by remember { mutableStateOf(generateGrid(targetNumber)) }
    var foundCount by remember { mutableIntStateOf(0) }
    var totalTargets by remember { mutableIntStateOf(countTargets(grid, targetNumber)) }
    var isCompleted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Barra superior
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text(
                    text = "← Volver",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "Busca el Número",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(80.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Modelo
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MODELO:",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = targetNumber.toString(),
                            fontSize = 48.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Cuadrícula 5x5
        val gridSize = 5
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (row in 0 until gridSize) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (col in 0 until gridSize) {
                        val index = row * gridSize + col
                        val cell = grid[index]
                        NumberCell(
                            cell = cell,
                            onClick = {
                                if (cell.isFound) return@NumberCell

                                if (cell.number == targetNumber) {
                                    val newGrid = grid.toMutableList()
                                    newGrid[index] = cell.copy(isFound = true)
                                    grid = newGrid
                                    foundCount++
                                    if (foundCount >= totalTargets) {
                                        isCompleted = true
                                    }
                                } else {
                                    val newGrid = grid.toMutableList()
                                    newGrid[index] = cell.copy(isWrong = true)
                                    grid = newGrid
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Estadísticas
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isCompleted) Color(0xFFC8E6C9) else Color(0xFFFFF3E0)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "✅ Encontrados: $foundCount",
                    fontWeight = FontWeight.SemiBold,
                    color = if (isCompleted) Color(0xFF1B5E20) else Color(0xFFE65100)
                )
                Text(
                    text = "🎯 Total: $totalTargets",
                    fontWeight = FontWeight.SemiBold,
                    color = if (isCompleted) Color(0xFF1B5E20) else Color(0xFFE65100)
                )
            }
        }

        if (isCompleted) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "¡Ejercicio completado! 🎉",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                targetNumber = Random.nextInt(10)
                grid = generateGrid(targetNumber)
                totalTargets = countTargets(grid, targetNumber)
                foundCount = 0
                isCompleted = false
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🔄 Nuevo Ejercicio")
        }
    }
}

@Composable
private fun NumberCell(
    cell: Cell,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        cell.isFound -> Color(0xFFA5D6A7)
        cell.isWrong -> Color(0xFFFFCDD2)
        else -> Color(0xFFFAFAFA)
    }

    val scale by animateFloatAsState(
        targetValue = if (cell.isFound) 1.1f else 1f
    )

    Box(
        modifier = Modifier
            .size(56.dp)
            .scale(scale)
            .background(backgroundColor, RoundedCornerShape(10.dp))
            .clickable(enabled = !cell.isFound, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = cell.number.toString(),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = when {
                cell.isFound -> Color(0xFF1B5E20)
                cell.isWrong -> Color(0xFFB71C1C)
                else -> Color(0xFF212121)
            }
        )
    }
}

private fun generateGrid(target: Int): List<Cell> {
    val size = 25
    val cells = mutableListOf<Cell>()
    var targetCount = 0

    repeat(size) {
        val isTarget = Random.nextFloat() < 0.25f
        val num = if (isTarget) {
            targetCount++
            target
        } else {
            Random.nextInt(10)
        }
        cells.add(Cell(number = num))
    }

    // Asegurar al menos 3 objetivos
    while (targetCount < 3) {
        val idx = Random.nextInt(size)
        if (cells[idx].number != target) {
            cells[idx] = Cell(number = target)
            targetCount++
        }
    }

    return cells
}

private fun countTargets(grid: List<Cell>, target: Int): Int {
    return grid.count { it.number == target }
}