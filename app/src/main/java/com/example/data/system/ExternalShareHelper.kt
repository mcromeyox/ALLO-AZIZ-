package com.example.data.system

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.local.OrderEntity

/**
 * ExternalShareHelper
 *
 * Provides real Android Intents for:
 * 1. Opening WhatsApp with real Moroccan and international phone numbers.
 * 2. Sharing order digital invoice / receipt via Android Sharesheet.
 * 3. Making phone calls via Android Dialer.
 */
object ExternalShareHelper {

    private const val PREFS_NAME = "allo_aziz_whatsapp_prefs"
    private const val KEY_SAVED_WHATSAPP = "saved_whatsapp_phone"

    fun getSavedWhatsAppNumber(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SAVED_WHATSAPP, "") ?: ""
    }

    fun saveWhatsAppNumber(context: Context, phone: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SAVED_WHATSAPP, phone.trim()).apply()
    }

    /**
     * Checks if a phone number is an invalid dummy/placeholder.
     */
    fun isPlaceholderOrInvalid(phone: String): Boolean {
        val digits = phone.replace("[^0-9]".toRegex(), "")
        if (digits.length < 9) return true
        if (digits.contains("123456") || digits.contains("000000") || digits.contains("234567") || digits.contains("998877")) return true
        if (digits == "212600123456" || digits == "212661234567" || digits == "212600000001") return true
        return false
    }

    /**
     * Formats Moroccan and international phone numbers cleanly for WhatsApp.
     * WhatsApp URL scheme requires digits without '+' sign and with country code (e.g. 212612345678).
     */
    fun formatForWhatsApp(rawPhone: String): String {
        var digits = rawPhone.replace("[^0-9]".toRegex(), "")
        if (digits.startsWith("00")) {
            digits = digits.substring(2)
        }
        if (digits.startsWith("0") && digits.length >= 10) {
            digits = "212" + digits.substring(1)
        } else if ((digits.startsWith("6") || digits.startsWith("7")) && digits.length == 9) {
            digits = "212$digits"
        }
        return digits
    }

    /**
     * Opens WhatsApp chat with a designated number (e.g. 2126XXXXXXXX).
     */
    fun openWhatsApp(context: Context, phoneE164: String, prefilledText: String): Boolean {
        val cleanPhone = formatForWhatsApp(phoneE164)
        if (cleanPhone.length < 9) return false

        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(prefilledText)}")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            // Fallback to SMS if WhatsApp isn't installed
            val smsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$cleanPhone")).apply {
                putExtra("sms_body", prefilledText)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(smsIntent)
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    /**
     * Shares a clean, formatted receipt via Android Sharesheet to WhatsApp, Telegram, Gmail, etc.
     */
    fun shareOrderReceipt(context: Context, order: OrderEntity) {
        val receiptText = buildString {
            appendLine("🧾 *إيصال طلب - ألو عزيز (ALLO AZIZ)*")
            appendLine("━━━━━━━━━━━━━━━━━━━")
            appendLine("🔢 *رقم الطلب:* #${order.id}")
            appendLine("🏪 *المتجر:* ${order.storeName}")
            appendLine("📍 *عنوان التوصيل:* ${order.deliveryAddress}")
            appendLine("🛵 *الكابتن:* ${order.courierName}")
            appendLine("💳 *طريقة الدفع:* ${order.paymentMethod}")
            appendLine("━━━━━━━━━━━━━━━━━━━")
            appendLine("🛒 *الطلبات:*")
            appendLine(order.itemsSummary)
            appendLine("━━━━━━━━━━━━━━━━━━━")
            appendLine("💰 المجموع الفرعي: ${order.subtotal} درهم")
            if (order.discount > 0) {
                appendLine("🎁 الخصم: -${order.discount} درهم")
            }
            appendLine("🛵 رسوم التوصيل: ${order.deliveryFee} درهم")
            appendLine("💵 *الإجمالي النهائي:* ${order.total} درهم مغربي")
            appendLine("━━━━━━━━━━━━━━━━━━━")
            appendLine("شكراً لاختياركم ألو عزيز! تتبع فوري وتوصيل سريع في الدار البيضاء والمحمدية.")
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "إيصال طلبك #${order.id} - ألو عزيز")
            putExtra(Intent.EXTRA_TEXT, receiptText)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val chooser = Intent.createChooser(shareIntent, "مشاركة إيصال الطلب عبر...").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(chooser)
    }

    /**
     * Dials a phone number directly.
     */
    fun dialPhoneNumber(context: Context, phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${phoneNumber.trim()}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }
}
