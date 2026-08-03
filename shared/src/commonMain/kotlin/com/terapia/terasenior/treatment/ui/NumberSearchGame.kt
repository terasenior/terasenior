package com.terapia.terasenior.treatment.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Barra de Navegación
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
                Text(
                    text = "Busca el Número",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Panel del Objetivo
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "BUSCA ESTE NÚMERO:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(100.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = targetNumber.toString(),
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Cuadrícula de Juego
            val gridSize = 5
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (row in 0 until gridSize) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (col in 0 until gridSize) {
                            val index = row * gridSize + col
                            val cell = grid[index]
                            NumberCell(
                                cell = cell,
                                onClick = {
                                    if (cell.isFound || isCompleted) return@NumberCell

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

            Spacer(modifier = Modifier.weight(1f))

            // Estado y Botón
            AnimatedVisibility(
                visible = isCompleted,
                enter = fadeIn() + expandVertically()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFC8E6C9)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "¡Excelente! Has encontrado todos los números.",
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = Color(0xFF1B5E20),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Button(
                onClick = {
                    targetNumber = Random.nextInt(10)
                    grid = generateGrid(targetNumber)
                    totalTargets = countTargets(grid, targetNumber)
                    foundCount = 0
                    isCompleted = false
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(if (isCompleted) "Siguiente Ejercicio" else "Reiniciar Ejercicio", fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun NumberCell(
    cell: Cell,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        cell.isFound -> Color(0xFF81C784)
        cell.isWrong -> Color(0xFFFF8A80)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    val scale by animateFloatAsState(
        targetValue = if (cell.isFound) 1.05f else 1f
    )

    Box(
        modifier = Modifier
            .size(60.dp)
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .clickable(enabled = !cell.isFound, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = cell.number.toString(),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = when {
                cell.isFound -> Color.White
                cell.isWrong -> Color.White
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

private fun generateGrid(target: Int): List<Cell> {
    val size = 25
    val cells = mutableListOf<Cell>()
    var targetCount = 0

    repeat(size) {
        val isTarget = Random.nextFloat() < 0.20f
        val num = if (isTarget) {
            targetCount++
            target
        } else {
            var n = Random.nextInt(10)
            while (n == target) n = Random.nextInt(10)
            n
        }
        cells.add(Cell(number = num))
    }

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
