package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellUsdtScreen(
    mainViewModel: MainViewModel,
    onBack: () -> Unit,
    onOrderPlaced: (Int) -> Unit
) {
    val currentUser by mainViewModel.currentUser.collectAsState()
    val rate by mainViewModel.sellExchangeRate.collectAsState()
    val feePercent by mainViewModel.serviceFee.collectAsState()

    var usdtAmount by remember { mutableStateOf("") }
    var receivingDetails by remember { mutableStateOf("") }
    var network by remember { mutableStateOf("TRC20") }
    val networks = listOf("TRC20", "BEP20")
    val paymentMethod = "Bank Transfer / UPI"

    val usdtValue = usdtAmount.toDoubleOrNull() ?: 0.0
    val grossInr = usdtValue * rate
    val feeAmount = grossInr * feePercent
    val finalInr = if (grossInr > feeAmount) grossInr - feeAmount else 0.0

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sell USDT", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            val usdtBalance = currentUser?.usdtBalance ?: 0.0
            val hasInsufficientBalance = usdtValue > usdtBalance

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Sell Details", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text("Balance: %.2f USDT".format(usdtBalance), color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = usdtAmount,
                onValueChange = { usdtAmount = it },
                label = { Text("Amount to Sell (USDT)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("sell_amount_input"),
                singleLine = true,
                isError = hasInsufficientBalance
            )
            if (hasInsufficientBalance) {
                Text(
                    text = "Insufficient USDT balance.",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, start = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Current Rate:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${rate} / USDT", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Gross Value:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹%.2f".format(grossInr), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Service Fee (${feePercent * 100}%):", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹%.2f".format(feeAmount), color = MaterialTheme.colorScheme.error)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("You Receive:", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text("₹%.2f".format(finalInr), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Network Information", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Select Release Network", color = MaterialTheme.colorScheme.onBackground)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp)) {
                networks.forEach { net ->
                    FilterChip(
                        selected = network == net,
                        onClick = { network = net },
                        label = { Text(net) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            OutlinedTextField(
                value = receivingDetails,
                onValueChange = { receivingDetails = it },
                label = { Text("Receiving UPI ID or Bank Account Details") },
                placeholder = { Text("E.g., upiid@ybl or Bank: Account No, IFSC") },
                modifier = Modifier.fillMaxWidth().testTag("receiving_details_input"),
                singleLine = false,
                maxLines = 3
            )
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Admin Note",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Admin Note / এডমিন নোট",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Once we receive your USDT, we will send the INR amount directly to your selected payment method.",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "আপনার পাঠানো USDT পাওয়ার সাথে সাথেই আমরা আপনার দেওয়া পেমেন্ট মেথডে (UPI অথবা ব্যাংক অ্যাকাউন্ট) INR পাঠিয়ে দেব।",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, contentDescription = "Warning", tint = MaterialTheme.colorScheme.onErrorContainer)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Please ensure your payment details are 100% correct. We are not responsible for transfers sent to incorrect accounts.",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    if (usdtValue <= 0) {
                        Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (receivingDetails.isBlank()) {
                        Toast.makeText(context, "Please enter your UPI or bank details to receive funds", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (hasInsufficientBalance) {
                        Toast.makeText(context, "Insufficient USDT balance to complete this sale.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    currentUser?.let { user ->
                        mainViewModel.createSellOrder(
                            userId = user.id,
                            usdtAmount = usdtValue,
                            walletAddress = receivingDetails,
                            network = network,
                            paymentMethod = paymentMethod,
                            onSuccess = { orderId ->
                                onOrderPlaced(orderId)
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("confirm_sell_button"),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Text("Confirm Sell Order", fontWeight = FontWeight.Bold)
            }
        }
    }
}
