with open("app/src/main/java/com/example/ui/viewmodels/AuthViewModel.kt", "r") as f:
    content = f.read()

import re
content = re.sub(r'\s*fun simulateGoogleLogin\(\) \{[\s\S]*?_authState\.value = AuthState\.Error\("Simulation failed"\)\n            \}\n        \}\n    \}', '', content)

with open("app/src/main/java/com/example/ui/viewmodels/AuthViewModel.kt", "w") as f:
    f.write(content)
