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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import coil.compose.AsyncImage
import com.example.stayeg.data.remote.SupabaseService
import com.example.stayeg.ui.components.StayEgAnimatedBenefitStrip
import com.example.stayeg.ui.theme.*
import com.example.stayeg.ui.viewmodel.StayEgViewModel
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeAuthOnboardingOverlay(viewModel: StayEgViewModel) {

    var currentStep by remember { mutableIntStateOf(1) } // 1 = Auth, 2 = Profile & KYC
    // 1 = Login, 0 = Sign Up
    var authTab by remember { mutableIntStateOf(1) }

    // Tab 0: Sign Up state
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+91 ") }
    var email by remember { mutableStateOf("") }
    var signupLocationArea by remember { mutableStateOf("Koramangala") }
    var otpTargetDestination by remember { mutableStateOf("") }
    var issuedSignupOtp by remember { mutableStateOf<String?>(null) }
    var otpCode by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var signupError by remember { mutableStateOf<String?>(null) }

    // Tab 1: Login (Password or Google) state
    var loginIdentifier by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf<String?>(null) }

    // Tab 1 & 2: Google Real OTP Verification state
    var googleName by remember { mutableStateOf("") }
    var googleEmail by remember { mutableStateOf("") }
    var issuedGoogleOtp by remember { mutableStateOf<String?>(null) }
    var googleOtpInput by remember { mutableStateOf("") }
    var googlePassword by remember { mutableStateOf("") }
    var googleError by remember { mutableStateOf<String?>(null) }

    // Tab 3: Forgot Password state
    var resetEmail by remember { mutableStateOf("") }
    var issuedResetOtp by remember { mutableStateOf<String?>(null) }
    var resetEmailOtp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var resetError by remember { mutableStateOf<String?>(null) }

    // Password Visibility Toggles
    var showLoginPassword by remember { mutableStateOf(false) }
    var showSignupPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var showGooglePassword by remember { mutableStateOf(false) }
    var showResetPassword by remember { mutableStateOf(false) }

    // Step 2: Complete Customizable Profile & Optional Manual KYC state
    var profilePhotoUri by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var occupation by remember { mutableStateOf("Software Engineer") }
    var organizationName by remember { mutableStateOf("") }
    var workMode by remember { mutableStateOf("Hybrid") }
    var preferredArea by remember { mutableStateOf("Koramangala 4th Block") }
    var foodPreference by remember { mutableStateOf("Veg & Non-Veg") }
    var sleepSchedule by remember { mutableStateOf("Flexible") }
    var bio by remember { mutableStateOf("") }
    var aadhaarNumber by remember { mutableStateOf("") }
    var panNumber by remember { mutableStateOf("") }

    val profilePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            profilePhotoUri = uri.toString()
            viewModel.updateProfilePhoto(profilePhotoUri)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftGalleryBg)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(bottom = 36.dp)
    ) {
        // 1. Branded Header — Poppins Bold Gradient StayEg Title in Deep Forest Teal Hero
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(TealDeep, TealDark, TealPrimary)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 22.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "StayEg",
                                style = androidx.compose.ui.text.TextStyle(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 30.sp,
                                    letterSpacing = (-0.8).sp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            MintSoft,
                                            CreamLight,
                                            Color(0xFFFDE68A)
                                        )
                                    )
                                )
                            )
                            Text(
                                text = "ZERO-BROKERAGE PG BED BOOKING",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MintSoft,
                                letterSpacing = 0.8.sp
                            )
                        }

                        Surface(
                            onClick = { viewModel.dismissAuthOnboarding() },
                            shape = RoundedCornerShape(999.dp),
                            color = SoftGoldPill,
                            modifier = Modifier.testTag("skip_auth_explore_button")
                        ) {
                            Text(
                                "Explore Beds →",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = GoldDark,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (currentStep == 1)
                            "Welcome to StayEg"
                        else
                            "Complete Your Resident Profile",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = PureWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (currentStep == 1)
                            "Verify your Gmail or Mobile with OTP, create your password, and log in securely anytime."
                        else
                            "Add your preferred Bengaluru locality and profile details for seamless check-in.",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = MintSoft.copy(alpha = 0.9f),
                        lineHeight = 17.sp
                    )
                }
            }
        }

        if (currentStep == 1) {
            item {
                val context = LocalContext.current
                val coroutineScope = rememberCoroutineScope()
                var showGoogleSheet by remember { mutableStateOf(false) }
                var showForgotPasswordView by remember { mutableStateOf(false) }

                fun triggerNativeGoogleSignIn() {
                    coroutineScope.launch {
                        try {
                            val credentialManager = CredentialManager.create(context)
                            val signInOption = GetSignInWithGoogleOption.Builder(
                                SupabaseService.GOOGLE_WEB_CLIENT_ID
                            ).build()
                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(signInOption)
                                .build()
                            val result = credentialManager.getCredential(
                                request = request,
                                context = context
                            )
                            val credential = result.credential
                            if (credential is CustomCredential &&
                                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                            ) {
                                val googleIdTokenCred = GoogleIdTokenCredential.createFrom(credential.data)
                                val idToken = googleIdTokenCred.idToken
                                val emailAddr = googleIdTokenCred.id
                                val displayName = googleIdTokenCred.displayName ?: emailAddr.substringBefore("@")
                                viewModel.signInWithGoogle(
                                    name = displayName,
                                    email = emailAddr,
                                    googleIdToken = idToken
                                )
                            } else {
                                showGoogleSheet = true
                            }
                        } catch (_: Exception) {
                            // When running on an emulator without a signed-in Google Play account, open the Gmail verification sheet
                            showGoogleSheet = true
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                ) {
                    // Standard 2-Option Auth Toggle: Login | Sign Up
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SoftSagePill)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            1 to "Login",
                            0 to "Create Account"
                        ).forEach { (idx, title) ->
                            val isSelected = authTab == idx
                            val tabBg by animateColorAsState(
                                targetValue = if (isSelected) TealDark else Color.Transparent,
                                animationSpec = tween(220),
                                label = "auth_tab_bg"
                            )
                            Surface(
                                onClick = {
                                    if (idx == 0 && loginIdentifier.isNotBlank()) {
                                        if (loginIdentifier.contains("@") && email.isBlank()) {
                                            email = loginIdentifier.trim()
                                        } else if (loginIdentifier.any { it.isDigit() } && phone.trim() == "+91") {
                                            phone = loginIdentifier.trim()
                                        }
                                    } else if (idx == 1 && loginIdentifier.isBlank()) {
                                        if (email.isNotBlank()) {
                                            loginIdentifier = email.trim()
                                        } else if (phone.count { it.isDigit() } >= 10) {
                                            loginIdentifier = phone.trim()
                                        }
                                    }
                                    authTab = idx
                                    showGoogleSheet = false
                                    showForgotPasswordView = false
                                    signupError = null
                                    loginError = null
                                    googleError = null
                                    resetError = null
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = tabBg,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag(if (idx == 1) "auth_tab_login" else "auth_tab_signup")
                            ) {
                                Text(
                                    text = title,
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) PureWhite else TealDark,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    when {
                        // ─── FORGOT PASSWORD VIEW (Gmail OTP Verification -> Reset Password) ───
                        showForgotPasswordView -> {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = PureWhite),
                                border = BorderStroke(1.dp, WhisperBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Forgot Password • Gmail OTP",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "← Back to Login",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = TealPrimary,
                                            modifier = Modifier.clickable {
                                                showForgotPasswordView = false
                                                resetError = null
                                            }
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Enter your registered Gmail / Email address, verify the 6-digit OTP code, and set your new password.",
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = resetEmail,
                                            onValueChange = {
                                                resetEmail = it
                                                resetError = null
                                            },
                                            label = { Text("Registered Gmail / Email *") },
                                            placeholder = { Text("yourname@gmail.com") },
                                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TealPrimary) },
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1f)
                                        )
                                        Button(
                                            onClick = {
                                                val cleanMail = resetEmail.trim()
                                                if (cleanMail.isEmpty() || !cleanMail.contains("@")) {
                                                    resetError = "Please enter your registered Gmail / Email address first."
                                                    return@Button
                                                }
                                                val code = viewModel.requestVerificationOtp(cleanMail)
                                                issuedResetOtp = code
                                                resetEmailOtp = code
                                                resetError = null
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = TealDark),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.height(54.dp)
                                        ) {
                                            Text(
                                                text = if (issuedResetOtp != null) "Resend" else "Send Gmail OTP",
                                                fontFamily = PoppinsFontFamily,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MintSoft
                                            )
                                        }
                                    }

                                    if (issuedResetOtp != null) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = SoftGoldPill,
                                            border = BorderStroke(1.2.dp, SoftGoldAccent),
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
                                                        text = "GMAIL OTP DISPATCHED TO ${resetEmail.trim().uppercase()}",
                                                        fontFamily = PoppinsFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 9.sp,
                                                        color = GoldDark
                                                    )
                                                    Text(
                                                        text = issuedResetOtp!!,
                                                        fontFamily = PoppinsFontFamily,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 21.sp,
                                                        color = TealDark,
                                                        letterSpacing = 3.sp
                                                    )
                                                }
                                                Surface(
                                                    onClick = { resetEmailOtp = issuedResetOtp!! },
                                                    shape = RoundedCornerShape(999.dp),
                                                    color = TealDark
                                                ) {
                                                    Text(
                                                        text = if (resetEmailOtp == issuedResetOtp) "Verified ✓" else "Auto-Fill",
                                                        fontFamily = PoppinsFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp,
                                                        color = PureWhite,
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = resetEmailOtp,
                                        onValueChange = {
                                            resetEmailOtp = it.filter { ch -> ch.isDigit() }.take(6)
                                            resetError = null
                                        },
                                        label = { Text("6-Digit Gmail OTP *") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = newPassword,
                                        onValueChange = {
                                            newPassword = it
                                            resetError = null
                                        },
                                        label = { Text("Create New Password *") },
                                        placeholder = { Text("Minimum 4 characters") },
                                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TealPrimary) },
                                        trailingIcon = {
                                            IconButton(onClick = { showResetPassword = !showResetPassword }) {
                                                Icon(
                                                    imageVector = if (showResetPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                    contentDescription = if (showResetPassword) "Hide password" else "Show password",
                                                    tint = TextSecondary
                                                )
                                            }
                                        },
                                        visualTransformation = if (showResetPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("reset_new_password_input")
                                    )

                                    if (resetError != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = ErrorRedLight,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(10.dp)
                                            ) {
                                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = resetError!!,
                                                    fontFamily = PoppinsFontFamily,
                                                    fontSize = 11.sp,
                                                    color = ErrorRed,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Button(
                                        onClick = {
                                            val cleanMail = resetEmail.trim()
                                            if (cleanMail.isBlank() || !cleanMail.contains("@")) {
                                                resetError = "Please enter your registered Gmail / Email address."
                                                return@Button
                                            }
                                            if (resetEmailOtp.isBlank() || !viewModel.isValidOtp(resetEmailOtp, cleanMail)) {
                                                resetError = "Invalid OTP. Please tap 'Send Gmail OTP' and enter the exact 6-digit code sent to $cleanMail."
                                                return@Button
                                            }
                                            if (newPassword.length < 4) {
                                                resetError = "New password must be at least 4 characters."
                                                return@Button
                                            }
                                            viewModel.resetPasswordWithEmailVerification(
                                                email = cleanMail,
                                                emailOtp = resetEmailOtp,
                                                newPassword = newPassword,
                                                onSuccess = {
                                                    loginIdentifier = cleanMail
                                                    loginPassword = newPassword
                                                    resetError = null
                                                    showForgotPasswordView = false
                                                    authTab = 1
                                                },
                                                onError = { err -> resetError = err }
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                    ) {
                                        Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Verify Gmail OTP & Update Password",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // ─── GOOGLE VERIFICATION STEP (Opened when user taps 'Continue with Google') ───
                        showGoogleSheet -> {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = PureWhite),
                                border = BorderStroke(1.dp, WhisperBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Sign In with Google",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "← Back",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = TealPrimary,
                                            modifier = Modifier.clickable {
                                                showGoogleSheet = false
                                                googleError = null
                                            }
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Verify your Google / Gmail account with a 6-digit OTP to sign in securely.",
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = googleName,
                                        onValueChange = { googleName = it },
                                        label = { Text("Your Full Name *") },
                                        placeholder = { Text("e.g., Ananya Rao") },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TealPrimary) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = googleEmail,
                                            onValueChange = {
                                                googleEmail = it
                                                googleError = null
                                            },
                                            label = { Text("Google / Gmail Address *") },
                                            placeholder = { Text("yourname@gmail.com") },
                                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TealPrimary) },
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1f)
                                        )

                                        Button(
                                            onClick = {
                                                val cleanMail = googleEmail.trim()
                                                if (cleanMail.isEmpty() || !cleanMail.contains("@")) {
                                                    googleError = "Please enter a valid Google / Gmail address first."
                                                    return@Button
                                                }
                                                val code = viewModel.requestVerificationOtp(cleanMail)
                                                issuedGoogleOtp = code
                                                googleOtpInput = code
                                                googleError = null
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = TealDark),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.height(54.dp)
                                        ) {
                                            Text(
                                                text = if (issuedGoogleOtp != null) "Resend" else "Send OTP",
                                                fontFamily = PoppinsFontFamily,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MintSoft
                                            )
                                        }
                                    }

                                    if (issuedGoogleOtp != null) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = SoftGoldPill,
                                            border = BorderStroke(1.2.dp, SoftGoldAccent),
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
                                                        text = "GMAIL OTP FOR ${googleEmail.trim()}",
                                                        fontFamily = PoppinsFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 9.sp,
                                                        color = GoldDark
                                                    )
                                                    Text(
                                                        text = issuedGoogleOtp!!,
                                                        fontFamily = PoppinsFontFamily,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 21.sp,
                                                        color = TealDark,
                                                        letterSpacing = 3.sp
                                                    )
                                                }

                                                Surface(
                                                    onClick = {
                                                        googleOtpInput = issuedGoogleOtp!!
                                                        googleError = null
                                                    },
                                                    shape = RoundedCornerShape(999.dp),
                                                    color = TealDark
                                                ) {
                                                    Text(
                                                        text = if (googleOtpInput == issuedGoogleOtp) "Filled ✓" else "Auto-Fill",
                                                        fontFamily = PoppinsFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp,
                                                        color = PureWhite,
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = googleOtpInput,
                                        onValueChange = {
                                            googleOtpInput = it.filter { ch -> ch.isDigit() }.take(6)
                                            googleError = null
                                        },
                                        label = { Text("6-Digit Verification OTP *") },
                                        placeholder = { Text("Enter 6-digit code") },
                                        leadingIcon = { Icon(Icons.Default.MarkEmailRead, contentDescription = null, tint = TealPrimary) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = googlePassword,
                                        onValueChange = { googlePassword = it },
                                        label = { Text("Create Password *") },
                                        placeholder = { Text("Minimum 4 characters for future login") },
                                        leadingIcon = { Icon(Icons.Default.Password, contentDescription = null, tint = TealPrimary) },
                                        trailingIcon = {
                                            IconButton(onClick = { showGooglePassword = !showGooglePassword }) {
                                                Icon(
                                                    imageVector = if (showGooglePassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                    contentDescription = if (showGooglePassword) "Hide password" else "Show password",
                                                    tint = TextSecondary
                                                )
                                            }
                                        },
                                        visualTransformation = if (showGooglePassword) VisualTransformation.None else PasswordVisualTransformation(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("google_password_input")
                                    )

                                    if (googleError != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = ErrorRedLight,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(10.dp)
                                            ) {
                                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = googleError!!,
                                                    fontFamily = PoppinsFontFamily,
                                                    fontSize = 11.sp,
                                                    color = ErrorRed,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Button(
                                        onClick = {
                                            val cleanMail = googleEmail.trim()
                                            if (cleanMail.isBlank() || !cleanMail.contains("@")) {
                                                googleError = "Please enter your Google / Gmail address and tap 'Send OTP'."
                                                return@Button
                                            }
                                            if (googleOtpInput.isBlank() || !viewModel.isValidOtp(googleOtpInput, cleanMail)) {
                                                googleError = "Invalid OTP. Please tap 'Send OTP' and enter the exact 6-digit verification code."
                                                return@Button
                                            }
                                            viewModel.verifyGoogleWithOtp(
                                                name = googleName,
                                                email = cleanMail,
                                                otp = googleOtpInput,
                                                password = googlePassword,
                                                onSuccess = {
                                                    googleError = null
                                                    showGoogleSheet = false
                                                    currentStep = 2
                                                },
                                                onError = { err -> googleError = err }
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = TealDeep),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("google_sign_in_btn")
                                    ) {
                                        Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Verify Gmail & Continue",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // ─── OPTION 1: LOGIN (Strict Registered Account + Password Verification) ───
                        authTab == 1 -> {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = PureWhite),
                                border = BorderStroke(1.dp, WhisperBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Sign In to Your Account",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp,
                                            color = TextPrimary
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(999.dp),
                                            color = SoftSagePill
                                        ) {
                                            Text(
                                                text = "Secure Login",
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                color = TealDark,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Enter your registered Gmail / Email or 10-digit Mobile Number and your account password.",
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    OutlinedTextField(
                                        value = loginIdentifier,
                                        onValueChange = {
                                            loginIdentifier = it
                                            loginError = null
                                        },
                                        label = { Text("Registered Gmail or Mobile Number *") },
                                        placeholder = { Text("you@gmail.com or +91 98801 23456") },
                                        leadingIcon = { Icon(Icons.Default.PersonOutline, contentDescription = null, tint = TealPrimary) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("login_identifier_input")
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = loginPassword,
                                        onValueChange = {
                                            loginPassword = it
                                            loginError = null
                                        },
                                        label = { Text("Password *") },
                                        placeholder = { Text("Enter your registered password") },
                                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TealPrimary) },
                                        trailingIcon = {
                                            IconButton(
                                                onClick = { showLoginPassword = !showLoginPassword },
                                                modifier = Modifier.testTag("toggle_login_password_visibility")
                                            ) {
                                                Icon(
                                                    imageVector = if (showLoginPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                    contentDescription = if (showLoginPassword) "Hide password" else "Show password",
                                                    tint = TextSecondary
                                                )
                                            }
                                        },
                                        visualTransformation = if (showLoginPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("login_password_input")
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "New user? Tap Sign Up first",
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 11.sp,
                                            color = TextSecondary,
                                            modifier = Modifier.clickable { authTab = 0 }
                                        )
                                        Text(
                                            text = "Forgot Password? (Gmail OTP)",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = TealPrimary,
                                            modifier = Modifier.clickable {
                                                resetEmail = if (loginIdentifier.contains("@")) loginIdentifier else email
                                                showForgotPasswordView = true
                                            }
                                        )
                                    }

                                    if (loginError != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = ErrorRedLight,
                                            border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.35f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = loginError!!,
                                                        fontFamily = PoppinsFontFamily,
                                                        fontSize = 11.sp,
                                                        color = ErrorRed,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Surface(
                                                        onClick = {
                                                            if (loginIdentifier.contains("@")) {
                                                                email = loginIdentifier.trim()
                                                            } else if (loginIdentifier.any { it.isDigit() }) {
                                                                phone = loginIdentifier.trim()
                                                            }
                                                            loginError = null
                                                            authTab = 0
                                                        },
                                                        shape = RoundedCornerShape(999.dp),
                                                        color = TealDark
                                                    ) {
                                                        Text(
                                                            text = "Create Account (Sign Up) →",
                                                            fontFamily = PoppinsFontFamily,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = PureWhite,
                                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                                        )
                                                    }
                                                    Surface(
                                                        onClick = {
                                                            resetEmail = if (loginIdentifier.contains("@")) loginIdentifier else email
                                                            loginError = null
                                                            showForgotPasswordView = true
                                                        },
                                                        shape = RoundedCornerShape(999.dp),
                                                        color = SoftGoldPill
                                                    ) {
                                                        Text(
                                                            text = "Reset via Gmail OTP",
                                                            fontFamily = PoppinsFontFamily,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = GoldDark,
                                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Button(
                                        onClick = {
                                            viewModel.loginWithPassword(
                                                identifier = loginIdentifier,
                                                password = loginPassword,
                                                onSuccess = {
                                                    loginError = null
                                                    viewModel.dismissAuthOnboarding()
                                                },
                                                onError = { err -> loginError = err }
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("login_password_btn")
                                    ) {
                                        Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Login with Credentials",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        HorizontalDivider(color = WhisperBorder, modifier = Modifier.weight(1f))
                                        Text(
                                            text = "  OR  ",
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 11.sp,
                                            color = TextSecondary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        HorizontalDivider(color = WhisperBorder, modifier = Modifier.weight(1f))
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    OutlinedButton(
                                        onClick = { triggerNativeGoogleSignIn() },
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.2.dp, TealDark),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("continue_with_google_btn")
                                    ) {
                                        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = TealDark, modifier = Modifier.size(19.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Continue with Google",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = TealDark
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "Don't have an account? ",
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                        Text(
                                            text = "Sign Up with Gmail / Mobile OTP",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = TealPrimary,
                                            modifier = Modifier.clickable { authTab = 0 }
                                        )
                                    }
                                }
                            }
                        }

                        // ─── OPTION 2: SIGN UP (Gmail OTP Auth + Mobile Number + Location + Password) ───
                        else -> {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = PureWhite),
                                border = BorderStroke(1.dp, WhisperBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Create Your StayEg Account",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Verify your Gmail address or Mobile Number with a 6-digit OTP and create your password to sign in anytime.",
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = fullName,
                                        onValueChange = {
                                            fullName = it
                                            signupError = null
                                        },
                                        label = { Text("Full Name *") },
                                        placeholder = { Text("e.g., Karthik Rao") },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TealPrimary) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("signup_name_input")
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // 1. GMAIL / EMAIL ADDRESS + SEND GMAIL OTP BUTTON
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = email,
                                            onValueChange = {
                                                email = it
                                                signupError = null
                                            },
                                            label = { Text("Gmail / Email Address *") },
                                            placeholder = { Text("yourname@gmail.com") },
                                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TealPrimary) },
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("signup_email_input")
                                        )

                                        Button(
                                            onClick = {
                                                val cleanEmail = email.trim()
                                                if (cleanEmail.isEmpty() || !cleanEmail.contains("@")) {
                                                    signupError = "Please enter a valid Gmail / Email address to receive your 6-digit OTP."
                                                    return@Button
                                                }
                                                val code = viewModel.requestVerificationOtp(cleanEmail)
                                                otpTargetDestination = cleanEmail
                                                issuedSignupOtp = code
                                                otpCode = code
                                                signupError = null
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = TealDark),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .height(54.dp)
                                                .testTag("get_gmail_signup_otp_btn")
                                        ) {
                                            Text(
                                                text = if (issuedSignupOtp != null && otpTargetDestination == email.trim()) "Resend" else "Gmail OTP",
                                                fontFamily = PoppinsFontFamily,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MintSoft
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // 2. MOBILE NUMBER + GET MOBILE OTP BUTTON
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = phone,
                                            onValueChange = {
                                                phone = it
                                                signupError = null
                                            },
                                            label = { Text("Mobile Number *") },
                                            placeholder = { Text("+91 98801 23456") },
                                            leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = TealPrimary) },
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("signup_phone_input")
                                        )

                                        Button(
                                            onClick = {
                                                val cleanPhone = phone.trim()
                                                val digitCount = cleanPhone.count { it.isDigit() }
                                                if (digitCount < 10) {
                                                    signupError = "Please enter your 10-digit mobile number to receive your OTP."
                                                    return@Button
                                                }
                                                val code = viewModel.requestVerificationOtp(cleanPhone)
                                                otpTargetDestination = cleanPhone
                                                issuedSignupOtp = code
                                                otpCode = code
                                                signupError = null
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .height(54.dp)
                                                .testTag("get_signup_otp_btn")
                                        ) {
                                            Text(
                                                text = if (issuedSignupOtp != null && otpTargetDestination == phone.trim()) "Resend" else "Mobile OTP",
                                                fontFamily = PoppinsFontFamily,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PureWhite
                                            )
                                        }
                                    }

                                    // Show verification code card once user requests Gmail or Mobile OTP
                                    if (issuedSignupOtp != null) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = SoftGoldPill,
                                            border = BorderStroke(1.2.dp, SoftGoldAccent),
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
                                                        text = "LIVE OTP DISPATCHED TO ${otpTargetDestination.ifBlank { email.ifBlank { phone } }}",
                                                        fontFamily = PoppinsFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 9.sp,
                                                        color = GoldDark
                                                    )
                                                    Text(
                                                        text = issuedSignupOtp!!,
                                                        fontFamily = PoppinsFontFamily,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 22.sp,
                                                        color = TealDark,
                                                        letterSpacing = 3.sp
                                                    )
                                                }

                                                Surface(
                                                    onClick = {
                                                        otpCode = issuedSignupOtp!!
                                                        signupError = null
                                                    },
                                                    shape = RoundedCornerShape(999.dp),
                                                    color = TealDark
                                                ) {
                                                    Text(
                                                        text = if (otpCode == issuedSignupOtp) "Verified ✓" else "Use OTP",
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

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = otpCode,
                                        onValueChange = {
                                            otpCode = it.filter { ch -> ch.isDigit() }.take(6)
                                            signupError = null
                                        },
                                        label = { Text("6-Digit Gmail / Mobile OTP *") },
                                        placeholder = { Text("Tap 'Gmail OTP' or 'Mobile OTP' above") },
                                        leadingIcon = { Icon(Icons.Default.LockClock, contentDescription = null, tint = TealPrimary) },
                                        trailingIcon = {
                                            if (otpCode.length == 6 && viewModel.isValidOtp(otpCode, otpTargetDestination)) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = "Valid OTP", tint = TealPrimary)
                                            }
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("signup_otp_input")
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // 3. PREFERRED BENGALURU LOCATION (Automatically stored in database on signup)
                                    Text(
                                        text = "Preferred Bengaluru Locality",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        val popularHubs = listOf(
                                            "Koramangala",
                                            "HSR Layout",
                                            "Indiranagar",
                                            "Whitefield",
                                            "Electronic City",
                                            "Bellandur",
                                            "BTM Layout",
                                            "Marathahalli"
                                        )
                                        items(popularHubs) { hub ->
                                            val isSelected = signupLocationArea.equals(hub, ignoreCase = true)
                                            Surface(
                                                onClick = {
                                                    signupLocationArea = hub
                                                    preferredArea = hub
                                                },
                                                shape = RoundedCornerShape(999.dp),
                                                color = if (isSelected) TealDark else SoftSagePill
                                            ) {
                                                Text(
                                                    text = hub,
                                                    fontFamily = PoppinsFontFamily,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) PureWhite else TealDark,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = password,
                                        onValueChange = {
                                            password = it
                                            signupError = null
                                        },
                                        label = { Text("Create Password *") },
                                        placeholder = { Text("Minimum 4 characters") },
                                        leadingIcon = { Icon(Icons.Default.Password, contentDescription = null, tint = TealPrimary) },
                                        trailingIcon = {
                                            IconButton(
                                                onClick = { showSignupPassword = !showSignupPassword },
                                                modifier = Modifier.testTag("toggle_signup_password_visibility")
                                            ) {
                                                Icon(
                                                    imageVector = if (showSignupPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                    contentDescription = if (showSignupPassword) "Hide password" else "Show password",
                                                    tint = TextSecondary
                                                )
                                            }
                                        },
                                        visualTransformation = if (showSignupPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("signup_password_input")
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = confirmPassword,
                                        onValueChange = {
                                            confirmPassword = it
                                            signupError = null
                                        },
                                        label = { Text("Confirm Password *") },
                                        placeholder = { Text("Re-enter password") },
                                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TealPrimary) },
                                        trailingIcon = {
                                            IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                                Icon(
                                                    imageVector = if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                    contentDescription = if (showConfirmPassword) "Hide password" else "Show password",
                                                    tint = TextSecondary
                                                )
                                            }
                                        },
                                        visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("signup_confirm_password_input")
                                    )

                                    if (signupError != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = ErrorRedLight,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(10.dp)
                                            ) {
                                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = signupError!!,
                                                    fontFamily = PoppinsFontFamily,
                                                    fontSize = 11.sp,
                                                    color = ErrorRed,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Button(
                                        onClick = {
                                            if (fullName.trim().length < 2) {
                                                signupError = "Please enter your full name."
                                                return@Button
                                            }
                                            if (email.trim().isEmpty() || !email.contains("@")) {
                                                signupError = "Please enter a valid Gmail / Email address."
                                                return@Button
                                            }
                                            if (phone.count { it.isDigit() } < 10) {
                                                signupError = "Please enter a valid 10-digit mobile number."
                                                return@Button
                                            }
                                            if (otpCode.isBlank()) {
                                                signupError = "Please tap 'Gmail OTP' or 'Mobile OTP' and enter the 6-digit verification code."
                                                return@Button
                                            }
                                            if (!viewModel.isValidOtp(otpCode, otpTargetDestination)) {
                                                signupError = "Invalid OTP ($otpCode). Please enter the exact 6-digit code sent to ${otpTargetDestination.ifBlank { "your Gmail / Mobile" }}."
                                                return@Button
                                            }
                                            if (password.length < 4) {
                                                signupError = "Please create a password (at least 4 characters) to secure your account."
                                                return@Button
                                            }
                                            if (confirmPassword.isNotEmpty() && confirmPassword != password) {
                                                signupError = "Passwords do not match. Please check your password."
                                                return@Button
                                            }
                                            viewModel.signUpOrLoginWithPhoneOtp(
                                                name = fullName.trim(),
                                                phone = phone.trim(),
                                                email = email.trim(),
                                                otp = otpCode,
                                                password = password,
                                                locality = signupLocationArea,
                                                onSuccess = {
                                                    signupError = null
                                                    preferredArea = signupLocationArea
                                                    viewModel.trackUserActivity(
                                                        actionType = "USER_SIGNUP_COMPLETED",
                                                        screenName = "AuthSignUp",
                                                        details = "Registered ${fullName.trim()} (${email.trim()}, ${phone.trim()}) in $signupLocationArea"
                                                    )
                                                    currentStep = 2
                                                },
                                                onError = { err -> signupError = err }
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("verify_otp_continue_btn")
                                    ) {
                                        Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Verify OTP & Create Account",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        HorizontalDivider(color = WhisperBorder, modifier = Modifier.weight(1f))
                                        Text(
                                            text = "  OR  ",
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 11.sp,
                                            color = TextSecondary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        HorizontalDivider(color = WhisperBorder, modifier = Modifier.weight(1f))
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedButton(
                                        onClick = { showGoogleSheet = true },
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.2.dp, TealDark),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                    ) {
                                        Icon(Icons.Default.MarkEmailRead, contentDescription = null, tint = TealDark, modifier = Modifier.size(19.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Quick Verify with Gmail OTP",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = TealDark
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "Already have an account? ",
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                        Text(
                                            text = "Login with Password",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = TealPrimary,
                                            modifier = Modifier.clickable { authTab = 1 }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // STEP 2: Complete Customizable Profile Setup (Photo Upload, Location, Occupation & Optional Manual KYC)
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        border = BorderStroke(1.dp, WhisperBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "2. Customize Your Tenant Profile",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Upload your profile photo, add your Bengaluru location and what you do. Digital KYC can be completed manually anytime.",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Profile Photo Upload Row (Zero-Permission Android Photo Picker)
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = SoftCardSurface,
                                border = BorderStroke(1.dp, WhisperBorder),
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
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(CircleShape)
                                                .background(SoftSagePill),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (profilePhotoUri.isNotBlank()) {
                                                AsyncImage(
                                                    model = profilePhotoUri,
                                                    contentDescription = "Profile Photo",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.AddAPhoto,
                                                    contentDescription = "Upload Photo",
                                                    tint = TealPrimary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = if (profilePhotoUri.isNotBlank()) "Profile Photo Selected ✓" else "Upload Profile Picture",
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "Pick from device gallery",
                                                fontFamily = PoppinsFontFamily,
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            profilePhotoPicker.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = TealDark),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = if (profilePhotoUri.isNotBlank()) "Change" else "Upload",
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PureWhite
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // What are you doing? (Quick Role Chips + Custom Input)
                            Text(
                                text = "What are you doing in Bengaluru?",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(
                                    listOf(
                                        "Software Engineer",
                                        "College Student",
                                        "Product Designer",
                                        "Data Scientist",
                                        "Startup Founder",
                                        "CA / Finance"
                                    )
                                ) { roleOption ->
                                    val selected = occupation.equals(roleOption, ignoreCase = true)
                                    Surface(
                                        onClick = { occupation = roleOption },
                                        shape = RoundedCornerShape(999.dp),
                                        color = if (selected) TealDark else SoftSagePill
                                    ) {
                                        Text(
                                            text = roleOption,
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selected) PureWhite else TealDark,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = occupation,
                                onValueChange = { occupation = it },
                                label = { Text("Your Role / Profession / Course *") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = organizationName,
                                onValueChange = { organizationName = it },
                                label = { Text("Company or College Name") },
                                placeholder = { Text("e.g., Wipro, Flipkart, Christ University") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = preferredArea,
                                onValueChange = { preferredArea = it },
                                label = { Text("Current / Preferred Bengaluru Location *") },
                                placeholder = { Text("e.g., Koramangala 4th Block, HSR Sector 2") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = bio,
                                onValueChange = { bio = it },
                                label = { Text("About You & Roommate Preferences") },
                                placeholder = { Text("e.g., Non-smoker, hybrid tech worker, looking for clean twin sharing") },
                                minLines = 2,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    viewModel.saveCustomizedProfile(
                                        name = fullName.ifBlank { googleName.ifBlank { "Bengaluru Tenant" } },
                                        phone = phone.ifBlank { "+91 91234 56789" },
                                        email = email.ifBlank { googleEmail.ifBlank { "tenant@stayeg.in" } },
                                        gender = gender,
                                        city = "Bengaluru",
                                        currentArea = preferredArea,
                                        occupation = occupation,
                                        organizationName = organizationName,
                                        workMode = workMode,
                                        foodPreference = foodPreference,
                                        sleepSchedule = sleepSchedule,
                                        languagesSpoken = "English, Hindi, Kannada",
                                        emergencyContactName = "",
                                        emergencyContactPhone = "",
                                        bio = bio,
                                        avatarUrl = profilePhotoUri
                                    )
                                    if (aadhaarNumber.trim().length >= 8 && panNumber.trim().length >= 6) {
                                        viewModel.updateKyc(aadhaarNumber, panNumber)
                                    }
                                    viewModel.dismissAuthOnboarding()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("complete_kyc_explore_btn")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Save Custom Profile & Explore PGs",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            TextButton(
                                onClick = { viewModel.dismissAuthOnboarding() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Skip for Now", fontFamily = PoppinsFontFamily, color = TealPrimary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerJoinLeadScreen(viewModel: StayEgViewModel) {
    val ownerLeads by viewModel.ownerActivationLeads.collectAsState()
    var ownerName by remember { mutableStateOf("") }
    var pgName by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("Koramangala") }
    var phone by remember { mutableStateOf("+91 ") }
    var email by remember { mutableStateOf("") }
    var totalBeds by remember { mutableStateOf("40") }
    var claimCode by remember { mutableStateOf("") }

    val popularBlrAreas = listOf(
        "Koramangala",
        "HSR Layout",
        "Whitefield",
        "Electronic City",
        "BTM Layout",
        "Indiranagar",
        "Nagavara",
        "Marathahalli",
        "Banashankari"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("List Your PG on StayEg", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
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
            contentPadding = PaddingValues(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Partner Offer Banner
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = GoldLight),
                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.55f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GoldAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "STAYEG PROPERTY PARTNER PROGRAM",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GoldDark
                                )
                                Text(
                                    text = "Verified Partner Listing & Bed Management",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Submit your property details below to verify your PG, showcase room & dining photos, and receive direct tenant enquiries.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Owner Enquiry Submission Form
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Property & Owner Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Our onboarding team will contact you to complete property verification.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = ownerName,
                            onValueChange = { ownerName = it },
                            label = { Text("Owner / Manager Name *") },
                            placeholder = { Text("e.g., Rajesh Reddy") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("owner_lead_name")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = pgName,
                            onValueChange = { pgName = it },
                            label = { Text("PG Property Name *") },
                            placeholder = { Text("e.g., Sri Lakshmi Residency PG") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("owner_lead_pg_name")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Select Locality:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(popularBlrAreas) { loc ->
                                val isSel = area.equals(loc, ignoreCase = true)
                                FilterChip(
                                    selected = isSel,
                                    onClick = { area = loc },
                                    label = { Text(loc, fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Mobile / WhatsApp Number *") },
                            placeholder = { Text("+91 98801 45210") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("owner_lead_phone")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            placeholder = { Text("owner@example.com") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = totalBeds,
                                onValueChange = { totalBeds = it },
                                label = { Text("Total Beds") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = claimCode,
                                onValueChange = { claimCode = it.uppercase() },
                                label = { Text("Listing Code (Optional)") },
                                placeholder = { Text("STG-...") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.3f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (ownerName.isBlank() || pgName.isBlank() || phone.length < 6) {
                                    viewModel.showToast("Please enter Owner Name, PG Name, and Mobile Number")
                                    return@Button
                                }
                                viewModel.submitOwnerActivationLead(
                                    ownerName = ownerName,
                                    pgName = pgName,
                                    area = area,
                                    phone = phone,
                                    email = email.ifBlank { "owner@stayeg.in" },
                                    totalBeds = totalBeds.toIntOrNull() ?: 35,
                                    claimCode = claimCode
                                )
                                ownerName = ""
                                pgName = ""
                                claimCode = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("submit_owner_lead_btn")
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Submit PG Enquiry", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (ownerLeads.isNotEmpty()) {
                item {
                    Text(
                        text = "Your Submitted Enquiries",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                }
                items(ownerLeads) { lead ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SageSurface),
                        border = BorderStroke(1.dp, SageSoft),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(lead.pgName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                Text("Received", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TealDeep)
                            }
                            Text("Owner: ${lead.ownerName} • ${lead.area} • ${lead.totalBeds} Beds", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackHubScreen(viewModel: StayEgViewModel) {
    val feedbacks by viewModel.featureFeedbacks.collectAsState()
    var selectedCategory by remember { mutableStateOf("Online Booking & Rent Payments") }
    var rating by remember { mutableIntStateOf(5) }
    var feedbackText by remember { mutableStateOf("") }

    val categories = listOf(
        "Online Booking & Rent Payments",
        "PG Discovery & Filters",
        "Property Verification",
        "Community & Experience Sharing"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Share Product Feedback", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
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
            contentPadding = PaddingValues(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SageSurface),
                    border = BorderStroke(1.dp, SageSoft),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = TealPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Help Us Improve StayEg",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TealDeep
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tell us what features or improvements would make your PG search and stay experience better.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Topic:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))

                        categories.forEach { cat ->
                            val isSel = selectedCategory == cat
                            Surface(
                                onClick = { selectedCategory = cat },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) SageSurface else BackgroundLight,
                                border = BorderStroke(1.dp, if (isSel) TealPrimary else BorderLight),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(cat, fontSize = 12.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium, color = TextPrimary)
                                    if (isSel) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Experience Rating ($rating / 5):", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            (1..5).forEach { star ->
                                IconButton(onClick = { rating = star }) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "$star Stars",
                                        tint = if (star <= rating) GoldAccent else BorderLight,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = feedbackText,
                            onValueChange = { feedbackText = it },
                            label = { Text("Your Feedback or Feature Request") },
                            placeholder = { Text("Share your suggestions...") },
                            minLines = 3,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("feedback_text_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (feedbackText.isBlank()) {
                                    viewModel.showToast("Please enter your feedback")
                                    return@Button
                                }
                                viewModel.submitFeatureFeedback(selectedCategory, rating, feedbackText)
                                feedbackText = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("submit_feedback_btn")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Submit Feedback", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (feedbacks.isNotEmpty()) {
                item {
                    Text("Your Submitted Feedback", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                }
                items(feedbacks) { fb ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        border = BorderStroke(1.dp, BorderLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(fb.featureCategory, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TealPrimary)
                                Text("${fb.rating}★ • ${fb.createdAt}", fontSize = 11.sp, color = GoldDark, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(fb.feedbackText, fontSize = 12.sp, color = TextPrimary)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(viewModel: StayEgViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About StayEg", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
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
            contentPadding = PaddingValues(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Modern PG & Co-Living Discovery", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "StayEg helps students and working professionals discover paying guest (PG) accommodations, co-living spaces, and hostels across Bengaluru.\n\nExplore verified and directory properties, inspect facilities visually, check commute directions on Google Maps, and connect directly with property owners with zero brokerage.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            item {
                InfoPillarCard(Icons.Default.MoneyOff, "Zero Brokerage Discovery", "Connect directly with PG owners via phone or Google Maps without middleman commissions.")
            }
            item {
                InfoPillarCard(Icons.Default.Verified, "Transparent Verification Labels", "Every listing clearly indicates whether it is a verified partner property or an unverified directory listing.")
            }
            item {
                InfoPillarCard(Icons.Default.Security, "Privacy & Trust First", "Your personal data and preferences are protected in compliance with Indian data privacy standards.")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(viewModel: StayEgViewModel) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contact & Support", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
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
            contentPadding = PaddingValues(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("StayEg Support Desk", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(10.dp))

                        ContactDetailRow(Icons.Default.Email, "Support & Grievance Email", "support@stayeg.in • legal@stayeg.in")
                        ContactDetailRow(Icons.Default.Phone, "Helpline", "+91 80-4567-8900 (9:00 AM – 9:00 PM)")
                        ContactDetailRow(Icons.Default.LocationOn, "Bengaluru Office", "Koramangala, Bengaluru, Karnataka 560034")
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Send Us a Message", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Your Name") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email or Phone Number") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = message,
                            onValueChange = { message = it },
                            label = { Text("How can we help you?") },
                            minLines = 3,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (name.isBlank() || message.isBlank()) {
                                    viewModel.showToast("Please enter your name and message")
                                    return@Button
                                }
                                viewModel.submitFeatureFeedback("Support Inquiry", 5, "$name ($email): $message")
                                name = ""
                                email = ""
                                message = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Text("Send Message", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeUseScreen(viewModel: StayEgViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tenant Safety & Guidelines", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
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
            contentPadding = PaddingValues(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ErrorRedLight),
                    border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Sos, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Emergency Helplines", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ErrorRed)
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        EmergencyContactRow("Emergency Response (Police / Fire / Medical)", "112")
                        EmergencyContactRow("Women Safety Helpline", "1091")
                        EmergencyContactRow("Ambulance", "108")
                        EmergencyContactRow("Cyber Crime Helpline", "1930")
                    }
                }
            }

            item {
                Text("Safety Checklist Before Moving In", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
            }

            item {
                SafetyRuleItem(
                    title = "1. Visit the Property in Person First",
                    desc = "Always inspect the room, washroom, food area, and neighborhood in person before paying any advance or security deposit."
                )
            }
            item {
                SafetyRuleItem(
                    title = "2. Check Security & House Rules in Writing",
                    desc = "Confirm working CCTV cameras, entry access, notice period, and deposit refund terms in writing with the property owner."
                )
            }
            item {
                SafetyRuleItem(
                    title = "3. Verify Owner / Warden Identity on Site",
                    desc = "Ensure you deal directly with the authorized PG owner or manager at the property reception."
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsScreen(viewModel: StayEgViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terms, Privacy & Disclaimers", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
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
            contentPadding = PaddingValues(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SageSurface),
                    border = BorderStroke(1.dp, SageSoft),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "LEGAL TERMS, PRIVACY POLICY & VERIFICATION DISCLAIMER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TealDeep
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Aligned with applicable Indian laws including the Information Technology Act, 2000, the Digital Personal Data Protection Act, 2023 (DPDPA), and the Indian Contract Act, 1872.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            item {
                LegalSectionCard(
                    title = "1. Scope of Service & Discovery Platform",
                    body = "• StayEg currently provides PG discovery and direct owner connections across Bengaluru.\n• StayEg does not currently process online bookings, rent payments, or security deposits inside the app.\n• Listings labeled 'Unverified Listing' are directory listings aggregated for tenant discovery. Tenants must verify room availability, pricing, and facilities directly with the property owner."
                )
            }

            item {
                LegalSectionCard(
                    title = "2. Tenant Due Diligence & Direct Owner Connections",
                    body = "• When you express interest, view Google Maps directions, or call a property owner, any subsequent visit, rental agreement, or offline payment takes place directly between you and the property owner.\n• StayEg charges zero brokerage to tenants and is not a party to any lease or payment transaction between tenants and property owners."
                )
            }

            item {
                LegalSectionCard(
                    title = "3. Privacy Policy (DPDPA 2023 Compliance)",
                    body = "• Information you provide during registration, profile creation, or enquiry submission (name, phone number, email, occupation, and optional KYC details) is collected with your consent solely to facilitate PG discovery, owner callbacks, and platform improvements.\n• You may request correction or deletion of your account data at any time by contacting support@stayeg.in."
                )
            }

            item {
                LegalSectionCard(
                    title = "4. Community Guidelines",
                    body = "• Community posts must reflect genuine, first-hand PG living experiences.\n• Abusive, defamatory, misleading, or promotional spam posts may be reported by users and removed by moderation."
                )
            }

            item {
                LegalSectionCard(
                    title = "5. Partner Enquiries & Verification",
                    body = "• Property owners submitting enquiries for the First 1,000 Verified PG Partner program confirm that they are authorized to represent the property.\n• Verification status is granted only after review of property details."
                )
            }
        }
    }
}

@Composable
private fun LegalSectionCard(title: String, body: String) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderLight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(body, fontSize = 12.sp, color = TextSecondary, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun InfoPillarCard(icon: ImageVector, title: String, desc: String) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderLight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SageSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                Text(desc, fontSize = 11.sp, color = TextSecondary, lineHeight = 16.sp)
            }
        }
    }
}

@Composable
private fun ContactDetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, fontSize = 11.sp, color = TextMuted)
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        }
    }
}

@Composable
private fun EmergencyContactRow(label: String, number: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
        Text(number, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
    }
}

@Composable
private fun SafetyRuleItem(title: String, desc: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderLight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(3.dp))
            Text(desc, fontSize = 12.sp, color = TextSecondary, lineHeight = 17.sp)
        }
    }
}
