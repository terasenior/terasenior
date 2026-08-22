package com.terapia.terasenior.treatment.repository

/**
 * Catálogo de 100+ preguntas de memoria para la v1.3.43.
 * Categorizado por nivel de dificultad GDS.
 */
object MemoryCatalog {
    data class MemoryQuestion(
        val type: String,
        val text: String,
        val options: List<String>,
        val correctAnswer: String,
        val difficulty: Int // GDS 1-5
    )

    fun getQuestion(type: String): MemoryQuestion {
        return when (type) {
            // --- GDS 3 (Nivel Inicial/Leve) ---
            "memory_cultural_inventor" -> MemoryQuestion(type, "¿Quién inventó la bombilla?", listOf("Thomas Edison", "Isaac Newton", "Albert Einstein", "Nikola Tesla"), "Thomas Edison", 3)
            "memory_cultural_discovery" -> MemoryQuestion(type, "¿En qué año se descubrió América?", listOf("1492", "1500", "1485", "1510"), "1492", 3)
            "memory_cultural_moon" -> MemoryQuestion(type, "¿Quién fue el primer hombre en pisar la Luna?", listOf("Neil Armstrong", "Buzz Aldrin", "Yuri Gagarin", "John Glenn"), "Neil Armstrong", 3)
            "memory_cultural_painting" -> MemoryQuestion(type, "¿Quién pintó 'Las Meninas'?", listOf("Velázquez", "Goya", "Picasso", "Dalí"), "Velázquez", 3)
            "memory_cultural_writer" -> MemoryQuestion(type, "¿Quién escribió 'El Quijote'?", listOf("Miguel de Cervantes", "Lope de Vega", "Quevedo", "Góngora"), "Miguel de Cervantes", 3)
            "memory_recent_today_weather" -> MemoryQuestion(type, "¿Qué tiempo ha hecho hoy al levantarse?", listOf("Soleado", "Lluvioso", "Nublado", "Nevando"), "Respuesta libre", 3)
            "memory_recent_last_meal" -> MemoryQuestion(type, "¿Qué cenó usted anoche?", listOf("Carne", "Pescado", "Verdura", "Sopa"), "Respuesta libre", 3)
            "memory_prospective_meds" -> MemoryQuestion(type, "¿A qué hora le toca su próxima medicación?", listOf("Mañana", "Tarde", "Noche", "No tomo"), "Respuesta libre", 3)
            "memory_working_numbers_rev" -> MemoryQuestion(type, "Diga al revés: 5 - 8 - 2", listOf("2 - 8 - 5", "5 - 8 - 2", "8 - 5 - 2", "2 - 5 - 8"), "2 - 8 - 5", 3)
            "memory_semantic_capital_italy" -> MemoryQuestion(type, "¿Cuál es la capital de Italia?", listOf("Roma", "Milán", "Venecia", "Nápoles"), "Roma", 3)
            "memory_semantic_capital_germany" -> MemoryQuestion(type, "¿Cuál es la capital de Alemania?", listOf("Berlín", "Múnich", "Hamburgo", "Frankfurt"), "Berlín", 3)
            "memory_semantic_capital_portugal" -> MemoryQuestion(type, "¿Cuál es la capital de Portugal?", listOf("Lisboa", "Oporto", "Coímbra", "Faro"), "Lisboa", 3)
            "memory_semantic_ocean_depth" -> MemoryQuestion(type, "¿Qué animal es el más grande del océano?", listOf("Ballena azul", "Tiburón blanco", "Delfín", "Orca"), "Ballena azul", 3)
            "memory_semantic_planet_red" -> MemoryQuestion(type, "¿Qué planeta es conocido como el planeta rojo?", listOf("Marte", "Júpiter", "Saturno", "Venus"), "Marte", 3)
            "memory_semantic_island_canary" -> MemoryQuestion(type, "¿Cuál de estas es una isla de Canarias?", listOf("Tenerife", "Mallorca", "Ibiza", "Menorca"), "Tenerife", 3)
            "memory_semantic_island_balearic" -> MemoryQuestion(type, "¿Cuál de estas es una isla de Baleares?", listOf("Mallorca", "Lanzarote", "Gran Canaria", "La Palma"), "Mallorca", 3)
            "memory_semantic_river_egypt" -> MemoryQuestion(type, "¿Qué río atraviesa Egipto?", listOf("Nilo", "Amazonas", "Ganges", "Misisipi"), "Nilo", 3)
            "memory_semantic_mountain_everest" -> MemoryQuestion(type, "¿Cómo se llama la montaña más alta del mundo?", listOf("Everest", "K2", "Teide", "Mont Blanc"), "Everest", 3)
            "memory_semantic_continent_kangaroo" -> MemoryQuestion(type, "¿En qué continente viven los canguros?", listOf("Oceanía", "África", "Asia", "América"), "Oceanía", 3)
            "memory_semantic_continent_pyramids" -> MemoryQuestion(type, "¿En qué continente están las pirámides de Giza?", listOf("África", "Asia", "Europa", "América"), "África", 3)

            // --- GDS 4 (Nivel Intermedio) ---
            "memory_utility_hammer" -> MemoryQuestion(type, "¿Para qué sirve un martillo?", listOf("Clavar clavos", "Cortar madera", "Pintar", "Atornillar"), "Clavar clavos", 4)
            "memory_utility_scissors" -> MemoryQuestion(type, "¿Para qué sirven las tijeras?", listOf("Cortar papel o tela", "Pegar", "Escribir", "Coser"), "Cortar papel o tela", 4)
            "memory_utility_broom" -> MemoryQuestion(type, "¿Para qué sirve la escoba?", listOf("Barrer el suelo", "Fregar los platos", "Cocinar", "Planchar"), "Barrer el suelo", 4)
            "memory_utility_keys" -> MemoryQuestion(type, "¿Para qué sirven las llaves?", listOf("Abrir cerraduras", "Comer", "Peinarse", "Limpiar"), "Abrir cerraduras", 4)
            "memory_utility_glasses" -> MemoryQuestion(type, "¿Para qué sirven las gafas?", listOf("Ver mejor", "Oír mejor", "Hablar", "Oler"), "Ver mejor", 4)
            "memory_needs_coffee" -> MemoryQuestion(type, "¿Qué necesitas para hacer un café?", listOf("Café y agua", "Harina y sal", "Aceite y vinagre", "Leche y azúcar"), "Café y agua", 4)
            "memory_needs_letter" -> MemoryQuestion(type, "¿Qué necesitas para escribir una carta?", listOf("Papel y bolígrafo", "Sartén y cuchara", "Martillo y clavos", "Peine y espejo"), "Papel y bolígrafo", 4)
            "memory_needs_wash_hair" -> MemoryQuestion(type, "¿Qué necesitas para lavarte el pelo?", listOf("Agua y champú", "Jabón de manos", "Pasta de dientes", "Crema"), "Agua y champú", 4)
            "memory_needs_teeth" -> MemoryQuestion(type, "¿Qué necesitas para cepillarte los dientes?", listOf("Cepillo y pasta", "Peine y gel", "Esponja", "Hilo"), "Cepillo y pasta", 4)
            "memory_needs_rain" -> MemoryQuestion(type, "¿Qué necesitas si está lloviendo?", listOf("Un paraguas", "Gafas de sol", "Un abanico", "Bañador"), "Un paraguas", 4)
            "memory_semantic_fruit_yellow" -> MemoryQuestion(type, "¿Cuál de estas frutas es amarilla?", listOf("Plátano", "Fresa", "Uva", "Cereza"), "Plátano", 4)
            "memory_semantic_fruit_red" -> MemoryQuestion(type, "¿Cuál de estas frutas es roja?", listOf("Cereza", "Pera", "Limón", "Piña"), "Cereza", 4)
            "memory_semantic_animal_bark" -> MemoryQuestion(type, "¿Qué animal ladra?", listOf("Perro", "Gato", "Vaca", "Oveja"), "Perro", 4)
            "memory_semantic_animal_meow" -> MemoryQuestion(type, "¿Qué animal maúlla?", listOf("Gato", "Perro", "Caballo", "Cerdo"), "Gato", 4)
            "memory_semantic_animal_moo" -> MemoryQuestion(type, "¿Qué animal muge?", listOf("Vaca", "Oveja", "Cabra", "Gallo"), "Vaca", 4)
            "memory_semantic_color_grass" -> MemoryQuestion(type, "¿De qué color es la hierba?", listOf("Verde", "Azul", "Rojo", "Amarillo"), "Verde", 4)
            "memory_semantic_color_sky" -> MemoryQuestion(type, "¿De qué color es el cielo despejado?", listOf("Azul", "Verde", "Gris", "Blanco"), "Azul", 4)
            "memory_semantic_color_coal" -> MemoryQuestion(type, "¿De qué color es el carbón?", listOf("Negro", "Blanco", "Rojo", "Marrón"), "Negro", 4)
            "memory_semantic_color_milk" -> MemoryQuestion(type, "¿De qué color es la leche?", listOf("Blanca", "Amarilla", "Azul", "Rosa"), "Blanca", 4)
            "memory_semantic_color_blood" -> MemoryQuestion(type, "¿De qué color es la sangre?", listOf("Roja", "Verde", "Blanca", "Negra"), "Roja", 4)
            "memory_semantic_season_cold" -> MemoryQuestion(type, "¿En qué estación del año hace más frío?", listOf("Invierno", "Verano", "Primavera", "Otoño"), "Invierno", 4)
            "memory_semantic_season_hot" -> MemoryQuestion(type, "¿En qué estación del año hace más calor?", listOf("Verano", "Invierno", "Otoño", "Primavera"), "Verano", 4)
            "memory_semantic_day_first" -> MemoryQuestion(type, "¿Cuál es el primer día de la semana?", listOf("Lunes", "Sábado", "Domingo", "Viernes"), "Lunes", 4)
            "memory_semantic_day_last" -> MemoryQuestion(type, "¿Cuál es el último día de la semana?", listOf("Domingo", "Sábado", "Lunes", "Viernes"), "Domingo", 4)
            "memory_semantic_month_first" -> MemoryQuestion(type, "¿Cuál es el primer mes del año?", listOf("Enero", "Diciembre", "Marzo", "Julio"), "Enero", 4)
            "orientation_calc_months_count" -> MemoryQuestion(type, "¿Cuántos meses tiene un año?", listOf("12", "10", "24", "7"), "12", 4)
            "orientation_calc_week_days_count" -> MemoryQuestion(type, "¿Cuántos días tiene una semana?", listOf("7", "5", "12", "30"), "7", 4)
            "memory_semantic_clothing_feet" -> MemoryQuestion(type, "¿Qué prenda se pone en los pies?", listOf("Zapatos", "Sombrero", "Guantes", "Bufanda"), "Zapatos", 4)
            "memory_semantic_clothing_hands" -> MemoryQuestion(type, "¿Qué prenda se pone en las manos?", listOf("Guantes", "Calcetines", "Pantalones", "Gorra"), "Guantes", 4)
            "memory_semantic_clothing_head" -> MemoryQuestion(type, "¿Qué prenda se pone en la cabeza?", listOf("Sombrero", "Camiseta", "Falda", "Cinturón"), "Sombrero", 4)
            "memory_semantic_tool_nails" -> MemoryQuestion(type, "¿Qué herramienta sirve para poner un clavo?", listOf("Martillo", "Destornillador", "Alicates", "Sierra"), "Martillo", 4)
            "memory_semantic_tool_screws" -> MemoryQuestion(type, "¿Qué herramienta sirve para un tornillo?", listOf("Destornillador", "Martillo", "Hacha", "Pala"), "Destornillador", 4)
            "memory_semantic_kitchen_fry" -> MemoryQuestion(type, "¿Qué objeto usamos para freír un huevo?", listOf("Sartén", "Cazuela", "Colador", "Molinillo"), "Sartén", 4)
            "memory_semantic_kitchen_soup" -> MemoryQuestion(type, "¿Qué objeto usamos para servir la sopa?", listOf("Cucharón", "Tenedor", "Cuchillo", "Pinzas"), "Cucharón", 4)
            "memory_semantic_kitchen_bake" -> MemoryQuestion(type, "¿Dónde metemos el pollo para asarlo?", listOf("Horno", "Nevera", "Lavavajillas", "Microondas"), "Horno", 4)
            "memory_semantic_home_sleep" -> MemoryQuestion(type, "¿En qué mueble dormimos?", listOf("Cama", "Mesa", "Silla", "Armario"), "Cama", 4)
            "memory_semantic_home_sit" -> MemoryQuestion(type, "¿En qué mueble nos sentamos para comer?", listOf("Silla", "Cama", "Estantería", "Espejo"), "Silla", 4)
            "memory_semantic_home_clothes" -> MemoryQuestion(type, "¿Dónde guardamos la ropa colgada?", listOf("Armario", "Cajonera", "Nevera", "Baúl"), "Armario", 4)
            "memory_semantic_home_food" -> MemoryQuestion(type, "¿Dónde guardamos la comida fresca?", listOf("Nevera", "Horno", "Lavadora", "Fregadero"), "Nevera", 4)
            "memory_semantic_body_see" -> MemoryQuestion(type, "¿Con qué parte del cuerpo vemos?", listOf("Ojos", "Oídos", "Nariz", "Boca"), "Ojos", 4)

            // --- GDS 5 (Nivel Avanzado/Deterioro Moderado-Grave) ---
            "memory_semantic_body_hear" -> MemoryQuestion(type, "¿Con qué parte del cuerpo oímos?", listOf("Oídos", "Ojos", "Manos", "Pies"), "Oídos", 5)
            "memory_semantic_body_smell" -> MemoryQuestion(type, "¿Con qué parte del cuerpo olemos?", listOf("Nariz", "Boca", "Ojos", "Orejas"), "Nariz", 5)
            "memory_semantic_body_eat" -> MemoryQuestion(type, "¿Con qué parte del cuerpo comemos?", listOf("Boca", "Nariz", "Ojos", "Manos"), "Boca", 5)
            "memory_semantic_body_walk" -> MemoryQuestion(type, "¿Con qué parte del cuerpo caminamos?", listOf("Pies", "Manos", "Cabeza", "Brazos"), "Pies", 5)
            "memory_semantic_body_write" -> MemoryQuestion(type, "¿Con qué parte del cuerpo escribimos?", listOf("Manos", "Pies", "Rodillas", "Codos"), "Manos", 5)
            "memory_semantic_family_son" -> MemoryQuestion(type, "¿Cómo se llama el hijo de su padre?", listOf("Hermano", "Sobrino", "Tío", "Abuelo"), "Hermano", 5)
            "memory_semantic_family_father_father" -> MemoryQuestion(type, "¿Quién es el padre de su padre?", listOf("Abuelo", "Hijo", "Tío", "Primo"), "Abuelo", 5)
            "memory_semantic_family_sister" -> MemoryQuestion(type, "¿Quién es la hija de su madre?", listOf("Hermana", "Tía", "Sobrina", "Nieta"), "Hermana", 5)
            "memory_semantic_family_uncle" -> MemoryQuestion(type, "¿Quién es el hermano de su madre?", listOf("Tío", "Primo", "Abuelo", "Padre"), "Tío", 5)
            "memory_semantic_family_nephew" -> MemoryQuestion(type, "¿Quién es el hijo de su hermano?", listOf("Sobrino", "Hijo", "Primo", "Tío"), "Sobrino", 5)
            "memory_semantic_object_umbrella" -> MemoryQuestion(type, "¿Qué usamos cuando llueve?", listOf("Paraguas", "Sombrero", "Gafas", "Reloj"), "Paraguas", 5)
            "memory_semantic_object_soap" -> MemoryQuestion(type, "¿Qué usamos para lavarnos las manos?", listOf("Jabón", "Champú", "Pasta de dientes", "Colonia"), "Jabón", 5)
            "memory_semantic_object_towel" -> MemoryQuestion(type, "¿Qué usamos para secarnos?", listOf("Toalla", "Sábana", "Manta", "Servilleta"), "Toalla", 5)
            "memory_semantic_object_keys" -> MemoryQuestion(type, "¿Qué usamos para abrir la puerta de casa?", listOf("Llaves", "Mando", "Tarjeta", "Martillo"), "Llaves", 5)
            "memory_semantic_object_phone" -> MemoryQuestion(type, "¿Qué usamos para llamar a alguien?", listOf("Teléfono", "Televisor", "Radio", "Libro"), "Teléfono", 5)
            "memory_semantic_object_tv" -> MemoryQuestion(type, "¿Qué usamos para ver las noticias?", listOf("Televisor", "Nevera", "Lavadora", "Cama"), "Televisor", 5)
            "memory_semantic_object_radio" -> MemoryQuestion(type, "¿Qué usamos para escuchar música?", listOf("Radio", "Reloj", "Espejo", "Lámpara"), "Radio", 5)
            "memory_semantic_object_clock" -> MemoryQuestion(type, "¿Qué usamos para saber la hora?", listOf("Reloj", "Calendario", "Termómetro", "Metro"), "Reloj", 5)
            "memory_semantic_object_money" -> MemoryQuestion(type, "¿Qué usamos para pagar en la tienda?", listOf("Dinero", "Papel", "Fotos", "Llaves"), "Dinero", 5)
            "memory_semantic_object_wallet" -> MemoryQuestion(type, "¿Dónde guardamos el dinero?", listOf("Cartera", "Caja", "Bolsillo", "Bote"), "Cartera", 5)
            "memory_semantic_animal_hen" -> MemoryQuestion(type, "¿Qué animal pone huevos?", listOf("Gallina", "Perro", "Gato", "Vaca"), "Gallina", 5)
            "memory_semantic_animal_cow" -> MemoryQuestion(type, "¿Qué animal da leche?", listOf("Vaca", "Cerdo", "Oveja", "Pájaro"), "Vaca", 5)
            "memory_semantic_animal_sheep" -> MemoryQuestion(type, "¿De qué animal sacamos la lana?", listOf("Oveja", "Cabra", "Vaca", "Caballo"), "Oveja", 5)
            "memory_semantic_animal_bee" -> MemoryQuestion(type, "¿Qué insecto hace la miel?", listOf("Abeja", "Hormiga", "Mosca", "Mosquito"), "Abeja", 5)
            "memory_semantic_animal_spider" -> MemoryQuestion(type, "¿Qué insecto teje telarañas?", listOf("Araña", "Abeja", "Escarabajo", "Grillo"), "Araña", 5)
            "memory_semantic_food_bread" -> MemoryQuestion(type, "¿Qué compramos en la panadería?", listOf("Pan", "Carne", "Pescado", "Fruta"), "Pan", 5)
            "memory_semantic_food_meat" -> MemoryQuestion(type, "¿Qué compramos en la carnicería?", listOf("Carne", "Pan", "Zapatos", "Libros"), "Carne", 5)
            "memory_semantic_food_fish" -> MemoryQuestion(type, "¿Qué compramos en la pescadería?", listOf("Pescado", "Carne", "Fruta", "Verdura"), "Pescado", 5)
            "memory_semantic_food_fruit" -> MemoryQuestion(type, "¿Qué compramos en la frutería?", listOf("Fruta", "Pan", "Pescado", "Ropa"), "Fruta", 5)
            "memory_semantic_food_medicine" -> MemoryQuestion(type, "¿Qué compramos en la farmacia?", listOf("Medicinas", "Comida", "Zapatos", "Juguetes"), "Medicinas", 5)
            "memory_semantic_place_church" -> MemoryQuestion(type, "¿A dónde vamos a misa?", listOf("Iglesia", "Hospital", "Cine", "Parque"), "Iglesia", 5)
            "memory_semantic_place_hospital" -> MemoryQuestion(type, "¿A dónde vamos cuando estamos enfermos?", listOf("Hospital", "Colegio", "Mercado", "Teatro"), "Hospital", 5)
            "memory_semantic_place_school" -> MemoryQuestion(type, "¿A dónde van los niños a aprender?", listOf("Colegio", "Cuartel", "Fábrica", "Oficina"), "Colegio", 5)
            "memory_semantic_place_park" -> MemoryQuestion(type, "¿A dónde vamos a pasear entre árboles?", listOf("Parque", "Garaje", "Sótano", "Cocina"), "Parque", 5)
            "memory_semantic_place_kitchen" -> MemoryQuestion(type, "¿En qué parte de la casa cocinamos?", listOf("Cocina", "Baño", "Dormitorio", "Salón"), "Cocina", 5)
            "memory_semantic_place_bathroom" -> MemoryQuestion(type, "¿En qué parte de la casa nos duchamos?", listOf("Baño", "Cocina", "Pasillo", "Terraza"), "Baño", 5)
            "memory_semantic_color_sun" -> MemoryQuestion(type, "¿De qué color es el sol?", listOf("Amarillo", "Azul", "Verde", "Negro"), "Amarillo", 5)
            "memory_semantic_color_tomato" -> MemoryQuestion(type, "¿De qué color es un tomate maduro?", listOf("Rojo", "Azul", "Amarillo", "Verde"), "Rojo", 5)
            "memory_semantic_color_lemon" -> MemoryQuestion(type, "¿De qué color es un limón?", listOf("Amarillo", "Rosa", "Marrón", "Blanco"), "Amarillo", 5)
            "memory_semantic_color_orange" -> MemoryQuestion(type, "¿De qué color es una naranja?", listOf("Naranja", "Lila", "Gris", "Turquesa"), "Naranja", 5)

            // --- EXTRAS PARA COMPLETAR 100 ---
            "memory_cultural_first_king" -> MemoryQuestion(type, "¿Quién fue el primer Rey de la democracia en España?", listOf("Juan Carlos I", "Felipe VI", "Alfonso XIII", "Amadeo de Saboya"), "Juan Carlos I", 3)
            "memory_cultural_civil_war" -> MemoryQuestion(type, "¿En qué siglo ocurrió la Guerra Civil Española?", listOf("Siglo XX", "Siglo XIX", "Siglo XVIII", "Siglo XXI"), "Siglo XX", 3)
            "memory_cultural_discovery_navigator" -> MemoryQuestion(type, "¿Cómo se llamaba el navegante que descubrió América?", listOf("Cristóbal Colón", "Magallanes", "Elcano", "Vasco de Gama"), "Cristóbal Colón", 3)
            "memory_cultural_capital_france" -> MemoryQuestion(type, "¿Cuál es la capital de Francia?", listOf("París", "Londres", "Berlín", "Roma"), "París", 3)
            "memory_cultural_capital_uk" -> MemoryQuestion(type, "¿Cuál es la capital del Reino Unido?", listOf("Londres", "Dublín", "Edimburgo", "Cardiff"), "Londres", 3)
            "memory_cultural_euro_intro" -> MemoryQuestion(type, "¿En qué año se introdujo el Euro en España?", listOf("2002", "1999", "2005", "2000"), "2002", 3)
            "memory_cultural_olympics_barcelona" -> MemoryQuestion(type, "¿En qué año fueron las Olimpiadas de Barcelona?", listOf("1992", "1988", "1996", "2000"), "1992", 3)
            "memory_cultural_monalisa" -> MemoryQuestion(type, "¿Quién pintó 'La Gioconda'?", listOf("Leonardo da Vinci", "Miguel Ángel", "Rafael", "Donatello"), "Leonardo da Vinci", 3)
            "memory_cultural_beethoven" -> MemoryQuestion(type, "¿Qué músico compuso la 'Novena Sinfonía'?", listOf("Beethoven", "Mozart", "Bach", "Chopin"), "Beethoven", 3)
            "memory_cultural_cervantes_birth" -> MemoryQuestion(type, "¿En qué ciudad nació Miguel de Cervantes?", listOf("Alcalá de Henares", "Madrid", "Toledo", "Sevilla"), "Alcalá de Henares", 3)

            else -> MemoryQuestion(type, "Pregunta de memoria: $type", listOf("A", "B", "C", "D"), "A", 3)
        }
    }

    private fun getMonthName(m: Int): String = listOf("", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")[m.coerceIn(0, 12)]
    private fun getDayName(d: Int): String = listOf("Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")[d.coerceIn(0, 7)]
    private fun getSeason(m: Int): String = when(m) { in 3..5 -> "Primavera"; in 6..8 -> "Verano"; in 9..11 -> "Otoño"; else -> "Invierno" }
    private fun getPartDay(h: Int): String = when(h) { in 6..12 -> "Mañana"; in 13..20 -> "Tarde"; in 21..23 -> "Noche"; else -> "Madrugada" }
}
