import re

with open("app/src/main/java/com/example/ui/screens/AuthScreens.kt", "r") as f:
    content = f.read()

content = content.replace("package com.example.ui.screens\n\nimport android.content.Context\nimport android.content.ContextWrapper\n\nfun Context.findActivity(): Activity? = when (this) {\n    is Activity -> this\n    is ContextWrapper -> baseContext.findActivity()\n    else -> null\n}\n", "package com.example.ui.screens\n\nimport android.content.Context\nimport android.content.ContextWrapper\n")

content = content + "\n\nfun Context.findActivity(): Activity? = when (this) {\n    is Activity -> this\n    is ContextWrapper -> baseContext.findActivity()\n    else -> null\n}\n"

with open("app/src/main/java/com/example/ui/screens/AuthScreens.kt", "w") as f:
    f.write(content)
