package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.AppLanguage
import com.example.ui.theme.AzizAmberSecondary
import com.example.ui.theme.AzizMint
import com.example.ui.theme.AzizOrangePrimary

/**
 * Merchant KYC & Document Onboarding Dialog (Glovo & Temu Standard).
 * Allows Food Restaurant owners and E-Commerce sellers to register and submit
 * all required business licenses, CIN, sanitary permits, RIB, and storefront photos.
 */
@Composable
fun MerchantRegistrationDialog(
    language: AppLanguage,
    onDismissRequest: () -> Unit,
    onMerchantSubmitted: (
        storeName: String,
        merchantType: String, // "RESTAURANT" or "ECOMMERCE"
        iceNumber: String,
        phone: String,
        address: String,
        cinUri: String?,
        licenseUri: String?,
        hygieneUri: String?,
        ribUri: String?,
        storefrontUri: String?
    ) -> Unit
) {
    val context = LocalContext.current
    var merchantType by remember { mutableStateOf("ECOMMERCE") } // "FOOD" or "ECOMMERCE"
    var storeName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var iceNumber by remember { mutableStateOf("") }
    var storeAddress by remember { mutableStateOf("") }

    // Uploaded Document URIs (Photos)
    var cinPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var licensePhotoUri by remember { mutableStateOf<Uri?>(null) }
    var hygienePermitPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var ribPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var storefrontPhotoUri by remember { mutableStateOf<Uri?>(null) }

    var isSubmitting by remember { mutableStateOf(false) }
    var isSubmittedSuccess by remember { mutableStateOf(false) }

    // Android Zero-Permission Photo Pickers for Merchant Documents
    val cinPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) cinPhotoUri = uri }

    val licensePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) licensePhotoUri = uri }

    val hygienePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) hygienePermitPhotoUri = uri }

    val ribPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) ribPhotoUri = uri }

    val storefrontPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) storefrontPhotoUri = uri }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 20.dp)
                .testTag("merchant_registration_dialog")
        ) {
            if (isSubmittedSuccess) {
                // Success View
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(AzizMint.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = AzizMint,
                            modifier = Modifier.size(46.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (language == AppLanguage.ARABIC) "تم تقديم طلب الاعتماد بنجاح! 🛡️" else "Dossier Marchand Transmis avec Succès !",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (language == AppLanguage.ARABIC) {
                            "شكراً لانضمامك إلى شبكة ديليفيو DELIVIO! تم استلام وثائق متجرك (البطاقة الوطنية، السجل التجاري، ورخصة النشاط). سيقوم فريق التحقق والمصادقة بالرد وتفعيل المتجر خلال 24 ساعة لضمان الجودة والأمان لكافة الزبائن."
                        } else {
                            "Merci d'avoir rejoint DELIVIO ! Vos documents (CIN, RC, Licence, RIB) sont en cours de validation par notre équipe d'audit (délai : 24h). Vous recevrez une notification d'activation officielle."
                        },
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onDismissRequest,
                        colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (language == AppLanguage.ARABIC) "تم • العودة للرئيسية" else "Terminer",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            } else {
                // Application Form
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
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
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(AzizOrangePrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = AzizOrangePrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (language == AppLanguage.ARABIC) "انضم كتاجر معتمد في ديليفيو 🛡️" else "Devenir Marchand Certifié DELIVIO",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (language == AppLanguage.ARABIC) "مصادقة المتاجر وإيداع الوثائق الرسمية (معايير Glovo & Temu)" else "Vérification KYC & Licence commerciale officielle",
                                        fontSize = 10.sp,
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

                    // 1. Merchant Type Toggle: Food vs E-Commerce
                    item {
                        Text(
                            text = if (language == AppLanguage.ARABIC) "نوع النشاط التجاري:" else "Type d'activité commerciale :",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                onClick = { merchantType = "ECOMMERCE" },
                                shape = RoundedCornerShape(12.dp),
                                color = if (merchantType == "ECOMMERCE") AzizOrangePrimary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingBag,
                                        contentDescription = null,
                                        tint = if (merchantType == "ECOMMERCE") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (language == AppLanguage.ARABIC) "تجارة إلكترونية 🛍️" else "E-Commerce 🛍️",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (merchantType == "ECOMMERCE") Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Surface(
                                onClick = { merchantType = "FOOD" },
                                shape = RoundedCornerShape(12.dp),
                                color = if (merchantType == "FOOD") AzizOrangePrimary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Restaurant,
                                        contentDescription = null,
                                        tint = if (merchantType == "FOOD") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (language == AppLanguage.ARABIC) "مطعم / أكلات 🍔" else "Restaurant 🍔",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (merchantType == "FOOD") Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // 2. Business Information Fields
                    item {
                        OutlinedTextField(
                            value = storeName,
                            onValueChange = { storeName = it },
                            label = { Text(if (language == AppLanguage.ARABIC) "اسم المتجر أو العلامة التجارية" else "Nom de la boutique / Enseigne") },
                            leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = AzizOrangePrimary) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = ownerName,
                            onValueChange = { ownerName = it },
                            label = { Text(if (language == AppLanguage.ARABIC) "اسم صاحب المتجر أو المسير القانوني" else "Nom du gérant légal") },
                            leadingIcon = { Icon(Icons.Default.ContactPage, contentDescription = null, tint = AzizOrangePrimary) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text(if (language == AppLanguage.ARABIC) "رقم الهاتف / واتساب" else "Téléphone") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = iceNumber,
                                onValueChange = { iceNumber = it },
                                label = { Text(if (language == AppLanguage.ARABIC) "رقم الـ ICE / السجل" else "N° ICE / RC") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = storeAddress,
                            onValueChange = { storeAddress = it },
                            label = { Text(if (language == AppLanguage.ARABIC) "عنوان المتجر / المستودع في المحمدية" else "Adresse du magasin / entrepôt à Mohammedia") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // 3. Document Upload Section (Glovo & Temu KYC Standard)
                    item {
                        Text(
                            text = if (language == AppLanguage.ARABIC) "الوثائق الرسمية المطلوبة للمصادقة 📑" else "Documents officiels obligatoires :",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (language == AppLanguage.ARABIC) "يرجى إرفاق صور واضحة لوثائقك لتفعيل المتجر والشحن" else "Veuillez joindre des photos lisibles pour validation",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Document 1: CIN
                    item {
                        DocumentUploadRow(
                            title = if (language == AppLanguage.ARABIC) "بطاقة التعريف الوطنية (CIN للمسؤول)" else "Carte d'Identité Nationale (CIN Gérant)",
                            subtitle = if (language == AppLanguage.ARABIC) "صورة واضحة للوجهين الأمامي والخلفي" else "Photo recto-verso bien lisible",
                            icon = Icons.Default.ContactPage,
                            uri = cinPhotoUri,
                            onPickClick = {
                                cinPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Document 2: RC / Auto-entrepreneur
                    item {
                        DocumentUploadRow(
                            title = if (language == AppLanguage.ARABIC) "السجل التجاري أو بطاقة المقاول الذاتي" else "Registre de Commerce ou Carte Auto-Entrepreneur",
                            subtitle = if (language == AppLanguage.ARABIC) "إثبات النشاط التجاري الرسمي (RC / Patente)" else "Attestation officielle d'activité commerciale",
                            icon = Icons.Default.Description,
                            uri = licensePhotoUri,
                            onPickClick = {
                                licensePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Document 3: Sanitary permit (for Food) or Trademark/License (for E-Commerce)
                    item {
                        DocumentUploadRow(
                            title = if (merchantType == "FOOD") {
                                if (language == AppLanguage.ARABIC) "الشهادة الصحية ورخصة السلامة الغذائية" else "Certificat d'Hygiène & Sécurité Alimentaire"
                            } else {
                                if (language == AppLanguage.ARABIC) "شهادة منشأ المنتجات أو ترخيص العلامة التجارية" else "Certificat d'Authenticité & Facture Fournisseur"
                            },
                            subtitle = if (merchantType == "FOOD") {
                                if (language == AppLanguage.ARABIC) "مطابقة معايير النظافة والجودة للمطاعم" else "Conformité sanitaire obligatoire"
                            } else {
                                if (language == AppLanguage.ARABIC) "ضمان جودة السلع ومطابقتها للمواصفات" else "Garantie des produits originaux"
                            },
                            icon = Icons.Default.HealthAndSafety,
                            uri = hygienePermitPhotoUri,
                            onPickClick = {
                                hygienePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Document 4: Bank RIB Attestation
                    item {
                        DocumentUploadRow(
                            title = if (language == AppLanguage.ARABIC) "شهادة الحساب البنكي (Relevé RIB)" else "Attestation RIB Bancaire Officielle",
                            subtitle = if (language == AppLanguage.ARABIC) "لتحويل مستحقات المبيعات وأرباح المتجر أسبوعياً" else "Pour versement hebdomadaire des ventes",
                            icon = Icons.Default.LocalAtm,
                            uri = ribPhotoUri,
                            onPickClick = {
                                ribPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Document 5: Storefront / Inventory Photos
                    item {
                        DocumentUploadRow(
                            title = if (merchantType == "FOOD") {
                                if (language == AppLanguage.ARABIC) "صورة واجهة المطعم والمطبخ أو اللوجو" else "Photo Façade Restaurant & Cuisine"
                            } else {
                                if (language == AppLanguage.ARABIC) "صور المستودع أو عينات المنتجات واللوجو" else "Photos Entrepôt & Échantillons Produits"
                            },
                            subtitle = if (language == AppLanguage.ARABIC) "لإبراز متجرك أمام الزبائن في التطبيق" else "Pour la fiche magasin dans l'application",
                            icon = Icons.Default.AddPhotoAlternate,
                            uri = storefrontPhotoUri,
                            onPickClick = {
                                storefrontPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                    }

                    // Submit Button
                    item {
                        Button(
                            onClick = {
                                if (storeName.isBlank() || phone.isBlank()) {
                                    val err = if (language == AppLanguage.ARABIC) "يرجى ملء اسم المتجر ورقم الهاتف!" else "Veuillez renseigner le nom et le téléphone !"
                                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                isSubmitting = true
                                onMerchantSubmitted(
                                    storeName,
                                    merchantType,
                                    iceNumber,
                                    phone,
                                    storeAddress,
                                    cinPhotoUri?.toString(),
                                    licensePhotoUri?.toString(),
                                    hygienePermitPhotoUri?.toString(),
                                    ribPhotoUri?.toString(),
                                    storefrontPhotoUri?.toString()
                                )
                                isSubmitting = false
                                isSubmittedSuccess = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("submit_merchant_application_btn")
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (language == AppLanguage.ARABIC) "إرسال الملف والمصادقة على المتجر 🚀" else "Soumettre le Dossier Marchand 🚀",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentUploadRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    uri: Uri?,
    onPickClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPickClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (uri != null) AzizMint.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (uri != null) Icons.Default.CheckCircle else icon,
                        contentDescription = null,
                        tint = if (uri != null) AzizMint else AzizOrangePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (uri != null) AzizMint.copy(alpha = 0.2f) else AzizOrangePrimary.copy(alpha = 0.15f)
            ) {
                Text(
                    text = if (uri != null) "مرفق ✓" else "إرفاق +",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (uri != null) AzizMint else AzizOrangePrimary
                )
            }
        }
    }
}
