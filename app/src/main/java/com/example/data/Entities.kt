package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

data class AppNotification(
    val id: String,
    val message: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val phone: String,
    val passwordHash: String,
    val verificationStatus: String = "Pending", // Pending, Verified, Rejected
    val role: String = "User", // User, Admin
    val inrBalance: Double = 0.0,
    val usdtBalance: Double = 0.0,
    val referralCode: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderId: String,
    val userId: Int,
    val fiatAmount: Double,
    val usdtAmount: Double,
    val exchangeRate: Double,
    val serviceFee: Double,
    val walletAddress: String,
    val network: String,
    val paymentMethod: String,
    val paymentTransactionId: String? = null,
    val status: String = "Pending Payment", // Pending Payment, Under Review, Approved, Completed, Rejected
    val adminNote: String? = null,
    val type: String = "Buy", // Buy or Sell
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val transactionId: String, // Added this
    val userId: Int,
    val orderId: Int?,
    val type: String, // Buy, Sell, Deposit, Withdrawal
    val amount: Double,
    val currency: String,
    val status: String,
    val transactionHash: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
