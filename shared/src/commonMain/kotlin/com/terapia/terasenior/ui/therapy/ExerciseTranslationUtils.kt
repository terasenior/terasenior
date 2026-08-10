package com.terapia.terasenior.ui.therapy

/**
 * Utilidad centralizada para traducir los nombres técnicos de los ejercicios
 * a lenguaje humano entendible por el usuario/terapeuta.
 */
object ExerciseTranslationUtils {
    fun getDisplayName(type: String): String {
        val cleanType = type.trim().lowercase()
        return when (cleanType) {
            // Orientación
            "orientation_temporal" -> "Orientación Temporal"
            
            // Atención
            "number_search" -> "Busca el Número"
            "attention_different", "attention_spot_odd_one_out" -> "El Intruso"
            "attention_equals_model" -> "Buscar Iguales"
            "attention_positions" -> "Orientación Espacial"
            "attention_letters" -> "Búsqueda de Letras"
            "attention_numbers" -> "Búsqueda de Números"
            "attention_symbols" -> "Búsqueda de Símbolos"
            "attention_matrices" -> "Matrices de Atención"
            "attention_row_cancel" -> "Tachado por Filas"
            "attention_consecutive" -> "Números Consecutivos"
            "attention_yes_no" -> "Tachar Sí/No"
            "attention_dual_task" -> "Tarea Dual"
            "attention_count" -> "Contar Dibujos"
            "attention_longest" -> "Discriminación de Longitud"
            "attention_differences" -> "Encontrar Diferencias"
            "attention_missing_part" -> "Completar Dibujo"
            "attention_word_search" -> "Sopa de Letras"
            
            // Memoria
            "memory_cultural" -> "Memoria Cultural"
            "memory_utility" -> "Utilidad de Objetos"
            "memory_needs" -> "Necesidades de Tarea"
            "memory_recent" -> "Memoria Reciente"
            "memory_pairs" -> "Parejas de Memoria"
            
            // Lenguaje
            "language_word_image" -> "Vocabulario: Imagen"
            "language_denomination", "language_naming_objects" -> "Denominación de Objetos"
            "language_semantic_category" -> "Categorías Semánticas"
            "language_start_letter" -> "Fluidez: Primera Letra"
            "language_start_syllable" -> "Fluidez: Primera Sílaba"
            "language_end_letter" -> "Fluidez: Letra Final"
            "language_end_syllable" -> "Fluidez: Sílaba Final"
            "language_complex_cluster" -> "Letras Trabadas"
            "language_semantic_completion" -> "Completar Categoría"
            "language_semantic_naming" -> "Nombrar Familia"
            
            // Funciones Ejecutivas / Cálculo
            "calculation_simple" -> "Cálculo Mental"
            "executive_color_shape_sequence" -> "Secuencias Lógicas"
            "executive_planning_steps" -> "Planificación de Pasos"
            "executive_shopping_list" -> "Lista de la Compra"
            "executive_money_calculation" -> "Lógica Monetaria"
            "executive_time_logic" -> "Lógica Temporal"
            "executive_logical_reasoning" -> "Razonamiento Lógico"
            "executive_analogies" -> "Analogías Conceptuales"
            "executive_abstractions" -> "Abstracción Símbolo-Número"
            "executive_intrusos" -> "Tachar el Intruso"
            "executive_math_advanced" -> "Cálculo Avanzado"
            
            // Percepción
            "perception_color_identification" -> "Identificación de Colores"
            "perception_size_ordering" -> "Orden de Tamaños"
            "perception_lateral_dominance" -> "Derecha e Izquierda"
            "perception_mirror" -> "Imágenes en Espejo"
            "perception_body_parts" -> "Esquema Corporal"
            
            // Lectoescritura
            "literacy_tracing", "literacy_tracing_basic" -> "Trazos y Caligrafía"
            "literacy_complete_letters" -> "Completar Palabras"
            "literacy_copy_words" -> "Copia de Palabras"
            "literacy_form_shapes" -> "Dibujar Formas"
            
            else -> cleanType.replace("_", " ").replaceFirstChar { it.uppercase() }
        }
    }
}
