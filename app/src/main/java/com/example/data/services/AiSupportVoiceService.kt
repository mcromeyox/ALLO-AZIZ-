package com.example.data.services

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Service providing conversational AI responses for customer support.
 * Designed specifically for voice interaction: replies are concise, warm,
 * and spoken naturally aloud to the customer.
 */
object AiSupportVoiceService {

    private const val TAG = "AiSupportVoiceService"
    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(18, TimeUnit.SECONDS)
        .build()

    suspend fun generateSupportResponse(
        userPrompt: String,
        language: AppLanguage,
        activeOrderSummary: String? = null
    ): String = withContext(Dispatchers.IO) {
        val query = userPrompt.trim()
        if (query.isBlank()) {
            return@withContext when (language) {
                AppLanguage.FRENCH -> "Bonjour ! Je suis Sara de l'assistance ALLO AZIZ. Comment puis-je vous aider aujourd'hui ?"
                AppLanguage.ENGLISH -> "Hello! I am Sara from ALLO AZIZ 24/7 Support. How can I assist you today?"
                AppLanguage.ARABIC -> "أهلاً بك! أنا سارة من الدعم الفني لألو عزيز. كيف يمكنني مساعدتك اليوم؟"
            }
        }

        // 1. Try Gemini API if key is present
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        if (apiKey.isNotBlank() && !apiKey.contains("PLACEHOLDER") && !apiKey.contains("YourKey") && !apiKey.contains("DEFAULT_GEMINI")) {
            try {
                val geminiReply = callGeminiForSupport(query, language, apiKey, activeOrderSummary)
                if (!geminiReply.isNullOrBlank()) {
                    return@withContext cleanVoiceText(geminiReply)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini Support API failed, falling back to local support knowledge: ${e.message}")
            }
        }

        // 2. Intelligent, empathetic Moroccan Customer Support Knowledge Base (French prioritized)
        return@withContext matchLocalSupportKnowledge(query, language, activeOrderSummary)
    }

    private fun callGeminiForSupport(
        userQuery: String,
        language: AppLanguage,
        apiKey: String,
        activeOrderSummary: String?
    ): String? {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val langDirective = when (language) {
            AppLanguage.FRENCH -> "Répondez impérativement en Français chaleureux, professionnel et concis (1 à 2 phrases max, adapté à la lecture vocale orale)."
            AppLanguage.ENGLISH -> "Respond in English, warmly and concisely (1-2 sentences max, tailored for voice playback)."
            AppLanguage.ARABIC -> "أجب بالعربية المغربية أو الفصحى الودودة في جملة أو جملتين فقط مناسبتين للاستماع الصوتي."
        }

        val systemInstruction = """
            Vous êtes 'Sara', l'assistante vocale intelligente du support client ALLO AZIZ, service de livraison express à Mohammedia et Casablanca (Maroc).
            $langDirective
            Faits importants :
            - Livraison en 15 à 25 minutes par notre coursier Aziz sur moto.
            - Code promo du jour : AZIZ20 (-20% sur la commande).
            - Hotline gratuite : 0800-ALLO-AZIZ (0800 25 56 29).
            - En cas d'article manquant : réexpédition gratuite immédiate ou remboursement portefeuille en 1 minute.
            - Paiements acceptés : Carte bancaire marocaine (CMI, Visa, Mastercard), Espèces à la livraison, Portefeuille ALLO Pay.
            - Contexte commande active : ${activeOrderSummary ?: "Aucune commande en retard."}
            Gardez votre réponse naturelle pour être prononcée à voix haute par synthèse vocale. Pas de puces, pas de markdown, pas de titres.
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", "$systemInstruction\n\nQuestion du client : $userQuery"))
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
                put("maxOutputTokens", 180)
            })
        }

        val request = Request.Builder()
            .url(endpoint)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return null

        val responseString = response.body?.string() ?: return null
        val root = JSONObject(responseString)
        val candidates = root.optJSONArray("candidates") ?: return null
        if (candidates.length() == 0) return null

        val content = candidates.getJSONObject(0).optJSONObject("content") ?: return null
        val parts = content.optJSONArray("parts") ?: return null
        if (parts.length() == 0) return null

        return parts.getJSONObject(0).optString("text")
    }

    private fun matchLocalSupportKnowledge(
        query: String,
        language: AppLanguage,
        activeOrderSummary: String?
    ): String {
        val q = query.lowercase()

        // Order tracking / Where is courier Aziz?
        if (q.contains("où") || q.contains("ou est") || q.contains("commande") || q.contains("coursier") ||
            q.contains("aziz") || q.contains("livreur") || q.contains("أين") || q.contains("طلبي") ||
            q.contains("فين") || q.contains("where") || q.contains("track")
        ) {
            return when (language) {
                AppLanguage.FRENCH -> {
                    if (activeOrderSummary != null) {
                        "Votre coursier Aziz est en route avec votre commande ! Arrivée estimée dans moins de 10 minutes. Vous pouvez suivre sa moto en direct sur la carte."
                    } else {
                        "Votre coursier Aziz est actuellement sur le boulevard Hassan II. Il arrive très rapidement ! Vous pouvez suivre sa position en direct dans l'onglet 'Suivi de commande'."
                    }
                }
                AppLanguage.ENGLISH -> "Courier Aziz is on his way with your order! Estimated arrival is under 10 minutes. You can track his live scooter on the map."
                AppLanguage.ARABIC -> "المندوب عزيز في الطريق إليك وموقعه يبعد دقائق معدودة عن عنوانك بالمحمدية. يمكنك تتبعه مباشرة على الخريطة!"
            }
        }

        // Promo code / Discounts
        if (q.contains("promo") || q.contains("code") || q.contains("réduction") || q.contains("reduction") ||
            q.contains("remise") || q.contains("خصم") || q.contains("كود") || q.contains("discount")
        ) {
            return when (language) {
                AppLanguage.FRENCH -> "Bonne nouvelle ! Utilisez le code promo exclusif AZIZ20 dans votre panier pour bénéficier immédiatement de 20% de réduction sur votre commande !"
                AppLanguage.ENGLISH -> "Great news! Use the exclusive promo code AZIZ20 at checkout for an instant 20% discount on your entire order!"
                AppLanguage.ARABIC -> "خبر سار! استخدم كود الخصم الحصري AZIZ20 في سلتك للحصول على تخفيض فوري 20% على طلبك!"
            }
        }

        // Missing items / Problem with order / Refund
        if (q.contains("manque") || q.contains("oubli") || q.contains("problème") || q.contains("probleme") ||
            q.contains("froid") || q.contains("remboursement") || q.contains("مشكل") || q.contains("ناقص") ||
            q.contains("رجوع") || q.contains("missing") || q.contains("refund")
        ) {
            return when (language) {
                AppLanguage.FRENCH -> "Ne vous inquiétez pas, vous êtes 100% protégé ! Notre équipe réexpédie l'article manquant immédiatement et gratuitement, ou crédite votre portefeuille en une minute."
                AppLanguage.ENGLISH -> "Do not worry, you are 100% covered! We immediately dispatch the missing item for free, or credit your wallet within one minute."
                AppLanguage.ARABIC -> "لا تقلق أبداً، حقك مضمون 100%! سنرسل المندوب فوراً وبالمجان لاستكمال طلبك أو نعيد المبلغ لمحفظتك خلال دقيقة."
            }
        }

        // Payment methods / Bank card / Cash
        if (q.contains("paiement") || q.contains("payer") || q.contains("carte") || q.contains("espèce") ||
            q.contains("espece") || q.contains("cash") || q.contains("cmi") || q.contains("دفع") ||
            q.contains("بطاقة") || q.contains("فلوس") || q.contains("pay")
        ) {
            return when (language) {
                AppLanguage.FRENCH -> "Nous acceptons les cartes bancaires marocaines CMI, Visa, Mastercard, le portefeuille ALLO Pay, ainsi que le paiement en espèces à la livraison auprès du coursier."
                AppLanguage.ENGLISH -> "We accept Moroccan bank cards (CMI, Visa, Mastercard), ALLO Pay wallet, and Cash on Delivery directly with courier Aziz."
                AppLanguage.ARABIC -> "نقبل الدفع الآمن بالبطاقة البنكية، محفظة ALLO Pay، وكذلك الدفع نقداً عند الاستلام مع الكابتن عزيز."
            }
        }

        // Delivery speed / Delivery areas / Mohammedia
        if (q.contains("temps") || q.contains("délai") || q.contains("combien") || q.contains("zone") ||
            q.contains("livrez") || q.contains("mohammedia") || q.contains("وقت") || q.contains("كم يستغرق") ||
            q.contains("مناطق") || q.contains("speed") || q.contains("time")
        ) {
            return when (language) {
                AppLanguage.FRENCH -> "ALLO AZIZ livre partout à Mohammedia et ses environs en 15 à 25 minutes seulement, grâce à nos livreurs déployés en continu !"
                AppLanguage.ENGLISH -> "ALLO AZIZ delivers everywhere in Mohammedia in just 15 to 25 minutes, with dedicated couriers stationed across the city!"
                AppLanguage.ARABIC -> "التوصيل مع ألو عزيز يستغرق ما بين 15 إلى 25 دقيقة فقط في كافة أحياء المحمدية مع تتبع فوري مباشر!"
            }
        }

        // Become a driver / Courier onboarding
        if (q.contains("devenir") || q.contains("coursier") || q.contains("chauffeur") || q.contains("recrutement") ||
            q.contains("travail") || q.contains("سائق") || q.contains("تفعيل") || q.contains("شغل") ||
            q.contains("driver") || q.contains("job")
        ) {
            return when (language) {
                AppLanguage.FRENCH -> "Pour devenir coursier Aziz, soumettez votre permis et carte nationale dans l'onglet Chauffeur. Notre équipe support valide votre compte en 24h avec d'excellents revenus !"
                AppLanguage.ENGLISH -> "To become a courier, upload your driver license and ID in the Driver section. Our support team verifies and activates eligible accounts within 24 hours!"
                AppLanguage.ARABIC -> "للانضمام ككابتن توصيل مع عزيز، ارفع رخصة السياقة والبطاقة في قسم السائقين وسيقوم فريق الدعم باعتماد حسابك فوراً!"
            }
        }

        // Greetings
        if (q.contains("bonjour") || q.contains("salut") || q.contains("hello") || q.contains("hi") ||
            q.contains("salam") || q.contains("سلام") || q.contains("أهلا")
        ) {
            return when (language) {
                AppLanguage.FRENCH -> "Bonjour ! Je suis Sara, votre assistante IA du support client ALLO AZIZ. Que puis-je faire pour vous aujourd'hui ?"
                AppLanguage.ENGLISH -> "Hello! I am Sara, your AI support assistant at ALLO AZIZ. How can I help you today?"
                AppLanguage.ARABIC -> "أهلاً بك! أنا سارة من الدعم الفني الذكي لألو عزيز. كيف يمكنني مساعدتك في طلبك اليوم؟"
            }
        }

        // Default friendly response
        return when (language) {
            AppLanguage.FRENCH -> "J'ai bien noté votre demande ! Notre équipe support 24/7 veille à ce que votre expérience soit parfaite. N'hésitez pas si vous avez d'autres questions."
            AppLanguage.ENGLISH -> "I have noted your inquiry! Our 24/7 customer care team is here to ensure you have a seamless experience. Let me know if you need anything else."
            AppLanguage.ARABIC -> "سجلت استفسارك بعناية! فريق دعم ألو عزيز متواجد 24 ساعة لخدمتك وحل أي مسألة فوراً."
        }
    }

    private fun cleanVoiceText(raw: String): String {
        return raw.replace("*", "")
            .replace("#", "")
            .replace("`", "")
            .replace("\n\n", " ")
            .replace("\n", " ")
            .trim()
    }
}
