package com.example.stayeg.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stayeg.ui.components.StayEgPopupInfoDialog
import com.example.stayeg.ui.components.openGoogleMapsIntent
import com.example.stayeg.ui.navigation.Screen
import com.example.stayeg.ui.theme.*
import com.example.stayeg.ui.viewmodel.StayEgViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    pgId: String,
    bedId: String,
    price: Int,
    roomCode: String,
    viewModel: StayEgViewModel
) {
    val context = LocalContext.current
    val allPgs by viewModel.allPgs.collectAsState()
    val pg = allPgs.firstOrNull { it.id == pgId } ?: allPgs.first()
    val currentUser by viewModel.currentUser.collectAsState()

    var tenantName by remember { mutableStateOf(currentUser?.name ?: "") }
    var tenantPhone by remember { mutableStateOf(currentUser?.phone ?: "") }
    var tenantEmail by remember { mutableStateOf(currentUser?.email ?: "") }
    var moveInDate by remember { mutableStateOf("This Week") }
    var preferredSharing by remember { mutableStateOf("Room $roomCode") }
    var feedbackNotes by remember { mutableStateOf("") }
    var showPaymentAlertPopup by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Free In-App Bed Booking",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = TealDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.goBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TealDark)
                    }
                },
                actions = {
                    IconButton(onClick = { showPaymentAlertPopup = true }) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = "Payment Info", tint = TealPrimary)
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
            // Selected PG & Room Summary Card (NO Owner Phone Numbers!)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = TealDark),
                    border = BorderStroke(1.5.dp, TealPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SoftGoldPill
                            ) {
                                Text(
                                    text = "₹0 TENANT BOOKING FEE",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GoldDark,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Surface(
                                onClick = { showPaymentAlertPopup = true },
                                shape = RoundedCornerShape(10.dp),
                                color = TealPrimary
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = MintSoft,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Zero Fee",
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PureWhite
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            pg.name,
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = PureWhite
                        )
                        Text(
                            "${pg.area} • Room $roomCode • ₹$price/mo",
                            fontFamily = JakartaFontFamily,
                            fontSize = 12.sp,
                            color = MintSoft
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                openGoogleMapsIntent(context, "${pg.name}, ${pg.address}, ${pg.area}, Bangalore")
                            },
                            border = BorderStroke(1.dp, MintSoft),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, tint = MintSoft, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Check Directions on Google Maps",
                                fontFamily = OutfitFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MintSoft
                            )
                        }
                    }
                }
            }

            // Free In-App Bed Booking Form
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Confirm Your Bed in StayEg",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = TealDark
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = tenantName,
                            onValueChange = { tenantName = it },
                            label = { Text("Your Full Name") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("booking_name_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = tenantPhone,
                            onValueChange = { tenantPhone = it },
                            label = { Text("Registered Mobile Number") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("booking_phone_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = moveInDate,
                            onValueChange = { moveInDate = it },
                            label = { Text("Check-In Date / Timeline") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = feedbackNotes,
                            onValueChange = { feedbackNotes = it },
                            label = { Text("Stay Preferences / Feedback") },
                            minLines = 2,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (tenantName.isBlank() || tenantPhone.isBlank()) {
                                    viewModel.showToast("Please enter your name and mobile number")
                                    return@Button
                                }
                                viewModel.bookBed(
                                    pgId = pg.id,
                                    bedId = bedId.ifBlank { "${pg.id}-b1" },
                                    checkInDate = moveInDate,
                                    amount = price,
                                    pgName = pg.name,
                                    roomCode = roomCode,
                                    bedNumber = 1
                                )
                                viewModel.expressInterestInPg(
                                    pgId = pg.id,
                                    pgName = pg.name,
                                    area = pg.area,
                                    tenantName = tenantName,
                                    tenantPhone = tenantPhone,
                                    tenantEmail = tenantEmail,
                                    preferredSharing = preferredSharing,
                                    expectedMoveIn = moveInDate,
                                    notes = feedbackNotes
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TealDark),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("confirm_booking_button")
                        ) {
                            Text(
                                "Confirm Free Bed Booking →",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = PureWhite
                            )
                        }
                    }
                }
            }
        }
    }

    if (showPaymentAlertPopup) {
        StayEgPopupInfoDialog(
            title = "Zero-Brokerage Bed Reservation",
            subtitle = "Book Your Bed Free • Pay at Check-In",
            bulletPoints = listOf(
                "100% Free Bed Reservation: Tenants never pay brokerage or booking fees on StayEg.",
                "Pay Directly at Check-In: Settle your monthly rent and security deposit directly when you move into your room.",
                "Share Feedback: Let us know how we can make your check-in smoother!"
            ),
            primaryButtonText = "Share Feedback",
            secondaryButtonText = "Close",
            onDismiss = { showPaymentAlertPopup = false },
            onPrimaryClick = {
                showPaymentAlertPopup = false
                viewModel.navigateTo(Screen.FeedbackHub)
            },
            onSecondaryClick = { showPaymentAlertPopup = false }
        )
    }
}
