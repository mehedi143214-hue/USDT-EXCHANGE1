#!/bin/bash

# We will inject the Admin Panel block just before the Logout block
awk '
/Card\(onClick = onLogout/ {
    print "        if (currentUser?.role == \"Admin\") {"
    print "            Spacer(modifier = Modifier.height(12.dp))"
    print "            Card(onClick = onNavigateToAdmin, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {"
    print "                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {"
    print "                    Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)"
    print "                    Spacer(modifier = Modifier.width(16.dp))"
    print "                    Text(\"Admin Panel\", fontWeight = FontWeight.Bold)"
    print "                }"
    print "            }"
    print "        }"
    print "        "
    print $0
    next
}
{ print $0 }
' app/src/main/java/com/example/ui/screens/HomeScreen.kt > app/src/main/java/com/example/ui/screens/HomeScreen.kt.tmp

mv app/src/main/java/com/example/ui/screens/HomeScreen.kt.tmp app/src/main/java/com/example/ui/screens/HomeScreen.kt
