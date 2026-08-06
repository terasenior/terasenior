package com.terapia.terasenior.ui.components.accessibility

import kotlinx.browser.window

/**
 * Implementación de Voz para JS (Browser) usando la Web Speech API.
 */
actual class SpeechManager actual constructor() {
    actual fun speak(text: String) {
        val msg = js("new SpeechSynthesisUtterance()")
        msg.text = text
        msg.lang = "es-ES"
        window.asDynamic().speechSynthesis.speak(msg)
    }

    actual fun stop() {
        window.asDynamic().speechSynthesis.cancel()
    }
}
