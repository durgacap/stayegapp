package com.example.stayeg.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.stayeg.ui.viewmodel.calculateGpsDistanceKm
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import com.example.stayeg.data.model.MapSearchHubCoord
import com.example.stayeg.data.model.PG
import com.example.stayeg.ui.navigation.Screen
import com.example.stayeg.ui.theme.*
import com.example.stayeg.ui.viewmodel.StayEgViewModel
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class StayEgGuideStep(
    val stepNumber: Int,
    val badge: String,
    val title: String,
    val subtitle: String,
    val highlightBenefit: String,
    val bulletPoints: List<String>,
    val actionLabel: String,
    val icon: ImageVector,
    val accentColor: Color
)

data class StayEgBenefitItem(
    val title: String,
    val valueBadge: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color
)

val StayEgDefaultGuideSteps = listOf(
    StayEgGuideStep(
        stepNumber = 1,
        badge = "STEP 01 • DISCOVER",
        title = "Explore 2,415+ Verified Bengaluru PGs",
        subtitle = "Filter by tech hub, metro corridor, sharing type, AC, food menu, and monthly budget with verified neighborhood visuals.",
        highlightBenefit = "Save 15+ hours of physical PG hunting across Bengaluru",
        bulletPoints = listOf(
            "Instant search across HSR, Koramangala, Indiranagar, Whitefield & BTM",
            "Transparent monthly rent & refundable security deposit breakdown",
            "Side-by-side PG comparison and saved favorites shortlist"
        ),
        actionLabel = "Explore Verified PGs",
        icon = Icons.Default.TravelExplore,
        accentColor = TealPrimary
    ),
    StayEgGuideStep(
        stepNumber = 2,
        badge = "STEP 02 • LIVE BED DECK",
        title = "Choose Your Exact Floor, Room & Bed",
        subtitle = "View real-time green available beds, window/balcony positions, and book a free cab pickup visit or instant ₹0-brokerage hold.",
        highlightBenefit = "₹0 Brokerage • Save ₹12,000 to ₹25,000 on move-in",
        bulletPoints = listOf(
            "Interactive floor-by-floor bed availability map",
            "Schedule an on-site visit with complimentary pickup drop",
            "Lock your chosen bed number with instant booking confirmation"
        ),
        actionLabel = "Search & Pick a Bed",
        icon = Icons.Default.SingleBed,
        accentColor = TealDark
    ),
    StayEgGuideStep(
        stepNumber = 3,
        badge = "STEP 03 • DIGITAL KYC PASSPORT",
        title = "One-Time Digital KYC & Custom Profile",
        subtitle = "Upload your Aadhaar, PAN, Passport, or Student ID once and customize your work/college preferences for paperless check-in.",
        highlightBenefit = "Instant Resident Passport • Zero photocopy paperwork",
        bulletPoints = listOf(
            "Upload front & back ID photos directly from your device",
            "Real-time KYC verification badge on your resident profile",
            "Reusable verification across all StayEg partner residences"
        ),
        actionLabel = "Open Digital KYC & Profile",
        icon = Icons.Default.VerifiedUser,
        accentColor = SoftGoldAccent
    ),
    StayEgGuideStep(
        stepNumber = 4,
        badge = "STEP 04 • SMART STAY HUB",
        title = "Manage Daily Meals, UPI Rent & Support",
        subtitle = "Control your entire PG lifestyle from one dashboard: daily 3-time meal attendance, 5G Wi-Fi, gate passes, UPI rent, and maintenance tickets.",
        highlightBenefit = "All-in-One Resident App • 24/7 Priority SLA Support",
        bulletPoints = listOf(
            "Pay monthly rent via Google Pay, PhonePe, Paytm or UPI ID with instant receipt",
            "Opt in/out of daily breakfast, lunch & dinner or copy 5G Wi-Fi credentials",
            "Raise photo-backed maintenance tickets & digital visitor gate passes"
        ),
        actionLabel = "Open My Stay Hub",
        icon = Icons.Default.AutoAwesome,
        accentColor = TealPrimary
    )
)

val StayEgDefaultBenefits = listOf(
    StayEgBenefitItem(
        title = "100% Zero Brokerage",
        valueBadge = "SAVE ₹18,000+",
        description = "Pay direct property owner rent with zero middleman commission or hidden booking charges.",
        icon = Icons.Default.Savings,
        accentColor = TealPrimary
    ),
    StayEgBenefitItem(
        title = "Live Bed & Room Selection",
        valueBadge = "EXACT BED PICK",
        description = "See live vacant beds by floor and lock your preferred window or quiet corner bed.",
        icon = Icons.Default.MeetingRoom,
        accentColor = TealDark
    ),
    StayEgBenefitItem(
        title = "Paperless Digital KYC",
        valueBadge = "2-MIN CHECK-IN",
        description = "Complete your identity verification once in-app for fast, secure move-in at any residence.",
        icon = Icons.Default.Badge,
        accentColor = SoftGoldAccent
    ),
    StayEgBenefitItem(
        title = "Homestyle Meals & 5G Wi-Fi",
        valueBadge = "ALL-INCLUSIVE",
        description = "Daily breakfast, lunch, and dinner tracking plus high-speed fiber Wi-Fi and 24/7 helpdesk.",
        icon = Icons.Default.Restaurant,
        accentColor = TealPrimary
    )
)

@Composable
fun StayEgTopBar(
    viewModel: StayEgViewModel,
    onMenuClick: () -> Unit = {}
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val showPostLoginGuide by viewModel.showPostLoginUserGuidePopup.collectAsState()

    if (showPostLoginGuide) {
        StayEgAnimatedUserGuideDialog(
            onDismiss = { viewModel.dismissPostLoginUserGuidePopup() },
            onNavigateToStep = { stepNumber ->
                viewModel.dismissPostLoginUserGuidePopup()
                when (stepNumber) {
                    1 -> viewModel.navigateTo(Screen.Explore)
                    2 -> viewModel.navigateTo(Screen.Search)
                    3 -> viewModel.navigateTo(Screen.Profile)
                    4 -> viewModel.navigateTo(Screen.MyStay)
                }
            }
        )
    }

    Surface(
        color = PureWhite,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: StayEg Text Logo Only (No Icon Box)
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { viewModel.navigateTo(Screen.Landing) }
                    .testTag("top_bar_logo")
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = TealDark, fontWeight = FontWeight.ExtraBold)) {
                            append("Stay")
                        }
                        withStyle(SpanStyle(color = TealPrimary, fontWeight = FontWeight.ExtraBold)) {
                            append("Eg")
                        }
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = PoppinsFontFamily,
                        fontSize = 21.sp,
                        letterSpacing = (-0.5).sp
                    )
                )
            }

            // Right: Clean Help Icon + Account / Sign In Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { viewModel.openPostLoginUserGuidePopup() },
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(SoftSagePill)
                        .testTag("top_bar_alert_info_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "User Guide",
                        tint = TealDark,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Profile / Sign In Pill
                val user = currentUser
                if (user != null) {
                    Surface(
                        color = TealDark,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .clickable { viewModel.navigateTo(Screen.Profile) }
                            .testTag("top_bar_profile_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile",
                                tint = MintSoft,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = user.name.split(" ").firstOrNull() ?: "Account",
                                style = MaterialTheme.typography.labelMedium,
                                color = PureWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = { viewModel.openAuthOnboarding() },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("top_bar_login_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Login,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Sign In",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Interactive Animated User Guide & Member Benefits Showcase Card.
 * Placed on LandingScreen and available across the app for step-by-step interactive guidance.
 */
@Composable
fun StayEgInteractiveUserGuideCard(
    onStepAction: (Int) -> Unit,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = true
) {
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }
    var activeStepIndex by remember { mutableIntStateOf(0) }
    var showBenefitsDrawer by remember { mutableStateOf(true) }

    val steps = StayEgDefaultGuideSteps
    val currentStep = steps[activeStepIndex]

    val infiniteTransition = rememberInfiniteTransition(label = "user_guide_glow")
    val iconFloatOffset by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_float"
    )
    val badgePulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.78f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badge_pulse"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            .testTag("interactive_user_guide_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(1.5.dp, currentStep.accentColor.copy(alpha = 0.28f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Gradient Editorial Header Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                TealDeep,
                                TealDark,
                                TealPrimary
                            )
                        )
                    )
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 18.dp, vertical = 14.dp)
                    .testTag("user_guide_expand_toggle")
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
                                .size(40.dp)
                                .offset(y = iconFloatOffset.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PureWhite.copy(alpha = 0.14f))
                                .border(1.dp, PureWhite.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MintSoft,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Surface(
                                color = SoftGoldPill.copy(alpha = badgePulseAlpha),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "INTERACTIVE USER GUIDE & BENEFITS",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        letterSpacing = 0.8.sp
                                    ),
                                    color = GoldDark,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "How StayEg Works in 4 Simple Steps",
                                style = MaterialTheme.typography.titleMedium,
                                color = PureWhite,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Surface(
                        color = PureWhite.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isExpanded) "Hide" else "View Guide",
                                style = MaterialTheme.typography.labelSmall,
                                color = PureWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle User Guide",
                                tint = PureWhite,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    // Step Selector Tabs with Animated Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        steps.forEachIndexed { index, step ->
                            val isSelected = index == activeStepIndex
                            val chipBg by animateColorAsState(
                                targetValue = if (isSelected) TealDark else SoftGalleryBg,
                                animationSpec = tween(240),
                                label = "step_chip_bg"
                            )
                            val chipText by animateColorAsState(
                                targetValue = if (isSelected) PureWhite else TextPrimary,
                                animationSpec = tween(240),
                                label = "step_chip_text"
                            )

                            Surface(
                                color = chipBg,
                                shape = RoundedCornerShape(14.dp),
                                border = if (isSelected) null else BorderStroke(1.dp, WhisperBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { activeStepIndex = index }
                                    .testTag("user_guide_step_tab_${step.stepNumber}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = step.icon,
                                        contentDescription = step.title,
                                        tint = if (isSelected) MintSoft else step.accentColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Step ${step.stepNumber}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = chipText,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Animated Step Content Card
                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            if (targetState.stepNumber > initialState.stepNumber) {
                                (slideInHorizontally { width -> width / 3 } + fadeIn(tween(240)))
                                    .togetherWith(slideOutHorizontally { width -> -width / 3 } + fadeOut(tween(180)))
                            } else {
                                (slideInHorizontally { width -> -width / 3 } + fadeIn(tween(240)))
                                    .togetherWith(slideOutHorizontally { width -> width / 3 } + fadeOut(tween(180)))
                            }
                        },
                        label = "user_guide_step_transition"
                    ) { step ->
                        Surface(
                            color = SoftSagePill.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, TealPrimary.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = TealPrimary.copy(alpha = 0.14f),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = step.badge,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TealDark,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }

                                    // Animated Progress Pill Dots
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        steps.forEachIndexed { idx, _ ->
                                            val dotWidth by animateDpAsState(
                                                targetValue = if (idx == activeStepIndex) 20.dp else 7.dp,
                                                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                                                label = "guide_dot_w"
                                            )
                                            val dotColor by animateColorAsState(
                                                targetValue = if (idx == activeStepIndex) TealPrimary else WhisperBorder,
                                                animationSpec = tween(250),
                                                label = "guide_dot_c"
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .height(7.dp)
                                                    .width(dotWidth)
                                                    .clip(CircleShape)
                                                    .background(dotColor)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = step.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = step.subtitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Highlight Benefit Pill
                                Surface(
                                    color = PureWhite,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, TealPrimary.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = null,
                                            tint = TealPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = step.highlightBenefit,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Bullet points
                                step.bulletPoints.forEach { bullet ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 3.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = TealPrimary,
                                            modifier = Modifier
                                                .padding(top = 2.dp)
                                                .size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = bullet,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextPrimary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Step navigation & Primary CTA row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = {
                                            activeStepIndex = (activeStepIndex + 1) % steps.size
                                        },
                                        modifier = Modifier.testTag("user_guide_next_step_button")
                                    ) {
                                        Text(
                                            text = if (activeStepIndex < steps.lastIndex) "Next Step (${activeStepIndex + 2}/4)" else "Replay Step 1",
                                            color = TealPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Button(
                                        onClick = { onStepAction(step.stepNumber) },
                                        colors = ButtonDefaults.buttonColors(containerColor = TealDark),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp),
                                        modifier = Modifier.testTag("user_guide_action_button_${step.stepNumber}")
                                    ) {
                                        Text(
                                            text = step.actionLabel,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = PureWhite,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = MintSoft,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Expandable Member Benefits Grid
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showBenefitsDrawer = !showBenefitsDrawer }
                            .padding(vertical = 4.dp)
                            .testTag("user_guide_benefits_toggle"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = TealPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Why 18,400+ Residents Choose StayEg",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Text(
                            text = if (showBenefitsDrawer) "Hide Perks" else "Show 4 Perks",
                            style = MaterialTheme.typography.labelSmall,
                            color = TealPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    AnimatedVisibility(
                        visible = showBenefitsDrawer,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StayEgBenefitMiniCard(
                                    benefit = StayEgDefaultBenefits[0],
                                    modifier = Modifier.weight(1f)
                                )
                                StayEgBenefitMiniCard(
                                    benefit = StayEgDefaultBenefits[1],
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StayEgBenefitMiniCard(
                                    benefit = StayEgDefaultBenefits[2],
                                    modifier = Modifier.weight(1f)
                                )
                                StayEgBenefitMiniCard(
                                    benefit = StayEgDefaultBenefits[3],
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StayEgBenefitMiniCard(
    benefit: StayEgBenefitItem,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = SoftGalleryBg,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, WhisperBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SoftSagePill),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = benefit.icon,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Surface(
                    color = SoftGoldPill,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = benefit.valueBadge,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                        color = GoldDark,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = benefit.title,
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = benefit.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                ),
                color = TextSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Full Interactive Animated User Guide & Member Benefits Dialog (launched from Top Bar "Guide" button).
 */
@Composable
fun StayEgAnimatedUserGuideDialog(
    onDismiss: () -> Unit,
    onNavigateToStep: (Int) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = 4-Step Guide, 1 = Resident Benefits
    var currentStepIdx by remember { mutableIntStateOf(0) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(spring(stiffness = Spring.StiffnessMediumLow))
                .testTag("popup_info_dialog")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
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
                                color = SoftGoldPill,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "STAYEG RESIDENT GUIDE & PERKS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldDark,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Guide",
                                    tint = PureWhite
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Smarter Co-Living in Bengaluru",
                            style = MaterialTheme.typography.titleLarge,
                            color = PureWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Zero brokerage • Verified rooms • Paperless KYC • Instant UPI receipts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MintSoft.copy(alpha = 0.9f)
                        )
                    }
                }

                // Mode Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SoftGalleryBg)
                        .padding(4.dp)
                ) {
                    listOf("4-Step Walkthrough", "Resident Benefits").forEachIndexed { idx, label ->
                        val active = selectedTab == idx
                        Surface(
                            color = if (active) TealDark else Color.Transparent,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = idx }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (active) PureWhite else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        fadeIn(tween(220)) togetherWith fadeOut(tween(180))
                    },
                    label = "dialog_tab_switch"
                ) { tab ->
                    if (tab == 0) {
                        val step = StayEgDefaultGuideSteps[currentStepIdx]
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                StayEgDefaultGuideSteps.forEachIndexed { i, s ->
                                    val isCurrent = i == currentStepIdx
                                    Surface(
                                        color = if (isCurrent) TealPrimary else SoftGalleryBg,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { currentStepIdx = i }
                                    ) {
                                        Text(
                                            text = "0${s.stepNumber}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (isCurrent) PureWhite else TextPrimary,
                                            fontWeight = FontWeight.ExtraBold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Surface(
                                color = SoftSagePill.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, TealPrimary.copy(alpha = 0.25f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = step.icon,
                                            contentDescription = null,
                                            tint = TealPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = step.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = step.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    step.bulletPoints.forEach { pt ->
                                        Row(
                                            modifier = Modifier.padding(vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = TealPrimary,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = pt,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextPrimary
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = { onNavigateToStep(step.stepNumber) },
                                        colors = ButtonDefaults.buttonColors(containerColor = TealDark),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = step.actionLabel,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = PureWhite,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StayEgDefaultBenefits.forEach { benefit ->
                                Surface(
                                    color = SoftGalleryBg,
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, WhisperBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(SoftSagePill),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = benefit.icon,
                                                contentDescription = null,
                                                tint = TealPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = benefit.title,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = TextPrimary,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                                Text(
                                                    text = benefit.valueBadge,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = GoldDark,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = benefit.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = TealDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("popup_info_close_button")
                    ) {
                        Text("Got It, Continue", color = PureWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Compact horizontal animated benefits ribbon that elevates forms and info screens.
 */
@Composable
fun StayEgAnimatedBenefitStrip(
    benefits: List<Pair<String, ImageVector>> = listOf(
        "₹0 Brokerage Fee" to Icons.Default.Savings,
        "Verified Bed Maps" to Icons.Default.SingleBed,
        "2-Min Digital KYC" to Icons.Default.VerifiedUser,
        "Instant UPI Receipts" to Icons.Default.ReceiptLong
    ),
    accentColor: Color = TealPrimary,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        benefits.forEach { (label, icon) ->
            Surface(
                color = SoftSagePill,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Reusable Popup Info Dialog supporting both single and dual action buttons.
 */
@Composable
fun StayEgPopupInfoDialog(
    title: String,
    subtitle: String,
    bulletPoints: List<String>,
    primaryButtonText: String = "Got It",
    secondaryButtonText: String? = null,
    onDismiss: () -> Unit,
    onPrimaryClick: () -> Unit = onDismiss,
    onSecondaryClick: (() -> Unit)? = null
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(spring(stiffness = Spring.StiffnessMediumLow))
                .testTag("popup_info_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(SoftSagePill),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = TealPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))
                bulletPoints.forEach { point ->
                    Row(
                        modifier = Modifier.padding(vertical = 5.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = point,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (secondaryButtonText != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { (onSecondaryClick ?: onDismiss).invoke() },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, WhisperBorder),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("popup_info_secondary_button")
                        ) {
                            Text(
                                text = secondaryButtonText,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Button(
                            onClick = onPrimaryClick,
                            colors = ButtonDefaults.buttonColors(containerColor = TealDark),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("popup_info_close_button")
                        ) {
                            Text(
                                text = primaryButtonText,
                                color = PureWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = onPrimaryClick,
                        colors = ButtonDefaults.buttonColors(containerColor = TealDark),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("popup_info_close_button")
                    ) {
                        Text(
                            text = primaryButtonText,
                            color = PureWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StayEgBottomNav(
    viewModel: StayEgViewModel,
    currentScreen: Screen
) {
    Surface(
        color = PureWhite,
        shadowElevation = 16.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        NavigationBar(
            containerColor = PureWhite,
            tonalElevation = 0.dp,
            modifier = Modifier
                .navigationBarsPadding()
                .height(68.dp)
        ) {
            val navItems = listOf(
                Triple("Home", Icons.Default.Home, Screen.Landing),
                Triple("Explore", Icons.Default.Explore, Screen.Explore),
                Triple("Search", Icons.Default.Search, Screen.Search),
                Triple("My Stay", Icons.Default.Bed, Screen.MyStay),
                Triple("Community", Icons.Default.Groups, Screen.Community),
                Triple("Profile", Icons.Default.Person, Screen.Profile)
            )

            navItems.forEach { (label, icon, screen) ->
                val isSelected = currentScreen::class == screen::class
                val iconScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.12f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "nav_icon_scale"
                )
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { viewModel.navigateTo(screen) },
                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            modifier = Modifier
                                .size(22.dp)
                                .scale(iconScale)
                        )
                    },
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            maxLines = 1
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TealDark,
                        selectedTextColor = TealDark,
                        indicatorColor = SoftSagePill,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("bottom_nav_${label.lowercase().replace(" ", "_")}")
                )
            }
        }
    }
}

@Composable
fun AmenityChip(
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = SoftSagePill,
        border = BorderStroke(1.dp, WhisperBorder),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Icon(
                imageVector = facilityIconFor(label),
                contentDescription = label,
                tint = TealPrimary,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

fun facilityIconFor(rawAmenity: String): ImageVector {
    val norm = rawAmenity.lowercase()
    return when {
        norm.contains("wifi") || norm.contains("wi-fi") || norm.contains("internet") -> Icons.Default.Wifi
        norm.contains("meal") || norm.contains("food") || norm.contains("mess") || norm.contains("dining") -> Icons.Default.Restaurant
        norm.contains("ac") || norm.contains("air") -> Icons.Default.AcUnit
        norm.contains("laundry") || norm.contains("wash") -> Icons.Default.LocalLaundryService
        norm.contains("power") || norm.contains("backup") -> Icons.Default.Bolt
        norm.contains("cctv") || norm.contains("security") || norm.contains("biometric") -> Icons.Default.Security
        norm.contains("gym") || norm.contains("fitness") -> Icons.Default.FitnessCenter
        norm.contains("parking") || norm.contains("bike") -> Icons.Default.DirectionsCar
        norm.contains("clean") || norm.contains("housekeeping") -> Icons.Default.CleaningServices
        norm.contains("tv") || norm.contains("lounge") -> Icons.Default.Tv
        norm.contains("water") || norm.contains("ro") -> Icons.Default.WaterDrop
        norm.contains("lift") || norm.contains("elevator") -> Icons.Default.Elevator
        else -> Icons.Default.CheckCircle
    }
}

fun openGoogleMapsIntent(context: Context, query: String) {
    try {
        val encoded = Uri.encode(query)
        val geoUri = Uri.parse("geo:0,0?q=$encoded")
        val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
            setPackage("com.google.android.apps.maps")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$encoded")
            context.startActivity(
                Intent(Intent.ACTION_VIEW, webUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        }
    } catch (_: Exception) {
        // Ignore on headless test environments without a browser/maps activity
    }
}

@Composable
fun SectionHeader(
    badge: String,
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Surface(
            color = SoftSagePill,
            shape = RoundedCornerShape(50)
        ) {
            Text(
                text = badge.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TealPrimary,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            fontWeight = FontWeight.ExtraBold
        )
        if (!subtitle.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun EditorialFacilityBox(
    icon: ImageVector,
    line1: String,
    line2: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SoftCardSurface,
        border = BorderStroke(1.dp, WhisperBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = line1,
                tint = TealPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = line1,
                style = MaterialTheme.typography.labelMedium,
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = line2,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun FacilityVisualCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isAvailable: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isAvailable) SoftSagePill.copy(alpha = 0.65f) else SoftCardSurface,
        border = BorderStroke(1.dp, if (isAvailable) TealPrimary.copy(alpha = 0.25f) else WhisperBorder),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PureWhite),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = TealPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ─── COMPACT (i) BUTTON FOR TRUSTED PG DIRECTORY & UNVERIFIED LISTINGS ──────

@Composable
fun StayEgTrustedListingInfoBanner(
    modifier: Modifier = Modifier,
    onOwnerClaimClick: (() -> Unit)? = null
) {
    var showInfoModal by remember { mutableStateOf(false) }

    Surface(
        onClick = { showInfoModal = true },
        shape = RoundedCornerShape(999.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, WhisperBorder),
        shadowElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("trusted_listing_info_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = "Trusted PG Directory",
                    tint = TealPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Trusted PG Directory • Unverified Listings",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Clean (i) Info Pill Button
            Surface(
                onClick = { showInfoModal = true },
                shape = RoundedCornerShape(999.dp),
                color = SoftSagePill,
                modifier = Modifier.testTag("trusted_listing_info_icon_btn")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "How Listings Work",
                        tint = TealDark,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Info",
                        fontFamily = JakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp,
                        color = TealDark
                    )
                }
            }
        }
    }

    if (showInfoModal) {
        StayEgPopupInfoDialog(
            title = "Trusted PG Directory",
            subtitle = "How Unverified & Owner-Claimed Listings Work",
            bulletPoints = listOf(
                "Unverified Public Listings: Properties are listed from public Bengaluru locality records so you can compare rents, photos, and exact Google Maps pins with zero brokerage.",
                "Inspect via Google Maps First: Always visit and check the room & facilities in person before paying any advance or security deposit.",
                "Owner Claim & Verification: PG owners can claim their listing anytime to activate verified room & bed inventory and QR check-in."
            ),
            primaryButtonText = "Got It",
            secondaryButtonText = if (onOwnerClaimClick != null) "Owner Claim" else null,
            onDismiss = { showInfoModal = false },
            onPrimaryClick = { showInfoModal = false },
            onSecondaryClick = {
                showInfoModal = false
                onOwnerClaimClick?.invoke()
            }
        )
    }
}

// ─── GPS NEARBY LOCATION TRACKING PILL BUTTON ────────────────────────────────

@SuppressLint("MissingPermission")
fun readDeviceGpsLocationOrNull(context: Context): Pair<Double, Double>? {
    return runCatching {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasFine && !hasCoarse) return null

        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )
        for (p in providers) {
            val loc = runCatching { lm.getLastKnownLocation(p) }.getOrNull()
            if (loc != null) {
                return loc.latitude to loc.longitude
            }
        }
        null
    }.getOrNull()
}

@Composable
fun StayEgGpsNearMePillButton(
    viewModel: StayEgViewModel,
    modifier: Modifier = Modifier,
    onActivated: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val filters by viewModel.filters.collectAsState()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        val coords = readDeviceGpsLocationOrNull(context)
        viewModel.activateNearMeGpsLocation(coords?.first, coords?.second)
        onActivated?.invoke()
    }

    val isActive = filters.nearMeActive

    Surface(
        onClick = {
            if (isActive) {
                viewModel.toggleNearMeGpsFilter()
            } else {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                if (hasPermission) {
                    val coords = readDeviceGpsLocationOrNull(context)
                    viewModel.activateNearMeGpsLocation(coords?.first, coords?.second)
                    onActivated?.invoke()
                } else {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }
        },
        shape = RoundedCornerShape(999.dp),
        color = if (isActive) TealDark else SoftSagePill,
        border = BorderStroke(1.dp, if (isActive) TealPrimary else WhisperBorder),
        modifier = modifier.testTag("gps_near_me_button")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Near Me GPS PGs",
                tint = if (isActive) MintSoft else TealPrimary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = if (isActive) "Near ${filters.userLocationLabel} ×" else "Near Me (GPS)",
                fontFamily = JakartaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = if (isActive) PureWhite else TealDark,
                maxLines = 1
            )
        }
    }
}

// ─── LIVE SEARCH AUTOCOMPLETE & RECOMMENDATIONS DROPDOWN ─────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StayEgSearchRecommendationsDropdown(
    query: String,
    viewModel: StayEgViewModel,
    onSelectArea: (String) -> Unit,
    onSelectPg: (PG) -> Unit,
    onViewAllResults: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val filters by viewModel.filters.collectAsState()
    val allPgs by viewModel.allPgs.collectAsState()

    val recommendations = remember(query, allPgs, filters.userLat, filters.userLng) {
        viewModel.getSearchRecommendations(query)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        val coords = readDeviceGpsLocationOrNull(context)
        viewModel.activateNearMeGpsLocation(coords?.first, coords?.second)
        onViewAllResults?.invoke()
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(1.dp, TealPrimary.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("search_recommendations_dropdown")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. One-Tap GPS Nearby Location Row
            Surface(
                onClick = {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

                    if (hasPermission) {
                        val coords = readDeviceGpsLocationOrNull(context)
                        viewModel.activateNearMeGpsLocation(coords?.first, coords?.second)
                        onViewAllResults?.invoke()
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                color = SoftSagePill.copy(alpha = 0.7f),
                border = BorderStroke(1.dp, TealPrimary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Use Current GPS Location",
                            tint = TealPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Use Current GPS Location • Near Me PGs",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = TealDark
                            )
                            Text(
                                text = if (filters.nearMeActive)
                                    "Active around ${filters.userLocationLabel} • Sorted by nearest km"
                                else
                                    "Detect nearby Bengaluru PGs around your location",
                                fontFamily = JakartaFontFamily,
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            // 2. Matching Localities / Areas
            if (recommendations.matchingAreas.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (query.isBlank()) "POPULAR BENGALURU HUBS" else "MATCHING LOCALITIES",
                        fontFamily = JakartaFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 9.5.sp,
                        letterSpacing = 0.7.sp,
                        color = TextSecondary
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        recommendations.matchingAreas.forEach { area ->
                            Surface(
                                onClick = { onSelectArea(area) },
                                shape = RoundedCornerShape(999.dp),
                                color = SoftCardSurface,
                                border = BorderStroke(1.dp, WhisperBorder)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = TealPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = area,
                                        fontFamily = JakartaFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Matching PG Listings
            if (recommendations.matchingPgs.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (query.isBlank()) "RECOMMENDED PGS NEAR YOU" else "MATCHING PG LISTINGS",
                        fontFamily = JakartaFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 9.5.sp,
                        letterSpacing = 0.7.sp,
                        color = TextSecondary
                    )
                    recommendations.matchingPgs.take(5).forEach { pg ->
                        val distKm = calculateGpsDistanceKm(filters.userLat, filters.userLng, pg.lat, pg.lng)
                        Surface(
                            onClick = { onSelectPg(pg) },
                            shape = RoundedCornerShape(12.dp),
                            color = SoftCardSurface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = pg.imageUrls.firstOrNull()
                                        ?: "https://images.unsplash.com/photo-1555854877-bab0e564b8d5?w=300&h=220&fit=crop",
                                    contentDescription = pg.name,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(PureWhite)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = pg.name,
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${pg.area} • ${String.format("%.1f km", distKm)} • ★ ${String.format("%.1f", pg.rating)}",
                                        fontFamily = JakartaFontFamily,
                                        fontSize = 10.5.sp,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "₹${pg.price}/mo",
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    color = TealDark
                                )
                            }
                        }
                    }
                }
            } else if (query.isNotBlank()) {
                Text(
                    text = "No exact PG match for \"$query\" — try Koramangala, HSR, Whitefield, or tap a locality above.",
                    fontFamily = JakartaFontFamily,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

// ─── IN-APP INTERACTIVE GOOGLE MAPS MARKER VIEW (MAP-ONLY INTEGRATION) ──────

val BengaluruMapPickupHubs = listOf(
    MapSearchHubCoord("Koramangala", "Sony World Signal", 12.9352, 77.6245, "Indiranagar Metro", 9, 0),
    MapSearchHubCoord("HSR Layout", "HSR BDA Complex", 12.9121, 77.6446, "Silk Board Metro", 11, 0),
    MapSearchHubCoord("Indiranagar", "Indiranagar 100ft Metro", 12.9784, 77.6408, "Indiranagar Metro", 8, 0),
    MapSearchHubCoord("Whitefield", "ITPL Main Gate", 12.9855, 77.7368, "Hopefarm Metro", 10, 0),
    MapSearchHubCoord("Electronic City", "Wipro Gate Phase 1", 12.8452, 77.6602, "Electronic City Metro", 8, 0),
    MapSearchHubCoord("Bellandur", "RMZ EcoSpace Gate", 12.9279, 77.6762, "Iblur Junction", 10, 0),
    MapSearchHubCoord("BTM Layout", "BTM Udupi Garden Signal", 12.9166, 77.6101, "Jayadeva Metro", 9, 0),
    MapSearchHubCoord("Marathahalli", "Marathahalli Bridge", 12.9591, 77.6974, "Kundalahalli Metro", 11, 0)
)

@Composable
fun StayEgRapidoMapRouteCard(
    pg: PG,
    viewModel: StayEgViewModel,
    onOpenFullGoogleMaps: () -> Unit,
    onScheduleVisitPickup: (String, String) -> Unit = { _, _ -> }
) {
    StayEgPropertyGoogleMapCard(
        pg = pg,
        onOpenFullGoogleMaps = {
            viewModel.trackUserActivity(
                actionType = "GOOGLE_MAPS_NAVIGATION",
                screenName = "PGDetailMap",
                targetPgId = pg.id,
                targetPgName = pg.name,
                details = "Opened Google Maps location for ${pg.name} (${pg.lat}, ${pg.lng})",
                latitude = pg.lat,
                longitude = pg.lng
            )
            onOpenFullGoogleMaps()
        }
    )
}

/**
 * Interactive Multi-Marker Bengaluru Google Maps View for ExploreScreen, SearchScreen & LandingScreen.
 */
@Composable
fun StayEgInteractivePgMapRadar(
    pgs: List<PG>,
    selectedArea: String,
    onSelectPg: (PG) -> Unit,
    onLaunchGoogleMapsForPg: (PG) -> Unit,
    onTrackMapCheckIn: (PG) -> Unit = {}
) {
    StayEgGoogleMapsListingsView(
        pgs = pgs,
        selectedArea = selectedArea,
        onSelectPg = onSelectPg,
        onLaunchGoogleMapsForPg = onLaunchGoogleMapsForPg,
        onTrackMapCheckIn = onTrackMapCheckIn
    )
}

// ─── AUTOMATIC MULTI-USE POPUP REVIEW & FEEDBACK DIALOG ─────────────────────

@Composable
fun StayEgSmartReviewPopupDialog(
    userName: String,
    onDismiss: () -> Unit,
    onSubmitReview: (Int, String, String) -> Unit
) {
    var selectedStars by remember { mutableIntStateOf(5) }
    var selectedHighlight by remember { mutableStateOf("Zero Brokerage & Verified PGs") }
    var reviewComment by remember { mutableStateOf("") }

    val highlightTags = listOf(
        "Zero Brokerage & Verified PGs",
        "Rapido Map & Easy Directions",
        "Fast Bed Booking & UPI Rent",
        "Digital KYC & Maintenance"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("smart_review_popup_dialog")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
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
                                color = SoftGoldPill,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "ENJOYING STAYEG?",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 9.5.sp,
                                    color = GoldDark,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss Review Popup",
                                    tint = PureWhite
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "How is your StayEg experience, ${userName.substringBefore(" ")}?",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = PureWhite
                        )
                        Text(
                            text = "You've explored multiple PGs & features! Share a quick rating to help us improve Bengaluru co-living.",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 11.5.sp,
                            color = MintSoft.copy(alpha = 0.92f)
                        )
                    }
                }

                Column(modifier = Modifier.padding(18.dp)) {
                    // 5-Star Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        (1..5).forEach { star ->
                            IconButton(
                                onClick = { selectedStars = star },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = if (star <= selectedStars) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "$star Stars",
                                    tint = if (star <= selectedStars) Color(0xFFF59E0B) else TextSecondary,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = when (selectedStars) {
                            5 -> "Loved it! Smooth & Super Helpful ★ 5/5"
                            4 -> "Great Experience ★ 4/5"
                            3 -> "Good, Can Be Better ★ 3/5"
                            else -> "Needs Improvement ★ $selectedStars/5"
                        },
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TealDark,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "What did you like most?",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    highlightTags.forEach { tag ->
                        val active = selectedHighlight == tag
                        Surface(
                            onClick = { selectedHighlight = tag },
                            shape = RoundedCornerShape(10.dp),
                            color = if (active) SoftSagePill else SoftCardSurface,
                            border = BorderStroke(1.dp, if (active) TealPrimary else WhisperBorder),
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
                                    text = tag,
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                    color = if (active) TealDark else TextPrimary
                                )
                                if (active) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = TealPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = reviewComment,
                        onValueChange = { reviewComment = it },
                        label = { Text("Your Feedback or Suggestion (Optional)") },
                        placeholder = { Text("Tell us how we can make your PG search even better...") },
                        singleLine = false,
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Text(
                                text = "Later",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                        }

                        Button(
                            onClick = {
                                onSubmitReview(
                                    selectedStars,
                                    selectedHighlight,
                                    reviewComment.ifBlank { "Rated $selectedStars/5 stars for $selectedHighlight" }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.4f)
                                .height(46.dp)
                                .testTag("submit_smart_review_btn")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Submit Review",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}


