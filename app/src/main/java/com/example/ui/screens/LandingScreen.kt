package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

val PrimaryGreen = Color(0xFF00E676)
val EmeraldGreen = Color(0xFF00C853)
val DarkNavy = Color(0xFF070B14)
val CardDark = Color(0xFF111827)
val GoldColor = Color(0xFFFFD700)
val DangerRed = Color(0xFFFF3D00)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandingScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CurrencyExchange,
                            contentDescription = "Logo",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("USDT EXCHANGE", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    }
                },
                actions = {
                    TextButton(onClick = onNavigateToLogin) {
                        Text("Log In", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = onNavigateToRegister,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Get Started", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkNavy.copy(alpha = 0.95f),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = DarkNavy
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 2. HERO SECTION
            item {
                HeroSection(onNavigateToRegister, onNavigateToLogin)
            }
            
            // 3. TRUST BADGES
            item {
                TrustBadges()
            }
            
            // 4. WHY CHOOSE US
            item {
                WhyChooseUsSection()
            }
            
            // 5. PRICE COMPARISON
            item {
                PriceComparisonSection()
            }
            
            // 6. APP PREVIEW
            item {
                AppPreviewSection()
            }
            
            // 7. HOW TO BUY USDT
            item {
                HowToBuySection()
            }
            
            // 8. SECURITY
            item {
                SecuritySection()
            }
            
            // 9. SUPPORTED NETWORKS
            item {
                SupportedNetworksSection()
            }
            
            // 10. LOW PRICE / VALUE
            item {
                LowPriceValueSection(onNavigateToRegister)
            }
            
            // 11. CUSTOMER SUPPORT
            item {
                CustomerSupportSection(onNavigateToSupport)
            }
            
            // 12. FOOTER
            item {
                FooterSection(
                    onNavigateToHome,
                    onNavigateToAbout,
                    onNavigateToPrivacy,
                    onNavigateToSupport
                )
            }
        }
    }
}

@Composable
fun HeroSection(onRegister: () -> Unit, onLogin: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D1F17), DarkNavy)
                )
            )
            .padding(horizontal = 24.dp, vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "BUY & SELL USDT WITH INR",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                style = androidx.compose.ui.text.TextStyle(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFFFFD700), Color(0xFF00E676))
                    )
                ),
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Fast • Secure • 10-Level Referral",
                fontSize = 15.sp,
                color = PrimaryGreen,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            // Dual buy/sell price cards
            Row(
                modifier = Modifier.fillMaxWidth(0.95f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Buy Rate Highlight Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.5.dp, Color(0xFFFFD700).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .background(Color(0xFF5D4037).copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("BUY USDT AT", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "₹98 - ₹100",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD700)
                        )
                    }
                }
                
                // Sell Rate Highlight Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.5.dp, PrimaryGreen.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .background(Color(0xFF0D1F17).copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SELL USDT AT", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "₹101 - ₹102",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryGreen
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Dual Buy/Sell buttons
            Row(
                modifier = Modifier.fillMaxWidth(0.95f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onRegister,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen, contentColor = Color.Black),
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("BUY USDT", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                }
                Button(
                    onClick = onRegister,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800), contentColor = Color.Black),
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("SELL USDT", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onLogin,
                border = BorderStroke(1.5.dp, PrimaryGreen),
                modifier = Modifier.fillMaxWidth(0.95f).height(50.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("LOGIN / GET STARTED", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
            }
        }
    }
}

@Composable
fun TrustBadges() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TrustBadgeItem(Icons.Filled.Speed, "FAST", Color(0xFF00E5FF))
        TrustBadgeItem(Icons.Filled.VerifiedUser, "SECURE", Color(0xFF00E676))
        TrustBadgeItem(Icons.Filled.ThumbUp, "TRUSTED", Color(0xFFFFD700))
    }
}

@Composable
fun TrustBadgeItem(icon: ImageVector, text: String, tint: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(CardDark, CircleShape)
                .border(1.dp, tint.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = text, tint = tint, modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun WhyChooseUsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("WHY CHOOSE US", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        Spacer(modifier = Modifier.height(32.dp))
        
        val features = listOf(
            val_feature(Icons.Filled.Security, "FAST & SECURE", "100% safe and secure transactions.", Color(0xFF00E5FF)),
            val_feature(Icons.Filled.AttachMoney, "BEST PRICE", "Get competitive USDT pricing.", Color(0xFFFFD700)),
            val_feature(Icons.Filled.Handshake, "FACE 2 FACE DEAL", "Trusted face-to-face deals available where applicable.", Color(0xFFE040FB)),
            val_feature(Icons.Filled.AccountBalanceWallet, "CASH DEPOSIT", "Convenient cash deposit options where available.", Color(0xFF00E676)),
            val_feature(Icons.Filled.Bolt, "INSTANT DELIVERY", "USDT can be delivered to the selected wallet after successful payment.", Color(0xFFFF6D00)),
            val_feature(Icons.Filled.HeadsetMic, "24/7 SUPPORT", "Our support team is available to assist users.", Color(0xFF29B6F6)),
            val_feature(Icons.Filled.MoneyOff, "NO HIDDEN FEES", "Transparent pricing and clear transaction details.", Color(0xFFEEFF41))
        )
        
        features.forEach { feature ->
            FeatureRowItem(feature.icon, feature.title, feature.desc, feature.color)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

data class val_feature(val icon: ImageVector, val title: String, val desc: String, val color: Color)

@Composable
fun FeatureRowItem(icon: ImageVector, title: String, description: String, accentColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardDark, RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(accentColor.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = accentColor)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, color = accentColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, color = Color.LightGray, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}

@Composable
fun PriceComparisonSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardDark.copy(alpha = 0.5f))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("WHY PAY MORE?", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Others
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(CardDark, RoundedCornerShape(12.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("OTHERS PLATFORM", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("RATE", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("₹104 - ₹105", color = DangerRed, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                Text("/ USDT", color = Color.LightGray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Icon(Icons.Filled.Cancel, contentDescription = null, tint = DangerRed, modifier = Modifier.size(24.dp))
            }
            
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(40.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFFF3D00), Color(0xFFFFB300))
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("VS", color = Color.White, fontWeight = FontWeight.Black)
            }
            
            // Us
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(PrimaryGreen.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .border(2.dp, PrimaryGreen, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("USDT EXCHANGE", color = PrimaryGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("RATE", color = PrimaryGreen.copy(alpha = 0.8f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("₹98 - ₹100", color = PrimaryGreen, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                Text("/ USDT", color = PrimaryGreen, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(24.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .background(Brush.horizontalGradient(listOf(Color(0xFF7C4DFF).copy(alpha = 0.5f), Color(0xFFFF6D00).copy(alpha = 0.5f))))
                .padding(vertical = 12.dp, horizontal = 24.dp)
        ) {
            Text("SAVE MORE ON EVERY PURCHASE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, textAlign = TextAlign.Center)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "* Rates may vary based on market conditions and applicable fees.",
            color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AppPreviewSection() {
    var previewBuy by remember { mutableStateOf(true) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("EXPERIENCE USDT EXCHANGE", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Our modern mobile app makes buying & selling digital assets effortless.", color = Color.LightGray, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(32.dp))
        
        // App Mockup UI
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(Color.Black, RoundedCornerShape(32.dp))
                .border(4.dp, Color(0xFF333333), RoundedCornerShape(32.dp))
                .padding(8.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(CardDark)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Toggle between Buy & Sell in Preview
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (previewBuy) PrimaryGreen else Color.Transparent)
                            .clickable { previewBuy = true }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Buy USDT Preview", color = if (previewBuy) Color.Black else Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (!previewBuy) Color(0xFFFF9800) else Color.Transparent)
                            .clickable { previewBuy = false }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Sell USDT Preview", color = if (!previewBuy) Color.Black else Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(if (previewBuy) "Buy USDT" else "Sell USDT", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(24.dp))
                
                // Exchange Details Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1F2937), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Exchange Details", color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text(if (previewBuy) "Balance: ₹0.00" else "Balance: 0.00 USDT", color = PrimaryGreen, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = if (previewBuy) "10,000" else "100.00",
                            onValueChange = {},
                            label = { Text(if (previewBuy) "Amount to Pay (INR)" else "Amount to Sell (USDT)", color = if (previewBuy) PrimaryGreen else Color(0xFFFF9800)) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 18.sp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (previewBuy) PrimaryGreen else Color(0xFFFF9800),
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(if (previewBuy) "Min. ₹98 - Max. ₹50,000" else "Min. 1.00 - Max. 5,000.00 USDT", color = Color.Gray, fontSize = 12.sp)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Current Rate:", color = Color.LightGray)
                            Text(if (previewBuy) "₹98 - ₹100 / USDT" else "₹101 - ₹102 / USDT", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Service Fee:", color = Color.LightGray)
                            Text(if (previewBuy) "₹1.00" else "0.00 USDT (0% Free)", color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("You Receive:", color = if (previewBuy) PrimaryGreen else Color(0xFFFF9800), fontWeight = FontWeight.Bold)
                            Text(if (previewBuy) "100.00 USDT" else "₹10,100.00", color = if (previewBuy) PrimaryGreen else Color(0xFFFF9800), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                if (previewBuy) {
                    Text("Select Network", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.border(1.dp, PrimaryGreen, RoundedCornerShape(8.dp)).background(PrimaryGreen.copy(alpha=0.2f), RoundedCornerShape(8.dp)).padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Text("TRC20", color = PrimaryGreen)
                        }
                        Box(modifier = Modifier.border(1.dp, Color.Gray, RoundedCornerShape(8.dp)).padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Text("BEP20", color = Color.Gray)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        placeholder = { Text("USDT Wallet Address", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Gray)
                    )
                } else {
                    Text("Select Cashout Settlement", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.border(1.dp, Color(0xFFFF9800), RoundedCornerShape(8.dp)).background(Color(0xFFFF9800).copy(alpha=0.2f), RoundedCornerShape(8.dp)).padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Text("UPI Transfer", color = Color(0xFFFF9800))
                        }
                        Box(modifier = Modifier.border(1.dp, Color.Gray, RoundedCornerShape(8.dp)).padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Text("Bank Account", color = Color.Gray)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        placeholder = { Text("Enter UPI ID or Bank Details", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Gray)
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (previewBuy) PrimaryGreen else Color(0xFFFF9800)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (previewBuy) "Confirm Purchase" else "Confirm Sale", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun HowToBuySection() {
    var activeTab by remember { mutableIntStateOf(0) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("HOW IT WORKS", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = Color.Transparent,
            contentColor = PrimaryGreen,
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("Buy USDT Flow", fontWeight = FontWeight.Bold, color = if (activeTab == 0) PrimaryGreen else Color.Gray) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("Sell USDT Flow", fontWeight = FontWeight.Bold, color = if (activeTab == 1) Color(0xFFFF9800) else Color.Gray) }
            )
        }
        
        if (activeTab == 0) {
            StepItem("01", "ENTER INR AMOUNT", "Enter the amount of INR you want to spend to buy USDT.", Color(0xFF00E5FF))
            StepItem("02", "PAY VIA UPI / BANK", "Transfer INR securely to the designated merchant account.", Color(0xFFE040FB))
            StepItem("03", "GET WALLET DELIVERY", "USDT is automatically sent to your TRC20/BEP20 wallet address.", Color(0xFF00E676))
        } else {
            StepItem("01", "ENTER SELL AMOUNT", "Enter the amount of USDT you wish to sell.", Color(0xFF00E5FF))
            StepItem("02", "SEND USDT TO SYSTEM", "Transfer USDT to the system's designated safe secure address.", Color(0xFFE040FB))
            StepItem("03", "GET PAID IN INR", "INR is credited to your bank or UPI instantly upon confirmation.", Color(0xFF00E676))
        }
    }
}

@Composable
fun StepItem(number: String, title: String, description: String, tint: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), verticalAlignment = Alignment.Top) {
        Text(number, color = tint, fontSize = 28.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, color = Color.LightGray, fontSize = 14.sp)
        }
    }
}

@Composable
fun SecuritySection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Filled.Security, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("SECURE & RELIABLE", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        Spacer(modifier = Modifier.height(24.dp))
        
        val items = listOf(
            "Secure account protection",
            "Encrypted transaction processes",
            "Wallet address verification",
            "Secure payment processing",
            "Transparent transaction details"
        )
        
        items.forEach { item ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF00E676))
                Spacer(modifier = Modifier.width(12.dp))
                Text(item, color = Color.White)
            }
        }
    }
}

@Composable
fun SupportedNetworksSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardDark.copy(alpha = 0.5f))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("SUPPORTED NETWORKS", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            NetworkBadge("TRC20", Color(0xFF00E5FF))
            NetworkBadge("BEP20", Color(0xFFFFD700))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .background(DangerRed.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                .border(1.dp, DangerRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Filled.Warning, contentDescription = "Warning", tint = DangerRed)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Always select the correct network before sending or receiving USDT. Sending assets through an incompatible network may result in permanent loss of funds.",
                    color = DangerRed,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun NetworkBadge(name: String, tint: Color) {
    Box(
        modifier = Modifier
            .border(1.dp, tint, RoundedCornerShape(24.dp))
            .background(tint.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Text(name, color = tint, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun LowPriceValueSection(onRegister: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("BETTER RATE. MORE VALUE.", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = GoldColor, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Buy at ~₹98 • Sell at ~₹101", fontSize = 16.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Text("• 0% Service Fee on Sell", color = Color.LightGray, fontSize = 12.sp)
            Text("• Instant UPI Settlement", color = Color.LightGray, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("• Secure escrow-backed transfers", color = Color.LightGray, fontSize = 12.sp)
        
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(0.95f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onRegister,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen, contentColor = Color.Black),
                modifier = Modifier.weight(1f).height(54.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("BUY NOW", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onRegister,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800), contentColor = Color.Black),
                modifier = Modifier.weight(1f).height(54.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("SELL NOW", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("* Rates are subject to market conditions.", color = Color.Gray, fontSize = 11.sp)
    }
}

@Composable
fun CustomerSupportSection(onNavigateToSupport: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("WE'RE HERE TO HELP", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SupportAction(Icons.AutoMirrored.Filled.Chat, "Contact Support", onNavigateToSupport)
            SupportAction(Icons.Filled.Email, "Email Us", onNavigateToSupport)
            SupportAction(Icons.AutoMirrored.Filled.HelpOutline, "Help Center", onNavigateToSupport)
        }
    }
}

@Composable
fun SupportAction(icon: ImageVector, text: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).padding(8.dp)
    ) {
        Box(
            modifier = Modifier.size(56.dp).background(DarkNavy, CircleShape).border(1.dp, PrimaryGreen, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = text, tint = PrimaryGreen, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun FooterSection(
    onNavigateToHome: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToSupport: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.CurrencyExchange, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("USDT EXCHANGE", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Fast • Secure • Reliable", color = PrimaryGreen, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Links", color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                FooterLink("Home", onNavigateToHome)
                FooterLink("Buy & Sell USDT", onNavigateToHome)
                FooterLink("Support", onNavigateToSupport)
            }
            Column {
                Text("Legal", color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                FooterLink("About Us", onNavigateToAbout)
                FooterLink("Privacy Policy", onNavigateToPrivacy)
                FooterLink("Terms & Conditions", onNavigateToPrivacy)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "Digital asset services involve risks. Users should use the platform responsibly and comply with applicable laws and regulations.",
            color = Color.Gray,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color.DarkGray)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "© 2026 USDT EXCHANGE. All rights reserved.",
            color = Color.Gray,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun FooterLink(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        color = Color.LightGray,
        fontSize = 14.sp,
        modifier = Modifier.clickable(onClick = onClick).padding(vertical = 6.dp)
    )
}
