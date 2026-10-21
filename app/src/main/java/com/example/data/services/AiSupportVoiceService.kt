package com.example.data.services

import com.example.data.model.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Conservative offline support replies.
 *
 * Do not put API credentials in the mobile app. AI-backed support should be connected
 * through a trusted backend, and operational claims must come from verified order/service data.
 */
object AiSupportVoiceService {
    suspend fun generateSupportResponse(
        userPrompt: String,
        language: AppLanguage,
        activeOrderSummary: String? = null
    ): String = withContext(Dispatchers.Default) {
        val query = userPrompt.trim().lowercase()
        if (query.isBlank()) {
            return@withContext when (language) {
                AppLanguage.FRENCH -> "Bonjour, je suis l'assistante DELIVIO. Comment puis-je vous aider ?"
                AppLanguage.ENGLISH -> "Hello, I am the DELIVIO assistant. How can I help?"
                AppLanguage.ARABIC -> "أهلاً بك، أنا مساعدة ديليفيو. كيف يمكنني مساعدتك؟"
            }
        }

        when {
            listOf("commande", "order", "tracking", "track", "فين", "طلبي", "أين").any(query::contains) ->
                when (language) {
                    AppLanguage.FRENCH -> "Je ne peux pas confirmer la position ou l'heure d'arrivée sans données de suivi vérifiées. Consultez le suivi affiché dans votre commande ou contactez le support."
                    AppLanguage.ENGLISH -> "I cannot confirm a location or arrival time without verified tracking data. Please check the tracking shown in your order or contact support."
                    AppLanguage.ARABIC -> "لا يمكنني تأكيد موقع المندوب أو وقت الوصول دون بيانات تتبع موثوقة. تحقق من صفحة الطلب أو تواصل مع الدعم."
                }
            listOf("remboursement", "refund", "missing", "manque", "ناقص", "استرجاع", "تعويض").any(query::contains) ->
                when (language) {
                    AppLanguage.FRENCH -> "Je suis désolée pour ce problème. Une demande de remboursement ou de remplacement doit être vérifiée par l'équipe support ; je ne peux pas confirmer qu'elle est approuvée."
                    AppLanguage.ENGLISH -> "I'm sorry about the issue. A refund or replacement request must be reviewed by support; I cannot confirm approval here."
                    AppLanguage.ARABIC -> "نأسف لهذه المشكلة. يجب أن يراجع فريق الدعم طلب الاسترجاع أو التعويض، ولا يمكنني تأكيد الموافقة عليه هنا."
                }
            listOf("paiement", "payment", "carte", "cash", "دفع", "بطاقة", "فلوس").any(query::contains) ->
                when (language) {
                    AppLanguage.FRENCH -> "Les moyens de paiement disponibles doivent être confirmés à l'écran de paiement. Ne partagez jamais votre code PIN ni le code de sécurité de votre carte dans le chat."
                    AppLanguage.ENGLISH -> "Available payment methods should be confirmed at checkout. Never share your PIN or card security code in chat."
                    AppLanguage.ARABIC -> "تحقق من وسائل الدفع المعروضة عند إتمام الطلب. لا تشارك أبداً الرقم السري أو رمز أمان البطاقة في المحادثة."
                }
            listOf("promo", "discount", "réduction", "خصم", "كود").any(query::contains) ->
                when (language) {
                    AppLanguage.FRENCH -> "Les promotions ne sont confirmées que si elles sont affichées et validées dans le panier."
                    AppLanguage.ENGLISH -> "A promotion is confirmed only when it is displayed and validated in the cart."
                    AppLanguage.ARABIC -> "يُعتمد العرض فقط إذا ظهر وتم التحقق منه داخل سلة التسوق."
                }
            else ->
                when (language) {
                    AppLanguage.FRENCH -> "Je peux vous orienter, mais je ne peux pas confirmer un état de commande, un remboursement, un délai ou un service qui n'est pas vérifié dans l'application. Veuillez contacter le support pour une vérification."
                    AppLanguage.ENGLISH -> "I can offer guidance, but I cannot confirm unverified order status, refunds, delivery times, or services. Please contact support for verification."
                    AppLanguage.ARABIC -> "يمكنني إرشادك، لكن لا أستطيع تأكيد حالة طلب أو استرجاع أو مدة توصيل أو خدمة دون بيانات موثوقة. تواصل مع الدعم للتحقق."
                }
        }
    }
}
