package com.terapia.terasenior.ui.components.accessibility

/**
 * Implementación dummy para Android para cumplir con el contrato expect/actual.
 * TODO: Integrar con TextToSpeech usando un contexto inyectado.
 */
actual class SpeechManager actual constructor() {
    actual fun speak(text: String) {
        // En Android requiere Context, se implementará vía CompositionLocal o Inyección
    }

    actual fun stop() {
    }
}
