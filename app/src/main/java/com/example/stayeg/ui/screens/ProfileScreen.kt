package com.example.stayeg.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import coil.compose.AsyncImage
import com.example.stayeg.data.model.KYCStatus
import com.example.stayeg.ui.components.StayEgAnimatedBenefitStrip
import com.example.stayeg.ui.navigation.Screen
import com.example.stayeg.ui.theme.*
import com.example.stayeg.ui.viewmodel.StayEgViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: StayEgViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val interestLeads by viewModel.interestLeads.collectAsState()

    // 0 = Personal Info, 1 = Digital KYC, 2 = Account & Legal
    var activeSectionTab by remember { mutableIntStateOf(0) }
    var showKycDialog by remember { mutableStateOf(false) }

    // Editable Profile States
    var nameInput by remember(currentUser?.name) { mutableStateOf(currentUser?.name ?: "Vikram Singh") }
    var phoneInput by remember(currentUser?.phone) { mutableStateOf(currentUser?.phone ?: "+91 91234 56789") }
    var emailInput by remember(currentUser?.email) { mutableStateOf(currentUser?.email ?: "vikram@stayeg.in") }
    var genderInput by remember(currentUser?.gender) { mutableStateOf(currentUser?.gender ?: "Male") }
    var cityInput by remember(currentUser?.city) { mutableStateOf(currentUser?.city ?: "Bengaluru") }
    var areaInput by remember(currentUser?.currentArea) { mutableStateOf(currentUser?.currentArea ?: "Koramangala 4th Block") }
    var occupationInput by remember(currentUser?.occupation) { mutableStateOf(currentUser?.occupation ?: "Software Engineer") }
    var orgInput by remember(currentUser?.organizationName) { mutableStateOf(currentUser?.organizationName ?: "") }
    var workModeInput by remember(currentUser?.workMode) { mutableStateOf(currentUser?.workMode ?: "Hybrid") }
    var foodPrefInput by remember(currentUser?.foodPreference) { mutableStateOf(currentUser?.foodPreference ?: "Veg & Non-Veg") }
    var sleepInput by remember(currentUser?.sleepSchedule) { mutableStateOf(currentUser?.sleepSchedule ?: "Flexible") }
    var languagesInput by remember(currentUser?.languagesSpoken) { mutableStateOf(currentUser?.languagesSpoken ?: "English, Hindi, Kannada") }
    var emergencyNameInput by remember(currentUser?.emergencyContactName) { mutableStateOf(currentUser?.emergencyContactName ?: "") }
    var emergencyPhoneInput by remember(currentUser?.emergencyContactPhone) { mutableStateOf(currentUser?.emergencyContactPhone ?: "") }
    var bioInput by remember(currentUser?.bio) { mutableStateOf(currentUser?.bio ?: "") }
    var avatarUrlInput by remember(currentUser?.avatarUrl) { mutableStateOf(currentUser?.avatarUrl ?: "") }

    // Digital KYC States
    var kycDocType by remember(currentUser?.kycDocumentType) { mutableStateOf(currentUser?.kycDocumentType ?: "Aadhaar Card") }
    var aadhaarInput by remember(currentUser?.aadhaarNumber) { mutableStateOf(currentUser?.aadhaarNumber ?: "") }
    var panInput by remember(currentUser?.panNumber) { mutableStateOf(currentUser?.panNumber ?: "") }
    var guardianInput by remember(currentUser?.guardianName) { mutableStateOf(currentUser?.guardianName ?: "") }
    var permanentAddrInput by remember(currentUser?.permanentAddress) { mutableStateOf(currentUser?.permanentAddress ?: "") }
    var kycFrontUri by remember(currentUser?.kycFrontPhotoUri) { mutableStateOf(currentUser?.kycFrontPhotoUri ?: "") }
    var kycBackUri by remember(currentUser?.kycBackPhotoUri) { mutableStateOf(currentUser?.kycBackPhotoUri ?: "") }
    var kycSelfieUri by remember(currentUser?.kycSelfiePhotoUri) { mutableStateOf(currentUser?.kycSelfiePhotoUri ?: "") }
    var kycConsentChecked by remember { mutableStateOf(true) }
    var kycValidationError by remember { mutableStateOf<String?>(null) }

    // Android Photo Pickers
    val profileAvatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            avatarUrlInput = uri.toString()
            viewModel.updateProfilePhoto(avatarUrlInput)
        }
    }

    val kycFrontPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            kycFrontUri = uri.toString()
            viewModel.uploadKycDocument("FRONT", kycFrontUri)
        }
    }

    val kycBackPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            kycBackUri = uri.toString()
            viewModel.uploadKycDocument("BACK", kycBackUri)
        }
    }

    val kycSelfiePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            kycSelfieUri = uri.toString()
            viewModel.uploadKycDocument("SELFIE", kycSelfieUri)
        }
    }

    val kycStatus = currentUser?.kycStatus ?: KYCStatus.NOT_STARTED
    val isKycVerified = kycStatus == KYCStatus.VERIFIED
    val uploadedDocsCount = currentUser?.uploadedKycDocsCount ?: 0
    val completionPercent = currentUser?.profileCompletionPercent ?: 55

    val kycStatusDisplayLabel = when (kycStatus) {
        KYCStatus.VERIFIED -> "KYC Verified ✓"
        KYCStatus.PENDING -> "Verification Pending"
        KYCStatus.REJECTED -> "Re-upload Required"
        KYCStatus.NOT_STARTED -> "KYC Not Verified"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftGalleryBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "My Profile",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = TextPrimary,
                        letterSpacing = (-0.4).sp
                    )
                    Text(
                        text = "Personal details & identity verification",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Surface(
                    onClick = { viewModel.openAuthOnboarding() },
                    shape = RoundedCornerShape(999.dp),
                    color = SoftSagePill,
                    border = BorderStroke(1.dp, TealPrimary.copy(alpha = 0.3f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockPerson,
                            contentDescription = null,
                            tint = TealDark,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Switch Account",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = TealDark
                        )
                    }
                }
            }
        }

        // 2. Profile Hero Card
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = TealDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                    .testTag("profile_user_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(TealDeep, TealDark, TealPrimary)
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                onClick = { activeSectionTab = 1 },
                                shape = RoundedCornerShape(999.dp),
                                color = when (kycStatus) {
                                    KYCStatus.VERIFIED -> MintSoft
                                    KYCStatus.REJECTED -> ErrorRedLight
                                    else -> SoftGoldPill
                                },
                                modifier = Modifier.testTag("realtime_kyc_status_badge")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = when (kycStatus) {
                                            KYCStatus.VERIFIED -> Icons.Default.VerifiedUser
                                            KYCStatus.PENDING -> Icons.Default.HourglassTop
                                            KYCStatus.REJECTED -> Icons.Default.ErrorOutline
                                            KYCStatus.NOT_STARTED -> Icons.Default.GppMaybe
                                        },
                                        contentDescription = null,
                                        tint = when (kycStatus) {
                                            KYCStatus.VERIFIED -> TealDeep
                                            KYCStatus.REJECTED -> ErrorRed
                                            else -> GoldDark
                                        },
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = kycStatusDisplayLabel,
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = when (kycStatus) {
                                            KYCStatus.VERIFIED -> TealDeep
                                            KYCStatus.REJECTED -> ErrorRed
                                            else -> GoldDark
                                        }
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = PureWhite.copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = "$completionPercent% Complete",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MintSoft,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(66.dp)
                                    .clickable {
                                        profileAvatarPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(MintSoft),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (avatarUrlInput.isNotBlank()) {
                                        AsyncImage(
                                            model = avatarUrlInput,
                                            contentDescription = "Profile Picture",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Text(
                                            text = (currentUser?.name ?: "V").take(1).uppercase(),
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = TealDeep
                                        )
                                    }
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = SoftGoldAccent,
                                    border = BorderStroke(1.5.dp, TealDeep),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.BottomEnd)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.PhotoCamera,
                                            contentDescription = "Upload Photo",
                                            tint = PureWhite,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentUser?.name?.ifBlank { "StayEg Resident" } ?: "StayEg Resident",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = PureWhite
                                )
                                Text(
                                    text = "${currentUser?.occupation?.ifBlank { "Add Profession" } ?: "Add Profession"}" +
                                            if (!currentUser?.organizationName.isNullOrBlank()) " • ${currentUser?.organizationName}" else "",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 12.sp,
                                    color = MintSoft,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "📍 ${currentUser?.currentArea?.ifBlank { "Koramangala" } ?: "Koramangala"}, ${currentUser?.city ?: "Bengaluru"} • ${currentUser?.workMode ?: "Hybrid"}",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 11.sp,
                                    color = PureWhite.copy(alpha = 0.85f)
                                )
                            }
                        }

                        if (!currentUser?.bio.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "\"${currentUser?.bio}\"",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 11.sp,
                                color = CreamLight,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = TealDeep.copy(alpha = 0.58f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp, horizontal = 10.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable { activeSectionTab = 1 }
                                ) {
                                    Text(
                                        text = when (kycStatus) {
                                            KYCStatus.VERIFIED -> "Verified ✓"
                                            KYCStatus.PENDING -> "In Review"
                                            KYCStatus.REJECTED -> "Action Needed"
                                            KYCStatus.NOT_STARTED -> "Not Verified"
                                        },
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = when (kycStatus) {
                                            KYCStatus.VERIFIED -> MintSoft
                                            KYCStatus.REJECTED -> ErrorRedLight
                                            else -> SoftGoldAccent
                                        }
                                    )
                                    Text(
                                        text = "Digital KYC",
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 10.sp,
                                        color = PureWhite.copy(alpha = 0.7f)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(24.dp)
                                        .background(PureWhite.copy(alpha = 0.16f))
                                )

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = currentUser?.foodPreference ?: "Veg & Non-Veg",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = PureWhite
                                    )
                                    Text(
                                        text = "Food Pref",
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 10.sp,
                                        color = PureWhite.copy(alpha = 0.7f)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(24.dp)
                                        .background(PureWhite.copy(alpha = 0.16f))
                                )

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "₹0 Free",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = CreamLight
                                    )
                                    Text(
                                        text = "Brokerage",
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 10.sp,
                                        color = PureWhite.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Clean 3-Tab Switcher: Personal Info | Digital KYC | Account
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SoftSagePill)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    0 to "Personal Info",
                    1 to "Digital KYC",
                    2 to "Account"
                ).forEach { (idx, label) ->
                    val selected = activeSectionTab == idx
                    Surface(
                        onClick = { activeSectionTab = idx },
                        shape = RoundedCornerShape(10.dp),
                        color = if (selected) TealDark else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .then(if (idx == 1) Modifier.testTag("kyc_card") else Modifier)
                    ) {
                        Text(
                            text = label,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp,
                            color = if (selected) PureWhite else TealDark,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 9.dp, horizontal = 4.dp)
                        )
                    }
                }
            }
        }

        // ─── TAB 0: PERSONAL & WORK DETAILS ───
        if (activeSectionTab == 0) {
            // Compact KYC Status Prompt Card (links directly to Digital KYC tab)
            item {
                Surface(
                    onClick = { activeSectionTab = 1 },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isKycVerified) SoftSagePill else SoftGoldPill,
                    border = BorderStroke(1.dp, if (isKycVerified) TealPrimary else SoftGoldAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isKycVerified) Icons.Default.VerifiedUser else Icons.Default.Badge,
                                contentDescription = null,
                                tint = if (isKycVerified) TealPrimary else GoldDark,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isKycVerified) "Identity Verification Complete" else "Complete Your Digital KYC",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isKycVerified) TealDark else GoldDark
                                )
                                Text(
                                    text = if (isKycVerified) {
                                        "${currentUser?.kycDocumentType ?: "Aadhaar Card"} & PAN verified"
                                    } else {
                                        "Upload your Government ID & PAN for instant PG check-in"
                                    },
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Text(
                            text = if (isKycVerified) "View →" else "Verify →",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isKycVerified) TealDark else GoldDark
                        )
                    }
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = PureWhite,
                    border = BorderStroke(1.dp, WhisperBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "Personal & Work Details",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Keep your contact info, workplace, and stay preferences up to date",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        // A. Profile Photo Upload
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = SoftCardSurface,
                            border = BorderStroke(1.dp, WhisperBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(CircleShape)
                                            .background(SoftSagePill),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (avatarUrlInput.isNotBlank()) {
                                            AsyncImage(
                                                model = avatarUrlInput,
                                                contentDescription = "Profile Photo",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.AddAPhoto,
                                                contentDescription = null,
                                                tint = TealPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Profile Photo",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = if (avatarUrlInput.isNotBlank()) "Tap change to update photo" else "Upload a clear photo from gallery",
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 10.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        profileAvatarPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = TealDark),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("upload_profile_photo_btn")
                                ) {
                                    Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (avatarUrlInput.isNotBlank()) "Change" else "Upload",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // B. Personal Identity Fields
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Full Name *") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TealPrimary) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Gender:",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                            listOf("Male", "Female", "Other").forEach { gOpt ->
                                val selected = genderInput.equals(gOpt, ignoreCase = true)
                                Surface(
                                    onClick = { genderInput = gOpt },
                                    shape = RoundedCornerShape(999.dp),
                                    color = if (selected) TealDark else SoftSagePill
                                ) {
                                    Text(
                                        text = gOpt,
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) PureWhite else TealDark,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = { phoneInput = it },
                                label = { Text("Mobile Number") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text("Email Address") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        HorizontalDivider(color = WhisperBorder)

                        // C. Occupation & Workplace
                        Text(
                            text = "Occupation & Workplace",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(
                                listOf(
                                    "Software Engineer",
                                    "College Student",
                                    "Product Designer",
                                    "Data Analyst",
                                    "Startup Founder",
                                    "Marketing & Sales",
                                    "CA / Finance"
                                )
                            ) { roleChip ->
                                val isSel = occupationInput.equals(roleChip, ignoreCase = true)
                                Surface(
                                    onClick = { occupationInput = roleChip },
                                    shape = RoundedCornerShape(999.dp),
                                    color = if (isSel) TealPrimary else SoftSagePill
                                ) {
                                    Text(
                                        text = roleChip,
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isSel) PureWhite else TealDark,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = occupationInput,
                            onValueChange = { occupationInput = it },
                            label = { Text("Occupation / Role *") },
                            placeholder = { Text("e.g., Software Engineer / B.Tech Student") },
                            leadingIcon = { Icon(Icons.Default.Work, contentDescription = null, tint = TealPrimary) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = orgInput,
                            onValueChange = { orgInput = it },
                            label = { Text("Company or College Name") },
                            placeholder = { Text("e.g., Wipro, Infosys, Flipkart, Christ University") },
                            leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = TealPrimary) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Hybrid", "Office Daily", "Remote / WFH", "College").forEach { modeOpt ->
                                val selected = workModeInput == modeOpt
                                Surface(
                                    onClick = { workModeInput = modeOpt },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selected) TealDark else SoftCardSurface,
                                    border = BorderStroke(1.dp, if (selected) TealDark else WhisperBorder),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = modeOpt,
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) PureWhite else TextPrimary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = WhisperBorder)

                        // D. Location
                        Text(
                            text = "Preferred Locality",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(
                                listOf(
                                    "Koramangala 4th Block",
                                    "HSR Layout Sector 2",
                                    "Indiranagar 100Ft Rd",
                                    "Whitefield ITPL",
                                    "Neeladri Rd Electronic City",
                                    "BTM Layout 2nd Stage",
                                    "Nagavara Manyata Tech Park",
                                    "Marathahalli"
                                )
                            ) { locChip ->
                                val isSel = areaInput.equals(locChip, ignoreCase = true)
                                Surface(
                                    onClick = { areaInput = locChip },
                                    shape = RoundedCornerShape(999.dp),
                                    color = if (isSel) SoftGoldAccent else SoftGoldPill
                                ) {
                                    Text(
                                        text = locChip,
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isSel) PureWhite else GoldDark,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = areaInput,
                                onValueChange = { areaInput = it },
                                label = { Text("Locality / Area *") },
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = TealPrimary) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.4f)
                            )
                            OutlinedTextField(
                                value = cityInput,
                                onValueChange = { cityInput = it },
                                label = { Text("City") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        HorizontalDivider(color = WhisperBorder)

                        // E. Stay Preferences & Emergency Contact
                        Text(
                            text = "Stay Preferences",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )

                        Text(
                            text = "Food Preference:",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Pure Veg", "Veg & Non-Veg", "Eggetarian", "Jain Food").forEach { fOpt ->
                                val selected = foodPrefInput == fOpt
                                Surface(
                                    onClick = { foodPrefInput = fOpt },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selected) TealPrimary else SoftCardSurface,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = fOpt,
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) PureWhite else TextPrimary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = emergencyNameInput,
                                onValueChange = { emergencyNameInput = it },
                                label = { Text("Emergency Contact Name") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = emergencyPhoneInput,
                                onValueChange = { emergencyPhoneInput = it },
                                label = { Text("Emergency Phone") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = bioInput,
                            onValueChange = { bioInput = it },
                            label = { Text("About You") },
                            placeholder = { Text("Short note about your work schedule or hobbies") },
                            minLines = 2,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                viewModel.saveCustomizedProfile(
                                    name = nameInput,
                                    phone = phoneInput,
                                    email = emailInput,
                                    gender = genderInput,
                                    city = cityInput,
                                    currentArea = areaInput,
                                    occupation = occupationInput,
                                    organizationName = orgInput,
                                    workMode = workModeInput,
                                    foodPreference = foodPrefInput,
                                    sleepSchedule = sleepInput,
                                    languagesSpoken = languagesInput,
                                    emergencyContactName = emergencyNameInput,
                                    emergencyContactPhone = emergencyPhoneInput,
                                    bio = bioInput,
                                    avatarUrl = avatarUrlInput
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("save_custom_profile_btn")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Save Changes",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // ─── TAB 1: DIGITAL KYC SECTION ───
        if (activeSectionTab == 1) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = PureWhite,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isKycVerified) TealPrimary else WhisperBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("digital_kyc_section")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Identity Verification (KYC)",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Required for PG resident check-in & police verification",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = when (kycStatus) {
                                    KYCStatus.VERIFIED -> SoftSagePill
                                    KYCStatus.PENDING -> SoftGoldPill
                                    KYCStatus.REJECTED -> ErrorRedLight
                                    KYCStatus.NOT_STARTED -> SoftCardSurface
                                }
                            ) {
                                Text(
                                    text = when (kycStatus) {
                                        KYCStatus.VERIFIED -> "Verified ✓"
                                        KYCStatus.PENDING -> "Pending"
                                        KYCStatus.REJECTED -> "Re-upload"
                                        KYCStatus.NOT_STARTED -> "Not Verified"
                                    },
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = when (kycStatus) {
                                        KYCStatus.VERIFIED -> TealDark
                                        KYCStatus.PENDING -> GoldDark
                                        KYCStatus.REJECTED -> ErrorRed
                                        KYCStatus.NOT_STARTED -> TextSecondary
                                    },
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        if (isKycVerified) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = SoftSagePill,
                                border = BorderStroke(1.dp, TealPrimary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VerifiedUser,
                                            contentDescription = null,
                                            tint = TealPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "KYC Verified",
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = TealDark
                                            )
                                            Text(
                                                text = "${currentUser?.kycDocumentType ?: "Aadhaar Card"}: ${currentUser?.aadhaarNumber} • PAN: ${currentUser?.panNumber}",
                                                fontFamily = PoppinsFontFamily,
                                                fontSize = 11.sp,
                                                color = TextPrimary
                                            )
                                        }
                                    }

                                    TextButton(
                                        onClick = {
                                            aadhaarInput = ""
                                            panInput = ""
                                            guardianInput = ""
                                            permanentAddrInput = ""
                                            kycFrontUri = ""
                                            kycBackUri = ""
                                            kycSelfieUri = ""
                                            viewModel.resetDigitalKyc()
                                        },
                                        modifier = Modifier.testTag("reset_digital_kyc_btn")
                                    ) {
                                        Text(
                                            text = "Edit",
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TealDark
                                        )
                                    }
                                }
                            }
                        }

                        // 1. Select ID Document Type
                        Text(
                            text = "Government ID Type",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(listOf("Aadhaar Card", "Passport", "Driving License", "Voter ID")) { docOpt ->
                                val selected = kycDocType == docOpt
                                Surface(
                                    onClick = { kycDocType = docOpt },
                                    shape = RoundedCornerShape(999.dp),
                                    color = if (selected) TealDark else SoftSagePill
                                ) {
                                    Text(
                                        text = docOpt,
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (selected) PureWhite else TealDark,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        // 2. ID Number & PAN Details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = aadhaarInput,
                                onValueChange = {
                                    aadhaarInput = it
                                    kycValidationError = null
                                },
                                label = { Text("$kycDocType No. *") },
                                placeholder = { Text("4589-2310-8912") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1.1f)
                                    .testTag("aadhaar_input")
                            )

                            OutlinedTextField(
                                value = panInput,
                                onValueChange = {
                                    panInput = it.uppercase()
                                    kycValidationError = null
                                },
                                label = { Text("PAN Number *") },
                                placeholder = { Text("ABCDE1234F") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(0.9f)
                                    .testTag("pan_input")
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = guardianInput,
                                onValueChange = { guardianInput = it },
                                label = { Text("Guardian Name") },
                                placeholder = { Text("Father / Mother Name") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = permanentAddrInput,
                                onValueChange = { permanentAddrInput = it },
                                label = { Text("Permanent Address") },
                                placeholder = { Text("City, State & PIN") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.2f)
                            )
                        }

                        HorizontalDivider(color = WhisperBorder)

                        // 3. Upload Identification Documents
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Identification Documents",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "$uploadedDocsCount of 3 uploaded",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 11.sp,
                                color = TealPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        KycDocumentUploadSlotCard(
                            title = "$kycDocType (Front)",
                            subtitle = "Clear photo showing name & ID number",
                            statusLabel = currentUser?.frontDocStatusLabel ?: "Not Uploaded",
                            photoUri = kycFrontUri,
                            remoteBucketUrl = currentUser?.kycFrontRemoteUrl.orEmpty(),
                            icon = Icons.Default.Badge,
                            uploadTestTag = "upload_kyc_front_btn",
                            onPickFromGallery = {
                                kycFrontPhotoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onRemoveDoc = {
                                kycFrontUri = ""
                                viewModel.removeKycDocument("FRONT")
                            }
                        )

                        KycDocumentUploadSlotCard(
                            title = "$kycDocType (Back) / PAN Card",
                            subtitle = "Address side or PAN card photo",
                            statusLabel = currentUser?.backDocStatusLabel ?: "Not Uploaded",
                            photoUri = kycBackUri,
                            remoteBucketUrl = currentUser?.kycBackRemoteUrl.orEmpty(),
                            icon = Icons.Default.CreditCard,
                            uploadTestTag = "upload_kyc_back_btn",
                            onPickFromGallery = {
                                kycBackPhotoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onRemoveDoc = {
                                kycBackUri = ""
                                viewModel.removeKycDocument("BACK")
                            }
                        )

                        KycDocumentUploadSlotCard(
                            title = "Selfie Photo",
                            subtitle = "Recent face photo for resident ID pass",
                            statusLabel = currentUser?.selfieDocStatusLabel ?: "Not Uploaded",
                            photoUri = kycSelfieUri,
                            remoteBucketUrl = currentUser?.kycSelfieRemoteUrl.orEmpty(),
                            icon = Icons.Default.CameraFront,
                            uploadTestTag = "upload_kyc_selfie_btn",
                            onPickFromGallery = {
                                kycSelfiePhotoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onRemoveDoc = {
                                kycSelfieUri = ""
                                viewModel.removeKycDocument("SELFIE")
                            }
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { kycConsentChecked = !kycConsentChecked }
                        ) {
                            Checkbox(
                                checked = kycConsentChecked,
                                onCheckedChange = { kycConsentChecked = it },
                                colors = CheckboxDefaults.colors(checkedColor = TealPrimary)
                            )
                            Text(
                                text = "I confirm that the above identification details belong to me.",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        if (kycValidationError != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = ErrorRedLight,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = kycValidationError!!,
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ErrorRed,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (aadhaarInput.trim().length < 8) {
                                    kycValidationError = "Please enter a valid $kycDocType number."
                                    return@Button
                                }
                                if (panInput.trim().length < 6) {
                                    kycValidationError = "Please enter a valid PAN number."
                                    return@Button
                                }
                                if (!kycConsentChecked) {
                                    kycValidationError = "Please accept the verification declaration."
                                    return@Button
                                }
                                kycValidationError = null
                                viewModel.submitManualDigitalKyc(
                                    documentType = kycDocType,
                                    aadhaarOrIdNumber = aadhaarInput,
                                    panNumber = panInput,
                                    guardianName = guardianInput.ifBlank { "Verified Guardian" },
                                    permanentAddress = permanentAddrInput.ifBlank { "$areaInput, $cityInput" },
                                    frontPhotoUri = kycFrontUri,
                                    selfiePhotoUri = kycSelfieUri,
                                    backPhotoUri = kycBackUri,
                                    targetStatus = KYCStatus.VERIFIED
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TealDark),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("submit_manual_kyc_btn")
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isKycVerified) "Update KYC Details" else "Submit KYC Verification",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = PureWhite
                            )
                        }
                    }
                }
            }
        }

        // ─── TAB 2: ACCOUNT, SAVED ENQUIRIES & LEGAL ───
        if (activeSectionTab == 2) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileQuickTile(
                            title = "My Bed & Stay",
                            subtitle = "Meals & Gate Passes",
                            icon = Icons.Default.Bed,
                            badgeBg = SoftGoldPill,
                            iconTint = GoldDark,
                            onClick = { viewModel.navigateTo(Screen.MyStay) },
                            modifier = Modifier.weight(1f)
                        )

                        ProfileQuickTile(
                            title = "List Your PG",
                            subtitle = "Partner Onboarding",
                            icon = Icons.Default.AddBusiness,
                            badgeBg = SoftSagePill,
                            iconTint = TealPrimary,
                            onClick = { viewModel.navigateTo(Screen.OwnerJoinLead) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileQuickTile(
                            title = "App Feedback",
                            subtitle = "Share Suggestions",
                            icon = Icons.Default.AutoAwesome,
                            badgeBg = SoftSagePill,
                            iconTint = TealPrimary,
                            onClick = { viewModel.navigateTo(Screen.FeedbackHub) },
                            modifier = Modifier.weight(1f)
                        )

                        ProfileQuickTile(
                            title = "Digital KYC",
                            subtitle = kycStatusDisplayLabel,
                            icon = Icons.Default.VerifiedUser,
                            badgeBg = SoftGoldPill,
                            iconTint = GoldDark,
                            onClick = { activeSectionTab = 1 },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (interestLeads.isNotEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = PureWhite,
                        border = BorderStroke(1.dp, WhisperBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Shortlisted PGs",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            interestLeads.forEach { lead ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SoftSagePill.copy(alpha = 0.6f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { viewModel.navigateTo(Screen.PGDetail(lead.pgId)) }
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = lead.pgName,
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = TealDark
                                        )
                                        Text(
                                            text = "${lead.area} • ${lead.preferredSharing} • Move-in: ${lead.expectedMoveIn}",
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Support & Legal",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = PureWhite,
                    border = BorderStroke(1.dp, WhisperBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        PolicyRowItem("Terms & Privacy Policy", Icons.Default.Gavel) {
                            viewModel.navigateTo(Screen.Terms)
                        }
                        HorizontalDivider(color = BorderLight)
                        PolicyRowItem("Resident Safety & Emergency Helplines", Icons.Default.Shield) {
                            viewModel.navigateTo(Screen.SafeUse)
                        }
                        HorizontalDivider(color = BorderLight)
                        PolicyRowItem("About StayEg", Icons.Default.Info) {
                            viewModel.navigateTo(Screen.About)
                        }
                        HorizontalDivider(color = BorderLight)
                        PolicyRowItem("Help & Support", Icons.Default.Headphones) {
                            viewModel.navigateTo(Screen.Contact)
                        }
                    }
                }
            }
        }

        // Sign Out Button
        item {
            if (currentUser != null) {
                Button(
                    onClick = { viewModel.logout() },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRedLight, contentColor = ErrorRed),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sign Out",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }

    if (showKycDialog) {
        AlertDialog(
            onDismissRequest = { showKycDialog = false },
            containerColor = PureWhite,
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = "Identity Verification (KYC)",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter your Aadhaar and PAN details for PG move-in verification.",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    OutlinedTextField(
                        value = aadhaarInput,
                        onValueChange = { aadhaarInput = it },
                        label = { Text("12-Digit Aadhaar Number") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = panInput,
                        onValueChange = { panInput = it.uppercase() },
                        label = { Text("10-Digit PAN Card") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (aadhaarInput.isBlank() || panInput.isBlank()) {
                            viewModel.showToast("Please enter both Aadhaar and PAN")
                            return@Button
                        }
                        viewModel.updateKyc(aadhaarInput, panInput)
                        showKycDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save KYC", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = PureWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showKycDialog = false }) {
                    Text("Cancel", fontFamily = PoppinsFontFamily, color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun KycDocumentUploadSlotCard(
    title: String,
    subtitle: String,
    statusLabel: String,
    photoUri: String,
    remoteBucketUrl: String = "",
    icon: ImageVector,
    uploadTestTag: String,
    onPickFromGallery: () -> Unit,
    onRemoveDoc: () -> Unit
) {
    val isUploaded = photoUri.isNotBlank()
    val isVerified = statusLabel.contains("VERIFIED", ignoreCase = true)

    Surface(
        onClick = onPickFromGallery,
        shape = RoundedCornerShape(14.dp),
        color = if (isUploaded) SoftSagePill.copy(alpha = 0.4f) else SoftCardSurface,
        border = BorderStroke(
            width = 1.dp,
            color = when {
                isVerified -> TealPrimary
                isUploaded -> TealDark
                else -> WhisperBorder
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isUploaded) MintSoft else SoftSagePill),
                    contentAlignment = Alignment.Center
                ) {
                    if (isUploaded) {
                        AsyncImage(
                            model = photoUri,
                            contentDescription = title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = TealPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = if (isUploaded) "Encrypted & Verified • Ready for Express Check-In" else subtitle,
                        fontFamily = PoppinsFontFamily,
                        fontSize = 10.sp,
                        color = if (isUploaded) TealDark else TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onPickFromGallery,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isUploaded) SoftSagePill else TealDark,
                        contentColor = if (isUploaded) TealDark else PureWhite
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag(uploadTestTag)
                ) {
                    Icon(
                        imageVector = if (isUploaded) Icons.Default.CheckCircle else Icons.Default.UploadFile,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isUploaded) "Uploaded" else "Upload",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }

                if (isUploaded) {
                    IconButton(
                        onClick = onRemoveDoc,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Remove Document",
                            tint = ErrorRed,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileQuickTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badgeBg: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, WhisperBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(19.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontFamily = PoppinsFontFamily,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun PolicyRowItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(SoftSagePill),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontFamily = PoppinsFontFamily,
                fontSize = 13.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
    }
}
