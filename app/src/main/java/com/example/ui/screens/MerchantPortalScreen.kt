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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantPortalScreen(
    onBackToCustomer: () -> Unit
) {
    var isAuthenticated by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    // Once authenticated, merchant selects which hotel they manage
    var managedHotel by remember { mutableStateOf<Hotel?>(null) }

    if (!isAuthenticated) {
        // --- PASSCODE GATE ENTRY ---
        PasscodeGate(
            enteredPin = enteredPin,
            error = pinError,
            onPinChange = { pin ->
                if (pin.length <= 4) {
                    enteredPin = pin
                    pinError = false
                }
                if (pin == "1234") {
                    isAuthenticated = true
                } else if (pin.length == 4) {
                    // Wrong PIN entered
                    pinError = true
                    enteredPin = ""
                }
            },
            onBack = onBackToCustomer
        )
    } else if (managedHotel == null) {
        // --- HOTEL SELECTOR FOR CASHIER ---
        CashierHotelSelector(
            onHotelSelected = { hotel ->
                managedHotel = hotel
                OrderRepository.setActiveMerchantHotel(hotel.id)
            },
            onBack = {
                isAuthenticated = false
                enteredPin = ""
            }
        )
    } else {
        // --- REAL-TIME CENTRAL MERCHANT CONSOLE ---
        MerchantConsoleDashboard(
            hotel = managedHotel!!,
            onLogout = {
                OrderRepository.setActiveMerchantHotel(null)
                managedHotel = null
                isAuthenticated = false
                enteredPin = ""
            }
        )
    }
}

@Composable
fun PasscodeGate(
    enteredPin: String,
    error: Boolean,
    onPinChange: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vendor Portal", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("pin_back_button")) {
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
                text = "ምቹ Merchant Center",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Secure Cashier Area. Enter PIN to manage pre-orders.",
                color = Color.Gray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            // Visual Dot Pin Representation
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                for (i in 1..4) {
                    val filled = i <= enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    error -> Color.Red
                                    filled -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                    )
                }
            }

            if (error) {
                Text(
                    text = "Incorrect Pin. Please try again.",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Text(
                    text = "Hint: Cashier default PIN is 1234",
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Interactive Passcode Numeric Grid Layout
            PasscodeNumPad(
                onKeyPress = { num ->
                    if (enteredPin.length < 4) {
                        onPinChange(enteredPin + num)
                    }
                },
                onDelete = {
                    if (enteredPin.isNotEmpty()) {
                        onPinChange(enteredPin.dropLast(1))
                    }
                }
            )
        }
    }
}

@Composable
fun PasscodeNumPad(onKeyPress: (String) -> Unit, onDelete: () -> Unit) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("✖", "0", "⌫")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        keys.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                row.forEach { key ->
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .clickable {
                                when (key) {
                                    "✖" -> {}
                                    "⌫" -> onDelete()
                                    else -> onKeyPress(key)
                                }
                            }
                            .testTag("numpad_key_$key"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = key,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashierHotelSelector(
    onHotelSelected: (Hotel) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Your Vendor", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Welcome, Cashier!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Select the Hawassa hotel branch database you are operating today.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            items(HotelData.HOTELS) { hotel ->
                Card(
                    onClick = { onHotelSelected(hotel) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("merchant_select_hotel_${hotel.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(hotel.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(hotel.branchName, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantConsoleDashboard(
    hotel: Hotel,
    onLogout: () -> Unit
) {
    val orders by OrderRepository.orders.collectAsState()
    val isAlarmMuted by OrderRepository.isAlarmMuted.collectAsState()

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(hotel.name, fontWeight = FontWeight.Bold)
                        Text("Merchant Console (Cashier)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
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
                            onUpdateStatus = { nextStatus ->
                                OrderRepository.updateOrderStatus(order.id, nextStatus)
                            },
                            onOpenChat = {
                                selectedOrderForChat = order
                            }
                        )
                    }
                }
            }
        }
    }

    // Cashier Live Chat dialogue sheet
    selectedOrderForChat?.let { order ->
        // To keep chat content updated in real-time if a message is received, fetch it from repository list
        val currentOrderState = orders.find { it.id == order.id } ?: order
        CashierChatDialog(
            order = currentOrderState,
            onDismiss = { selectedOrderForChat = null }
        )
    }
}

@Composable
fun CashierOrderCard(
    order: Order,
    onUpdateStatus: (OrderStatus) -> Unit,
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

            // Actions panel: step status updater & chat launcher
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

                Divider()

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
                        // Re-use ChatBubble with correct sender alignment
                        ChatBubble(message = msg, isCustomer = !isCashierMessage)
                    }
                }

                Divider()

                // Cashier Quick Reply Template Row (MANDATORY REQUIREMENT F)
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
