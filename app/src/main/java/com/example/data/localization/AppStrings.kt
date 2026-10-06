package com.example.data.localization

import com.example.data.model.AppLanguage

object AppStrings {

    fun appName(lang: AppLanguage): String = "ALLO AZIZ"

    fun slogan(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "أسرع توصيل في مدينتك.. طلبك واصل مع عزيز!"
        AppLanguage.FRENCH -> "La livraison la plus rapide de votre ville avec Aziz!"
        AppLanguage.ENGLISH -> "The fastest delivery in your city with Aziz!"
    }

    // Tabs
    fun tabHome(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "الرئيسية"
        AppLanguage.FRENCH -> "Accueil"
        AppLanguage.ENGLISH -> "Home"
    }

    fun tabSearch(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "البحث"
        AppLanguage.FRENCH -> "Recherche"
        AppLanguage.ENGLISH -> "Search"
    }

    fun tabFavorites(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "المفضلة"
        AppLanguage.FRENCH -> "Favoris"
        AppLanguage.ENGLISH -> "Favorites"
    }

    fun tabOrders(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "طلباتي"
        AppLanguage.FRENCH -> "Commandes"
        AppLanguage.ENGLISH -> "My Orders"
    }

    fun tabSupport(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "دعم 24/7"
        AppLanguage.FRENCH -> "Support 24/7"
        AppLanguage.ENGLISH -> "24/7 Support"
    }

    fun tabNotifications(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "الإشعارات"
        AppLanguage.FRENCH -> "Notifications"
        AppLanguage.ENGLISH -> "Alerts"
    }

    fun tabProfile(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "حسابي"
        AppLanguage.FRENCH -> "Profil"
        AppLanguage.ENGLISH -> "Profile"
    }

    // Home screen
    fun deliverTo(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "التوصيل إلى"
        AppLanguage.FRENCH -> "Livrer à"
        AppLanguage.ENGLISH -> "Deliver to"
    }

    fun defaultAddress(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "حي الرياض، شارع النخيل - الدار البيضاء"
        AppLanguage.FRENCH -> "Hay Riad, Rue des Palmiers - Casablanca"
        AppLanguage.ENGLISH -> "Hay Riad, Palm Street - Casablanca"
    }

    fun searchPlaceholder(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "ابحث عن وجبة، مطعم أو بقالة..."
        AppLanguage.FRENCH -> "Rechercher plat, restaurant, épicerie..."
        AppLanguage.ENGLISH -> "Search food, restaurants, groceries..."
    }

    fun categories(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "الأقسام"
        AppLanguage.FRENCH -> "Catégories"
        AppLanguage.ENGLISH -> "Categories"
    }

    fun featuredStores(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "المتاجر والمطاعم المميزة ⭐"
        AppLanguage.FRENCH -> "Restaurants & Boutiques Vedettes ⭐"
        AppLanguage.ENGLISH -> "Featured Stores & Kitchens ⭐"
    }

    fun popularMeals(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "الأطباق الأكثر طلباً 🔥"
        AppLanguage.FRENCH -> "Plats les plus demandés 🔥"
        AppLanguage.ENGLISH -> "Most Popular Dishes 🔥"
    }

    fun expressBannerTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "خدمة ألو عزيز إكسبريس ⚡"
        AppLanguage.FRENCH -> "Service ALLO AZIZ Express ⚡"
        AppLanguage.ENGLISH -> "ALLO AZIZ Express ⚡"
    }

    fun expressBannerDesc(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "توصيل خلال 20 دقيقة مع تتبع مباشر ومندوب خاص بك!"
        AppLanguage.FRENCH -> "Livraison en 20 min avec suivi en direct et coursier dédié!"
        AppLanguage.ENGLISH -> "Delivery in 20 mins with live tracking and dedicated courier!"
    }

    fun minTime(lang: AppLanguage, mins: Int): String = when (lang) {
        AppLanguage.ARABIC -> "$mins دقيقة"
        AppLanguage.FRENCH -> "$mins min"
        AppLanguage.ENGLISH -> "$mins mins"
    }

    fun currency(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "د.م"
        AppLanguage.FRENCH -> "DH"
        AppLanguage.ENGLISH -> "MAD"
    }

    fun addToCart(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "أضف للسلة"
        AppLanguage.FRENCH -> "Ajouter au panier"
        AppLanguage.ENGLISH -> "Add to Cart"
    }

    fun viewCart(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "عرض السلة"
        AppLanguage.FRENCH -> "Voir le panier"
        AppLanguage.ENGLISH -> "View Cart"
    }

    // Cart & Checkout
    fun cartTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "سلة المشتريات"
        AppLanguage.FRENCH -> "Votre Panier"
        AppLanguage.ENGLISH -> "Your Basket"
    }

    fun emptyCart(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "السلة فارغة حالياً"
        AppLanguage.FRENCH -> "Votre panier est vide"
        AppLanguage.ENGLISH -> "Your basket is empty"
    }

    fun emptyCartTip(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تصفح أشهى الأطباق والمنتجات وأضف ما ترغب به!"
        AppLanguage.FRENCH -> "Explorez nos délicieux plats et ajoutez vos envies!"
        AppLanguage.ENGLISH -> "Explore delicious meals and add what you love!"
    }

    fun orderSummary(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "ملخص الحساب"
        AppLanguage.FRENCH -> "Résumé de la commande"
        AppLanguage.ENGLISH -> "Order Summary"
    }

    fun subtotal(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "المجموع الفرعي"
        AppLanguage.FRENCH -> "Sous-total"
        AppLanguage.ENGLISH -> "Subtotal"
    }

    fun deliveryFee(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "رسوم التوصيل"
        AppLanguage.FRENCH -> "Frais de livraison"
        AppLanguage.ENGLISH -> "Delivery Fee"
    }

    fun discount(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "الخصم"
        AppLanguage.FRENCH -> "Réduction"
        AppLanguage.ENGLISH -> "Discount"
    }

    fun total(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "المجموع الكلي"
        AppLanguage.FRENCH -> "Total TTC"
        AppLanguage.ENGLISH -> "Total"
    }

    fun promoCodePrompt(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "كود الخصم (جرب: AZIZ20)"
        AppLanguage.FRENCH -> "Code Promo (ex: AZIZ20)"
        AppLanguage.ENGLISH -> "Promo Code (try: AZIZ20)"
    }

    fun apply(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تطبيق"
        AppLanguage.FRENCH -> "Appliquer"
        AppLanguage.ENGLISH -> "Apply"
    }

    fun deliveryNotes(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "ملاحظات للتوصيل (مثال: الشقة 4، الاتصال عند الباب)"
        AppLanguage.FRENCH -> "Instructions de livraison (ex: étage 4, appeler)"
        AppLanguage.ENGLISH -> "Delivery instructions (e.g. Apt 4, call on arrival)"
    }

    // Payment
    fun paymentMethod(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "طريقة الدفع الآمنة 🔒"
        AppLanguage.FRENCH -> "Moyen de Paiement Sécurisé 🔒"
        AppLanguage.ENGLISH -> "Secure Payment Method 🔒"
    }

    fun payCard(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "بطاقة مصرفية (Visa / Mastercard / CMI)"
        AppLanguage.FRENCH -> "Carte bancaire (Visa / Mastercard / CMI)"
        AppLanguage.ENGLISH -> "Bank Card (Visa / Mastercard / CMI)"
    }

    fun payCash(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "الدفع نقداً عند الاستلام (COD)"
        AppLanguage.FRENCH -> "Paiement en espèces à la livraison"
        AppLanguage.ENGLISH -> "Cash on Delivery"
    }

    fun payWallet(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "محفظة ALLO Pay (رصيدك:"
        AppLanguage.FRENCH -> "Portefeuille ALLO Pay (Solde:"
        AppLanguage.ENGLISH -> "ALLO Pay Wallet (Balance:"
    }

    fun payAppleGoogle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "Google Pay / Apple Pay"
        AppLanguage.FRENCH -> "Google Pay / Apple Pay"
        AppLanguage.ENGLISH -> "Google Pay / Apple Pay"
    }

    fun securityBadge(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "دفع آمن ومحمي 100% بتشفير بنكي 256-bit SSL"
        AppLanguage.FRENCH -> "Paiement 100% sécurisé cryptage SSL 256-bit"
        AppLanguage.ENGLISH -> "100% secure payment with 256-bit SSL encryption"
    }

    fun confirmAndPay(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تأكيد الطلب والدفع"
        AppLanguage.FRENCH -> "Confirmer et Commander"
        AppLanguage.ENGLISH -> "Confirm & Place Order"
    }

    // Order Tracking
    fun liveTracking(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "التتبع الفوري للطلب"
        AppLanguage.FRENCH -> "Suivi en direct de la commande"
        AppLanguage.ENGLISH -> "Live Order Tracking"
    }

    fun orderIdPrefix(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "طلب رقم"
        AppLanguage.FRENCH -> "Commande N°"
        AppLanguage.ENGLISH -> "Order #"
    }

    fun statusReceived(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تم استلام الطلب"
        AppLanguage.FRENCH -> "Commande reçue"
        AppLanguage.ENGLISH -> "Order Received"
    }

    fun statusPreparing(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "المطعم يقوم بالتحضير والتجهيز"
        AppLanguage.FRENCH -> "En cours de préparation"
        AppLanguage.ENGLISH -> "Preparing in Kitchen"
    }

    fun statusOnTheWay(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "المندوب استلم الطلب وهو في الطريق إليك"
        AppLanguage.FRENCH -> "Le livreur est en route vers vous"
        AppLanguage.ENGLISH -> "Courier on the way to you"
    }

    fun statusArrived(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "المندوب وصل عند موقعك!"
        AppLanguage.FRENCH -> "Le coursier est arrivé!"
        AppLanguage.ENGLISH -> "Courier has arrived!"
    }

    fun statusDelivered(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تم التوصيل بنجاح.. بالصحة والعافية!"
        AppLanguage.FRENCH -> "Commande livrée! Bon appétit!"
        AppLanguage.ENGLISH -> "Delivered! Enjoy your meal!"
    }

    fun estimatedArrival(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "الوقت التقديري للوصول"
        AppLanguage.FRENCH -> "Temps d'arrivée estimé"
        AppLanguage.ENGLISH -> "Estimated Arrival"
    }

    fun courierInfo(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "معلومات المندوب"
        AppLanguage.FRENCH -> "Informations du coursier"
        AppLanguage.ENGLISH -> "Courier Information"
    }

    fun callCourier(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "اتصال بالمندوب"
        AppLanguage.FRENCH -> "Appeler le coursier"
        AppLanguage.ENGLISH -> "Call Courier"
    }

    fun messageCourier(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "مراسلة المندوب"
        AppLanguage.FRENCH -> "Écrire un message"
        AppLanguage.ENGLISH -> "Chat Courier"
    }

    fun rateExperience(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "قيّم تجربتك والطلب ⭐"
        AppLanguage.FRENCH -> "Évaluez votre expérience ⭐"
        AppLanguage.ENGLISH -> "Rate Your Experience ⭐"
    }

    // Reviews & Ratings
    fun storeRating(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تقييم المطعم/المتجر"
        AppLanguage.FRENCH -> "Note du restaurant"
        AppLanguage.ENGLISH -> "Store Rating"
    }

    fun courierRating(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تقييم المندوب عزيز"
        AppLanguage.FRENCH -> "Note du livreur Aziz"
        AppLanguage.ENGLISH -> "Courier Aziz Rating"
    }

    fun writeReviewPlaceholder(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "اكتب رأيك بصراحة (سرعة التوصيل، جودة الأكل، النظافة...)"
        AppLanguage.FRENCH -> "Partagez votre avis (rapidité, goût, emballage...)"
        AppLanguage.ENGLISH -> "Write your review (speed, food quality, packaging...)"
    }

    fun submitReview(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "إرسال التقييم"
        AppLanguage.FRENCH -> "Envoyer l'avis"
        AppLanguage.ENGLISH -> "Submit Review"
    }

    fun reviewSubmittedThanks(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "شكراً لتقييمك! رأيك يساعدنا على تحسين الخدمة دائماً."
        AppLanguage.FRENCH -> "Merci pour votre avis! Votre retour nous aide à progresser."
        AppLanguage.ENGLISH -> "Thank you! Your feedback helps us improve."
    }

    // Orders History
    fun orderHistoryTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "سجل الطلبات السابقة"
        AppLanguage.FRENCH -> "Historique des commandes"
        AppLanguage.ENGLISH -> "Order History"
    }

    fun activeOrders(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "الطلبات الجارية"
        AppLanguage.FRENCH -> "Commandes en cours"
        AppLanguage.ENGLISH -> "Active Orders"
    }

    fun pastOrders(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "الطلبات السابقة"
        AppLanguage.FRENCH -> "Commandes passées"
        AppLanguage.ENGLISH -> "Past Orders"
    }

    fun reorder(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "إعادة الطلب"
        AppLanguage.FRENCH -> "Commander à nouveau"
        AppLanguage.ENGLISH -> "Reorder"
    }

    fun viewReceipt(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "عرض الفاتورة"
        AppLanguage.FRENCH -> "Voir la facture"
        AppLanguage.ENGLISH -> "View Receipt"
    }

    // 24/7 Support
    fun supportTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "الدعم الفني المباشر 24/7"
        AppLanguage.FRENCH -> "Assistance Client 24/7"
        AppLanguage.ENGLISH -> "24/7 Live Customer Support"
    }

    fun supportOnlineBadge(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "متصلون الآن على مدار الساعة للرد فوراً"
        AppLanguage.FRENCH -> "En ligne 24h/24 et 7j/7 pour vous aider"
        AppLanguage.ENGLISH -> "Online 24/7 to assist you immediately"
    }

    fun supportCallHotline(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "اتصل بخط المساعدة المباشر"
        AppLanguage.FRENCH -> "Appeler le service client"
        AppLanguage.ENGLISH -> "Call 24/7 Hotline"
    }

    fun supportWhatsApp(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "محادثة عبر واتساب"
        AppLanguage.FRENCH -> "Discussion via WhatsApp"
        AppLanguage.ENGLISH -> "WhatsApp Support"
    }

    fun supportTypeMessage(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "اكتب استفسارك هنا..."
        AppLanguage.FRENCH -> "Écrivez votre message..."
        AppLanguage.ENGLISH -> "Type your message here..."
    }

    fun supportSend(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "إرسال"
        AppLanguage.FRENCH -> "Envoyer"
        AppLanguage.ENGLISH -> "Send"
    }

    // Notifications
    fun notificationsTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "مركز التنبيهات والإشعارات"
        AppLanguage.FRENCH -> "Centre de Notifications"
        AppLanguage.ENGLISH -> "Notification Center"
    }

    fun markAllRead(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تحديد الكل كمقروء"
        AppLanguage.FRENCH -> "Tout marquer comme lu"
        AppLanguage.ENGLISH -> "Mark all as read"
    }

    // Profile & Settings
    fun chooseLanguage(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "اختر اللغة المفضلة"
        AppLanguage.FRENCH -> "Choisir la langue"
        AppLanguage.ENGLISH -> "Choose Language"
    }

    fun walletBalance(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "رصيد المحفظة"
        AppLanguage.FRENCH -> "Solde portefeuille"
        AppLanguage.ENGLISH -> "Wallet Balance"
    }

    fun topUpWallet(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "شحن المحفظة"
        AppLanguage.FRENCH -> "Recharger le solde"
        AppLanguage.ENGLISH -> "Top Up Balance"
    }

    fun savedAddresses(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "العناوين المحفوظة"
        AppLanguage.FRENCH -> "Adresses enregistrées"
        AppLanguage.ENGLISH -> "Saved Addresses"
    }

    fun aboutApp(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "حول تطبيق ألو عزيز"
        AppLanguage.FRENCH -> "À propos de ALLO AZIZ"
        AppLanguage.ENGLISH -> "About ALLO AZIZ"
    }

    fun appVersion(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "الإصدار 2.5.0 - جميع الحقوق محفوظة"
        AppLanguage.FRENCH -> "Version 2.5.0 - Tous droits réservés"
        AppLanguage.ENGLISH -> "Version 2.5.0 - All rights reserved"
    }
}
