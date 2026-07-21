with open("app/src/main/java/com/example/ui/viewmodels/AuthViewModel.kt", "r") as f:
    content = f.read()

imports = """import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.FirebaseException
import android.app.Activity
import java.util.concurrent.TimeUnit
"""

content = content.replace("import com.google.firebase.auth.FirebaseAuth", imports + "import com.google.firebase.auth.FirebaseAuth")

phone_auth_code = """
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

content = content.replace("fun loginWithGoogle(credential: Credential) {", phone_auth_code + "\n    fun loginWithGoogle(credential: Credential) {")

content = content.replace("object Success : AuthState()", "object Success : AuthState()\n    object OtpSent : AuthState()")

with open("app/src/main/java/com/example/ui/viewmodels/AuthViewModel.kt", "w") as f:
    f.write(content)
