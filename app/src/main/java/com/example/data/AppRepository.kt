package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val userDao: UserDao,
    private val orderDao: OrderDao,
    private val transactionDao: TransactionDao
) {
    suspend fun getUserByEmail(email: String) = userDao.getUserByEmail(email)
    fun getUserById(id: Int) = userDao.getUserById(id)
    fun getAllUsers() = userDao.getAllUsers()
    fun getReferrals(phone: String, email: String, userIdStr: String) = userDao.getReferrals(phone, email, userIdStr)
    suspend fun insertUser(user: User) = userDao.insertUser(user)
    suspend fun updateUser(user: User) = userDao.updateUser(user)

    fun getOrdersByUser(userId: Int) = orderDao.getOrdersByUser(userId)
    fun getAllOrders() = orderDao.getAllOrders()
    fun getOrderById(id: Int) = orderDao.getOrderById(id)
    suspend fun getOrderByOrderId(orderId: String) = orderDao.getOrderByOrderId(orderId)
    suspend fun insertOrder(order: Order) = orderDao.insertOrder(order)
    suspend fun updateOrder(order: Order) = orderDao.updateOrder(order)

    fun getTransactionsByUser(userId: Int) = transactionDao.getTransactionsByUser(userId)
    fun getAllTransactions() = transactionDao.getAllTransactions()
    suspend fun getTransactionByTransactionId(transactionId: String) = transactionDao.getTransactionByTransactionId(transactionId)
    suspend fun insertTransaction(transaction: Transaction) = transactionDao.insertTransaction(transaction)
    suspend fun updateTransaction(transaction: Transaction) = transactionDao.updateTransaction(transaction)
}
