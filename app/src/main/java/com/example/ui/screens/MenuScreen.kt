package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Hotel
import com.example.data.model.MenuItem
import com.example.data.repository.HotelData
import com.example.ui.viewmodel.SharedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    hotelId: String,
    viewModel: SharedViewModel,
    onBackToHotels: () -> Unit,
    onNavigateToCart: () -> Unit
) {
    val hotel = remember(hotelId) { HotelData.HOTELS.find { it.id == hotelId } }
    val fullMenu = remember(hotelId) { HotelData.getMenuForHotel(hotelId) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showExitWarningDialog by remember { mutableStateOf(false) }

    // Customization Modal State
    var itemToCustomize by remember { mutableStateOf<MenuItem?>(null) }

    val cartItems by viewModel.cartItems.collectAsState()
    val cartCount by viewModel.cartItemCount.collectAsState()
    val cartTotal by viewModel.totalCartPrice.collectAsState()

    // Filter menu items
    val filteredMenuItems = remember(searchQuery, selectedCategory, fullMenu) {
        fullMenu.filter { item ->
            val matchesCategory = selectedCategory == "All" || item.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = item.name.contains(searchQuery, ignoreCase = true) ||
                    item.englishName.contains(searchQuery, ignoreCase = true) ||
                    item.description.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    if (hotel == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Hotel not found")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(hotel.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            hotel.branchName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (cartItems.isNotEmpty()) {
                                showExitWarningDialog = true
                            } else {
                                onBackToHotels()
                            }
                        },
                        modifier = Modifier.testTag("menu_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp), // Extra space so bottom cart isn't covering lists
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Search Input
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search dishes...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .testTag("menu_search_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                // Category Tabs Row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val categories = listOf("All", "Breakfast", "Meat")
                        categories.forEach { category ->
                            val isSelected = selectedCategory == category
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = category },
                                label = { Text(category, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.testTag("category_chip_$category"),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                    labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // Menu list items
                if (filteredMenuItems.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "No dishes match your filters.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(filteredMenuItems) { item ->
                        MenuItemCard(
                            menuItem = item,
                            onAddClicked = { itemToCustomize = item }
                        )
                    }
                }
            }

            // Floating Bottom Cart Bar
            AnimatedVisibility(
                visible = cartItems.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .navigationBarsPadding() // Support edge-to-edge
            ) {
                Card(
                    onClick = onNavigateToCart,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .testTag("floating_cart_bar")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                            Column {
                                Text(
                                    text = "$cartCount items in cart",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "From ${hotel.name}",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "ETB %,.2f".format(cartTotal),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "View Cart ➔",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Customization Dialog
    itemToCustomize?.let { menuItem ->
        CustomizationDialog(
            menuItem = menuItem,
            onDismiss = { itemToCustomize = null },
            onConfirmAdd = { qty, injera, spice ->
                viewModel.addToCart(menuItem, qty, injera, spice)
                itemToCustomize = null
            }
        )
    }

    // Exit Warning Dialog (switching hotels warning)
    if (showExitWarningDialog) {
        AlertDialog(
            onDismissRequest = { showExitWarningDialog = false },
            title = { Text("Clear your cart?") },
            text = { Text("Leaving this screen will clear your current pre-order items from ${hotel.name}. Do you want to continue?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearCart()
                        showExitWarningDialog = false
                        onBackToHotels()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_clear_cart_button")
                ) {
                    Text("Clear and Exit")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExitWarningDialog = false },
                    modifier = Modifier.testTag("cancel_clear_cart_button")
                ) {
                    Text("Stay Here")
                }
            }
        )
    }
}

@Composable
fun MenuItemCard(menuItem: MenuItem, onAddClicked: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Food visual card placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                // Displaying a food icon or beautiful placeholder letter representation
                Text(
                    text = "🍲",
                    fontSize = 32.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = menuItem.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                if (menuItem.englishName != menuItem.name) {
                    Text(
                        text = menuItem.englishName,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Text(
                    text = menuItem.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 2
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ETB %,.2f".format(menuItem.price),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 15.sp
                    )
                    
                    Button(
                        onClick = onAddClicked,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("add_item_button_${menuItem.id}"),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizationDialog(
    menuItem: MenuItem,
    onDismiss: () -> Unit,
    onConfirmAdd: (quantity: Int, injeraType: String, spicyLevel: String) -> Unit
) {
    var quantity by remember { mutableStateOf(1) }
    var injeraType by remember { mutableStateOf("Standard Injera") }
    var spicyLevel by remember { mutableStateOf("Medium") }

    val basePrice = menuItem.price
    val calculatedItemPrice = remember(injeraType) {
        if (injeraType == "Teff Injera") basePrice + 15.0 else basePrice
    }
    val calculatedTotalPrice = calculatedItemPrice * quantity

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.background),
        content = {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Customize Plate",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = menuItem.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("customize_close")) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 1. Injera Selection Customization
                Text(
                    text = "Injera Customization",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val injeras = listOf(
                        "Standard Injera" to "Included",
                        "Teff Injera" to "+ ETB 15.00"
                    )
                    injeras.forEach { (type, extra) ->
                        val selected = injeraType == type
                        Card(
                            onClick = { injeraType = type },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .testTag("injera_option_$type"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.tertiary
                            ),
                            border = if (selected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(type, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(extra, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Spicy Level
                Text(
                    text = "Spicy Level (Awaze / Pepper)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val levels = listOf("Mild", "Medium", "Extra Spicy")
                    levels.forEach { level ->
                        val selected = spicyLevel == level
                        Card(
                            onClick = { spicyLevel = level },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("spicy_option_$level"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = level,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 3. Quantity Incrementer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Select Quantity", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("qty_minus")
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = quantity.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        IconButton(
                            onClick = { if (quantity < 10) quantity++ },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("qty_plus")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom CTA action button
                Button(
                    onClick = { onConfirmAdd(quantity, injeraType, spicyLevel) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("add_customized_to_cart_button"),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Add to Basket", fontWeight = FontWeight.Bold)
                        Text(
                            text = "ETB %,.2f".format(calculatedTotalPrice),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    )
}
