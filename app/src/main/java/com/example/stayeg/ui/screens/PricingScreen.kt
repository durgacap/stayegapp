package com.example.stayeg.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stayeg.data.model.PricingPlan
import com.example.stayeg.ui.theme.*
import com.example.stayeg.ui.viewmodel.StayEgViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PricingScreen(viewModel: StayEgViewModel) {
    val plans = viewModel.repository.pricingPlans

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PG Owner Plans", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.goBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 14.dp, horizontal = 0.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Limited time offer card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = GoldLight),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(GoldAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("First 1,000 PG Owners Get 1 Year FREE!", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                            Text("Use coupon FIRSTFREE at checkout. No credit card required.", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
            }

            // Tenant Guarantee Note
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SuccessGreenLight),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "StayEg is 100% FREE for Tenants. Zero brokerage, free discovery, and digital rent receipts forever.",
                            fontSize = 12.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Pricing Plans
            items(plans) { plan ->
                PricingPlanCard(plan = plan, onSubscribe = {
                    viewModel.showToast("Subscribed to ${plan.name} Plan! Welcome aboard.")
                })
            }

            // Money Back Guarantee
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("15-Day Money-Back Guarantee", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "If you are not 100% satisfied with our room & rent management features within 15 days, we will refund your complete amount. No questions asked.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PricingPlanCard(plan: PricingPlan, onSubscribe: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = androidx.compose.foundation.BorderStroke(
            if (plan.popular) 2.dp else 1.dp,
            if (plan.popular) BluePrimary else BorderLight
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (plan.popular) 4.dp else 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(plan.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                    Text("${plan.duration} Subscription", fontSize = 12.sp, color = TextMuted)
                }

                if (plan.badge.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (plan.popular) BluePrimary else GoldAccent)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(plan.badge, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text("₹${plan.price}", fontWeight = FontWeight.Bold, fontSize = 26.sp, color = TextPrimary)
                if (plan.discount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "₹${plan.originalPrice}",
                        fontSize = 14.sp,
                        color = TextMuted,
                        style = androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = BorderLight)
            Spacer(modifier = Modifier.height(12.dp))

            Text("What's Included:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(8.dp))

            plan.features.forEach { feat ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(feat, fontSize = 12.sp, color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSubscribe,
                colors = ButtonDefaults.buttonColors(containerColor = if (plan.popular) BluePrimary else BlueDeep),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Text("Choose ${plan.name}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
