package com.example.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object Splash

@Serializable
object Landing

@Serializable
object Login

@Serializable
object Register

@Serializable
object Home

@Serializable
object BuyUsdt

@Serializable
object Deposit

@Serializable
data class OrderDetails(val orderId: Int)

@Serializable
object AdminDashboard

@Serializable
object Support

@Serializable
object Company

@Serializable
object PrivacyPolicy

@Serializable
object About

@Serializable
object TermsAndConditions

@Serializable
object LiveChat

@Serializable
object ReferAndTeam

@Serializable
object SellUsdt

