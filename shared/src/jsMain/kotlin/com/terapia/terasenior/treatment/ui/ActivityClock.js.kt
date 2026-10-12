package com.terapia.terasenior.treatment.ui

actual fun platformActivityTimeMillisText(): String = js("Date.now().toString()") as String

actual fun platformTodayIsoDate(): String = js("""
    (() => {
        const date = new Date();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return date.getFullYear() + '-' + month + '-' + day;
    })()
""") as String
