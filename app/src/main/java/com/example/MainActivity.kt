package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.UserPreferencesRepository
import com.example.data.dataStore
import com.example.ui.navigation.AdminDashboard
import com.example.ui.navigation.BuyUsdt
import com.example.ui.navigation.SellUsdt
import com.example.ui.navigation.Deposit
import com.example.ui.navigation.Home
import com.example.ui.navigation.Login
import com.example.ui.navigation.OrderDetails
import com.example.ui.navigation.Register
import com.example.ui.navigation.Splash
import com.example.ui.navigation.Support
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.BuyUsdtScreen
import com.example.ui.screens.SellUsdtScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.OrderDetailsScreen
import com.example.ui.screens.RegisterScreen
import com.example.ui.screens.LandingScreen
import com.example.ui.screens.SupportScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodels.AuthViewModel
import com.example.ui.viewmodels.MainViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApiKey(BuildConfig.FIREBASE_API_KEY)
                    .setApplicationId(BuildConfig.FIREBASE_APP_ID)
                    .setProjectId(BuildConfig.FIREBASE_PROJECT_ID)
                    .build()
                FirebaseApp.initializeApp(this, options)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val database = AppDatabase.getDatabase(this)
        val repository = AppRepository(database.userDao(), database.orderDao(), database.transactionDao())
        val userPrefs = UserPreferencesRepository(dataStore)
        val factory = AppViewModelFactory(repository, userPrefs)

        setContent {
            val mainViewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
            val isDarkMode by mainViewModel.isDarkMode.collectAsState()
            
            MyApplicationTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(factory = factory, mainViewModel = mainViewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(factory: AppViewModelFactory, mainViewModel: MainViewModel) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(factory = factory)

    val loggedInUserId by authViewModel.loggedInUserId.collectAsState()
    
    val isMaintenanceMode by mainViewModel.isMaintenanceMode.collectAsState()
    val currentUser by mainViewModel.currentUser.collectAsState()

    if (isMaintenanceMode && loggedInUserId != null && currentUser != null && currentUser?.role != "Admin") {
        com.example.ui.screens.MaintenanceScreen()
        return
    }

    NavHost(navController = navController, startDestination = Splash) {
        composable<Splash> {
            val isSessionLoaded by authViewModel.isSessionLoaded.collectAsState()
            LaunchedEffect(isSessionLoaded, loggedInUserId) {
                if (isSessionLoaded && loggedInUserId != null) {
                    navController.navigate(Home) {
                        popUpTo(Splash) { inclusive = true }
                    }
                }
            }
            
            if (!isSessionLoaded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(androidx.compose.ui.graphics.Color(0xFF070B14)),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.CurrencyExchange,
                            contentDescription = "Loading Logo",
                            tint = androidx.compose.ui.graphics.Color(0xFF00E676),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        CircularProgressIndicator(color = androidx.compose.ui.graphics.Color(0xFF00E676))
                    }
                }
            } else {
                LandingScreen(
                    onNavigateToLogin = { navController.navigate(Login) },
                    onNavigateToRegister = { navController.navigate(Register) },
                    onNavigateToSupport = { navController.navigate(Support) },
                    onNavigateToAbout = { navController.navigate(com.example.ui.navigation.About) },
                    onNavigateToPrivacy = { navController.navigate(com.example.ui.navigation.PrivacyPolicy) },
                    onNavigateToHome = {
                        if (loggedInUserId != null) {
                            navController.navigate(Home) {
                                popUpTo(0) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Login) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                )
            }
        }
        composable<Login> {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate(Register) },
                onLoginSuccess = {
                    navController.navigate(Home) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable<Register> {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { navController.navigate(Login) },
                onRegisterSuccess = {
                    navController.navigate(Home) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable<Home> {
            HomeScreen(
                mainViewModel = mainViewModel,
                authViewModel = authViewModel,
                onNavigateToBuy = { navController.navigate(BuyUsdt) },
                onNavigateToSell = { navController.navigate(SellUsdt) },
                onNavigateToDeposit = { navController.navigate(Deposit) },
                onNavigateToOrderDetails = { orderId -> navController.navigate(OrderDetails(orderId)) },
                onNavigateToAdmin = { navController.navigate(AdminDashboard) },
                onNavigateToSupport = { navController.navigate(Support) },
                onNavigateToCompany = { navController.navigate(com.example.ui.navigation.Company) },
                onNavigateToPrivacy = { navController.navigate(com.example.ui.navigation.PrivacyPolicy) },
                onNavigateToTerms = { navController.navigate(com.example.ui.navigation.TermsAndConditions) },
                onNavigateToAbout = { navController.navigate(com.example.ui.navigation.About) },
                onNavigateToRefer = { navController.navigate(com.example.ui.navigation.ReferAndTeam) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Login) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable<com.example.ui.navigation.ReferAndTeam> {
            com.example.ui.screens.ReferAndTeamScreen(
                mainViewModel = mainViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable<Deposit> {
            com.example.ui.screens.DepositScreen(
                mainViewModel = mainViewModel,
                onBack = { navController.popBackStack() },
                onDepositSubmitted = { navController.popBackStack() }
            )
        }
        composable<Support> {
            SupportScreen(onBack = { navController.popBackStack() }, onNavigateToLiveChat = { navController.navigate(com.example.ui.navigation.LiveChat) })
        }
        composable<com.example.ui.navigation.LiveChat> {
            com.example.ui.screens.LiveChatScreen(onBack = { navController.popBackStack() })
        }
        composable<com.example.ui.navigation.Company> {
            com.example.ui.screens.InfoScreen(
                title = "Company",
                content = "USDT Exchange Inc.\nWe provide secure, fast, and reliable crypto transactions. Our mission is to make cryptocurrency accessible to everyone seamlessly.",
                onBack = { navController.popBackStack() }
            )
        }
        composable<com.example.ui.navigation.PrivacyPolicy> {
            val privacyPolicyText = """
                PRIVACY POLICY

                Last Updated: 17 July 2026

                This Privacy Policy explains how [Your Company Legal Name] (“Company”, “we”, “us”, or “our”) collects, uses, stores, protects, and discloses information when you access or use our website, mobile application, and digital asset-related services (collectively, the “Platform”).

                Our Platform may provide services related to the purchase, sale, transfer, or other transactions involving digital assets such as USDT, subject to applicable laws, regulations, and compliance requirements.

                By accessing or using the Platform, you acknowledge that you have read and understood this Privacy Policy.

                1. COMPANY INFORMATION

                Legal Name: USDT EXCHANGE 
                Brand Name: USDT EXCHANGE 
                Registered Office: Bengaluru, Karnataka, India
                Email: privacy@usdtexchange.com
                Customer Support: support@usdtexchange.com

                2. INFORMATION WE COLLECT

                We may collect personal information including your full name, email address, mobile phone number, date of birth where required, address details, government-issued identification documents, KYC information, and identity verification photographs where applicable.

                We may also collect transaction-related information including fiat currency transaction details, USDT purchase details, wallet addresses, blockchain network information, transaction IDs or hashes, payment references, payment proof, order details, and transaction history.

                We may automatically collect technical information such as IP address, device information, browser type, operating system, app version, login information, and usage or diagnostic data.

                3. HOW WE USE YOUR INFORMATION

                We may use your information to create and manage your account, process and manage transactions, verify payments, conduct KYC and identity verification, perform fraud prevention and risk assessments, comply with applicable laws and regulations, monitor suspicious activity, provide customer support, send transaction and security notifications, improve our Platform, and protect the security and integrity of our systems.

                4. KYC, AML AND COMPLIANCE

                Where required by applicable law or our internal compliance procedures, we may request information and documents for identity verification, risk assessment, fraud prevention, and anti-money laundering compliance.

                We reserve the right to delay, restrict, suspend, reject, or cancel a transaction where we reasonably believe that the transaction may violate applicable law, appears suspicious or fraudulent, requires additional verification, or presents unacceptable compliance or security risks.

                5. SHARING YOUR INFORMATION

                We may share your information with third-party service providers, identity verification services, payment processors, banking partners, legal and regulatory authorities, law enforcement agencies, or in connection with a corporate transaction (such as a merger or sale of assets).

                6. DATA SECURITY

                We implement reasonable technical, administrative, and physical security measures designed to protect your information. However, no data transmission or storage system can be guaranteed to be 100% secure.

                7. YOUR RIGHTS

                Depending on your location and applicable law, you may have rights regarding your personal information, including the right to access, correct, delete, or restrict the processing of your data.

                8. CONTACT US

                If you have questions about this Privacy Policy, please contact us at privacy@usdtexchange.com.
            """.trimIndent()
            
            com.example.ui.screens.InfoScreen(
                title = "Privacy Policy",
                content = privacyPolicyText,
                onBack = { navController.popBackStack() }
            )
        }
        composable<com.example.ui.navigation.About> {
            val aboutText = """
                ABOUT US

                Welcome to USDT EXCHANGE — a modern and user-friendly platform designed to make USDT purchasing simple, convenient, and transparent.

                Our goal is to provide users with a smooth digital asset exchange experience through a simple interface, clear transaction details, reliable order processing, and responsive customer support.

                At USDT EXCHANGE, we focus on:

                • Simple and easy-to-use platform
                • Transparent exchange rates and transaction details
                • Secure account and transaction processes
                • Support for selected blockchain networks
                • Convenient USDT purchasing experience
                • Reliable customer support

                We continuously work to improve our platform and provide a better experience for our users.

                Please use our services responsibly and ensure that your use of digital asset services complies with all applicable laws and regulations.

                Thank you for choosing USDT EXCHANGE.

                © 2026 USDT EXCHANGE. All rights reserved.
            """.trimIndent()
            
            com.example.ui.screens.InfoScreen(
                title = "About",
                content = aboutText,
                onBack = { navController.popBackStack() }
            )
        }
        composable<com.example.ui.navigation.TermsAndConditions> {
            val termsText = """
                TERMS AND CONDITIONS

                Welcome to USDT EXCHANGE. By accessing and using our platform, you agree to these terms:

                1. ACCOUNT REGISTRATION
                You must be at least 18 years old to create an account. You agree to provide accurate information and complete any required KYC verification.

                2. TRANSACTIONS
                All transactions are final once executed on the blockchain. You are responsible for providing the correct USDT wallet address. We are not liable for funds sent to an incorrect address provided by you.

                3. COMPLIANCE
                You agree to comply with all applicable local laws regarding cryptocurrency exchange and taxation. 

                4. ACCOUNT SUSPENSION
                We reserve the right to suspend or terminate accounts involved in suspicious, fraudulent, or illegal activities without prior notice.

                5. LIMITATION OF LIABILITY
                USDT EXCHANGE is not liable for any losses arising from market volatility, blockchain network congestion, or unforeseen technical failures.

                By continuing to use this service, you accept these terms in full.
            """.trimIndent()
            
            com.example.ui.screens.InfoScreen(
                title = "Terms & Conditions",
                content = termsText,
                onBack = { navController.popBackStack() }
            )
        }
        composable<BuyUsdt> {
            BuyUsdtScreen(
                mainViewModel = mainViewModel,
                onBack = { navController.popBackStack() },
                onOrderPlaced = { orderId ->
                    navController.navigate(OrderDetails(orderId)) {
                        popUpTo(Home)
                    }
                }
            )
        }
        composable<com.example.ui.navigation.SellUsdt> {
            com.example.ui.screens.SellUsdtScreen(
                mainViewModel = mainViewModel,
                onBack = { navController.popBackStack() },
                onOrderPlaced = { orderId ->
                    navController.navigate(OrderDetails(orderId)) {
                        popUpTo(Home)
                    }
                }
            )
        }
        composable<OrderDetails> { backStackEntry ->
            val orderId = backStackEntry.toRoute<OrderDetails>().orderId
            OrderDetailsScreen(
                orderId = orderId,
                mainViewModel = mainViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable<AdminDashboard> {
            AdminDashboardScreen(
                mainViewModel = mainViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
