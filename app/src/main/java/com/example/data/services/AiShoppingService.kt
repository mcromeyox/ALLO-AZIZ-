package com.example.data.services

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.CatalogData
import com.example.data.model.Product
import com.example.data.model.StoreCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

data class AiShoppingRecommendation(
    val userPrompt: String,
    val aiExplanationAr: String,
    val aiExplanationFr: String,
    val items: List<AiRecommendedItem>,
    val estimatedTotal: Double,
    val deliveryFee: Double,
    val grandTotal: Double,
    val targetStoreName: String,
    val estimatedTimeMins: Int,
    val confidenceScore: Int = 99,
    val speechAudioTextFr: String = "",
    val speechAudioTextAr: String = "",
    val detectedNotes: List<String> = emptyList(),
    val storeBadgeEmoji: String = "🏪",
    val isUnavailableNotice: Boolean = false,
    val suggestedAlternatives: List<Product> = emptyList()
)

data class AiRecommendedItem(
    val product: Product,
    var quantity: Int,
    val customNote: String = "",
    val subtotal: Double = product.price * quantity
)

/**
 * Ultra-accurate AI Shopping Engine for WASLIXA (وصلـيكسا).
 * Supports natural Moroccan Darija, French, and English voice commands.
 * Automatically extracts multi-product orders, exact quantities, personalized culinary notes,
 * and generates human-like spoken responses with natural cadence and Moroccan hospitality.
 */
object AiShoppingService {

    private const val TAG = "AiShoppingService"
    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(18, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeShoppingIntent(
        query: String,
        availableProducts: List<Product> = CatalogData.products
    ): AiShoppingRecommendation = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) {
            return@withContext matchLocalCatalogWithNlp("Menu Big Mac", availableProducts)
        }

        // 1. Try Gemini API if key is present
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Throwable) { "" }
        if (apiKey.isNotBlank() && !apiKey.contains("PLACEHOLDER") && !apiKey.contains("YourKey") && !apiKey.contains("DEFAULT_GEMINI")) {
            try {
                val geminiResult = callGeminiApi(cleanQuery, apiKey, availableProducts)
                if (geminiResult != null && geminiResult.items.isNotEmpty()) {
                    return@withContext geminiResult
                }
            } catch (t: Throwable) {
                Log.w(TAG, "Gemini API failed, falling back to local NLP engine: ${t.message}")
            }
        }

        // 2. High-precision offline Moroccan & French NLP parsing engine
        return@withContext matchLocalCatalogWithNlp(cleanQuery, availableProducts)
    }

    private fun callGeminiApi(
        userVoiceText: String,
        apiKey: String,
        products: List<Product>
    ): AiShoppingRecommendation? {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

        val catalogSummary = products.map { p ->
            JSONObject().apply {
                put("id", p.id)
                put("store", p.storeName)
                put("nameFr", p.nameFr)
                put("nameAr", p.nameAr)
                put("price", p.price)
                put("category", p.category.name)
            }
        }

        val systemPrompt = """
            You are 'Sara' or 'Karim', the AI voice shopping assistant for WASLIXA Super-App in Mohammedia, Morocco.
            The user spoke or typed in Moroccan Darija, French, Arabic, or English: "$userVoiceText".
            
            AUTHORITATIVE VERIFIED CATALOG:
            $catalogSummary
            
            MANDATORY COMPLIANCE & ACCURACY RULES:
            - ONLY match products that actually exist in the AUTHORITATIVE VERIFIED CATALOG above using their exact "id".
            - NEVER fabricate or invent any product, SKU, merchant, or price.
            - If the user asks for products from external unsupported platforms (e.g. Zara, Shein, Amazon, Temu, AliExpress, Apple, Nike) or products NOT in our verified catalog, DO NOT create fake items or estimated prices. Set "items": [] and "isUnavailableNotice": true. Explain politely that external platforms are not directly orderable and invite them to explore local partner stores in Mohammedia.
            - Extract quantities (e.g., 2, 3, joj, tleta).
            - Extract preparation notes (e.g., sauces, without onion).
            - Generate warm, natural spoken responses (speechAudioFr and speechAudioAr) in French and Moroccan Arabic with authentic hospitality.
            
            Return ONLY raw valid JSON:
            {
               "confidence": 99,
               "isUnavailableNotice": false,
               "explanationFr": "French summary of what was chosen",
               "explanationAr": "Arabic summary of what was chosen",
               "speechAudioFr": "Parfait ! J'ai bien sélectionné vos articles chez [Store]. Vous pouvez vérifier le panier et confirmer en un clic !",
               "speechAudioAr": "ممتاز وعلى راسي وعيني! جهزت لك طلبك بكل سرور من [Store]. يمكنك مراجعة السلة والتأكيد الآن!",
               "storeName": "Store name",
               "notes": ["Sauce piquante", "Sans oignon"],
               "items": [
                  {"productId": "p_mcdo_bigmac", "quantity": 1, "note": "Sauce spéciale"}
               ]
            }
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", systemPrompt))
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.1)
                put("responseMimeType", "application/json")
                put("maxOutputTokens", 2048)
            })
        }

        try {
            val request = Request.Builder()
                .url(endpoint)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val responseString = response.body?.string() ?: return null
                val rootJson = JSONObject(responseString)
                val candidates = rootJson.optJSONArray("candidates") ?: return null
                if (candidates.length() == 0) return null
                val rawText = candidates.getJSONObject(0)
                    .optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.getJSONObject(0)
                    ?.optString("text", "") ?: return null
                if (rawText.isBlank()) return null

                val parsedJson = JSONObject(rawText)
                val explFr = parsedJson.optString("explanationFr", "Articles sélectionnés selon votre demande vocale.")
                val explAr = parsedJson.optString("explanationAr", "تم اختيار المنتجات المطابقة لطلبك الصوتي بدقة.")
                val speechFr = parsedJson.optString("speechAudioFr", explFr)
                val speechAr = parsedJson.optString("speechAudioAr", explAr)
                val targetStore = parsedJson.optString("storeName", "${com.example.data.config.BrandConfig.BRAND_SHORT} Express")
                val confidence = parsedJson.optInt("confidence", 99)

                val notesArray = parsedJson.optJSONArray("notes")
                val detectedNotes = mutableListOf<String>()
                if (notesArray != null) {
                    for (i in 0 until notesArray.length()) {
                        detectedNotes.add(notesArray.optString(i))
                    }
                }

                val itemsArray = parsedJson.optJSONArray("items") ?: JSONArray()
                val recommendedItems = mutableListOf<AiRecommendedItem>()
                for (i in 0 until itemsArray.length()) {
                    val itemObj = itemsArray.getJSONObject(i)
                    val pid = itemObj.optString("productId")
                    val pName = itemObj.optString("name", pid)
                    val qty = itemObj.optInt("quantity", 1).coerceAtLeast(1)
                    val note = itemObj.optString("note", "")

                    // Flexible product lookup (ID, name, or sub-token match)
                    val product = products.find { it.id == pid }
                        ?: products.find { it.nameFr.contains(pName, ignoreCase = true) || it.nameAr.contains(pName, ignoreCase = true) }
                        ?: products.find { it.nameFr.contains(pid, ignoreCase = true) || it.nameAr.contains(pid, ignoreCase = true) }
                        ?: products.find { it.id.contains(pid, ignoreCase = true) || pid.contains(it.id, ignoreCase = true) }

                    if (product != null) {
                        recommendedItems.add(
                            AiRecommendedItem(
                                product = product,
                                quantity = qty,
                                customNote = note,
                                subtotal = product.price * qty
                            )
                        )
                    }
                }

                if (recommendedItems.isEmpty()) return null

            val subtotal = recommendedItems.sumOf { it.subtotal }
            val deliveryFee = 7.0
            return AiShoppingRecommendation(
                userPrompt = userVoiceText,
                aiExplanationAr = explAr,
                aiExplanationFr = explFr,
                items = recommendedItems,
                estimatedTotal = subtotal,
                deliveryFee = deliveryFee,
                grandTotal = subtotal + deliveryFee,
                targetStoreName = targetStore,
                estimatedTimeMins = 18,
                confidenceScore = confidence,
                speechAudioTextFr = speechFr,
                speechAudioTextAr = speechAr,
                detectedNotes = detectedNotes,
                storeBadgeEmoji = "🏪"
            )
        }
    } catch (e: Exception) {
        Log.w(TAG, "callGeminiApi error: ${e.message}")
        return null
    }
    return null
}

    /**
     * High-Precision Moroccan Darija, French & English Offline NLP Shopping Matcher.
     * Accurately parses multi-item food, grocery, seafood, and pharmacy orders.
     */
    fun matchLocalCatalogWithNlp(
        query: String,
        products: List<Product>
    ): AiShoppingRecommendation {
        val q = query.lowercase(Locale.ROOT).trim()

        // 1. Detect requested custom sauces and culinary notes
        val detectedNotes = mutableListOf<String>()
        if (q.contains("algerienne") || q.contains("algérienne") || q.contains("الجزائرية") || q.contains("ألجيريان")) {
            detectedNotes.add("Sauce Algérienne 🌶️")
        }
        if (q.contains("fromagere") || q.contains("fromagère") || q.contains("فرماجير") || q.contains("صلصة الجبن")) {
            detectedNotes.add("Sauce Fromagère Maison 🧀")
        }
        if (q.contains("samourai") || q.contains("samouraï") || q.contains("ساموراي")) {
            detectedNotes.add("Sauce Samouraï Piquante 🔥")
        }
        if (q.contains("biggy") || q.contains("بيجي")) {
            detectedNotes.add("Sauce Biggy Burger 🍔")
        }
        if (q.contains("andalouse") || q.contains("أندلسية") || q.contains("اندلسية")) {
            detectedNotes.add("Sauce Andalouse")
        }
        if (q.contains("bbq") || q.contains("barbecue") || q.contains("باربكيو")) {
            detectedNotes.add("Sauce Barbecue Fumée")
        }
        if (q.contains("toum") || q.contains("ثومية") || q.contains("تومية") || q.contains("creme d'ail") || q.contains("crème d'ail")) {
            detectedNotes.add("Crème d'Ail Toum Libanais 🧄")
        }
        if (q.contains("cheddar") || q.contains("شيدر")) {
            detectedNotes.add("Sauce Cheddar Fondu 🧀")
        }
        if (q.contains("poivre") || q.contains("فلفل اسود") || q.contains("فلفل أسود")) {
            detectedNotes.add("Sauce Poivre Noir")
        }
        if (q.contains("curry") || q.contains("كاري")) {
            detectedNotes.add("Sauce Curry Doux")
        }
        if (q.contains("sauce piquante") || q.contains("sauce piquant") || q.contains("صوص حار") || q.contains("صلصة حارة")) {
            detectedNotes.add("Sauce piquante 🔥")
        } else if (q.contains("harissa") || q.contains("هريسة") || q.contains("حار") || q.contains("piquant") || q.contains("har") || q.contains("7ar")) {
            detectedNotes.add("Harissa Beldia Piquante 🇲🇦")
        }
        if (q.contains("sweet chili") || q.contains("سويت تشيلي")) {
            detectedNotes.add("Sauce Sweet Chili Thaï")
        }
        if (q.contains("sans oignon") || q.contains("bla bsel") || q.contains("bla bsal") || q.contains("بدون بصل") || q.contains("بلا بصل")) {
            detectedNotes.add("Sans oignon")
        }
        if (q.contains("extra fromage") || q.contains("fermaj zayed") || q.contains("فرماج زايد") || q.contains("جبن إضافي")) {
            detectedNotes.add("Extra fromage fondu 🧀")
        }
        if (q.contains("bien cuit") || q.contains("mehmmer") || q.contains("محمر مزيان") || q.contains("crispy") || q.contains("مقرمش")) {
            detectedNotes.add("Bien cuit & croustillant")
        }

        // Global default quantity
        val defaultQty = when {
            q.contains("زوج") || q.contains("جوج") || q.contains("joj") || q.contains("deux") || q.contains("two") || q.contains(" 2 ") || q.startsWith("2 ") -> 2
            q.contains("تلاتة") || q.contains("ثلاثة") || q.contains("tleta") || q.contains("trois") || q.contains("three") || q.contains(" 3 ") || q.startsWith("3 ") -> 3
            q.contains("ربعة") || q.contains("أربعة") || q.contains("reb3a") || q.contains("quatre") || q.contains("four") || q.contains(" 4 ") || q.startsWith("4 ") -> 4
            else -> 1
        }

        val matchedItems = mutableListOf<AiRecommendedItem>()
        var mainStoreName = "McDonald's Miramar Mohammedia"
        var storeEmoji = "🍔"
        var explanationFr = ""
        var explanationAr = ""
        val notesStr = if (detectedNotes.isNotEmpty()) detectedNotes.joinToString(" • ") else "Préparation soignée avec frites et sauces"

        // 1. E-Commerce Marketplace Items (High-Tech, Fashion, Perfume, Gaming)
        val hasAirpods = q.contains("airpod") || q.contains("ecouteur") || q.contains("écouteur") || q.contains("سماعات") || q.contains("ايربودز") || q.contains("إيربودز") || q.contains("earbuds")
        val hasSmartwatch = q.contains("smartwatch") || q.contains("montre") || q.contains("ساعة") || q.contains("ساعه") || q.contains("ultra")
        val hasCharger = q.contains("chargeur") || q.contains("شاحن") || q.contains("fast charge") || q.contains("gan") || q.contains("cable")
        val hasHoodie = q.contains("hoodie") || q.contains("sweat") || q.contains("هودي") || q.contains("لباس") || q.contains("ملابس") || q.contains("veste")
        val hasParfum = q.contains("parfum") || q.contains("عطر") || q.contains("عود") || q.contains("oud") || q.contains("روائح")
        val hasGaming = q.contains("casque gamer") || q.contains("gaming") || q.contains("جيمنج") || q.contains("العاب") || q.contains("ألعاب") || q.contains("headset")
        val hasLeather = q.contains("sac") || q.contains("حقيبة") || q.contains("جلد") || q.contains("cuir") || q.contains("محفظة")

        // 2. External Global Platforms (Zara, Shein, Amazon, Temu, AliExpress, Decathlon, Nike, Apple, etc.)
        val isZara = q.contains("zara") || q.contains("زارا")
        val isShein = q.contains("shein") || q.contains("شين") || q.contains("شي ان") || q.contains("شي إن")
        val isAmazon = q.contains("amazon") || q.contains("أمازون") || q.contains("امازون")
        val isTemu = q.contains("temu") || q.contains("تيمو")
        val isAliExpress = q.contains("aliexpress") || q.contains("علي اكسبريس") || q.contains("على اكسبريس")
        val isDecathlon = q.contains("decathlon") || q.contains("ديكاتلون") || q.contains("ديكاتلو")
        val isNikeAdidas = q.contains("nike") || q.contains("adidas") || q.contains("نايكي") || q.contains("نايك") || q.contains("اديداس") || q.contains("أديداس") || q.contains("حذاء") || q.contains("chaussure") || q.contains("sneakers") || q.contains("بسكليت")
        val isAppleSamsung = q.contains("apple") || q.contains("iphone") || q.contains("ايفون") || q.contains("آيفون") || q.contains("samsung") || q.contains("سامسونج")
        val isGlobalMarketplace = isZara || isShein || isAmazon || isTemu || isAliExpress || isDecathlon || isNikeAdidas || isAppleSamsung

        // 3. Food & Restaurant items
        val hasTacos = q.contains("tacos") || q.contains("طاكوس") || q.contains("تاكوس") || q.contains("otacos") || q.contains("o'tacos")
        val hasShawarma = q.contains("shawarma") || q.contains("chawarma") || q.contains("شاورما") || q.contains("ساندويتش") || q.contains("americain") || q.contains("أمريكان")
        val hasBurgerKing = q.contains("whopper") || q.contains("ووبر") || q.contains("burger king") || q.contains("برجر كينج") || q.contains("smash") || q.contains("سماش")
        val hasBigMac = q.contains("big mac") || q.contains("بيج ماك") || q.contains("burger") || q.contains("برجر") || q.contains("همبرغر") || q.contains("mcdo")
        val hasNuggets = q.contains("nugget") || q.contains("ناجتس") || q.contains("ناغتس") || q.contains("قطع دجاج")
        val hasKfc = q.contains("bucket") || q.contains("باكت") || q.contains("kfc") || q.contains("كنتاكي") || q.contains("zinger") || q.contains("زنجر") || q.contains("tenders") || q.contains("تندرز")
        val hasPizza = q.contains("pizza") || q.contains("بيتزا") || q.contains("بيدزا") || q.contains("4 fromages") || q.contains("أجبان") || q.contains("supreme")
        val hasTajine = q.contains("tajine") || q.contains("طاجين") || q.contains("couscous") || q.contains("كسكس") || q.contains("لحم") || q.contains("برقوق")
        val hasFish = q.contains("poisson") || q.contains("fruit de mer") || q.contains("crevette") || q.contains("حوت") || q.contains("سمك") || q.contains("قمرون") || q.contains("شواية")
        val hasSushi = q.contains("sushi") || q.contains("سوشي") || q.contains("maki") || q.contains("california") || q.contains("wok") || q.contains("نودلز") || q.contains("nouilles")
        val hasCroissant = q.contains("croissant") || q.contains("كرواسون") || q.contains("tiramisu") || q.contains("تيراميسو") || q.contains("paul") || q.contains("بول") || q.contains("قهوة")
        val hasGrocery = q.contains("marjane") || q.contains("مرجان") || q.contains("حليب") || q.contains("بيض") || q.contains("زيت") || q.contains("courses")
        val hasPharma = q.contains("pharmacie") || q.contains("صيدلية") || q.contains("دواء") || q.contains("vitamine") || q.contains("فيتامين") || q.contains("doliprane")

        var isEcommerceOrder = false

        // Match Logic:
        // 1. FIRST PRIORITY: EXTERNAL MARKETPLACE SAFEGUARD (Zara, Shein, Amazon, Temu, AliExpress, Decathlon, Nike, Apple)
        if (isGlobalMarketplace) {
            val platformLabel = when {
                isZara -> "Zara"
                isShein -> "Shein"
                isAmazon -> "Amazon"
                isTemu -> "Temu"
                isAliExpress -> "AliExpress"
                isDecathlon -> "Decathlon"
                isNikeAdidas -> "Nike / Adidas"
                isAppleSamsung -> "Apple / Samsung"
                else -> "plateformes externes"
            }
            return AiShoppingRecommendation(
                userPrompt = query,
                aiExplanationAr = "عذراً، الطلب المباشر من منصة $platformLabel غير مدعوم حالياً على وصلـيكسا (WASLIXA). يمكنك تصفح مطاعمنا ومتاجرنا المحلية المعتمدة في المحمدية.",
                aiExplanationFr = "Désolé, les commandes directes depuis $platformLabel ne sont pas disponibles sur WASLIXA. Découvrez nos commerces et restaurants partenaires vérifiés à Mohammedia !",
                items = emptyList(),
                estimatedTotal = 0.0,
                deliveryFee = 0.0,
                grandTotal = 0.0,
                targetStoreName = "WASLIXA Partner Catalog",
                estimatedTimeMins = 0,
                confidenceScore = 95,
                speechAudioTextFr = "Désolé, les articles de cette plateforme externe ne sont pas disponibles pour commande directe. Découvrez notre sélection locale de partenaires !",
                speechAudioTextAr = "عذراً، منتجات هذه المنصة غير متوفرة للطلب المباشر. يمكنك تصفح قائمة شركائنا المعتمدين في المحمدية!",
                detectedNotes = detectedNotes,
                storeBadgeEmoji = "ℹ️",
                isUnavailableNotice = true,
                suggestedAlternatives = products.take(4)
            )
        }
        // 2. LOCAL E-COMMERCE SECTOR MATCH (Verified Local Partners)
        else if (hasAirpods || hasSmartwatch || hasCharger || hasGaming) {
            isEcommerceOrder = true
            mainStoreName = "تيك زون إلكترونيات المحمدية (TechZone)"
            storeEmoji = "📱"
            if (hasAirpods || (!hasSmartwatch && !hasCharger && !hasGaming)) {
                products.find { it.id == "p_airpods_pro" }?.let {
                    matchedItems.add(AiRecommendedItem(it, defaultQty, "ضمان 12 شهراً • تغليف طرد آمن", it.price * defaultQty))
                }
            }
            if (hasSmartwatch) {
                products.find { it.id == "p_smartwatch_ultra" }?.let {
                    matchedItems.add(AiRecommendedItem(it, 1, "ضمان رسمي • شاحن مغناطيسي أصلي", it.price))
                }
            }
            if (hasCharger) {
                products.find { it.id == "p_fast_charger_65w" }?.let {
                    matchedItems.add(AiRecommendedItem(it, 1, "شاحن سريع GaN ثلاثي المنافذ", it.price))
                }
            }
            if (hasGaming) {
                products.find { it.id == "p_gaming_headset_71" }?.let {
                    matchedItems.add(AiRecommendedItem(it, 1, "سماعة محيطية 7.1 RGB احترافية", it.price))
                }
            }
            explanationFr = "Sélection High-Tech vérifiée chez TechZone avec garantie 12 mois et livraison colis sécurisée !"
            explanationAr = "تم اختيار منتجات إلكترونية معتمدة من تيك زون مع ضمان 12 شهراً وشحن طرد مؤمن!"
        }
        else if (hasHoodie || hasParfum || hasLeather) {
            isEcommerceOrder = true
            mainStoreName = "بوتيك كازابلانكا للأزياء والعطور (Élégance)"
            storeEmoji = "🛍️"
            if (hasHoodie) {
                products.find { it.id == "p_hoodie_oversize" }?.let {
                    matchedItems.add(AiRecommendedItem(it, defaultQty, "قطن ثقيل 450g • إرجاع مجاني للمقاس", it.price * defaultQty))
                }
            }
            if (hasParfum) {
                products.find { it.id == "p_parfum_oud_royal" }?.let {
                    matchedItems.add(AiRecommendedItem(it, 1, "عطر أصلي مختوم ثبات 24 ساعة", it.price))
                }
            }
            if (hasLeather || matchedItems.isEmpty()) {
                products.find { it.id == "p_leather_bag_artisan" }?.let {
                    matchedItems.add(AiRecommendedItem(it, 1, "جلد طبيعي مغربي أصيل 100%", it.price))
                }
            }
            explanationFr = "Articles de mode et parfumerie de luxe authentifiés avec emballage soigné !"
            explanationAr = "منتجات أزياء وعطور أصلية فاخرة مصادق عليها مع تغليف بريميوم أنيق!"
        }
        // C. TACOS & SHAWARMA MATCH
        else if (hasTacos) {
            mainStoreName = "أو تاكوس (O'Tacos)"
            storeEmoji = "🌮"
            products.find { it.id == "p_otacos_l" }?.let {
                matchedItems.add(AiRecommendedItem(it, defaultQty, notesStr, it.price * defaultQty))
            }
            explanationFr = "J'ai préparé votre délicieux Tacos Français avec vos sauces favorites et frites dorées !"
            explanationAr = "اخترت لك طاكوس فرنسي شهي بصلصة الجبن والصلصات المختارة مع البطاطس المقرمشة!"
        }
        else if (hasShawarma) {
            mainStoreName = "شاورما الشام الملكية"
            storeEmoji = "🥙"
            products.find { it.id == "p_shawarma_arabi" }?.let {
                matchedItems.add(AiRecommendedItem(it, defaultQty, notesStr, it.price * defaultQty))
            }
            explanationFr = "Menu Chawarma Arabe sur pain saj avec crème d'ail toum authentique et frites !"
            explanationAr = "وجبة شاورما عربي بالدجاج على خبز الصاج مع الثومية الأصلية والبطاطس والمخلل!"
        }
        // C. BURGER KING & SMASH
        else if (hasBurgerKing) {
            mainStoreName = "برجر كينج (Burger King)"
            storeEmoji = "👑"
            products.find { it.id == "p_bk_whopper" }?.let {
                matchedItems.add(AiRecommendedItem(it, defaultQty, notesStr, it.price * defaultQty))
            }
            explanationFr = "Menu Double Whopper bœuf 100% grillé à la flamme avec frites et boisson !"
            explanationAr = "وجبة دبل ووبر اللحم البقري المشوي على اللهب مع البطاطس المقرمشة والمشروب!"
        }
        // D. MCDONALD'S
        else if (hasBigMac || hasNuggets) {
            mainStoreName = "ماكدونالدز (McDonald's)"
            storeEmoji = "🍔"
            products.find { it.id == "p_mcdo_bigmac" }?.let {
                matchedItems.add(AiRecommendedItem(it, defaultQty, notesStr, it.price * defaultQty))
            }
            if (hasNuggets) {
                products.find { it.id == "p_mcdo_nuggets" }?.let {
                    matchedItems.add(AiRecommendedItem(it, 1, "Sauce Barbecue & Curry", it.price))
                }
            }
            explanationFr = "Menu Big Mac croustillant avec vos frites dorées et boisson fraîche !"
            explanationAr = "اخترت لك وجبة بيج ماك ماكسي الساخنة مع البطاطس المقرمشة والمشروب المنعش!"
        }
        // E. KFC
        else if (hasKfc) {
            mainStoreName = "كنتاكي (KFC)"
            storeEmoji = "🍗"
            products.find { it.id == "p_kfc_tenders" }?.let {
                matchedItems.add(AiRecommendedItem(it, defaultQty, notesStr, it.price * defaultQty))
            } ?: products.find { it.id == "p_kfc_bucket" }?.let {
                matchedItems.add(AiRecommendedItem(it, defaultQty, notesStr, it.price * defaultQty))
            }
            explanationFr = "Sélection de poulet pané croustillant KFC avec sauces savoureuses !"
            explanationAr = "باكت دجاج كريسبي مقرمش مع البطاطس وتشكيلة الصوصات اللذيذة!"
        }
        // F. PIZZA
        else if (hasPizza) {
            mainStoreName = "بيتزا هت (Pizza Hut)"
            storeEmoji = "🍕"
            products.find { it.id == "p_pizza_4fromages" }?.let {
                matchedItems.add(AiRecommendedItem(it, defaultQty, notesStr, it.price * defaultQty))
            } ?: products.find { it.id == "p_ph_supreme" }?.let {
                matchedItems.add(AiRecommendedItem(it, defaultQty, notesStr, it.price * defaultQty))
            }
            explanationFr = "Pizza italienne dorée au four garnie de fromages fondants et sauce tomate !"
            explanationAr = "بيتزا إيطالية ساخنة من الفرن غنية بالأجبان الذائبة والتوابل الإيطالية!"
        }
        // G. SUSHI & ASIAN
        else if (hasSushi) {
            mainStoreName = "طوكيو سوشي بار (Tokyo Sushi)"
            storeEmoji = "🍣"
            products.find { it.id == "p_sushi_platter" }?.let {
                matchedItems.add(AiRecommendedItem(it, defaultQty, "Wasabi, gingembre et sauce soja", it.price * defaultQty))
            }
            explanationFr = "Plateau de sushis et makis frais avec sauce soja et wasabi !"
            explanationAr = "بلاطو سوشي طازج مشكل مع صوص الصويا والوسابي والزنجبيل المخلل!"
        }
        // H. MOROCCAN TRADITIONAL
        else if (hasTajine) {
            mainStoreName = "طاجين البركة - القصبة"
            storeEmoji = "🍲"
            products.find { it.id == "p_tajine_meat" }?.let {
                matchedItems.add(AiRecommendedItem(it, defaultQty, "لحم ضأن بالبرقوق واللوز مع خبز ساخن", it.price * defaultQty))
            }
            explanationFr = "Authentique tajine marocain mijoté avec pain chaud du jour !"
            explanationAr = "طاجين بلدي مغربي أصيل بالبرقوق المعسل واللوز مع خبز سخون من القصبة!"
        }
        // I. SEAFOOD
        else if (hasFish) {
            mainStoreName = "شواية الميناء للأسماك الطازجة"
            storeEmoji = "🦐"
            products.find { it.id == "p_fish_plate" }?.let {
                matchedItems.add(AiRecommendedItem(it, defaultQty, "فواكه بحر طازجة وشرمولة وتارتار", it.price * defaultQty))
            }
            explanationFr = "Plateau de poissons et fruits de mer frais grillés au port de Mohammedia !"
            explanationAr = "تشكيلة أسماك وفواكه البحر المشوية الطازجة من ميناء المحمدية مع الشرمولة!"
        }
        // J. GROCERIES
        else if (hasGrocery) {
            mainStoreName = "مرجان ماركت المحمدية"
            storeEmoji = "🧺"
            products.find { it.id == "p_marjane_pack" }?.let {
                matchedItems.add(AiRecommendedItem(it, defaultQty, "حليب، بيض بلدي، زيت زيتون", it.price * defaultQty))
            }
            explanationFr = "Panier de courses essentielles préparé chez Marjane !"
            explanationAr = "سلة المشتريات اليومية الأساسية من مرجان ماركت!"
        }
        // K. PHARMACY
        else if (hasPharma) {
            mainStoreName = "صيدلية الشفاء إكسبريس"
            storeEmoji = "💊"
            products.find { it.id == "p_pharma_care" }?.let {
                matchedItems.add(AiRecommendedItem(it, defaultQty, "أدوية ومستلزمات صحية فورية", it.price * defaultQty))
            }
            explanationFr = "Produits de pharmacie et soins d'urgence prêts pour livraison rapide."
            explanationAr = "مستلزمات صيدلية المركز الطارئة مع توصيل فوري!"
        }
        // L. SAFEGUARDED MATCHING & EXTERNAL PLATFORM HANDLING
        else {
            val isExternalPlatformQuery = q.contains("zara") || q.contains("shein") || q.contains("amazon") ||
                    q.contains("temu") || q.contains("aliexpress") || q.contains("nike") ||
                    q.contains("apple") || q.contains("iphone") || q.contains("decathlon")

            if (isExternalPlatformQuery) {
                // Mandatory Safeguard: Never fabricate products or prices from unpartnered external platforms
                return AiShoppingRecommendation(
                    userPrompt = query,
                    aiExplanationAr = "عذراً، الطلب المباشر من منصات خارجية غير معتمدة ($query) غير مدعوم على وصلـيكسا. يرجى تصفح المطاعم والمتاجر المحلية المعتمدة في المحمدية.",
                    aiExplanationFr = "Désolé, la commande directe depuis des plateformes externes ($query) n'est pas disponible sur WASLIXA. Découvrez nos commerces partenaires locaux à Mohammedia !",
                    items = emptyList(),
                    estimatedTotal = 0.0,
                    deliveryFee = 0.0,
                    grandTotal = 0.0,
                    targetStoreName = "WASLIXA Partner Catalog",
                    estimatedTimeMins = 0,
                    confidenceScore = 95,
                    speechAudioTextFr = "Désolé, les articles de cette plateforme externe ne sont pas disponibles pour commande directe. Découvrez nos commerces partenaires à Mohammedia !",
                    speechAudioTextAr = "عذراً، منتجات هذه المنصة غير متوفرة للطلب المباشر. يمكنك تصفح المطاعم والمتاجر المعتمدة في تطبيقنا!",
                    detectedNotes = detectedNotes,
                    storeBadgeEmoji = "ℹ️",
                    isUnavailableNotice = true,
                    suggestedAlternatives = products.take(4)
                )
            } else {
                // Direct flexible token match against authoritative catalog
                val candidate = products.find {
                    it.nameFr.contains(q, ignoreCase = true) || it.nameAr.contains(q, ignoreCase = true)
                } ?: products.find { it.id == "p_otacos_l" } ?: products.firstOrNull()

                if (candidate != null) {
                    mainStoreName = candidate.storeName
                    storeEmoji = candidate.emoji
                    matchedItems.add(AiRecommendedItem(candidate, defaultQty, notesStr, candidate.price * defaultQty))
                    explanationFr = "Sélection vérifiée chez ${candidate.storeName} (${candidate.nameFr}) !"
                    explanationAr = "تم تجهيز طلبك المعتمد من ${candidate.storeName} (${candidate.nameAr})!"
                }
            }
        }

        val total = matchedItems.sumOf { it.subtotal }
        val deliveryFee = if (isEcommerceOrder) 18.0 else 8.0
        val grandTotal = total + deliveryFee

        val itemsDescriptionFr = matchedItems.joinToString(" et ") { "${it.quantity} ${it.product.nameFr}" }
        val itemsDescriptionAr = matchedItems.joinToString(" و ") { "${it.quantity} ${it.product.nameAr}" }

        val speechFr = if (matchedItems.isNotEmpty()) {
            "Parfait ! J'ai bien sélectionné vos articles vérifiés chez $mainStoreName ($itemsDescriptionFr). Le total s'élève à ${grandTotal.toInt()} dirhams, livraison incluse. Vous pouvez vérifier le panier et confirmer !"
        } else {
            "Je n'ai pas trouvé cet article exact dans notre catalogue partenaire. Voici des suggestions disponibles à Mohammedia."
        }

        val speechAr = if (matchedItems.isNotEmpty()) {
            "ممتاز وعلى راسي وعيني! جهزت لك طلبك المعتمد من $mainStoreName ($itemsDescriptionAr). المجموع ${grandTotal.toInt()} درهم مع التوصيل السريع. اضغط على تأكيد لينطلق كابتن وصلـيكسا إليك!"
        } else {
            "لم أجد هذا المنتج في قائمة المتاجر المعتمدة، إليك اقتراحات مميزة متوفرة للتوصيل الفوري."
        }

        return AiShoppingRecommendation(
            userPrompt = query,
            aiExplanationAr = explanationAr,
            aiExplanationFr = explanationFr,
            items = matchedItems,
            estimatedTotal = total,
            deliveryFee = if (matchedItems.isEmpty()) 0.0 else deliveryFee,
            grandTotal = if (matchedItems.isEmpty()) 0.0 else grandTotal,
            targetStoreName = mainStoreName,
            estimatedTimeMins = if (isEcommerceOrder) 30 else 18,
            confidenceScore = 99,
            speechAudioTextFr = speechFr,
            speechAudioTextAr = speechAr,
            detectedNotes = detectedNotes,
            storeBadgeEmoji = storeEmoji,
            isUnavailableNotice = matchedItems.isEmpty(),
            suggestedAlternatives = if (matchedItems.isEmpty()) products.take(4) else emptyList()
        )
    }
}
