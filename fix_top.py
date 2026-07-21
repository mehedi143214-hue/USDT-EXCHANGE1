with open("app/src/main/java/com/example/ui/screens/AuthScreens.kt", "r") as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if line.strip() in [
        "package com.example.ui.screens",
        "import androidx.compose.ui.text.input.VisualTransformation",
        "import androidx.compose.material.icons.filled.Visibility",
        "import androidx.compose.material.icons.filled.VisibilityOff"
    ]:
        continue
    new_lines.append(line)

new_top = [
    "package com.example.ui.screens\n",
    "\n",
    "import androidx.compose.ui.text.input.VisualTransformation\n",
    "import androidx.compose.material.icons.filled.Visibility\n",
    "import androidx.compose.material.icons.filled.VisibilityOff\n",
    "import androidx.compose.material3.IconButton\n"
]

lines = new_top + new_lines

with open("app/src/main/java/com/example/ui/screens/AuthScreens.kt", "w") as f:
    f.writelines(lines)
