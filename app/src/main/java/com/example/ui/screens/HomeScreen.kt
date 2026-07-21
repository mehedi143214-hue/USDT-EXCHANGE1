package com.example.ui.screens

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Email
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.ui.platform.testTag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Order
import com.example.ui.viewmodels.AuthViewModel
import com.example.ui.viewmodels.MainViewModel
import kotlinx.coroutines.delay

import androidx.compose.material.icons.automirrored.filled.ContactSupport

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    mainViewModel: MainViewModel,
    authViewModel: AuthViewModel,
    onNavigateToBuy: () -> Unit,
    onNavigateToSell: () -> Unit,
    onNavigateToDeposit: () -> Unit,
    onNavigateToOrderDetails: (Int) -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToCompany: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToTerms: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToRefer: () -> Unit,
    onLogout: () -> Unit
) {
    val currentUser by mainViewModel.currentUser.collectAsState()
    val userOrders by mainViewModel.currentUserOrders.collectAsState()
    val userTransactions by mainViewModel.currentUserTransactions.collectAsState()
    val rate by mainViewModel.exchangeRate.collectAsState()
    val notifications by mainViewModel.notifications.collectAsState()
    val unreadCount by mainViewModel.unreadNotificationsCount.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showNotifications by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(when(selectedTab) {
                        0 -> "Dashboard"
                        1 -> "Order History"
                        2 -> "Profile"
                        else -> "Settings"
                    }) 
                },
                actions = {
                    Box {
                        IconButton(onClick = { 
                            showNotifications = true 
                            mainViewModel.markNotificationsAsSeen()
                        }) {
                            Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                            if (unreadCount > 0) {
                                Badge(
                                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp),
                                    containerColor = Color.Red
                                ) {
                                    Text(unreadCount.toString(), color = Color.White, fontSize = 10.sp)
                                }
                            }
                        }
                        DropdownMenu(
                            expanded = showNotifications,
                            onDismissRequest = { showNotifications = false },
                            modifier = Modifier.width(300.dp).padding(8.dp)
                        ) {
                            Text("Notifications", fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                            HorizontalDivider()
                            if (notifications.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No new notifications") },
                                    onClick = { showNotifications = false }
                                )
                            } else {
                                notifications.take(5).forEach { notif ->
                                    DropdownMenuItem(
                                        text = { 
                                            Column {
                                                Text(notif.message, fontSize = 14.sp)
                                                Text(
                                                    java.text.SimpleDateFormat("dd MMM, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(notif.createdAt)), 
                                                    fontSize = 10.sp, 
                                                    color = Color.Gray
                                                )
                                            }
                                        },
                                        onClick = { showNotifications = false }
                                    )
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home", tint = if (selectedTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) },
                    label = { Text("Home", color = if (selectedTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.History, contentDescription = "History", tint = if (selectedTab == 1) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) },
                    label = { Text("History", color = if (selectedTab == 1) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Profile", tint = if (selectedTab == 2) Color(0xFF2196F3) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) },
                    label = { Text("Profile", color = if (selectedTab == 2) Color(0xFF2196F3) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = if (selectedTab == 3) Color(0xFFFF9800) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) },
                    label = { Text("Settings", color = if (selectedTab == 3) Color(0xFFFF9800) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 }
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            val context = androidx.compose.ui.platform.LocalContext.current
            when (selectedTab) {
                0 -> HomeTab(
                    rate = rate, 
                    totalUsdt = currentUser?.usdtBalance ?: 0.0,
                    totalInr = currentUser?.inrBalance ?: 0.0,
                    onNavigateToBuy = onNavigateToBuy,
                    onNavigateToSell = onNavigateToSell,
                    onNavigateToSupport = onNavigateToSupport,
                    onNavigateToDeposit = onNavigateToDeposit,
                    onNavigateToRefer = onNavigateToRefer
                )
                1 -> HistoryTab(
                    userOrders = userOrders,
                    userTransactions = userTransactions,
                    onNavigateToOrderDetails = onNavigateToOrderDetails
                )
                2 -> ProfileTab(
                    currentUser = currentUser,
                    onNavigateToSupport = onNavigateToSupport,
                    onNavigateToCompany = onNavigateToCompany,
                    onNavigateToPrivacy = onNavigateToPrivacy,
                    onNavigateToTerms = onNavigateToTerms,
                    onNavigateToAbout = onNavigateToAbout,
                    onNavigateToAdmin = onNavigateToAdmin,
                    onNavigateToRefer = onNavigateToRefer,
                    onLogout = {
                        authViewModel.logout()
                        onLogout()
                    }
                )
                3 -> SettingsTab(
                    mainViewModel = mainViewModel
                )
            }
        }
    }
}

@Composable
fun HomeTab(
    rate: Double, 
    totalUsdt: Double,
    totalInr: Double,
    onNavigateToBuy: () -> Unit,
    onNavigateToSell: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToDeposit: () -> Unit,
    onNavigateToRefer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        val banners = listOf(
            "🚀 Best Rates Guaranteed - Buy & Sell USDT seamlessly",
            "🔒 Bank-Grade Security - 100% protected transactions",
            "⚡ Lightning Fast - Payouts and releases in minutes"
        )
        val pagerState = rememberPagerState(pageCount = { banners.size })
        LaunchedEffect(Unit) {
            while (true) {
                delay(3000)
                val nextPage = (pagerState.currentPage + 1) % banners.size
                pagerState.animateScrollToPage(
                    page = nextPage,
                    animationSpec = tween(durationMillis = 800)
                )
            }
        }
        
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) { page ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = banners[page],
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        // Landing page Prominent Buy/Sell USDT with INR Hero Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .testTag("buy_sell_landing_banner"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "🇮🇳 Buy & Sell USDT with INR",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Fast, secure, peer-to-peer and instant bank transfer settlement with 0% extra fee.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onNavigateToBuy,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1f).height(40.dp).testTag("buy_usdt_landing_btn"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Buy USDT", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Button(
                        onClick = onNavigateToSell,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.weight(1f).height(40.dp).testTag("sell_usdt_landing_btn"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Sell USDT", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        // Balance Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("USDT Balance", color = Color.White.copy(alpha = 0.9f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "%.2f".format(totalUsdt),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("INR Balance", color = Color.White.copy(alpha = 0.9f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₹%.2f".format(totalInr),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Current Rate: 1 USDT = ₹%.2f".format(rate), color = Color.White.copy(alpha = 0.9f))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Quick Actions", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            QuickActionItem(icon = Icons.Filled.ShoppingCart, label = "Buy USDT", onClick = onNavigateToBuy)
            QuickActionItem(icon = Icons.Filled.Payments, label = "Sell USDT", onClick = onNavigateToSell)
            QuickActionItem(icon = Icons.Filled.AccountBalance, label = "Deposit", onClick = onNavigateToDeposit)
            QuickActionItem(icon = Icons.Filled.HeadsetMic, label = "Support", onClick = onNavigateToSupport)
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        // Refer & Earn Card
        Card(
            onClick = onNavigateToRefer,
            modifier = Modifier.fillMaxWidth().testTag("refer_dashboard_banner"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Unspecified)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFF9800), // Orange
                                Color(0xFFFF5722)  // Deep Orange / Sunset
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CardGiftcard,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Refer & Earn (10 Levels)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Earn passive commission down to 10 levels!",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Why Choose Us?", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(12.dp))
        
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FeatureItem(icon = Icons.Filled.Security, title = "Bank-Grade Security", description = "Your assets are protected with industry-leading security protocols and cold storage.")
            FeatureItem(icon = Icons.Filled.FlashOn, title = "Instant Transactions", description = "Experience lightning-fast deposits and withdrawals with automated processing.")
            FeatureItem(icon = Icons.Filled.VerifiedUser, title = "Regulated & Compliant", description = "Fully licensed platform adhering to strict KYC/AML guidelines for your safety.")
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(value = "50K+", label = "Active Users")
                StatItem(value = "$2M+", label = "24h Volume")
                StatItem(value = "99.9%", label = "Uptime")
            }
        }
        
        Spacer(modifier = Modifier.height(80.dp)) // padding for bottom nav
    }
}

@Composable
fun QuickActionItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick).padding(8.dp)) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, shape = androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun FeatureItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketTab(rate: Double, onNavigateToBuy: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text("Market", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            onClick = onNavigateToBuy,
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF26A17B), shape = androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "₮",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Tether (USDT)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp)
                        Text("TRC20 Network", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 14.sp)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("₹%.2f".format(rate), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
                    Button(onClick = onNavigateToBuy, modifier = Modifier.padding(top = 4.dp).height(32.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)) {
                        Text("Buy")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            onClick = onNavigateToBuy,
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF26A17B), shape = androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "₮",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Tether (USDT)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp)
                        Text("BEP20 Network", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 14.sp)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("₹%.2f".format(rate), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
                    Button(onClick = onNavigateToBuy, modifier = Modifier.padding(top = 4.dp).height(32.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3BA2F), contentColor = Color.Black)) {
                        Text("Buy")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun HistoryTab(
    userOrders: List<Order>,
    userTransactions: List<com.example.data.Transaction>,
    onNavigateToOrderDetails: (Int) -> Unit
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Buy USDT", "Deposits")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSubTab == index,
                    onClick = { selectedSubTab = index },
                    text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            if (selectedSubTab == 0) {
                // USDT Buy History
                if (userOrders.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No buy orders yet.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(userOrders) { order ->
                            OrderCard(order = order, onClick = { onNavigateToOrderDetails(order.id) })
                        }
                    }
                }
            } else {
                // Deposit History
                val deposits = userTransactions.filter { it.type == "Deposit" }
                if (deposits.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No deposits yet.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(deposits) { tx ->
                            TransactionCard(tx = tx)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionCard(tx: com.example.data.Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (tx.type == "Deposit") "Deposit INR" else tx.type,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (tx.transactionHash != null) {
                    Text(
                        text = "UTR: ${tx.transactionHash}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Text(
                    text = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(tx.createdAt)),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${tx.amount}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                StatusChip(status = tx.status)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTab(
    currentUser: com.example.data.User?,
    onNavigateToSupport: () -> Unit,
    onNavigateToCompany: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToTerms: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToRefer: () -> Unit,
    onLogout: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("Profile", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(currentUser?.name ?: "User", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(currentUser?.email ?: "", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            onClick = onNavigateToRefer,
            modifier = Modifier.fillMaxWidth().testTag("refer_profile_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.CardGiftcard, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Refer & Earn", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("Invite friends & manage your team network", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(onClick = onNavigateToSupport, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.ContactSupport, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Text("Customer Support", fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        Card(onClick = onNavigateToCompany, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Text("Company", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Card(onClick = onNavigateToPrivacy, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Text("Privacy Policy", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Card(onClick = onNavigateToTerms, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Text("Terms & Conditions", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Card(onClick = onNavigateToAbout, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Text("About", fontWeight = FontWeight.Bold)
            }
        }

        
        Spacer(modifier = Modifier.height(12.dp))
        if (currentUser?.role == "Admin") {
            Spacer(modifier = Modifier.height(12.dp))
            Card(onClick = onNavigateToAdmin, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Admin Panel", fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Card(onClick = onLogout, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                Spacer(modifier = Modifier.width(16.dp))
                Text("Logout", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderCard(order: Order, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Order: ${order.orderId}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Amount: ₹${order.fiatAmount} -> %.2f USDT".format(order.usdtAmount), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
            }
            StatusChip(status = order.status)
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val color = when (status) {
        "Completed", "Approved", "USDT Sent" -> MaterialTheme.colorScheme.primary
        "Pending Payment", "Under Review", "Processing" -> MaterialTheme.colorScheme.tertiary
        "Rejected", "Cancelled" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = status,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SettingsTab(mainViewModel: MainViewModel) {
    val isDarkMode by mainViewModel.isDarkMode.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Appearance", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dark Mode", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { mainViewModel.setDarkMode(it) }
                )
            }
        }
    }
}
