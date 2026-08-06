package com.terapia.terasenior.ui.components.accessibility

/**
 * Implementación dummy de Voz para JVM (Desktop).
 * TODO: Implementar usando una librería TTS para Desktop si es necesario.
 */
actual class SpeechManager actual constructor() {
    actual fun speak(text: String) {
        println("TTS (Desktop): $text")
    }

    actual fun stop() {
        println("TTS (Desktop): Stop")
    }
}
