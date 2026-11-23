package com.example.stayeg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.stayeg.data.remote.SupabaseService
import com.example.stayeg.ui.components.StayEgBottomNav
import com.example.stayeg.ui.components.StayEgSmartReviewPopupDialog
import com.example.stayeg.ui.components.StayEgTopBar
import com.example.stayeg.ui.navigation.Screen
import com.example.stayeg.ui.screens.*
import com.example.stayeg.ui.theme.StayEgTheme
import com.example.stayeg.ui.viewmodel.StayEgViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val viewModel: StayEgViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        SupabaseService.initAppContext(applicationContext)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel.restorePersistedSession()
        viewModel.ensureSupabaseLoaded()

        setContent {
            StayEgTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()
                val showAuthOverlay by viewModel.showAuthOnboardingOverlay.collectAsState()
                val showSmartReviewPopup by viewModel.shouldShowSmartReviewPopup.collectAsState()
                val currentUser by viewModel.currentUser.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }

                if (showSmartReviewPopup && !showAuthOverlay) {
                    StayEgSmartReviewPopupDialog(
                        userName = currentUser?.name ?: "Resident",
                        onDismiss = { viewModel.dismissSmartReviewPopup() },
                        onSubmitReview = { stars, highlightTag, reviewComment ->
                            viewModel.submitFeatureFeedback(
                                featureCategory = highlightTag,
                                rating = stars,
                                feedbackText = reviewComment
                            )
                            viewModel.trackUserActivity(
                                actionType = "APP_REVIEW_SUBMITTED",
                                screenName = currentScreen::class.simpleName ?: "App",
                                details = "Rated $stars/5 ($highlightTag): $reviewComment"
                            )
                            viewModel.dismissSmartReviewPopup()
                        }
                    )
                }

                LaunchedEffect(Unit) {
                    viewModel.toastMessage.collectLatest { message ->
                        snackbarHostState.showSnackbar(message)
                    }
                }

                BackHandler(enabled = true) {
                    val handled = viewModel.goBack()
                    if (!handled) {
                        finish()
                    }
                }

                val isMainTabScreen = !showAuthOverlay && (
                        currentScreen is Screen.Landing ||
                        currentScreen is Screen.Explore ||
                        currentScreen is Screen.Search ||
                        currentScreen is Screen.MyStay ||
                        currentScreen is Screen.Community ||
                        currentScreen is Screen.Profile
                )

                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        if (isMainTabScreen) {
                            StayEgTopBar(viewModel = viewModel)
                        }
                    },
                    bottomBar = {
                        if (isMainTabScreen) {
                            StayEgBottomNav(
                                viewModel = viewModel,
                                currentScreen = currentScreen
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = if (isMainTabScreen) innerPadding.calculateTopPadding() else 0.dp,
                                bottom = if (isMainTabScreen) innerPadding.calculateBottomPadding() else 0.dp
                            )
                    ) {
                        if (showAuthOverlay) {
                            WelcomeAuthOnboardingOverlay(viewModel = viewModel)
                        } else {
                            androidx.compose.animation.AnimatedContent(
                                targetState = currentScreen,
                                transitionSpec = {
                                    androidx.compose.animation.fadeIn(
                                        animationSpec = androidx.compose.animation.core.tween(210)
                                    ) togetherWith androidx.compose.animation.fadeOut(
                                        animationSpec = androidx.compose.animation.core.tween(160)
                                    )
                                },
                                label = "main_screen_transition"
                            ) { screen ->
                                when (screen) {
                                    is Screen.WelcomeAuth -> WelcomeAuthOnboardingOverlay(viewModel = viewModel)
                                    is Screen.Landing -> LandingScreen(viewModel = viewModel)
                                    is Screen.Explore -> ExploreScreen(viewModel = viewModel)
                                    is Screen.Search -> SearchScreen(viewModel = viewModel)
                                    is Screen.PGDetail -> PGDetailScreen(pgId = screen.pgId, viewModel = viewModel)
                                    is Screen.Booking -> BookingScreen(
                                        pgId = screen.pgId,
                                        bedId = screen.bedId,
                                        price = screen.price,
                                        roomCode = screen.roomCode,
                                        viewModel = viewModel
                                    )
                                    is Screen.OwnerJoinLead -> OwnerJoinLeadScreen(viewModel = viewModel)
                                    is Screen.FeedbackHub -> FeedbackHubScreen(viewModel = viewModel)
                                    is Screen.TenantHome -> TenantHomeScreen(viewModel = viewModel)
                                    is Screen.MyStay -> MyStayScreen(viewModel = viewModel)
                                    is Screen.Payments -> PaymentsScreen(viewModel = viewModel)
                                    is Screen.Complaints -> ComplaintsScreen(viewModel = viewModel)
                                    is Screen.OwnerDashboard -> OwnerDashboardScreen(viewModel = viewModel)
                                    is Screen.OwnerPGs -> OwnerPGsScreen(viewModel = viewModel)
                                    is Screen.OwnerRent -> OwnerRentScreen(viewModel = viewModel)
                                    is Screen.OwnerComplaints -> OwnerComplaintsScreen(viewModel = viewModel)
                                    is Screen.Community -> CommunityScreen(viewModel = viewModel)
                                    is Screen.Pricing -> PricingScreen(viewModel = viewModel)
                                    is Screen.Profile -> ProfileScreen(viewModel = viewModel)
                                    is Screen.About -> AboutScreen(viewModel = viewModel)
                                    is Screen.Contact -> ContactScreen(viewModel = viewModel)
                                    is Screen.SafeUse -> SafeUseScreen(viewModel = viewModel)
                                    is Screen.Terms -> TermsScreen(viewModel = viewModel)
                                    is Screen.SupabaseSync -> SupabaseSyncScreen(viewModel = viewModel)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
