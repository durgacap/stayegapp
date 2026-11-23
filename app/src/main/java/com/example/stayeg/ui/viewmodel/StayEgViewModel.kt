package com.example.stayeg.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.stayeg.data.model.*
import com.example.stayeg.data.repository.StayEgRepository
import com.example.stayeg.ui.navigation.Screen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class SearchFilterState(
    val query: String = "",
    val city: String = "Bangalore",
    val selectedArea: String = "All Areas",
    val listingMode: String = "ALL", // "ALL", "VERIFIED", "DIRECTORY"
    val pgCategoryType: String = "ALL", // "ALL", "LADIES", "GENTS", "COLIVING", "UNISEX", "HOSTELS"
    val preferredSharingFilter: String = "ALL", // "ALL", "SINGLE", "DOUBLE", "TRIPLE"
    val onlyAvailableBeds: Boolean = false,
    val gender: PGGender? = null,
    val minPrice: Int = 0,
    val maxPrice: Int = 30000,
    val selectedAmenities: Set<String> = emptySet(),
    val sortBy: String = "Rating", // "Rating", "Nearest", "Price: Low to High", "Price: High to Low"
    val nearMeActive: Boolean = false,
    val userLat: Double = 12.9352,
    val userLng: Double = 77.6245,
    val userLocationLabel: String = "Koramangala",
    val maxRadiusKm: Double = 6.5
) {
    val verificationFilter: String
        get() = listingMode
}

fun calculateGpsDistanceKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val earthRadiusKm = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
        sin(dLng / 2) * sin(dLng / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return (earthRadiusKm * c).coerceAtLeast(0.15)
}

class StayEgViewModel(
    val repository: StayEgRepository = StayEgRepository()
) : ViewModel() {

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Landing)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _showAuthOnboardingOverlay = MutableStateFlow(false)
    val showAuthOnboardingOverlay: StateFlow<Boolean> = _showAuthOnboardingOverlay.asStateFlow()

    private val _showFirstTimeGuidance = MutableStateFlow(true)
    val showFirstTimeGuidance: StateFlow<Boolean> = _showFirstTimeGuidance.asStateFlow()

    private val _showPostLoginUserGuidePopup = MutableStateFlow(false)
    val showPostLoginUserGuidePopup: StateFlow<Boolean> = _showPostLoginUserGuidePopup.asStateFlow()

    private val screenStack = mutableListOf<Screen>()

    private val _filters = MutableStateFlow(SearchFilterState())
    val filters: StateFlow<SearchFilterState> = _filters.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    val currentUser = repository.currentUser
    val activeStayConfig = repository.activeStayConfig
    val mealAttendance = repository.mealAttendance
    val gatePasses = repository.gatePasses
    val wardenMessages = repository.wardenMessages
    val allPgs = repository.pgs
    val bookings = repository.bookings
    val payments = repository.payments
    val complaints = repository.complaints
    val communityPosts = repository.communityPosts
    val communityGroups = repository.communityGroups
    val visitSchedules = repository.visitSchedules
    val interestLeads = repository.interestLeads
    val ownerActivationLeads = repository.ownerActivationLeads
    val featureFeedbacks = repository.featureFeedbacks

    val uniquePrecreatedOtps = repository.uniquePrecreatedOtps
    val bangaloreEventsAndSpots = repository.bangaloreEventsAndSpots
    val seoGeoFaqs = repository.seoGeoFaqs

    val supabaseStatus = repository.supabaseStatus
    val isSyncing = repository.isSyncing
    val realtimeState = repository.realtimeState
    val realtimeEvents = repository.realtimeEvents
    val activeJwtToken = repository.activeJwtToken
    val activeAuthSession = repository.activeAuthSession
    val cloudMediaUploads = repository.cloudMediaUploads
    val storageBucketObjects = repository.storageBucketObjects
    val userActivityLogs = repository.userActivityLogs
    val interactionCount = repository.interactionCount
    val shouldShowSmartReviewPopup = repository.shouldShowSmartReviewPopup

    private val bgScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        bgScope.launch {
            repository.syncWithSupabase()
        }
    }

    fun trackUserActivity(
        actionType: String,
        screenName: String,
        targetPgId: String = "",
        targetPgName: String = "",
        details: String = "",
        latitude: Double = 12.9352,
        longitude: Double = 77.6245
    ) {
        repository.trackUserActivity(
            actionType = actionType,
            screenName = screenName,
            targetPgId = targetPgId,
            targetPgName = targetPgName,
            details = details,
            latitude = latitude,
            longitude = longitude
        )
    }

    fun dismissSmartReviewPopup() {
        repository.dismissSmartReviewPopup()
    }

    fun refreshStorageBucketsFromSupabase(bucketFilter: String? = null) {
        bgScope.launch {
            repository.refreshStorageBucketsFromSupabase(bucketFilter)
        }
    }

    fun dismissFirstTimeGuidance() {
        _showFirstTimeGuidance.value = false
    }

    fun openPostLoginUserGuidePopup() {
        _showPostLoginUserGuidePopup.value = true
    }

    fun dismissPostLoginUserGuidePopup() {
        _showPostLoginUserGuidePopup.value = false
    }

    fun dismissAuthOnboarding() {
        _showAuthOnboardingOverlay.value = false
    }

    fun openAuthOnboarding() {
        _showAuthOnboardingOverlay.value = true
    }

    fun ensureSupabaseLoaded() {
        repository.ensureSupabaseSnapshotLoaded()
    }

    fun restorePersistedSession(): Boolean {
        val restored = repository.restorePersistedSession()
        _showAuthOnboardingOverlay.value = !restored
        return restored
    }

    fun testSupabase() {
        bgScope.launch {
            val res = repository.testSupabaseConnection()
            showToast(res.message)
        }
    }

    fun syncWithSupabase() {
        bgScope.launch {
            val success = repository.syncWithSupabase()
            if (success) {
                showToast("Synced 2,415 Bengaluru PG listings")
            } else {
                showToast("Loaded 2,415 Bengaluru PG listings")
            }
        }
    }

    fun toggleRealtimeSubscription(enable: Boolean) {
        repository.toggleRealtimeSubscription(enable)
        showToast(if (enable) "Live updates enabled" else "Live updates paused")
    }

    fun triggerLiveListingUpdate() {
        bgScope.launch {
            val ok = repository.triggerLiveListingUpdate()
            if (ok) {
                showToast("Listing updated")
            } else {
                showToast("Could not update listing")
            }
        }
    }

    fun setSupabaseApiKey(key: String) {
        repository.setSupabaseApiKey(key)
        testSupabase()
    }

    // Filtered PGs
    val filteredPgs: StateFlow<List<PG>> = combine(allPgs, filters) { pgs, f ->
        var list = pgs.filter { pg ->
            val matchesCity = f.city.isEmpty() ||
                    f.city.equals("All Cities", ignoreCase = true) ||
                    pg.city.equals(f.city, ignoreCase = true) ||
                    (f.city.equals("Bengaluru", ignoreCase = true) && pg.city.equals("Bangalore", ignoreCase = true)) ||
                    (f.city.equals("Bangalore", ignoreCase = true) && pg.city.equals("Bengaluru", ignoreCase = true))
            val matchesMode = when (f.listingMode) {
                "VERIFIED" -> !pg.isDirectoryListing || pg.isVerified
                "DIRECTORY" -> pg.isDirectoryListing && !pg.isVerified
                else -> true
            }
            val matchesAvailability = !f.onlyAvailableBeds ||
                    (pg.isVerified && !pg.isDirectoryListing && pg.rooms.any { r -> r.beds.any { b -> b.status == BedStatus.AVAILABLE } })

            val matchesSharing = when (f.preferredSharingFilter) {
                "SINGLE" -> pg.rooms.isEmpty() || pg.rooms.any { it.roomType == RoomType.SINGLE }
                "DOUBLE" -> pg.rooms.isEmpty() || pg.rooms.any { it.roomType == RoomType.DOUBLE }
                "TRIPLE" -> pg.rooms.isEmpty() || pg.rooms.any { it.roomType == RoomType.TRIPLE }
                else -> true
            }

            val matchesArea = f.selectedArea == "All Areas" ||
                    pg.area.contains(f.selectedArea, ignoreCase = true) ||
                    pg.address.contains(f.selectedArea, ignoreCase = true)
            val matchesQuery = f.query.isEmpty() ||
                    pg.city.contains(f.query, ignoreCase = true) ||
                    ("bengaluru".contains(f.query.trim().lowercase()) && pg.city.contains("Bangalore", ignoreCase = true)) ||
                    pg.name.contains(f.query, ignoreCase = true) ||
                    pg.address.contains(f.query, ignoreCase = true) ||
                    pg.area.contains(f.query, ignoreCase = true) ||
                    pg.category.contains(f.query, ignoreCase = true) ||
                    pg.claimCode.contains(f.query, ignoreCase = true)

            val matchesCategoryType = when (f.pgCategoryType) {
                "LADIES" -> pg.gender == PGGender.FEMALE ||
                        pg.category.contains("Women", ignoreCase = true) ||
                        pg.category.contains("Ladies", ignoreCase = true) ||
                        pg.category.contains("Girls", ignoreCase = true)
                "GENTS" -> pg.gender == PGGender.MALE ||
                        pg.category.contains("Men", ignoreCase = true) ||
                        pg.category.contains("Gents", ignoreCase = true) ||
                        pg.category.contains("Boys", ignoreCase = true)
                "COLIVING" -> pg.gender == PGGender.UNISEX ||
                        pg.category.contains("Co-Living", ignoreCase = true)
                "UNISEX" -> pg.gender == PGGender.UNISEX
                "HOSTELS" -> pg.name.contains("Hostel", ignoreCase = true) ||
                        pg.category.contains("Hostel", ignoreCase = true) ||
                        pg.price <= 8500
                else -> true
            }

            val matchesGender = f.gender == null || pg.gender == f.gender
            val matchesPrice = pg.price in f.minPrice..f.maxPrice
            val matchesAmenities = f.selectedAmenities.isEmpty() ||
                    f.selectedAmenities.all { a -> pg.amenities.any { it.contains(a, ignoreCase = true) } }

            matchesCity && matchesMode && matchesAvailability && matchesSharing && matchesArea && matchesQuery && matchesCategoryType && matchesGender && matchesPrice && matchesAmenities
        }

        if (f.nearMeActive) {
            val withinRadius = list.filter { pg ->
                calculateGpsDistanceKm(f.userLat, f.userLng, pg.lat, pg.lng) <= f.maxRadiusKm
            }
            list = (if (withinRadius.isNotEmpty()) withinRadius else list).sortedBy { pg ->
                calculateGpsDistanceKm(f.userLat, f.userLng, pg.lat, pg.lng)
            }
        } else {
            list = when (f.sortBy) {
                "Nearest" -> list.sortedBy { pg ->
                    calculateGpsDistanceKm(f.userLat, f.userLng, pg.lat, pg.lng)
                }
                "Price: Low to High" -> list.sortedBy { it.price }
                "Price: High to Low" -> list.sortedByDescending { it.price }
                else -> list.sortedByDescending { it.rating }
            }
        }
        list
    }.flowOn(Dispatchers.Default).stateIn(bgScope, SharingStarted.Eagerly, repository.pgs.value)

    fun navigateTo(screen: Screen) {
        _showAuthOnboardingOverlay.value = false
        if (_currentScreen.value != screen) {
            screenStack.add(_currentScreen.value)
            _currentScreen.value = screen
            when (screen) {
                is Screen.PGDetail -> {
                    val pg = allPgs.value.find { it.id == screen.pgId }
                    repository.trackUserActivity(
                        actionType = "PG_DETAIL_VIEW",
                        screenName = "PGDetail",
                        targetPgId = screen.pgId,
                        targetPgName = pg?.name ?: screen.pgId,
                        details = "Viewed PG in ${pg?.area ?: "Bengaluru"} (₹${pg?.price ?: 0}/mo)",
                        latitude = pg?.lat ?: 12.9352,
                        longitude = pg?.lng ?: 77.6245
                    )
                }
                is Screen.Explore -> {
                    repository.trackUserActivity(
                        actionType = "EXPLORE_PGS",
                        screenName = "Explore",
                        details = "Area=${_filters.value.selectedArea}, Category=${_filters.value.pgCategoryType}"
                    )
                }
                else -> {
                    repository.trackUserActivity(
                        actionType = "SCREEN_VISIT",
                        screenName = screen::class.simpleName ?: "Screen",
                        details = "Navigated to ${screen::class.simpleName ?: "Screen"}"
                    )
                }
            }
        }
    }

    fun goBack(): Boolean {
        if (_showAuthOnboardingOverlay.value) {
            _showAuthOnboardingOverlay.value = false
            return true
        }
        if (screenStack.isNotEmpty()) {
            _currentScreen.value = screenStack.removeAt(screenStack.lastIndex)
            return true
        }
        if (_currentScreen.value != Screen.Landing) {
            _currentScreen.value = Screen.Landing
            return true
        }
        return false
    }

    fun setQuery(q: String) {
        _filters.value = _filters.value.copy(query = q)
    }

    fun setCity(city: String) {
        _filters.value = _filters.value.copy(city = city)
    }

    fun setSelectedArea(area: String) {
        _filters.value = _filters.value.copy(selectedArea = area)
    }

    fun setListingMode(mode: String) {
        _filters.value = _filters.value.copy(
            listingMode = mode,
            onlyAvailableBeds = if (mode == "DIRECTORY") false else _filters.value.onlyAvailableBeds
        )
    }

    fun setVerificationFilter(mode: String) {
        setListingMode(mode)
    }

    fun scheduleFreeVisitPickup(
        pgId: String,
        pgName: String,
        pickupLandmark: String,
        visitDateSlot: String,
        vehicleType: String
    ) {
        scheduleVisit(
            pgId = pgId,
            pgName = pgName,
            visitorName = currentUser.value?.name ?: "StayEg Visitor",
            visitorPhone = currentUser.value?.phone ?: "+91 98800 00000",
            visitDate = visitDateSlot,
            visitTime = visitDateSlot,
            pickupMode = vehicleType,
            pickupLocation = pickupLandmark
        )
    }

    fun toggleOnlyAvailableBeds() {
        val next = !_filters.value.onlyAvailableBeds
        _filters.value = _filters.value.copy(
            onlyAvailableBeds = next,
            listingMode = if (next) "VERIFIED" else _filters.value.listingMode
        )
    }

    fun setPreferredSharingFilter(sharing: String) {
        _filters.value = _filters.value.copy(preferredSharingFilter = sharing)
    }

    fun selectPgCategory(categoryType: String, navigateToExplore: Boolean = false) {
        val mappedGender = when (categoryType) {
            "LADIES" -> PGGender.FEMALE
            "GENTS" -> PGGender.MALE
            "UNISEX", "COLIVING" -> PGGender.UNISEX
            else -> null
        }
        _filters.value = _filters.value.copy(
            pgCategoryType = categoryType,
            gender = mappedGender
        )
        if (navigateToExplore) {
            navigateTo(Screen.Explore)
        }
    }

    fun setGender(gender: PGGender?) {
        val catType = when (gender) {
            PGGender.FEMALE -> "LADIES"
            PGGender.MALE -> "GENTS"
            PGGender.UNISEX -> "UNISEX"
            null -> "ALL"
        }
        _filters.value = _filters.value.copy(gender = gender, pgCategoryType = catType)
    }

    fun setPriceRange(min: Int, max: Int) {
        _filters.value = _filters.value.copy(minPrice = min, maxPrice = max)
    }

    fun toggleAmenity(amenity: String) {
        val current = _filters.value.selectedAmenities
        val updated = if (current.contains(amenity)) current - amenity else current + amenity
        _filters.value = _filters.value.copy(selectedAmenities = updated)
    }

    fun setSortBy(sort: String) {
        _filters.value = _filters.value.copy(sortBy = sort)
    }

    fun resetFilters() {
        _filters.value = SearchFilterState(city = _filters.value.city)
    }

    fun showToast(message: String) {
        bgScope.launch {
            _toastMessage.emit(message)
        }
    }

    // ─── Auth, OTP Verification, Password, Email Recovery, Google & KYC ─────

    fun requestVerificationOtp(destination: String): String {
        val code = repository.requestOtpForDestination(destination)
        showToast("Verification OTP sent to $destination: $code")
        return code
    }

    fun isValidOtp(otp: String, destination: String = ""): Boolean {
        return repository.isValidPrecreatedOtp(otp, destination)
    }

    fun signUpOrLoginWithPhoneOtp(
        name: String,
        phone: String,
        email: String,
        otp: String,
        password: String,
        locality: String = "Koramangala",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val res = repository.signUpOrLoginWithPhoneOtp(
            name = name,
            phone = phone,
            email = email,
            otp = otp,
            password = password,
            locality = locality
        )
        res.onSuccess { user ->
            _showPostLoginUserGuidePopup.value = true
            showToast("Account created! Welcome to StayEg, ${user.name}!")
            onSuccess()
        }.onFailure { err ->
            val msg = err.message ?: "Sign-up failed"
            showToast(msg)
            onError(msg)
        }
    }

    fun loginWithPassword(
        identifier: String,
        password: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val res = repository.loginWithPassword(identifier, password)
        res.onSuccess { user ->
            _showAuthOnboardingOverlay.value = false
            _showPostLoginUserGuidePopup.value = true
            showToast("Welcome back, ${user.name}!")
            onSuccess()
        }.onFailure { err ->
            val msg = err.message ?: "Login failed"
            showToast(msg)
            onError(msg)
        }
    }

    fun verifyGoogleWithOtp(
        name: String,
        email: String,
        otp: String,
        password: String = "",
        googleIdToken: String = "",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val res = repository.verifyGoogleWithOtp(
            name = name,
            email = email,
            otp = otp,
            password = password,
            googleIdToken = googleIdToken
        )
        res.onSuccess { user ->
            _showPostLoginUserGuidePopup.value = true
            showToast("Signed in with Google as ${user.email}")
            onSuccess()
        }.onFailure { err ->
            val msg = err.message ?: "Invalid Google verification OTP"
            showToast(msg)
            onError(msg)
        }
    }

    fun signInWithGoogle(name: String, email: String, googleIdToken: String = "", onSuccess: () -> Unit = {}) {
        val user = repository.signInWithGoogle(name, email, googleIdToken)
        _showAuthOnboardingOverlay.value = false
        _showPostLoginUserGuidePopup.value = true
        showToast("Signed in with Google as ${user.email}")
        onSuccess()
    }

    fun resetPasswordWithEmailVerification(
        email: String,
        emailOtp: String,
        newPassword: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val ok = repository.resetPasswordWithEmailVerification(email, emailOtp, newPassword)
        if (ok) {
            showToast("Password updated for $email! You can now log in with your new password.")
            onSuccess()
        } else {
            val msg = "Please enter a valid email, a valid 6-digit OTP from the pool, and a password (4+ chars)."
            showToast(msg)
            onError(msg)
        }
    }

    fun completeProfileAndKyc(
        name: String,
        phone: String,
        email: String,
        occupation: String,
        city: String,
        aadhaar: String,
        pan: String
    ) {
        repository.updateProfileAndKyc(name, phone, email, occupation, city, aadhaar, pan)
        _showAuthOnboardingOverlay.value = false
        _showPostLoginUserGuidePopup.value = true
        showToast("Profile & KYC saved!")
    }

    // ─── Tenant Interest, Owner Enquiries & Feedback ────────────────────────

    fun expressInterestInPg(
        pgId: String,
        pgName: String,
        area: String,
        tenantName: String,
        tenantPhone: String,
        tenantEmail: String,
        preferredSharing: String,
        expectedMoveIn: String,
        notes: String
    ) {
        repository.expressInterestInPg(
            pgId = pgId,
            pgName = pgName,
            area = area,
            tenantName = tenantName,
            tenantPhone = tenantPhone,
            tenantEmail = tenantEmail,
            preferredSharing = preferredSharing,
            expectedMoveIn = expectedMoveIn,
            notes = notes
        )
        showToast("Interest registered for $pgName!")
    }

    fun submitOwnerActivationLead(
        ownerName: String,
        pgName: String,
        area: String,
        phone: String,
        email: String,
        totalBeds: Int,
        claimCode: String
    ) {
        val lead = repository.submitOwnerActivationLead(ownerName, pgName, area, phone, email, totalBeds, claimCode)
        showToast("Registered '${lead.pgName}' in First 1,000 Free Verified Pool (#${lead.poolRank}/1000)!")
    }

    fun submitFeatureFeedback(
        featureCategory: String,
        rating: Int,
        feedbackText: String
    ) {
        repository.submitFeatureFeedback(featureCategory, rating, feedbackText)
        showToast("Thank you! Your feedback on $featureCategory has been submitted.")
    }

    fun reportCommunityPost(postId: String, reason: String) {
        repository.reportCommunityPost(postId, reason)
        showToast("Report submitted. Our moderation team will review this post.")
    }

    fun switchRole(role: UserRole) {
        repository.loginAs(role)
        showToast("Switched to ${role.name} mode")
        when (role) {
            UserRole.TENANT -> navigateTo(Screen.Landing)
            UserRole.OWNER -> navigateTo(Screen.OwnerJoinLead)
            UserRole.ADMIN -> navigateTo(Screen.OwnerDashboard)
            UserRole.VENDOR -> navigateTo(Screen.Landing)
        }
    }

    fun logout() {
        repository.logout()
        showToast("Signed out")
        _showAuthOnboardingOverlay.value = true
    }

    fun bookBed(
        pgId: String,
        bedId: String,
        checkInDate: String,
        amount: Int,
        pgName: String,
        roomCode: String,
        bedNumber: Int
    ) {
        repository.createBooking(pgId, bedId, checkInDate, amount, pgName, roomCode, bedNumber)
        showToast("Interest recorded for $pgName")
        navigateTo(Screen.MyStay)
    }

    fun scheduleVisit(
        pgId: String,
        pgName: String,
        visitorName: String,
        visitorPhone: String,
        visitDate: String,
        visitTime: String,
        pickupMode: String,
        pickupLocation: String
    ) {
        repository.scheduleVisit(
            pgId = pgId,
            pgName = pgName,
            visitorName = visitorName,
            visitorPhone = visitorPhone,
            visitDate = visitDate,
            visitTime = visitTime,
            pickupMode = pickupMode,
            pickupLocation = pickupLocation
        )
        showToast("Visit scheduled for $visitDate at $visitTime")
    }

    fun claimAndVerifyPg(pgId: String, verifiedPrice: Int) {
        repository.claimDirectoryListing(pgId, verifiedPrice)
        showToast("Property verified! Direct Owner Contact & Bed Availability are now unlocked.")
    }

    fun updatePgDetails(pgId: String, newName: String, newPrice: Int, newAddress: String) {
        repository.updatePgDetails(pgId, newName, newPrice, newAddress)
        showToast("Property '$newName' updated")
    }

    fun addRoomToPg(pgId: String, roomCode: String, roomType: RoomType, floor: Int, bedPrice: Int) {
        repository.addRoomToPg(pgId, roomCode, roomType, floor, bedPrice)
        showToast("Room ${roomCode.uppercase()} (${roomType.name}) added")
    }

    fun payRent(bookingId: String, amount: Int, method: String) {
        repository.payRent(bookingId, amount, method)
        showToast("Payment preference recorded")
    }

    fun payMonthlyRentViaUpi(
        amount: Int = activeStayConfig.value.monthlyRent + activeStayConfig.value.electricityExtra,
        upiApp: String = "Google Pay",
        upiId: String = "stayeg.escrow@axis",
        paymentType: PaymentType = PaymentType.RENT
    ): Payment {
        val payment = repository.payMonthlyRentViaUpi(
            amount = amount,
            upiApp = upiApp,
            upiId = upiId,
            paymentType = paymentType
        )
        showToast("Paid ₹${payment.amount} via $upiApp • Digital Receipt #${payment.id.takeLast(4).uppercase()} generated")
        return payment
    }

    fun buildUpiDeepLinkUri(
        amount: Int,
        upiPayeeVpa: String = "stayeg.escrow@axis",
        payeeName: String = "StayEg Residences",
        note: String = "Monthly PG Rent - Room ${activeStayConfig.value.roomCode}"
    ): String {
        return repository.buildUpiDeepLinkUri(amount, upiPayeeVpa, payeeName, note)
    }

    fun raiseComplaint(
        pgId: String,
        title: String,
        description: String,
        category: ComplaintCategory,
        priority: ComplaintPriority,
        pgName: String = activeStayConfig.value.pgName,
        roomCode: String = activeStayConfig.value.roomCode,
        bedNumber: Int = activeStayConfig.value.bedNumber,
        issueSubType: String = "Maintenance",
        preferredVisitSlot: String = "Morning (9 AM - 12 PM)",
        photoAttachmentUri: String = ""
    ) {
        val created = repository.raiseComplaint(
            pgId = pgId,
            title = title,
            description = description,
            category = category,
            priority = priority,
            pgName = pgName,
            roomCode = roomCode,
            bedNumber = bedNumber,
            issueSubType = issueSubType,
            preferredVisitSlot = preferredVisitSlot,
            photoAttachmentUri = photoAttachmentUri
        )
        showToast("Maintenance ticket #${created.id.takeLast(4).uppercase()} submitted • Assigned to ${created.assignedTo}")
    }

    fun updateComplaintStatus(
        complaintId: String,
        status: ComplaintStatus,
        resolution: String = "",
        assignedTo: String = ""
    ) {
        repository.updateComplaintStatus(complaintId, status, resolution, assignedTo)
        val statusLabel = when (status) {
            ComplaintStatus.OPEN -> "Reopened"
            ComplaintStatus.IN_PROGRESS -> "In Progress"
            ComplaintStatus.RESOLVED -> "Resolved"
            ComplaintStatus.CLOSED -> "Closed"
        }
        showToast("Ticket status updated to $statusLabel")
    }

    fun addComplaintFollowUpNote(complaintId: String, note: String) {
        repository.addComplaintFollowUpNote(complaintId, note)
        showToast("Update added to ticket")
    }

    fun refreshComplaintsFromSupabase() {
        bgScope.launch {
            repository.refreshComplaintsFromSupabase()
        }
    }

    fun resolveComplaint(complaintId: String, resolution: String) {
        repository.updateComplaintStatus(complaintId, ComplaintStatus.RESOLVED, resolution)
        showToast("Ticket marked as resolved")
    }

    fun toggleLikePost(postId: String) {
        repository.toggleLikePost(postId)
    }

    fun toggleJoinGroup(groupId: String) {
        repository.toggleJoinGroup(groupId)
    }

    fun createPost(
        title: String,
        content: String,
        category: String,
        tags: List<String>,
        pgName: String = "",
        areaName: String = "Bengaluru",
        experienceRating: Int = 5
    ) {
        repository.createPost(title, content, category, tags, pgName, areaName, experienceRating)
        showToast("Your PG experience has been shared!")
    }

    fun updateKyc(aadhaar: String, pan: String) {
        repository.updateKyc(aadhaar, pan)
        showToast("Digital KYC details verified & saved")
    }

    fun uploadKycDocument(slot: String, uri: String) {
        repository.uploadKycDocument(slot, uri)
        val slotLabel = when (slot.uppercase()) {
            "FRONT" -> "Government ID Front"
            "BACK", "PAN" -> "ID Back / PAN Document"
            "SELFIE" -> "Live Tenant Selfie"
            else -> "Identification Document"
        }
        showToast("$slotLabel uploaded — real-time KYC status updated")
    }

    fun removeKycDocument(slot: String) {
        repository.removeKycDocument(slot)
        showToast("Document removed — real-time KYC status updated")
    }

    fun updateKycStatusRealtime(status: KYCStatus, note: String = "") {
        repository.updateKycStatusRealtime(status, note)
        showToast("Real-time KYC Status: ${status.name.replace('_', ' ')}")
    }

    fun submitManualDigitalKyc(
        documentType: String,
        aadhaarOrIdNumber: String,
        panNumber: String,
        guardianName: String,
        permanentAddress: String,
        frontPhotoUri: String,
        selfiePhotoUri: String,
        backPhotoUri: String = "",
        targetStatus: KYCStatus = KYCStatus.VERIFIED
    ) {
        repository.submitManualDigitalKyc(
            documentType = documentType,
            aadhaarOrIdNumber = aadhaarOrIdNumber,
            panNumber = panNumber,
            guardianName = guardianName,
            permanentAddress = permanentAddress,
            frontPhotoUri = frontPhotoUri,
            selfiePhotoUri = selfiePhotoUri,
            backPhotoUri = backPhotoUri,
            targetStatus = targetStatus
        )
        val msg = if (targetStatus == KYCStatus.VERIFIED) {
            "Digital KYC Verified! Your Tenant Passport is now active."
        } else {
            "Identification documents submitted! Real-time status: Pending Review."
        }
        showToast(msg)
    }

    fun resetDigitalKyc() {
        repository.resetDigitalKyc()
        showToast("KYC status reset. You can submit fresh Digital KYC details.")
    }

    fun updateProfilePhoto(photoUri: String) {
        repository.updateProfilePhoto(photoUri)
        showToast("Profile photo updated!")
    }

    fun saveCustomizedProfile(
        name: String,
        phone: String,
        email: String,
        gender: String,
        city: String,
        currentArea: String,
        occupation: String,
        organizationName: String,
        workMode: String,
        foodPreference: String,
        sleepSchedule: String,
        languagesSpoken: String,
        emergencyContactName: String,
        emergencyContactPhone: String,
        bio: String,
        avatarUrl: String
    ) {
        repository.saveCustomizedProfile(
            name = name,
            phone = phone,
            email = email,
            gender = gender,
            city = city,
            currentArea = currentArea,
            occupation = occupation,
            organizationName = organizationName,
            workMode = workMode,
            foodPreference = foodPreference,
            sleepSchedule = sleepSchedule,
            languagesSpoken = languagesSpoken,
            emergencyContactName = emergencyContactName,
            emergencyContactPhone = emergencyContactPhone,
            bio = bio,
            avatarUrl = avatarUrl
        )
        showToast("Customized Tenant Profile saved!")
    }

    fun updateActiveStayConfig(
        pgId: String,
        pgName: String,
        area: String,
        roomCode: String,
        floorNumber: Int,
        bedNumber: Int,
        sharingType: String,
        monthlyRent: Int,
        moveInDate: String
    ) {
        repository.updateActiveStayConfig(
            pgId = pgId,
            pgName = pgName,
            area = area,
            roomCode = roomCode,
            floorNumber = floorNumber,
            bedNumber = bedNumber,
            sharingType = sharingType,
            monthlyRent = monthlyRent,
            moveInDate = moveInDate
        )
        showToast("Updated active stay: $pgName • Room $roomCode • Bed #$bedNumber")
    }

    fun toggleMealOptIn(slot: String) {
        repository.toggleMealOptIn(slot)
    }

    fun updateMealPreferences(mealMode: String, specialNote: String) {
        repository.updateMealPreferences(mealMode, specialNote)
        showToast("Mess & meal preference updated: $mealMode")
    }

    fun createGatePass(
        passType: String,
        title: String,
        visitorOrReason: String,
        dateRange: String,
        timeSlot: String
    ) {
        repository.createGatePass(passType, title, visitorOrReason, dateRange, timeSlot)
        showToast("Gate Pass created & synced with property security!")
    }

    fun cancelGatePass(passId: String) {
        repository.cancelGatePass(passId)
        showToast("Gate Pass cancelled")
    }

    fun sendWardenChatMessage(messageText: String) {
        repository.sendWardenChatMessage(messageText)
        showToast("Message delivered to property warden in-app")
    }

    fun toggleMoveOutNotice(active: Boolean, targetDate: String = "30 Dec 2026") {
        repository.toggleMoveOutNotice(active, targetDate)
        showToast(if (active) "30-Day Move-Out Notice activated for $targetDate" else "Move-Out Notice withdrawn")
    }

    fun activateStayViaQrCode(
        qrCode: String,
        pgId: String = "",
        roomCode: String = "101",
        floorNumber: Int = 1,
        bedNumber: Int = 1,
        sharingType: String = "Double Sharing"
    ): ActiveStayConfig {
        val stay = repository.activateStayViaQrCode(
            qrCode = qrCode,
            pgId = pgId,
            roomCode = roomCode,
            floorNumber = floorNumber,
            bedNumber = bedNumber,
            sharingType = sharingType
        )
        showToast("Checked into ${stay.pgName} (Room ${stay.roomCode} • Bed #${stay.bedNumber}) via QR Code!")
        return stay
    }

    fun deactivateActiveStay() {
        repository.deactivateActiveStay()
        showToast("Left active stay view")
    }

    // ─── GPS Nearby Location & Live Search Recommendations ──────────────────

    private val bengaluruHubCoords = listOf(
        Triple("Koramangala", 12.9352, 77.6245),
        Triple("HSR Layout", 12.9121, 77.6446),
        Triple("Indiranagar", 12.9784, 77.6408),
        Triple("Whitefield", 12.9855, 77.7368),
        Triple("Electronic City", 12.8452, 77.6602),
        Triple("Bellandur", 12.9279, 77.6762),
        Triple("BTM Layout", 12.9166, 77.6101),
        Triple("Marathahalli", 12.9591, 77.6974),
        Triple("Nagavara", 13.0451, 77.6203)
    )

    fun distanceFromUserKm(pg: PG): Double {
        val f = _filters.value
        return calculateGpsDistanceKm(f.userLat, f.userLng, pg.lat, pg.lng)
    }

    fun activateNearMeGpsLocation(
        rawLat: Double? = null,
        rawLng: Double? = null,
        customAreaHint: String = ""
    ) {
        val currentF = _filters.value
        val inBengaluruBounds = rawLat != null && rawLng != null &&
            rawLat in 12.65..13.25 && rawLng in 77.35..77.90

        val (resolvedArea, resolvedLat, resolvedLng) = when {
            customAreaHint.isNotBlank() && customAreaHint != "All Areas" -> {
                bengaluruHubCoords.firstOrNull {
                    it.first.contains(customAreaHint, ignoreCase = true) ||
                        customAreaHint.contains(it.first, ignoreCase = true)
                } ?: Triple(customAreaHint, 12.9352, 77.6245)
            }
            inBengaluruBounds -> {
                val lat = rawLat!!
                val lng = rawLng!!
                val nearestHub = bengaluruHubCoords.minByOrNull {
                    calculateGpsDistanceKm(lat, lng, it.second, it.third)
                }?.first ?: "Bengaluru"
                Triple(nearestHub, lat, lng)
            }
            currentF.selectedArea != "All Areas" && currentF.selectedArea.isNotBlank() -> {
                bengaluruHubCoords.firstOrNull {
                    it.first.contains(currentF.selectedArea, ignoreCase = true)
                } ?: Triple(currentF.selectedArea, 12.9352, 77.6245)
            }
            else -> {
                val userArea = currentUser.value?.currentArea.orEmpty()
                bengaluruHubCoords.firstOrNull {
                    userArea.isNotBlank() && userArea.contains(it.first, ignoreCase = true)
                } ?: Triple("Koramangala", 12.9352, 77.6245)
            }
        }

        _filters.value = currentF.copy(
            nearMeActive = true,
            userLat = resolvedLat,
            userLng = resolvedLng,
            userLocationLabel = resolvedArea,
            sortBy = "Nearest"
        )
        trackUserActivity(
            actionType = "GPS_NEARBY_PGS_ACTIVATED",
            screenName = _currentScreen.value::class.simpleName ?: "Search",
            details = "GPS Near Me activated around $resolvedArea (${String.format("%.4f", resolvedLat)}, ${String.format("%.4f", resolvedLng)})",
            latitude = resolvedLat,
            longitude = resolvedLng
        )
        showToast("GPS active: Showing PGs nearest to $resolvedArea")
    }

    fun toggleNearMeGpsFilter(rawLat: Double? = null, rawLng: Double? = null) {
        if (_filters.value.nearMeActive && rawLat == null) {
            _filters.value = _filters.value.copy(
                nearMeActive = false,
                sortBy = "Rating"
            )
            showToast("Nearby GPS filter cleared")
        } else {
            activateNearMeGpsLocation(rawLat, rawLng)
        }
    }

    fun getSearchRecommendations(rawQuery: String): SearchRecommendationsResult {
        val q = rawQuery.trim()
        val f = _filters.value
        val pgs = allPgs.value
        val allAreas = getDistinctAreasWithPgCounts().map { it.first }

        val matchingAreas = if (q.isEmpty()) {
            listOf("Koramangala", "HSR Layout", "Whitefield", "Electronic City", "Indiranagar", "BTM Layout")
        } else {
            allAreas.filter { area ->
                area.contains(q, ignoreCase = true)
            }.take(5)
        }

        val matchingPgs = if (q.isEmpty()) {
            pgs.sortedBy { calculateGpsDistanceKm(f.userLat, f.userLng, it.lat, it.lng) }.take(4)
        } else {
            pgs.filter { pg ->
                pg.name.contains(q, ignoreCase = true) ||
                    pg.area.contains(q, ignoreCase = true) ||
                    pg.address.contains(q, ignoreCase = true) ||
                    pg.category.contains(q, ignoreCase = true)
            }.sortedBy { pg ->
                val nameStarts = if (pg.name.startsWith(q, ignoreCase = true)) 0 else 1
                val areaStarts = if (pg.area.startsWith(q, ignoreCase = true)) 0 else 1
                nameStarts * 2 + areaStarts
            }.take(6)
        }

        val nearbyPgs = pgs.sortedBy {
            calculateGpsDistanceKm(f.userLat, f.userLng, it.lat, it.lng)
        }.take(4)

        return SearchRecommendationsResult(
            query = q,
            matchingAreas = matchingAreas,
            matchingPgs = matchingPgs,
            nearbyPgs = nearbyPgs
        )
    }

    fun querySupabasePgsByCityOrArea(
        cityOrAreaQuery: String,
        selectedCity: String = "All Cities",
        selectedArea: String = "All Areas",
        onlyAvailableBeds: Boolean = false,
        categoryFilter: String = "ALL"
    ): List<PG> {
        val baseResults = repository.querySupabasePgsByCityOrArea(
            cityOrAreaQuery = cityOrAreaQuery,
            selectedCity = selectedCity,
            selectedArea = selectedArea,
            onlyAvailableBeds = onlyAvailableBeds,
            categoryFilter = categoryFilter
        )
        val f = _filters.value
        return if (f.nearMeActive) {
            val within = baseResults.filter { pg ->
                calculateGpsDistanceKm(f.userLat, f.userLng, pg.lat, pg.lng) <= f.maxRadiusKm
            }
            (if (within.isNotEmpty()) within else baseResults).sortedBy { pg ->
                calculateGpsDistanceKm(f.userLat, f.userLng, pg.lat, pg.lng)
            }
        } else {
            baseResults
        }
    }

    fun getDistinctAreasWithPgCounts(): List<Pair<String, Int>> {
        return repository.getDistinctAreasWithPgCounts()
    }
}

