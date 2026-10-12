package com.terapia.terasenior

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemoryCodeVerifierCache
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.postgrest.Postgrest

@OptIn(SupabaseInternal::class)
val supabase = createSupabaseClient(
    supabaseUrl = "https://qwrykyyjxsfbeazroequ.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InF3cnlreXlqeHNmYmVhenJvZXF1Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODQ5ODYyNTQsImV4cCI6MjEwMDU2MjI1NH0.Z88UBb_AWQqMdeLSMBiNg0ZYIaz5NGKcTzusnvbHYcI"
) {
    install(Auth) {
        // Wasm no puede inicializar el gestor de Settings de la librería de
        // autenticación. Usar almacenamiento en memoria evita esa ruta nativa.
        // La sesión se crea normalmente al iniciar sesión de forma explícita.
        sessionManager = MemorySessionManager()
        codeVerifierCache = MemoryCodeVerifierCache()
        autoLoadFromStorage = false
        autoSaveToStorage = false
        alwaysAutoRefresh = false
        // La inicialización automática de plataforma de auth ejecuta trabajo en
        // segundo plano al crear el cliente. En Wasm falla antes de interactuar
        // con la pantalla; el acceso por correo sigue funcionando de forma explícita.
        autoSetupPlatform = false
    }
    install(Postgrest)
}
