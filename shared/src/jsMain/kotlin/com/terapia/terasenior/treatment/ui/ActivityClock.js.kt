package com.terapia.terasenior.treatment.ui

actual fun platformActivityTimeMillisText(): String = js("Date.now().toString()") as String
