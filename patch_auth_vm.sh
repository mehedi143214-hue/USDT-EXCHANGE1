#!/bin/bash
cat << 'INNER_EOF' > app/src/main/java/com/example/ui/viewmodels/AuthViewModel.kt
package com.example.ui.viewmodels

import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.data.User
import com.example.data.UserPreferencesRepository
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(
    private val repository: AppRepository,
    private val userPrefs: UserPreferencesRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    val loggedInUserId = userPrefs.loggedInUserId.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun loginWithGoogle(credential: Credential) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            if (credential !is CustomCredential) {
                _authState.value = AuthState.Error("Invalid credential type")
                return@launch
            }
            if (credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                _authState.value = AuthState.Error("Unexpected credential type")
                return@launch
            }
            
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val firebaseUser = authResult.user
                
                if (firebaseUser != null) {
                    val email = firebaseUser.email ?: "unknown@example.com"
                    val existingUser = repository.getUserByEmail(email)
                    if (existingUser != null) {
                        userPrefs.saveLoggedInUserId(existingUser.id)
                    } else {
                        val newUser = User(
                            name = firebaseUser.displayName ?: "User",
                            email = email,
                            phone = "",
                            passwordHash = "", 
                            role = if (email.contains("admin")) "Admin" else "User"
                        )
                        val id = repository.insertUser(newUser)
                        userPrefs.saveLoggedInUserId(id.toInt())
                    }
                    _authState.value = AuthState.Success
                } else {
                    _authState.value = AuthState.Error("Firebase user is null")
                }
            } catch (e: GoogleIdTokenParsingException) {
                _authState.value = AuthState.Error("Failed to parse Google ID Token")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Google Sign-In failed")
            }
        }
    }

    fun login(email: String, passwordHash: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            // Hardcoded Admin bypassing Firebase
            if (email == "kawsarali742304@gmail.com" && passwordHash == "Kawsar@1432") {
                val existing = repository.getUserByEmail(email)
                if (existing == null) {
                    val newUser = User(
                        name = "Admin",
                        email = email,
                        phone = "",
                        passwordHash = passwordHash,
                        role = "Admin"
                    )
                    repository.insertUser(newUser)
                } else if (existing.role != "Admin" || existing.passwordHash != passwordHash) {
                    val updatedUser = existing.copy(role = "Admin", passwordHash = passwordHash)
                    repository.updateUser(updatedUser)
                }
                val user = repository.getUserByEmail(email)
                if (user != null) {
                    userPrefs.saveLoggedInUserId(user.id)
                    _authState.value = AuthState.Success
                    return@launch
                }
            }
            
            try {
                // Login via Firebase
                auth.signInWithEmailAndPassword(email, passwordHash).await()
                
                val user = repository.getUserByEmail(email)
                if (user != null) {
                    userPrefs.saveLoggedInUserId(user.id)
                    _authState.value = AuthState.Success
                } else {
                    // Create local user profile if it doesn't exist
                    val newUser = User(
                        name = "User",
                        email = email,
                        phone = "",
                        passwordHash = "", // Don't store password anymore
                        role = "User"
                    )
                    val id = repository.insertUser(newUser)
                    userPrefs.saveLoggedInUserId(id.toInt())
                    _authState.value = AuthState.Success
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Authentication failed")
            }
        }
    }

    fun register(name: String, email: String, phone: String, passwordHash: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            try {
                // Register via Firebase
                auth.createUserWithEmailAndPassword(email, passwordHash).await()
                
                val role = if (email.contains("admin")) "Admin" else "User"
                
                val newUser = User(
                    name = name,
                    email = email,
                    phone = phone,
                    passwordHash = "", // Security: Do not store actual passwords
                    role = role
                )
                val id = repository.insertUser(newUser)
                userPrefs.saveLoggedInUserId(id.toInt())
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            auth.signOut()
            userPrefs.clearLoggedInUserId()
        }
    }
    
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
INNER_EOF
