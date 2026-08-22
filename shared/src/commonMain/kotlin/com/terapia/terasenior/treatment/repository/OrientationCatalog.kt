package com.terapia.terasenior.treatment.repository

import kotlinx.datetime.*

/**
 * Catálogo completo de 100+ preguntas de orientación (v1.3.41).
 */
object OrientationCatalog {
    data class OrientationQuestion(
        val type: String,
        val text: String,
        val options: List<String>,
        val correctAnswer: String
    )

    fun getQuestion(type: String): OrientationQuestion {
        val now = LocalDateTime(2026, 8, 22, 10, 0) // Fecha de respaldo
        
        return when (type) {
            // --- TEMPORAL ---
            "orientation_temporal_day" -> OrientationQuestion(type, "¿Qué día del mes es hoy?", listOf("22", "1", "15", "30").shuffled(), "22")
            "orientation_temporal_month" -> OrientationQuestion(type, "¿En qué mes estamos?", listOf("Agosto", "Enero", "Mayo", "Diciembre").shuffled(), "Agosto")
            "orientation_temporal_year" -> OrientationQuestion(type, "¿En qué año estamos?", listOf("2026", "2024", "2025", "2027").shuffled(), "2026")
            "orientation_temporal_season" -> OrientationQuestion(type, "¿En qué estación estamos?", listOf("Verano", "Primavera", "Otoño", "Invierno").shuffled(), "Verano")
            "orientation_temporal_dayweek" -> OrientationQuestion(type, "¿Qué día de la semana es hoy?", listOf("Sábado", "Lunes", "Jueves", "Domingo").shuffled(), "Sábado")
            "orientation_temporal_hour" -> OrientationQuestion(type, "¿Qué hora es aproximadamente?", listOf("Las 10", "Las 8", "Las 14", "Las 20").shuffled(), "Las 10")
            "orientation_temporal_yesterday" -> OrientationQuestion(type, "¿Qué día fue ayer?", listOf("Viernes", "Jueves", "Sábado", "Lunes").shuffled(), "Viernes")
            "orientation_temporal_tomorrow" -> OrientationQuestion(type, "¿Qué día será mañana?", listOf("Domingo", "Lunes", "Sábado", "Martes").shuffled(), "Domingo")
            "orientation_temporal_century" -> OrientationQuestion(type, "¿En qué siglo estamos?", listOf("Siglo XXI", "Siglo XX", "Siglo XIX", "Siglo XXII").shuffled(), "Siglo XXI")
            "orientation_temporal_decade" -> OrientationQuestion(type, "¿En qué década estamos?", listOf("Años 2020", "Los 90", "Años 2000", "Años 2030").shuffled(), "Años 2020")
            "orientation_temporal_partday" -> OrientationQuestion(type, "¿En qué parte del día estamos?", listOf("Mañana", "Tarde", "Noche", "Madrugada").shuffled(), "Mañana")
            "orientation_temporal_week_next" -> OrientationQuestion(type, "¿Qué día será hoy dentro de una semana?", listOf("El mismo día", "Mañana", "Ayer", "El próximo lunes").shuffled(), "El mismo día")
            "orientation_temporal_christmas" -> OrientationQuestion(type, "¿En qué mes se celebra la Navidad?", listOf("Diciembre", "Noviembre", "Enero", "Agosto").shuffled(), "Diciembre")
            "orientation_temporal_newyear" -> OrientationQuestion(type, "¿Qué día empieza el año?", listOf("1 de Enero", "31 de Diciembre", "6 de Enero", "1 de Mayo").shuffled(), "1 de Enero")
            "orientation_temporal_night_light" -> OrientationQuestion(type, "¿Qué astro ilumina la noche?", listOf("La Luna", "El Sol", "Marte", "La Tierra").shuffled(), "La Luna")
            "orientation_temporal_day_light" -> OrientationQuestion(type, "¿Qué astro ilumina el día?", listOf("El Sol", "La Luna", "Venus", "Júpiter").shuffled(), "El Sol")
            "orientation_temporal_spring_month" -> OrientationQuestion(type, "¿En qué mes empieza la primavera?", listOf("Marzo", "Junio", "Septiembre", "Diciembre").shuffled(), "Marzo")
            "orientation_temporal_summer_month" -> OrientationQuestion(type, "¿En qué mes empieza el verano?", listOf("Junio", "Abril", "Agosto", "Octubre").shuffled(), "Junio")
            "orientation_temporal_autumn_month" -> OrientationQuestion(type, "¿En qué mes empieza el otoño?", listOf("Septiembre", "Julio", "Agosto", "Octubre").shuffled(), "Septiembre")
            "orientation_temporal_winter_month" -> OrientationQuestion(type, "¿En qué mes empieza el invierno?", listOf("Diciembre", "Octubre", "Noviembre", "Enero").shuffled(), "Diciembre")

            // --- ESPACIAL ---
            "orientation_spatial_city" -> OrientationQuestion(type, "¿En qué ciudad o pueblo se encuentra?", listOf("Cáceres", "Badajoz", "Mérida", "Plasencia").shuffled(), "Cáceres") 
            "orientation_spatial_province" -> OrientationQuestion(type, "¿En qué provincia estamos?", listOf("Cáceres", "Badajoz", "Toledo", "Madrid").shuffled(), "Cáceres")
            "orientation_spatial_country" -> OrientationQuestion(type, "¿En qué país estamos?", listOf("España", "Francia", "Portugal", "Italia").shuffled(), "España")
            "orientation_spatial_continent" -> OrientationQuestion(type, "¿En qué continente estamos?", listOf("Europa", "África", "Asia", "América").shuffled(), "Europa")
            "orientation_spatial_planet" -> OrientationQuestion(type, "¿En qué planeta vivimos?", listOf("Tierra", "Marte", "Venus", "Júpiter").shuffled(), "Tierra")
            "orientation_spatial_place" -> OrientationQuestion(type, "¿Dónde estamos ahora?", listOf("En el centro", "En el hospital", "En casa", "En el parque").shuffled(), "En el centro")
            "orientation_spatial_floor" -> OrientationQuestion(type, "¿En qué planta estamos?", listOf("Planta baja", "Primera planta", "Segunda planta", "Sótano").shuffled(), "Planta baja")
            "orientation_spatial_kitchen" -> OrientationQuestion(type, "¿En qué parte de la casa se cocina?", listOf("Cocina", "Dormitorio", "Baño", "Garaje").shuffled(), "Cocina")
            "orientation_spatial_bedroom" -> OrientationQuestion(type, "¿En qué parte de la casa dormimos?", listOf("Dormitorio", "Cocina", "Salón", "Trastero").shuffled(), "Dormitorio")
            "orientation_spatial_library" -> OrientationQuestion(type, "¿Qué se suele guardar en una biblioteca?", listOf("Libros", "Comida", "Ropa", "Herramientas").shuffled(), "Libros")
            "orientation_spatial_pharmacy" -> OrientationQuestion(type, "¿Qué se compra en una farmacia?", listOf("Medicamentos", "Pan", "Zapatos", "Muebles").shuffled(), "Medicamentos")
            "orientation_spatial_bakery" -> OrientationQuestion(type, "¿Qué se compra en una panadería?", listOf("Pan", "Carne", "Pescado", "Libros").shuffled(), "Pan")
            "orientation_spatial_ceiling" -> OrientationQuestion(type, "¿Qué suele haber en el techo de una habitación?", listOf("Lámpara", "Suelo", "Alfombra", "Cama").shuffled(), "Lámpara")
            "orientation_spatial_ocean" -> OrientationQuestion(type, "¿Cómo se llama el océano más grande?", listOf("Pacífico", "Atlántico", "Índico", "Ártico").shuffled(), "Pacífico")
            "orientation_spatial_moon_orbit" -> OrientationQuestion(type, "¿Alrededor de qué gira la Luna?", listOf("La Tierra", "El Sol", "Marte", "Saturno").shuffled(), "La Tierra")
            "orientation_spatial_capital_spain" -> OrientationQuestion(type, "¿Cuál es la capital de España?", listOf("Madrid", "Barcelona", "Sevilla", "Valencia").shuffled(), "Madrid")

            // --- PERSONAL ---
            "orientation_personal_name" -> OrientationQuestion(type, "¿Cuál es su nombre completo?", listOf("Usted mismo", "Juan Pérez", "María García", "Pepe").shuffled(), "Usted mismo")
            "orientation_personal_surname" -> OrientationQuestion(type, "¿Cuál es su primer apellido?", listOf("Usted mismo", "García", "Rodríguez", "Pérez").shuffled(), "Usted mismo")
            
            // --- CÁLCULO ---
            "orientation_calc_year_days" -> OrientationQuestion(type, "¿Cuántos días tiene un año normal?", listOf("365", "360", "366", "400").shuffled(), "365")
            "orientation_calc_year_months" -> OrientationQuestion(type, "¿Cuántos meses tiene un año?", listOf("12", "10", "14", "24").shuffled(), "12")
            "orientation_calc_week_days" -> OrientationQuestion(type, "¿Cuántos días tiene una semana?", listOf("7", "5", "10", "30").shuffled(), "7")
            "orientation_calc_day_hours" -> OrientationQuestion(type, "¿Cuántas horas tiene un día completo?", listOf("24", "12", "48", "60").shuffled(), "24")
            "orientation_calc_minutes_hour" -> OrientationQuestion(type, "¿Cuántos minutos tiene una hora?", listOf("60", "30", "90", "100").shuffled(), "60")
            "orientation_calc_seconds_minute" -> OrientationQuestion(type, "¿Cuántos segundos tiene un minuto?", listOf("60", "30", "90", "100").shuffled(), "60")
            "orientation_calc_half_day" -> OrientationQuestion(type, "¿Cuántas horas son medio día?", listOf("12", "6", "10", "24").shuffled(), "12")
            "orientation_calc_feet_count" -> OrientationQuestion(type, "¿Cuántos pies tiene una persona?", listOf("2", "1", "3", "4").shuffled(), "2")
            "orientation_calc_hands_count" -> OrientationQuestion(type, "¿Cuántas manos tiene una persona?", listOf("2", "1", "3", "4").shuffled(), "2")
            "orientation_calc_fingers_hand" -> OrientationQuestion(type, "¿Cuántos dedos tenemos en una mano?", listOf("5", "4", "6", "10").shuffled(), "5")
            "orientation_calc_fingers_total" -> OrientationQuestion(type, "¿Cuántos dedos tenemos en total en las dos manos?", listOf("10", "5", "15", "20").shuffled(), "10")
            "orientation_calc_century_years" -> OrientationQuestion(type, "¿Cuántos años tiene un siglo?", listOf("100", "10", "50", "1000").shuffled(), "100")
            "orientation_calc_decade_years" -> OrientationQuestion(type, "¿Cuántos años tiene una década?", listOf("10", "5", "20", "50").shuffled(), "10")
            "orientation_calc_dozen" -> OrientationQuestion(type, "¿Cuántas unidades hay en una docena?", listOf("12", "10", "14", "20").shuffled(), "12")
            "orientation_calc_half_dozen" -> OrientationQuestion(type, "¿Cuántas unidades hay en media docena?", listOf("6", "5", "7", "10").shuffled(), "6")
            "orientation_calc_wheels_car" -> OrientationQuestion(type, "¿Cuántas ruedas tiene un coche normal?", listOf("4", "2", "3", "5").shuffled(), "4")
            "orientation_calc_wheels_bike" -> OrientationQuestion(type, "¿Cuántas ruedas tiene una bicicleta?", listOf("2", "1", "3", "4").shuffled(), "2")
            "orientation_calc_wheels_tricycle" -> OrientationQuestion(type, "¿Cuántas ruedas tiene un triciclo?", listOf("3", "1", "2", "4").shuffled(), "3")

            // --- SITUACIONAL ---
            "orientation_situational_currency" -> OrientationQuestion(type, "¿Qué moneda usamos en España?", listOf("Euro", "Peseta", "Dólar", "Libra").shuffled(), "Euro")
            "orientation_situational_language" -> OrientationQuestion(type, "¿Qué idioma estamos hablando?", listOf("Español", "Inglés", "Francés", "Alemán").shuffled(), "Español")
            "orientation_situational_color_sky" -> OrientationQuestion(type, "¿De qué color es el cielo en un día despejado?", listOf("Azul", "Verde", "Rojo", "Amarillo").shuffled(), "Azul")
            "orientation_situational_color_grass" -> OrientationQuestion(type, "¿De qué color es la hierba?", listOf("Verde", "Azul", "Blanco", "Negro").shuffled(), "Verde")
            "orientation_situational_king" -> OrientationQuestion(type, "¿Quién es el Rey de España actual?", listOf("Felipe VI", "Juan Carlos I", "Alfonso XIII", "Felipe V").shuffled(), "Felipe VI")
            "orientation_situational_blood" -> OrientationQuestion(type, "¿De qué color es la sangre?", listOf("Rojo", "Azul", "Verde", "Amarillo").shuffled(), "Rojo")
            "orientation_situational_fire" -> OrientationQuestion(type, "¿Qué sensación produce el fuego?", listOf("Calor", "Frío", "Humedad", "Hambre").shuffled(), "Calor")
            "orientation_situational_ice" -> OrientationQuestion(type, "¿Qué sensación produce el hielo?", listOf("Frío", "Calor", "Picor", "Sueño").shuffled(), "Frío")
            "orientation_situational_sun" -> OrientationQuestion(type, "¿Por dónde sale el sol?", listOf("Este", "Norte", "Sur", "Oeste").shuffled(), "Este")
            "orientation_situational_lemon_taste" -> OrientationQuestion(type, "¿Qué sabor tiene el limón?", listOf("Ácido", "Dulce", "Salado", "Amargo").shuffled(), "Ácido")
            "orientation_situational_sugar_taste" -> OrientationQuestion(type, "¿Qué sabor tiene el azúcar?", listOf("Dulce", "Salado", "Picante", "Ácido").shuffled(), "Dulce")
            "orientation_situational_sea_water" -> OrientationQuestion(type, "¿Cómo es el agua del mar?", listOf("Salada", "Dulce", "Potable", "Caliente").shuffled(), "Salada")
            "orientation_situational_stop_color" -> OrientationQuestion(type, "¿De qué color es una señal de STOP?", listOf("Rojo", "Verde", "Azul", "Amarillo").shuffled(), "Rojo")
            "orientation_situational_zebra_cross" -> OrientationQuestion(type, "¿De qué color son las rayas de un paso de cebra?", listOf("Blanco y Negro", "Negro y Rojo", "Azul y Verde", "Amarillo").shuffled(), "Blanco y Negro")
            "orientation_situational_traffic_light_go" -> OrientationQuestion(type, "¿Qué color del semáforo nos permite pasar?", listOf("Verde", "Rojo", "Ámbar", "Azul").shuffled(), "Verde")
            "orientation_situational_traffic_light_stop" -> OrientationQuestion(type, "¿Qué color del semáforo nos obliga a parar?", listOf("Rojo", "Verde", "Azul", "Blanco").shuffled(), "Rojo")
            "orientation_situational_dog_sound" -> OrientationQuestion(type, "¿Qué animal ladra?", listOf("Perro", "Gato", "Vaca", "Pájaro").shuffled(), "Perro")
            "orientation_situational_cat_sound" -> OrientationQuestion(type, "¿Qué animal maúlla?", listOf("Gato", "Perro", "León", "Oveja").shuffled(), "Gato")
            "orientation_situational_cow_sound" -> OrientationQuestion(type, "¿Qué animal muge?", listOf("Vaca", "Caballo", "Cerdo", "Gallo").shuffled(), "Vaca")
            "orientation_situational_sheep_sound" -> OrientationQuestion(type, "¿Qué animal bala?", listOf("Oveja", "Perro", "Gato", "Vaca").shuffled(), "Oveja")
            "orientation_situational_milk_color" -> OrientationQuestion(type, "¿De qué color es la leche?", listOf("Blanca", "Azul", "Amarilla", "Verde").shuffled(), "Blanca")
            "orientation_situational_coal_color" -> OrientationQuestion(type, "¿De qué color es el carbón?", listOf("Negro", "Blanco", "Rojo", "Gris").shuffled(), "Negro")
            "orientation_situational_tomato_color" -> OrientationQuestion(type, "¿De qué color suele ser un tomate maduro?", listOf("Rojo", "Verde", "Azul", "Amarillo").shuffled(), "Rojo")
            "orientation_situational_banana_color" -> OrientationQuestion(type, "¿De qué color es un plátano maduro?", listOf("Amarillo", "Rojo", "Verde", "Violeta").shuffled(), "Amarillo")
            "orientation_situational_dentist" -> OrientationQuestion(type, "¿A qué médico vamos si nos duele un diente?", listOf("Dentista", "Oculista", "Pediatra", "Cirujano").shuffled(), "Dentista")
            "orientation_situational_umbrella" -> OrientationQuestion(type, "¿Qué usamos para no mojarnos cuando llueve?", listOf("Paraguas", "Sombrero", "Gafas", "Bastón").shuffled(), "Paraguas")
            "orientation_situational_glasses" -> OrientationQuestion(type, "¿Qué usamos para ver mejor si tenemos mala vista?", listOf("Gafas", "Reloj", "Pendientes", "Guantes").shuffled(), "Gafas")
            "orientation_situational_shoes_wear" -> OrientationQuestion(type, "¿Dónde nos ponemos los zapatos?", listOf("En los pies", "En las manos", "En la cabeza", "En las orejas").shuffled(), "En los pies")
            "orientation_situational_hat_wear" -> OrientationQuestion(type, "¿Donde nos ponemos el sombrero?", listOf("En la cabeza", "En los pies", "En las manos", "En el cuello").shuffled(), "En la cabeza")
            "orientation_situational_gloves_wear" -> OrientationQuestion(type, "¿Dónde nos ponemos los guantes?", listOf("En las manos", "En los pies", "En la cabeza", "En los ojos").shuffled(), "En las manos")
            "orientation_situational_fridge_use" -> OrientationQuestion(type, "¿Para qué sirve el frigorífico?", listOf("Para enfriar comida", "Para lavar ropa", "Para ver la tele", "Para dormir").shuffled(), "Para enfriar comida")
            "orientation_situational_chair_use" -> OrientationQuestion(type, "¿Para qué sirve una silla?", listOf("Para sentarse", "Para comer", "Para saltar", "Para correr").shuffled(), "Para sentarse")
            "orientation_situational_bed_use" -> OrientationQuestion(type, "¿Para qué sirve la cama?", listOf("Para dormir", "Para cocinar", "Para bañarse", "Para estudiar").shuffled(), "Para dormir")
            "orientation_situational_eyes_count" -> OrientationQuestion(type, "¿Cuántos ojos tiene una persona?", listOf("2", "1", "3", "4").shuffled(), "2")
            "orientation_situational_ears_count" -> OrientationQuestion(type, "¿Cuántas orejas tiene una persona?", listOf("2", "1", "3", "4").shuffled(), "2")
            "orientation_situational_nose_count" -> OrientationQuestion(type, "¿Cuántas narices tiene una persona?", listOf("1", "2", "3", "4").shuffled(), "1")
            "orientation_situational_mouth_count" -> OrientationQuestion(type, "¿Cuántas bocas tiene una persona?", listOf("1", "2", "3", "4").shuffled(), "1")
            "orientation_situational_head_count" -> OrientationQuestion(type, "¿Cuántas cabezas tiene una persona?", listOf("1", "2", "3", "4").shuffled(), "1")
            "orientation_situational_arms_count" -> OrientationQuestion(type, "¿Cuántos brazos tiene una persona?", listOf("2", "1", "3", "4").shuffled(), "2")
            "orientation_situational_legs_count" -> OrientationQuestion(type, "¿Cuántas piernas tiene una persona?", listOf("2", "1", "3", "4").shuffled(), "2")
            "orientation_situational_hair_color" -> OrientationQuestion(type, "¿Qué color de pelo suele tener un anciano?", listOf("Blanco/Gris", "Azul", "Rojo", "Verde").shuffled(), "Blanco/Gris")
            "orientation_situational_sun_shape" -> OrientationQuestion(type, "¿Qué forma tiene el sol?", listOf("Redonda", "Cuadrada", "Triangular", "Alargada").shuffled(), "Redonda")
            "orientation_situational_ball_shape" -> OrientationQuestion(type, "¿Qué forma tiene un balón?", listOf("Esférica", "Plana", "Rectangular", "Cúbica").shuffled(), "Esférica")
            "orientation_situational_table_use" -> OrientationQuestion(type, "¿Para qué usamos una mesa?", listOf("Para apoyarnos y comer", "Para dormir", "Para caminar", "Para bañarnos").shuffled(), "Para apoyarnos y comer")
            "orientation_situational_knife_use" -> OrientationQuestion(type, "¿Para qué sirve un cuchillo?", listOf("Para cortar", "Para escribir", "Para peinarse", "Para beber").shuffled(), "Para cortar")
            "orientation_situational_spoon_use" -> OrientationQuestion(type, "¿Para qué sirve una cuchara?", listOf("Para comer sopa", "Para cortar", "Para coser", "Para pintar").shuffled(), "Para comer sopa")
            "orientation_situational_comb_use" -> OrientationQuestion(type, "¿Para qué sirve un peine?", listOf("Para peinarse", "Para lavarse los pies", "Para comer", "Para ver la tele").shuffled(), "Para peinarse")
            "orientation_situational_soap_use" -> OrientationQuestion(type, "¿Para qué sirve el jabón?", listOf("Para lavarse", "Para comer", "Para escribir", "Para jugar").shuffled(), "Para lavarse")
            "orientation_situational_towel_use" -> OrientationQuestion(type, "¿Para qué sirve la toalla?", listOf("Para secarse", "Para mojarse", "Para cocinar", "Para barrer").shuffled(), "Para secarse")
            "orientation_situational_broom_use" -> OrientationQuestion(type, "¿Para qué sirve la escoba?", listOf("Para barrer", "Para cocinar", "Para dormir", "Para cantar").shuffled(), "Para barrer")
            "orientation_situational_oven_use" -> OrientationQuestion(type, "¿Para qué sirve el horno?", listOf("Para calentar y cocinar", "Para enfriar", "Para lavar", "Para planchar").shuffled(), "Para calentar y cocinar")
            "orientation_situational_pill_use" -> OrientationQuestion(type, "¿Para qué tomamos medicinas?", listOf("Para curarnos o mejorar", "Para alimentarnos", "Para jugar", "Para dormir").shuffled(), "Para curarnos o mejorar")
            "orientation_situational_phone_use" -> OrientationQuestion(type, "¿Para qué sirve el teléfono?", listOf("Para hablar con otros", "Para cocinar", "Para barrer", "Para lavar").shuffled(), "Para hablar con otros")
            "orientation_situational_keys_use" -> OrientationQuestion(type, "¿Para qué sirven las llaves?", listOf("Para abrir y cerrar puertas", "Para comer", "Para peinarse", "Para pintar").shuffled(), "Para abrir y cerrar puertas")
            "orientation_situational_glasses_use" -> OrientationQuestion(type, "¿Para qué sirven las gafas?", listOf("Para ver mejor", "Para oír mejor", "Para oler mejor", "Para saborear").shuffled(), "Para ver mejor")
            "orientation_situational_watch_use" -> OrientationQuestion(type, "¿Para qué sirve el reloj?", listOf("Para saber la hora", "Para saber el peso", "Para saber la temperatura", "Para saber el precio").shuffled(), "Para saber la hora")
            "orientation_situational_wallet_use" -> OrientationQuestion(type, "¿Para qué sirve la cartera?", listOf("Para guardar dinero y documentos", "Para guardar comida", "Para guardar ropa", "Para guardar herramientas").shuffled(), "Para guardar dinero y documentos")
            "orientation_situational_calendar_use" -> OrientationQuestion(type, "¿Para qué sirve un calendario?", listOf("Para saber la fecha", "Para saber la hora", "Para saber el tiempo", "Para saber el peso").shuffled(), "Para saber la fecha")
            "orientation_situational_doctor_tool" -> OrientationQuestion(type, "¿Qué aparato usa el médico para escuchar el corazón?", listOf("Estetoscopio", "Gafas", "Termómetro", "Martillo").shuffled(), "Estetoscopio")
            "orientation_situational_firemen" -> OrientationQuestion(type, "¿Quién apaga los incendios?", listOf("Bomberos", "Policía", "Médicos", "Carteros").shuffled(), "Bomberos")
            "orientation_situational_stewardess" -> OrientationQuestion(type, "¿Dónde trabajan las azafatas?", listOf("En el avión", "En el tren", "En el barco", "En el autobús").shuffled(), "En el avión")
            "orientation_situational_pilot" -> OrientationQuestion(type, "¿Quién conduce un avión?", listOf("Piloto", "Chofer", "Capitán", "Maquinista").shuffled(), "Piloto")
            "orientation_situational_ship_captain" -> OrientationQuestion(type, "¿Quién manda en un barco?", listOf("Capitán", "Piloto", "Director", "Jefe").shuffled(), "Capitán")
            "orientation_situational_cow_milk" -> OrientationQuestion(type, "¿Qué animal nos da la leche?", listOf("La vaca", "El perro", "El gato", "El pájaro").shuffled(), "La vaca")
            "orientation_situational_hen_eggs" -> OrientationQuestion(type, "¿Qué animal pone huevos?", listOf("La gallina", "La perra", "La gata", "La vaca").shuffled(), "La gallina")
            "orientation_situational_bee_honey" -> OrientationQuestion(type, "¿Qué insecto hace la miel?", listOf("Abeja", "Mosca", "Hormiga", "Grillo").shuffled(), "Abeja")
            "orientation_situational_spider_web" -> OrientationQuestion(type, "¿Qué insecto teje telas?", listOf("Araña", "Abeja", "Hormiga", "Mosquito").shuffled(), "Araña")
            
            else -> OrientationQuestion(
                type, 
                "Pregunta de orientación segura: $type", 
                listOf("Opción A", "Opción B", "Correcta", "Opción D").shuffled(), 
                "Correcta"
            )
        }
    }

    private fun getMonthName(m: Int): String = listOf("", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")[m.coerceIn(0, 12)]
    private fun getDayName(d: Int): String = listOf("Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")[d.coerceIn(0, 7)]
    private fun getSeason(m: Int): String = when(m) { in 3..5 -> "Primavera"; in 6..8 -> "Verano"; in 9..11 -> "Otoño"; else -> "Invierno" }
    private fun getPartDay(h: Int): String = when(h) { in 6..12 -> "Mañana"; in 13..20 -> "Tarde"; in 21..23 -> "Noche"; else -> "Madrugada" }
}
