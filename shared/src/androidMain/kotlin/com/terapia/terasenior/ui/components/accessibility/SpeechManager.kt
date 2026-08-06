package com.terapia.terasenior.ui.components.accessibility

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Implementación de Voz para Android.
 * Nota: En una app real esto se inyectaría como Singleton.
 */
actual class SpeechManager(context: Context) {
    private var tts: TextToSpeech? = null
    
    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("es", "ES")
            }
        }
    }

    actual fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    actual fun stop() {
        tts?.stop()
    }
}
