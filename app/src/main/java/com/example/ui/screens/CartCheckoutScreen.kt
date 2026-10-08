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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PhoneAndroid
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
    onUseCurrentGpsLocation: (() -> Unit)? = null
) {
    BackHandler { onBack() }

    var promoInput by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }

    val subtotal = cartItems.sumOf { it.product.price * it.quantity }
    val deliveryFee = if (cartItems.isNotEmpty()) 10.0 else 0.0
    val discount = subtotal * discountPercent
    val total = (subtotal - discount + deliveryFee).coerceAtLeast(0.0)

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
        // Title
        item {
            Text(
                text = AppStrings.cartTitle(language),
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Cart items list
        items(cartItems) { item ->
            CartItemRow(
                item = item,
                language = language,
                onIncrease = { onUpdateQuantity(item.product.id, 1) },
                onDecrease = { onUpdateQuantity(item.product.id, -1) }
            )
        }

        // Delivery Address Card
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

                    Spacer(modifier = Modifier.height(8.dp))
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

        // Promo Code Card
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

        // Secure Payment Method Selector
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

                    // 1. Credit Card
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

                    // 2. Cash on Delivery
                    PaymentOptionRow(
                        title = AppStrings.payCash(language),
                        subtitle = "ادفع نقداً للمندوب عزيز عند استلام طلبك",
                        icon = Icons.Default.Money,
                        isSelected = selectedPaymentMethod == PaymentMethodType.CASH_ON_DELIVERY,
                        onClick = { onSelectPaymentMethod(PaymentMethodType.CASH_ON_DELIVERY) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                    // 3. ALLO Pay Wallet
                    PaymentOptionRow(
                        title = "${AppStrings.payWallet(language)} $walletBalance ${AppStrings.currency(language)})",
                        subtitle = if (walletBalance >= total) "رصيدك كافٍ - دفع فوري بنقرة واحدة ⚡" else "الرصيد غير كافٍ، يمكنك شحن المحفظة من الحساب",
                        icon = Icons.Default.AccountBalanceWallet,
                        isSelected = selectedPaymentMethod == PaymentMethodType.ALLO_PAY_WALLET,
                        onClick = { onSelectPaymentMethod(PaymentMethodType.ALLO_PAY_WALLET) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                    // 4. Google Pay / Apple Pay
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

        // Cost Breakdown & Summary
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
                            text = AppStrings.deliveryFee(language),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$deliveryFee ${AppStrings.currency(language)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
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
                }
            }
        }

        // Place Order CTA Button
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

            // Quantity buttons
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
