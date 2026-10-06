package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.localization.AppStrings
import com.example.data.model.AppLanguage
import com.example.data.model.MealOptionItem
import com.example.data.model.Product
import com.example.ui.theme.AzizGreenPrimary
import com.example.ui.theme.AzizMint

@Composable
fun ProductCustomizationDialog(
    product: Product,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onConfirmAddToCart: (quantity: Int, selectedOptions: List<String>, extraPrice: Double, notes: String) -> Unit
) {
    var quantity by remember { mutableIntStateOf(1) }
    var specialInstructions by remember { mutableStateOf("") }

    // Map: OptionGroupId -> Set of Selected Option IDs
    val selectedOptionsMap = remember {
        mutableStateMapOf<String, MutableSet<String>>().apply {
            // Pre-select first item of required groups
            product.optionGroups.forEach { group ->
                if (group.isRequired && group.options.isNotEmpty()) {
                    put(group.id, mutableSetOf(group.options.first().id))
                } else {
                    put(group.id, mutableSetOf())
                }
            }
        }
    }

    // Calculate extra options price sum
    var extraPrice = 0.0
    val selectedOptionNames = mutableListOf<String>()

    product.optionGroups.forEach { group ->
        val selectedIds = selectedOptionsMap[group.id] ?: emptySet()
        group.options.filter { selectedIds.contains(it.id) }.forEach { opt ->
            extraPrice += opt.extraPrice
            val name = when (language) {
                AppLanguage.ARABIC -> opt.nameAr
                AppLanguage.FRENCH -> opt.nameFr
                AppLanguage.ENGLISH -> opt.nameEn
            }
            selectedOptionNames.add(name)
        }
    }

    val singleUnitPrice = product.price + extraPrice
    val totalPrice = singleUnitPrice * quantity

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .height(640.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("product_customization_modal"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header with image and close button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    if (product.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = product.imageUrl,
                            contentDescription = product.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = product.emoji, fontSize = 60.sp)
                        }
                    }

                    // Close Button
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    ) {
                        IconButton(onClick = onDismiss, modifier = Modifier.size(34.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                // Scrollable content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Title, Description & Base Price
                    item {
                        Column {
                            val name = when (language) {
                                AppLanguage.ARABIC -> product.nameAr
                                AppLanguage.FRENCH -> product.nameFr
                                AppLanguage.ENGLISH -> product.nameEn
                            }
                            val desc = when (language) {
                                AppLanguage.ARABIC -> product.descriptionAr
                                AppLanguage.FRENCH -> product.descriptionFr
                                AppLanguage.ENGLISH -> product.descriptionEn
                            }

                            Text(
                                text = name,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = desc,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${product.price} ${AppStrings.currency(language)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = AzizGreenPrimary
                            )
                        }
                    }

                    // Option Groups (Size, Cheese, Sauces, Extras, Drinks)
                    items(product.optionGroups) { group ->
                        val groupTitle = when (language) {
                            AppLanguage.ARABIC -> group.titleAr
                            AppLanguage.FRENCH -> group.titleFr
                            AppLanguage.ENGLISH -> group.titleEn
                        }

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = groupTitle,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (group.isRequired) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = AzizGreenPrimary.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = "مطلوب",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AzizGreenPrimary
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                group.options.forEach { option ->
                                    val optName = when (language) {
                                        AppLanguage.ARABIC -> option.nameAr
                                        AppLanguage.FRENCH -> option.nameFr
                                        AppLanguage.ENGLISH -> option.nameEn
                                    }
                                    val isSelected = selectedOptionsMap[group.id]?.contains(option.id) == true

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                val set = selectedOptionsMap[group.id]?.toMutableSet() ?: mutableSetOf()
                                                if (group.isMultiSelect) {
                                                    if (set.contains(option.id)) set.remove(option.id) else set.add(option.id)
                                                } else {
                                                    set.clear()
                                                    set.add(option.id)
                                                }
                                                selectedOptionsMap[group.id] = set
                                            }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (group.isMultiSelect) {
                                            Checkbox(
                                                checked = isSelected,
                                                onCheckedChange = { checked ->
                                                    val set = selectedOptionsMap[group.id]?.toMutableSet() ?: mutableSetOf()
                                                    if (checked) set.add(option.id) else set.remove(option.id)
                                                    selectedOptionsMap[group.id] = set
                                                },
                                                colors = CheckboxDefaults.colors(checkedColor = AzizGreenPrimary)
                                            )
                                        } else {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = {
                                                    selectedOptionsMap[group.id] = mutableSetOf(option.id)
                                                },
                                                colors = RadioButtonDefaults.colors(selectedColor = AzizGreenPrimary)
                                            )
                                        }

                                        Text(
                                            text = optName,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            modifier = Modifier.weight(1f)
                                        )

                                        if (option.extraPrice > 0.0) {
                                            Text(
                                                text = "+${option.extraPrice} د.م",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AzizMint
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Special Instructions
                    item {
                        OutlinedTextField(
                            value = specialInstructions,
                            onValueChange = { specialInstructions = it },
                            label = { Text("تعليمات خاصة للمطبخ") },
                            placeholder = { Text("مثال: بدون بصل، الصوص على حدة، خبز محمص إضافي") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                HorizontalDivider()

                // Bottom bar with quantity and Add Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Quantity selector
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(4.dp)
                    ) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = "$quantity",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(
                            onClick = { quantity++ },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            onConfirmAddToCart(quantity, selectedOptionNames, extraPrice, specialInstructions)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AzizGreenPrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("confirm_add_to_cart_btn")
                    ) {
                        Text(
                            text = "إضافة للسلة • $totalPrice ${AppStrings.currency(language)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
