package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.User
import com.example.data.Order
import com.example.ui.viewmodels.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferAndTeamScreen(
    mainViewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by mainViewModel.currentUser.collectAsState()
    val allUsers by mainViewModel.allUsers.collectAsState()
    val allOrders by mainViewModel.allOrders.collectAsState()

    // Active tab: 0 -> Team Network, 1 -> Commission Rates, 2 -> Earning History
    var activeSubTab by remember { mutableIntStateOf(0) }

    val referralCode = remember(currentUser) {
        val user = currentUser
        when {
            user == null -> "Loading..."
            user.phone.isNotBlank() -> user.phone
            user.email.isNotBlank() -> {
                val prefix = user.email.substringBefore("@")
                if (prefix.isNotBlank()) prefix else "REF${user.id}"
            }
            else -> "REF${user.id}"
        }
    }
    
    val referralLink = "https://github.com/mehedi143214-hue/USDT-EXCHANGE1/releases/latest"

    // Compute the 10 level network and commission data dynamically
    val teamData = remember(currentUser, allUsers, allOrders) {
        val user = currentUser
        if (user == null) {
            TeamData(emptyMap(), 0.0, 0.0, emptyList())
        } else {
            calculate10LevelTeam(user, allUsers, allOrders)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Refer & Earn (10 Levels)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Gold Banner Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFFFD54F), // Bright Golden Yellow
                                        Color(0xFFFFB300)  // Deep Amber Gold
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.CardGiftcard,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Invite Friends, Earn 10 Levels!",
                                color = Color.Black,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Earn passive trading commissions down to 10 levels of referred members, plus instant registration rewards for expanding your team!",
                                color = Color.Black.copy(alpha = 0.8f),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Earnings Stats Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🎁 Your Referral Earnings Summary",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Total Earning", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                                Text("₹%.2f".format(teamData.totalCommission + teamData.totalSignupRewards), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Trading Commissions", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                                Text("₹%.2f".format(teamData.totalCommission), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Signup Rewards", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                                Text("₹%.2f".format(teamData.totalSignupRewards), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Network Size:",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            val totalNetworkSize = teamData.levels.values.sumOf { it.size }
                            Text(
                                text = "$totalNetworkSize members (10 Levels)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Referral Code Display
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Your Referral Code",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Referral Code Display
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    RoundedCornerShape(8.dp)
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = referralCode,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Referral Code", referralCode)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Referral code copied!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(24.dp).testTag("copy_code_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ContentCopy,
                                        contentDescription = "Copy Referral Code",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Your Referral Link",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // Referral Link Display
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    RoundedCornerShape(8.dp)
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = referralLink,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Referral Link", referralLink)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Referral link copied!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(24.dp).testTag("copy_link_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ContentCopy,
                                        contentDescription = "Copy Referral Link",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Share Link Button
                        Button(
                            onClick = {
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Hey! Sign up and trade USDT with INR on this high-yield 10-level reward platform. Get started now:\n$referralLink\nReferral Code: $referralCode"
                                    )
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Referral Link"))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("share_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Share Referral Link", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Sub Tab Selection Bar
            item {
                TabRow(selectedTabIndex = activeSubTab, modifier = Modifier.fillMaxWidth()) {
                    Tab(
                        selected = activeSubTab == 0,
                        onClick = { activeSubTab = 0 },
                        text = { Text("Team Network", fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Filled.People, contentDescription = null) }
                    )
                    Tab(
                        selected = activeSubTab == 1,
                        onClick = { activeSubTab = 1 },
                        text = { Text("10-Level Rates", fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Filled.Info, contentDescription = null) }
                    )
                    Tab(
                        selected = activeSubTab == 2,
                        onClick = { activeSubTab = 2 },
                        text = { Text("Earnings", fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Filled.TrendingUp, contentDescription = null) }
                    )
                }
            }

            // Active tab content rendering
            when (activeSubTab) {
                0 -> {
                    // Team Network (10 expandable levels)
                    item {
                        Text(
                            text = "My 10-Level Referral Network",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    for (levelNum in 1..10) {
                        val membersInLevel = teamData.levels[levelNum] ?: emptyList()
                        item {
                            ExpandableLevelRow(
                                level = levelNum,
                                commissionRate = getLevelRatePercentage(levelNum),
                                members = membersInLevel
                            )
                        }
                    }
                }
                1 -> {
                    // Commission Rates
                    item {
                        CommissionRatesCard()
                    }
                }
                2 -> {
                    // Earning History
                    if (teamData.commissionHistory.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(24.dp)
                                        .fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.TrendingUp,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No earnings history yet",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "You will earn trading commissions automatically when your team members make successful USDT buy/sell transactions.",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(teamData.commissionHistory) { record ->
                            CommissionHistoryRow(record = record)
                        }
                    }
                }
            }

            // Margin at bottom
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// Expandable list row for team level
@Composable
fun ExpandableLevelRow(
    level: Int,
    commissionRate: String,
    members: List<User>
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (members.isNotEmpty()) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (members.isNotEmpty()) 1.dp else 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = level.toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Level $level Network",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Commission: $commissionRate rate • Signup reward: ₹${getSignupReward(level)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = if (members.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        contentColor = if (members.isNotEmpty()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "${members.size} members",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (expanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                if (members.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No team members on Level $level yet.\nInvite friends to expand your multi-level hierarchy!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        members.forEach { member ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = member.name.take(2).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = member.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Joined: " + SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(member.createdAt)),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "Active Team",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 10-Level Rate Sheet Card Composable
@Composable
fun CommissionRatesCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📊 10-Level Reward Structure",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "When anyone in your 10-level network buys or sells USDT, the platform shares a percentage of the transaction service fee with you instantly:",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            val levelsData = listOf(
                Pair("Level 1", "5.0%"),
                Pair("Level 2", "3.0%"),
                Pair("Level 3", "2.0%"),
                Pair("Level 4", "1.5%"),
                Pair("Level 5", "1.0%"),
                Pair("Level 6", "0.8%"),
                Pair("Level 7", "0.5%"),
                Pair("Level 8", "0.3%"),
                Pair("Level 9", "0.2%"),
                Pair("Level 10", "0.1%")
            )

            levelsData.chunked(2).forEach { pairList ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    pairList.forEach { pair ->
                        Card(
                            modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(pair.first, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(pair.second, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "🎁 Signup Registration Bonuses",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Additionally, you get cash/signup bonuses for every register down your tree:\n" +
                       "• Level 1 registration: ₹10\n" +
                       "• Level 2 registration: ₹3\n" +
                       "• Levels 3-10 registration: ₹1 each",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                lineHeight = 18.sp
            )
        }
    }
}

// Commission History Row Composable
@Composable
fun CommissionHistoryRow(record: CommissionRecord) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Commission from ${record.memberName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Level ${record.level} Trade • Ord: ${record.orderId} (₹%.2f)".format(record.orderAmount),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    text = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(record.timestamp)),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
            Text(
                text = "+₹%.4f".format(record.commissionAmount),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF4CAF50) // Green
            )
        }
    }
}

// Level Rates / Signup Bonuses Utilities
fun getLevelRatePercentage(level: Int): String {
    return when (level) {
        1 -> "5.0%"
        2 -> "3.0%"
        3 -> "2.0%"
        4 -> "1.5%"
        5 -> "1.0%"
        6 -> "0.8%"
        7 -> "0.5%"
        8 -> "0.3%"
        9 -> "0.2%"
        10 -> "0.1%"
        else -> "0.0%"
    }
}

fun getSignupReward(level: Int): Int {
    return when (level) {
        1 -> 10
        2 -> 3
        else -> 1
    }
}

// Data models for 10 level calculation
data class TeamData(
    val levels: Map<Int, List<User>>,
    val totalCommission: Double,
    val totalSignupRewards: Double,
    val commissionHistory: List<CommissionRecord>
)

data class CommissionRecord(
    val memberName: String,
    val level: Int,
    val orderId: String,
    val orderAmount: Double,
    val commissionAmount: Double,
    val timestamp: Long
)

// 10 level network and commission math function
fun calculate10LevelTeam(
    currentUser: User,
    allUsers: List<User>,
    allOrders: List<Order>
): TeamData {
    val levels = mutableMapOf<Int, MutableList<User>>()
    for (i in 1..10) {
        levels[i] = mutableListOf()
    }

    // Direct match parent code (it can be parent's phone, email, or id string)
    val directCodes = listOfNotNull(
        currentUser.phone.trim().lowercase(),
        currentUser.email.trim().lowercase(),
        currentUser.id.toString()
    )

    // Level 1 users (direct referrals)
    val level1List = allUsers.filter { user ->
        val refCodeClean = user.referralCode?.trim()?.lowercase() ?: ""
        refCodeClean in directCodes && user.id != currentUser.id
    }
    levels[1]?.addAll(level1List)

    // Calculate level 2 to 10 recursively
    var prevLevelUsers = level1List
    for (lvl in 2..10) {
        if (prevLevelUsers.isEmpty()) break
        
        val prevCodes = prevLevelUsers.flatMap { u ->
            listOfNotNull(u.phone.trim().lowercase(), u.email.trim().lowercase(), u.id.toString())
        }.toSet()
        
        // Find users referred by previous level, not already counted in previous levels to avoid loops
        val levelList = allUsers.filter { user ->
            val refCodeClean = user.referralCode?.trim()?.lowercase() ?: ""
            refCodeClean in prevCodes &&
                    user.id != currentUser.id &&
                    !levels.values.flatMap { it.map { it.id } }.contains(user.id)
        }
        levels[lvl]?.addAll(levelList)
        prevLevelUsers = levelList
    }

    // Rates map
    val commissionRates = mapOf(
        1 to 0.05,
        2 to 0.03,
        3 to 0.02,
        4 to 0.015,
        5 to 0.01,
        6 to 0.008,
        7 to 0.005,
        8 to 0.003,
        9 to 0.002,
        10 to 0.001
    )

    var totalCommissionEarned = 0.0
    val commissionHistory = mutableListOf<CommissionRecord>()

    // For each level, calculate trade rewards from completed transactions of users in that level
    for (lvl in 1..10) {
        val usersInLvl = levels[lvl] ?: emptyList()
        val userIdsInLvl = usersInLvl.map { it.id }.toSet()
        val rate = commissionRates[lvl] ?: 0.0
        
        // Match completed orders of members in this level
        val ordersInLvl = allOrders.filter { it.userId in userIdsInLvl && (it.status == "Completed" || it.status == "Approved") }
        for (order in ordersInLvl) {
            val earnt = order.serviceFee * rate
            totalCommissionEarned += earnt
            val member = usersInLvl.find { it.id == order.userId }
            if (member != null) {
                commissionHistory.add(
                    CommissionRecord(
                        memberName = member.name,
                        level = lvl,
                        orderId = order.orderId,
                        orderAmount = order.fiatAmount,
                        commissionAmount = earnt,
                        timestamp = order.createdAt
                    )
                )
            }
        }
    }

    // Direct registration rewards calculation: E.g., ₹10 for L1, ₹3 for L2, ₹1 for L3-L10
    var totalSignupRewards = 0.0
    for (lvl in 1..10) {
        val size = levels[lvl]?.size ?: 0
        val reward = when (lvl) {
            1 -> 10.0
            2 -> 3.0
            else -> 1.0
        }
        totalSignupRewards += size * reward
    }

    return TeamData(
        levels = levels,
        totalCommission = totalCommissionEarned,
        totalSignupRewards = totalSignupRewards,
        commissionHistory = commissionHistory.sortedByDescending { it.timestamp }
    )
}
