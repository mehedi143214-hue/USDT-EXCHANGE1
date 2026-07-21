import re

with open("app/src/main/java/com/example/ui/viewmodels/AuthViewModel.kt", "r") as f:
    content = f.read()

pending_data = """
    private var pendingRegistrationUser: User? = null

    fun registerWithPhoneVerification(name: String, email: String, phone: String, passwordHash: String, activity: Activity) {
        pendingRegistrationUser = User(name = name, email = email, phone = phone, passwordHash = passwordHash, role = "User")
        sendPhoneOtp(phone, activity)
    }
"""

content = content.replace("private var storedVerificationId: String? = null", pending_data + "    private var storedVerificationId: String? = null")

signInWithPhoneAuthCredential_old = """
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            try {
                val authResult = auth.signInWithCredential(credential).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val phone = firebaseUser.phoneNumber ?: "unknown_phone"
                    val existingUser = repository.getUserByEmail(phone)
                    if (existingUser != null) {
                        userPrefs.saveLoggedInUserId(existingUser.id)
                    } else {
                        val newUser = User(
                            name = "Phone User",
                            email = phone,
                            phone = phone,
                            passwordHash = "", 
                            role = "User"
                        )
                        val id = repository.insertUser(newUser)
                        userPrefs.saveLoggedInUserId(id.toInt())
                    }
                    _authState.value = AuthState.Success
                } else {
                    _authState.value = AuthState.Error("Firebase user is null")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Phone Sign-In failed")
            }
        }
    }
"""

signInWithPhoneAuthCredential_new = """
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
                            val id = repository.insertUser(pendingRegistrationUser!!.copy(phone = phone))
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
"""

content = content.replace(signInWithPhoneAuthCredential_old.strip(), signInWithPhoneAuthCredential_new.strip())

with open("app/src/main/java/com/example/ui/viewmodels/AuthViewModel.kt", "w") as f:
    f.write(content)
