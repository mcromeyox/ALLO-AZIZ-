package com.example.data.config

import com.example.data.model.AppLanguage

/**
 * Centralized Brand Configuration for VELORA ONE Super-App.
 * Keeps all brand names, slogans, contact channels, and AI identities
 * unified in a single authoritative source of truth.
 */
object BrandConfig {
    const val BRAND_NAME = "VELORA ONE"
    const val BRAND_SHORT = "VELORA"
    const val BRAND_TITLE_AR = "فيلورا ون"
    const val DOMAIN = "velora.one"
    const val SUPPORT_EMAIL = "support@velora.one"
    const val SUPPORT_PHONE = "+212 523-320000"
    const val WHATSAPP_SUPPORT = "+212661458920"
    const val HEADQUARTERS_CITY = "Mohammedia"
    const val HEADQUARTERS_COUNTRY = "Morocco"

    // AI Concierge & Voice Assistant Names
    const val AI_SHOPPING_AGENT_NAME = "Velora AI"
    const val AI_SUPPORT_AGENT_NAME = "Velora Care"

    fun slogan(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "فيلورا ون • تطبيق التوصيل السريع الذكي والسوق المتكامل في المحمدية والمغرب"
        AppLanguage.FRENCH -> "VELORA ONE • Super-App de livraison express & marketplace intelligent au Maroc"
        AppLanguage.ENGLISH -> "VELORA ONE • Smart express delivery & unified marketplace super-app in Morocco"
    }

    fun aiVoiceGreeting(lang: AppLanguage, storeName: String, priceText: String): String = when (lang) {
        AppLanguage.ARABIC -> "مرحباً بك في فيلورا ون! جهزت لك طلبك بكل سرور من $storeName بمجموع $priceText درهم مع التوصيل السريع المعتمد."
        AppLanguage.FRENCH -> "Bienvenue sur VELORA ONE ! J'ai bien préparé votre commande chez $storeName pour un montant vérifié de $priceText DH."
        AppLanguage.ENGLISH -> "Welcome to VELORA ONE! Your verified order from $storeName is ready for $priceText MAD."
    }

    fun legalTermsNotice(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تطبيق فيلورا ون خاضع للقوانين المغربية المعمول بها وقانون حماية المعطيات ذات الطابع الشخصي (القانون رقم 09-08 ولجنة CNDP). جميع المعاملات مشفرة ومؤمنة."
        AppLanguage.FRENCH -> "L'application VELORA ONE respecte la législation marocaine sur la protection des données personnelles (Loi 09-08 / CNDP). Transactions chiffrées et sécurisées."
        AppLanguage.ENGLISH -> "VELORA ONE complies with applicable Moroccan personal data protection legislation (Law 09-08 / CNDP). All transactions are encrypted and protected."
    }
}
