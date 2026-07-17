package com.example.data.repository

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import com.example.data.model.CartItem
import com.example.data.model.ChatMessage
import com.example.data.model.Hotel
import com.example.data.model.MenuItem
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object OrderRepository {
    private const val TAG = "OrderRepository"

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _isAlarmMuted = MutableStateFlow(false)
    val isAlarmMuted: StateFlow<Boolean> = _isAlarmMuted.asStateFlow()

    // Dynamic Lists of Hotels and Menu Items to support real-time online status and item availability
    private val _hotels = MutableStateFlow<List<Hotel>>(HotelData.HOTELS)
    val hotels: StateFlow<List<Hotel>> = _hotels.asStateFlow()

    private val _menuItems = MutableStateFlow<List<MenuItem>>(HotelData.MENU_ITEMS)
    val menuItems: StateFlow<List<MenuItem>> = _menuItems.asStateFlow()

    // Active merchant hotel to listen to
    private val _activeMerchantHotelId = MutableStateFlow<String?>(null)
    val activeMerchantHotelId: StateFlow<String?> = _activeMerchantHotelId.asStateFlow()

    // Active merchant role: "cashier" or "owner"
    private val _activeMerchantRole = MutableStateFlow<String?>(null)
    val activeMerchantRole: StateFlow<String?> = _activeMerchantRole.asStateFlow()

    private var toneGenerator: ToneGenerator? = null
    private var alarmJob: Job? = null
    private var heartbeatJob: Job? = null
    private val repositoryScope = CoroutineScope(Dispatchers.Default)

    private var appContext: Context? = null

    init {
        // Pre-populate with some realistic mock orders to make first-launch and merchant dashboard fully testable
        prepopulateMockOrders()
        
        // Start the automated alarm monitor
        startAlarmMonitor()
    }

    fun initialize(context: Context) {
        appContext = context.applicationContext
        NotificationHelper.createNotificationChannel(context)
        Log.d(TAG, "OrderRepository initialized with Context")
    }

    fun loginMerchant(hotelId: String, role: String) {
        _activeMerchantHotelId.value = hotelId
        _activeMerchantRole.value = role
        Log.d(TAG, "Merchant logged in: hotelId=$hotelId, role=$role")
        
        // Cashier Heartbeat: set online and start the periodic 60-second update job
        startHeartbeat(hotelId)
    }

    fun logoutMerchant() {
        val hotelId = _activeMerchantHotelId.value
        if (hotelId != null) {
            setHotelOnlineStatus(hotelId, isOnline = false)
        }
        _activeMerchantHotelId.value = null
        _activeMerchantRole.value = null
        heartbeatJob?.cancel()
        Log.d(TAG, "Merchant logged out")
    }

    private fun startHeartbeat(hotelId: String) {
        heartbeatJob?.cancel()
        heartbeatJob = repositoryScope.launch {
            try {
                while (true) {
                    setHotelOnlineStatus(hotelId, isOnline = true)
                    delay(60000) // update lastActive and isOnline every 60 seconds
                }
            } catch (e: Exception) {
                Log.e(TAG, "Heartbeat interrupted for hotel: $hotelId", e)
            }
        }
    }

    fun setHotelOnlineStatus(hotelId: String, isOnline: Boolean) {
        _hotels.value = _hotels.value.map { hotel ->
            if (hotel.id == hotelId) {
                hotel.copy(isOnline = isOnline, lastActive = System.currentTimeMillis())
            } else {
                hotel
            }
        }
    }

    fun toggleMenuItemAvailability(itemId: String, isAvailable: Boolean) {
        _menuItems.value = _menuItems.value.map { item ->
            if (item.id == itemId) {
                item.copy(isAvailable = isAvailable)
            } else {
                item
            }
        }
        Log.d(TAG, "Menu item $itemId availability updated to: $isAvailable")
    }

    fun getMenuForHotel(hotelId: String): List<MenuItem> {
        val items = _menuItems.value.filter { it.hotelId == hotelId }
        if (items.isEmpty()) {
            val fallback = listOf(
                MenuItem(
                    id = "bf_chechebsa_gen_$hotelId",
                    hotelId = hotelId,
                    name = "Standard Chechebsa",
                    englishName = "Standard Chechebsa",
                    price = 220.00,
                    category = "Breakfast",
                    description = "Shredded flatbread cooked with local herbal spiced butter and hot berbere, served with a hint of honey.",
                    isAvailable = true
                ),
                MenuItem(
                    id = "meat_tibs_gen_$hotelId",
                    hotelId = hotelId,
                    name = "Sautéed Beef Tibs",
                    englishName = "Sautéed Beef Tibs",
                    price = 450.00,
                    category = "Meat",
                    description = "Fresh juicy beef chunks sautéed with onions, green chilies, garlic, and rosemary herbs. Extremely aromatic.",
                    isAvailable = true
                )
            )
            // Save fallback to current menu list to keep it consistent
            val currentList = _menuItems.value.toMutableList()
            currentList.addAll(fallback)
            _menuItems.value = currentList
            return fallback
        }
        return items
    }

    fun setActiveMerchantHotel(hotelId: String?) {
        _activeMerchantHotelId.value = hotelId
        Log.d(TAG, "Merchant active hotel set to: $hotelId")
    }

    fun setAlarmMuted(muted: Boolean) {
        _isAlarmMuted.value = muted
        Log.d(TAG, "Alarm muted state changed to: $muted")
    }

    fun cancelOrder(orderId: String) {
        updateOrderStatus(orderId, OrderStatus.CANCELLED)
    }

    fun clearAllOrdersForHotel(hotelId: String) {
        _orders.value = _orders.value.filter { it.hotelId != hotelId }
        Log.d(TAG, "Cleared all orders for hotel: $hotelId")
    }

    fun placeOrder(order: Order) {
        val currentList = _orders.value.toMutableList()
        currentList.add(order)
        _orders.value = currentList
        Log.d(TAG, "Placed new order: ${order.id} for hotel: ${order.hotelId}")
        
        // When a new order is placed, unmute the alarm automatically to notify the merchant!
        _isAlarmMuted.value = false

        // Notify Cashier dynamically even if screen is locked
        appContext?.let { context ->
            val activeHotel = _activeMerchantHotelId.value
            if (activeHotel == order.hotelId) {
                NotificationHelper.triggerNotification(
                    context = context,
                    title = "🚨 New Pre-Order #${order.id}",
                    message = "New pre-order received from ${order.customerName} for pickup at ${order.pickupTime}!"
                )
            } else {
                // Also trigger general test alert so tester gets visual confirmation of local push notification
                NotificationHelper.triggerNotification(
                    context = context,
                    title = "Michu: Pre-Order Placed!",
                    message = "Your pre-order #${order.id} for ${order.customerName} has been submitted successfully."
                )
            }
        }
    }

    fun updateOrderStatus(orderId: String, status: OrderStatus) {
        _orders.value = _orders.value.map { order ->
            if (order.id == orderId) {
                val updatedChat = order.chatMessages.toMutableList()
                val messageText = when (status) {
                    OrderStatus.PREPARING -> "🍳 We are preparing your food! It will be ready in approximately ${HotelData.HOTELS.find { it.id == order.hotelId }?.prepTimeMinutes ?: 30} minutes."
                    OrderStatus.READY -> "✅ Your food is ready for pickup! Please head to the branch counter."
                    OrderStatus.COMPLETED -> "🎉 Order completed! Thank you for choosing Michu (ምቹ)."
                    OrderStatus.CANCELLED -> "❌ This order has been cancelled by the merchant."
                    else -> ""
                }
                
                if (messageText.isNotEmpty()) {
                    updatedChat.add(
                        ChatMessage(
                            id = "system_${System.currentTimeMillis()}",
                            sender = "merchant",
                            message = messageText,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }

                // Trigger push notification to Customer for status update
                appContext?.let { context ->
                    NotificationHelper.triggerNotification(
                        context = context,
                        title = "Michu (ምቹ): Order Updated!",
                        message = "Your order #${order.id} status is now: ${status.displayName}."
                    )
                }

                order.copy(
                    orderStatus = status,
                    paymentStatus = if (status == OrderStatus.COMPLETED) "paid" else order.paymentStatus,
                    chatMessages = updatedChat
                )
            } else {
                order
            }
        }
        Log.d(TAG, "Updated order $orderId to status: $status")
    }

    fun addChatMessage(orderId: String, sender: String, text: String, imageUrl: String? = null) {
        _orders.value = _orders.value.map { order ->
            if (order.id == orderId) {
                val updatedChat = order.chatMessages.toMutableList()
                updatedChat.add(
                    ChatMessage(
                        id = "chat_${System.currentTimeMillis()}",
                        sender = sender,
                        message = text,
                        timestamp = System.currentTimeMillis(),
                        imageUrl = imageUrl
                    )
                )
                
                // If the customer sends a receipt image, we automatically update paymentStatus to "paid" (or pending verification)
                val newPaymentStatus = if (imageUrl != null && sender == "customer") {
                    "paid"
                } else {
                    order.paymentStatus
                }

                // Trigger notifications based on sender
                appContext?.let { context ->
                    if (sender == "merchant") {
                        NotificationHelper.triggerNotification(
                            context = context,
                            title = "Michu: New message from Cashier!",
                            message = text
                        )
                    } else if (sender == "customer") {
                        val activeHotel = _activeMerchantHotelId.value
                        if (activeHotel == order.hotelId) {
                            NotificationHelper.triggerNotification(
                                context = context,
                                title = "Michu Chat: #${order.id}",
                                message = "${order.customerName}: $text"
                            )
                        }
                    }
                }

                order.copy(
                    chatMessages = updatedChat,
                    paymentStatus = newPaymentStatus,
                    paymentReceiptImageUri = imageUrl ?: order.paymentReceiptImageUri
                )
            } else {
                order
            }
        }
        Log.d(TAG, "Added chat message to order $orderId from $sender: $text")
    }

    private fun startAlarmMonitor() {
        alarmJob?.cancel()
        alarmJob = repositoryScope.launch {
            try {
                toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 85)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize ToneGenerator", e)
            }

            try {
                while (true) {
                    val merchantHotelId = _activeMerchantHotelId.value
                    val isMuted = _isAlarmMuted.value
                    
                    if (merchantHotelId != null && !isMuted) {
                        // Check if there are any PENDING orders for the merchant's hotel
                        val hasPendingOrders = _orders.value.any { 
                            it.hotelId == merchantHotelId && it.orderStatus == OrderStatus.PENDING 
                        }
                        
                        if (hasPendingOrders) {
                            try {
                                // Play a double high-frequency beep to alert the cashier
                                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_PIP, 200)
                                delay(350)
                                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_PIP, 200)
                            } catch (e: Exception) {
                                Log.e(TAG, "Tone playback failed", e)
                            }
                        }
                    }
                    // Sleep 2 seconds between checks/beeps
                    delay(2000)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Alarm monitor loop interrupted", e)
            }
        }
    }

    private fun prepopulateMockOrders() {
        val haileHotel = HotelData.HOTELS[0] // Haile Resort
        val lewiHotel = HotelData.HOTELS[1] // Lewi Resort

        val menuHaile = HotelData.getMenuForHotel(haileHotel.id)
        val menuLewi = HotelData.getMenuForHotel(lewiHotel.id)

        val item1 = CartItem(
            id = "cart_1",
            menuItem = menuHaile[0], // Special Chechebsa
            quantity = 2,
            injeraType = "Standard Injera",
            spicyLevel = "Medium"
        )

        val item2 = CartItem(
            id = "cart_2",
            menuItem = menuLewi[0], // Lewi Classic Chechebsa
            quantity = 1,
            injeraType = "Teff Injera",
            spicyLevel = "Extra Spicy"
        )

        val mockOrder1 = Order(
            id = "order_1001",
            hotelId = haileHotel.id,
            customerName = "Tamagn Melaku",
            customerPhone = "0912345678",
            pickupTime = "12:30 PM",
            items = listOf(item1),
            subtotal = 560.0,
            total = 560.0,
            paymentStatus = "pending",
            orderStatus = OrderStatus.PENDING,
            chatMessages = listOf(
                ChatMessage(
                    id = "msg_1",
                    sender = "customer",
                    message = "Hello! I am ordering Chechebsa. Please make it with extra honey.",
                    timestamp = System.currentTimeMillis() - 600000
                ),
                ChatMessage(
                    id = "msg_2",
                    sender = "merchant",
                    message = "Understood! Please send the CBE/telebirr transfer and upload your receipt screenshot here so we can accept and start preparing.",
                    timestamp = System.currentTimeMillis() - 500000
                )
            ),
            timestamp = System.currentTimeMillis() - 600000
        )

        val mockOrder2 = Order(
            id = "order_1002",
            hotelId = lewiHotel.id,
            customerName = "Selamawit Kebede",
            customerPhone = "0987654321",
            pickupTime = "01:15 PM",
            items = listOf(item2),
            subtotal = 275.0, // 260 + 15 (teff)
            total = 275.0,
            paymentStatus = "paid",
            orderStatus = OrderStatus.PREPARING,
            chatMessages = listOf(
                ChatMessage(
                    id = "msg_3",
                    sender = "customer",
                    message = "Hi! I just paid via telebirr.",
                    timestamp = System.currentTimeMillis() - 300000
                ),
                ChatMessage(
                    id = "msg_4",
                    sender = "merchant",
                    message = "Payment verified! We are preparing your food. 🍳",
                    timestamp = System.currentTimeMillis() - 200000
                )
            ),
            timestamp = System.currentTimeMillis() - 300000
        )

        _orders.value = listOf(mockOrder1, mockOrder2)
    }
}
