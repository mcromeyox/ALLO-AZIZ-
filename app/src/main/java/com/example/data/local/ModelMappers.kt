package com.example.data.local

import com.example.data.model.CatalogData
import com.example.data.model.Product
import com.example.data.model.Store
import com.example.data.model.StoreCategory

fun StoreEntity.toStore(): Store {
    val cat = StoreCategory.values().firstOrNull { it.id.equals(category, ignoreCase = true) } ?: StoreCategory.ALL
    return Store(
        id = id,
        name = name,
        nameAr = nameAr,
        nameFr = nameFr,
        category = cat,
        rating = rating,
        reviewCount = reviewCount,
        deliveryTimeMins = deliveryTimeMins,
        deliveryFee = deliveryFee,
        minOrder = minOrder,
        address = address,
        emoji = emoji,
        isFeatured = isFeatured,
        badge = badge,
        isOpen = isOpen,
        bannerUrl = bannerUrl,
        distanceKm = distanceKm
    )
}

fun ProductEntity.toProduct(): Product {
    val cat = StoreCategory.values().firstOrNull { it.id.equals(category, ignoreCase = true) } ?: StoreCategory.FAST_FOOD
    val options = if (cat == StoreCategory.BURGER || nameEn.contains("Burger", ignoreCase = true)) {
        CatalogData.burgerOptions
    } else {
        emptyList()
    }
    return Product(
        id = id,
        storeId = storeId,
        storeName = storeName,
        nameEn = nameEn,
        nameAr = nameAr,
        nameFr = nameFr,
        descriptionEn = descriptionEn,
        descriptionAr = descriptionAr,
        descriptionFr = descriptionFr,
        price = price,
        category = cat,
        emoji = emoji,
        rating = rating,
        isPopular = isPopular,
        calories = calories,
        isAvailable = isAvailable,
        imageUrl = imageUrl,
        optionGroups = options
    )
}
