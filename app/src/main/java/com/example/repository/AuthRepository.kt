package com.example.repository

import android.content.Context
import android.util.Log
import com.example.supabase
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String, 
    @SerialName("full_name") val fullName: String, 
    val email: String, 
    @SerialName("selected_categories") val selectedCategories: List<String> = emptyList()
)

data class AppUser(val uid: String, val email: String, val displayName: String)

class AuthRepository(context: Context) {

    private val _currentUser = MutableStateFlow<AppUser?>(null)
    val currentUser: StateFlow<AppUser?> = _currentUser

    init {
        CoroutineScope(Dispatchers.IO).launch {
            supabase.auth.sessionStatus.collect { status ->
                val user = supabase.auth.currentSessionOrNull()?.user
                if (user != null) {
                    val profileName = user.userMetadata?.get("name")?.toString()?.removeSurrounding("\"") ?: "User"
                    _currentUser.value = AppUser(
                        uid = user.id,
                        email = user.email ?: "",
                        displayName = profileName
                    )
                } else {
                    _currentUser.value = null
                }
            }
        }
    }
    
    suspend fun signIn(email: String, pass: String): Result<Unit> {
        return try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = pass
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(name: String, email: String, pass: String): Result<Unit> {
        return try {
            val user = supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = pass
            }
            val uid = user?.id
            if (uid != null) {
                val profile = UserProfile(id = uid, fullName = name, email = email)
                try {
                    supabase.postgrest["profiles"].insert(profile)
                } catch (e: Exception) {
                    Log.e("AuthRepository", "Error creating profile", e)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            supabase.auth.resetPasswordForEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(): Result<Unit> {
        return try {
            supabase.auth.signInWith(Google)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut(): Result<Unit> {
        return try {
            supabase.auth.signOut()
            Result.success(Unit)
        } catch(e: Exception) {
            Result.failure(e)
        }
    }
}

