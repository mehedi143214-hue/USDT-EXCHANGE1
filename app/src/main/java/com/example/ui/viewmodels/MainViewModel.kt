package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.data.Order
import com.example.data.User
import com.example.data.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class MainViewModel(
    private val repository: AppRepository,
    private val userPrefs: UserPreferencesRepository
) : ViewModel() {

    private fun getDoubleSafe(doc: com.google.firebase.firestore.DocumentSnapshot, field: String): Double {
        val value = doc.get(field)
        return when (value) {
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
    }

    private fun getLongSafe(doc: com.google.firebase.firestore.DocumentSnapshot, field: String): Long {
        val value = doc.get(field)
        return when (value) {
            is Number -> value.toLong()
            is String -> value.toLongOrNull() ?: 0L
            else -> 0L
        }
    }

    init {
        syncWithFirestore()
    }

    private suspend fun syncUserToFirestore(user: User) {
        try {
            val db = FirebaseFirestore.getInstance()
            val userData = hashMapOf(
                "name" to user.name,
                "email" to user.email,
                "phone" to user.phone,
                "verificationStatus" to user.verificationStatus,
                "role" to user.role,
                "inrBalance" to user.inrBalance,
                "usdtBalance" to user.usdtBalance,
                "referralCode" to user.referralCode,
                "createdAt" to user.createdAt
            )
            db.collection("users").document(user.email).set(userData)
                .addOnSuccessListener {
                    Log.d("FirestoreSync", "User synced successfully: ${user.email}")
                }
                .addOnFailureListener { e ->
                    Log.e("FirestoreSync", "Failed to sync user: ${user.email}, error: ${e.message}", e)
                }
        } catch(e: Exception){
            Log.e("FirestoreSync", "Exception during user sync in MainViewModel: ${e.message}", e)
        }
    }

    private fun syncWithFirestore() {
        val db = FirebaseFirestore.getInstance()
        
        // Listen to Rates
        db.collection("settings").document("rates").addSnapshotListener { snapshot, error ->
            if (error != null) {
                _firestoreError.value = "Firestore Rates Link Failed: ${error.localizedMessage}. Check Firestore security rules!"
                Log.e("FirestoreSync", "Rates listener error", error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val buyValue = snapshot.get("buyRate")
                val sellValue = snapshot.get("sellRate")
                if (buyValue is Number) {
                    _buyExchangeRate.value = buyValue.toDouble()
                    _exchangeRate.value = buyValue.toDouble()
                }
                if (sellValue is Number) {
                    _sellExchangeRate.value = sellValue.toDouble()
                }
            }
        }

        // Listen to Users
        db.collection("users").addSnapshotListener { snapshot, error ->
            if (error != null) {
                _firestoreError.value = "Firestore Users Link Failed: ${error.localizedMessage}. Check Firestore security rules!"
                Log.e("FirestoreSync", "Users listener error", error)
                return@addSnapshotListener
            }
            viewModelScope.launch {
                snapshot?.documents?.forEach { doc ->
                    try {
                        val email = doc.getString("email") ?: return@forEach
                        val existingUser = repository.getUserByEmail(email)
                        
                        val user = User(
                            id = existingUser?.id ?: 0,
                            name = doc.getString("name") ?: "",
                            email = email,
                            phone = doc.getString("phone") ?: "",
                            passwordHash = existingUser?.passwordHash ?: "",
                            verificationStatus = doc.getString("verificationStatus") ?: "Pending",
                            role = doc.getString("role") ?: "User",
                            inrBalance = getDoubleSafe(doc, "inrBalance"),
                            usdtBalance = getDoubleSafe(doc, "usdtBalance"),
                            referralCode = doc.getString("referralCode"),
                            createdAt = getLongSafe(doc, "createdAt")
                        )
                        if (existingUser == null) {
                            repository.insertUser(user)
                        } else {
                            repository.updateUser(user)
                        }
                    } catch(e:Exception){}
                }
            }
        }
        
        // Listen to Orders
        db.collection("orders").addSnapshotListener { snapshot, error ->
            if (error != null) {
                _firestoreError.value = "Firestore Orders Link Failed: ${error.localizedMessage}. Check Firestore security rules!"
                Log.e("FirestoreSync", "Orders listener error", error)
                return@addSnapshotListener
            }
            viewModelScope.launch {
                snapshot?.documents?.forEach { doc ->
                    try {
                        val orderId = doc.getString("orderId") ?: return@forEach
                        val userId = getLongSafe(doc, "userId").toInt()
                        val fiatAmount = getDoubleSafe(doc, "fiatAmount")
                        val usdtAmount = getDoubleSafe(doc, "usdtAmount")
                        val exchangeRate = getDoubleSafe(doc, "exchangeRate")
                        val serviceFee = getDoubleSafe(doc, "serviceFee")
                        val walletAddress = doc.getString("walletAddress") ?: ""
                        val network = doc.getString("network") ?: ""
                        val paymentMethod = doc.getString("paymentMethod") ?: ""
                        val paymentTransactionId = doc.getString("paymentTransactionId")
                        val status = doc.getString("status") ?: "Processing"
                        val adminNote = doc.getString("adminNote")
                        val type = doc.getString("type") ?: "Buy"
                        val createdAt = getLongSafe(doc, "createdAt")
                        
                        val existingOrder = repository.getOrderByOrderId(orderId)
                        val order = Order(
                            id = existingOrder?.id ?: 0,
                            orderId = orderId,
                            userId = userId,
                            fiatAmount = fiatAmount,
                            usdtAmount = usdtAmount,
                            exchangeRate = exchangeRate,
                            serviceFee = serviceFee,
                            walletAddress = walletAddress,
                            network = network,
                            paymentMethod = paymentMethod,
                            paymentTransactionId = paymentTransactionId,
                            status = status,
                            adminNote = adminNote,
                            type = type,
                            createdAt = createdAt
                        )
                        if (existingOrder == null) {
                            repository.insertOrder(order)
                        } else {
                            repository.updateOrder(order)
                        }
                    } catch (e: Exception) { Log.e("MainViewModel", "Order sync error", e) }
                }
            }
        }
        
        // Listen to Deposits/Transactions
        db.collection("deposits").addSnapshotListener { snapshot, error ->
            if (error != null) {
                _firestoreError.value = "Firestore Deposits Link Failed: ${error.localizedMessage}. Check Firestore security rules!"
                Log.e("FirestoreSync", "Deposits listener error", error)
                return@addSnapshotListener
            }
            viewModelScope.launch {
                snapshot?.documents?.forEach { doc ->
                    try {
                        val transactionId = doc.getString("utr") ?: return@forEach
                        val userId = getLongSafe(doc, "userId").toInt()
                        val amount = getDoubleSafe(doc, "amount")
                        val currency = doc.getString("currency") ?: "INR"
                        val status = doc.getString("status") ?: "Under Review"
                        val type = doc.getString("type") ?: "Deposit"
                        
                        val existingTx = repository.getTransactionByTransactionId(transactionId)
                        val tx = com.example.data.Transaction(
                            id = existingTx?.id ?: 0,
                            transactionId = transactionId,
                            userId = userId,
                            orderId = null,
                            type = type,
                            amount = amount,
                            currency = currency,
                            status = status,
                            transactionHash = transactionId
                        )
                        if (existingTx == null) {
                            repository.insertTransaction(tx)
                        } else {
                            repository.updateTransaction(tx)
                        }
                    } catch(e:Exception){}
                }
            }
        }
        
        // Listen to Notifications
        db.collection("notifications")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _firestoreError.value = "Firestore Notifications Link Failed: ${error.localizedMessage}. Check Firestore security rules!"
                    Log.e("FirestoreSync", "Notifications listener error", error)
                    return@addSnapshotListener
                }
                val notifs = snapshot?.documents?.mapNotNull { doc ->
                    val msg = doc.getString("message") ?: return@mapNotNull null
                    com.example.data.AppNotification(
                        id = doc.id,
                        message = msg,
                        createdAt = getLongSafe(doc, "createdAt")
                    )
                }?.sortedByDescending { it.createdAt } ?: emptyList()
                _notifications.value = notifs
            }
    }
    
    private val _notifications = MutableStateFlow<List<com.example.data.AppNotification>>(emptyList())
    val notifications = _notifications.asStateFlow()

    val unreadNotificationsCount: StateFlow<Int> = combine(
        notifications,
        userPrefs.lastSeenNotificationTime
    ) { list, lastSeen ->
        list.count { it.createdAt > lastSeen }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    fun markNotificationsAsSeen() {
        viewModelScope.launch {
            try {
                userPrefs.saveLastSeenNotificationTime(System.currentTimeMillis())
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error marking notifications as seen", e)
            }
        }
    }

    private val _isMaintenanceMode = MutableStateFlow(false)
    val isMaintenanceMode = _isMaintenanceMode.asStateFlow()

    private val _firestoreError = MutableStateFlow<String?>(null)
    val firestoreError = _firestoreError.asStateFlow()

    fun clearFirestoreError() {
        _firestoreError.value = null
    }

    init {
        val db = FirebaseFirestore.getInstance()
        db.collection("settings").document("maintenance").addSnapshotListener { snapshot, error ->
            if (error != null) {
                _firestoreError.value = "Firestore Maintenance Link Failed: ${error.localizedMessage}. Check rules!"
                Log.e("FirestoreSync", "Maintenance listener error", error)
                return@addSnapshotListener
            }
            val isMaintenance = snapshot?.getBoolean("isMaintenance") ?: false
            _isMaintenanceMode.value = isMaintenance
        }
    }

    fun updateMaintenanceMode(isMaintenance: Boolean) {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("settings").document("maintenance").set(
                    hashMapOf("isMaintenance" to isMaintenance)
                ).addOnSuccessListener {
                    Log.d("FirestoreSync", "Maintenance mode updated successfully to: $isMaintenance")
                }.addOnFailureListener { e ->
                    Log.e("FirestoreSync", "Failed to update maintenance mode: ${e.message}", e)
                    _firestoreError.value = "Failed to update maintenance: ${e.localizedMessage}"
                }
            } catch (e: Exception) {
                Log.e("FirestoreSync", "Exception updating maintenance mode: ${e.message}", e)
                _firestoreError.value = "Failed to update maintenance: ${e.localizedMessage}"
            }
        }
    }

    fun sendNotification(message: String) {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                val data = hashMapOf(
                    "message" to message,
                    "createdAt" to System.currentTimeMillis()
                )
                db.collection("notifications").add(data)
                    .addOnSuccessListener {
                        Log.d("FirestoreSync", "Notification sent successfully: $message")
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirestoreSync", "Failed to send notification: ${e.message}", e)
                        _firestoreError.value = "Failed to send notification: ${e.localizedMessage}"
                    }
            } catch (e: Exception) {
                Log.e("FirestoreSync", "Exception sending notification: ${e.message}", e)
                _firestoreError.value = "Failed to send notification: ${e.localizedMessage}"
            }
        }
    }

    private val _exchangeRate = MutableStateFlow(98.00) // 1 USDT = 98.00 INR
    val exchangeRate = _exchangeRate.asStateFlow()

    private val _buyExchangeRate = MutableStateFlow(98.00)
    val buyExchangeRate = _buyExchangeRate.asStateFlow()

    private val _sellExchangeRate = MutableStateFlow(97.00)
    val sellExchangeRate = _sellExchangeRate.asStateFlow()
    
    private val _serviceFee = MutableStateFlow(0.01) // 1%
    val serviceFee = _serviceFee.asStateFlow()

    // Current User State
    val currentUser = userPrefs.loggedInUserId.flatMapLatest { userId ->
        if (userId != null) {
            repository.getUserById(userId)
        } else {
            flowOf(null)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // User Orders State
    val currentUserOrders = userPrefs.loggedInUserId.flatMapLatest { userId ->
        if (userId != null) {
            repository.getOrdersByUser(userId)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // User Transactions State
    val currentUserTransactions = userPrefs.loggedInUserId.flatMapLatest { userId ->
        if (userId != null) {
            repository.getTransactionsByUser(userId)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // User Referrals (Team) State
    val currentUserReferrals = currentUser.flatMapLatest { user ->
        if (user != null) {
            repository.getReferrals(
                phone = user.phone,
                email = user.email,
                userIdStr = user.id.toString()
            )
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val isDarkMode = userPrefs.isDarkMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    fun setDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            userPrefs.setDarkMode(isDark)
        }
    }

    // Admin State
    val allOrders = repository.getAllOrders().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allTransactions = repository.getAllTransactions().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allUsers = repository.getAllUsers().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // User Actions
    fun createOrder(userId: Int, fiatAmount: Double, walletAddress: String, network: String, paymentMethod: String, onSuccess: (Int) -> Unit) {
        viewModelScope.launch {
            val rate = _exchangeRate.value
            val fee = fiatAmount * _serviceFee.value
            val usdt = (fiatAmount - fee) / rate
            
            val order = Order(
                orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).uppercase(),
                userId = userId,
                fiatAmount = fiatAmount,
                usdtAmount = usdt,
                exchangeRate = rate,
                serviceFee = fee,
                walletAddress = walletAddress,
                network = network,
                paymentMethod = paymentMethod,
                status = "Processing",
                type = "Buy"
            )
            val id = repository.insertOrder(order)
            
            try {
                val db = FirebaseFirestore.getInstance()
                val orderData = hashMapOf(
                    "orderId" to order.orderId,
                    "userId" to order.userId,
                    "fiatAmount" to order.fiatAmount,
                    "usdtAmount" to order.usdtAmount,
                    "exchangeRate" to order.exchangeRate,
                    "serviceFee" to order.serviceFee,
                    "walletAddress" to order.walletAddress,
                    "network" to order.network,
                    "paymentMethod" to order.paymentMethod,
                    "status" to order.status,
                    "type" to order.type,
                    "createdAt" to order.createdAt
                )
                db.collection("orders").document(order.orderId).set(orderData)
            } catch(e: Exception){}
            
            val currentUserData = currentUser.value
            if (currentUserData != null && currentUserData.id == userId) {
                val updatedUser = currentUserData.copy(inrBalance = currentUserData.inrBalance - fiatAmount)
                repository.updateUser(updatedUser)
                syncUserToFirestore(updatedUser)
            }
            
            onSuccess(id.toInt())
        }
    }

    fun createSellOrder(userId: Int, usdtAmount: Double, walletAddress: String, network: String, paymentMethod: String, onSuccess: (Int) -> Unit) {
        viewModelScope.launch {
            val rate = _exchangeRate.value
            val fiatAmount = usdtAmount * rate
            val fee = fiatAmount * _serviceFee.value
            val finalInrAmount = fiatAmount - fee
            
            val order = Order(
                orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).uppercase(),
                userId = userId,
                fiatAmount = finalInrAmount,
                usdtAmount = usdtAmount,
                exchangeRate = rate,
                serviceFee = fee,
                walletAddress = walletAddress,
                network = network,
                paymentMethod = paymentMethod,
                status = "Processing",
                type = "Sell"
            )
            val id = repository.insertOrder(order)
            
            try {
                val db = FirebaseFirestore.getInstance()
                val orderData = hashMapOf(
                    "orderId" to order.orderId,
                    "userId" to order.userId,
                    "fiatAmount" to order.fiatAmount,
                    "usdtAmount" to order.usdtAmount,
                    "exchangeRate" to order.exchangeRate,
                    "serviceFee" to order.serviceFee,
                    "walletAddress" to order.walletAddress,
                    "network" to order.network,
                    "paymentMethod" to order.paymentMethod,
                    "status" to order.status,
                    "type" to order.type,
                    "createdAt" to order.createdAt
                )
                db.collection("orders").document(order.orderId).set(orderData)
            } catch(e: Exception){}
            
            val currentUserData = currentUser.value
            if (currentUserData != null && currentUserData.id == userId) {
                val updatedUser = currentUserData.copy(usdtBalance = currentUserData.usdtBalance - usdtAmount)
                repository.updateUser(updatedUser)
                syncUserToFirestore(updatedUser)
            }
            
            onSuccess(id.toInt())
        }
    }

    fun submitPaymentProof(order: Order, transactionId: String) {
        viewModelScope.launch {
            val updatedOrder = order.copy(
                status = "Under Review",
                paymentTransactionId = transactionId,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateOrder(updatedOrder)
            try {
                FirebaseFirestore.getInstance().collection("orders").document(order.orderId)
                    .update("status", "Under Review", "paymentTransactionId", transactionId)
            } catch(e: Exception){}
        }
    }

    private fun getBase64FromUri(context: Context, uri: Uri): String? {
        return try {
            val contentResolver = context.contentResolver
            
            // 1. Decode bounds to find original dimensions
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            // 2. Calculate sample size to downscale the image to a reasonable size (e.g., max 1024px)
            val maxDimension = 1024
            var scale = 1
            if (options.outWidth > maxDimension || options.outHeight > maxDimension) {
                val halfWidth = options.outWidth / 2
                val halfHeight = options.outHeight / 2
                while ((halfWidth / scale) >= maxDimension && (halfHeight / scale) >= maxDimension) {
                    scale *= 2
                }
            }

            // 3. Decode bitmap with inSampleSize
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = scale
            }
            val inputStream2 = contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream2, null, decodeOptions)
            inputStream2.close()

            if (bitmap != null) {
                // 4. Scale down precisely if needed
                val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                    val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
                    val (newWidth, newHeight) = if (ratio > 1) {
                        Pair(maxDimension, (maxDimension / ratio).toInt())
                    } else {
                        Pair((maxDimension * ratio).toInt(), maxDimension)
                    }
                    Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
                } else {
                    bitmap
                }

                // 5. Compress to JPEG at 60% quality (usually results in 40KB - 120KB, well under Firestore's 1MB limit)
                val outputStream = java.io.ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
                val compressedBytes = outputStream.toByteArray()
                outputStream.close()

                // Recycle bitmaps to avoid memory leaks
                if (scaledBitmap != bitmap) {
                    scaledBitmap.recycle()
                }
                bitmap.recycle()

                Base64.encodeToString(compressedBytes, Base64.DEFAULT)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("MainViewModel", "Error converting Uri to Base64 with compression", e)
            null
        }
    }

    fun requestDeposit(context: Context, userId: Int, amount: Double, utr: String, imageUri: Uri?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val screenshotBase64 = imageUri?.let { getBase64FromUri(context, it) } ?: ""

            // 1. Insert into local Room database
            val tx = com.example.data.Transaction(
                transactionId = utr,
                userId = userId,
                orderId = null,
                type = "Deposit",
                amount = amount,
                currency = "INR",
                status = "Under Review",
                transactionHash = utr
            )
            val localId = repository.insertTransaction(tx)

            // 2. Save to Cloud Firestore
            try {
                val db = FirebaseFirestore.getInstance()
                val depositData = hashMapOf(
                    "localId" to localId,
                    "userId" to userId,
                    "amount" to amount,
                    "currency" to "INR",
                    "utr" to utr,
                    "type" to "Deposit",
                    "screenshotBase64" to screenshotBase64,
                    "status" to "Under Review",
                    "createdAt" to System.currentTimeMillis()
                )
                db.collection("deposits")
                    .document("DEP_$utr")
                    .set(depositData)
                    .await()
                
                Log.d("MainViewModel", "Deposit with UTR $utr saved successfully to Firestore")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to save UTR $utr and screenshot to Firestore", e)
            }

            onSuccess()
        }
    }

    // Admin Actions
    fun updateDepositStatus(tx: com.example.data.Transaction, newStatus: String) {
        viewModelScope.launch {
            repository.updateTransaction(tx.copy(status = newStatus))
            try {
                FirebaseFirestore.getInstance().collection("deposits").document("DEP_${tx.transactionHash}")
                    .update("status", newStatus)
            } catch(e: Exception){}
            
            if (newStatus == "Completed") {
                val user = repository.getUserById(tx.userId).firstOrNull()
                if (user != null) {
                    val updatedUser = user.copy(inrBalance = user.inrBalance + tx.amount)
                    repository.updateUser(updatedUser)
                    syncUserToFirestore(updatedUser)
                }
            }
        }
    }

    fun updateOrderStatus(order: Order, newStatus: String, adminNote: String? = null) {
        viewModelScope.launch {
            val updatedAdminNote = adminNote ?: order.adminNote
            repository.updateOrder(order.copy(
                status = newStatus,
                adminNote = updatedAdminNote,
                updatedAt = System.currentTimeMillis()
            ))
            
            try {
                FirebaseFirestore.getInstance().collection("orders").document(order.orderId)
                    .update("status", newStatus, "adminNote", updatedAdminNote)
            } catch(e: Exception){}
            
            val user = repository.getUserById(order.userId).firstOrNull()
            if (user != null) {
                if (newStatus == "Completed") {
                    val updatedUser = user.copy(usdtBalance = user.usdtBalance + order.usdtAmount)
                    repository.updateUser(updatedUser)
                    syncUserToFirestore(updatedUser)

                    // Create transaction history
                    val tx = com.example.data.Transaction(
                        transactionId = UUID.randomUUID().toString(),
                        userId = order.userId,
                        orderId = order.id,
                        type = "Buy",
                        amount = order.usdtAmount,
                        currency = "USDT",
                        status = "Completed",
                        transactionHash = order.orderId
                    )
                    repository.insertTransaction(tx)
                } else if (newStatus == "Rejected") {
                    // Refund INR balance since it was deducted when placing the order
                    val updatedUser = user.copy(inrBalance = user.inrBalance + order.fiatAmount)
                    repository.updateUser(updatedUser)
                    syncUserToFirestore(updatedUser)

                    // Create transaction history
                    val tx = com.example.data.Transaction(
                        transactionId = UUID.randomUUID().toString(),
                        userId = order.userId,
                        orderId = order.id,
                        type = "Buy",
                        amount = order.usdtAmount,
                        currency = "USDT",
                        status = "Rejected",
                        transactionHash = order.orderId
                    )
                    repository.insertTransaction(tx)
                }
            }
        }
    }
    
    fun updateExchangeRate(newRate: Double) {
        updateRates(newRate, newRate - 1.0)
    }

    fun updateRates(buyRate: Double, sellRate: Double) {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                val data = hashMapOf(
                    "buyRate" to buyRate,
                    "sellRate" to sellRate
                )
                db.collection("settings").document("rates").set(data).await()
                _buyExchangeRate.value = buyRate
                _exchangeRate.value = buyRate
                _sellExchangeRate.value = sellRate
                Log.d("FirestoreSync", "Rates updated successfully: buy=$buyRate, sell=$sellRate")
            } catch (e: Exception) {
                Log.e("FirestoreSync", "Failed to update rates: ${e.message}", e)
                _firestoreError.value = "Failed to update rates: ${e.localizedMessage}"
            }
        }
    }

    fun updateUserRole(user: User, newRole: String) {
        viewModelScope.launch {
            val updatedUser = user.copy(role = newRole)
            repository.updateUser(updatedUser)
            syncUserToFirestore(updatedUser)
        }
    }
}
