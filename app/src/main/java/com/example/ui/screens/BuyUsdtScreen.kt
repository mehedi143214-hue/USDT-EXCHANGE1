package com.example.ui.screens
import androidx.compose.foundation.verticalScroll

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyUsdtScreen(
    mainViewModel: MainViewModel,
    onBack: () -> Unit,
    onOrderPlaced: (Int) -> Unit
) {
    val currentUser by mainViewModel.currentUser.collectAsState()
    val rate by mainViewModel.exchangeRate.collectAsState()
    val feePercent by mainViewModel.serviceFee.collectAsState()

    var fiatAmount by remember { mutableStateOf("") }
    var walletAddress by remember { mutableStateOf("") }
    var network by remember { mutableStateOf("TRC20") }
    val networks = listOf("TRC20", "BEP20")
    val paymentMethod = "INR Balance"

    val fiatValue = fiatAmount.toDoubleOrNull() ?: 0.0
    val feeAmount = fiatValue * feePercent
    val usdtAmount = if (rate > 0) (fiatValue - feeAmount) / rate else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buy USDT") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
                .padding(16.dp)
        ) {
            val inrBalance = currentUser?.inrBalance ?: 0.0
            val hasInsufficientBalance = fiatValue > inrBalance

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Exchange Details", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text("Balance: ₹%.2f".format(inrBalance), color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = fiatAmount,
                onValueChange = { fiatAmount = it },
                label = { Text("Amount to Pay (INR)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = hasInsufficientBalance
            )
            if (hasInsufficientBalance) {
                Text(
                    text = "Insufficient INR balance. Please deposit funds.",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, start = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Current Rate:", color = MaterialTheme.colorScheme.onSurface)
                        Text("₹${rate} / USDT", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Service Fee (${feePercent * 100}%):", color = MaterialTheme.colorScheme.onSurface)
                        Text("₹%.2f".format(feeAmount), color = MaterialTheme.colorScheme.error)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("You Receive:", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text("%.2f USDT".format(usdtAmount), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Wallet Information", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Select Network", color = MaterialTheme.colorScheme.onBackground)
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
                value = walletAddress,
                onValueChange = { walletAddress = it },
                label = { Text("USDT Wallet Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, contentDescription = "Warning", tint = MaterialTheme.colorScheme.onErrorContainer)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Network: $network. Send USDT only to the selected network. Incorrect network will result in loss of funds.",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            val context = androidx.compose.ui.platform.LocalContext.current
            Button(
                onClick = {
                    if (fiatValue <= 0) {
                        android.widget.Toast.makeText(context, "Please enter a valid amount", android.widget.Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (walletAddress.isBlank()) {
                        android.widget.Toast.makeText(context, "Please enter your wallet address", android.widget.Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (hasInsufficientBalance) {
                        android.widget.Toast.makeText(context, "Insufficient INR balance. Please deposit funds first.", android.widget.Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    currentUser?.let { user ->
                        mainViewModel.createOrder(
                            userId = user.id,
                            fiatAmount = fiatValue,
                            walletAddress = walletAddress,
                            network = network,
                            paymentMethod = paymentMethod,
                            onSuccess = { orderId ->
                                onOrderPlaced(orderId)
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Confirm Order")
            }
        }
    }
}
