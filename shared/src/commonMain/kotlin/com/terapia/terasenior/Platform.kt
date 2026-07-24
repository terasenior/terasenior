package com.terapia.terasenior

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform