package com.terapia.terasenior.treatment.repository

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class CatalogItem(
    val name: String,
    val icon: ImageVector,
    val imageUrl: String,
    val category: String
)

/**
 * Catálogo centralizado de contenido real para ejercicios (v1.3.3).
 */
object ExerciseContentCatalog {
    val items = listOf(
        CatalogItem("Manzana", Icons.Default.Restaurant, "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=400", "Frutas"),
        CatalogItem("Perro", Icons.Default.Pets, "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=400", "Animales"),
        CatalogItem("Reloj", Icons.Default.WatchLater, "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400", "Objetos"),
        CatalogItem("Taza", Icons.Default.Coffee, "https://images.unsplash.com/photo-1585059895324-582b12879c73?w=400", "Hogar"),
        CatalogItem("Cama", Icons.Default.Bed, "https://images.unsplash.com/photo-1505691938895-1758d7feb511?w=400", "Hogar"),
        CatalogItem("Silla", Icons.Default.Chair, "https://images.unsplash.com/photo-1551488831-00ddcb6c6bd3?w=400", "Hogar"),
        CatalogItem("Mesa", Icons.Default.TableBar, "https://images.unsplash.com/photo-1583847268964-b28dc2f51ac9?w=400", "Hogar"),
        CatalogItem("Teléfono", Icons.Default.Phone, "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400", "Objetos"),
        CatalogItem("Libro", Icons.AutoMirrored.Filled.MenuBook, "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=400", "Objetos"),
        CatalogItem("Gato", Icons.Default.Pets, "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=400", "Animales")
    )
}
