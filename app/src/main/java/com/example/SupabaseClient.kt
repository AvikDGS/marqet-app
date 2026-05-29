package com.example

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest

val supabase = createSupabaseClient(
    supabaseUrl = "https://vsqvwzyeoqnefgsakhvt.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InZzcXZ3enllb3FuZWZnc2FraHZ0Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODAwMDgwMTAsImV4cCI6MjA5NTU4NDAxMH0.vwa63qdqGZAi2rfmEgXHiWrxu1P6E4Cn9mVsDGLHzH4"
) {
    install(Auth)
    install(Postgrest)
}
