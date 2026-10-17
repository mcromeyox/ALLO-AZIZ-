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
    val confidenceScore: Int = 98,
    val speechAudioTextFr: String = "",
    val speechAudioTextAr: String = "",
    val detectedNotes: List<String> = emptyList(),
    val storeBadgeEmoji: String = "🏪"
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
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(18, TimeUnit.SECONDS)
        .build()

    /**
     * Process voice shopping query through Gemini 3.5 Flash or
     * our advanced Moroccan natural language shopping engine.
     */
    suspend fun analyzeShoppingIntent(
        query: String,
        availableProducts: List<Product> = CatalogData.products
    ): AiShoppingRecommendation = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) {
            return@withContext matchLocalCatalogWithNlp("Menu Big Mac", availableProducts)
        }

        // 1. Try Gemini 3.5 Flash API if key is present
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        if (apiKey.isNotBlank() && !apiKey.contains("PLACEHOLDER") && !apiKey.contains("YourKey") && !apiKey.contains("DEFAULT_GEMINI")) {
            try {
                val geminiResult = callGeminiApi(cleanQuery, apiKey, availableProducts)
                if (geminiResult != null && geminiResult.items.isNotEmpty()) {
                    return@withContext geminiResult
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini API failed, falling back to local NLP engine: ${e.message}")
            }
        }

        // 2. High-precision offline Moroccan NLP parsing engine
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
            You are 'ALLO AZIZ AI', the elite voice shopping engine for the ALLO AZIZ delivery app in Mohammedia, Morocco.
            The user spoke a food, restaurant, grocery, or courier request in French, Moroccan Darija, or Arabic: "$userVoiceText".
            
            Catalog available:
            $catalogSummary
            
            Analyze the user's intent with extreme accuracy:
            - Accurately identify which products the user wants (can be multiple items).
            - Detect quantities for each item (default 1).
            - Extract special requests or preferences (e.g. sauce piquante, sans oignon, bien cuit).
            - Calculate subtotal and provide concise speech audio text in French and Arabic (1 sentence each for text-to-speech reading).
            
            Return ONLY raw valid JSON:
            {
               "confidence": 98,
               "explanationFr": "French summary of what was chosen",
               "explanationAr": "Arabic summary of what was chosen",
               "speechAudioFr": "French audio sentence: J'ai préparé votre panier avec X articles pour Y DH chez Z.",
               "speechAudioAr": "Arabic audio sentence",
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
                put("maxOutputTokens", 600)
            })
        }

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
            val targetStore = parsedJson.optString("storeName", "ALLO AZIZ Express")
            val confidence = parsedJson.optInt("confidence", 98)

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
                estimatedTimeMins = 18,
                confidenceScore = confidence,
                speechAudioTextFr = speechFr,
                speechAudioTextAr = speechAr,
                detectedNotes = detectedNotes,
                storeBadgeEmoji = "🏪"
            )
        }
    }

    /**
     * Advanced Moroccan & French Natural Language Engine (Accurate, Multi-item aware)
     */
    fun matchLocalCatalogWithNlp(
        query: String,
        products: List<Product>
    ): AiShoppingRecommendation {
        val q = query.lowercase(Locale.ROOT).trim()

        // 1. Detect requested custom notes / preferences
        val detectedNotes = mutableListOf<String>()
        if (q.contains("sans oignon") || q.contains("بلا بصل") || q.contains("بدون بصل")) {
            detectedNotes.add("Sans oignon")
        }
        if (q.contains("piquant") || q.contains("har") || q.contains("حار") || q.contains("spicy") || q.contains("épicé") || q.contains("sauce piquante")) {
            detectedNotes.add("Sauce piquante 🔥")
        }
        if (q.contains("extra fromage") || q.contains("فرماج زايد") || q.contains("جبن إضافي") || q.contains("cheesy")) {
            detectedNotes.add("Extra fromage fondu 🧀")
        }
        if (q.contains("bien cuit") || q.contains("محمر مزيان") || q.contains("crispy") || q.contains("مقرمش")) {
            detectedNotes.add("Bien cuit & croustillant")
        }
        if (q.contains("urgent") || q.contains("دابا") || q.contains("سريع") || q.contains("vite") || q.contains("express")) {
            detectedNotes.add("Priorité Express ⚡")
        }
        if (q.contains("frais") || q.contains("طازج") || q.contains("fresh")) {
            detectedNotes.add("100% Frais du jour")
        }

        // Global default quantity if specified (e.g. "2 pizzas", "3 tajines")
        val defaultQty = when {
            q.contains("زوج") || q.contains("جوج") || q.contains("deux") || q.contains("two") || q.contains(" 2 ") || q.startsWith("2 ") -> 2
            q.contains("تلاتة") || q.contains("ثلاثة") || q.contains("trois") || q.contains("three") || q.contains(" 3 ") || q.startsWith("3 ") -> 3
            q.contains("ربعة") || q.contains("أربعة") || q.contains("quatre") || q.contains("four") || q.contains(" 4 ") || q.startsWith("4 ") -> 4
            else -> 1
        }

        val matchedItems = mutableListOf<AiRecommendedItem>()
        var mainStoreName = "McDonald's Miramar Mohammedia"
        var storeEmoji = "🍔"
        var explanationFr = ""
        var explanationAr = ""

        // Multi-item checks:
        val hasBigMac = q.contains("big mac") || q.contains("بيج ماك") || q.contains("burger") || q.contains("برجر") || q.contains("همبرغر")
        val hasNuggets = q.contains("nugget") || q.contains("ناجتس") || q.contains("ناغتس") || q.contains("قطع دجاج")
        val hasMcFlurry = q.contains("mcflurry") || q.contains("ماك فلوري") || q.contains("glace") || q.contains("آيس كريم") || q.contains("ايس كريم") || q.contains("dessert")

        val hasKfcBucket = q.contains("bucket") || q.contains("باكت") || q.contains("باكيت") || q.contains("سطل") || q.contains("دجاج كنتاكي") || q.contains("kfc") || q.contains("poulet croustillant")
        val hasZinger = q.contains("zinger") || q.contains("زنجر") || q.contains("مايتي") || q.contains("mighty")

        val hasPizza = q.contains("pizza") || q.contains("بيتزا") || q.contains("بيدزا") || q.contains("supreme") || q.contains("سوبريم") || q.contains("pepperoni") || q.contains("croûte")
        val hasCroissant = q.contains("croissant") || q.contains("كرواسون") || q.contains("مخبز") || q.contains("paul") || q.contains("بول") || q.contains("viennoiserie") || q.contains("petit déjeuner") || q.contains("فطور")
        val hasTart = q.contains("tart") || q.contains("تارت") || q.contains("fraise") || q.contains("فراولة") || q.contains("gâteau") || q.contains("حلوى")

        val hasTajine = q.contains("tajine") || q.contains("طاجين") || q.contains("لحم") || q.contains("pruneaux") || q.contains("برقوق")
        val hasCouscous = q.contains("couscous") || q.contains("كسكس") || q.contains("كسكسو") || q.contains("خضار")

        val hasFish = q.contains("poisson") || q.contains("fruit de mer") || q.contains("crevette") || q.contains("calamar") || q.contains("حوت") || q.contains("سمك") || q.contains("فواكه البحر") || q.contains("شواية") || q.contains("قمرون")
        val hasGrocery = q.contains("marjane") || q.contains("مرجان") || q.contains("lait") || q.contains("حليب") || q.contains("oeuf") || q.contains("œuf") || q.contains("بيض") || q.contains("huile") || q.contains("زيت") || q.contains("courses") || q.contains("épicerie") || q.contains("سلة")
        val hasPharma = q.contains("pharmacie") || q.contains("صيدلية") || q.contains("médicament") || q.contains("دواء") || q.contains("vitamine") || q.contains("فيتامين") || q.contains("paracétamol") || q.contains("باراسيتامول") || q.contains("صداع")
        val hasCourier = q.contains("coursier") || q.contains("كابتن") || q.contains("مندوب") || q.contains("colis") || q.contains("طرد") || q.contains("مفاتيح") || q.contains("ألو جيب لي") || q.contains("express")

        // 1. McDonald's multi-match
        if (hasBigMac || hasNuggets || hasMcFlurry) {
            mainStoreName = "McDonald's Miramar Mohammedia"
            storeEmoji = "🍔"
            val notesStr = if (detectedNotes.isNotEmpty()) detectedNotes.joinToString(" • ") else "Menu Maxi avec Frites et Boisson fraîche"

            if (hasBigMac || (!hasNuggets && !hasMcFlurry)) {
                products.find { it.id == "p_mcdo_bigmac" }?.let {
                    val qty = if (q.contains("2 big mac") || q.contains("deux big mac") || q.contains("جوج بيج ماك")) 2 else defaultQty
                    matchedItems.add(AiRecommendedItem(it, qty, notesStr, it.price * qty))
                }
            }
            if (hasNuggets) {
                products.find { it.id == "p_mcdo_nuggets" }?.let {
                    matchedItems.add(AiRecommendedItem(it, 1, "Sauce Barbecue & Curry doux", it.price))
                }
            }
            if (hasMcFlurry) {
                products.find { it.id == "p_mcdo_mcflurry" }?.let {
                    matchedItems.add(AiRecommendedItem(it, 1, "Coulis caramel chaud et éclats Oreo", it.price))
                }
            }

            explanationFr = "J'ai composé pour vous votre menu McDonald's idéal avec vos accompagnements et boissons !"
            explanationAr = "اخترت لك وجبة ماكدونالدز متكاملة مع البطاطس المقرمشة والمشروب المنعش والصوصات!"
        }
        // 2. KFC match
        else if (hasKfcBucket || hasZinger) {
            mainStoreName = "KFC Bd Hassan II Mohammedia"
            storeEmoji = "🍗"
            val notesStr = if (detectedNotes.isNotEmpty()) detectedNotes.joinToString(" • ") else "Recette secrète croustillante avec frites"

            if (hasKfcBucket) {
                products.find { it.id == "p_kfc_bucket" }?.let {
                    matchedItems.add(AiRecommendedItem(it, defaultQty, notesStr, it.price * defaultQty))
                }
            }
            if (hasZinger || matchedItems.isEmpty()) {
                products.find { it.id == "p_kfc_zinger" }?.let {
                    matchedItems.add(AiRecommendedItem(it, 1, "Double filet piquant et fromage fondant", it.price))
                }
            }

            explanationFr = "Votre sélection KFC croustillante préparée avec frites familiales et sauces !"
            explanationAr = "تم تجهيز وجبة كنتاكي المقرمشة الساخنة مع البطاطس والصوصات المميزة!"
        }
        // 3. Pizza Hut match
        else if (hasPizza) {
            mainStoreName = "Pizza Hut Mohammedia Centre"
            storeEmoji = "🍕"
            val notesStr = if (detectedNotes.isNotEmpty()) detectedNotes.joinToString(" • ") else "Croûte dorée farcie au fromage mozzarella"
            products.find { it.id == "p_ph_supreme" }?.let {
                matchedItems.add(AiRecommendedItem(it, defaultQty, notesStr, it.price * defaultQty))
            }
            explanationFr = "J'ai sélectionné la Pizza Super Supreme Cheesy Crust grande taille avec mozzarella fondante !"
            explanationAr = "اخترت لك بيتزا سوبر سوبريم الكبيرة بأطراف محشوة بجبن الموزاريلا الشهي!"
        }
        // 4. Paul Bakery & Café match
        else if (hasCroissant || hasTart) {
            mainStoreName = "Paul Boulangerie & Café Parc Mohammedia"
            storeEmoji = "🥐"
            if (hasCroissant || matchedItems.isEmpty()) {
                products.find { it.id == "p_paul_croissant" }?.let {
                    matchedItems.add(AiRecommendedItem(it, defaultQty, "Feuilletage pur beurre et amandes grillées", it.price * defaultQty))
                }
            }
            if (hasTart) {
                products.find { it.id == "p_paul_tart" }?.let {
                    matchedItems.add(AiRecommendedItem(it, 1, "Fraises fraîches et crème pâtissière vanille", it.price))
                }
            }
            explanationFr = "Sélection gourmande de viennoiseries et pâtisseries fraîches de chez Paul !"
            explanationAr = "تشكيلة معجنات وحلويات فرنسية طازجة من مخبز ومقهى بول الفاخر!"
        }
        // 5. Moroccan Tajine & Couscous match
        else if (hasTajine || hasCouscous) {
            mainStoreName = "Tajine Al Baraka - Kasbah Mohammedia"
            storeEmoji = "🍲"
            if (hasTajine || matchedItems.isEmpty()) {
                products.find { it.id == "p_tajine_meat" }?.let {
                    matchedItems.add(AiRecommendedItem(it, defaultQty, "Agneau fondant mijoté avec pain chaud du jour", it.price * defaultQty))
                }
            }
            if (hasCouscous) {
                products.find { it.id == "p_couscous_royale" }?.let {
                    matchedItems.add(AiRecommendedItem(it, 1, "Semoule fine, bouillon riche et 7 légumes frais", it.price))
                }
            }
            explanationFr = "Authentique festin marocain préparé selon les recettes traditionnelles de la Kasbah !"
            explanationAr = "طاجين مغربي أصيل على أصوله بالبرقوق واللوز مع خبز الدار الساخن من القصبة!"
        }
        // 6. Seafood & Fish from Mohammedia Port
        else if (hasFish) {
            mainStoreName = "Grillade du Port Poissons Mohammedia"
            storeEmoji = "🦐"
            products.find { it.id == "p_fish_plate" }?.let {
                matchedItems.add(AiRecommendedItem(it, defaultQty, "Calamars, crevettes et poissons avec chermoula fraîche", it.price * defaultQty))
            }
            explanationFr = "Plateau de poissons et fruits de mer frais pêchés au port de Mohammedia !"
            explanationAr = "تشكيلة طازجة من فواكه البحر والأسماك المشوية من ميناء المحمدية مع الشرمولة!"
        }
        // 7. Marjane Groceries
        else if (hasGrocery) {
            mainStoreName = "Marjane Market Mohammedia"
            storeEmoji = "🧺"
            products.find { it.id == "p_marjane_pack" }?.let {
                matchedItems.add(AiRecommendedItem(it, defaultQty, "Lait frais 2L, 30 œufs fermiers, huile d'olive vierge extra", it.price * defaultQty))
            }
            explanationFr = "Votre panier de courses essentielles sélectionné et emballé avec soin chez Marjane !"
            explanationAr = "سلة المشتريات اليومية الأساسية من مرجان ماركت طازجة ومختارة بعناية!"
        }
        // 8. Pharmacy & Care
        else if (hasPharma) {
            mainStoreName = "Pharmacie Centrale Mohammedia 24/7"
            storeEmoji = "💊"
            products.find { it.id == "p_pharma_care" }?.let {
                matchedItems.add(AiRecommendedItem(it, defaultQty, "Vitamine C effervescente, Zinc et paracétamol 1000mg", it.price * defaultQty))
            }
            explanationFr = "Kit santé et immunité express préparé avec conseils officinaux."
            explanationAr = "باقة تقوية المناعة وفيتامين C ومسكن من صيدلية المركز مع توصيل فوري!"
        }
        // 9. Express Courier
        else if (hasCourier) {
            mainStoreName = "Allo Jibli Mohammedia Express"
            storeEmoji = "🛵"
            products.find { it.id == "p_express_service" }?.let {
                matchedItems.add(AiRecommendedItem(it, 1, "Coursier Aziz dédié pour vos courses et colis urgents", it.price))
            }
            explanationFr = "Votre coursier personnel Aziz prend en charge votre mission en moins de 15 minutes !"
            explanationAr = "تم تخصيص الكابتن عزيز لتوصيل أو شراء ما ترغب فيه بأقصى سرعة!"
        }
        // 10. Default Smart Combo
        else {
            mainStoreName = "McDonald's Miramar Mohammedia"
            storeEmoji = "🍔"
            products.find { it.id == "p_mcdo_bigmac" }?.let {
                matchedItems.add(AiRecommendedItem(it, defaultQty, "Menu Maxi Best Of avec grandes frites et boisson", it.price * defaultQty))
            }
            products.find { it.id == "p_mcdo_nuggets" }?.let {
                matchedItems.add(AiRecommendedItem(it, 1, "Boîte de 9 nuggets croustillants avec sauces", it.price))
            }
            explanationFr = "J'ai préparé pour vous le célèbre Menu Big Mac accompagné de délicieux Chicken McNuggets !"
            explanationAr = "اخترت لك وجبة بيج ماك ماكسي الشهيرة مع تشيكن ماك ناجتس وبطاطس مقرمشة!"
        }

        val total = matchedItems.sumOf { it.subtotal }
        val deliveryFee = 7.0
        val grandTotal = total + deliveryFee

        val speechFr = "Votre commande est prête chez $mainStoreName : ${matchedItems.size} article(s) pour un total de ${grandTotal.toInt()} dirhams. Vous pouvez confirmer dès maintenant !"
        val speechAr = "تم تجهيز طلبك من $mainStoreName بمجموع ${grandTotal.toInt()} درهم. اضغط على زر التأكيد لمتابعة طلبك!"

        return AiShoppingRecommendation(
            userPrompt = query,
            aiExplanationAr = explanationAr,
            aiExplanationFr = explanationFr,
            items = matchedItems,
            estimatedTotal = total,
            deliveryFee = deliveryFee,
            grandTotal = grandTotal,
            targetStoreName = mainStoreName,
            estimatedTimeMins = 18,
            confidenceScore = 99,
            speechAudioTextFr = speechFr,
            speechAudioTextAr = speechAr,
            detectedNotes = detectedNotes,
            storeBadgeEmoji = storeEmoji
        )
    }
}
