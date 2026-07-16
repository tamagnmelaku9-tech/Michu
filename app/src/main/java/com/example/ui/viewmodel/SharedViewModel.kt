package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CartItem
import com.example.data.model.Hotel
import com.example.data.model.MenuItem
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.repository.HotelData
import com.example.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SharedViewModel : ViewModel() {

    private val _selectedHotel = MutableStateFlow<Hotel?>(null)
    val selectedHotel: StateFlow<Hotel?> = _selectedHotel.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _latestPlacedOrderId = MutableStateFlow<String?>(null)
    val latestPlacedOrderId: StateFlow<String?> = _latestPlacedOrderId.asStateFlow()

    val totalCartPrice: StateFlow<Double> = MutableStateFlow(0.0).apply {
        viewModelScope.launch {
            _cartItems.collect { items ->
                value = items.sumOf { it.totalPrice }
            }
        }
    }

    val cartItemCount: StateFlow<Int> = MutableStateFlow(0).apply {
        viewModelScope.launch {
            _cartItems.collect { items ->
                value = items.sumOf { it.quantity }
            }
        }
    }

    fun selectHotel(hotelId: String) {
        val hotel = HotelData.getHotels().find { it.id == hotelId }
        if (_selectedHotel.value?.id != hotelId) {
            // Warn or automatically clear cart when switching hotels
            _cartItems.value = emptyList()
        }
        _selectedHotel.value = hotel
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun addToCart(
        menuItem: MenuItem,
        quantity: Int,
        injeraType: String,
        spicyLevel: String
    ) {
        val current = _cartItems.value.toMutableList()
        // Check if an identical customized item is already in the cart, if so, increase quantity
        val existingIndex = current.indexOfFirst {
            it.menuItem.id == menuItem.id &&
                    it.injeraType == injeraType &&
                    it.spicyLevel == spicyLevel
        }

        if (existingIndex != -1) {
            val existing = current[existingIndex]
            current[existingIndex] = existing.copy(quantity = existing.quantity + quantity)
        } else {
            current.add(
                CartItem(
                    id = "cart_item_${System.currentTimeMillis()}",
                    menuItem = menuItem,
                    quantity = quantity,
                    injeraType = injeraType,
                    spicyLevel = spicyLevel
                )
            )
        }
        _cartItems.value = current
    }

    fun updateCartItemQuantity(cartItemId: String, newQuantity: Int) {
        if (newQuantity <= 0) {
            _cartItems.value = _cartItems.value.filter { it.id != cartItemId }
        } else {
            _cartItems.value = _cartItems.value.map {
                if (it.id == cartItemId) it.copy(quantity = newQuantity) else it
            }
        }
    }

    fun checkout(customerName: String, customerPhone: String, pickupTime: String): String? {
        val hotel = _selectedHotel.value ?: return null
        val items = _cartItems.value
        if (items.isEmpty()) return null

        val subtotal = items.sumOf { it.totalPrice }
        val orderId = "order_${System.currentTimeMillis().toString().takeLast(4)}"

        val newOrder = Order(
            id = orderId,
            hotelId = hotel.id,
            customerName = customerName,
            customerPhone = customerPhone,
            pickupTime = pickupTime,
            items = items,
            subtotal = subtotal,
            total = subtotal, // In this model total = subtotal
            paymentStatus = "pending",
            orderStatus = OrderStatus.PENDING,
            chatMessages = listOf(
                com.example.data.model.ChatMessage(
                    id = "chat_welcome_${System.currentTimeMillis()}",
                    sender = "merchant",
                    message = "Selam, $customerName! We received your pre-order. Please transfer ETB $subtotal to our ${hotel.bankDetails.bankName} account and attach the payment receipt screenshot below to begin preparation.",
                    timestamp = System.currentTimeMillis()
                )
            )
        )

        // Submit order to order repository
        OrderRepository.placeOrder(newOrder)

        // Clear cart and update latest order ID for navigation
        _cartItems.value = emptyList()
        _latestPlacedOrderId.value = orderId

        return orderId
    }

    fun resetLatestOrderId() {
        _latestPlacedOrderId.value = null
    }
}
