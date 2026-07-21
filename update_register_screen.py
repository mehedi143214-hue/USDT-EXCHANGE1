import re

with open("app/src/main/java/com/example/ui/screens/AuthScreens.kt", "r") as f:
    content = f.read()

register_screen_old = """    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()"""

register_screen_new = """    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()"""

content = content.replace(register_screen_old, register_screen_new)

register_button_old = """        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { viewModel.register(name, email, phone, password) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            } else {
                Text("Sign Up", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }"""

register_button_new = """        if (authState is AuthState.OtpSent) {
            OutlinedTextField(
                value = otpCode,
                onValueChange = { otpCode = it },
                label = { Text("OTP Code") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { viewModel.verifyPhoneOtp(otpCode) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Verify OTP & Sign Up", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { 
                    context.findActivity()?.let { 
                        viewModel.registerWithPhoneVerification(name, email, phone, password, it) 
                    } ?: Toast.makeText(context, "Activity not found", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Send OTP", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }"""

content = content.replace(register_button_old, register_button_new)

with open("app/src/main/java/com/example/ui/screens/AuthScreens.kt", "w") as f:
    f.write(content)
