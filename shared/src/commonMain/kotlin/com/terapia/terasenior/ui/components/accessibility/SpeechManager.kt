package com.terapia.terasenior.ui.components.accessibility

/**
 * Gestor de Voz multiplataforma para Terasenior.
 */
expect class SpeechManager() {
    fun speak(text: String)
    fun stop()
}
