package com.terapia.terasenior.treatment.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Componente de Cuadrícula Responsiva que ajusta el tamaño de los elementos 
 * para que siempre quepan en la pantalla disponible sin hacer scroll (v1.3.3).
 */
@Composable
fun <T> ResponsiveGrid(
    items: List<T>,
    columns: Int,
    modifier: Modifier = Modifier,
    spacing: Dp = 12.dp,
    content: @Composable (Int, T, Dp) -> Unit
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val availableWidth = maxWidth
        val availableHeight = maxHeight
        
        if (items.isEmpty()) return@BoxWithConstraints

        val numRows = kotlin.math.ceil(items.size.toDouble() / columns).toInt()
        
        val cellWidth = (availableWidth - (spacing * (columns + 1))) / columns
        val cellHeight = (availableHeight - (spacing * (numRows + 1))) / numRows
        
        val itemSize = minOf(cellWidth, cellHeight)

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterVertically),
            contentPadding = PaddingValues(spacing),
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = false
        ) {
            itemsIndexed(items) { index, item ->
                content(index, item, itemSize)
            }
        }
    }
}
