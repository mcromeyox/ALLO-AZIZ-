package com.example.data.system

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.local.OrderEntity

/**
 * ExternalShareHelper
 *
 * Provides real Android Intents for:
 * 1. Opening WhatsApp with support or driver.
 * 2. Sharing order digital invoice / receipt via Android Sharesheet.
 * 3. Making phone calls via Android Dialer.
 */
object ExternalShareHelper {

    /**
     * Opens WhatsApp chat with a designated number (e.g. +212 600-123456)
     */
    fun openWhatsApp(context: Context, phoneE164: String, prefilledText: String) {
        val cleanPhone = phoneE164.replace("[^0-9+]".toRegex(), "")
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(prefilledText)}")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            // Fallback to SMS if WhatsApp isn't installed
            val smsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$cleanPhone")).apply {
                putExtra("sms_body", prefilledText)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(smsIntent)
            } catch (_: Exception) {
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
