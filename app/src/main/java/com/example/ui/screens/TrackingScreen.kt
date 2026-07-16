package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.repository.HotelData
import com.example.data.repository.OrderRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(
    orderId: String,
    onBackToSelection: () -> Unit
) {
    val orders by OrderRepository.orders.collectAsState()
    val order = remember(orders, orderId) { orders.find { it.id == orderId } }
    val hotel = remember(order) { order?.let { o -> HotelData.HOTELS.find { it.id == o.hotelId } } }

    var chatText by remember { mutableStateOf("") }
    var showReceiptSimulatorDialog by remember { mutableStateOf(false) }
    
    val chatListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll chat to the bottom on new messages
    LaunchedEffect(order?.chatMessages?.size) {
        order?.chatMessages?.size?.let { size ->
            if (size > 0) {
                chatListState.animateScrollToItem(size - 1)
            }
        }
    }

    if (order == null || hotel == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Order #$orderId not found.")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onBackToSelection) {
                    Text("Back to Hotels")
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Pre-Order Tracking", fontWeight = FontWeight.Bold)
                        Text(
                            "Order: #${order.id} • ${hotel.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackToSelection, modifier = Modifier.testTag("tracking_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding() // Avoid keyboard overlay
        ) {
            // 1. Live Step Progress Bar Component
            OrderStatusProgressBar(status = order.orderStatus)

            Divider()

            // 2. Chat / Order Content Area split
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    
                    // CBE / telebirr receipt info bar if payment is pending
                    if (order.paymentStatus == "pending" && order.paymentReceiptImageUri == null) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("💸", fontSize = 24.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Payment Pending Verification",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = "Please transfer ETB %,.2f to the CBE/telebirr account and attach the receipt below.".format(order.total),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    } else if (order.paymentReceiptImageUri != null && order.paymentStatus != "completed") {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("🧾", fontSize = 24.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Receipt Attached - Pending Verification",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                    Text(
                                        text = "The hotel cashier has been notified. They are verifying your receipt to begin cooking.",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    // Chat messages scrollable area
                    LazyColumn(
                        state = chatListState,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Order details summarized inside chat as a starter card
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Pre-Order Details Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    order.items.forEach { item ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${item.quantity}x ${item.menuItem.name} (${item.injeraType})",
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = "ETB %,.2f".format(item.totalPrice),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Total Amount Due", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(
                                            "ETB %,.2f".format(order.total),
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Pickup Time: ${order.pickupTime}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Chat messages loop
                        items(order.chatMessages) { msg ->
                            val isCustomer = msg.sender == "customer"
                            ChatBubble(message = msg, isCustomer = isCustomer)
                        }
                    }
                }
            }

            // 3. Compose Messaging Bar
            Surface(
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .navigationBarsPadding() // Keep clear of Android gesture pill
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { showReceiptSimulatorDialog = true },
                        modifier = Modifier
                            .testTag("attach_receipt_button")
                            .background(
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Attach Receipt",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    OutlinedTextField(
                        value = chatText,
                        onValueChange = { chatText = it },
                        placeholder = { Text("Ask the cashier something...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        singleLine = true
                    )

                    IconButton(
                        onClick = {
                            if (chatText.isNotBlank()) {
                                OrderRepository.addChatMessage(
                                    orderId = orderId,
                                    sender = "customer",
                                    text = chatText
                                )
                                chatText = ""
                            }
                        },
                        enabled = chatText.isNotBlank(),
                        modifier = Modifier
                            .testTag("send_chat_button")
                            .background(
                                if (chatText.isNotBlank()) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                                CircleShape
                            )
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
    }

    // Receipt Simulator Dialog
    if (showReceiptSimulatorDialog) {
        AlertDialog(
            onDismissRequest = { showReceiptSimulatorDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("⚡")
                    Text("Payment Receipt Simulator", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Because you are running in the AI Studio secure environment, you can attach a simulated CBE or telebirr receipt below to test the merchant verification alert.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "SIMULATION RECEIPT OPTIONS:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Simulated Options
                    Button(
                        onClick = {
                            OrderRepository.addChatMessage(
                                orderId = orderId,
                                sender = "customer",
                                text = "Attached: [telebirr Receipt #TXN983742 - ETB %,.2f]".format(order.total),
                                imageUrl = "simulated_telebirr_receipt"
                            )
                            showReceiptSimulatorDialog = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("simulate_receipt_telebirr")
                    ) {
                        Text("Simulate telebirr Receipt Transfer")
                    }

                    Button(
                        onClick = {
                            OrderRepository.addChatMessage(
                                orderId = orderId,
                                sender = "customer",
                                text = "Attached: [CBE Birr Receipt #FT264821 - ETB %,.2f]".format(order.total),
                                imageUrl = "simulated_cbe_receipt"
                            )
                            showReceiptSimulatorDialog = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("simulate_receipt_cbe")
                    ) {
                        Text("Simulate CBE Birr Transfer Receipt")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showReceiptSimulatorDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun OrderStatusProgressBar(status: OrderStatus) {
    val steps = listOf(
        OrderStatus.PENDING to "Pending",
        OrderStatus.PREPARING to "Preparing",
        OrderStatus.READY to "Ready",
        OrderStatus.COMPLETED to "Completed"
    )

    // Find the current active step index
    val activeIndex = steps.indexOfFirst { it.first == status }.let { if (it == -1) 0 else it }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, (stepStatus, label) ->
                val isCompletedStep = index <= activeIndex
                val isCurrent = index == activeIndex

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCurrent -> MaterialTheme.colorScheme.primary
                                    isCompletedStep -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = when (stepStatus) {
                            OrderStatus.PENDING -> Icons.Default.Schedule
                            OrderStatus.PREPARING -> Icons.Default.Restaurant
                            OrderStatus.READY -> Icons.Default.CheckCircle
                            OrderStatus.COMPLETED -> Icons.Default.CheckCircle
                            else -> Icons.Default.Schedule
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isCompletedStep || isCurrent) Color.White else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                        color = if (isCompletedStep || isCurrent) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }

                // Draw connector line if not the last step
                if (index < steps.size - 1) {
                    Box(
                        modifier = Modifier
                            .weight(0.5f)
                            .height(4.dp)
                            .background(
                                if (index < activeIndex) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: com.example.data.model.ChatMessage, isCustomer: Boolean) {
    val bubbleColor = if (isCustomer) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val textColor = if (isCustomer) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val alignment = if (isCustomer) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("chat_bubble_${message.id}"),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isCustomer) 16.dp else 4.dp,
                        bottomEnd = if (isCustomer) 4.dp else 16.dp
                    )
                )
                .background(bubbleColor)
                .padding(12.dp)
                .widthIn(max = 280.dp)
        ) {
            Column {
                if (message.imageUrl != null) {
                    // Receipt visual card indicator
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.1f))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = if (isCustomer) Color.White else MaterialTheme.colorScheme.primary)
                            Column {
                                Text(
                                    text = if (message.imageUrl.contains("telebirr")) "telebirr Receipt Attached" else "CBE Receipt Attached",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isCustomer) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Text("Click to view full image", fontSize = 9.sp, color = if (isCustomer) Color.White.copy(alpha = 0.7f) else Color.Gray)
                            }
                        }
                    }
                }
                
                Text(
                    text = message.message,
                    color = textColor,
                    fontSize = 13.sp
                )
            }
        }
        
        Text(
            text = if (isCustomer) "You" else "Hotel Cashier",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            fontSize = 9.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}
