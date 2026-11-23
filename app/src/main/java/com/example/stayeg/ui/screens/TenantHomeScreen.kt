package com.example.stayeg.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stayeg.ui.navigation.Screen
import com.example.stayeg.ui.theme.*
import com.example.stayeg.ui.viewmodel.StayEgViewModel

@Composable
fun TenantHomeScreen(viewModel: StayEgViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val stayConfig by viewModel.activeStayConfig.collectAsState()
    val bookings by viewModel.bookings.collectAsState()
    val allPgs by viewModel.allPgs.collectAsState()
    val activeBooking = bookings.firstOrNull()
    val bookedPg = allPgs.firstOrNull { it.id == activeBooking?.pgId } ?: allPgs.firstOrNull()
    val monthlyRent = activeBooking?.advancePaid ?: stayConfig.monthlyRent
    val complaints by viewModel.complaints.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Welcome Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Hello, ${currentUser?.name ?: "Guest"}!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Manage your stay, rent payments, and services.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SoftSagePill),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = TealDark)
                }
            }
        }

        // Active Stay Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, WhisperBorder),
                modifier = Modifier.fillMaxWidth().testTag("active_stay_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SuccessGreenLight)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("ACTIVE STAY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                        }

                        Text(
                            text = "Check-in: ${activeBooking?.checkInDate ?: stayConfig.moveInDate}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = activeBooking?.pgName ?: stayConfig.pgName.ifBlank { bookedPg?.name ?: "StayEg Verified Residence" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Room ${activeBooking?.roomCode ?: stayConfig.roomCode} · Bed ${activeBooking?.bedNumber ?: stayConfig.bedNumber}",
                        fontSize = 13.sp,
                        color = TealPrimary,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = BorderLight)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Monthly Rent", fontSize = 11.sp, color = TextMuted)
                            Text("₹$monthlyRent", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        }
                        Button(
                            onClick = { viewModel.navigateTo(Screen.MyStay) },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftSagePill, contentColor = TealDark),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Room Details", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Rent Due Alert Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(WarningAmber),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Next Rent Due: 1st of Month", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                        Text("Amount: ₹$monthlyRent · Zero Late Fees", fontSize = 11.sp, color = TextSecondary)
                    }
                    Button(
                        onClick = { viewModel.navigateTo(Screen.Payments) },
                        colors = ButtonDefaults.buttonColors(containerColor = TealDark),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Pay Rent", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Quick Action Shortcuts Grid
        item {
            Text("Quick Services", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionItem(
                    icon = Icons.Default.Bed,
                    label = "My Stay",
                    color = TealDark,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(Screen.MyStay) }
                )
                QuickActionItem(
                    icon = Icons.Default.Payment,
                    label = "Payments",
                    color = SuccessGreen,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(Screen.Payments) }
                )
                QuickActionItem(
                    icon = Icons.Default.ReportProblem,
                    label = "Complaints",
                    color = WarningAmber,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(Screen.Complaints) }
                )
                QuickActionItem(
                    icon = Icons.Default.Groups,
                    label = "Community",
                    color = TealPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(Screen.Community) }
                )
            }
        }

        // Recent Complaints Snippet
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Recent Complaints", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        TextButton(onClick = { viewModel.navigateTo(Screen.Complaints) }) {
                            Text("View All", fontSize = 11.sp, color = TealPrimary)
                        }
                    }

                    if (complaints.isEmpty()) {
                        Text("No active complaints. Everything is running smoothly!", fontSize = 12.sp, color = TextSecondary)
                    } else {
                        complaints.take(2).forEach { complaint ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(complaint.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                                    Text(complaint.createdAt, fontSize = 10.sp, color = TextMuted)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (complaint.status.name == "RESOLVED") SuccessGreenLight else WarningAmberLight)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = complaint.status.name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (complaint.status.name == "RESOLVED") SuccessGreen else WarningAmber
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
private fun QuickActionItem(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}
