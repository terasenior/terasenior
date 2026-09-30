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
        CatalogItem("Manzana", Icons.Default.Fastfood, RealisticExerciseImageCatalog.apple, "Frutas"),
        CatalogItem("Perro", Icons.Default.Pets, RealisticExerciseImageCatalog.dog, "Animales"),
        CatalogItem("Reloj", Icons.Default.WatchLater, RealisticExerciseImageCatalog.clock, "Objetos"),
        CatalogItem("Taza", Icons.Default.Coffee, RealisticExerciseImageCatalog.cup, "Hogar"),
        CatalogItem("Silla", Icons.Default.Chair, RealisticExerciseImageCatalog.chair, "Hogar"),
        CatalogItem("Teléfono", Icons.Default.Phone, RealisticExerciseImageCatalog.phone, "Objetos"),
        CatalogItem("Libro", Icons.AutoMirrored.Filled.MenuBook, RealisticExerciseImageCatalog.book, "Objetos"),
        CatalogItem("Plátano", Icons.Default.Fastfood, RealisticExerciseImageCatalog.banana, "Frutas"),
        CatalogItem("Autobús", Icons.Default.DirectionsBus, RealisticExerciseImageCatalog.bus, "Transporte"),
        CatalogItem("Gato", Icons.Default.Pets, RealisticExerciseImageCatalog.cat, "Animales")
    )
}
