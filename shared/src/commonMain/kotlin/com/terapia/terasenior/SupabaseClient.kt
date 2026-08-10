package com.terapia.terasenior

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

val supabase = createSupabaseClient(
    supabaseUrl = "https://qwrykyyjxsfbeazroequ.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InF3cnlreXlqeHNmYmVhenJvZXF1Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODQ5ODYyNTQsImV4cCI6MjEwMDU2MjI1NH0.Z88UBb_AWQqMdeLSMBiNg0ZYIaz5NGKcTzusnvbHYcI"
) {
    install(Auth)
    install(Postgrest)
    install(Storage)
}