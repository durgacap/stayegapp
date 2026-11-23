package com.example.stayeg.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stayeg.ui.theme.*
import com.example.stayeg.ui.viewmodel.StayEgViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(viewModel: StayEgViewModel) {
    val stayConfig by viewModel.activeStayConfig.collectAsState()
    val payments by viewModel.payments.collectAsState()
    val feedbacks by viewModel.featureFeedbacks.collectAsState()

    var selectedUpiApp by remember { mutableStateOf("Google Pay") }
    var feedbackText by remember { mutableStateOf("") }
    var preferredPaymentMode by remember { mutableStateOf("UPI AutoPay / Zero-Fee UPI") }

    val totalDue = stayConfig.monthlyRent + stayConfig.electricityExtra

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Rent, Deposit & Digital Receipts",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = TealDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.goBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TealDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PureWhite)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Active Stay Rent & Escrow Summary Hero Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = TealDark),
                    border = BorderStroke(1.5.dp, TealPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = SoftGoldPill
                            ) {
                                Text(
                                    text = "ZERO CONVENIENCE FEE",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GoldDark,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = "Check-In: ${stayConfig.moveInDate}",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MintSoft
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = stayConfig.pgName,
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = PureWhite
                        )
                        Text(
                            text = "Room ${stayConfig.roomCode} • Bed ${stayConfig.bedNumber} • Security Deposit ₹${stayConfig.securityDeposit}",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 12.sp,
                            color = MintSoft
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = PureWhite.copy(alpha = 0.14f))
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Monthly Rent + Utilities",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 11.sp,
                                    color = MintSoft
                                )
                                Text(
                                    text = "₹$totalDue",
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 24.sp,
                                    color = PureWhite
                                )
                                Text(
                                    text = "Base ₹${stayConfig.monthlyRent} + Power/Maint ₹${stayConfig.electricityExtra}",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 10.sp,
                                    color = MintSoft.copy(alpha = 0.85f)
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Payment App",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 10.sp,
                                    color = MintSoft
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("Google Pay", "PhonePe", "UPI").forEach { app ->
                                        val isSel = selectedUpiApp == app
                                        Surface(
                                            onClick = { selectedUpiApp = app },
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSel) SoftGoldPill else TealPrimary
                                        ) {
                                            Text(
                                                text = app,
                                                fontFamily = PoppinsFontFamily,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSel) GoldDark else PureWhite,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                viewModel.payMonthlyRentViaUpi(
                                    amount = totalDue,
                                    upiApp = selectedUpiApp
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SoftGoldPill,
                                contentColor = GoldDark
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("pay_rent_button")
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Record & Confirm Rent Payment (₹$totalDue)",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Digital Rent Receipts & Payment Ledger
            if (payments.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Digital Rent Receipts (${payments.size})",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "HRA & Owner Verified",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TealPrimary
                        )
                    }
                }

                items(payments.take(6)) { payment ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        border = BorderStroke(1.dp, WhisperBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(SoftSagePill),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ReceiptLong,
                                        contentDescription = null,
                                        tint = TealDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "${payment.type.name} • ₹${payment.amount}",
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${payment.method} • ${payment.paidDate.ifBlank { payment.dueDate }}",
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = SoftSagePill
                            ) {
                                Text(
                                    text = payment.status.name,
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = TealDark,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Payment Preferences & Escrow Feedback Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, WhisperBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Customize Your Rent & Deposit Protection",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Select your preferred billing and deposit protection option for your stay.",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        listOf(
                            "UPI AutoPay / Zero-Fee UPI",
                            "Escrow Security Deposit Protection",
                            "Automated GST / HRA Rent Receipts",
                            "Split Rent with Roommate"
                        ).forEach { mode ->
                            val isSel = preferredPaymentMode == mode
                            Surface(
                                onClick = { preferredPaymentMode = mode },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) SoftSagePill else SoftCardSurface,
                                border = BorderStroke(1.dp, if (isSel) TealPrimary else WhisperBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = mode,
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSel) TealDark else TextPrimary
                                    )
                                    RadioButton(
                                        selected = isSel,
                                        onClick = { preferredPaymentMode = mode },
                                        colors = RadioButtonDefaults.colors(selectedColor = TealDark)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = feedbackText,
                            onValueChange = { feedbackText = it },
                            label = { Text("Notes or Custom Billing Request") },
                            placeholder = { Text("e.g., Need monthly HRA rent receipt with landlord PAN for tax exemption...") },
                            minLines = 2,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                val text = if (feedbackText.isBlank()) "Requested $preferredPaymentMode" else "$preferredPaymentMode: $feedbackText"
                                viewModel.submitFeatureFeedback("Payment Gateway", 5, text)
                                feedbackText = ""
                            },
                            border = BorderStroke(1.dp, TealPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("submit_payment_feedback_button")
                        ) {
                            Icon(Icons.Default.RateReview, contentDescription = null, tint = TealDark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Save Payment Preference",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = TealDark
                            )
                        }
                    }
                }
            }

            if (feedbacks.isNotEmpty()) {
                item {
                    Text(
                        text = "Saved Billing Preferences",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                }
                items(feedbacks) { fb ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        border = BorderStroke(1.dp, WhisperBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "${fb.featureCategory} • ${fb.createdAt}",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = TealPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(fb.feedbackText, fontFamily = PoppinsFontFamily, fontSize = 12.sp, color = TextPrimary)
                        }
                    }
                }
            }
        }
    }
}
