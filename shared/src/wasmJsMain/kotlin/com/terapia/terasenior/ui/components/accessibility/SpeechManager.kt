package com.terapia.terasenior.ui.components.accessibility

import kotlin.js.ExperimentalWasmJsInterop

/**
 * Implementación de Voz para Web (WASM) usando la Web Speech API.
 */
actual class SpeechManager actual constructor() {
    @OptIn(ExperimentalWasmJsInterop::class)
    actual fun speak(text: String) {
        speakJs(text)
    }

    @OptIn(ExperimentalWasmJsInterop::class)
    actual fun stop() {
        cancelJs()
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun speakJs(text: String) {
    js("""
        var msg = new SpeechSynthesisUtterance();
        msg.text = text;
        msg.lang = 'es-ES';
        window.speechSynthesis.speak(msg);
    """)
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun cancelJs() {
    js("window.speechSynthesis.cancel();")
}
