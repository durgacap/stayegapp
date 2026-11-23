package com.example.stayeg.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.stayeg.data.model.ComplaintCategory
import com.example.stayeg.data.model.ComplaintPriority
import com.example.stayeg.data.model.ComplaintStatus
import com.example.stayeg.data.model.PaymentType
import com.example.stayeg.ui.components.StayEgPopupInfoDialog
import com.example.stayeg.ui.navigation.Screen
import com.example.stayeg.ui.theme.*
import com.example.stayeg.ui.viewmodel.StayEgViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyStayScreen(viewModel: StayEgViewModel) {
    val context = LocalContext.current
    val activeStay by viewModel.activeStayConfig.collectAsState()
    val mealState by viewModel.mealAttendance.collectAsState()
    val gatePasses by viewModel.gatePasses.collectAsState()
    val wardenMessages by viewModel.wardenMessages.collectAsState()
    val complaints by viewModel.complaints.collectAsState()
    val payments by viewModel.payments.collectAsState()
    val allPgs by viewModel.allPgs.collectAsState()

    val hasActiveStay = activeStay.isStayActivated
    var isPreviewSampleStay by remember { mutableStateOf(false) }
    var showQrScannerDialog by remember { mutableStateOf(false) }

    // 0 = Meals & Wi-Fi, 1 = Tickets & Warden, 2 = Gate Passes & Notice, 3 = Rent & Donut
    var activeManageTab by remember { mutableIntStateOf(0) }

    var showConfigureStayModal by remember { mutableStateOf(false) }
    var showPaymentGatewayPopup by remember { mutableStateOf(false) }

    // Camera permission for QR code check-in scanner
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        showQrScannerDialog = true
    }

    val launchQrCheckInScanner = {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            showQrScannerDialog = true
        } else {
            runCatching {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }.onFailure {
                showQrScannerDialog = true
            }
        }
    }

    // Meal Preference local edit state
    var selectedMealMode by remember(mealState.mealMode) { mutableStateOf(mealState.mealMode) }
    var dietaryNoteInput by remember(mealState.specialDietaryNote) { mutableStateOf(mealState.specialDietaryNote) }

    // New Ticket state
    var ticketCategoryChip by remember { mutableStateOf("Room Cleaning") }
    var ticketTitleInput by remember { mutableStateOf("") }
    var ticketUrgent by remember { mutableStateOf(false) }

    // Warden Chat state
    var wardenChatInput by remember { mutableStateOf("") }

    // Gate Pass Creator state
    var passTypeChip by remember { mutableStateOf("LATE_ENTRY") }
    var passVisitorOrReason by remember { mutableStateOf("") }
    var passDateInput by remember { mutableStateOf("Tonight • 19 Nov") }
    var passTimeInput by remember { mutableStateOf("11:30 PM") }

    // Move-out target date state
    var moveOutDateInput by remember { mutableStateOf("31 Dec 2026") }

    val monthlyRent = activeStay.monthlyRent
    val baseBedRent = (monthlyRent * 0.62).toInt()
    val mealsShare = (monthlyRent * 0.24).toInt()
    val wifiPowerLaundry = monthlyRent - baseBedRent - mealsShare

    // ─── FRESH ACCOUNT / NO ACTIVE PG STAY YET: QR CHECK-IN + VISUAL HOW-IT-WORKS GUIDE ───
    if (!hasActiveStay && !isPreviewSampleStay) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftGalleryBg),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Clean Top Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "My Stay",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = TextPrimary,
                            letterSpacing = (-0.4).sp
                        )
                        Text(
                            text = "No active PG stay linked to your account yet",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Surface(
                        onClick = { launchQrCheckInScanner() },
                        shape = RoundedCornerShape(999.dp),
                        color = TealDark,
                        modifier = Modifier.testTag("open_qr_scanner_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Scan PG QR",
                                tint = MintSoft,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Scan PG QR",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = PureWhite
                            )
                        }
                    }
                }
            }

            // 2. Hero Card: Staying at a Verified PG? Scan QR Code or Book a PG
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = TealDark,
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
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
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = MintSoft
                                ) {
                                    Text(
                                        text = "VERIFIED PG RESIDENT CHECK-IN",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        color = TealDeep,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                Surface(
                                    onClick = { showConfigureStayModal = true },
                                    shape = RoundedCornerShape(999.dp),
                                    color = PureWhite.copy(alpha = 0.15f),
                                    modifier = Modifier.testTag("configure_my_stay_btn")
                                ) {
                                    Text(
                                        text = "Enter Room Manually",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp,
                                        color = PureWhite,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Text(
                                text = "Activate Your PG Stay in 10 Seconds",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 19.sp,
                                color = PureWhite
                            )
                            Text(
                                text = "When you book a verified PG on StayEg or scan the reception QR code at your verified PG, your real room, daily mess meals, Wi-Fi pass, tickets & rent ledger appear here automatically.",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 11.sp,
                                color = MintSoft.copy(alpha = 0.92f),
                                lineHeight = 16.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { launchQrCheckInScanner() },
                                    colors = ButtonDefaults.buttonColors(containerColor = SoftGoldAccent),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = null,
                                        tint = PitchBlack,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Scan PG QR Code",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = PitchBlack
                                    )
                                }

                                OutlinedButton(
                                    onClick = { viewModel.navigateTo(Screen.Explore) },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, MintSoft.copy(alpha = 0.7f)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Explore,
                                        contentDescription = null,
                                        tint = PureWhite,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "Explore PGs",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = PureWhite
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Visual Guide: How "My Stay" Works When You Check Into a PG
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = PureWhite,
                    border = BorderStroke(1.dp, WhisperBorder),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
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
                            Column {
                                Text(
                                    text = "How My Stay Works",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "A quick guide to what unlocks once you move into your PG",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = SoftSagePill
                            ) {
                                Text(
                                    text = "Resident Guide",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = TealPrimary,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                )
                            }
                        }

                        MyStayGuideStepRow(
                            stepNumber = "1",
                            icon = Icons.Default.QrCodeScanner,
                            title = "Book a Bed or Scan Verified PG QR",
                            description = "Book any verified PG room in the app or scan the QR standee at your PG reception desk to link your Room & Bed."
                        )
                        MyStayGuideStepRow(
                            stepNumber = "2",
                            icon = Icons.Default.Restaurant,
                            title = "Daily Mess Meals & Office Tiffin Box",
                            description = "Opt in or skip Breakfast, Lunch & Dinner, request an office lunch tiffin box, and add kitchen dietary notes."
                        )
                        MyStayGuideStepRow(
                            stepNumber = "3",
                            icon = Icons.Default.Wifi,
                            title = "Floor 5G Wi-Fi & Digital Gate Passes",
                            description = "Copy your floor Wi-Fi password in 1 tap and generate instant late-entry, guest visit, or vacation passes."
                        )
                        MyStayGuideStepRow(
                            stepNumber = "4",
                            icon = Icons.Default.SupportAgent,
                            title = "Room Tickets, Warden Chat & UPI Rent",
                            description = "Raise housekeeping or maintenance tickets, chat with your warden in-app, and get digital UPI rent receipts."
                        )
                    }
                }
            }

            // 4. Preview Sample My Stay Dashboard (Just for Understanding)
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = SoftGoldPill,
                    border = BorderStroke(1.dp, SoftGoldAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(SoftGoldAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Want to See How My Stay Looks?",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TealDark
                                )
                                Text(
                                    text = "Open an interactive sample dashboard to understand how meals, tickets, gate passes & rent tracking work.",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 11.sp,
                                    color = GoldDark
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { isPreviewSampleStay = true },
                                colors = ButtonDefaults.buttonColors(containerColor = TealDark),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("preview_sample_my_stay_btn")
                            ) {
                                Text(
                                    text = "Preview Sample My Stay →",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = PureWhite
                                )
                            }

                            OutlinedButton(
                                onClick = { viewModel.navigateTo(Screen.Complaints) },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, TealDark),
                                modifier = Modifier.testTag("open_complaints_screen_btn")
                            ) {
                                Text(
                                    text = "Helpdesk",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = TealDark
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftGalleryBg),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Optional Sample Preview Banner when user is previewing before booking/scanning
        if (isPreviewSampleStay && !hasActiveStay) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SoftGoldPill,
                    border = BorderStroke(1.dp, SoftGoldAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = GoldDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SAMPLE GUIDE PREVIEW • Scan PG QR or book a PG to activate your real stay",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = GoldDark
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                onClick = { launchQrCheckInScanner() },
                                shape = RoundedCornerShape(999.dp),
                                color = TealDark
                            ) {
                                Text(
                                    text = "Scan QR",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = PureWhite,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                onClick = { isPreviewSampleStay = false },
                                shape = RoundedCornerShape(999.dp),
                                color = PureWhite
                            ) {
                                Text(
                                    text = "Exit",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = TealDark,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 1. Header with QR Scan & Quick Stay Config Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "My Stay Manager",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = TextPrimary,
                        letterSpacing = (-0.4).sp
                    )
                    Text(
                        text = if (activeStay.qrCheckInCode.isNotBlank())
                            "QR Checked-In (${activeStay.qrCheckInCode}) • Manage meals, tickets & rent"
                        else
                            "Manage room, daily mess meals, tickets, gate passes & rent",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        onClick = { launchQrCheckInScanner() },
                        shape = RoundedCornerShape(999.dp),
                        color = SoftSagePill,
                        border = BorderStroke(1.dp, TealPrimary.copy(alpha = 0.35f)),
                        modifier = Modifier.testTag("open_qr_scanner_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Scan PG QR",
                                tint = TealDark,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "QR",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = TealDark
                            )
                        }
                    }

                    Surface(
                        onClick = { showConfigureStayModal = true },
                        shape = RoundedCornerShape(999.dp),
                        color = SoftGoldPill,
                        border = BorderStroke(1.dp, SoftGoldAccent),
                        modifier = Modifier.testTag("configure_my_stay_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EditCalendar,
                                contentDescription = "Configure Stay",
                                tint = GoldDark,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Edit Room",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = GoldDark
                            )
                        }
                    }
                }
            }
        }

        // 2. Active Stay Hero Residence Card (Interactive Room, Bed, Floor & Quick Actions)
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = TealDark,
                modifier = Modifier.fillMaxWidth()
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
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = MintSoft
                            ) {
                                Text(
                                    text = "ACTIVE TENANT STAY • ₹0 BROKERAGE",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = TealDeep,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            if (activeStay.moveOutNoticeActive) {
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = SoftGoldPill
                                ) {
                                    Text(
                                        text = "NOTICE: ${activeStay.moveOutTargetDate}",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        color = GoldDark,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = activeStay.pgName,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 19.sp,
                            color = PureWhite
                        )
                        Text(
                            text = "📍 ${activeStay.area}, Bengaluru • Move-in: ${activeStay.moveInDate}",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 11.sp,
                            color = MintSoft.copy(alpha = 0.9f)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 4-Metric Live Room & Stay Bar
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = TealDeep.copy(alpha = 0.58f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StayMetricItem("Room ${activeStay.roomCode}", "Floor ${activeStay.floorNumber}")
                                StayDivider()
                                StayMetricItem("Bed #${activeStay.bedNumber}", activeStay.sharingType)
                                StayDivider()
                                StayMetricItem("₹$monthlyRent", "Monthly Rent")
                                StayDivider()
                                StayMetricItem("${mealState.mealsAttendedThisMonth} Meals", "${mealState.mealsSkippedSavedCount} Skipped")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                onClick = { viewModel.navigateTo(Screen.PGDetail(activeStay.pgId)) },
                                shape = RoundedCornerShape(12.dp),
                                color = PureWhite.copy(alpha = 0.14f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(vertical = 9.dp, horizontal = 10.dp)
                                ) {
                                    Icon(Icons.Default.Bed, contentDescription = null, tint = MintSoft, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "View Room Deck",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = PureWhite
                                    )
                                }
                            }

                            Surface(
                                onClick = { showConfigureStayModal = true },
                                shape = RoundedCornerShape(12.dp),
                                color = SoftGoldPill,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(vertical = 9.dp, horizontal = 10.dp)
                                ) {
                                    Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = GoldDark, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Switch Room / Bed",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = GoldDark
                                    )
                                }
                            }

                            Surface(
                                onClick = { viewModel.navigateTo(Screen.Complaints) },
                                shape = RoundedCornerShape(12.dp),
                                color = PureWhite.copy(alpha = 0.14f),
                                modifier = Modifier.testTag("open_complaints_screen_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(vertical = 9.dp, horizontal = 12.dp)
                                ) {
                                    Icon(Icons.Default.Build, contentDescription = "Maintenance Tickets", tint = MintSoft, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "Tickets",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = PureWhite
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. 4-Tab Interactive Stay Management Switcher
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
                    0 to "Meals & Wi-Fi",
                    1 to "Tickets & Chat",
                    2 to "Gate Passes",
                    3 to "Rent & Budget"
                ).forEach { (idx, label) ->
                    val isSelected = activeManageTab == idx
                    Surface(
                        onClick = { activeManageTab = idx },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) TealDark else Color.Transparent,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = label,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 10.sp,
                            color = if (isSelected) PureWhite else TealDark,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 9.dp, horizontal = 2.dp)
                        )
                    }
                }
            }
        }

        // ─── TAB 0: DAILY MESS & MEAL ATTENDANCE MANAGER + 5G WI-FI PASS ───
        if (activeManageTab == 0) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = PureWhite,
                    border = BorderStroke(1.dp, WhisperBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Today's Mess Attendance",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Toggle meals you are attending or skipping today",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = SoftSagePill
                            ) {
                                Text(
                                    text = "${mealState.mealsAttendedThisMonth} Attended",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = TealPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        MealToggleRow(
                            title = "Breakfast (7:30 AM – 9:30 AM)",
                            menuDesc = "Masala Dosa, Coconut Chutney, Sambar & Filter Coffee",
                            isOptedIn = mealState.breakfastOptIn,
                            onToggle = { viewModel.toggleMealOptIn("TODAY_BREAKFAST") }
                        )
                        MealToggleRow(
                            title = "Lunch (12:30 PM – 2:30 PM)",
                            menuDesc = "Phulka, Chole Masala, Steamed Rice, Dal & Boondi Raita",
                            isOptedIn = mealState.lunchOptIn,
                            onToggle = { viewModel.toggleMealOptIn("TODAY_LUNCH") }
                        )
                        MealToggleRow(
                            title = "Evening Snacks (5:00 PM – 6:15 PM)",
                            menuDesc = "Sweet Corn Chaat & Ginger Masala Chai",
                            isOptedIn = mealState.snacksOptIn,
                            onToggle = { viewModel.toggleMealOptIn("TODAY_SNACKS") }
                        )
                        MealToggleRow(
                            title = "Dinner (8:00 PM – 10:00 PM)",
                            menuDesc = "Hyderabadi Dum Biryani / Paneer Tikka Masala & Salad",
                            isOptedIn = mealState.dinnerOptIn,
                            onToggle = { viewModel.toggleMealOptIn("TODAY_DINNER") }
                        )

                        HorizontalDivider(color = WhisperBorder)

                        Text(
                            text = "Tomorrow's Advance Meal Opt-In",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TomorrowMealChip(
                                label = "Breakfast",
                                active = mealState.tomorrowBreakfastOptIn,
                                onClick = { viewModel.toggleMealOptIn("TOMORROW_BREAKFAST") },
                                modifier = Modifier.weight(1f)
                            )
                            TomorrowMealChip(
                                label = "Lunch",
                                active = mealState.tomorrowLunchOptIn,
                                onClick = { viewModel.toggleMealOptIn("TOMORROW_LUNCH") },
                                modifier = Modifier.weight(1f)
                            )
                            TomorrowMealChip(
                                label = "Dinner",
                                active = mealState.tomorrowDinnerOptIn,
                                onClick = { viewModel.toggleMealOptIn("TOMORROW_DINNER") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        HorizontalDivider(color = WhisperBorder)

                        // Meal Mode & Tiffin Box Request
                        Text(
                            text = "Mess Dining Mode & Office Tiffin Box",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(
                                listOf(
                                    "Regular Dining Hall",
                                    "Pack Office Lunch Box",
                                    "Light Diet / Khichdi"
                                )
                            ) { modeOpt ->
                                val selected = selectedMealMode == modeOpt
                                Surface(
                                    onClick = { selectedMealMode = modeOpt },
                                    shape = RoundedCornerShape(999.dp),
                                    color = if (selected) TealDark else SoftSagePill
                                ) {
                                    Text(
                                        text = modeOpt,
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (selected) PureWhite else TealDark,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = dietaryNoteInput,
                                onValueChange = { dietaryNoteInput = it },
                                label = { Text("Special Kitchen Note") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    viewModel.updateMealPreferences(selectedMealMode, dietaryNoteInput)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(52.dp)
                            ) {
                                Text("Save", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 5G Wi-Fi & Biometric Access Pass Card
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = SoftGoldPill,
                    border = BorderStroke(1.dp, SoftGoldAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(SoftGoldAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Wifi, contentDescription = null, tint = PureWhite)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Floor ${activeStay.floorNumber} Wi-Fi: StayEg_5G_Mesh",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TealDark
                                )
                                Text(
                                    text = "Pass: StayEg@2026 • 200 Mbps Fiber Active",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 11.sp,
                                    color = GoldDark
                                )
                            }
                        }

                        Surface(
                            onClick = { viewModel.showToast("Copied Wi-Fi Password: StayEg@2026") },
                            shape = RoundedCornerShape(999.dp),
                            color = TealDark
                        ) {
                            Text(
                                text = "Copy Pass",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = PureWhite,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }
        }

        // ─── TAB 1: ROOM HOUSEKEEPING, MAINTENANCE TICKETS & IN-APP WARDEN CHAT ───
        if (activeManageTab == 1) {
            // Raise New Room Ticket Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = PureWhite,
                    border = BorderStroke(1.dp, WhisperBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Raise Room Housekeeping / Service Ticket",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Assigned to Room ${activeStay.roomCode} • Bed #${activeStay.bedNumber}",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(
                                listOf(
                                    "Room Cleaning",
                                    "Wi-Fi / Router",
                                    "AC / Fan",
                                    "Plumbing / Geyser",
                                    "Mess Food",
                                    "Laundry"
                                )
                            ) { catChip ->
                                val selected = ticketCategoryChip == catChip
                                Surface(
                                    onClick = { ticketCategoryChip = catChip },
                                    shape = RoundedCornerShape(999.dp),
                                    color = if (selected) TealDark else SoftSagePill
                                ) {
                                    Text(
                                        text = catChip,
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (selected) PureWhite else TealDark,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = ticketTitleInput,
                            onValueChange = { ticketTitleInput = it },
                            label = { Text("Describe Issue or Request *") },
                            placeholder = { Text("e.g., Please clean Room ${activeStay.roomCode} & check bathroom geyser") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { ticketUrgent = !ticketUrgent }
                            ) {
                                Checkbox(
                                    checked = ticketUrgent,
                                    onCheckedChange = { ticketUrgent = it },
                                    colors = CheckboxDefaults.colors(checkedColor = ErrorRed)
                                )
                                Text(
                                    text = "Mark High Priority / Urgent",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 12.sp,
                                    color = if (ticketUrgent) ErrorRed else TextSecondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Button(
                                onClick = {
                                    val mappedCat = when {
                                        ticketCategoryChip.contains("Clean", ignoreCase = true) -> ComplaintCategory.CLEANLINESS
                                        ticketCategoryChip.contains("Food", ignoreCase = true) -> ComplaintCategory.GENERAL
                                        else -> ComplaintCategory.MAINTENANCE
                                    }
                                    val cleanTitle = ticketTitleInput.trim().ifEmpty {
                                        "$ticketCategoryChip request for Room ${activeStay.roomCode} • Bed #${activeStay.bedNumber}"
                                    }
                                    viewModel.raiseComplaint(
                                        pgId = activeStay.pgId,
                                        title = "[$ticketCategoryChip] $cleanTitle",
                                        description = "Room ${activeStay.roomCode}, Floor ${activeStay.floorNumber}, Bed #${activeStay.bedNumber} at ${activeStay.pgName}",
                                        category = mappedCat,
                                        priority = if (ticketUrgent) ComplaintPriority.URGENT else ComplaintPriority.MEDIUM
                                    )
                                    ticketTitleInput = ""
                                    ticketUrgent = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.AddTask, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Create Ticket", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Active Tickets List with 1-Tap Resolve
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = PureWhite,
                    border = BorderStroke(1.dp, WhisperBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Your Room Tickets (${complaints.size})",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Full Helpdesk →",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = TealPrimary,
                                modifier = Modifier.clickable {
                                    viewModel.navigateTo(Screen.Complaints)
                                }
                            )
                        }

                        complaints.take(6).forEach { ticket ->
                            val isResolved = ticket.status == ComplaintStatus.RESOLVED || ticket.status == ComplaintStatus.CLOSED
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isResolved) SoftSagePill.copy(alpha = 0.5f) else SoftCardSurface,
                                border = BorderStroke(1.dp, if (isResolved) TealPrimary.copy(alpha = 0.3f) else WhisperBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(999.dp),
                                                color = if (isResolved) TealPrimary else SoftGoldAccent
                                            ) {
                                                Text(
                                                    text = ticket.status.name,
                                                    fontFamily = PoppinsFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 8.sp,
                                                    color = PureWhite,
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                                )
                                            }
                                            Text(
                                                text = ticket.createdAt,
                                                fontFamily = PoppinsFontFamily,
                                                fontSize = 10.sp,
                                                color = TextSecondary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = ticket.title,
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = ticket.description,
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }

                                    if (!isResolved) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            onClick = {
                                                viewModel.resolveComplaint(ticket.id, "Confirmed resolved by tenant")
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            color = TealDark
                                        ) {
                                            Text(
                                                text = "Resolve ✓",
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = PureWhite,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Live In-App Warden Chat Thread
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = PureWhite,
                    border = BorderStroke(1.dp, WhisperBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Direct In-App Warden Desk",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Mohan Rao • On-Site Manager (Zero phone numbers shared)",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = SoftSagePill
                            ) {
                                Text(
                                    text = "ONLINE",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = TealPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        wardenMessages.takeLast(4).forEach { msg ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (msg.isFromTenant) TealDark else SoftCardSurface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = msg.senderName,
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = if (msg.isFromTenant) MintSoft else TealPrimary
                                        )
                                        Text(
                                            text = msg.timestamp,
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 9.sp,
                                            color = if (msg.isFromTenant) PureWhite.copy(alpha = 0.7f) else TextSecondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = msg.message,
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 12.sp,
                                        color = if (msg.isFromTenant) PureWhite else TextPrimary
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = wardenChatInput,
                                onValueChange = { wardenChatInput = it },
                                label = { Text("Message Warden In-App") },
                                placeholder = { Text("e.g., Please send drinking water can to Room ${activeStay.roomCode}") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    if (wardenChatInput.isNotBlank()) {
                                        viewModel.sendWardenChatMessage(wardenChatInput)
                                        wardenChatInput = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TealDark),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(52.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // ─── TAB 2: GATE PASSES, GUEST VISITS & 30-DAY MOVE-OUT NOTICE MANAGER ───
        if (activeManageTab == 2) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = PureWhite,
                    border = BorderStroke(1.dp, WhisperBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Create Digital Gate / Guest Pass",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Generate instant biometric security approval for late entry, guest visits or outstation leave",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "LATE_ENTRY" to "Late Entry",
                                "GUEST_VISIT" to "Guest Visit",
                                "OUTSTATION_LEAVE" to "Leave Pass"
                            ).forEach { (code, label) ->
                                val selected = passTypeChip == code
                                Surface(
                                    onClick = { passTypeChip = code },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selected) TealDark else SoftSagePill,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = label,
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (selected) PureWhite else TealDark,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = passVisitorOrReason,
                            onValueChange = { passVisitorOrReason = it },
                            label = {
                                Text(
                                    when (passTypeChip) {
                                        "GUEST_VISIT" -> "Visitor / Parent Name & Relation *"
                                        "OUTSTATION_LEAVE" -> "Hometown / Destination & Meal Pause *"
                                        else -> "Late Return Reason (e.g., Office Release / Flight) *"
                                    }
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = passDateInput,
                                onValueChange = { passDateInput = it },
                                label = { Text("Date / Duration") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = passTimeInput,
                                onValueChange = { passTimeInput = it },
                                label = { Text("Entry / Visit Time") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Button(
                            onClick = {
                                val title = when (passTypeChip) {
                                    "GUEST_VISIT" -> "Day Guest Visit Pass (Room ${activeStay.roomCode})"
                                    "OUTSTATION_LEAVE" -> "Outstation Vacation & Mess Pause Pass"
                                    else -> "Late Night Biometric Entry Pass"
                                }
                                viewModel.createGatePass(
                                    passType = passTypeChip,
                                    title = title,
                                    visitorOrReason = passVisitorOrReason.ifBlank { "Approved for Room ${activeStay.roomCode}" },
                                    dateRange = passDateInput,
                                    timeSlot = passTimeInput
                                )
                                passVisitorOrReason = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Generate Security Gate Pass",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Active Gate Passes List
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = PureWhite,
                    border = BorderStroke(1.dp, WhisperBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Active Security Passes (${gatePasses.size})",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )

                        gatePasses.forEach { pass ->
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
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(999.dp),
                                                color = SoftSagePill
                                            ) {
                                                Text(
                                                    text = pass.status,
                                                    fontFamily = PoppinsFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 9.sp,
                                                    color = TealPrimary,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${pass.dateRange} • ${pass.timeSlot}",
                                                fontFamily = PoppinsFontFamily,
                                                fontSize = 10.sp,
                                                color = TextSecondary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = pass.title,
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = pass.visitorOrReason,
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }

                                    TextButton(onClick = { viewModel.cancelGatePass(pass.id) }) {
                                        Text(
                                            text = "Cancel",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = ErrorRed
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 30-Day Move-Out Notice Manager
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = PureWhite,
                    border = BorderStroke(1.dp, WhisperBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "30-Day Move-Out / Room Transfer Notice",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (activeStay.moveOutNoticeActive)
                                        "Move-out notice active for ${activeStay.moveOutTargetDate}"
                                    else
                                        "No move-out notice active. Lock-in: ${activeStay.lockInMonths} months.",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 11.sp,
                                    color = if (activeStay.moveOutNoticeActive) GoldDark else TextSecondary
                                )
                            }
                            Switch(
                                checked = activeStay.moveOutNoticeActive,
                                onCheckedChange = { enabled ->
                                    viewModel.toggleMoveOutNotice(enabled, moveOutDateInput)
                                },
                                colors = SwitchDefaults.colors(checkedTrackColor = TealPrimary)
                            )
                        }

                        OutlinedTextField(
                            value = moveOutDateInput,
                            onValueChange = { moveOutDateInput = it },
                            label = { Text("Planned Move-Out / Transfer Date") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // ─── TAB 3: RENT BREAKDOWN DONUT CHART & PAYMENT LEDGER ───
        if (activeManageTab == 3) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = PureWhite,
                    border = BorderStroke(1.dp, WhisperBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Monthly Stay Breakdown",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = SoftSagePill
                            ) {
                                Text(
                                    text = "₹0 Booking Fee",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = TealPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier.size(164.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(144.dp)) {
                                val strokeWidth = 22.dp.toPx()
                                drawArc(
                                    color = TealPrimary,
                                    startAngle = -90f,
                                    sweepAngle = 218f,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                                )
                                drawArc(
                                    color = SoftGoldAccent,
                                    startAngle = 132f,
                                    sweepAngle = 82f,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                                )
                                drawArc(
                                    color = TealDark,
                                    startAngle = 218f,
                                    sweepAngle = 48f,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "₹ $monthlyRent",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 19.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "All-Inclusive",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        DonutLegendRow(
                            dotColor = TealPrimary,
                            label = "Room & Bed (Room ${activeStay.roomCode} • Bed #${activeStay.bedNumber})",
                            amount = "₹ $baseBedRent",
                            percent = "62%"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        DonutLegendRow(
                            dotColor = SoftGoldAccent,
                            label = "Daily 3-Time Homestyle Mess",
                            amount = "₹ $mealsShare",
                            percent = "24%"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        DonutLegendRow(
                            dotColor = TealDark,
                            label = "5G Wi-Fi, Power & Housekeeping",
                            amount = "₹ $wifiPowerLaundry",
                            percent = "14%"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        DonutLegendRow(
                            dotColor = MintSoft,
                            label = "StayEg Tenant Brokerage",
                            amount = "₹ 0",
                            percent = "0%"
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { showPaymentGatewayPopup = true },
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("mystay_pay_upi_tab_btn")
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pay Monthly Rent (₹$monthlyRent) via UPI",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Recent Digital Rent Receipts & Payment Ledger
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = PureWhite,
                    border = BorderStroke(1.dp, WhisperBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Digital Rent Receipts (${payments.size})",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        payments.take(5).forEach { pay ->
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
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "₹${pay.amount} • ${pay.type.name}",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "${pay.method} • ${pay.paidDate.ifBlank { pay.dueDate }}",
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = SoftSagePill
                                    ) {
                                        Text(
                                            text = "PAID ✓",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = TealPrimary,
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Rent & Security Deposit UPI Payment Row
        item {
            Surface(
                onClick = { showPaymentGatewayPopup = true },
                shape = RoundedCornerShape(16.dp),
                color = PureWhite,
                border = BorderStroke(1.dp, WhisperBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mystay_pay_rent_alert_btn")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp, horizontal = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Pay Monthly Rent via UPI",
                            tint = TealPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Pay Rent via UPI (₹$monthlyRent)",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Google Pay, PhonePe, Paytm • Instant Digital Receipt",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
    } // end else (hasActiveStay || isPreviewSampleStay)

    // Modal 0: Verified PG QR Code Check-In Scanner Dialog
    if (showQrScannerDialog) {
        val verifiedPgs = remember(allPgs) {
            allPgs.filter { it.isVerified && !it.isDirectoryListing }.ifEmpty { allPgs.take(6) }
        }
        var selectedQrPg by remember { mutableStateOf(verifiedPgs.firstOrNull()) }
        var qrCodeInput by remember(selectedQrPg) {
            mutableStateOf(
                selectedQrPg?.let { "STAYEG-QR-${it.id.uppercase()}-201" } ?: "STAYEG-QR-ZOLO-CROWN-201"
            )
        }
        var roomInput by remember { mutableStateOf("201") }
        var floorInput by remember { mutableStateOf("2") }
        var bedInput by remember { mutableStateOf("1") }
        var sharingInput by remember { mutableStateOf("Double Sharing") }

        AlertDialog(
            onDismissRequest = { showQrScannerDialog = false },
            containerColor = PureWhite,
            shape = RoundedCornerShape(22.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = "Scan Verified PG QR Code",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Point at PG reception QR standee or select below",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Simulated Camera QR Viewfinder Frame
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = TealDeep,
                        border = BorderStroke(1.5.dp, TealPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(126.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCode2,
                                    contentDescription = "QR Scanner Frame",
                                    tint = MintSoft,
                                    modifier = Modifier.size(44.dp)
                                )
                                Text(
                                    text = "Camera QR Scanner Ready • Auto-Detecting Standee",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = PureWhite
                                )
                                Text(
                                    text = qrCodeInput,
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp,
                                    color = SoftGoldAccent
                                )
                            }
                        }
                    }

                    Text(
                        text = "Quick-Scan Verified PG Reception QR:",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = TextPrimary
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(verifiedPgs.take(8)) { vPg ->
                            val isSelected = selectedQrPg?.id == vPg.id
                            Surface(
                                onClick = {
                                    selectedQrPg = vPg
                                    qrCodeInput = "STAYEG-QR-${vPg.id.uppercase()}-$roomInput"
                                },
                                shape = RoundedCornerShape(999.dp),
                                color = if (isSelected) TealDark else SoftSagePill,
                                border = BorderStroke(1.dp, if (isSelected) TealDark else WhisperBorder)
                            ) {
                                Text(
                                    text = vPg.name.take(22),
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = if (isSelected) PureWhite else TealDark,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = qrCodeInput,
                        onValueChange = { qrCodeInput = it },
                        label = { Text("Verified PG QR / Check-In Code") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("qr_code_checkin_input")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = roomInput,
                            onValueChange = {
                                roomInput = it
                                selectedQrPg?.let { vPg ->
                                    qrCodeInput = "STAYEG-QR-${vPg.id.uppercase()}-${it.ifBlank { "201" }}"
                                }
                            },
                            label = { Text("Room #") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = floorInput,
                            onValueChange = { floorInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Floor") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = bedInput,
                            onValueChange = { bedInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Bed #") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Single Sharing", "Double Sharing", "Triple Sharing").forEach { shOpt ->
                            val sel = sharingInput == shOpt
                            Surface(
                                onClick = { sharingInput = shOpt },
                                shape = RoundedCornerShape(8.dp),
                                color = if (sel) TealPrimary else SoftCardSurface,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = shOpt.substringBefore(" "),
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = if (sel) PureWhite else TextPrimary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.activateStayViaQrCode(
                            qrCode = qrCodeInput,
                            pgId = selectedQrPg?.id ?: "",
                            roomCode = roomInput.ifBlank { "201" },
                            floorNumber = floorInput.toIntOrNull() ?: 2,
                            bedNumber = bedInput.toIntOrNull() ?: 1,
                            sharingType = sharingInput
                        )
                        isPreviewSampleStay = false
                        showQrScannerDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealDark),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("confirm_qr_checkin_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = MintSoft,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Activate My Stay",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showQrScannerDialog = false }) {
                    Text("Cancel", fontFamily = PoppinsFontFamily, color = TextSecondary)
                }
            }
        )
    }

    // Modal 1: Configure / Switch Active PG Room, Floor, Bed & Rent
    if (showConfigureStayModal) {
        var selectedPgId by remember { mutableStateOf(activeStay.pgId) }
        var selectedPgName by remember { mutableStateOf(activeStay.pgName) }
        var selectedArea by remember { mutableStateOf(activeStay.area) }
        var roomInput by remember { mutableStateOf(activeStay.roomCode) }
        var floorInput by remember { mutableStateOf(activeStay.floorNumber.toString()) }
        var bedInput by remember { mutableStateOf(activeStay.bedNumber.toString()) }
        var sharingInput by remember { mutableStateOf(activeStay.sharingType) }
        var rentInput by remember { mutableStateOf(activeStay.monthlyRent.toString()) }
        var moveInInput by remember { mutableStateOf(activeStay.moveInDate) }

        AlertDialog(
            onDismissRequest = { showConfigureStayModal = false },
            containerColor = PureWhite,
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = "Manage Active PG & Room",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Pick a verified PG or enter your current room, bed & monthly rent:",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(allPgs.take(8)) { candidatePg ->
                            val isChosen = selectedPgId == candidatePg.id
                            Surface(
                                onClick = {
                                    selectedPgId = candidatePg.id
                                    selectedPgName = candidatePg.name
                                    selectedArea = candidatePg.area
                                    rentInput = candidatePg.price.toString()
                                },
                                shape = RoundedCornerShape(999.dp),
                                color = if (isChosen) TealDark else SoftSagePill
                            ) {
                                Text(
                                    text = candidatePg.name.take(20),
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = if (isChosen) PureWhite else TealDark,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = selectedPgName,
                        onValueChange = { selectedPgName = it },
                        label = { Text("PG Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = roomInput,
                            onValueChange = { roomInput = it },
                            label = { Text("Room #") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = floorInput,
                            onValueChange = { floorInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Floor") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = bedInput,
                            onValueChange = { bedInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Bed #") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Single Sharing", "Double Sharing", "Triple Sharing").forEach { shOpt ->
                            val sel = sharingInput == shOpt
                            Surface(
                                onClick = { sharingInput = shOpt },
                                shape = RoundedCornerShape(8.dp),
                                color = if (sel) TealPrimary else SoftCardSurface,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = shOpt.substringBefore(" "),
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = if (sel) PureWhite else TextPrimary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = rentInput,
                            onValueChange = { rentInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Monthly Rent (₹)") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = moveInInput,
                            onValueChange = { moveInInput = it },
                            label = { Text("Move-In Date") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateActiveStayConfig(
                            pgId = selectedPgId,
                            pgName = selectedPgName.ifBlank { "Sunrise PG - Koramangala" },
                            area = selectedArea.ifBlank { "Koramangala" },
                            roomCode = roomInput.ifBlank { "101" },
                            floorNumber = floorInput.toIntOrNull() ?: 1,
                            bedNumber = bedInput.toIntOrNull() ?: 1,
                            sharingType = sharingInput,
                            monthlyRent = rentInput.toIntOrNull() ?: 12000,
                            moveInDate = moveInInput.ifBlank { "01 Nov 2026" }
                        )
                        showConfigureStayModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save My Stay", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = PureWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfigureStayModal = false }) {
                    Text("Cancel", fontFamily = PoppinsFontFamily, color = TextSecondary)
                }
            }
        )
    }

    if (showPaymentGatewayPopup) {
        var selectedUpiApp by remember { mutableStateOf("Google Pay") }
        var upiVpaInput by remember { mutableStateOf("stayeg.escrow@axis") }
        var amountInput by remember(monthlyRent) { mutableStateOf(monthlyRent.toString()) }

        AlertDialog(
            onDismissRequest = { showPaymentGatewayPopup = false },
            containerColor = PureWhite,
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = "Pay Monthly Rent via UPI",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "${activeStay.pgName} • Room ${activeStay.roomCode} • Bed #${activeStay.bedNumber}",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = TealDark
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Google Pay", "PhonePe", "Paytm").forEach { appName ->
                            val isSelected = selectedUpiApp == appName
                            Surface(
                                onClick = { selectedUpiApp = appName },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) TealDark else SoftSagePill,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = appName,
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isSelected) PureWhite else TealDark,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Rent Amount (₹)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = upiVpaInput,
                        onValueChange = { upiVpaInput = it },
                        label = { Text("Property / Escrow UPI ID") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val payAmount = amountInput.toIntOrNull() ?: monthlyRent
                        val upiDeepLink = viewModel.buildUpiDeepLinkUri(
                            amount = payAmount,
                            upiPayeeVpa = upiVpaInput
                        )
                        runCatching {
                            val upiIntent = Intent(Intent.ACTION_VIEW, Uri.parse(upiDeepLink))
                            context.startActivity(upiIntent)
                        }
                        viewModel.payMonthlyRentViaUpi(
                            amount = payAmount,
                            upiApp = selectedUpiApp,
                            upiId = upiVpaInput,
                            paymentType = PaymentType.RENT
                        )
                        showPaymentGatewayPopup = false
                        activeManageTab = 3
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("confirm_upi_rent_payment_btn")
                ) {
                    Text(
                        text = "Pay via $selectedUpiApp",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentGatewayPopup = false }) {
                    Text("Cancel", fontFamily = PoppinsFontFamily, color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun StayMetricItem(value: String, subtitle: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = PureWhite
        )
        Text(
            text = subtitle,
            fontFamily = PoppinsFontFamily,
            fontSize = 9.sp,
            color = MintSoft.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun StayDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(24.dp)
            .background(PureWhite.copy(alpha = 0.16f))
    )
}

@Composable
private fun MealToggleRow(
    title: String,
    menuDesc: String,
    isOptedIn: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(14.dp),
        color = if (isOptedIn) SoftSagePill.copy(alpha = 0.55f) else SoftCardSurface,
        border = BorderStroke(1.dp, if (isOptedIn) TealPrimary.copy(alpha = 0.35f) else WhisperBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isOptedIn) Icons.Default.Restaurant else Icons.Default.DoNotDisturbOn,
                        contentDescription = null,
                        tint = if (isOptedIn) TealPrimary else TextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = menuDesc,
                    fontFamily = PoppinsFontFamily,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(999.dp),
                color = if (isOptedIn) TealPrimary else SoftGoldPill
            ) {
                Text(
                    text = if (isOptedIn) "Attending ✓" else "Skipping",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = if (isOptedIn) PureWhite else GoldDark,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun TomorrowMealChip(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (active) TealDark else SoftCardSurface,
        border = BorderStroke(1.dp, if (active) TealDark else WhisperBorder),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp)
        ) {
            Icon(
                imageVector = if (active) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (active) MintSoft else TextSecondary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = if (active) PureWhite else TextPrimary
            )
        }
    }
}

@Composable
private fun DonutLegendRow(
    dotColor: Color,
    label: String,
    amount: String,
    percent: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontFamily = PoppinsFontFamily,
                fontSize = 12.sp,
                color = TextPrimary
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = amount,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = percent,
                fontFamily = PoppinsFontFamily,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun MyStayGuideStepRow(
    stepNumber: String,
    icon: ImageVector,
    title: String,
    description: String
) {
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
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(TealDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MintSoft,
                    modifier = Modifier.size(16.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = SoftSagePill
                    ) {
                        Text(
                            text = "STEP $stepNumber",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = TealPrimary,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = title,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = description,
                    fontFamily = PoppinsFontFamily,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

