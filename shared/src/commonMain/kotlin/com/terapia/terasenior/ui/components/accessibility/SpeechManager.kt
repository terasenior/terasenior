package com.terapia.terasenior.ui.components.accessibility

/**
 * Gestor de Voz multiplataforma para Terasenior (v1.0.1).
 */
expect class SpeechManager() {
    fun speak(text: String)
    fun stop()
}
