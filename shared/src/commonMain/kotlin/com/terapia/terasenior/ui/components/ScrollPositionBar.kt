package com.terapia.terasenior.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** A visible, draggable alternative to mouse-wheel scrolling on web canvases. */
@Composable
fun ScrollPositionBar(
    state: ScrollState,
    modifier: Modifier = Modifier
) {
    if (state.maxValue == 0) return

    val scope = rememberCoroutineScope()
    Slider(
        value = state.value.toFloat(),
        onValueChange = { value -> scope.launch { state.scrollTo(value.roundToInt()) } },
        valueRange = 0f..state.maxValue.toFloat(),
        modifier = modifier.fillMaxWidth()
    )
}

/** A visible, draggable position bar for lists with an arbitrary number of items. */
@Composable
fun ListScrollPositionBar(
    state: LazyListState,
    itemCount: Int,
    modifier: Modifier = Modifier
) {
    if (itemCount <= 1) return

    val scope = rememberCoroutineScope()
    val lastIndex = itemCount - 1
    Slider(
        value = state.firstVisibleItemIndex.coerceIn(0, lastIndex).toFloat(),
        onValueChange = { value ->
            scope.launch { state.scrollToItem(value.roundToInt().coerceIn(0, lastIndex)) }
        },
        valueRange = 0f..lastIndex.toFloat(),
        modifier = modifier.fillMaxWidth()
    )
}
