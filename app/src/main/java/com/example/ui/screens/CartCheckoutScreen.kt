package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.localization.AppStrings
import com.example.data.model.AppLanguage
import com.example.data.model.CartItem
import com.example.data.model.PaymentMethodType
import com.example.ui.theme.AzizAmberSecondary
import com.example.ui.theme.AzizMint
import com.example.ui.theme.AzizOrangePrimary

@Composable
fun CartCheckoutScreen(
    language: AppLanguage,
    cartItems: List<CartItem>,
    walletBalance: Double,
    deliveryAddress: String,
    deliveryNotes: String,
    promoCode: String?,
    discountPercent: Double,
    selectedPaymentMethod: PaymentMethodType,
    onBack: () -> Unit,
    onUpdateQuantity: (String, Int) -> Unit,
    onAddressChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onApplyPromo: (String) -> Boolean,
    onSelectPaymentMethod: (PaymentMethodType) -> Unit,
    onPlaceOrder: () -> Unit,
    onNavigateToHome: () -> Unit,
    onUseCurrentGpsLocation: (() -> Unit)? = null,
    onOpenLocationPicker: (() -> Unit)? = null,
    onOpenPricingModal: (() -> Unit)? = null
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    var promoInput by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }

    var selectedDeliveryTiming by remember { mutableStateOf("NOW") }
    var selectedTipAmount by remember { mutableStateOf(0.0) }
    var splitPeopleCount by remember { mutableIntStateOf(2) }
    var showSplitBill by remember { mutableStateOf(false) }

    val hasEcommerceItems = cartItems.any { it.product.category == com.example.data.model.StoreCategory.ECOMMERCE }
    val subtotal = cartItems.sumOf { (it.product.price + it.extraOptionsPrice) * it.quantity }
    val deliveryFee = if (cartItems.isEmpty()) 0.0 else if (hasEcommerceItems) 18.0 else 8.0
    val discount = subtotal * discountPercent
    val total = (subtotal - discount + deliveryFee + selectedTipAmount).coerceAtLeast(0.0)

    if (cartItems.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .testTag("empty_cart_container"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(AzizOrangePrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = AzizOrangePrimary,
                        modifier = Modifier.size(50.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = AppStrings.emptyCart(language),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = AppStrings.emptyCartTip(language),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onNavigateToHome,
                    colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("empty_cart_shop_button")
                ) {
                    Text(
                        text = "تصفح القائمة الآن 🍽️",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("cart_checkout_scroll"),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = AppStrings.cartTitle(language),
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(cartItems) { item ->
            CartItemRow(
                item = item,
                language = language,
                onIncrease = { onUpdateQuantity(item.product.id, 1) },
                onDecrease = { onUpdateQuantity(item.product.id, -1) }
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = AzizOrangePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = AppStrings.deliverTo(language),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        if (onUseCurrentGpsLocation != null) {
                            TextButton(
                                onClick = onUseCurrentGpsLocation,
                                modifier = Modifier.testTag("auto_detect_gps_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = "GPS",
                                    tint = AzizMint,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "تحديد موقعي GPS 📍",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AzizMint
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (onOpenLocationPicker != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AzizOrangePrimary.copy(alpha = 0.10f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AzizOrangePrimary.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenLocationPicker() }
                                .testTag("open_map_location_picker_checkout_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🗺️", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "تحديد موقعك بدقة على خريطة Google Maps",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AzizOrangePrimary
                                        )
                                        Text(
                                            text = "انقر لفتح الخريطة وتثبيت الدبوس وأحياء المحمدية",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = AzizOrangePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    OutlinedTextField(
                        value = deliveryAddress,
                        onValueChange = onAddressChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("delivery_address_input"),
                        placeholder = {
                            Text(
                                text = "أدخل عنوان التوصيل بالتفصيل (الشارع، رقم العمارة، الحي...)",
                                fontSize = 12.sp
                            )
                        },
                        label = { Text("عنوان التوصيل 📍", fontSize = 11.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = deliveryNotes,
                        onValueChange = onNotesChange,
                        placeholder = {
                            Text(
                                text = AppStrings.deliveryNotes(language),
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("delivery_notes_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // Modern Delivery Scheduling Card (Uber Eats & Glovo Style)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (language == AppLanguage.ARABIC) "توقيت استلام الطلب ⏱️" else "Créneau de livraison ⏱️",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = { selectedDeliveryTiming = "NOW" },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedDeliveryTiming == "NOW") AzizOrangePrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (language == AppLanguage.ARABIC) "⚡ الآن (فوري)" else "⚡ Immédiat",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedDeliveryTiming == "NOW") Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Surface(
                            onClick = { selectedDeliveryTiming = "SCHEDULED" },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedDeliveryTiming == "SCHEDULED") AzizOrangePrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (language == AppLanguage.ARABIC) "⏰ جدولة وقت لاحق" else "⏰ Programmer",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedDeliveryTiming == "SCHEDULED") Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Driver Courier Tip Card (Glovo & Temu Style)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (language == AppLanguage.ARABIC) "إكرامية كابتن التوصيل 🛵" else "Pourboire du livreur 🛵",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (selectedTipAmount > 0) {
                            Text(
                                text = "+$selectedTipAmount د.م",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = AzizMint
                            )
                        }
                    }
                    Text(
                        text = if (language == AppLanguage.ARABIC) "100% من الإكرامية تذهب مباشرة إلى كابتن التوصيل" else "100% du pourboire revient au livreur",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0.0 to "0 د.م", 3.0 to "+3 د.م", 5.0 to "+5 د.م", 10.0 to "+10 د.م").forEach { (amount, label) ->
                            val isSelected = selectedTipAmount == amount
                            Surface(
                                onClick = { selectedTipAmount = amount },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) AzizMint else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = label,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = promoInput,
                        onValueChange = { promoInput = it },
                        placeholder = {
                            Text(
                                text = AppStrings.promoCodePrompt(language),
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("promo_code_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (promoInput.isNotBlank()) {
                                onApplyPromo(promoInput)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("apply_promo_btn")
                    ) {
                        Text(
                            text = AppStrings.apply(language),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.testTag("payment_methods_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppStrings.paymentMethod(language),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = AzizMint,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SSL 256-Bit",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AzizMint
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    PaymentOptionRow(
                        title = AppStrings.payCard(language),
                        subtitle = "تشفير بنكي آمن 100% مع حماية 3D-Secure",
                        icon = Icons.Default.CreditCard,
                        isSelected = selectedPaymentMethod == PaymentMethodType.CREDIT_CARD,
                        onClick = { onSelectPaymentMethod(PaymentMethodType.CREDIT_CARD) }
                    )

                    AnimatedVisibility(visible = selectedPaymentMethod == PaymentMethodType.CREDIT_CARD) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AzizOrangePrimary.copy(alpha = 0.06f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "💳 بطاقة بنكية معتمدة (Visa / Mastercard / CMI)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = AzizOrangePrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = cardNumber,
                                    onValueChange = { if (it.length <= 19) cardNumber = it },
                                    modifier = Modifier.weight(1.8f),
                                    label = { Text("رقم البطاقة", fontSize = 10.sp) },
                                    placeholder = { Text("4111 2222 3333 4444", fontSize = 9.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                OutlinedTextField(
                                    value = cardExpiry,
                                    onValueChange = { if (it.length <= 5) cardExpiry = it },
                                    modifier = Modifier.weight(1f),
                                    label = { Text("MM/YY", fontSize = 10.sp) },
                                    placeholder = { Text("12/28", fontSize = 9.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                OutlinedTextField(
                                    value = cardCvv,
                                    onValueChange = { if (it.length <= 4) cardCvv = it },
                                    modifier = Modifier.weight(0.8f),
                                    label = { Text("CVV", fontSize = 10.sp) },
                                    placeholder = { Text("123", fontSize = 9.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                    PaymentOptionRow(
                        title = AppStrings.payCash(language),
                        subtitle = "ادفع نقداً للمندوب عزيز عند استلام طلبك",
                        icon = Icons.Default.Money,
                        isSelected = selectedPaymentMethod == PaymentMethodType.CASH_ON_DELIVERY,
                        onClick = { onSelectPaymentMethod(PaymentMethodType.CASH_ON_DELIVERY) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                    PaymentOptionRow(
                        title = "${AppStrings.payWallet(language)} $walletBalance ${AppStrings.currency(language)})",
                        subtitle = if (walletBalance >= total) "رصيدك كافٍ - دفع فوري بنقرة واحدة ⚡" else "الرصيد غير كافٍ، يمكنك شحن المحفظة",
                        icon = Icons.Default.AccountBalanceWallet,
                        isSelected = selectedPaymentMethod == PaymentMethodType.ALLO_PAY_WALLET,
                        onClick = { onSelectPaymentMethod(PaymentMethodType.ALLO_PAY_WALLET) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                    PaymentOptionRow(
                        title = AppStrings.payAppleGoogle(language),
                        subtitle = "الدفع بلمسة واحدة عبر محفظتك الرقمية",
                        icon = Icons.Default.PhoneAndroid,
                        isSelected = selectedPaymentMethod == PaymentMethodType.DIGITAL_WALLET,
                        onClick = { onSelectPaymentMethod(PaymentMethodType.DIGITAL_WALLET) }
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = AppStrings.orderSummary(language),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = AppStrings.subtotal(language),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$subtotal ${AppStrings.currency(language)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (hasEcommerceItems) {
                                if (language == AppLanguage.ARABIC) "شحن طرد تجارة إلكترونية مؤمن 📦" else "Livraison Colis E-Commerce Assuré 📦"
                            } else {
                                AppStrings.deliveryFee(language)
                            },
                            fontSize = 12.sp,
                            fontWeight = if (hasEcommerceItems) FontWeight.Bold else FontWeight.Normal,
                            color = if (hasEcommerceItems) AzizOrangePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$deliveryFee ${AppStrings.currency(language)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (hasEcommerceItems) AzizOrangePrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (discount > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${AppStrings.discount(language)} (${(discountPercent * 100).toInt()}%)",
                                fontSize = 13.sp,
                                color = AzizMint
                            )
                            Text(
                                text = "-$discount ${AppStrings.currency(language)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AzizMint
                            )
                        }
                    }

                    if (selectedTipAmount > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (language == AppLanguage.ARABIC) "إكرامية كابتن التوصيل 🛵" else "Pourboire livreur",
                                fontSize = 12.sp,
                                color = AzizMint
                            )
                            Text(
                                text = "+$selectedTipAmount ${AppStrings.currency(language)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AzizMint
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppStrings.total(language),
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "$total ${AppStrings.currency(language)}",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = AzizOrangePrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        onClick = { onOpenPricingModal?.invoke() },
                        shape = RoundedCornerShape(10.dp),
                        color = AzizOrangePrimary.copy(alpha = 0.08f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = AzizOrangePrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.ARABIC) "كم الخدمة؟ دليل التسعير والشفافية التامة 🔍" else "Tarifs & Transparence des frais 🔍",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AzizOrangePrimary
                                )
                            }
                            Text(
                                text = if (language == AppLanguage.ARABIC) "عرض التفاصيل ←" else "Détails →",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Split Bill with Friends (High-Tech Feature)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSplitBill = !showSplitBill },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AzizMint.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null,
                                    tint = AzizMint,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.ARABIC) "قسمة الفاتورة مع الأصدقاء (Split Bill) 👥" else "Partager l'addition (Split Bill) 👥",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (language == AppLanguage.ARABIC) "احسب نصيب كل شخص وشاركه عبر واتساب" else "Calculez la part de chacun et partagez sur WhatsApp",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = if (showSplitBill) "إخفاء ▲" else "احسب الآن ▼",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AzizMint
                        )
                    }

                    AnimatedVisibility(visible = showSplitBill) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            HorizontalDivider(modifier = Modifier.padding(bottom = 10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (language == AppLanguage.ARABIC) "عدد الأشخاص المشاركين:" else "Nombre de personnes :",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { if (splitPeopleCount > 2) splitPeopleCount-- },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Text("－", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        text = "$splitPeopleCount",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                    IconButton(
                                        onClick = { if (splitPeopleCount < 12) splitPeopleCount++ },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Text("＋", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            val perPerson = total / splitPeopleCount
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = AzizMint.copy(alpha = 0.12f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = if (language == AppLanguage.ARABIC) "نصيب كل فرد:" else "Part par personne :",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${"%.2f".format(perPerson)} ${AppStrings.currency(language)}",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 18.sp,
                                            color = AzizMint
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            val itemsSummary = cartItems.joinToString(", ") { "${it.quantity}x ${it.product.nameAr}" }
                                            com.example.data.system.ExternalShareHelper.shareSplitBill(
                                                context = context,
                                                total = total,
                                                count = splitPeopleCount,
                                                perPerson = perPerson,
                                                summary = itemsSummary
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AzizMint),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (language == AppLanguage.ARABIC) "مشاركة الحسبة 📲" else "Partager 📲",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = onPlaceOrder,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("confirm_and_place_order_button"),
                colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${AppStrings.confirmAndPay(language)} • $total ${AppStrings.currency(language)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    language: AppLanguage,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AzizOrangePrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                if (item.product.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = item.product.imageUrl,
                        contentDescription = item.product.nameAr,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(text = item.product.emoji, fontSize = 24.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                val name = when (language) {
                    AppLanguage.ARABIC -> item.product.nameAr
                    AppLanguage.FRENCH -> item.product.nameFr
                    AppLanguage.ENGLISH -> item.product.nameEn
                }
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "${item.product.price} ${AppStrings.currency(language)}",
                    fontSize = 12.sp,
                    color = AzizOrangePrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                IconButton(
                    onClick = onDecrease,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (item.quantity == 1) Icons.Default.Delete else Icons.Default.Remove,
                        contentDescription = "Decrease",
                        tint = if (item.quantity == 1) Color.Red else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = "${item.quantity}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                IconButton(
                    onClick = onIncrease,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase",
                        tint = AzizOrangePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentOptionRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = AzizOrangePrimary)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) AzizOrangePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
