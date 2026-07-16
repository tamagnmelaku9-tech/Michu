package com.example.data.repository

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import com.example.data.model.CartItem
import com.example.data.model.ChatMessage
import com.example.data.model.Order
import com.example.data.model.OrderStatus
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

    // Active merchant hotel to listen to
    private val _activeMerchantHotelId = MutableStateFlow<String?>(null)
    val activeMerchantHotelId: StateFlow<String?> = _activeMerchantHotelId.asStateFlow()

    private var toneGenerator: ToneGenerator? = null
    private var alarmJob: Job? = null
    private val repositoryScope = CoroutineScope(Dispatchers.Default)

    init {
        // Pre-populate with some realistic mock orders to make first-launch and merchant dashboard fully testable
        prepopulateMockOrders()
        
        // Start the automated alarm monitor
        startAlarmMonitor()
    }

    fun setActiveMerchantHotel(hotelId: String?) {
        _activeMerchantHotelId.value = hotelId
        Log.d(TAG, "Merchant active hotel set to: $hotelId")
    }

    fun setAlarmMuted(muted: Boolean) {
        _isAlarmMuted.value = muted
        Log.d(TAG, "Alarm muted state changed to: $muted")
    }

    fun placeOrder(order: Order) {
        val currentList = _orders.value.toMutableList()
        currentList.add(order)
        _orders.value = currentList
        Log.d(TAG, "Placed new order: ${order.id} for hotel: ${order.hotelId}")
        
        // When a new order is placed, unmute the alarm automatically to notify the merchant!
        _isAlarmMuted.value = false
    }

    fun updateOrderStatus(orderId: String, status: OrderStatus) {
        _orders.value = _orders.value.map { order ->
            if (order.id == orderId) {
                // If the order becomes prepared/ready, we might append an automated status message to chat
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
