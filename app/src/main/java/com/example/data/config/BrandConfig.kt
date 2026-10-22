package com.example.data.config

import com.example.data.model.AppLanguage

/**
 * Centralized Brand Configuration for WASLIXA Super-App.
 * Keeps all brand names, slogans, contact channels, and AI identities
 * unified in a single authoritative source of truth.
 */
object BrandConfig {
    const val BRAND_NAME = "WASLIXA"
    const val BRAND_SHORT = "WASLIXA"
    const val BRAND_TITLE_AR = "وصلـيكسا"
    const val DOMAIN = "waslixa.com"
    const val SUPPORT_EMAIL = "support@waslixa.com"
    const val SUPPORT_PHONE = "+212 523-320000"
    const val WHATSAPP_SUPPORT = "+212661458920"
    const val HEADQUARTERS_CITY = "Mohammedia"
    const val HEADQUARTERS_COUNTRY = "Morocco"

    // AI Concierge & Voice Assistant Names
    const val AI_SHOPPING_AGENT_NAME = "Waslixa AI"
    const val AI_SUPPORT_AGENT_NAME = "Waslixa Care"

    fun slogan(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "وصلـيكسا • كل ما تريده واصل عندك في دقائق في المحمدية والمغرب"
        AppLanguage.FRENCH -> "WASLIXA • Tout ce que vous aimez, livré en quelques minutes au Maroc"
        AppLanguage.ENGLISH -> "WASLIXA • Everything you crave, delivered in minutes in Morocco"
    }

    fun aiVoiceGreeting(lang: AppLanguage, storeName: String, priceText: String): String = when (lang) {
        AppLanguage.ARABIC -> "مرحباً بك في وصلـيكسا! جهزت لك طلبك بكل سرور من $storeName بمجموع $priceText درهم مع التوصيل السريع المعتمد."
        AppLanguage.FRENCH -> "Bienvenue sur WASLIXA ! J'ai bien préparé votre commande chez $storeName pour un montant vérifié de $priceText DH."
        AppLanguage.ENGLISH -> "Welcome to WASLIXA! Your verified order from $storeName is ready for $priceText MAD."
    }

    fun legalTermsNotice(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تطبيق وصلـيكسا خاضع للقوانين المغربية المعمول بها وقانون حماية المعطيات ذات الطابع الشخصي (القانون رقم 09-08 ولجنة CNDP). جميع المعاملات مشفرة ومؤمنة."
        AppLanguage.FRENCH -> "L'application WASLIXA respecte la législation marocaine sur la protection des données personnelles (Loi 09-08 / CNDP). Transactions chiffrées et sécurisées."
        AppLanguage.ENGLISH -> "WASLIXA complies with applicable Moroccan personal data protection legislation (Law 09-08 / CNDP). All transactions are encrypted and protected."
    }
}
