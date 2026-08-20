package com.terapia.terasenior.treatment.repository

import kotlinx.datetime.*

/**
 * Catálogo de 100 preguntas de orientación para la v1.3.31.
 */
object OrientationCatalog {
    data class OrientationQuestion(
        val type: String,
        val text: String,
        val options: List<String>,
        val correctAnswer: String
    )

    fun getQuestion(type: String): OrientationQuestion {
        // Garantizamos que 'now' siempre tenga un valor válido usando Throwable (v1.3.31)
        val now = try { 
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) 
        } catch(t: Throwable) { 
            LocalDateTime(2026, 8, 20, 12, 0) 
        }
        
        return when (type) {
            "orientation_temporal_day" -> {
                val correct = now.dayOfMonth.toString()
                OrientationQuestion(type, "¿Qué día del mes es hoy?", listOf(correct, "15", "1", "30").distinct(), correct)
            }
            "orientation_temporal_month" -> {
                val correct = getMonthName(now.monthNumber)
                OrientationQuestion(type, "¿En qué mes estamos?", listOf(correct, "Enero", "Mayo", "Agosto", "Diciembre").distinct(), correct)
            }
            "orientation_temporal_year" -> {
                val correct = now.year.toString()
                OrientationQuestion(type, "¿En qué año estamos?", listOf(correct, "2024", "2025", "2026", "2027").distinct(), correct)
            }
            "orientation_temporal_season" -> {
                val correct = getSeason(now.monthNumber)
                OrientationQuestion(type, "¿En qué estación estamos?", listOf(correct, "Primavera", "Verano", "Otoño", "Invierno").distinct(), correct)
            }
            "orientation_temporal_dayweek" -> {
                val correct = getDayName(now.dayOfWeek.ordinal + 1)
                val others = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo").filter { it != correct }
                val options = (others.shuffled().take(3) + correct).shuffled()
                OrientationQuestion(type, "¿Qué día de la semana es hoy?", options, correct)
            }
            "orientation_temporal_hour" -> {
                val correct = "Las ${now.hour}"
                OrientationQuestion(type, "¿Qué hora es aproximadamente?", listOf(correct, "Las 10", "Las 12", "Las 17", "Las 20").distinct(), correct)
            }
            "orientation_temporal_yesterday" -> {
                val correct = getDayName(now.dayOfWeek.ordinal)
                val others = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo").filter { it != correct }
                val options = (others.shuffled().take(3) + correct).shuffled()
                OrientationQuestion(type, "¿Qué día fue ayer?", options, correct)
            }
            "orientation_temporal_tomorrow" -> {
                val correct = getDayName((now.dayOfWeek.ordinal + 2) % 7)
                val others = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo").filter { it != correct }
                val options = (others.shuffled().take(3) + correct).shuffled()
                OrientationQuestion(type, "¿Qué día será mañana?", options, correct)
            }
            "orientation_temporal_century" -> OrientationQuestion(type, "¿En qué siglo estamos?", listOf("Siglo XIX", "Siglo XX", "Siglo XXI", "Siglo XXII"), "Siglo XXI")
            "orientation_temporal_decade" -> OrientationQuestion(type, "¿En qué década estamos?", listOf("Los 90", "Años 2000", "Años 2020", "Años 2030"), "Años 2020")
            "orientation_temporal_partday" -> {
                val correct = getPartDay(now.hour)
                OrientationQuestion(type, "¿En qué parte del día estamos?", listOf(correct, "Mañana", "Tarde", "Noche", "Madrugada").distinct(), correct)
            }
            "orientation_temporal_week_next" -> OrientationQuestion(type, "¿Qué día será hoy dentro de una semana?", listOf("El mismo día", "Mañana", "Ayer", "El próximo lunes"), "El mismo día")
            "orientation_temporal_christmas" -> OrientationQuestion(type, "¿En qué mes se celebra la Navidad?", listOf("Noviembre", "Diciembre", "Enero", "Agosto"), "Diciembre")
            "orientation_temporal_newyear" -> OrientationQuestion(type, "¿Qué día empieza el año?", listOf("1 de Enero", "31 de Diciembre", "6 de Enero", "1 de Mayo"), "1 de Enero")
            "orientation_temporal_night_light" -> OrientationQuestion(type, "¿Qué astro ilumina la noche?", listOf("El Sol", "La Luna", "Marte", "La Tierra"), "La Luna")
            "orientation_temporal_day_light" -> OrientationQuestion(type, "¿Qué astro ilumina el día?", listOf("La Luna", "El Sol", "Venus", "Júpiter"), "El Sol")
            "orientation_temporal_spring_month" -> OrientationQuestion(type, "¿En qué mes empieza la primavera?", listOf("Marzo", "Junio", "Septiembre", "Diciembre"), "Marzo")
            "orientation_temporal_summer_month" -> OrientationQuestion(type, "¿En qué mes empieza el verano?", listOf("Abril", "Junio", "Agosto", "Octubre"), "Junio")
            "orientation_temporal_autumn_month" -> OrientationQuestion(type, "¿En qué mes empieza el otoño?", listOf("Julio", "Agosto", "Septiembre", "Octubre"), "Septiembre")
            "orientation_temporal_winter_month" -> OrientationQuestion(type, "¿En qué mes empieza el invierno?", listOf("Octubre", "Noviembre", "Diciembre", "Enero"), "Diciembre")

            "orientation_spatial_city" -> OrientationQuestion(type, "¿En qué ciudad o pueblo se encuentra?", listOf("Cáceres", "Badajoz", "Mérida", "Plasencia"), "Cáceres") 
            "orientation_spatial_province" -> OrientationQuestion(type, "¿En qué provincia estamos?", listOf("Cáceres", "Badajoz", "Toledo", "Madrid"), "Cáceres")
            "orientation_spatial_country" -> OrientationQuestion(type, "¿En qué país estamos?", listOf("Francia", "Portugal", "España", "Italia"), "España")
            "orientation_spatial_continent" -> OrientationQuestion(type, "¿En qué continente estamos?", listOf("Europa", "África", "Asia", "América"), "Europa")
            "orientation_spatial_planet" -> OrientationQuestion(type, "¿En qué planeta vivimos?", listOf("Marte", "Venus", "Tierra", "Júpiter"), "Tierra")
            "orientation_spatial_place" -> OrientationQuestion(type, "¿Dónde estamos ahora?", listOf("En el hospital", "En el centro", "En casa", "En el parque"), "En el centro")
            "orientation_spatial_floor" -> OrientationQuestion(type, "¿En qué planta estamos?", listOf("Planta baja", "Primera planta", "Segunda planta", "Sótano"), "Planta baja")
            "orientation_spatial_kitchen" -> OrientationQuestion(type, "¿En qué parte de la casa se cocina?", listOf("Dormitorio", "Baño", "Cocina", "Garaje"), "Cocina")
            "orientation_spatial_bedroom" -> OrientationQuestion(type, "¿En qué parte de la casa dormimos?", listOf("Cocina", "Dormitorio", "Salón", "Trastero"), "Dormitorio")
            "orientation_spatial_library" -> OrientationQuestion(type, "¿Qué se suele guardar en una biblioteca?", listOf("Comida", "Ropa", "Libros", "Herramientas"), "Libros")
            "orientation_spatial_pharmacy" -> OrientationQuestion(type, "¿Qué se compra en una farmacia?", listOf("Pan", "Medicamentos", "Zapatos", "Muebles"), "Medicamentos")
            "orientation_spatial_bakery" -> OrientationQuestion(type, "¿Qué se compra en una panadería?", listOf("Carne", "Pan", "Pescado", "Libros"), "Pan")
            "orientation_spatial_ceiling" -> OrientationQuestion(type, "¿Qué suele haber en el techo de una habitación?", listOf("Suelo", "Lámpara", "Alfombra", "Cama"), "Lámpara")
            "orientation_spatial_ocean" -> OrientationQuestion(type, "¿Cómo se llama el océano más grande?", listOf("Atlántico", "Índico", "Pacífico", "Ártico"), "Pacífico")
            "orientation_spatial_moon_orbit" -> OrientationQuestion(type, "¿Alrededor de qué gira la Luna?", listOf("El Sol", "La Tierra", "Marte", "Saturno"), "La Tierra")
            "orientation_spatial_capital_spain" -> OrientationQuestion(type, "¿Cuál es la capital de España?", listOf("Barcelona", "Sevilla", "Madrid", "Valencia"), "Madrid")

            "orientation_personal_name" -> OrientationQuestion(type, "¿Cuál es su nombre completo?", listOf("Juan Pérez", "María García", "Usted mismo", "Pepe"), "Usted mismo")
            "orientation_personal_surname" -> OrientationQuestion(type, "¿Cuál es su primer apellido?", listOf("García", "Rodríguez", "Usted mismo", "Pérez"), "Usted mismo")
            
            "orientation_calc_year_days" -> OrientationQuestion(type, "¿Cuántos días tiene un año normal?", listOf("360", "365", "366", "400"), "365")
            "orientation_calc_year_months" -> OrientationQuestion(type, "¿Cuántos meses tiene un año?", listOf("10", "12", "14", "24"), "12")
            "orientation_calc_week_days" -> OrientationQuestion(type, "¿Cuántos días tiene una semana?", listOf("5", "7", "10", "30"), "7")
            "orientation_calc_day_hours" -> OrientationQuestion(type, "¿Cuántas horas tiene un día completo?", listOf("12", "24", "48", "60"), "24")
            "orientation_calc_minutes_hour" -> OrientationQuestion(type, "¿Cuántos minutos tiene una hora?", listOf("30", "60", "90", "100"), "60")
            "orientation_calc_seconds_minute" -> OrientationQuestion(type, "¿Cuántos segundos tiene un minuto?", listOf("30", "60", "90", "100"), "60")
            "orientation_calc_half_day" -> OrientationQuestion(type, "¿Cuántas horas son medio día?", listOf("6", "10", "12", "24"), "12")
            "orientation_calc_feet_count" -> OrientationQuestion(type, "¿Cuántos pies tiene una persona?", listOf("1", "2", "3", "4"), "2")
            "orientation_calc_hands_count" -> OrientationQuestion(type, "¿Cuántas manos tiene una persona?", listOf("1", "2", "3", "4"), "2")
            "orientation_calc_fingers_hand" -> OrientationQuestion(type, "¿Cuántos dedos tenemos en una mano?", listOf("4", "5", "6", "10"), "5")
            "orientation_calc_fingers_total" -> OrientationQuestion(type, "¿Cuántos dedos tenemos en total en las dos manos?", listOf("5", "10", "15", "20"), "10")
            "orientation_calc_century_years" -> OrientationQuestion(type, "¿Cuántos años tiene un siglo?", listOf("10", "50", "100", "1000"), "100")
            "orientation_calc_decade_years" -> OrientationQuestion(type, "¿Cuántos años tiene una década?", listOf("5", "10", "20", "50"), "10")
            "orientation_calc_dozen" -> OrientationQuestion(type, "¿Cuántas unidades hay en una docena?", listOf("10", "12", "14", "20"), "12")
            "orientation_calc_half_dozen" -> OrientationQuestion(type, "¿Cuántas unidades hay en media docena?", listOf("5", "6", "7", "10"), "6")
            "orientation_calc_wheels_car" -> OrientationQuestion(type, "¿Cuántas ruedas tiene un coche normal?", listOf("2", "3", "4", "5"), "4")
            "orientation_calc_wheels_bike" -> OrientationQuestion(type, "¿Cuántas ruedas tiene una bicicleta?", listOf("1", "2", "3", "4"), "2")
            "orientation_calc_wheels_tricycle" -> OrientationQuestion(type, "¿Cuántas ruedas tiene un triciclo?", listOf("1", "2", "3", "4"), "3")

            "orientation_situational_currency" -> OrientationQuestion(type, "¿Qué moneda usamos en España?", listOf("Peseta", "Dólar", "Euro", "Libra"), "Euro")
            "orientation_situational_language" -> OrientationQuestion(type, "¿Qué idioma estamos hablando?", listOf("Inglés", "Francés", "Español", "Alemán"), "Español")
            "orientation_situational_color_sky" -> OrientationQuestion(type, "¿De qué color es el cielo en un día despejado?", listOf("Verde", "Rojo", "Azul", "Amarillo"), "Azul")
            "orientation_situational_color_grass" -> OrientationQuestion(type, "¿De qué color es la hierba?", listOf("Azul", "Verde", "Blanco", "Negro"), "Verde")
            "orientation_situational_king" -> OrientationQuestion(type, "¿Quién es el Rey de España actual?", listOf("Juan Carlos I", "Felipe VI", "Alfonso XIII", "Felipe V"), "Felipe VI")
            "orientation_situational_blood" -> OrientationQuestion(type, "¿De qué color es la sangre?", listOf("Azul", "Rojo", "Verde", "Amarillo"), "Rojo")
            "orientation_situational_fire" -> OrientationQuestion(type, "¿Qué sensación produce el fuego?", listOf("Frío", "Calor", "Humedad", "Hambre"), "Calor")
            "orientation_situational_ice" -> OrientationQuestion(type, "¿Qué sensación produce el hielo?", listOf("Calor", "Frío", "Picor", "Sueño"), "Frío")
            "orientation_situational_sun" -> OrientationQuestion(type, "¿Por dónde sale el sol?", listOf("Norte", "Sur", "Este", "Oeste"), "Este")
            "orientation_situational_lemon_taste" -> OrientationQuestion(type, "¿Qué sabor tiene el limón?", listOf("Dulce", "Salado", "Ácido", "Amargo"), "Ácido")
            "orientation_situational_sugar_taste" -> OrientationQuestion(type, "¿Qué sabor tiene el azúcar?", listOf("Dulce", "Salado", "Picante", "Ácido"), "Dulce")
            "orientation_situational_sea_water" -> OrientationQuestion(type, "¿Cómo es el agua del mar?", listOf("Dulce", "Salada", "Potable", "Caliente"), "Salada")
            "orientation_situational_stop_color" -> OrientationQuestion(type, "¿De qué color es una señal de STOP?", listOf("Verde", "Azul", "Rojo", "Amarillo"), "Rojo")
            "orientation_situational_zebra_cross" -> OrientationQuestion(type, "¿De qué color son las rayas de un paso de cebra?", listOf("Negro y Rojo", "Blanco y Negro", "Azul y Verde", "Amarillo"), "Blanco y Negro")
            "orientation_situational_traffic_light_go" -> OrientationQuestion(type, "¿Qué color del semáforo nos permite pasar?", listOf("Rojo", "Ámbar", "Verde", "Azul"), "Verde")
            "orientation_situational_traffic_light_stop" -> OrientationQuestion(type, "¿Qué color del semáforo nos obliga a parar?", listOf("Verde", "Rojo", "Azul", "Blanco"), "Rojo")
            "orientation_situational_dog_sound" -> OrientationQuestion(type, "¿Qué animal ladra?", listOf("Gato", "Perro", "Vaca", "Pájaro"), "Perro")
            "orientation_situational_cat_sound" -> OrientationQuestion(type, "¿Qué animal maúlla?", listOf("Perro", "Gato", "León", "Oveja"), "Gato")
            "orientation_situational_cow_sound" -> OrientationQuestion(type, "¿Qué animal muge?", listOf("Caballo", "Vaca", "Cerdo", "Gallo"), "Vaca")
            "orientation_situational_sheep_sound" -> OrientationQuestion(type, "¿Qué animal bala?", listOf("Oveja", "Perro", "Gato", "Vaca"), "Oveja")
            "orientation_situational_milk_color" -> OrientationQuestion(type, "¿De qué color es la leche?", listOf("Blanca", "Azul", "Amarilla", "Verde"), "Blanca")
            "orientation_situational_coal_color" -> OrientationQuestion(type, "¿De qué color es el carbón?", listOf("Blanco", "Rojo", "Negro", "Gris"), "Negro")
            "orientation_situational_tomato_color" -> OrientationQuestion(type, "¿De qué color suele ser un tomate maduro?", listOf("Verde", "Azul", "Rojo", "Amarillo"), "Rojo")
            "orientation_situational_banana_color" -> OrientationQuestion(type, "¿De qué color es un plátano maduro?", listOf("Rojo", "Verde", "Amarillo", "Violeta"), "Amarillo")
            "orientation_situational_dentist" -> OrientationQuestion(type, "¿A qué médico vamos si nos duele un diente?", listOf("Oculista", "Dentista", "Pediatra", "Cirujano"), "Dentista")
            "orientation_situational_umbrella" -> OrientationQuestion(type, "¿Qué usamos para no mojarnos cuando llueve?", listOf("Sombrero", "Paraguas", "Gafas", "Bastón"), "Paraguas")
            "orientation_situational_glasses" -> OrientationQuestion(type, "¿Qué usamos para ver mejor si tenemos mala vista?", listOf("Gafas", "Reloj", "Pendientes", "Guantes"), "Gafas")
            "orientation_situational_shoes_wear" -> OrientationQuestion(type, "¿Dónde nos ponemos los zapatos?", listOf("En las manos", "En los pies", "En la cabeza", "En las orejas"), "En los pies")
            "orientation_situational_hat_wear" -> OrientationQuestion(type, "¿Donde nos ponemos el sombrero?", listOf("En los pies", "En las manos", "En la cabeza", "En el cuello"), "En la cabeza")
            "orientation_situational_gloves_wear" -> OrientationQuestion(type, "¿Dónde nos ponemos los guantes?", listOf("En los pies", "En las manos", "En la cabeza", "En los ojos"), "En las manos")
            "orientation_situational_fridge_use" -> OrientationQuestion(type, "¿Para qué sirve el frigorífico?", listOf("Para lavar ropa", "Para enfriar comida", "Para ver la tele", "Para dormir"), "Para enfriar comida")
            "orientation_situational_chair_use" -> OrientationQuestion(type, "¿Para qué sirve una silla?", listOf("Para comer", "Para sentarse", "Para saltar", "Para correr"), "Para sentarse")
            "orientation_situational_bed_use" -> OrientationQuestion(type, "¿Para qué sirve la cama?", listOf("Para cocinar", "Para dormir", "Para bañarse", "Para estudiar"), "Para dormir")
            "orientation_situational_eyes_count" -> OrientationQuestion(type, "¿Cuántos ojos tiene una persona?", listOf("1", "2", "3", "4"), "2")
            "orientation_situational_ears_count" -> OrientationQuestion(type, "¿Cuántas orejas tiene una persona?", listOf("1", "2", "3", "4"), "2")
            "orientation_situational_nose_count" -> OrientationQuestion(type, "¿Cuántas narices tiene una persona?", listOf("1", "2", "3", "4"), "1")
            "orientation_situational_mouth_count" -> OrientationQuestion(type, "¿Cuántas bocas tiene una persona?", listOf("1", "2", "3", "4"), "1")
            "orientation_situational_head_count" -> OrientationQuestion(type, "¿Cuántas cabezas tiene una persona?", listOf("1", "2", "3", "4"), "1")
            "orientation_situational_arms_count" -> OrientationQuestion(type, "¿Cuántos brazos tiene una persona?", listOf("1", "2", "3", "4"), "2")
            "orientation_situational_legs_count" -> OrientationQuestion(type, "¿Cuántas piernas tiene una persona?", listOf("1", "2", "3", "4"), "2")
            "orientation_situational_hair_color" -> OrientationQuestion(type, "¿Qué color de pelo suele tener un anciano?", listOf("Azul", "Rojo", "Blanco/Gris", "Verde"), "Blanco/Gris")
            "orientation_situational_sun_shape" -> OrientationQuestion(type, "¿Qué forma tiene el sol?", listOf("Cuadrada", "Redonda", "Triangular", "Alargada"), "Redonda")
            "orientation_situational_ball_shape" -> OrientationQuestion(type, "¿Qué forma tiene un balón?", listOf("Plana", "Esférica", "Rectangular", "Cúbica"), "Esférica")
            "orientation_situational_table_use" -> OrientationQuestion(type, "¿Para qué usamos una mesa?", listOf("Para dormir", "Para apoyarnos y comer", "Para caminar", "Para bañarnos"), "Para apoyarnos y comer")
            "orientation_situational_knife_use" -> OrientationQuestion(type, "¿Para qué sirve un cuchillo?", listOf("Para escribir", "Para cortar", "Para peinarse", "Para beber"), "Para cortar")
            "orientation_situational_spoon_use" -> OrientationQuestion(type, "¿Para qué sirve una cuchara?", listOf("Para cortar", "Para comer sopa", "Para coser", "Para pintar"), "Para comer sopa")
            "orientation_situational_comb_use" -> OrientationQuestion(type, "¿Para qué sirve un peine?", listOf("Para lavarse los pies", "Para peinarse", "Para comer", "Para ver la tele"), "Para peinarse")
            "orientation_situational_soap_use" -> OrientationQuestion(type, "¿Para qué sirve el jabón?", listOf("Para comer", "Para lavarse", "Para escribir", "Para jugar"), "Para lavarse")
            "orientation_situational_towel_use" -> OrientationQuestion(type, "¿Para qué sirve la toalla?", listOf("Para mojarse", "Para secarse", "Para cocinar", "Para barrer"), "Para secarse")
            "orientation_situational_broom_use" -> OrientationQuestion(type, "¿Para qué sirve la escoba?", listOf("Para cocinar", "Para barrer", "Para dormir", "Para cantar"), "Para barrer")
            "orientation_situational_oven_use" -> OrientationQuestion(type, "¿Para qué sirve el horno?", listOf("Para enfriar", "Para calentar y cocinar", "Para lavar", "Para planchar"), "Para calentar y cocinar")
            "orientation_situational_pill_use" -> OrientationQuestion(type, "¿Para qué tomamos medicinas?", listOf("Para alimentarnos", "Para curarnos o mejorar", "Para jugar", "Para dormir"), "Para curarnos o mejorar")
            "orientation_situational_phone_use" -> OrientationQuestion(type, "¿Para qué sirve el teléfono?", listOf("Para cocinar", "Para hablar con otros", "Para barrer", "Para lavar"), "Para hablar con otros")
            "orientation_situational_keys_use" -> OrientationQuestion(type, "¿Para qué sirven las llaves?", listOf("Para comer", "Para abrir y cerrar puertas", "Para peinarse", "Para pintar"), "Para abrir y cerrar puertas")
            "orientation_situational_glasses_use" -> OrientationQuestion(type, "¿Para qué sirven las gafas?", listOf("Para oír mejor", "Para ver mejor", "Para oler mejor", "Para saborear"), "Para ver mejor")
            "orientation_situational_watch_use" -> OrientationQuestion(type, "¿Para qué sirve el reloj?", listOf("Para saber el peso", "Para saber la hora", "Para saber la temperatura", "Para saber el precio"), "Para saber la hora")
            "orientation_situational_wallet_use" -> OrientationQuestion(type, "¿Para qué sirve la cartera?", listOf("Para guardar comida", "Para guardar dinero y documentos", "Para guardar ropa", "Para guardar herramientas"), "Para guardar dinero y documentos")
            "orientation_situational_calendar_use" -> OrientationQuestion(type, "¿Para qué sirve un calendario?", listOf("Para saber la hora", "Para saber la fecha", "Para saber el tiempo", "Para saber el peso"), "Para saber la fecha")
            "orientation_situational_doctor_tool" -> OrientationQuestion(type, "¿Qué aparato usa el médico para escuchar el corazón?", listOf("Gafas", "Estetoscopio", "Termómetro", "Martillo"), "Estetoscopio")
            "orientation_situational_firemen" -> OrientationQuestion(type, "¿Quién apaga los incendios?", listOf("Policía", "Bomberos", "Médicos", "Carteros"), "Bomberos")
            "orientation_situational_stewardess" -> OrientationQuestion(type, "¿Dónde trabajan las azafatas?", listOf("En el tren", "En el avión", "En el barco", "En el autobús"), "En el avión")
            "orientation_situational_pilot" -> OrientationQuestion(type, "¿Quién conduce un avión?", listOf("Chofer", "Piloto", "Capitán", "Maquinista"), "Piloto")
            "orientation_situational_ship_captain" -> OrientationQuestion(type, "¿Quién manda en un barco?", listOf("Piloto", "Capitán", "Director", "Jefe"), "Capitán")
            "orientation_situational_cow_milk" -> OrientationQuestion(type, "¿Qué animal nos da la leche?", listOf("El perro", "La vaca", "El gato", "El pájaro"), "La vaca")
            "orientation_situational_hen_eggs" -> OrientationQuestion(type, "¿Qué animal pone huevos?", listOf("La perra", "La gallina", "La gata", "La vaca"), "La gallina")
            "orientation_situational_bee_honey" -> OrientationQuestion(type, "¿Qué insecto hace la miel?", listOf("Mosca", "Abeja", "Hormiga", "Grillo"), "Abeja")
            "orientation_situational_spider_web" -> OrientationQuestion(type, "¿Qué insecto teje telas?", listOf("Araña", "Abeja", "Hormiga", "Mosquito"), "Araña")
            
            else -> OrientationQuestion(
                type, 
                "Pregunta de orientación: $type", 
                listOf("Opción A", "Opción B", "Correcta", "Opción D"), 
                "Correcta"
            )
        }
    }

    private fun getMonthName(m: Int): String = listOf("", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")[m]
    private fun getDayName(d: Int): String = listOf("Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")[d.coerceIn(0, 7)]
    private fun getSeason(m: Int): String = when(m) { in 3..5 -> "Primavera"; in 6..8 -> "Verano"; in 9..11 -> "Otoño"; else -> "Invierno" }
    private fun getPartDay(h: Int): String = when(h) { in 6..12 -> "Mañana"; in 13..20 -> "Tarde"; in 21..23 -> "Noche"; else -> "Madrugada" }
}
