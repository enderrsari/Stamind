package com.stamindapp.stamind

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.stamindapp.stamind.auth.AuthManager
import com.stamindapp.stamind.auth.SubscriptionManager
import com.stamindapp.stamind.model.HomeViewModel
import com.stamindapp.stamind.model.HomeViewModelFactory
import com.stamindapp.stamind.model.JournalViewModel
import com.stamindapp.stamind.model.JournalViewModelFactory
import com.stamindapp.stamind.screens.DailyResultActivity
import com.stamindapp.stamind.screens.HomeActivity
import com.stamindapp.stamind.screens.JournalActivity
import com.stamindapp.stamind.screens.LoginScreen
import com.stamindapp.stamind.screens.OfferScreen
import com.stamindapp.stamind.screens.OnboardingFlowScreen
import com.stamindapp.stamind.screens.ProfileScreen
import com.stamindapp.stamind.screens.ReportsActivity
import com.stamindapp.stamind.screens.WelcomeScreen
import com.stamindapp.stamind.ui.theme.LexendTypography
import com.stamindapp.stamind.ui.theme.StamindColors
import com.stamindapp.stamind.ui.theme.StamindTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            StamindTheme {
                StamindApp()
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

@Composable
fun StamindApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val bottomNavItems = listOf(
        BottomNavItem("home", Icons.Filled.Home, "Ana Sayfa"),
        BottomNavItem("journal", Icons.Filled.Article, "Günlük"),
        BottomNavItem("reports", Icons.Filled.BarChart, "Raporlar"),
        BottomNavItem("profile", Icons.Filled.Person, "Profil")
    )
    val bottomRoutes = bottomNavItems.map { it.route }

    // ViewModels
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(context)
    )

    val journalViewModel: JournalViewModel = viewModel(
        factory = JournalViewModelFactory(context)
    )

    // Managers
    val authManager = remember { AuthManager(context) }
    val subscriptionManager = remember { SubscriptionManager(context) }

    // State - Kullanıcı giriş yapmışsa home, yapmamışsa welcome ekranına git
    val startDestination = if (authManager.getCurrentUser() != null) "home" else "welcome"

    // Coroutine Scope for async operations
    val scope = rememberCoroutineScope()

    // Premium upsell navigation
    val onPremiumUpsellClick = {
        navController.navigate("offer")
    }

    // Google Sign-In
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            val idToken = account.idToken!!
            scope.launch {
                val signInResult = authManager.signInWithGoogle(idToken)
                if (signInResult.isSuccess) {
                    val (user, isNewUser) = signInResult.getOrThrow()
                    Toast.makeText(context, "Başarıyla giriş yapıldı.", Toast.LENGTH_SHORT).show()

                    if (isNewUser) {
                        // Yeni kullanıcı → Onboarding'e yönlendir
                        navController.navigate("onboarding") {
                            popUpTo("welcome") { inclusive = true }
                        }
                    } else {
                        // Mevcut kullanıcı → Ana sayfaya yönlendir
                        navController.navigate("home") {
                            popUpTo("welcome") { inclusive = true }
                        }
                    }
                } else {
                    Toast.makeText(context, "Google ile giriş başarısız oldu.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        } catch (e: ApiException) {
            Toast.makeText(context, "Google ile giriş hatası: ${e.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }



    Scaffold(
        containerColor = Color.Transparent, // Scaffold arka planı şeffaf
        bottomBar = {
            if (currentDestination?.route in bottomRoutes) {
                FloatingBottomBar(
                    items = bottomNavItems,
                    currentRoute = currentDestination?.route,
                    onItemClick = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id)
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {


            composable("home") {
                HomeActivity(
                    viewModel = homeViewModel,
                    onNavigateToJournal = { navController.navigate("journal") },
                    onNavigateToAnalysis = { navController.navigate("reports") },
                    onNavigateToSettings = { navController.navigate("profile") },
                    onNavigateToOffer = onPremiumUpsellClick
                )
            }
            composable("journal") {
                JournalActivity(
                    navController = navController,
                    viewModel = journalViewModel
                )
            }

            composable("reports") {
                ReportsActivity(
                    viewModel = journalViewModel,
                    navController = navController
                )
            }

            composable("dailyResult") {
                DailyResultActivity(
                    navController = navController,
                    viewModel = journalViewModel,
                    onNavigateToOffer = onPremiumUpsellClick
                )
            }
            composable("offer") {
                OfferScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable("welcome") {
                WelcomeScreen(
                    onGetStartedClick = {
                        navController.navigate("onboarding")
                    },
                    onLoginClick = {
                        navController.navigate("login")
                    }
                )
            }

            composable("onboarding") {
                OnboardingFlowScreen(
                    onComplete = { userData ->
                        // TODO: userData'yı kaydet
                        navController.navigate("login") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                )
            }

            composable("login") {
                LoginScreen(
                    onGoogleSignInClick = {
                        googleSignInLauncher.launch(googleSignInClient.signInIntent)
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable("profile") {
                ProfileScreen(
                    authManager = authManager,
                    subscriptionManager = subscriptionManager,
                    journalViewModel = journalViewModel,
                    onSignInClick = { navController.navigate("welcome") },
                    onSignOut = {
                        scope.launch {
                            // Also sign out from Google to allow account switching
                            googleSignInClient.signOut().await()
                            authManager.signOut()

                            // After signing out, navigate to welcome and clear the history
                            navController.navigate("welcome") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                )
            }
        }
    }
}

/**
 * Full-Width Bottom Bar with minimal height
 * Selected item shows icon + label below, unselected shows only icon
 */
@Composable
fun FloatingBottomBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onItemClick: (String) -> Unit
) {
    val selectedColor = StamindColors.Green500
    val unselectedColor = Color(0xFF9CA3AF) // Gray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route

            Column(
                modifier = Modifier
                    .clickable { onItemClick(item.route) }
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = if (isSelected) selectedColor else unselectedColor,
                    modifier = Modifier.size(24.dp)
                )

                // Show label only for selected item
                if (isSelected) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.label,
                        color = selectedColor,
                        style = LexendTypography.Bold9
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun FloatingBottomBarPreview() {
    val sampleItems = listOf(
        BottomNavItem("home", Icons.Filled.Home, "Ana Sayfa"),
        BottomNavItem("journal", Icons.Filled.Article, "Günlük"),
        BottomNavItem("reports", Icons.Filled.BarChart, "Raporlar"),
        BottomNavItem("profile", Icons.Filled.Person, "Profil")
    )

    FloatingBottomBar(
        items = sampleItems,
        currentRoute = "home",
        onItemClick = {}
    )
}
