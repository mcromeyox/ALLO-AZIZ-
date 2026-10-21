package com.example.data.services

import com.example.data.model.CatalogData
import com.example.data.model.Product
import com.example.data.model.StoreCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

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
    val storeBadgeEmoji: String = "🏪"
)

data class AiRecommendedItem(
    val product: Product,
    var quantity: Int,
    val customNote: String = "",
    val subtotal: Double = product.price * quantity
)

/**
 * Ultra-accurate AI Shopping Engine for ALLO AZIZ.
 * Supports natural Moroccan Darija, French, and English voice commands.
 * Automatically extracts multi-product orders, exact quantities, personalized culinary notes,
 * and generates human-like spoken responses with natural cadence and Moroccan hospitality.
 */
object AiShoppingService {

    suspend fun analyzeShoppingIntent(
        query: String,
        availableProducts: List<Product> = CatalogData.products
    ): AiShoppingRecommendation = withContext(Dispatchers.Default) {
        val cleanQuery = query.trim()
        // Keep product matching local until a trusted backend proxy is configured.
        // Never embed a Gemini API key in the APK or send it in a URL query string.
        matchLocalCatalogWithNlp(cleanQuery, availableProducts)
    }

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
        // A. E-COMMERCE SECTOR MATCH
        if (hasAirpods || hasSmartwatch || hasCharger || hasGaming) {
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
        // B. GLOBAL & EXTERNAL MARKETPLACE CONCIERGE (Zara, Shein, Amazon, Temu, AliExpress, Decathlon, Nike, Apple)
        else if (isGlobalMarketplace) {
            isEcommerceOrder = true
            val platformLabel = when {
                isZara -> "زارا (Zara Fashion)"
                isShein -> "شين (Shein Marketplace)"
                isAmazon -> "أمازون (Amazon Global)"
                isTemu -> "تيمو (Temu Express)"
                isAliExpress -> "علي إكسبريس (AliExpress)"
                isDecathlon -> "ديكاتلون (Decathlon Sports)"
                isNikeAdidas -> "نايكي وأديداس (Nike & Adidas Store)"
                isAppleSamsung -> "آبل وسامسونغ (Apple & Samsung Center)"
                else -> "كونسيرج المنصات العالمية (Global Marketplace Concierge)"
            }
            mainStoreName = "DELIVIO Concierge • $platformLabel"
            storeEmoji = when {
                isZara || isShein -> "👗"
                isNikeAdidas || isDecathlon -> "👟"
                isAppleSamsung -> "📱"
                else -> "📦"
            }
            val itemNameAr = when {
                isZara -> "قطعة أزياء زارا العصرية المختارة"
                isShein -> "فستان ولباس شين العصري الأنيق"
                isAmazon -> "منتج أصلي مستورد عبر أمازون جلوبال"
                isTemu -> "منتج كونسيرج تيمو إكسبريس بجودة ممتازة"
                isDecathlon -> "حذاء ومعدات رياضية أصلية من ديكاتلون"
                isNikeAdidas -> "حذاء سنيكرز نايكي / أديداس أصلي مريح"
                isAppleSamsung -> "إكسسوار وجهاز أصلي معتمد مع الضمان"
                else -> "طلبك الخاص من $platformLabel"
            }
            val itemNameFr = when {
                isZara -> "Article de mode tendance sélectionné chez Zara"
                isShein -> "Tenue et accessoires élégants Shein"
                isAmazon -> "Produit authentique importé Amazon Global"
                isTemu -> "Article express vérifié qualité Temu"
                isDecathlon -> "Équipement sportif certifié Decathlon"
                isNikeAdidas -> "Baskets sneakers originales Nike / Adidas"
                isAppleSamsung -> "Accessoire officiel certifié avec garantie"
                else -> "Commande concierge depuis $platformLabel"
            }
            val dynamicPrice = when {
                isAppleSamsung -> 380.0
                isNikeAdidas -> 350.0
                isZara -> 290.0
                isDecathlon -> 220.0
                isShein || isTemu -> 149.0
                else -> 199.0
            }
            val dynamicProduct = Product(
                id = "p_concierge_${System.currentTimeMillis()}",
                storeId = "store_concierge_global",
                storeName = mainStoreName,
                nameEn = itemNameFr,
                nameAr = itemNameAr,
                nameFr = itemNameFr,
                descriptionEn = "Concierge item sourced with quality assurance",
                descriptionAr = "طلب خاص تم توفيره فورياً عبر خدمة ديليفيو كونسيرج الذكية للمنصات العالمية مع الضمان والتوصيل المؤمن",
                descriptionFr = "Service Concierge DELIVIO multi-plateformes avec emballage sécurisé et garantie",
                price = dynamicPrice,
                category = StoreCategory.ECOMMERCE,
                emoji = storeEmoji,
                rating = 4.95,
                imageUrl = "https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=600"
            )
            matchedItems.add(AiRecommendedItem(dynamicProduct, defaultQty, "تغليف مؤمن وشحن سريع ومطابقة الجودة 100%", dynamicPrice * defaultQty))
            explanationFr = "Excellent choix ! Votre article chez $platformLabel a été identifié et préparé par le Concierge DELIVIO avec garantie et expédition express."
            explanationAr = "اختيار رائع! تم إيجاد طلبك وتحديده من $platformLabel وتجهيزه عبر كونسيرج ديليفيو الذكي مع ضمان الجودة والشحن المؤمن."
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
        // L. Unknown or unavailable request: never fabricate a purchasable product.
        else {
            mainStoreName = "DELIVIO"
            storeEmoji = "🛍️"
            explanationFr = "Je n'ai pas trouvé ce produit dans le catalogue disponible. Aucun article n'a été ajouté."
            explanationAr = "لم أجد هذا المنتج في الكتالوج المتاح. لم تتم إضافة أي منتج إلى السلة."
        }

        // Keep a single seller per cart and derive the displayed seller and fees from the catalog.
        val storeIds = matchedItems.map { it.product.storeId }.distinct()
        if (storeIds.size > 1) {
            matchedItems.clear()
            explanationFr = "Les articles sélectionnés proviennent de plusieurs commerces. Aucun article n'a été ajouté."
            explanationAr = "المنتجات المطلوبة من متاجر مختلفة. لم تتم إضافة أي منتجات إلى السلة."
        }
        var selectedProduct = matchedItems.firstOrNull()?.product
        var selectedStore = selectedProduct?.let { product ->
            CatalogData.stores.firstOrNull { it.id == product.storeId }
        }
        if (selectedProduct != null && selectedStore == null) {
            matchedItems.clear()
            selectedProduct = null
            explanationFr = "Les informations de livraison de ce commerce ne sont pas configurées. Aucun article n'a été ajouté."
            explanationAr = "بيانات توصيل هذا المتجر غير مهيأة. لم تتم إضافة أي منتج."
        }
        if (selectedProduct != null) mainStoreName = selectedProduct.storeName else mainStoreName = "DELIVIO"
        if (matchedItems.isEmpty()) {
            explanationFr = "Aucun article correspondant n'a été trouvé dans le catalogue disponible."
            explanationAr = "لم يتم العثور على منتج مطابق في الكتالوج المتاح."
        }
        val total = matchedItems.sumOf { (it.product.price.coerceAtLeast(0.0)) * it.quantity.coerceAtLeast(1) }
        val deliveryFee = if (matchedItems.isEmpty()) 0.0 else (selectedStore?.deliveryFee ?: 0.0).coerceAtLeast(0.0)
        val grandTotal = total + deliveryFee

        val itemsDescriptionFr = matchedItems.joinToString(" et ") { "${it.quantity} ${it.product.nameFr}" }
        val itemsDescriptionAr = matchedItems.joinToString(" و ") { "${it.quantity} ${it.product.nameAr}" }

        val speechFr = if (matchedItems.isEmpty()) {
            "Je n'ai trouvé aucun article correspondant dans le catalogue disponible. Aucun article n'a été ajouté. La confirmation de commande n'est pas encore disponible."
        } else {
            "J'ai préparé une sélection à partir du catalogue de $mainStoreName : $itemsDescriptionFr. Le montant indicatif est de ${grandTotal.toInt()} DH, selon les données du catalogue. Vérifiez les articles avant de continuer ; la confirmation de commande n'est pas encore disponible."
        }

        val speechAr = if (matchedItems.isEmpty()) {
            "لم أجد منتجاً مطابقاً في الكتالوج المتاح، ولم تتم إضافة أي منتج. تأكيد الطلبات غير متاح حالياً."
        } else {
            "حضّرت لك اختياراً من كتالوج $mainStoreName: $itemsDescriptionAr. المبلغ التقديري ${grandTotal.toInt()} درهم حسب بيانات الكتالوج. راجع المنتجات قبل المتابعة؛ تأكيد الطلبات غير متاح حالياً."
        }

        return AiShoppingRecommendation(
            userPrompt = query,
            aiExplanationAr = explanationAr,
            aiExplanationFr = explanationFr,
            items = matchedItems,
            estimatedTotal = total,
            deliveryFee = deliveryFee,
            grandTotal = grandTotal,
            targetStoreName = mainStoreName,
            estimatedTimeMins = selectedStore?.deliveryTimeMins ?: 0,
            confidenceScore = if (matchedItems.isEmpty()) 0 else 80,
            speechAudioTextFr = speechFr,
            speechAudioTextAr = speechAr,
            detectedNotes = detectedNotes,
            storeBadgeEmoji = storeEmoji
        )
    }
}
