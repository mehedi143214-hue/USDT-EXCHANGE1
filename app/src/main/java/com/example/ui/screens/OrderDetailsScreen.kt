package com.example.ui.screens
import androidx.compose.foundation.verticalScroll

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(
    orderId: Int,
    mainViewModel: MainViewModel,
    onBack: () -> Unit
) {
    val orders by mainViewModel.currentUserOrders.collectAsState()
    val order = orders.find { it.id == orderId }

    var transactionId by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (order?.status == "Pending Payment") "Payment Page" else "Order Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        if (order == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Order not found.", color = MaterialTheme.colorScheme.onBackground)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .imePadding()
                    .verticalScroll(androidx.compose.foundation.rememberScrollState())
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Order ID:", color = MaterialTheme.colorScheme.onSurface)
                            Text(order.orderId, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Status:", color = MaterialTheme.colorScheme.onSurface)
                            StatusChip(order.status)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Amount to Pay:", color = MaterialTheme.colorScheme.onSurface)
                            Text("₹${order.fiatAmount}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("USDT to Receive:", color = MaterialTheme.colorScheme.onSurface)
                            Text("%.2f USDT".format(order.usdtAmount), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Wallet: ${order.walletAddress}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                }

                if (order.status == "Pending Payment") {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Payment Instructions", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Please transfer ₹${order.fiatAmount} to the following bank account:", color = MaterialTheme.colorScheme.onBackground)
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Bank Name: Example Bank", color = MaterialTheme.colorScheme.onSurface)
                            Text("Account Name: USDT Exchange Inc.", color = MaterialTheme.colorScheme.onSurface)
                            Text("Account No: 123456789012", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            Text("IFSC Code: EXBK0001234", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = transactionId,
                        onValueChange = { transactionId = it },
                        label = { Text("Transaction ID / UTR") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { mainViewModel.submitPaymentProof(order, transactionId) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = transactionId.isNotBlank()
                    ) {
                        Text("Submit Payment Proof")
                    }
                } else if (order.adminNote != null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Admin Note:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    Text(order.adminNote, color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
    }
}
