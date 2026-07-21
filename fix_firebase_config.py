import re
with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

old_config = """                val options = FirebaseOptions.Builder()
                    .setApiKey("AIzaSyBsvrwW1LjnwQOcOLhUWL-w7R-BHEhv3-E")
                    .setApplicationId("1:304994101736:web:bcbd2693fbdb900a9b9c5d")
                    .setProjectId("usdt-exchange-fd361")
                    .build()"""

new_config = """                // CRITICAL FIX: Phone Authentication on Android requires an ANDROID Application ID.
                // The previous ID ("1:...:web:...") was a Web Application ID, which causes the
                // "operation is not allowed / sign-in-provider is disabled" error on Android.
                // You must replace this with your actual Android Application ID from your Firebase Console.
                val options = FirebaseOptions.Builder()
                    .setApiKey("AIzaSyBsvrwW1LjnwQOcOLhUWL-w7R-BHEhv3-E")
                    .setApplicationId("1:304994101736:android:REPLACE_WITH_YOUR_ANDROID_HASH") // <-- FIX REQUIRED HERE
                    .setProjectId("usdt-exchange-fd361")
                    .build()"""

content = content.replace(old_config, new_config)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
