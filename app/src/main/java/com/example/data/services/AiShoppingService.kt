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
    val estimatedTimeMins: Int
)

data class AiRecommendedItem(
    val product: Product,
    var quantity: Int,
    val customNote: String = "",
    val subtotal: Double = product.price * quantity
)

object AiShoppingService {

    private const val TAG = "AiShoppingService"
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    /**
     * Process voice or text shopping query through Gemini 3.5 Flash,
     * or use the smart Moroccan NLP heuristic engine as an offline-first fallback.
     */
    suspend fun analyzeShoppingIntent(
        query: String,
        availableProducts: List<Product> = CatalogData.products
    ): AiShoppingRecommendation = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) {
            return@withContext buildFallbackRecommendation("وجبة بيج ماك كومبو", availableProducts)
        }

        // Try Gemini REST API if key is available
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        if (apiKey.isNotBlank() && !apiKey.contains("PLACEHOLDER") && !apiKey.contains("YourKey") && !apiKey.contains("DEFAULT_GEMINI")) {
            try {
                val geminiResult = callGeminiApi(cleanQuery, apiKey, availableProducts)
                if (geminiResult != null && geminiResult.items.isNotEmpty()) {
                    return@withContext geminiResult
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini API call failed or timed out, falling back to local NLP engine: ${e.message}")
            }
        }

        // Offline / zero-dependency Moroccan NLP matcher
        return@withContext matchLocalCatalogWithNlp(cleanQuery, availableProducts)
    }

    /**
     * Call Gemini REST API using gemini-3.5-flash model
     */
    private fun callGeminiApi(
        userVoiceText: String,
        apiKey: String,
        products: List<Product>
    ): AiShoppingRecommendation? {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val catalogSummary = products.take(15).map { p ->
            JSONObject().apply {
                put("id", p.id)
                put("store", p.storeName)
                put("nameAr", p.nameAr)
                put("price", p.price)
                put("category", p.category.name)
            }
        }

        val systemPrompt = """
            You are 'Allo Aziz AI', the smart voice shopping assistant for the Allo Aziz delivery app in Mohammedia, Morocco.
            The user just spoke an order or grocery request in Arabic, Moroccan Darija, or French.
            Analyze their intent and select the matching product(s) from this catalog:
            ${catalogSummary}
            
            Return ONLY a raw valid JSON object with this exact structure:
            {
               "explanationAr": "Short friendly Arabic explanation summarizing what was selected and wishing good appetite/service",
               "explanationFr": "Short French translation",
               "storeName": "Store name",
               "items": [
                  {"productId": "...", "quantity": 1, "note": "..."}
               ]
            }
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            val contents = JSONArray()
            val contentObj = JSONObject().apply {
                val parts = JSONArray()
                parts.put(JSONObject().put("text", "$systemPrompt\n\nUser Voice Request: \"$userVoiceText\""))
                put("parts", parts)
            }
            contents.put(contentObj)
            put("contents", contents)
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.2)
                put("responseMimeType", "application/json")
            })
        }

        val request = Request.Builder()
            .url(endpoint)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini HTTP error ${response.code}")
                return null
            }
            val responseString = response.body?.string() ?: return null
            val rootJson = JSONObject(responseString)
            val candidates = rootJson.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null
            val candidate = candidates.getJSONObject(0)
            val content = candidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            if (parts.length() == 0) return null
            val rawText = parts.getJSONObject(0).optString("text", "")
            if (rawText.isBlank()) return null

            val parsedJson = JSONObject(rawText)
            val explAr = parsedJson.optString("explanationAr", "تم اختيار أفضل المنتجات المطابقة لطلبك!")
            val explFr = parsedJson.optString("explanationFr", "Produits sélectionnés pour votre commande !")
            val targetStore = parsedJson.optString("storeName", "ألو عزيز إكسبريس")
            val itemsArray = parsedJson.optJSONArray("items") ?: JSONArray()

            val recommendedItems = mutableListOf<AiRecommendedItem>()
            for (i in 0 until itemsArray.length()) {
                val itemObj = itemsArray.getJSONObject(i)
                val pid = itemObj.optString("productId")
                val qty = itemObj.optInt("quantity", 1).coerceAtLeast(1)
                val note = itemObj.optString("note", "")
                val product = products.find { it.id == pid }
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
                estimatedTimeMins = 20
            )
        }
    }

    /**
     * Moroccan & Arabic Natural Language matching engine
     * Handles Darija, standard Arabic, French, and English voice queries.
     */
    fun matchLocalCatalogWithNlp(
        query: String,
        products: List<Product>
    ): AiShoppingRecommendation {
        val q = query.lowercase().trim()

        // Extract requested quantity
        val quantity = when {
            q.contains("اثنان") || q.contains("2") || q.contains("زوج") || q.contains("deux") || q.contains("two") -> 2
            q.contains("ثلاثة") || q.contains("3") || q.contains("تلاتة") || q.contains("trois") || q.contains("three") -> 3
            q.contains("أربعة") || q.contains("4") || q.contains("quatre") || q.contains("four") -> 4
            else -> 1
        }

        val matchedItems = mutableListOf<AiRecommendedItem>()
        var mainStoreName = "ماكدونالدز ميرامار"
        var explanationAr = ""
        var explanationFr = ""

        when {
            // 1. Pizza Hut & Pizzas
            q.contains("بيتزا") || q.contains("pizza") || q.contains("بيدزا") || q.contains("جبن") || q.contains("كراست") -> {
                val pizzaProduct = products.find { it.id == "p_pizza_super" }
                    ?: products.find { it.category == StoreCategory.PIZZA }
                    ?: products.first()
                matchedItems.add(
                    AiRecommendedItem(
                        product = pizzaProduct,
                        quantity = quantity,
                        customNote = "أطراف محشوة بالجبن الموزاريلا 🧀 صوص حار جانبي",
                        subtotal = pizzaProduct.price * quantity
                    )
                )
                mainStoreName = "بيتزا هت المحمدية"
                explanationAr = "اخترت لك بيتزا سوبر سوبريم الشهيرة بعجينة مقرمشة وأطراف محشوة بالموزاريلا اللذيذة! 🍕"
                explanationFr = "J'ai sélectionné la pizza Super Supreme croustillante avec croûte farcie au fromage !"
            }

            // 2. KFC & Fried Chicken
            q.contains("دجاج") || q.contains("كنتاكي") || q.contains("kfc") || q.contains("باكت") || q.contains("مقرمش") || q.contains("كريسبي") || q.contains("poulet") -> {
                val kfcProduct = products.find { it.id == "p_kfc_bucket" }
                    ?: products.find { it.storeId == "store_kfc" }
                    ?: products.first()
                matchedItems.add(
                    AiRecommendedItem(
                        product = kfcProduct,
                        quantity = quantity,
                        customNote = "خلطة سرية مقرمشة حارة 🔥 مع بطاطس وصوص كول سلو",
                        subtotal = kfcProduct.price * quantity
                    )
                )
                mainStoreName = "كنتاكي شارع الحسن الثاني"
                explanationAr = "اخترت لك وجبة باكت كنتاكي 12 قطعة كريسبي المقرمشة الساخنة مع البطاطس والصوصات المميزة! 🍗"
                explanationFr = "J'ai préparé le fameux Bucket KFC 12 pièces croustillant avec frites et sauces !"
            }

            // 3. Moroccan Tajine & Traditional food
            q.contains("طاجين") || q.contains("tajine") || q.contains("لحم") || q.contains("برقوق") || q.contains("مغربي") || q.contains("بلدي") || q.contains("قصبة") -> {
                val tajineProduct = products.find { it.id == "p_tajine_meat" }
                    ?: products.find { it.category == StoreCategory.MOROCCAN }
                    ?: products.first()
                matchedItems.add(
                    AiRecommendedItem(
                        product = tajineProduct,
                        quantity = quantity,
                        customNote = "خبز قمح بلدي ساخن، زيتون محمر، وشاي بالنعناع 🇲🇦",
                        subtotal = tajineProduct.price * quantity
                    )
                )
                mainStoreName = "طاجين البركة - القصبة"
                explanationAr = "اخترت لك طاجين لحم بلدي معسل بالبرقوق واللوز المقلي مع خبز الدار الساخن من قصبة المحمدية! 🍲"
                explanationFr = "J'ai choisi pour vous un authentique tajine de bœuf aux pruneaux et amandes !"
            }

            // 4. Seafood & Fish from Mohammedia Port
            q.contains("حوت") || q.contains("سمك") || q.contains("شواية") || q.contains("كروفيت") || q.contains("جمبري") || q.contains("poisson") || q.contains("seafood") -> {
                val seafoodProduct = products.find { it.id == "p_seafood_platter" }
                    ?: products.find { it.category == StoreCategory.MOROCCAN }
                    ?: products.first()
                matchedItems.add(
                    AiRecommendedItem(
                        product = seafoodProduct,
                        quantity = quantity,
                        customNote = "شرمولة مغربية، صوص حار، وسلطة مغربية طازجة 🍋",
                        subtotal = seafoodProduct.price * quantity
                    )
                )
                mainStoreName = "شواية ميناء المحمدية للأسماك"
                explanationAr = "اخترت لك طبق مشكل من فواكه البحر والأسماك الطازجة من ميناء المحمدية متبلة بالشرمولة التقليدية! 🦐"
                explanationFr = "Plateau de fruits de mer frais du port de Mohammedia grillé avec chermoula !"
            }

            // 5. Bakery, Coffee & Breakfast
            q.contains("كرواسون") || q.contains("فطور") || q.contains("قهوة") || q.contains("بول") || q.contains("paul") || q.contains("كافيه") || q.contains("croissant") || q.contains("café") -> {
                val paulProduct = products.find { it.id == "p_paul_croissant" }
                    ?: products.find { it.category == StoreCategory.CAFE }
                    ?: products.first()
                matchedItems.add(
                    AiRecommendedItem(
                        product = paulProduct,
                        quantity = quantity,
                        customNote = "طازج وساخن من الفرن مع زبدة ومربى فرنسي ☕",
                        subtotal = paulProduct.price * quantity
                    )
                )
                mainStoreName = "مقهى ومخبز بول - حديقة الزهور"
                explanationAr = "اخترت لك تشكيلة معجنات فرنسية فاخرة وكرواسون زبدة ساخن من بول كافيه لبداية يوم مثالية! 🥐"
                explanationFr = "Sélection de viennoiseries françaises fraîches et croustillantes chez Paul !"
            }

            // 6. Groceries & Supermarket (Marjane)
            q.contains("مرجان") || q.contains("marjane") || q.contains("حليب") || q.contains("بيض") || q.contains("زيت") || q.contains("سلة") || q.contains("خضار") || q.contains("courses") || q.contains("épicerie") -> {
                val groceryProduct = products.find { it.id == "p_marjane_pack" }
                    ?: products.find { it.category == StoreCategory.GROCERY }
                    ?: products.first()
                matchedItems.add(
                    AiRecommendedItem(
                        product = groceryProduct,
                        quantity = quantity,
                        customNote = "منتجات طازجة ومختارة بعناية من رفوف مرجان إكسبريس 🥦",
                        subtotal = groceryProduct.price * quantity
                    )
                )
                mainStoreName = "مرجان ماركت المحمدية"
                explanationAr = "جهزت لك سلة المشتريات اليومية الأساسية: حليب طازج 2 لتر، 30 بيضة بلدية، وزيت زيتون بكر ممتاز! 🧺"
                explanationFr = "Votre panier de courses essentielles Marjane Market : lait, œufs frais et huile d'olive !"
            }

            // 7. Pharmacy & Health
            q.contains("دواء") || q.contains("صيدلية") || q.contains("فيتامين") || q.contains("صداع") || q.contains("pharmacie") || q.contains("médicament") || q.contains("paracétamol") -> {
                val pharmaProduct = products.find { it.id == "p_pharma_care" }
                    ?: products.find { it.storeId == "store_pharmacy" }
                    ?: products.first()
                matchedItems.add(
                    AiRecommendedItem(
                        product = pharmaProduct,
                        quantity = quantity,
                        customNote = "توصيل عاجل في ظرف 15 دقيقة مع فاتورة صيدلية معتمدة 🚑",
                        subtotal = pharmaProduct.price * quantity
                    )
                )
                mainStoreName = "صيدلية المركز المحمدية 24/7"
                explanationAr = "اخترت لك باقة تقوية المناعة وفيتامين C مع مسكن سريع للصداع ومعقم طبي من صيدلية المركز! 💊"
                explanationFr = "Kit santé et bien-être : Vitamine C, paracétamol et gel hydroalcoolique 24/7."
            }

            // 8. Express Courier Pickup
            q.contains("طرد") || q.contains("ألو جيب لي") || q.contains("سريع") || q.contains("مفاتيح") || q.contains("توصيل") || q.contains("colis") || q.contains("coursier") -> {
                val expressProduct = products.find { it.id == "p_express_service" }
                    ?: products.find { it.storeId == "store_express" }
                    ?: products.first()
                matchedItems.add(
                    AiRecommendedItem(
                        product = expressProduct,
                        quantity = 1,
                        customNote = "توصيل فوري خلال 15 دقيقة بأمان تام 🚀",
                        subtotal = expressProduct.price
                    )
                )
                mainStoreName = "ألو جيب لي المحمدية"
                explanationAr = "تم تخصيص كابتن ألو عزيز الفوري لخدمتك في نقل أو شراء ما تطلبه وتوصيله خلال دقائق! 🛵"
                explanationFr = "Coursier express dédié pour vos courses et livraisons immédiates !"
            }

            // 9. Default: McDonald's Combo Burger Feast
            else -> {
                val mcdoBigMac = products.find { it.id == "p_mcdo_bigmac" }
                    ?: products.first()
                val mcdoNuggets = products.find { it.id == "p_mcdo_nuggets" }

                matchedItems.add(
                    AiRecommendedItem(
                        product = mcdoBigMac,
                        quantity = quantity,
                        customNote = "وجبة ماكسي كومبو مع بطاطس مقرمشة ومشروب بارد مثلج 🍟",
                        subtotal = mcdoBigMac.price * quantity
                    )
                )

                if (q.contains("وجبة") || q.contains("غداء") || q.contains("عشاء") || q.contains("كاملة") || q.contains("مع")) {
                    mcdoNuggets?.let {
                        matchedItems.add(
                            AiRecommendedItem(
                                product = it,
                                quantity = 1,
                                customNote = "صوص باربكيو وصوص ألو عزيز السري 🌶️",
                                subtotal = it.price
                            )
                        )
                    }
                }

                mainStoreName = "ماكدونالدز ميرامار المحمدية"
                explanationAr = "اخترت لك وجبة بيج ماك ماكسي كومبو الشهيرة مع بطاطس ذهبية ومشروب منعش ساخن ومقرمش! 🍔"
                explanationFr = "J'ai préparé pour vous le délicieux Menu Maxi Best Of Big Mac avec frites et boisson !"
            }
        }

        val total = matchedItems.sumOf { it.subtotal }
        val deliveryFee = 7.0

        return AiShoppingRecommendation(
            userPrompt = query,
            aiExplanationAr = explanationAr,
            aiExplanationFr = explanationFr,
            items = matchedItems,
            estimatedTotal = total,
            deliveryFee = deliveryFee,
            grandTotal = total + deliveryFee,
            targetStoreName = mainStoreName,
            estimatedTimeMins = 18
        )
    }

    private fun buildFallbackRecommendation(query: String, products: List<Product>): AiShoppingRecommendation {
        return matchLocalCatalogWithNlp(query, products)
    }
}
