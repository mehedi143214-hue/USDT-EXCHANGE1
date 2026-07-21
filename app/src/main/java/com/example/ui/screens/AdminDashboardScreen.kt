package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.example.data.Order
import com.example.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    mainViewModel: MainViewModel,
    onBack: () -> Unit
) {
    val allOrders by mainViewModel.allOrders.collectAsState()
    val allTransactions by mainViewModel.allTransactions.collectAsState()
    
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
        val pendingOrders = allOrders.filter { it.status == "Under Review" || it.status == "Processing" }
        val pendingDeposits = allTransactions.filter { it.type == "Deposit" && it.status == "Under Review" }
        val isMaintenance by mainViewModel.isMaintenanceMode.collectAsState()
        
        val firestoreError by mainViewModel.firestoreError.collectAsState()
        val context = LocalContext.current
        
        LaunchedEffect(firestoreError) {
            firestoreError?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                mainViewModel.clearFirestoreError()
            }
        }
        
        val buyRate by mainViewModel.buyExchangeRate.collectAsState()
        val sellRate by mainViewModel.sellExchangeRate.collectAsState()
        
        var buyRateInput by remember { mutableStateOf("") }
        var sellRateInput by remember { mutableStateOf("") }
        var notificationMessage by remember { mutableStateOf("") }
        
        LaunchedEffect(buyRate, sellRate) {
            if (buyRateInput.isEmpty() || buyRateInput.toDoubleOrNull() != buyRate) {
                buyRateInput = buyRate.toString()
            }
            if (sellRateInput.isEmpty() || sellRateInput.toDoubleOrNull() != sellRate) {
                sellRateInput = sellRate.toString()
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Rate Management
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Rate Management", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = buyRateInput,
                                onValueChange = { buyRateInput = it },
                                label = { Text("Buy Price (INR)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = sellRateInput,
                                onValueChange = { sellRateInput = it },
                                label = { Text("Sell Price (INR)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Button(
                            onClick = { 
                                val buy = buyRateInput.toDoubleOrNull()
                                val sell = sellRateInput.toDoubleOrNull()
                                if (buy != null && sell != null) {
                                    mainViewModel.updateRates(buy, sell)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Update Rates")
                        }
                    }
                }
            }

            // System Maintenance
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("System Maintenance", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(if (isMaintenance) "Maintenance Mode is ON" else "Maintenance Mode is OFF")
                            Switch(
                                checked = isMaintenance,
                                onCheckedChange = { mainViewModel.updateMaintenanceMode(it) }
                            )
                        }
                    }
                }
            }

            // Send Notification
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Send Notification", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(value = notificationMessage, onValueChange = { notificationMessage = it }, label = { Text("Message") }, modifier = Modifier.weight(1f), maxLines = 3)
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = { if (notificationMessage.isNotBlank()) { mainViewModel.sendNotification(notificationMessage); notificationMessage = "" } }) { Text("Send") }
                        }
                    }
                }
            }

            // Pending Approvals
            item {
                Text("Pending Approvals", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }

            if (pendingOrders.isEmpty() && pendingDeposits.isEmpty()) {
                item {
                    Text("No pending orders or deposits.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                }
            } else {
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

            // All Orders
            item {
                Text("All Orders", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }

            if (allOrders.isEmpty()) {
                item {
                    Text("No orders placed yet.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                }
            } else {
                items(allOrders) { order ->
                    OrderCard(order = order, onClick = {}) // Just display for admin
                }
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
