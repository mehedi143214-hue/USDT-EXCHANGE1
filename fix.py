with open("app/src/main/java/com/example/ui/screens/AuthScreens.kt", "r") as f:
    content = f.read()

import re
content = re.sub(r'\} catch \(e: Exception\) \{\n\s+viewModel\.simulateGoogleLogin\(\)\n\s+Toast\.makeText\(context, "Simulated Google Sign-In \(Demo mode\)", Toast\.LENGTH_SHORT\)\.show\(\)\n\s+\}', r'} catch (e: GetCredentialException) {\n                        Toast.makeText(context, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()\n                    } catch (e: Exception) {\n                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()\n                    }', content)

with open("app/src/main/java/com/example/ui/screens/AuthScreens.kt", "w") as f:
    f.write(content)
