with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Add imports
if "import com.google.firebase.FirebaseApp" not in content:
    content = content.replace("import com.example.ui.viewmodels.MainViewModel", "import com.example.ui.viewmodels.MainViewModel\nimport com.google.firebase.FirebaseApp\nimport com.google.firebase.FirebaseOptions")

# Add init block
init_block = """        enableEdgeToEdge()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApiKey("AIzaSyBsvrwW1LjnwQOcOLhUWL-w7R-BHEhv3-E")
                    .setApplicationId("1:304994101736:web:bcbd2693fbdb900a9b9c5d")
                    .setProjectId("usdt-exchange-fd361")
                    .build()
                FirebaseApp.initializeApp(this, options)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }"""
content = content.replace("        enableEdgeToEdge()", init_block)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
