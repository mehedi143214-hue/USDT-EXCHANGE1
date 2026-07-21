import re

with open("app/src/main/java/com/example/ui/screens/AuthScreens.kt", "r") as f:
    content = f.read()

imports_to_add = """import android.content.Context
import android.content.ContextWrapper

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
"""
content = content.replace("package com.example.ui.screens\n\n", "package com.example.ui.screens\n\n" + imports_to_add)

content = content.replace("viewModel.sendPhoneOtp(phoneNumber, context as Activity)", "context.findActivity()?.let { viewModel.sendPhoneOtp(phoneNumber, it) } ?: Toast.makeText(context, \"Activity not found\", Toast.LENGTH_SHORT).show()")

with open("app/src/main/java/com/example/ui/screens/AuthScreens.kt", "w") as f:
    f.write(content)
