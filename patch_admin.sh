#!/bin/bash

cat << 'INNER_EOF' > app/src/main/java/com/example/ui/screens/AdminDashboardScreen.kt
package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.Order
import com.example.data.User
import com.example.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    mainViewModel: MainViewModel,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Approvals", "Users", "Settings")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.tertiary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                when (selectedTab) {
                    0 -> AdminApprovalsTab(mainViewModel)
                    1 -> AdminUsersTab(mainViewModel)
                    2 -> AdminSettingsTab(mainViewModel)
                }
            }
        }
    }
}

@Composable
fun AdminApprovalsTab(mainViewModel: MainViewModel) {
    val allOrders by mainViewModel.allOrders.collectAsState()
    val allTransactions by mainViewModel.allTransactions.collectAsState()
    val pendingOrders = allOrders.filter { it.status == "Under Review" }
    val pendingDeposits = allTransactions.filter { it.type == "Deposit" && it.status == "Under Review" }
    
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Pending Approvals", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(16.dp))

        if (pendingOrders.isEmpty() && pendingDeposits.isEmpty()) {
            Text("No pending orders or deposits.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                items(pendingDeposits) { tx ->
                    AdminDepositCard(
                        tx = tx,
                        onApprove = { mainViewModel.updateDepositStatus(tx, "Completed") },
                        onReject = { mainViewModel.updateDepositStatus(tx, "Rejected") }
                    )
                }
                items(pendingOrders) { order ->
                    AdminOrderCard(
                        order = order,
                        onApprove = { mainViewModel.updateOrderStatus(order, "Completed", "Payment verified. USDT Sent.") },
                        onReject = { mainViewModel.updateOrderStatus(order, "Rejected", "Invalid payment proof.") }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminUsersTab(mainViewModel: MainViewModel) {
    val allUsers by mainViewModel.allUsers.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text("User Management", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
            items(allUsers) { user ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(user.name, fontWeight = FontWeight.Bold)
                        Text(user.email, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Text("Role: ${user.role}", color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            if (user.role == "User") {
                                OutlinedButton(onClick = { mainViewModel.updateUserRole(user, "Admin") }) {
                                    Text("Make Admin")
                                }
                            } else {
                                OutlinedButton(onClick = { mainViewModel.updateUserRole(user, "User") }) {
                                    Text("Remove Admin")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSettingsTab(mainViewModel: MainViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Rate Management
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Rate Management", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(8.dp))
                val currentRate by mainViewModel.exchangeRate.collectAsState()
                var rateInput by remember(currentRate) { mutableStateOf(currentRate.toString()) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = rateInput,
                        onValueChange = { rateInput = it },
                        label = { Text("Exchange Rate (INR/USDT)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { 
                        rateInput.toDoubleOrNull()?.let { mainViewModel.updateExchangeRate(it) } 
                    }) {
                        Text("Update")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("All Orders History", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(16.dp))
        val allOrders by mainViewModel.allOrders.collectAsState()
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
            items(allOrders) { order ->
                OrderCard(order = order, onClick = {}) // Just display for admin
            }
        }
    }
}

@Composable
fun AdminDepositCard(
    tx: com.example.data.Transaction,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Deposit Request", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("₹${tx.amount}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("User ID: ${tx.userId}", color = MaterialTheme.colorScheme.onSurface)
            Text("UTR: ${tx.transactionHash ?: "N/A"}", color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onReject,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Reject", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reject")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = onApprove) {
                    Icon(Icons.Filled.Check, contentDescription = "Approve", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Approve")
                }
            }
        }
    }
}

@Composable
fun AdminOrderCard(
    order: Order,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(order.orderId, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("₹${order.fiatAmount} -> %.2f USDT".format(order.usdtAmount), color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Tx ID: ${order.paymentTransactionId ?: "N/A"}", color = MaterialTheme.colorScheme.onSurface)
            Text("Wallet: ${order.walletAddress}", color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onReject,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Reject", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reject")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = onApprove) {
                    Icon(Icons.Filled.Check, contentDescription = "Approve", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Approve")
                }
            }
        }
    }
}
INNER_EOF

chmod +x patch_admin.sh
./patch_admin.sh