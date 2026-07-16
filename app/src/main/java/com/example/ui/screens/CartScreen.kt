package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.SharedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: SharedViewModel,
    onBack: () -> Unit,
    onNavigateToTracking: (String) -> Unit
) {
    val hotel by viewModel.selectedHotel.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val totalAmount by viewModel.totalCartPrice.collectAsState()

    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var selectedPickupTime by remember { mutableStateOf("In 30 Minutes") }
    var paymentMethod by remember { mutableStateOf("telebirr") }

    val isCheckoutEnabled = customerName.isNotBlank() && customerPhone.isNotBlank() && cartItems.isNotEmpty()

    LaunchedEffect(viewModel.latestPlacedOrderId) {
        viewModel.latestPlacedOrderId.collect { orderId ->
            if (orderId != null) {
                onNavigateToTracking(orderId)
                viewModel.resetLatestOrderId()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Pre-Order Basket", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("cart_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        if (hotel == null || cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🛍️", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Your basket is empty", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Go back to the hotel menu to add delicious items.", color = Color.Gray)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onBack) {
                        Text("Browse Menus")
                    }
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(), // Ensure keyboard doesn't cover input
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Hotel info banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Ordering from: ${hotel?.name}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Branch: ${hotel?.branchName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // 2. Items list in Cart
            item {
                Text("Pre-order Items", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }

            items(cartItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cart_item_card_${item.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.menuItem.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                text = "${item.injeraType} • ${item.spicyLevel}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "ETB %,.2f".format(item.totalPrice),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp
                            )
                        }

                        // Quantity selector
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.updateCartItemQuantity(item.id, item.quantity - 1) },
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("cart_qty_minus_${item.id}")
                            ) {
                                Icon(
                                    imageVector = if (item.quantity == 1) Icons.Default.Delete else Icons.Default.Remove,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (item.quantity == 1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = item.quantity.toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            IconButton(
                                onClick = { viewModel.updateCartItemQuantity(item.id, item.quantity + 1) },
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("cart_qty_plus_${item.id}")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }

            // 3. CBE / telebirr Bank Payment Guide Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                            Text(
                                text = "Bank Payment Guidelines",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "To pre-order, transfer the total directly to the hotel's accounts below. You will attach the screenshot of the transaction on the next tracking page to verify.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("Merchant CBE Account Details", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(
                                    text = hotel?.bankDetails?.accountHolder ?: "",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = hotel?.bankDetails?.accountNumber ?: "",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 4. Contact Details Input fields
            item {
                Text("Your Details", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name (for pickup)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_customer_name"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("Phone Number (Ethiopia)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_customer_phone"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // 5. Preferred Pickup Time Selection Row
            item {
                Text("Select Pickup Time", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val times = listOf("In 30 Minutes", "In 1 Hour", "Custom Time")
                    times.forEach { time ->
                        val selected = selectedPickupTime == time
                        Card(
                            onClick = { selectedPickupTime = time },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("pickup_time_$time"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = time,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // 6. Payment method option selector
            item {
                Text("Payment Service", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val methods = listOf("telebirr", "CBE Birr")
                    methods.forEach { method ->
                        val selected = paymentMethod == method
                        Card(
                            onClick = { paymentMethod = method },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("payment_method_$method"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.tertiary
                            ),
                            border = if (selected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = method,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Price Breakdown summary
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal", color = Color.Gray)
                        Text("ETB %,.2f".format(totalAmount))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Convenience Prep Fee", color = Color.Gray)
                        Text("FREE (ምቹ)", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Amount Due", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text = "ETB %,.2f".format(totalAmount),
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 20.sp
                        )
                    }
                }
            }

            // Checkout submission CTA
            item {
                Button(
                    onClick = {
                        viewModel.checkout(customerName, customerPhone, selectedPickupTime)
                    },
                    enabled = isCheckoutEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("submit_checkout_button"),
                    shape = RoundedCornerShape(27.dp)
                ) {
                    Text(
                        text = "Submit Pre-Order & Transfer",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
