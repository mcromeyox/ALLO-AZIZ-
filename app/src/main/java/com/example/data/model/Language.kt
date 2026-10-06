package com.example.data.model

enum class AppLanguage(val code: String, val displayName: String, val isRtl: Boolean) {
    ARABIC("ar", "العربية 🇲🇦", true),
    FRENCH("fr", "Français 🇫🇷", false),
    ENGLISH("en", "English 🇬🇧", false)
}
