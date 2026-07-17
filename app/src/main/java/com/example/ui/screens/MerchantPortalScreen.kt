@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Hotel
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.repository.HotelData
import com.example.data.repository.OrderRepository
import com.example.data.repository.PasscodeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantPortalScreen(
    onBackToCustomer: () -> Unit
) {
    val activeHotelId by OrderRepository.activeMerchantHotelId.collectAsState()
    val activeRole by OrderRepository.activeMerchantRole.collectAsState()

    val managedHotel = remember(activeHotelId) {
        HotelData.HOTELS.find { it.id == activeHotelId }
    }

    if (managedHotel == null || activeRole == null) {
        // Safe redirection if not properly logged in
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Vendor Portal", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBackToCustomer, modifier = Modifier.testTag("portal_back_button")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🔒 Admin Access Only",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Please open the specific hotel's menu screen, then click the discrete 'Hotel Admin Portal' (lock icon) in the header to enter with your passcode.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onBackToCustomer,
                    modifier = Modifier.testTag("portal_redirect_button")
                ) {
                    Text("Return to Hotel Selection")
                }
            }
        }
    } else {
        // Authenticated Session Console
        MerchantConsoleDashboard(
            hotel = managedHotel,
            role = activeRole!!,
            onLogout = {
                OrderRepository.logoutMerchant()
                onBackToCustomer()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantConsoleDashboard(
    hotel: Hotel,
    role: String,
    onLogout: () -> Unit
) {
    val orders by OrderRepository.orders.collectAsState()
    val isAlarmMuted by OrderRepository.isAlarmMuted.collectAsState()

    var showCloseoutConfirmDialog by remember { mutableStateOf(false) }
    var showSecuritySettings by remember { mutableStateOf(false) }

    // Filter dashboard: Only active (Pending, Preparing, Ready) orders for THIS hotel
    val activeMerchantOrders = remember(orders, hotel.id) {
        orders.filter {
            it.hotelId == hotel.id &&
                    it.orderStatus != OrderStatus.COMPLETED &&
                    it.orderStatus != OrderStatus.CANCELLED
        }
    }

    // Check if there are any un-acknowledged/pending orders to show sound alarm status
    val hasPendingPreorders = remember(activeMerchantOrders) {
        activeMerchantOrders.any { it.orderStatus == OrderStatus.PENDING }
    }

    var selectedOrderForChat by remember { mutableStateOf<Order?>(null) }
    var activeTab by remember { mutableStateOf("orders") } // "orders" | "stock"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(hotel.name, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (role == "owner") "Owner (ባለቤት) Dashboard" else "Cashier (ካሼር) Console",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onLogout, modifier = Modifier.testTag("merchant_logout")) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Exit Portal", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Owner actions panel (visible ONLY to owners)
            if (role == "owner") {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Owner Controls", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            Text("Manage security & reset orders", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Security Settings Button
                            FilledTonalButton(
                                onClick = { showSecuritySettings = true },
                                modifier = Modifier.testTag("owner_settings_button"),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Security", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Daily Closeout Button
                            Button(
                                onClick = { showCloseoutConfirmDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.testTag("owner_closeout_button"),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Closeout", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Tab Row Controls
            TabRow(
                selectedTabIndex = if (activeTab == "orders") 0 else 1,
                modifier = Modifier.fillMaxWidth().testTag("merchant_tab_row")
            ) {
                Tab(
                    selected = activeTab == "orders",
                    onClick = { activeTab = "orders" },
                    text = { Text("Pre-Orders (${activeMerchantOrders.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("merchant_orders_tab")
                )
                Tab(
                    selected = activeTab == "stock",
                    onClick = { activeTab = "stock" },
                    text = { Text("Stock & Availability", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.RestaurantMenu, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("merchant_stock_tab")
                )
            }

            if (activeTab == "orders") {
                // Pulsing Alarm Banner if we have pending orders and alarm is unmuted
                AnimatedVisibility(
                    visible = hasPendingPreorders,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    val infiniteTransition = rememberInfiniteTransition()
                    val pulseColor by infiniteTransition.animateColor(
                        initialValue = MaterialTheme.colorScheme.error,
                        targetValue = MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )

                    Surface(
                        color = if (isAlarmMuted) MaterialTheme.colorScheme.secondaryContainer else pulseColor,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(
                                    imageVector = if (isAlarmMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    tint = if (isAlarmMuted) MaterialTheme.colorScheme.onSecondaryContainer else Color.White
                                )
                                Column {
                                    Text(
                                        text = if (isAlarmMuted) "Alarm Silenced (Acknowledged)" else "🚨 NEW PRE-ORDER RECEIVED!",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = if (isAlarmMuted) MaterialTheme.colorScheme.onSecondaryContainer else Color.White
                                    )
                                    Text(
                                        text = "Pending pre-orders require confirmation",
                                        fontSize = 11.sp,
                                        color = if (isAlarmMuted) Color.Gray else Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Button(
                                onClick = { OrderRepository.setAlarmMuted(!isAlarmMuted) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isAlarmMuted) MaterialTheme.colorScheme.primary else Color.White,
                                    contentColor = if (isAlarmMuted) Color.White else MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.testTag("mute_alarm_button")
                            ) {
                                Text(
                                    text = if (isAlarmMuted) "Unmute Alarm" else "Mute Alert",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Dashboard orders view list
                if (activeMerchantOrders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                            Text("☕", fontSize = 64.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Dashboard is quiet", style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No active pre-orders for your branch right now.", color = Color.Gray, textAlign = TextAlign.Center)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(activeMerchantOrders) { order ->
                            CashierOrderCard(
                                order = order,
                                role = role,
                                onUpdateStatus = { nextStatus ->
                                    OrderRepository.updateOrderStatus(order.id, nextStatus)
                                },
                                onCancelOrder = {
                                    OrderRepository.cancelOrder(order.id)
                                },
                                onOpenChat = {
                                    selectedOrderForChat = order
                                }
                            )
                        }
                    }
                }
            } else {
                // Stock / Availability Management View Tab
                val menuItems by OrderRepository.menuItems.collectAsState()
                val hotelMenuItems = remember(menuItems, hotel.id) {
                    menuItems.filter { it.hotelId == hotel.id }
                }

                if (hotelMenuItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No items configured for this hotel", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "Item Availability Management",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = "Toggle switches to instantly mark dishes as Sold Out (አልቋል) or Available for customer pre-orders.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }

                        items(hotelMenuItems) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth().testTag("cashier_stock_card_${item.id}"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(item.englishName, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("ETB %,.2f".format(item.price), fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = if (item.isAvailable) "Available" else "Sold Out",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (item.isAvailable) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                                        )
                                        Switch(
                                            checked = item.isAvailable,
                                            onCheckedChange = { isChecked ->
                                                OrderRepository.toggleMenuItemAvailability(item.id, isChecked)
                                            },
                                            modifier = Modifier.testTag("switch_item_availability_${item.id}"),
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = Color(0xFF2E7D32),
                                                uncheckedThumbColor = Color.White,
                                                uncheckedTrackColor = MaterialTheme.colorScheme.error
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Cashier Live Chat dialogue sheet
    selectedOrderForChat?.let { order ->
        val currentOrderState = orders.find { it.id == order.id } ?: order
        CashierChatDialog(
            order = currentOrderState,
            onDismiss = { selectedOrderForChat = null }
        )
    }

    // 1. Owner Security Settings Dialog
    if (showSecuritySettings && role == "owner") {
        val context = LocalContext.current
        var tempCashierPin by remember { mutableStateOf(PasscodeManager.getCashierPasscode(context, hotel.id)) }
        var tempOwnerPin by remember { mutableStateOf(PasscodeManager.getOwnerPasscode(context, hotel.id)) }
        var saveSuccess by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { 
                showSecuritySettings = false 
                saveSuccess = false
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Security Settings", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Modify authentication passcodes for ${hotel.name}. Changes take effect immediately.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    
                    OutlinedTextField(
                        value = tempCashierPin,
                        onValueChange = { tempCashierPin = it },
                        label = { Text("Change Cashier Passcode") },
                        modifier = Modifier.fillMaxWidth().testTag("change_cashier_pin_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    OutlinedTextField(
                        value = tempOwnerPin,
                        onValueChange = { tempOwnerPin = it },
                        label = { Text("Change Owner Passcode") },
                        modifier = Modifier.fillMaxWidth().testTag("change_owner_pin_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    if (saveSuccess) {
                        Text(
                            text = "✅ Passcodes updated successfully!",
                            color = Color(0xFF2E7D32),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempCashierPin.isNotBlank() && tempOwnerPin.isNotBlank()) {
                            PasscodeManager.saveCashierPasscode(context, hotel.id, tempCashierPin)
                            PasscodeManager.saveOwnerPasscode(context, hotel.id, tempOwnerPin)
                            saveSuccess = true
                        }
                    },
                    modifier = Modifier.testTag("save_passcodes_button")
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showSecuritySettings = false 
                        saveSuccess = false
                    },
                    modifier = Modifier.testTag("close_settings_button")
                ) {
                    Text("Close")
                }
            }
        )
    }

    // 2. Daily Closeout Confirmation Dialog
    if (showCloseoutConfirmDialog && role == "owner") {
        AlertDialog(
            onDismissRequest = { showCloseoutConfirmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Text("Daily Closeout Confirmation")
                }
            },
            text = {
                Text("This action will archive and CLEAR all pre-orders for ${hotel.name}. The dashboard will be reset to 0 for the next business day. This action is irreversible. Do you want to proceed?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        OrderRepository.clearAllOrdersForHotel(hotel.id)
                        showCloseoutConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_closeout_button")
                ) {
                    Text("Reset Dashboard")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCloseoutConfirmDialog = false },
                    modifier = Modifier.testTag("cancel_closeout_button")
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CashierOrderCard(
    order: Order,
    role: String,
    onUpdateStatus: (OrderStatus) -> Unit,
    onCancelOrder: () -> Unit,
    onOpenChat: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cashier_order_card_${order.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Order ID and Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "ORDER ID: #${order.id}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                    Text(
                        text = "Customer: ${order.customerName} (${order.customerPhone})",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            when (order.orderStatus) {
                                OrderStatus.PENDING -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                OrderStatus.PREPARING -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                OrderStatus.READY -> Color(0xFF2E7D32).copy(alpha = 0.1f)
                                else -> Color.Gray.copy(alpha = 0.1f)
                            },
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = order.orderStatus.displayName.uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = when (order.orderStatus) {
                            OrderStatus.PENDING -> MaterialTheme.colorScheme.error
                            OrderStatus.PREPARING -> MaterialTheme.colorScheme.primary
                            OrderStatus.READY -> Color(0xFF2E7D32)
                            else -> Color.Gray
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Order items breakdown
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(12.dp)
            ) {
                Column {
                    order.items.forEach { item ->
                        Text(
                            text = "• ${item.quantity}x ${item.menuItem.name} [${item.injeraType} | ${item.spicyLevel}]",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Payment Receipt Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(
                        imageVector = if (order.paymentReceiptImageUri != null) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (order.paymentReceiptImageUri != null) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (order.paymentReceiptImageUri != null) "Receipt Screenshot Attached" else "No Receipt Uploaded Yet",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (order.paymentReceiptImageUri != null) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                    )
                }

                Text(
                    text = "Total Due: ETB %,.2f".format(order.total),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pickup requirement
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                Text(
                    text = "Requested Pickup: ${order.pickupTime}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions panel: step status updater & chat launcher & owner cancel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenChat,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("cashier_open_chat_${order.id}"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Live Chat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Status advancement CTA
                val (ctaText, nextStatus) = when (order.orderStatus) {
                    OrderStatus.PENDING -> "Verify & Cook" to OrderStatus.PREPARING
                    OrderStatus.PREPARING -> "Mark as Ready" to OrderStatus.READY
                    OrderStatus.READY -> "Complete Order" to OrderStatus.COMPLETED
                    else -> "" to null
                }

                if (nextStatus != null) {
                    Button(
                        onClick = { onUpdateStatus(nextStatus) },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(44.dp)
                            .testTag("cashier_action_${order.id}"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(ctaText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Owner Cancel Action Button
                if (role == "owner") {
                    OutlinedButton(
                        onClick = onCancelOrder,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("owner_cancel_${order.id}"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = "Cancel", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashierChatDialog(
    order: Order,
    onDismiss: () -> Unit
) {
    var chatText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .padding(16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.background),
        content = {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Live Chat Console",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Order: #${order.id} • Customer: ${order.customerName}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_cashier_chat")) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider()

                // Chat history scroll list
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(order.chatMessages) { msg ->
                        val isCashierMessage = msg.sender == "merchant"
                        ChatBubble(message = msg, isCustomer = !isCashierMessage)
                    }
                }

                HorizontalDivider()

                // Cashier Quick Reply Template Row
                Text(
                    text = "Quick Reply Templates:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val templates = listOf(
                        "Verified! Cooking. 🍳" to "Payment verified! We are preparing your food. 🍳",
                        "Ready for Pick up! ✅" to "Your pre-order is ready for pickup! Please come to our cashier counter. ✅",
                        "Running late ⏳" to "Sorry, we are experiencing a high volume of orders. Your food will take 10 minutes longer."
                    )
                    
                    templates.forEachIndexed { idx, (label, fullText) ->
                        AssistChip(
                            onClick = {
                                OrderRepository.addChatMessage(
                                    orderId = order.id,
                                    sender = "merchant",
                                    text = fullText
                                )
                            },
                            label = { Text(label, fontSize = 10.sp) },
                            modifier = Modifier.testTag("cashier_quick_reply_$idx")
                        )
                    }
                }

                // Compose panel
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = chatText,
                        onValueChange = { chatText = it },
                        placeholder = { Text("Reply to customer...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("cashier_chat_input"),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )

                    IconButton(
                        onClick = {
                            if (chatText.isNotBlank()) {
                                OrderRepository.addChatMessage(
                                    orderId = order.id,
                                    sender = "merchant",
                                    text = chatText
                                )
                                chatText = ""
                            }
                        },
                        enabled = chatText.isNotBlank(),
                        modifier = Modifier
                            .background(
                                if (chatText.isNotBlank()) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                                CircleShape
                            )
                            .testTag("cashier_send_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    )
}
