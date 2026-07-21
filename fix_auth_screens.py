with open("app/src/main/java/com/example/ui/screens/AuthScreens.kt", "r") as f:
    content = f.read()

bad_imports = "import androidx.compose.ui.text.input.VisualTransformationimport androidx.compose.material.icons.filled.Visibilityimport androidx.compose.material.icons.filled.VisibilityOffpackage com.example.ui.screens"
good_imports = "package com.example.ui.screens\n\nimport androidx.compose.ui.text.input.VisualTransformation\nimport androidx.compose.material.icons.filled.Visibility\nimport androidx.compose.material.icons.filled.VisibilityOff"

if bad_imports in content:
    content = content.replace(bad_imports, good_imports)

with open("app/src/main/java/com/example/ui/screens/AuthScreens.kt", "w") as f:
    f.write(content)
