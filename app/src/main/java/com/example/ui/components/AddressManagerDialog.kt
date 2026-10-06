package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.AddressEntity
import com.example.data.model.AppLanguage
import com.example.ui.theme.AzizGreenPrimary
import com.example.ui.theme.AzizMint
import com.example.ui.theme.AzizOrangePrimary

@Composable
fun AddressManagerDialog(
    language: AppLanguage,
    currentAddress: String,
    currentCity: String,
    savedAddresses: List<AddressEntity>,
    onSelectAddress: (String) -> Unit,
    onSelectCity: (String) -> Unit,
    onAddNewAddress: (title: String, type: String, city: String, neighborhood: String, street: String, notes: String) -> Unit,
    onDeleteAddress: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var showAddForm by remember { mutableStateOf(false) }

    // New address inputs
    var newTitle by remember { mutableStateOf("") }
    var newType by remember { mutableStateOf("HOME") }
    var newNeighborhood by remember { mutableStateOf("حي الوفاء") }
    var newStreet by remember { mutableStateOf("") }
    var newNotes by remember { mutableStateOf("") }

    val mohammediaNeighborhoods = listOf(
        "حي الوفاء",
        "حي العالية",
        "حي ميرامار (الكورنيش)",
        "حي مونيكا",
        "شارع الحسن الثاني",
        "شارع المقاومة",
        "القصبة التاريخية",
        "بارك المدن المتوأمة",
        "حي ديار المنصور",
        "المنطقة الصناعية"
    )

    val moroccanCities = listOf(
        "المحمدية",
        "الدار البيضاء",
        "الرباط",
        "مراكش",
        "طنجة",
        "أكادير"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .height(620.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("address_manager_modal")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = AzizOrangePrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = AzizOrangePrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = when (language) {
                                    AppLanguage.ARABIC -> "عناوين التوصيل والموقع 📍"
                                    AppLanguage.FRENCH -> "Adresses de livraison 📍"
                                    AppLanguage.ENGLISH -> "Delivery Addresses 📍"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "المحمدية، المغرب (Mohammedia)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // City Selector Row
                Text(
                    text = "المدينة:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(moroccanCities) { city ->
                        val isSelected = currentCity.contains(city)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectCity(city) },
                            label = { Text(city, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AzizOrangePrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // GPS Detect Button
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = AzizMint.copy(alpha = 0.1f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val detected = "حي ميرامار، قرب شاطئ المحمدية 🇲🇦"
                                    onSelectAddress(detected)
                                    onDismiss()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.MyLocation, contentDescription = null, tint = AzizMint)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "استخدام موقعي الحالي عبر GPS",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = AzizMint
                                    )
                                    Text(
                                        text = "تحديد النقطة تلقائياً بالمحمدية بدقة عالية",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Saved Addresses List
                    item {
                        Text(
                            text = "العناوين المحفوظة:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Default hardcoded address items if DB list is empty
                    val addressesToShow = if (savedAddresses.isNotEmpty()) savedAddresses else listOf(
                        AddressEntity(
                            id = "addr_1",
                            title = "المنزل (المحمدية)",
                            type = "HOME",
                            city = "المحمدية",
                            neighborhood = "حي الوفاء",
                            streetDetails = "شارع المقاومة، إقامة الياسمين، عمارة ب رقم 4",
                            isDefault = true
                        ),
                        AddressEntity(
                            id = "addr_2",
                            title = "مكتب العمل",
                            type = "WORK",
                            city = "المحمدية",
                            neighborhood = "وسط المدينة",
                            streetDetails = "شارع الحسن الثاني، قرب حديقة المدن المتوأمة",
                            isDefault = false
                        )
                    )

                    items(addressesToShow) { addr ->
                        val isCurrent = currentAddress.contains(addr.neighborhood) || currentAddress.contains(addr.title)
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) AzizOrangePrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = if (isCurrent) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AzizOrangePrimary)) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectAddress("${addr.title} - ${addr.streetDetails}، ${addr.neighborhood}، ${addr.city}")
                                    onDismiss()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (addr.type == "WORK") Icons.Default.Business else Icons.Default.Home,
                                    contentDescription = null,
                                    tint = if (isCurrent) AzizOrangePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = addr.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isCurrent) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = AzizOrangePrimary
                                            ) {
                                                Text(
                                                    text = "الحالي ✓",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${addr.neighborhood} - ${addr.streetDetails}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 15.sp
                                    )
                                }

                                if (savedAddresses.size > 1) {
                                    IconButton(
                                        onClick = { onDeleteAddress(addr.id) },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color.Red.copy(alpha = 0.7f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Add New Address Section
                    item {
                        if (!showAddForm) {
                            OutlinedButton(
                                onClick = { showAddForm = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("إضافة عنوان جديد بالمحمدية", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = CardDefaults.outlinedCardBorder(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "بيانات العنوان الجديد:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Title
                                    OutlinedTextField(
                                        value = newTitle,
                                        onValueChange = { newTitle = it },
                                        label = { Text("تسمية العنوان (مثال: بيت العائلة، الشقة)", fontSize = 11.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Neighborhood selection
                                    Text("الحي بالمحمدية:", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        items(mohammediaNeighborhoods) { n ->
                                            FilterChip(
                                                selected = newNeighborhood == n,
                                                onClick = { newNeighborhood = n },
                                                label = { Text(n, fontSize = 10.sp) }
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Street
                                    OutlinedTextField(
                                        value = newStreet,
                                        onValueChange = { newStreet = it },
                                        label = { Text("الشارع، رقم العمارة / الطابق", fontSize = 11.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Notes
                                    OutlinedTextField(
                                        value = newNotes,
                                        onValueChange = { newNotes = it },
                                        label = { Text("ملاحظات للمندوب (اختياري)", fontSize = 11.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                if (newTitle.isNotBlank() && newStreet.isNotBlank()) {
                                                    onAddNewAddress(newTitle, newType, currentCity, newNeighborhood, newStreet, newNotes)
                                                    onSelectAddress("$newTitle - $newStreet، $newNeighborhood، $currentCity")
                                                    showAddForm = false
                                                    onDismiss()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("حفظ واختيار", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = { showAddForm = false },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("إلغاء", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
