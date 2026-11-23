package com.example.stayeg.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
fun OwnerDashboardScreen(viewModel: StayEgViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allPgs by viewModel.allPgs.collectAsState()
    val complaints by viewModel.complaints.collectAsState()
    val visitSchedules by viewModel.visitSchedules.collectAsState()

    val verifiedPgs = allPgs.filter { !it.isDirectoryListing || it.isVerified }
    val allBeds = verifiedPgs.flatMap { it.rooms }.flatMap { it.beds }
    val occupiedBeds = allBeds.count { it.status == com.example.stayeg.data.model.BedStatus.OCCUPIED }
    val totalBeds = allBeds.size.coerceAtLeast(1)
    val occupancyPct = ((occupiedBeds * 100) / totalBeds).coerceIn(1, 100)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Owner Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Owner Dashboard",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Managing ${verifiedPgs.size} Verified PGs • ${allPgs.count { it.isDirectoryListing && !it.isVerified }} Directory PGs",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoldLight)
                        .border(1.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("STARTER PLAN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GoldDark)
                }
            }
        }

        // Metrics Grid (2x2)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Monthly Revenue",
                        value = "₹1,86,400",
                        subtitle = "+12% from last month",
                        icon = Icons.Default.CurrencyRupee,
                        color = TealDark,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Occupancy Rate",
                        value = "$occupancyPct%",
                        subtitle = "$occupiedBeds of ${allBeds.size} beds occupied",
                        icon = Icons.Default.Bed,
                        color = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Pending Rent Dues",
                        value = "₹24,000",
                        subtitle = "2 tenants awaiting payment",
                        icon = Icons.Default.PendingActions,
                        color = WarningAmber,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Active Issues",
                        value = "${complaints.count { it.status.name != "RESOLVED" }}",
                        subtitle = "${visitSchedules.size} scheduled visits",
                        icon = Icons.Default.Warning,
                        color = ErrorRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Scheduled Campus Visits (Rapido / Cab Pickup)
        if (visitSchedules.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Upcoming Visitor Pickups (${visitSchedules.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SuccessGreen
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        visitSchedules.take(3).forEach { v ->
                            Text(
                                text = "• ${v.visitorName} (${v.visitorPhone}) → ${v.pgName} on ${v.visitDate} ${v.visitTime} [${v.pickupMode}]",
                                fontSize = 11.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Quick Operations Menu
        item {
            Text("Property Operations", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OperationRow(
                    title = "My Properties & Rooms (${verifiedPgs.size})",
                    subtitle = "Manage ${verifiedPgs.size} verified PGs, rooms, and bed configurations",
                    icon = Icons.Default.Apartment,
                    color = TealDark,
                    onClick = { viewModel.navigateTo(Screen.OwnerPGs) }
                )
                OperationRow(
                    title = "Rent Collection & Reminders",
                    subtitle = "Track tenant payments, send WhatsApp reminders",
                    icon = Icons.Default.AccountBalanceWallet,
                    color = SuccessGreen,
                    onClick = { viewModel.navigateTo(Screen.OwnerRent) }
                )
                OperationRow(
                    title = "Tenant Complaints Management",
                    subtitle = "Resolve issues, assign plumber/electrician staff",
                    icon = Icons.Default.ReportProblem,
                    color = WarningAmber,
                    onClick = { viewModel.navigateTo(Screen.OwnerComplaints) }
                )
                OperationRow(
                    title = "Digital QR Code Check-In",
                    subtitle = "Tenant scans QR code at entrance for quick onboarding",
                    icon = Icons.Default.QrCode2,
                    color = TealPrimary,
                    onClick = { viewModel.showToast("QR Code generated for Sunrise PG front desk!") }
                )
                OperationRow(
                    title = "Live Property Inventory & Cloud Sync",
                    subtitle = "${allPgs.size} Bengaluru PG listings synchronized in real time",
                    icon = Icons.Default.CloudSync,
                    color = Color(0xFF10B981),
                    onClick = { viewModel.navigateTo(Screen.SupabaseSync) }
                )
            }
        }

        // Recent Activity Feed
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Recent Activity Log", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(10.dp))

                    ActivityItem("Rent Collected", "₹12,000 received from Vikram Singh via UPI", "2h ago")
                    ActivityItem("New Booking", "Rohan Mehta booked Bed 2 in Room A101", "1d ago")
                    ActivityItem("Complaint Resolved", "Plumber fixed bathroom tap leakage in PG #1", "2d ago")
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 10.sp, color = TextMuted)
        }
    }
}

@Composable
private fun OperationRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                Text(subtitle, fontSize = 11.sp, color = TextSecondary)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
        }
    }
}

@Composable
private fun ActivityItem(title: String, desc: String, time: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(BluePrimary)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TextPrimary)
            Text(desc, fontSize = 11.sp, color = TextSecondary)
        }
        Text(time, fontSize = 10.sp, color = TextMuted)
    }
}
