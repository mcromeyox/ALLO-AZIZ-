package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AppLanguage
import com.example.ui.theme.AzizAmberSecondary
import com.example.ui.theme.AzizMint
import com.example.ui.theme.AzizOrangePrimary

/**
 * Transparent Pricing & Service Fee Breakdown Dialog ("كم الخدمة؟")
 * Explains clearly the difference between fast food delivery (8 DH),
 * e-commerce parcel shipping (18 DH), and express personal courier (12 DH).
 */
@Composable
fun PricingTransparencyDialog(
    language: AppLanguage,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
                .testTag("pricing_transparency_dialog")
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(AzizOrangePrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = AzizOrangePrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.ARABIC) "كم الخدمة؟ • دليل الأسعار الشامل 💡"
                                    else "Tarifs & Frais de Service DELIVIO",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (language == AppLanguage.ARABIC) "شفافية مطلقة بدون أي رسوم خفية"
                                    else "Transparence totale, aucun frais caché",
                                    fontSize = 11.sp,
                                    color = AzizOrangePrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        IconButton(onClick = onDismissRequest) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // 1. Food Delivery
                item {
                    PriceBreakdownCard(
                        icon = Icons.Default.DeliveryDining,
                        iconColor = AzizOrangePrimary,
                        title = if (language == AppLanguage.ARABIC) "توصيل المأكولات والمطاعم 🍔" else "Livraison Restaurants & Repas",
                        price = "8.00 د.م",
                        time = if (language == AppLanguage.ARABIC) "20 - 30 دقيقة" else "20-30 min",
                        description = if (language == AppLanguage.ARABIC)
                            "توصيل ساخن وفوري من أفضل مطاعم المحمدية مباشرة إلى باب منزلك بحافظة حرارية معزولة."
                        else "Livraison chaude immédiate depuis vos restaurants préférés avec sac isotherme."
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // 2. E-Commerce Parcel Shipping
                item {
                    PriceBreakdownCard(
                        icon = Icons.Default.LocalShipping,
                        iconColor = Color(0xFF6366F1),
                        title = if (language == AppLanguage.ARABIC) "شحن طرود التجارة الإلكترونية 📦" else "Expédition Colis & E-Commerce",
                        price = "18.00 د.م",
                        time = if (language == AppLanguage.ARABIC) "30 - 45 دقيقة" else "30-45 min",
                        description = if (language == AppLanguage.ARABIC)
                            "رسوم مخصصة لشحن الطرود وتأمين الأجهزة والملابس والعطور، مع تغليف مضاد للصدمات وتتبع مسار الشحنة وضمان الفحص والاستبدال 7 أيام."
                        else "Tarif colis incluant assurance transport, emballage antichoc, suivi et garantie retour 7 jours."
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // 3. Express Errand Courier
                item {
                    PriceBreakdownCard(
                        icon = Icons.Default.Speed,
                        iconColor = AzizAmberSecondary,
                        title = if (language == AppLanguage.ARABIC) "خدمة ديليفيو إكسبريس ⚡ (DELIVIO Express)" else "DELIVIO Express - Courses sur mesure",
                        price = "12 - 15 د.م",
                        time = if (language == AppLanguage.ARABIC) "15 - 25 دقيقة" else "15-25 min",
                        description = if (language == AppLanguage.ARABIC)
                            "كابتن خاص يقضي لك أي غرض أو وثيقة أو يشتري لك من أي صيدلية أو متجر غير مسجل بالتطبيق."
                        else "Un coursier dédié pour récupérer vos colis, clés, pharmacie ou courses personnalisées."
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // 4. Zero Platform Service Fee
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = AzizMint.copy(alpha = 0.12f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AzizMint),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (language == AppLanguage.ARABIC) "رسوم المنصة وتأمين العمليات: 0 د.م (مجاناً) 🎉" else "Frais de service plateforme : 0 DH (Gratuit)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = AzizMint
                                )
                                Text(
                                    text = if (language == AppLanguage.ARABIC) "لا نفرض أي رسوم خدمة إضافية غير معلنة. الدفع يتم نقداً أو بالبطاقة أو عبر محفظة DELIVIO Pay."
                                    else "Aucun frais de gestion caché. Paiement à la livraison, portefeuille DELIVIO Pay ou par carte sécurisée.",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // 5. Prime Subscription
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF59E0B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (language == AppLanguage.ARABIC) "عرض ديليفيو برايم (DELIVIO PRIME) 👑" else "DELIVIO PRIME 👑",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = if (language == AppLanguage.ARABIC) "اشتراك شهري بـ 29 د.م يمنحك توصيلاً مجانياً غير محدود لجميع الطلبات فوق 50 درهم!"
                                    else "29 DH/mois pour des livraisons gratuites illimitées dès 50 DH d'achats !",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Close Button
                item {
                    Button(
                        onClick = onDismissRequest,
                        colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.ARABIC) "فهمت • العودة للتسوق" else "Compris",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceBreakdownCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    price: String,
    time: String,
    description: String
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(iconColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = iconColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = price,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = iconColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "⏱️ وقت التوصيل التقديري: $time",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
