package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.CartScreen
import com.example.ui.screens.HotelSelectionScreen
import com.example.ui.screens.MenuScreen
import com.example.ui.screens.MerchantPortalScreen
import com.example.ui.screens.TrackingScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.SharedViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val sharedViewModel: SharedViewModel = viewModel()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Set up navigation graph for Michu (ምቹ) pre-order flows
                    NavHost(
                        navController = navController,
                        startDestination = "hotel_selection"
                    ) {
                        // SCREEN 1: Customer Hotel Selection Screen
                        composable("hotel_selection") {
                            HotelSelectionScreen(
                                onHotelSelected = { hotelId ->
                                    sharedViewModel.selectHotel(hotelId)
                                    navController.navigate("menu/$hotelId")
                                },
                                onMerchantPortalClicked = {
                                    navController.navigate("merchant_portal")
                                }
                            )
                        }

                        // SCREEN 2: Menu Grid Filtered Screen
                        composable(
                            route = "menu/{hotelId}",
                            arguments = listOf(navArgument("hotelId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val hotelId = backStackEntry.arguments?.getString("hotelId") ?: ""
                            MenuScreen(
                                hotelId = hotelId,
                                viewModel = sharedViewModel,
                                onBackToHotels = { navController.popBackStack() },
                                onNavigateToCart = { navController.navigate("cart") }
                            )
                        }

                        // SCREEN 3: Cart Customization & Checkout Screen
                        composable("cart") {
                            CartScreen(
                                viewModel = sharedViewModel,
                                onBack = { navController.popBackStack() },
                                onNavigateToTracking = { orderId ->
                                    navController.navigate("tracking/$orderId") {
                                        popUpTo("hotel_selection") { inclusive = false }
                                    }
                                }
                            )
                        }

                        // SCREEN 4: Live Order Tracking Screen
                        composable(
                            route = "tracking/{orderId}",
                            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                            TrackingScreen(
                                orderId = orderId,
                                onBackToSelection = {
                                    navController.navigate("hotel_selection") {
                                        popUpTo("hotel_selection") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // SCREEN 5: Multi-Vendor Cashier (Merchant) Portal
                        composable("merchant_portal") {
                            MerchantPortalScreen(
                                onBackToCustomer = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
