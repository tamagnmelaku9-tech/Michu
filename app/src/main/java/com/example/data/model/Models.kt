package com.example.data.model

import java.io.Serializable

data class BankDetails(
    val bankName: String,
    val accountHolder: String,
    val accountNumber: String
) : Serializable

data class Hotel(
    val id: String,
    val name: String,
    val branchName: String,
    val logoUrl: String = "",
    val logoResId: Int? = null,
    val isClosed: Boolean = false,
    val prepTimeMinutes: Int = 30,
    val bankDetails: BankDetails
) : Serializable

data class MenuItem(
    val id: String,
    val hotelId: String,
    val name: String,
    val englishName: String,
    val price: Double,
    val category: String, // "Breakfast" | "Meat" | "All"
    val image: String = "",
    val imageResId: Int? = null,
    val description: String
) : Serializable

data class CartItem(
    val id: String,
    val menuItem: MenuItem,
    val quantity: Int,
    val injeraType: String, // "Standard Injera" | "Teff Injera"
    val spicyLevel: String, // "Mild" | "Medium" | "Extra Spicy"
    val selectedAddons: List<String> = emptyList()
) : Serializable {
    val totalPrice: Double
        get() {
            var itemPrice = menuItem.price
            if (injeraType == "Teff Injera") {
                itemPrice += 15.0 // Teff injera addition price
            }
            return itemPrice * quantity
        }
}

enum class OrderStatus(val displayName: String) {
    PENDING("Pending"),
    PREPARING("Preparing"),
    READY("Ready"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

data class ChatMessage(
    val id: String,
    val sender: String, // "customer" | "merchant"
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUrl: String? = null // For attached receipt screenshots
) : Serializable

data class Order(
    val id: String,
    val hotelId: String,
    val customerName: String,
    val customerPhone: String,
    val pickupTime: String,
    val items: List<CartItem>,
    val subtotal: Double,
    val total: Double,
    val paymentStatus: String = "pending", // "pending" | "paid"
    val orderStatus: OrderStatus = OrderStatus.PENDING,
    val chatMessages: List<ChatMessage> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val paymentReceiptImageUri: String? = null
) : Serializable
