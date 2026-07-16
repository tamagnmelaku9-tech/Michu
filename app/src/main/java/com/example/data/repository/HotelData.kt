package com.example.data.repository

import com.example.R
import com.example.data.model.BankDetails
import com.example.data.model.Hotel
import com.example.data.model.MenuItem

object HotelData {

    val HOTELS = listOf(
        Hotel(
            id = "haile_resort",
            name = "Haile Resort Hawassa",
            branchName = "Lakefront Main Branch",
            logoResId = R.drawable.img_app_icon,
            isClosed = false,
            prepTimeMinutes = 25,
            bankDetails = BankDetails(
                bankName = "CBE & Telebirr",
                accountHolder = "Haile Resorts Hawassa PLC",
                accountNumber = "CBE: 1000203495812 | Telebirr: 0911223344"
            )
        ),
        Hotel(
            id = "lewi_resort",
            name = "Lewi Resort & Spa",
            branchName = "Tabor Hill Branch",
            logoResId = R.drawable.img_app_icon,
            isClosed = false,
            prepTimeMinutes = 35,
            bankDetails = BankDetails(
                bankName = "CBE & Telebirr",
                accountHolder = "Lewi Resort Operations",
                accountNumber = "CBE: 1000192837465 | Telebirr: 0911556677"
            )
        ),
        Hotel(
            id = "ker_awud",
            name = "Ker Awud International",
            branchName = "Downtown Main Road",
            logoResId = R.drawable.img_app_icon,
            isClosed = false,
            prepTimeMinutes = 30,
            bankDetails = BankDetails(
                bankName = "Commercial Bank of Ethiopia",
                accountHolder = "Ker Awud Hotel Group",
                accountNumber = "CBE: 1000384950123"
            )
        ),
        Hotel(
            id = "central_hawassa",
            name = "Central Hawassa Hotel",
            branchName = "Piazza Business Center",
            logoResId = R.drawable.img_app_icon,
            isClosed = false,
            prepTimeMinutes = 20,
            bankDetails = BankDetails(
                bankName = "CBE & Telebirr",
                accountHolder = "Central Hawassa Hospitality",
                accountNumber = "CBE: 1000283749210 | Telebirr: 0912334455"
            )
        ),
        Hotel(
            id = "rori_hotel",
            name = "Rori Hotel Hawassa",
            branchName = "Rori Square",
            logoResId = R.drawable.img_app_icon,
            isClosed = false,
            prepTimeMinutes = 40,
            bankDetails = BankDetails(
                bankName = "Commercial Bank of Ethiopia",
                accountHolder = "Rori International PLC",
                accountNumber = "CBE: 1000495837261"
            )
        ),
        Hotel(
            id = "eudora_hotel",
            name = "Eudora Hotel Hawassa",
            branchName = "Cheshire Area",
            logoResId = R.drawable.img_app_icon,
            isClosed = true, // Set one hotel to closed for UI visual states testing
            prepTimeMinutes = 30,
            bankDetails = BankDetails(
                bankName = "CBE & Telebirr",
                accountHolder = "Eudora Hawassa Hotel",
                accountNumber = "CBE: 1000392847561 | Telebirr: 0911998877"
            )
        ),
        Hotel(
            id = "gezahegn_elfenesh",
            name = "G&E Resort Hawassa",
            branchName = "Lakefront Garden Branch",
            logoResId = R.drawable.img_app_icon,
            isClosed = false,
            prepTimeMinutes = 30,
            bankDetails = BankDetails(
                bankName = "CBE & Telebirr",
                accountHolder = "Gezahegn & Elfenesh Plc",
                accountNumber = "CBE: 1000584736152 | Telebirr: 0911883344"
            )
        ),
        Hotel(
            id = "plaza_hotel",
            name = "Plaza Hotel Hawassa",
            branchName = "Main Bus Station Area",
            logoResId = R.drawable.img_app_icon,
            isClosed = false,
            prepTimeMinutes = 15, // Ultra fast
            bankDetails = BankDetails(
                bankName = "Commercial Bank of Ethiopia",
                accountHolder = "Plaza Hawassa Cashier",
                accountNumber = "CBE: 1000394857102"
            )
        ),
        Hotel(
            id = "south_star",
            name = "South Star International",
            branchName = "Piazza Main Branch",
            logoResId = R.drawable.img_app_icon,
            isClosed = false,
            prepTimeMinutes = 45,
            bankDetails = BankDetails(
                bankName = "CBE & Telebirr",
                accountHolder = "South Star Int Hotel",
                accountNumber = "CBE: 1000293847120 | Telebirr: 0912112233"
            )
        ),
        Hotel(
            id = "tabor_castle",
            name = "Tabor Castle Hotel",
            branchName = "Tabor Hill View",
            logoResId = R.drawable.img_app_icon,
            isClosed = false,
            prepTimeMinutes = 30,
            bankDetails = BankDetails(
                bankName = "Commercial Bank of Ethiopia",
                accountHolder = "Tabor Castle Hotel Plc",
                accountNumber = "CBE: 1000495827361"
            )
        )
    )

    val MENU_ITEMS = listOf(
        // === Breakfast items ===
        MenuItem(
            id = "bf_chechebsa_haile",
            hotelId = "haile_resort",
            name = "Special Chechebsa",
            englishName = "Special Chechebsa",
            price = 280.00,
            category = "Breakfast",
            description = "Traditional shredded flatbread (Kita) lightly fried with pure spiced butter (Kibe) and hot pepper (Berbere). Served with honey and fresh yogurt."
        ),
        MenuItem(
            id = "bf_scrambled_haile",
            hotelId = "haile_resort",
            name = "Kibe Scrambled Eggs",
            englishName = "Kibe Scrambled Eggs",
            price = 220.00,
            category = "Breakfast",
            description = "Three country eggs scrambled with red onions, green hot peppers, fresh tomatoes, and cooked with rich Ethiopian spiced butter."
        ),
        MenuItem(
            id = "bf_firfir_haile",
            hotelId = "haile_resort",
            name = "Quanta Firfir",
            englishName = "Beef Jerky Firfir",
            price = 350.00,
            category = "Breakfast",
            description = "Sautéed dried beef jerky (Quanta) cooked in rich onions, garlic, berbere sauce, and tossed with shredded traditional Injera. Hearty and savory."
        ),

        // === Meat items ===
        MenuItem(
            id = "meat_shekla_haile",
            hotelId = "haile_resort",
            name = "Sizzling Shekla Tibs",
            englishName = "Sizzling Shekla Tibs",
            price = 580.00,
            category = "Meat",
            description = "Premium lean beef sautéed with onions, green chili peppers, rosemary, and exotic Ethiopian spices. Served bubbling hot on a traditional clay pot (Shekla) over charcoal."
        ),
        MenuItem(
            id = "meat_kitfo_haile",
            hotelId = "haile_resort",
            name = "Special Hawassa Kitfo",
            englishName = "Premium Beef Kitfo",
            price = 650.00,
            category = "Meat",
            description = "Finely minced extra-lean beef warmed in spiced clarified butter (Niter Kibe) and rich hot pepper (Mitmita). Served with Ayib (cottage cheese) and Gomen (collard greens)."
        ),

        // Menu for Lewi Resort
        MenuItem(
            id = "bf_chechebsa_lewi",
            hotelId = "lewi_resort",
            name = "Lewi Classic Chechebsa",
            englishName = "Lewi Classic Chechebsa",
            price = 260.00,
            category = "Breakfast",
            description = "Hand-shredded fresh wheat flatbread cooked with Ethiopian herbal butter and a touch of honey. Perfectly spiced."
        ),
        MenuItem(
            id = "meat_tibs_lewi",
            hotelId = "lewi_resort",
            name = "Dereko Tibs (Crispy Beef)",
            englishName = "Crispy Dried Beef Tibs",
            price = 520.00,
            category = "Meat",
            description = "Beef cubes sautéed dry with red onion, spicy green peppers, and served with a side of hot Senafich (mustard dip) and awaze."
        ),

        // Menu for Ker Awud
        MenuItem(
            id = "bf_foul_ker",
            hotelId = "ker_awud",
            name = "Special Hawassa Foul",
            englishName = "Spiced Fava Beans",
            price = 190.00,
            category = "Breakfast",
            description = "Crushed fava beans cooked with spices, garnished with chopped fresh onions, tomatoes, green peppers, spiced butter, and dollop of yogurt. Served with fresh bread."
        ),
        MenuItem(
            id = "meat_dulet_ker",
            hotelId = "ker_awud",
            name = "Hawassa Fish Dulet",
            englishName = "Hawassa Fish Dulet",
            price = 390.00,
            category = "Meat",
            description = "Hawassa's signature dish: finely chopped fresh lake Tilapia fish sautéed in spiced butter (Kibe), red onions, hot chilies, and cardamom spices."
        ),

        // Menu for Central Hawassa
        MenuItem(
            id = "bf_chechebsa_central",
            hotelId = "central_hawassa",
            name = "Piazza Honey Chechebsa",
            englishName = "Piazza Honey Chechebsa",
            price = 240.00,
            category = "Breakfast",
            description = "Rich chechebsa sweetened with pure local Hawassa forest honey and served with warm spiced milk or coffee."
        ),
        MenuItem(
            id = "meat_tibs_central",
            hotelId = "central_hawassa",
            name = "Zilzil Tibs (Strip Beef)",
            englishName = "Sautéed Beef Strips",
            price = 480.00,
            category = "Meat",
            description = "Long, elegant strips of tender beef sautéed with bell peppers, garlic, rosemary, and tossed in spicy awaze sauce."
        ),

        // Menu for Rori Hotel
        MenuItem(
            id = "bf_egg_rori",
            hotelId = "rori_hotel",
            name = "Special Cashier Scramble",
            englishName = "Special Cashier Scramble",
            price = 210.00,
            category = "Breakfast",
            description = "Eggs scrambled with diced tomatoes, onions, sliced chilies, and a touch of cardamom."
        ),
        MenuItem(
            id = "meat_shekla_rori",
            hotelId = "rori_hotel",
            name = "Rori Sizzler Shekla",
            englishName = "Rori Sizzler Shekla",
            price = 600.00,
            category = "Meat",
            description = "Rori's premium choice beef cubes seasoned with garlic, black pepper, and rosemary, served sizzling over charcoal clay stove."
        ),

        // Menu for G&E Resort
        MenuItem(
            id = "bf_firfir_ge",
            hotelId = "gezahegn_elfenesh",
            name = "Garden Injera Firfir",
            englishName = "Garden Injera Firfir",
            price = 300.00,
            category = "Breakfast",
            description = "Traditional Injera pieces cooked in a savory berbere chili, garlic, and fresh herb broth, finished with delicious spiced butter."
        ),
        MenuItem(
            id = "meat_gomen_ge",
            hotelId = "gezahegn_elfenesh",
            name = "Gomen Besiga",
            englishName = "Collard Greens with Beef",
            price = 490.00,
            category = "Meat",
            description = "Tender beef chunks simmered with collard greens (Gomen), onions, garlic, and fresh green peppers in herbal spiced butter."
        )
    )

    fun getHotels(): List<Hotel> = HOTELS

    fun getMenuForHotel(hotelId: String): List<MenuItem> {
        // Fallback: if a hotel has no menu items, generate some standard ones so that it's always populated!
        val items = MENU_ITEMS.filter { it.hotelId == hotelId }
        if (items.isEmpty()) {
            return listOf(
                MenuItem(
                    id = "bf_chechebsa_gen_$hotelId",
                    hotelId = hotelId,
                    name = "Standard Chechebsa",
                    englishName = "Standard Chechebsa",
                    price = 220.00,
                    category = "Breakfast",
                    description = "Shredded flatbread cooked with local herbal spiced butter and hot berbere, served with a hint of honey."
                ),
                MenuItem(
                    id = "meat_tibs_gen_$hotelId",
                    hotelId = hotelId,
                    name = "Sautéed Beef Tibs",
                    englishName = "Sautéed Beef Tibs",
                    price = 450.00,
                    category = "Meat",
                    description = "Fresh juicy beef chunks sautéed with onions, green chilies, garlic, and rosemary herbs. Extremely aromatic."
                )
            )
        }
        return items
    }
}
