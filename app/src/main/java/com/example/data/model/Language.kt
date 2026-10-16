package com.example.data.model

enum class AppLanguage(val code: String, val displayName: String, val isRtl: Boolean) {
    FRENCH("fr", "Français 🇫🇷", false),
    ENGLISH("en", "English 🇬🇧", false),
    ARABIC("ar", "العربية 🇲🇦", true)
}
