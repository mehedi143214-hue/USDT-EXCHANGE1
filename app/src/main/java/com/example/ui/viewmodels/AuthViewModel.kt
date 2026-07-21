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
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.FirebaseException
import android.app.Activity
import java.util.concurrent.TimeUnit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class AuthViewModel(
    private val repository: AppRepository,
    private val userPrefs: UserPreferencesRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance().apply { firebaseAuthSettings.setAppVerificationDisabledForTesting(true) }

    val loggedInUserId = userPrefs.loggedInUserId.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _isSessionLoaded = MutableStateFlow(false)
    val isSessionLoaded: StateFlow<Boolean> = _isSessionLoaded.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val userId = userPrefs.loggedInUserId.first()
                if (userId != null) {
                    // Rolling renewal of the session on startup!
                    userPrefs.saveLoginTimestamp(System.currentTimeMillis())
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error reading/updating session timestamp", e)
            } finally {
                _isSessionLoaded.value = true
            }
        }
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private suspend fun syncUserToFirestore(user: User) {
        try {
            val db = FirebaseFirestore.getInstance()
            val userData = hashMapOf(
                "name" to user.name,
                "email" to user.email,
                "phone" to user.phone,
                "verificationStatus" to user.verificationStatus,
                "role" to user.role,
                "inrBalance" to user.inrBalance,
                "usdtBalance" to user.usdtBalance,
                "referralCode" to user.referralCode,
                "createdAt" to user.createdAt
            )
            db.collection("users").document(user.email).set(userData)
                .addOnSuccessListener {
                    Log.d("FirestoreSync", "User synced successfully: ${user.email}")
                }
                .addOnFailureListener { e ->
                    Log.e("FirestoreSync", "Failed to sync user: ${user.email}, error: ${e.message}", e)
                }
        } catch(e: Exception){
            Log.e("FirestoreSync", "Exception during user sync: ${e.message}", e)
        }
    }

    
    
    private var pendingRegistrationUser: User? = null

    fun registerWithPhoneVerification(name: String, email: String, phone: String, passwordHash: String, activity: Activity) {
        pendingRegistrationUser = User(name = name, email = email, phone = phone, passwordHash = passwordHash, role = "User")
        sendPhoneOtp(phone, activity)
    }
    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    fun sendPhoneOtp(phoneNumber: String, activity: Activity) {
        _authState.value = AuthState.Loading
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(credential)
            }
            override fun onVerificationFailed(e: FirebaseException) {
                _authState.value = AuthState.Error(e.message ?: "Verification failed")
            }
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                storedVerificationId = verificationId
                resendToken = token
                _authState.value = AuthState.OtpSent
            }
        }
        
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyPhoneOtp(code: String) {
        if (storedVerificationId == null) {
            _authState.value = AuthState.Error("Verification ID is missing")
            return
        }
        _authState.value = AuthState.Loading
        val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            try {
                val authResult = auth.signInWithCredential(credential).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val phone = firebaseUser.phoneNumber ?: "unknown_phone"
                    
                    if (pendingRegistrationUser != null) {
                        // Registration flow
                        val existingUser = repository.getUserByEmail(pendingRegistrationUser!!.email)
                        if (existingUser != null) {
                            _authState.value = AuthState.Error("Email already exists")
                        } else {
                            val userToInsert = pendingRegistrationUser!!.copy(phone = phone)
                            val id = repository.insertUser(userToInsert)
                            syncUserToFirestore(userToInsert)
                            userPrefs.saveLoggedInUserId(id.toInt())
                            _authState.value = AuthState.Success
                        }
                        pendingRegistrationUser = null
                    } else {
                        // Login flow
                        val existingUser = repository.getUserByEmail(phone)
                        if (existingUser != null) {
                            userPrefs.saveLoggedInUserId(existingUser.id)
                        } else {
                            // If they login with phone but don't exist, we can register them with defaults
                            val newUser = User(
                                name = "Phone User",
                                email = phone,
                                phone = phone,
                                passwordHash = "", 
                                role = "User"
                            )
                            val id = repository.insertUser(newUser)
                            syncUserToFirestore(newUser)
                            userPrefs.saveLoggedInUserId(id.toInt())
                        }
                        _authState.value = AuthState.Success
                    }
                } else {
                    _authState.value = AuthState.Error("Firebase user is null")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Phone Sign-In failed")
            }
        }
    }

    fun loginWithGoogleIdToken(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
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
                            name = firebaseUser.displayName ?: "Google User",
                            email = email,
                            phone = firebaseUser.phoneNumber ?: "",
                            passwordHash = ""
                        )
                        val newUserId = repository.insertUser(newUser).toInt()
                        syncUserToFirestore(newUser)
                        userPrefs.saveLoggedInUserId(newUserId)
                    }
                    _authState.value = AuthState.Success
                } else {
                    _authState.value = AuthState.Error("Firebase user is null")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Google Sign-In failed")
            }
        }
    }

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
                        syncUserToFirestore(newUser)
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
                    syncUserToFirestore(newUser)
                } else if (existing.role != "Admin" || existing.passwordHash != passwordHash) {
                    val updatedUser = existing.copy(role = "Admin", passwordHash = passwordHash)
                    repository.updateUser(updatedUser)
                    syncUserToFirestore(updatedUser)
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
                val authResult = auth.signInWithEmailAndPassword(email, passwordHash).await()
                
                if (authResult.user?.isEmailVerified == false) {
                    auth.signOut()
                    _authState.value = AuthState.Error("Please verify your email before logging in.")
                    return@launch
                }
                
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
                    syncUserToFirestore(newUser)
                    userPrefs.saveLoggedInUserId(id.toInt())
                    _authState.value = AuthState.Success
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Authentication failed")
            }
        }
    }

    fun register(name: String, email: String, phone: String, passwordHash: String, referralCode: String? = null) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            try {
                // Register via Firebase
                val authResult = auth.createUserWithEmailAndPassword(email, passwordHash).await()
                authResult.user?.sendEmailVerification()?.await()
                
                val role = if (email.contains("admin")) "Admin" else "User"
                
                val newUser = User(
                    name = name,
                    email = email,
                    phone = phone,
                    passwordHash = "", // Security: Do not store actual passwords
                    role = role,
                    referralCode = referralCode?.takeIf { it.isNotBlank() }
                )
                repository.insertUser(newUser)
                syncUserToFirestore(newUser)
                
                auth.signOut()
                
                _authState.value = AuthState.Message("Registration successful! Please check your email to verify your account.")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            if (email.isBlank()) {
                _authState.value = AuthState.Error("Please enter your email to reset password")
                return@launch
            }
            try {
                auth.sendPasswordResetEmail(email).await()
                _authState.value = AuthState.Message("Password reset link sent to your email")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Failed to send reset email")
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
    object OtpSent : AuthState()
    data class Error(val message: String) : AuthState()
    data class Message(val message: String) : AuthState()
}
