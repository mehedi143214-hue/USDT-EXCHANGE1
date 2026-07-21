package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositScreen(
    mainViewModel: MainViewModel,
    onBack: () -> Unit,
    onDepositSubmitted: () -> Unit
) {
    val currentUser by mainViewModel.currentUser.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    var amount by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("UPI") }
    var step by remember { mutableIntStateOf(1) }
    var utr by remember { mutableStateOf("") }
    var screenshotUploaded by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                imageUri = uri
                screenshotUploaded = true
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("INR Deposit", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (step == 2) step = 1 else onBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
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
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Step Progress Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepIndicatorItem(
                    stepNumber = 1,
                    title = "Enter Amount",
                    isActive = step >= 1,
                    isCompleted = step > 1
                )
                Divider(
                    modifier = Modifier.width(40.dp),
                    color = if (step > 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    thickness = 2.dp
                )
                StepIndicatorItem(
                    stepNumber = 2,
                    title = "Payment Proof",
                    isActive = step == 2,
                    isCompleted = false
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
            ) {
                if (step == 1) {
                    // STEP 1: ENTER AMOUNT
                    Text(
                        text = "Specify Deposit Amount",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = "Funds will be added to your INR wallet after verification. Minimum deposit is ₹1,000.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { input ->
                            // Allow digits only
                            if (input.all { it.isDigit() }) {
                                amount = input
                            }
                        },
                        label = { Text("Amount (INR)") },
                        placeholder = { Text("Enter amount in ₹") },
                        prefix = { Text("₹", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick select options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("1000", "5000", "10000", "25000").forEach { quickAmount ->
                            SuggestionChip(
                                onClick = { amount = quickAmount },
                                label = { Text("₹$quickAmount") },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    labelColor = if (amount == quickAmount) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (amount == quickAmount) 1.5.dp else 1.dp,
                                    color = if (amount == quickAmount) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Select Payment Method",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Modern card-based Payment Method selector
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { paymentMethod = "UPI" }
                            .border(
                                width = 1.5.dp,
                                color = if (paymentMethod == "UPI") MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (paymentMethod == "UPI") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_upi),
                                    contentDescription = "UPI",
                                    modifier = Modifier.width(64.dp).height(22.dp),
                                    tint = Color.Unspecified
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "UPI Transfer / Scanner",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Instant apps like PhonePe, GPay, Paytm",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                            RadioButton(
                                selected = paymentMethod == "UPI",
                                onClick = { paymentMethod = "UPI" }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Instruction Alert Box
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Info",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Please ensure the deposit amount entered here matches the actual payment made to prevent processing delays.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    val isStep1Valid = amount.toDoubleOrNull()?.let { it in 1000.0..49000.0 } ?: false
                    Button(
                        onClick = { step = 2 },
                        enabled = isStep1Valid,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Proceed to Pay ₹${amount.ifBlank { "0" }}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    if (!isStep1Valid && amount.isNotEmpty()) {
                        Text(
                            text = "Amount must be between ₹1,000 and ₹49,000",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // STEP 2: PAYMENT & PROOF SUBMISSION
                    Text(
                        text = "Transfer to UPI Account",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = "Scan the QR code or click on your preferred payment app below to pay ₹$amount.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // QR Card with Copy Action
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .background(Color.White, shape = RoundedCornerShape(12.dp))
                                    .padding(8.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.upi_qr),
                                    contentDescription = "UPI QR Code",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "UPI ID: kawsarali@upi",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString("kawsarali@upi"))
                                    Toast.makeText(context, "UPI ID copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy UPI ID", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Pay via Installed Apps",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val upiUri = "upi://pay?pa=kawsarali@upi&pn=Kawsar%20Ali&am=$amount&cu=INR"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // PhonePe App Option
                        PaymentAppButton(
                            weight = 1f,
                            iconId = R.drawable.ic_phonepe,
                            label = "PhonePe",
                            onClick = {
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(upiUri))
                                    intent.setPackage("com.phonepe.app")
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "PhonePe app not found", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        // GooglePay App Option
                        PaymentAppButton(
                            weight = 1f,
                            iconId = R.drawable.ic_gpay,
                            label = "GPay",
                            onClick = {
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(upiUri))
                                    intent.setPackage("com.google.android.apps.nbu.paisa.user")
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "GPay app not found", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        // Paytm App Option
                        PaymentAppButton(
                            weight = 1f,
                            iconId = R.drawable.ic_paytm,
                            label = "Paytm",
                            onClick = {
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(upiUri))
                                    intent.setPackage("net.one97.paytm")
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Paytm app not found", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Submit Transfer Details",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = utr,
                        onValueChange = { utr = it },
                        label = { Text("UTR / Transaction ID (12 Digits)") },
                        placeholder = { Text("Enter 12-digit UPI Transaction reference") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Elegant Dashed-Styled Upload Container or Selected Screenshot View
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .border(
                                width = 1.dp,
                                color = if (screenshotUploaded) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (screenshotUploaded) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (screenshotUploaded && imageUri != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.UploadFile,
                                            contentDescription = "Uploaded",
                                            tint = MaterialTheme.colorScheme.tertiary
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Screenshot selected successfully!",
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 13.sp
                                        )
                                    }
                                    TextButton(onClick = {
                                        imageUri = null
                                        screenshotUploaded = false
                                    }) {
                                        Text("Remove", color = MaterialTheme.colorScheme.error)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                coil.compose.AsyncImage(
                                    model = imageUri,
                                    contentDescription = "Uploaded Screenshot",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.UploadFile,
                                    contentDescription = "Upload",
                                    modifier = Modifier.size(36.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Upload Transaction Screenshot",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Supports JPEG, PNG file formats",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    val isStep2Valid = utr.isNotBlank() && screenshotUploaded
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { step = 1 },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Back", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val amountValue = amount.toDoubleOrNull() ?: 0.0
                                currentUser?.let { user ->
                                    mainViewModel.requestDeposit(context, user.id, amountValue, utr, imageUri) {
                                        Toast.makeText(context, "Deposit request submitted successfully!", Toast.LENGTH_LONG).show()
                                        onDepositSubmitted()
                                    }
                                }
                            },
                            enabled = isStep2Valid,
                            modifier = Modifier
                                .weight(2.5f)
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Confirm & Submit Proof", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepIndicatorItem(
    stepNumber: Int,
    title: String,
    isActive: Boolean,
    isCompleted: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    color = if (isCompleted) {
                        MaterialTheme.colorScheme.primary
                    } else if (isActive) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber.toString(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCompleted) {
                    MaterialTheme.colorScheme.onPrimary
                } else if (isActive) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                }
            )
        }
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) {
                MaterialTheme.colorScheme.onBackground
            } else {
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            }
        )
    }
}

@Composable
fun RowScope.PaymentAppButton(
    weight: Float,
    iconId: Int,
    label: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .weight(weight)
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        Image(
            painter = painterResource(id = iconId),
            contentDescription = label,
            modifier = Modifier.height(32.dp).widthIn(max = 100.dp)
        )
    }
}
