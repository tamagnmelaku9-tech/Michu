package com.example.data.repository

import android.content.Context
import android.util.Log

object PasscodeManager {
    private const val PREFS_NAME = "michu_passcodes_prefs"
    private const val CASHIER_PREFIX = "cashier_passcode_"
    private const val OWNER_PREFIX = "owner_passcode_"

    // Default unique passcodes for each hotel
    private val defaultCashierPasscodes = mapOf(
        "haile_resort" to "1111",
        "lewi_resort" to "2221",
        "ker_awud" to "3331",
        "central_hawassa" to "4441",
        "rori_hotel" to "5551",
        "eudora_hotel" to "6661",
        "gezahegn_elfenesh" to "7771",
        "plaza_hotel" to "8881",
        "south_star" to "9991",
        "tabor_castle" to "1011"
    )

    private val defaultOwnerPasscodes = mapOf(
        "haile_resort" to "1112",
        "lewi_resort" to "2222",
        "ker_awud" to "3332",
        "central_hawassa" to "4442",
        "rori_hotel" to "5552",
        "eudora_hotel" to "6662",
        "gezahegn_elfenesh" to "7772",
        "plaza_hotel" to "8882",
        "south_star" to "9992",
        "tabor_castle" to "1012"
    )

    fun getCashierPasscode(context: Context, hotelId: String): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(CASHIER_PREFIX + hotelId, defaultCashierPasscodes[hotelId] ?: "1234") ?: "1234"
    }

    fun getOwnerPasscode(context: Context, hotelId: String): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(OWNER_PREFIX + hotelId, defaultOwnerPasscodes[hotelId] ?: "5678") ?: "5678"
    }

    fun saveCashierPasscode(context: Context, hotelId: String, passcode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(CASHIER_PREFIX + hotelId, passcode).apply()
        Log.d("PasscodeManager", "Saved cashier passcode for $hotelId: $passcode")
    }

    fun saveOwnerPasscode(context: Context, hotelId: String, passcode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(OWNER_PREFIX + hotelId, passcode).apply()
        Log.d("PasscodeManager", "Saved owner passcode for $hotelId: $passcode")
    }
}
