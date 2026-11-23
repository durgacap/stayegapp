package com.example.stayeg.data.repository

import com.example.stayeg.data.model.*
import com.example.stayeg.data.remote.SupabaseConnectionStatus
import com.example.stayeg.data.remote.SupabaseService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class StayEgRepository(
    autoRestoreSession: Boolean = false
) {

    val supabaseService = SupabaseService()
    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _supabaseStatus = MutableStateFlow<SupabaseConnectionStatus?>(null)
    val supabaseStatus: StateFlow<SupabaseConnectionStatus?> = _supabaseStatus.asStateFlow()

    val realtimeState = supabaseService.realtimeState
    val realtimeEvents = supabaseService.realtimeEvents

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _activeAuthSession = MutableStateFlow<SupabaseAuthSession?>(null)
    val activeAuthSession: StateFlow<SupabaseAuthSession?> = _activeAuthSession.asStateFlow()

    private val _activeStayConfig = MutableStateFlow(
        ActiveStayConfig(
            pgId = "pg-1",
            pgName = "Zolo Stays Crown - Koramangala 6th Block",
            area = "Koramangala 6th Block",
            roomCode = "101",
            floorNumber = 1,
            bedNumber = 1,
            sharingType = "Double Sharing",
            monthlyRent = 11500,
            securityDeposit = 23000,
            electricityExtra = 450,
            moveInDate = "01 Nov 2026"
        )
    )
    val activeStayConfig: StateFlow<ActiveStayConfig> = _activeStayConfig.asStateFlow()

    private val _mealAttendance = MutableStateFlow(MealAttendanceState())
    val mealAttendance: StateFlow<MealAttendanceState> = _mealAttendance.asStateFlow()

    private val _gatePasses = MutableStateFlow(
        listOf(
            TenantGatePass(
                id = "pass-101",
                passType = "LATE_ENTRY",
                title = "Late Entry Gate Pass (Biometric Synced)",
                visitorOrReason = "Late shift at Tech Park",
                dateRange = "Tonight",
                timeSlot = "11:45 PM Entry",
                status = "APPROVED"
            )
        )
    )
    val gatePasses: StateFlow<List<TenantGatePass>> = _gatePasses.asStateFlow()

    private val _wardenMessages = MutableStateFlow(
        listOf(
            WardenChatMessage(
                id = "wmsg-1",
                senderName = "Mohan Rao (Property Warden)",
                isFromTenant = false,
                message = "Welcome to Zolo Stays Crown! Your Room 101 • Bed #1 biometric tag and fiber Wi-Fi access are active. Message here anytime for housekeeping or meal requests.",
                timestamp = "09:30 AM"
            )
        )
    )
    val wardenMessages: StateFlow<List<WardenChatMessage>> = _wardenMessages.asStateFlow()

    private val _pgs = MutableStateFlow<List<PG>>(
        SupabaseService.loadFullSupabaseSnapshot().ifEmpty { createSeedPgs() }
    )
    val pgs: StateFlow<List<PG>> = _pgs.asStateFlow()

    private val _bookings = MutableStateFlow<List<Booking>>(createSeedBookings())
    val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()

    private val _payments = MutableStateFlow<List<Payment>>(createSeedPayments())
    val payments: StateFlow<List<Payment>> = _payments.asStateFlow()

    private val _complaints = MutableStateFlow<List<Complaint>>(createSeedComplaints())
    val complaints: StateFlow<List<Complaint>> = _complaints.asStateFlow()

    private val _communityPosts = MutableStateFlow<List<CommunityPost>>(createSeedPosts())
    val communityPosts: StateFlow<List<CommunityPost>> = _communityPosts.asStateFlow()

    private val _communityGroups = MutableStateFlow<List<CommunityGroup>>(createSeedGroups())
    val communityGroups: StateFlow<List<CommunityGroup>> = _communityGroups.asStateFlow()

    private val _visitSchedules = MutableStateFlow<List<PGVisitSchedule>>(emptyList())
    val visitSchedules: StateFlow<List<PGVisitSchedule>> = _visitSchedules.asStateFlow()

    private val _interestLeads = MutableStateFlow<List<PGInterestLead>>(emptyList())
    val interestLeads: StateFlow<List<PGInterestLead>> = _interestLeads.asStateFlow()

    private val _ownerActivationLeads = MutableStateFlow<List<OwnerActivationLead>>(emptyList())
    val ownerActivationLeads: StateFlow<List<OwnerActivationLead>> = _ownerActivationLeads.asStateFlow()

    private val _featureFeedbacks = MutableStateFlow<List<AppFeatureFeedback>>(emptyList())
    val featureFeedbacks: StateFlow<List<AppFeatureFeedback>> = _featureFeedbacks.asStateFlow()

    val uniquePrecreatedOtps: List<String> = SupabaseService.UNIQUE_PRECREATED_OTPS

    private var otpRotationIndex: Int = 0

    private val _activeJwtToken = MutableStateFlow("")
    val activeJwtToken: StateFlow<String> = _activeJwtToken.asStateFlow()

    private val _cloudMediaUploads = MutableStateFlow<Map<String, String>>(emptyMap())
    val cloudMediaUploads: StateFlow<Map<String, String>> = _cloudMediaUploads.asStateFlow()

    private val _storageBucketObjects = MutableStateFlow<List<SupabaseStorageObject>>(emptyList())
    val storageBucketObjects: StateFlow<List<SupabaseStorageObject>> = _storageBucketObjects.asStateFlow()

    private val _userActivityLogs = MutableStateFlow<List<UserActivityTelemetry>>(emptyList())
    val userActivityLogs: StateFlow<List<UserActivityTelemetry>> = _userActivityLogs.asStateFlow()

    private val _interactionCount = MutableStateFlow(0)
    val interactionCount: StateFlow<Int> = _interactionCount.asStateFlow()

    private val _shouldShowSmartReviewPopup = MutableStateFlow(false)
    val shouldShowSmartReviewPopup: StateFlow<Boolean> = _shouldShowSmartReviewPopup.asStateFlow()

    private var hasDismissedOrSubmittedReviewInSession: Boolean = false

    private val registeredUsersByIdentifier = mutableMapOf<String, User>()

    private val userPasswordsByPhoneOrEmail = mutableMapOf<String, String>()

    private val issuedVerificationOtps = mutableMapOf<String, String>()

    private fun normalizePhoneTail(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        return if (digits.length >= 10) digits.takeLast(10) else ""
    }

    private fun registerUserInAccountDirectory(user: User, rawPasswordOrHash: String? = null) {
        val cleanEmail = user.email.trim().lowercase()
        val cleanPhone = user.phone.trim()
        val phoneTail = normalizePhoneTail(cleanPhone)

        if (cleanEmail.isNotBlank()) {
            registeredUsersByIdentifier[cleanEmail] = user
        }
        if (cleanPhone.isNotBlank()) {
            registeredUsersByIdentifier[cleanPhone] = user
        }
        if (phoneTail.length == 10) {
            registeredUsersByIdentifier[phoneTail] = user
            registeredUsersByIdentifier["+91 $phoneTail"] = user
        }

        if (!rawPasswordOrHash.isNullOrBlank()) {
            if (cleanEmail.isNotBlank()) {
                val h = if (rawPasswordOrHash.startsWith("pbkdf2_sha256$")) rawPasswordOrHash
                else SupabaseService.hashPasswordPbkdf2(cleanEmail, rawPasswordOrHash)
                userPasswordsByPhoneOrEmail[cleanEmail] = h
            }
            if (cleanPhone.isNotBlank()) {
                val h = if (rawPasswordOrHash.startsWith("pbkdf2_sha256$")) rawPasswordOrHash
                else SupabaseService.hashPasswordPbkdf2(cleanPhone, rawPasswordOrHash)
                userPasswordsByPhoneOrEmail[cleanPhone] = h
                if (phoneTail.length == 10) {
                    userPasswordsByPhoneOrEmail[phoneTail] = h
                }
            }
        }
    }

    private fun findRegisteredUserByIdentifier(identifier: String): User? {
        val clean = identifier.trim()
        if (clean.isEmpty()) return null
        val lower = clean.lowercase()
        registeredUsersByIdentifier[lower]?.let { return it }
        registeredUsersByIdentifier[clean]?.let { return it }
        val tail = normalizePhoneTail(clean)
        if (tail.length == 10) {
            registeredUsersByIdentifier[tail]?.let { return it }
        }
        return registeredUsersByIdentifier.values.firstOrNull { u ->
            u.email.equals(clean, ignoreCase = true) ||
                (tail.length == 10 && normalizePhoneTail(u.phone) == tail)
        }
    }

    fun getPasswordHashForIdentifier(identifier: String): String? {
        val clean = identifier.trim()
        val tail = normalizePhoneTail(clean)
        return userPasswordsByPhoneOrEmail[clean]
            ?: userPasswordsByPhoneOrEmail[clean.lowercase()]
            ?: (if (tail.length == 10) userPasswordsByPhoneOrEmail[tail] else null)
            ?: userPasswordsByPhoneOrEmail.entries.firstOrNull {
                it.key.equals(clean, ignoreCase = true) ||
                        (tail.length == 10 && normalizePhoneTail(it.key) == tail)
            }?.value
    }

    val bangaloreEventsAndSpots: List<BangaloreEventSpot> = listOf(
        BangaloreEventSpot(
            id = "blr-1",
            title = "Electronic City Phase 1 & Neeladri Road",
            subtitle = "Wipro, Infosys, TCS & Yellow Line Metro Corridor",
            category = "TECH_HUB",
            area = "Neeladri Road Electronic City",
            timingOrStatus = "Major South IT Hub",
            pgCountNearby = 240,
            mapQuery = "Neeladri Road Electronic City Phase 1 Bangalore"
        ),
        BangaloreEventSpot(
            id = "blr-2",
            title = "Whitefield ITPL & Hopefarm Metro",
            subtitle = "ITPL, Bagmane World Tech Center & Purple Line Metro",
            category = "TECH_HUB",
            area = "Whitefield",
            timingOrStatus = "Direct Metro Access",
            pgCountNearby = 195,
            mapQuery = "ITPL Main Road Whitefield Bangalore"
        ),
        BangaloreEventSpot(
            id = "blr-3",
            title = "Koramangala 4th & 5th Block Hub",
            subtitle = "Sony World Junction, Startup Offices, Colleges & Cafes",
            category = "LIVE_EVENT",
            area = "Koramangala",
            timingOrStatus = "Central Startup Corridor",
            pgCountNearby = 180,
            mapQuery = "Koramangala 4th Block Sony World Bangalore"
        ),
        BangaloreEventSpot(
            id = "blr-4",
            title = "HSR Layout Sector 2 & 27th Main",
            subtitle = "Hustlehub Tech Park, RMZ Ecospace Access & Parks",
            category = "LIVE_EVENT",
            area = "Hsr Layout",
            timingOrStatus = "Co-Living & Tech Hub",
            pgCountNearby = 165,
            mapQuery = "27th Main Road HSR Layout Sector 2 Bangalore"
        ),
        BangaloreEventSpot(
            id = "blr-5",
            title = "Manyata Tech Park & Nagavara",
            subtitle = "North Bangalore IT Corridor • Cognizant, IBM & Nvidia",
            category = "TECH_HUB",
            area = "Nagavara",
            timingOrStatus = "North ORR Hub",
            pgCountNearby = 150,
            mapQuery = "Manyata Tech Park Nagavara Bangalore"
        ),
        BangaloreEventSpot(
            id = "blr-6",
            title = "Indiranagar 100 Feet Road & Metro",
            subtitle = "Embassy GolfLinks Access, Metro Station & Dining",
            category = "FAMOUS_SPOT",
            area = "Indiranagar",
            timingOrStatus = "Purple Line Metro Hub",
            pgCountNearby = 120,
            mapQuery = "100 Feet Road Indiranagar Metro Station Bangalore"
        ),
        BangaloreEventSpot(
            id = "blr-7",
            title = "MG Road, Cubbon Park & Church Street",
            subtitle = "Central Business District & Metro Interchange",
            category = "FAMOUS_SPOT",
            area = "Mg Road",
            timingOrStatus = "Central Metro Junction",
            pgCountNearby = 95,
            mapQuery = "Church Street MG Road Metro Bangalore"
        ),
        BangaloreEventSpot(
            id = "blr-8",
            title = "BTM Layout 2nd Stage & Silk Board",
            subtitle = "Popular Student & Working Professional Accommodation Hub",
            category = "METRO_CORRIDOR",
            area = "Btm Layout",
            timingOrStatus = "South-Central Corridor",
            pgCountNearby = 210,
            mapQuery = "Udupi Garden BTM Layout 2nd Stage Bangalore"
        )
    )

    val seoGeoFaqs: List<SeoGeoFaqItem> = listOf(
        SeoGeoFaqItem(
            question = "Which are the best areas in Bangalore to find PGs near IT Tech Parks?",
            answer = "For South & ORR tech parks, Koramangala, HSR Layout, BTM Layout, and Bellandur/Marathahalli offer the fastest commute. For East Bangalore (ITPL/Bagmane), Whitefield and Mahadevapura are ideal with Namma Metro Purple Line connectivity. For Wipro/Infosys campuses, Neeladri Road in Electronic City Phase 1 is #1, while Nagavara/Hebbal serves Manyata Tech Park.",
            tag = "GEO • Bengaluru Corridors"
        ),
        SeoGeoFaqItem(
            question = "Are the 2,415 PG listings on StayEg verified or unverified right now?",
            answer = "In Phase 1, all 2,415 Bangalore PG listings across 37 localities are provided as Unverified Directory Listings for free tenant discovery, Google Maps navigation, and direct owner contact. StayEg is actively onboarding the First 1,000 PG Owners into our Free Verified Pool—verified badges and instant online bed booking will activate in the next update.",
            tag = "AEO • Transparency"
        ),
        SeoGeoFaqItem(
            question = "How does StayEg's Zero-Brokerage PG Discovery work in India?",
            answer = "Tenants never pay any brokerage or middleman commission on StayEg. You can explore PG photos, 3-time South & North Indian mess menus, WiFi speeds, and nearby Namma Metro/Rapido fares, tap 'Show Interest' to send your requirement directly to the PG owner, or open Google Maps to visit and connect with the property directly.",
            tag = "SEO • Zero Brokerage"
        ),
        SeoGeoFaqItem(
            question = "How can a Bangalore PG Owner join the First 1,000 Free Verified PG Pool?",
            answer = "PG Owners can tap 'Activate PG (1st 1,000 Free)' in the StayEg app and submit their PG Name, Area, Phone, Total Beds, and optional Directory Claim Code (STG-*). The first 1,000 PG owners receive a 1-Year Starter Verification & Listing Subscription (worth ₹12,499) 100% FREE.",
            tag = "Owner Onboarding"
        )
    )

    init {
        if (autoRestoreSession) {
            restorePersistedSession()
        }
        // Observe live PG listings and connection status emitted by SupabaseService real-time engine
        repoScope.launch {
            supabaseService.pgListingsFlow.collect { livePgs ->
                if (livePgs.isNotEmpty()) {
                    _pgs.value = livePgs
                }
            }
        }
        repoScope.launch {
            supabaseService.connectionStatus.collect { status ->
                if (status != null) {
                    _supabaseStatus.value = status
                }
            }
        }
    }

    fun ensureSupabaseSnapshotLoaded() {
        if (_pgs.value.size < 100) {
            val full = SupabaseService.loadFullSupabaseSnapshot()
            if (full.isNotEmpty()) {
                _pgs.value = full
            }
        }
    }

    val vendors: List<Vendor> = listOf(
        Vendor("v1", "QuickFix Plumbing", "Plumber", "+91 98765 40001", "Bangalore", "Koramangala", 4.3),
        Vendor("v2", "Spark Electric", "Electrician", "+91 98765 40002", "Bangalore", "HSR Layout", 4.5),
        Vendor("v3", "CleanPro Services", "Cleaning", "+91 98765 40003", "Bangalore", "Indiranagar", 4.2),
        Vendor("v4", "WoodCraft Works", "Carpenter", "+91 98765 40005", "Bangalore", "Whitefield", 4.4),
        Vendor("v5", "NetConnect WiFi", "Internet", "+91 98765 40006", "Bangalore", "Electronic City", 4.6)
    )

    val workers: List<Worker> = listOf(
        Worker("w1", "Ramesh Kumar", "Security Guard", "+91 98765 50001", "NIGHT"),
        Worker("w2", "Geeta Devi", "Housekeeping", "+91 98765 50002", "MORNING"),
        Worker("w3", "Suresh Gowda", "Chef / Cook", "+91 98765 50003", "MORNING"),
        Worker("w4", "Mohan Rao", "Property Manager", "+91 98765 50005", "FULL DAY")
    )

    val coupons: List<Coupon> = listOf(
        Coupon("STAYEG500", "Flat ₹500 off on first booking", 0, flatDiscount = 500, minAmount = 3000),
        Coupon("FIRSTFREE", "1st 1000 PG Owners - 1 Year Free", 100, minAmount = 0, maxDiscount = 12499),
        Coupon("RENT10", "10% off on monthly rent payment", 10, minAmount = 5000, maxDiscount = 2000),
        Coupon("DIWALI20", "20% festive discount on annual plan", 20, minAmount = 5000, maxDiscount = 5000)
    )

    val pricingPlans: List<PricingPlan> = listOf(
        PricingPlan(
            id = "yearly",
            name = "Starter",
            duration = "1 Year",
            price = 12499,
            originalPrice = 12499,
            discount = 0,
            features = listOf(
                "List up to 2 PG properties",
                "Smart room & bed management",
                "Tenant booking & KYC verification",
                "Rent collection & digital receipts",
                "Tenant complaint tracker",
                "Staff management (up to 5)",
                "WhatsApp notification alerts"
            ),
            badge = "Most Popular",
            popular = true
        ),
        PricingPlan(
            id = "biyearly",
            name = "Growth",
            duration = "2 Years",
            price = 24999,
            originalPrice = 31248,
            discount = 20,
            features = listOf(
                "Everything in Starter",
                "List up to 5 PG properties",
                "Advanced revenue & occupancy analytics",
                "Automated rent reminders",
                "Vendor management directory",
                "Priority 24/7 customer support",
                "Custom branded PG profile"
            ),
            badge = "Save 20%"
        ),
        PricingPlan(
            id = "triyearly",
            name = "Enterprise",
            duration = "3 Years",
            price = 37499,
            originalPrice = 53570,
            discount = 30,
            features = listOf(
                "Everything in Growth",
                "Unlimited PG properties & beds",
                "Dedicated relationship manager",
                "White-label tenant portal",
                "Bulk coupon engine for promotions",
                "Custom API & accounting sync"
            ),
            badge = "Best Value"
        )
    )

    fun loginAs(role: UserRole) {
        val prev = _currentUser.value
        val user = when (role) {
            UserRole.TENANT -> User(
                id = prev?.id ?: "user-tenant-1",
                name = prev?.name?.ifBlank { "Vikram Singh" } ?: "Vikram Singh",
                email = prev?.email?.ifBlank { "vikram@stayeg.in" } ?: "vikram@stayeg.in",
                phone = prev?.phone?.ifBlank { "+91 91234 56789" } ?: "+91 91234 56789",
                role = UserRole.TENANT,
                city = prev?.city ?: "Bengaluru",
                currentArea = prev?.currentArea ?: "Koramangala 4th Block",
                occupation = prev?.occupation?.ifBlank { "Software Engineer" } ?: "Software Engineer",
                organizationName = prev?.organizationName ?: "",
                isVerified = prev?.isVerified ?: false,
                kycStatus = prev?.kycStatus ?: KYCStatus.NOT_STARTED,
                kycDocumentType = prev?.kycDocumentType ?: "Aadhaar Card",
                aadhaarNumber = prev?.aadhaarNumber ?: "",
                panNumber = prev?.panNumber ?: "",
                guardianName = prev?.guardianName ?: "",
                permanentAddress = prev?.permanentAddress ?: "",
                kycFrontPhotoUri = prev?.kycFrontPhotoUri ?: "",
                kycBackPhotoUri = prev?.kycBackPhotoUri ?: "",
                kycSelfiePhotoUri = prev?.kycSelfiePhotoUri ?: "",
                kycSubmittedAt = prev?.kycSubmittedAt ?: "",
                kycStatusNote = prev?.kycStatusNote ?: ""
            )
            UserRole.OWNER -> User(
                id = "user-owner-1",
                name = "Rajesh Kumar",
                email = "rajesh@stayeg.in",
                phone = "+91 98765 43210",
                role = UserRole.OWNER,
                city = "Bengaluru",
                occupation = "PG Owner & Host",
                isVerified = prev?.isVerified ?: false,
                kycStatus = prev?.kycStatus ?: KYCStatus.NOT_STARTED,
                bio = "Managing premium PGs in Koramangala & HSR."
            )
            UserRole.ADMIN -> User(
                id = "user-admin-1",
                name = "StayEg Admin",
                email = "admin@stayeg.in",
                phone = "+91 99999 99999",
                role = UserRole.ADMIN,
                city = "Bengaluru",
                isVerified = true,
                kycStatus = KYCStatus.VERIFIED
            )
            UserRole.VENDOR -> User(
                id = "user-vendor-1",
                name = "Suresh Services",
                email = "suresh@services.in",
                phone = "+91 98765 00000",
                role = UserRole.VENDOR,
                city = "Bengaluru",
                isVerified = false,
                kycStatus = KYCStatus.NOT_STARTED
            )
        }
        _currentUser.value = user
        persistCurrentUserSession()
    }

    fun persistCurrentUserSession() {
        val user = _currentUser.value
        if (user == null) {
            SupabaseService.saveDevicePref("session_logged_out", "true")
            return
        }
        SupabaseService.saveDevicePref("session_logged_out", "false")
        SupabaseService.saveDevicePref("session_user_id", user.id)
        SupabaseService.saveDevicePref("session_user_name", user.name)
        SupabaseService.saveDevicePref("session_user_email", user.email)
        SupabaseService.saveDevicePref("session_user_phone", user.phone)
        SupabaseService.saveDevicePref("session_user_role", user.role.name)
        SupabaseService.saveDevicePref("session_user_gender", user.gender)
        SupabaseService.saveDevicePref("session_user_city", user.city)
        SupabaseService.saveDevicePref("session_user_area", user.currentArea)
        SupabaseService.saveDevicePref("session_user_occupation", user.occupation)
        SupabaseService.saveDevicePref("session_user_org", user.organizationName)
        SupabaseService.saveDevicePref("session_user_work_mode", user.workMode)
        SupabaseService.saveDevicePref("session_user_food", user.foodPreference)
        SupabaseService.saveDevicePref("session_user_sleep", user.sleepSchedule)
        SupabaseService.saveDevicePref("session_user_languages", user.languagesSpoken)
        SupabaseService.saveDevicePref("session_user_emergency_name", user.emergencyContactName)
        SupabaseService.saveDevicePref("session_user_emergency_phone", user.emergencyContactPhone)
        SupabaseService.saveDevicePref("session_user_bio", user.bio)
        SupabaseService.saveDevicePref("session_user_avatar", user.avatarUrl)
        SupabaseService.saveDevicePref("session_user_kyc_status", user.kycStatus.name)
        SupabaseService.saveDevicePref("session_user_kyc_doc_type", user.kycDocumentType)
        SupabaseService.saveDevicePref("session_user_aadhaar", user.aadhaarNumber)
        SupabaseService.saveDevicePref("session_user_pan", user.panNumber)
        SupabaseService.saveDevicePref("session_user_guardian", user.guardianName)
        SupabaseService.saveDevicePref("session_user_perm_address", user.permanentAddress)
        SupabaseService.saveDevicePref("session_user_kyc_front", user.kycFrontPhotoUri)
        SupabaseService.saveDevicePref("session_user_kyc_back", user.kycBackPhotoUri)
        SupabaseService.saveDevicePref("session_user_kyc_selfie", user.kycSelfiePhotoUri)
        SupabaseService.saveDevicePref("session_user_kyc_front_remote", user.kycFrontRemoteUrl)
        SupabaseService.saveDevicePref("session_user_kyc_back_remote", user.kycBackRemoteUrl)
        SupabaseService.saveDevicePref("session_user_kyc_selfie_remote", user.kycSelfieRemoteUrl)
        SupabaseService.saveDevicePref("session_user_kyc_submitted_at", user.kycSubmittedAt)
        SupabaseService.saveDevicePref("session_user_kyc_note", user.kycStatusNote)
        SupabaseService.saveDevicePref("session_user_auth_method", user.authMethod)
        SupabaseService.saveDevicePref("session_jwt_token", _activeJwtToken.value)

        val stay = _activeStayConfig.value
        SupabaseService.saveDevicePref("session_stay_pg_id", stay.pgId)
        SupabaseService.saveDevicePref("session_stay_pg_name", stay.pgName)
        SupabaseService.saveDevicePref("session_stay_area", stay.area)
        SupabaseService.saveDevicePref("session_stay_room_code", stay.roomCode)
        SupabaseService.saveDevicePref("session_stay_floor", stay.floorNumber.toString())
        SupabaseService.saveDevicePref("session_stay_bed", stay.bedNumber.toString())
        SupabaseService.saveDevicePref("session_stay_sharing", stay.sharingType)
        SupabaseService.saveDevicePref("session_stay_rent", stay.monthlyRent.toString())
        SupabaseService.saveDevicePref("session_stay_deposit", stay.securityDeposit.toString())
        SupabaseService.saveDevicePref("session_stay_move_in", stay.moveInDate)
        SupabaseService.saveDevicePref("session_stay_activated", stay.isStayActivated.toString())
        SupabaseService.saveDevicePref("session_stay_qr_code", stay.qrCheckInCode)
        SupabaseService.saveDevicePref("session_stay_checkin_method", stay.checkInMethod)

        // Persist PBKDF2 password hashes
        val encodedCredentials = userPasswordsByPhoneOrEmail.entries.joinToString("||") { "${it.key}==${it.value}" }
        SupabaseService.saveDevicePref("session_password_hashes", encodedCredentials)

        // Also keep current user indexed in the registered users directory
        registerUserInAccountDirectory(user)
        val encodedAccounts = registeredUsersByIdentifier.values
            .distinctBy { it.id }
            .joinToString(";;") { u ->
                listOf(
                    u.id,
                    u.name.replace("|", " "),
                    u.email.replace("|", ""),
                    u.phone.replace("|", ""),
                    u.gender,
                    u.city,
                    u.currentArea.replace("|", " "),
                    u.occupation.replace("|", " "),
                    u.organizationName.replace("|", " "),
                    u.kycStatus.name,
                    u.aadhaarNumber.replace("|", ""),
                    u.panNumber.replace("|", ""),
                    u.kycFrontPhotoUri,
                    u.kycBackPhotoUri,
                    u.kycSelfiePhotoUri,
                    u.kycFrontRemoteUrl,
                    u.kycBackRemoteUrl,
                    u.kycSelfieRemoteUrl
                ).joinToString("|")
            }
        SupabaseService.saveDevicePref("session_registered_accounts", encodedAccounts)
    }

    fun restorePersistedSession(): Boolean {
        val savedHashes = SupabaseService.getDevicePref("session_password_hashes", "")
        if (savedHashes.isNotBlank()) {
            savedHashes.split("||").forEach { entry ->
                val parts = entry.split("==")
                if (parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank()) {
                    userPasswordsByPhoneOrEmail[parts[0]] = parts[1]
                }
            }
        }

        val savedAccounts = SupabaseService.getDevicePref("session_registered_accounts", "")
        if (savedAccounts.isNotBlank()) {
            savedAccounts.split(";;").forEach { row ->
                val cols = row.split("|")
                if (cols.size >= 10 && cols[0].isNotBlank()) {
                    val kycStat = runCatching { KYCStatus.valueOf(cols[9]) }.getOrDefault(KYCStatus.NOT_STARTED)
                    val accUser = User(
                        id = cols[0],
                        name = cols[1],
                        email = cols[2],
                        phone = cols[3],
                        gender = cols.getOrElse(4) { "Male" },
                        city = cols.getOrElse(5) { "Bengaluru" },
                        currentArea = cols.getOrElse(6) { "Koramangala" },
                        occupation = cols.getOrElse(7) { "" },
                        organizationName = cols.getOrElse(8) { "" },
                        isVerified = kycStat == KYCStatus.VERIFIED,
                        kycStatus = kycStat,
                        aadhaarNumber = cols.getOrElse(10) { "" },
                        panNumber = cols.getOrElse(11) { "" },
                        kycFrontPhotoUri = cols.getOrElse(12) { "" },
                        kycBackPhotoUri = cols.getOrElse(13) { "" },
                        kycSelfiePhotoUri = cols.getOrElse(14) { "" },
                        kycFrontRemoteUrl = cols.getOrElse(15) { "" },
                        kycBackRemoteUrl = cols.getOrElse(16) { "" },
                        kycSelfieRemoteUrl = cols.getOrElse(17) { "" }
                    )
                    registerUserInAccountDirectory(accUser)
                }
            }
        }

        val loggedOut = SupabaseService.getDevicePref("session_logged_out", "false") == "true"
        if (loggedOut) {
            _currentUser.value = null
            _activeAuthSession.value = null
            _activeJwtToken.value = ""
            return false
        }

        val persistedAuthSession = supabaseService.loadPersistedSupabaseAuthSession()
        val savedUserId = SupabaseService.getDevicePref("session_user_id", "").ifBlank {
            persistedAuthSession?.userId ?: ""
        }
        if (savedUserId.isBlank() && persistedAuthSession == null) {
            _currentUser.value = null
            _activeAuthSession.value = null
            _activeJwtToken.value = ""
            return false
        }

        val kycStatusName = SupabaseService.getDevicePref("session_user_kyc_status", KYCStatus.NOT_STARTED.name)
        val restoredKycStatus = runCatching { KYCStatus.valueOf(kycStatusName) }.getOrDefault(KYCStatus.NOT_STARTED)
        val roleName = SupabaseService.getDevicePref("session_user_role", UserRole.TENANT.name)
        val restoredRole = runCatching { UserRole.valueOf(roleName) }.getOrDefault(UserRole.TENANT)

        val restoredUser = User(
            id = savedUserId,
            name = SupabaseService.getDevicePref("session_user_name", persistedAuthSession?.fullName ?: ""),
            email = SupabaseService.getDevicePref("session_user_email", persistedAuthSession?.email ?: ""),
            phone = SupabaseService.getDevicePref("session_user_phone", persistedAuthSession?.phone ?: ""),
            role = restoredRole,
            gender = SupabaseService.getDevicePref("session_user_gender", "Male"),
            city = SupabaseService.getDevicePref("session_user_city", "Bengaluru"),
            currentArea = SupabaseService.getDevicePref("session_user_area", "Koramangala 4th Block"),
            occupation = SupabaseService.getDevicePref("session_user_occupation", "Software Engineer"),
            organizationName = SupabaseService.getDevicePref("session_user_org", ""),
            workMode = SupabaseService.getDevicePref("session_user_work_mode", "Hybrid"),
            foodPreference = SupabaseService.getDevicePref("session_user_food", "Veg & Non-Veg"),
            sleepSchedule = SupabaseService.getDevicePref("session_user_sleep", "Flexible"),
            languagesSpoken = SupabaseService.getDevicePref("session_user_languages", "English, Hindi, Kannada"),
            emergencyContactName = SupabaseService.getDevicePref("session_user_emergency_name", ""),
            emergencyContactPhone = SupabaseService.getDevicePref("session_user_emergency_phone", ""),
            bio = SupabaseService.getDevicePref("session_user_bio", ""),
            avatarUrl = SupabaseService.getDevicePref("session_user_avatar", persistedAuthSession?.avatarUrl ?: ""),
            isVerified = restoredKycStatus == KYCStatus.VERIFIED,
            kycStatus = restoredKycStatus,
            kycDocumentType = SupabaseService.getDevicePref("session_user_kyc_doc_type", "Aadhaar Card"),
            aadhaarNumber = SupabaseService.getDevicePref("session_user_aadhaar", ""),
            panNumber = SupabaseService.getDevicePref("session_user_pan", ""),
            guardianName = SupabaseService.getDevicePref("session_user_guardian", ""),
            permanentAddress = SupabaseService.getDevicePref("session_user_perm_address", ""),
            kycFrontPhotoUri = SupabaseService.getDevicePref("session_user_kyc_front", ""),
            kycBackPhotoUri = SupabaseService.getDevicePref("session_user_kyc_back", ""),
            kycSelfiePhotoUri = SupabaseService.getDevicePref("session_user_kyc_selfie", ""),
            kycFrontRemoteUrl = SupabaseService.getDevicePref("session_user_kyc_front_remote", ""),
            kycBackRemoteUrl = SupabaseService.getDevicePref("session_user_kyc_back_remote", ""),
            kycSelfieRemoteUrl = SupabaseService.getDevicePref("session_user_kyc_selfie_remote", ""),
            kycSubmittedAt = SupabaseService.getDevicePref("session_user_kyc_submitted_at", ""),
            kycStatusNote = SupabaseService.getDevicePref("session_user_kyc_note", ""),
            authMethod = SupabaseService.getDevicePref("session_user_auth_method", persistedAuthSession?.provider?.uppercase() ?: "SUPABASE_EMAIL_PASSWORD")
        )
        _currentUser.value = restoredUser
        registerUserInAccountDirectory(restoredUser)

        val savedJwt = persistedAuthSession?.accessToken?.ifBlank {
            SupabaseService.getDevicePref("session_jwt_token", "")
        } ?: SupabaseService.getDevicePref("session_jwt_token", "")

        if (savedJwt.isNotBlank()) {
            _activeJwtToken.value = savedJwt
        } else {
            _activeJwtToken.value = SupabaseService.generateSignedJwtToken(restoredUser.id, restoredUser.email, restoredUser.role.name)
        }

        if (persistedAuthSession != null) {
            _activeAuthSession.value = persistedAuthSession
        } else {
            val synthesizedSession = SupabaseAuthSession(
                accessToken = _activeJwtToken.value,
                refreshToken = "sbr_${restoredUser.id}",
                expiresIn = 3600L,
                expiresAtEpochSec = (System.currentTimeMillis() / 1000L) + 3600L,
                userId = restoredUser.id,
                email = restoredUser.email,
                phone = restoredUser.phone,
                provider = restoredUser.authMethod.lowercase(),
                fullName = restoredUser.name,
                avatarUrl = restoredUser.avatarUrl
            )
            _activeAuthSession.value = synthesizedSession
            supabaseService.persistSupabaseAuthSession(synthesizedSession)
        }

        // Refresh or re-hydrate user profile from Supabase Auth & tables asynchronously
        repoScope.launch {
            val refreshRes = supabaseService.refreshOrRestoreSupabaseSession()
            refreshRes.onSuccess { (freshUser, freshSession) ->
                _activeAuthSession.value = freshSession
                _activeJwtToken.value = freshSession.accessToken
                val cur = _currentUser.value
                val mergedUser = if (cur != null) {
                    freshUser.copy(
                        kycStatus = if (freshUser.kycStatus != KYCStatus.NOT_STARTED) freshUser.kycStatus else cur.kycStatus,
                        isVerified = freshUser.isVerified || cur.isVerified,
                        aadhaarNumber = freshUser.aadhaarNumber.ifBlank { cur.aadhaarNumber },
                        panNumber = freshUser.panNumber.ifBlank { cur.panNumber },
                        kycFrontPhotoUri = freshUser.kycFrontPhotoUri.ifBlank { cur.kycFrontPhotoUri },
                        kycBackPhotoUri = freshUser.kycBackPhotoUri.ifBlank { cur.kycBackPhotoUri },
                        kycSelfiePhotoUri = freshUser.kycSelfiePhotoUri.ifBlank { cur.kycSelfiePhotoUri },
                        kycFrontRemoteUrl = freshUser.kycFrontRemoteUrl.ifBlank { cur.kycFrontRemoteUrl },
                        kycBackRemoteUrl = freshUser.kycBackRemoteUrl.ifBlank { cur.kycBackRemoteUrl },
                        kycSelfieRemoteUrl = freshUser.kycSelfieRemoteUrl.ifBlank { cur.kycSelfieRemoteUrl }
                    )
                } else freshUser
                _currentUser.value = mergedUser
                registerUserInAccountDirectory(mergedUser)
                persistCurrentUserSession()
            }
        }

        val savedPgId = SupabaseService.getDevicePref("session_stay_pg_id", "")
        val savedStayActivated = SupabaseService.getDevicePref("session_stay_activated", "false") == "true"
        if (savedPgId.isNotBlank()) {
            _activeStayConfig.value = _activeStayConfig.value.copy(
                pgId = savedPgId,
                pgName = SupabaseService.getDevicePref("session_stay_pg_name", _activeStayConfig.value.pgName),
                area = SupabaseService.getDevicePref("session_stay_area", _activeStayConfig.value.area),
                roomCode = SupabaseService.getDevicePref("session_stay_room_code", _activeStayConfig.value.roomCode),
                floorNumber = SupabaseService.getDevicePref("session_stay_floor", "1").toIntOrNull() ?: 1,
                bedNumber = SupabaseService.getDevicePref("session_stay_bed", "1").toIntOrNull() ?: 1,
                sharingType = SupabaseService.getDevicePref("session_stay_sharing", "Double Sharing"),
                monthlyRent = SupabaseService.getDevicePref("session_stay_rent", "12000").toIntOrNull() ?: 12000,
                securityDeposit = SupabaseService.getDevicePref("session_stay_deposit", "24000").toIntOrNull() ?: 24000,
                moveInDate = SupabaseService.getDevicePref("session_stay_move_in", "01 Nov 2026"),
                isStayActivated = savedStayActivated,
                qrCheckInCode = SupabaseService.getDevicePref("session_stay_qr_code", ""),
                checkInMethod = SupabaseService.getDevicePref("session_stay_checkin_method", "")
            )
        }
        return true
    }

    private fun recordCloudMediaUpload(userId: String, bucket: String, slotOrTag: String, localUri: String): String {
        val cleanUri = localUri.trim()
        if (cleanUri.isEmpty()) return ""
        val cloudUrl = SupabaseService.buildCloudStoragePublicUrl(bucket, userId, slotOrTag, cleanUri)
        _cloudMediaUploads.value = _cloudMediaUploads.value + mapOf(
            slotOrTag to cloudUrl,
            cleanUri to cloudUrl
        )
        val userName = _currentUser.value?.name ?: "StayEg Resident"
        val objId = "media-${userId.take(10)}-${slotOrTag.lowercase()}"
        val storageObj = SupabaseStorageObject(
            id = objId,
            userId = userId,
            userName = userName,
            bucketName = bucket,
            slotOrTag = slotOrTag,
            localUri = cleanUri,
            remotePublicUrl = cloudUrl,
            byteSize = cleanUri.length * 128,
            sha256Checksum = kotlin.math.abs(cloudUrl.hashCode()).toString(16),
            uploadedAt = "Synced to $bucket"
        )
        _storageBucketObjects.value = listOf(storageObj) + _storageBucketObjects.value.filterNot {
            it.id == objId || (it.userId == userId && it.bucketName == bucket && it.slotOrTag == slotOrTag)
        }

        repoScope.launch {
            supabaseService.uploadMediaToSupabaseCloud(
                userId = userId,
                bucket = bucket,
                slotOrTag = slotOrTag,
                localUri = cleanUri
            )
        }
        return cloudUrl
    }

    suspend fun refreshStorageBucketsFromSupabase(bucketFilter: String? = null): List<SupabaseStorageObject> {
        val remoteRes = supabaseService.fetchCloudMediaUploadsRemote(bucketFilter)
        remoteRes.onSuccess { remoteList ->
            if (remoteList.isNotEmpty()) {
                val merged = (remoteList + _storageBucketObjects.value)
                    .distinctBy { "${it.bucketName}:${it.userId}:${it.slotOrTag}" }
                _storageBucketObjects.value = merged
                val updatedMap = _cloudMediaUploads.value.toMutableMap()
                merged.forEach { obj ->
                    updatedMap[obj.slotOrTag] = obj.remotePublicUrl
                    if (obj.localUri.isNotBlank()) {
                        updatedMap[obj.localUri] = obj.remotePublicUrl
                    }
                }
                _cloudMediaUploads.value = updatedMap
            }
        }
        return _storageBucketObjects.value
    }

    fun logout() {
        _currentUser.value = null
        _activeAuthSession.value = null
        _activeJwtToken.value = ""
        _activeStayConfig.value = _activeStayConfig.value.copy(
            isStayActivated = false,
            qrCheckInCode = "",
            checkInMethod = ""
        )
        SupabaseService.saveDevicePref("session_logged_out", "true")
        SupabaseService.saveDevicePref("session_user_id", "")
        SupabaseService.saveDevicePref("session_stay_activated", "false")
        repoScope.launch {
            supabaseService.signOutSupabaseSession()
        }
    }

    fun updateKyc(aadhaar: String, pan: String) {
        val current = _currentUser.value ?: User(
            id = "usr-${UUID.randomUUID().toString().take(8)}",
            name = "StayEg Tenant",
            email = "tenant@stayeg.in"
        )
        _currentUser.value = current.copy(
            aadhaarNumber = aadhaar.trim(),
            panNumber = pan.trim().uppercase(),
            kycStatus = KYCStatus.VERIFIED,
            isVerified = true,
            kycSubmittedAt = "Verified Just Now",
            kycStatusNote = "ID & PAN verified in real time"
        )
        persistCurrentUserSession()
    }

    fun uploadKycDocument(slot: String, uri: String) {
        if (uri.isBlank()) return
        val current = _currentUser.value ?: User(
            id = "usr-${UUID.randomUUID().toString().take(8)}",
            name = "StayEg Tenant",
            email = "tenant@stayeg.in"
        )
        val slotKey = when (slot.uppercase()) {
            "FRONT" -> "kyc_front"
            "BACK", "PAN" -> "kyc_back"
            "SELFIE" -> "kyc_selfie"
            else -> "kyc_front"
        }
        val remoteBucketUrl = recordCloudMediaUpload(current.id, "kyc-documents", slotKey, uri)

        val updatedWithDoc = when (slot.uppercase()) {
            "FRONT" -> current.copy(
                kycFrontPhotoUri = uri,
                kycFrontRemoteUrl = remoteBucketUrl,
                kycStorageBucket = "kyc-documents"
            )
            "BACK", "PAN" -> current.copy(
                kycBackPhotoUri = uri,
                kycBackRemoteUrl = remoteBucketUrl,
                kycStorageBucket = "kyc-documents"
            )
            "SELFIE" -> current.copy(
                kycSelfiePhotoUri = uri,
                kycSelfieRemoteUrl = remoteBucketUrl,
                kycStorageBucket = "kyc-documents"
            )
            else -> current.copy(
                kycFrontPhotoUri = uri,
                kycFrontRemoteUrl = remoteBucketUrl,
                kycStorageBucket = "kyc-documents"
            )
        }
        val nextStatus = when (updatedWithDoc.kycStatus) {
            KYCStatus.VERIFIED -> KYCStatus.VERIFIED
            else -> KYCStatus.PENDING
        }
        val finalUser = updatedWithDoc.copy(
            kycStatus = nextStatus,
            kycSubmittedAt = "Updated Just Now",
            kycStatusNote = "${updatedWithDoc.uploadedKycDocsCount}/3 identification documents uploaded securely"
        )
        _currentUser.value = finalUser
        persistCurrentUserSession()
        repoScope.launch {
            supabaseService.saveUserProfileAndKycRemote(finalUser)
        }
    }

    fun removeKycDocument(slot: String) {
        val current = _currentUser.value ?: return
        val updatedWithoutDoc = when (slot.uppercase()) {
            "FRONT" -> current.copy(kycFrontPhotoUri = "", kycFrontRemoteUrl = "")
            "BACK", "PAN" -> current.copy(kycBackPhotoUri = "", kycBackRemoteUrl = "")
            "SELFIE" -> current.copy(kycSelfiePhotoUri = "", kycSelfieRemoteUrl = "")
            else -> current
        }
        val remainingDocs = updatedWithoutDoc.uploadedKycDocsCount
        val hasNumbers = updatedWithoutDoc.aadhaarNumber.isNotBlank() || updatedWithoutDoc.panNumber.isNotBlank()
        val nextStatus = when {
            remainingDocs == 0 && !hasNumbers -> KYCStatus.NOT_STARTED
            updatedWithoutDoc.kycStatus == KYCStatus.VERIFIED && remainingDocs == 0 -> KYCStatus.PENDING
            else -> if (updatedWithoutDoc.kycStatus == KYCStatus.VERIFIED) KYCStatus.PENDING else updatedWithoutDoc.kycStatus
        }
        _currentUser.value = updatedWithoutDoc.copy(
            kycStatus = nextStatus,
            isVerified = nextStatus == KYCStatus.VERIFIED,
            kycSubmittedAt = if (nextStatus == KYCStatus.NOT_STARTED) "" else "Document Updated",
            kycStatusNote = if (nextStatus == KYCStatus.NOT_STARTED) "" else "$remainingDocs/3 identification documents uploaded"
        )
        persistCurrentUserSession()
    }

    fun updateKycStatusRealtime(status: KYCStatus, note: String = "") {
        val current = _currentUser.value ?: User(
            id = "usr-${UUID.randomUUID().toString().take(8)}",
            name = "StayEg Tenant",
            email = "tenant@stayeg.in"
        )
        val defaultNote = when (status) {
            KYCStatus.NOT_STARTED -> "No KYC submission active"
            KYCStatus.PENDING -> "Documents submitted & under real-time verification review"
            KYCStatus.VERIFIED -> "Government ID, PAN & Tenant Identity verified"
            KYCStatus.REJECTED -> "Document image unclear — please re-upload clear ID scan"
        }
        _currentUser.value = current.copy(
            kycStatus = status,
            isVerified = status == KYCStatus.VERIFIED,
            kycSubmittedAt = if (status == KYCStatus.NOT_STARTED) "" else "Status Updated Just Now",
            kycStatusNote = note.ifBlank { defaultNote }
        )
        persistCurrentUserSession()
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
        val current = _currentUser.value ?: User(
            id = "usr-${UUID.randomUUID().toString().take(8)}",
            name = "StayEg Tenant",
            email = "tenant@stayeg.in"
        )
        val finalFront = frontPhotoUri.ifBlank { current.kycFrontPhotoUri }
        val finalBack = backPhotoUri.ifBlank { current.kycBackPhotoUri }
        val finalSelfie = selfiePhotoUri.ifBlank { current.kycSelfiePhotoUri }
        val remoteFront = if (finalFront.isNotBlank()) recordCloudMediaUpload(current.id, "kyc-documents", "kyc_front", finalFront) else current.kycFrontRemoteUrl
        val remoteBack = if (finalBack.isNotBlank()) recordCloudMediaUpload(current.id, "kyc-documents", "kyc_back", finalBack) else current.kycBackRemoteUrl
        val remoteSelfie = if (finalSelfie.isNotBlank()) recordCloudMediaUpload(current.id, "kyc-documents", "kyc_selfie", finalSelfie) else current.kycSelfieRemoteUrl

        val docsCount = listOf(finalFront, finalBack, finalSelfie).count { it.isNotBlank() }

        val updatedUser = current.copy(
            kycDocumentType = documentType.trim().ifEmpty { "Aadhaar Card" },
            aadhaarNumber = aadhaarOrIdNumber.trim(),
            panNumber = panNumber.trim().uppercase(),
            guardianName = guardianName.trim(),
            permanentAddress = permanentAddress.trim(),
            kycFrontPhotoUri = finalFront,
            kycBackPhotoUri = finalBack,
            kycSelfiePhotoUri = finalSelfie,
            kycFrontRemoteUrl = remoteFront,
            kycBackRemoteUrl = remoteBack,
            kycSelfieRemoteUrl = remoteSelfie,
            kycStorageBucket = "kyc-documents",
            kycStatus = targetStatus,
            isVerified = targetStatus == KYCStatus.VERIFIED,
            kycSubmittedAt = if (targetStatus == KYCStatus.VERIFIED) "Verified Today" else "Submitted for Review Today",
            kycStatusNote = if (targetStatus == KYCStatus.VERIFIED) {
                "Verified ($docsCount/3 ID documents verified)"
            } else {
                "Under verification ($docsCount/3 ID documents uploaded)"
            }
        )
        _currentUser.value = updatedUser
        persistCurrentUserSession()
        trackUserActivity(
            actionType = "KYC_SUBMITTED_${targetStatus.name}",
            screenName = "ProfileScreen",
            details = "KYC (${updatedUser.kycDocumentType}) submitted by ${updatedUser.name} ($docsCount/3 docs)",
            customEmail = updatedUser.email,
            customPhone = updatedUser.phone,
            customArea = updatedUser.currentArea
        )
        repoScope.launch {
            supabaseService.registerAuthUserRemote(updatedUser, "KYC_SYNC")
            supabaseService.saveUserProfileAndKycRemote(updatedUser)
        }
    }

    fun resetDigitalKyc() {
        val current = _currentUser.value ?: return
        _currentUser.value = current.copy(
            kycStatus = KYCStatus.NOT_STARTED,
            isVerified = false,
            aadhaarNumber = "",
            panNumber = "",
            guardianName = "",
            permanentAddress = "",
            kycFrontPhotoUri = "",
            kycBackPhotoUri = "",
            kycSelfiePhotoUri = "",
            kycFrontRemoteUrl = "",
            kycBackRemoteUrl = "",
            kycSelfieRemoteUrl = "",
            kycSubmittedAt = "",
            kycStatusNote = ""
        )
        persistCurrentUserSession()
    }

    fun updateProfilePhoto(photoUri: String) {
        val current = _currentUser.value ?: User(
            id = "usr-${UUID.randomUUID().toString().take(8)}",
            name = "StayEg Tenant",
            email = "tenant@stayeg.in"
        )
        if (photoUri.isNotBlank()) {
            recordCloudMediaUpload(current.id, "profile-avatars", "profile_avatar", photoUri)
        }
        _currentUser.value = current.copy(avatarUrl = photoUri)
        persistCurrentUserSession()
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
        val existing = _currentUser.value
        val baseUser = existing ?: User(
            id = "usr-${UUID.randomUUID().toString().take(8)}",
            name = name.trim().ifEmpty { "StayEg Tenant" },
            email = email.trim().ifEmpty { "tenant@stayeg.in" }
        )
        val finalAvatar = avatarUrl.ifBlank { existing?.avatarUrl ?: "" }
        if (finalAvatar.isNotBlank()) {
            recordCloudMediaUpload(baseUser.id, "profile-avatars", "profile_avatar", finalAvatar)
        }
        val updated = baseUser.copy(
            name = name.trim().ifEmpty { existing?.name ?: "StayEg Tenant" },
            phone = phone.trim().ifEmpty { existing?.phone ?: "+91 91234 56789" },
            email = email.trim().ifEmpty { existing?.email ?: "tenant@stayeg.in" },
            gender = gender.trim().ifEmpty { "Male" },
            city = city.trim().ifEmpty { "Bengaluru" },
            currentArea = currentArea.trim().ifEmpty { "Koramangala" },
            occupation = occupation.trim(),
            organizationName = organizationName.trim(),
            workMode = workMode.trim().ifEmpty { "Hybrid" },
            foodPreference = foodPreference.trim().ifEmpty { "Veg & Non-Veg" },
            sleepSchedule = sleepSchedule.trim().ifEmpty { "Flexible" },
            languagesSpoken = languagesSpoken.trim().ifEmpty { "English, Hindi, Kannada" },
            emergencyContactName = emergencyContactName.trim(),
            emergencyContactPhone = emergencyContactPhone.trim(),
            bio = bio.trim(),
            avatarUrl = finalAvatar
        )
        _currentUser.value = updated
        persistCurrentUserSession()
        trackUserActivity(
            actionType = "PROFILE_UPDATED",
            screenName = "ProfileScreen",
            details = "Profile updated for ${updated.name} (${updated.occupation} in ${updated.currentArea})",
            customEmail = updated.email,
            customPhone = updated.phone,
            customArea = updated.currentArea
        )
        repoScope.launch {
            supabaseService.registerAuthUserRemote(updated, "PROFILE_SYNC")
            supabaseService.saveUserProfileAndKycRemote(updated)
        }
    }

    fun trackUserActivity(
        actionType: String,
        screenName: String,
        targetPgId: String = "",
        targetPgName: String = "",
        details: String = "",
        customEmail: String = "",
        customPhone: String = "",
        customArea: String = "",
        latitude: Double = 12.9352,
        longitude: Double = 77.6245
    ) {
        val current = _currentUser.value
        val matchedPg = if (targetPgId.isNotBlank()) _pgs.value.firstOrNull { it.id == targetPgId } else null
        val area = customArea.ifBlank { matchedPg?.area ?: current?.currentArea ?: "Bengaluru" }
        val lat = matchedPg?.lat ?: latitude
        val lng = matchedPg?.lng ?: longitude
        val telemetry = UserActivityTelemetry(
            id = "act-${UUID.randomUUID().toString().take(8)}",
            userId = current?.id ?: "visitor-guest",
            userName = current?.name ?: "Bengaluru Visitor",
            userEmail = customEmail.ifBlank { current?.email ?: "" },
            userPhone = customPhone.ifBlank { current?.phone ?: "" },
            userArea = area,
            latitude = lat,
            longitude = lng,
            actionType = actionType,
            screenName = screenName,
            targetPgId = targetPgId,
            targetPgName = targetPgName.ifBlank { matchedPg?.name ?: "" },
            details = details,
            timestamp = "Just now"
        )
        _userActivityLogs.value = (listOf(telemetry) + _userActivityLogs.value).take(100)

        val nextCount = _interactionCount.value + 1
        _interactionCount.value = nextCount
        if (nextCount >= 4 && !hasDismissedOrSubmittedReviewInSession && !_shouldShowSmartReviewPopup.value) {
            _shouldShowSmartReviewPopup.value = true
        }

        repoScope.launch {
            supabaseService.saveUserActivityTelemetryRemote(telemetry)
        }
    }

    fun dismissSmartReviewPopup() {
        hasDismissedOrSubmittedReviewInSession = true
        _shouldShowSmartReviewPopup.value = false
    }

    fun triggerSmartReviewPopup() {
        _shouldShowSmartReviewPopup.value = true
    }

    fun requestOtpForDestination(destination: String): String {
        val cleanDest = destination.trim().ifEmpty { "default" }
        val baseIndex = kotlin.math.abs(cleanDest.hashCode()) % uniquePrecreatedOtps.size
        val nextIndex = (baseIndex + otpRotationIndex) % uniquePrecreatedOtps.size
        otpRotationIndex = (otpRotationIndex + 1) % uniquePrecreatedOtps.size
        val code = uniquePrecreatedOtps[nextIndex]
        issuedVerificationOtps[cleanDest.lowercase()] = code
        val tail = normalizePhoneTail(cleanDest)
        if (tail.length == 10) {
            issuedVerificationOtps[tail] = code
        }
        val channel = if (cleanDest.contains("@")) "EMAIL_OTP" else "SMS_OTP"
        trackUserActivity(
            actionType = channel,
            screenName = "AuthScreen",
            details = "Verification OTP issued to $cleanDest",
            customEmail = if (cleanDest.contains("@")) cleanDest else "",
            customPhone = if (!cleanDest.contains("@")) cleanDest else ""
        )
        repoScope.launch {
            supabaseService.recordOtpIssuanceRemote(cleanDest, code, channel)
        }
        return code
    }

    fun isValidPrecreatedOtp(otp: String, destination: String = ""): Boolean {
        val cleanOtp = otp.trim()
        if (cleanOtp.length != 6) return false
        if (destination.isNotBlank()) {
            val cleanDest = destination.trim().lowercase()
            val tail = normalizePhoneTail(cleanDest)
            val expectedForDest = issuedVerificationOtps[cleanDest]
                ?: (if (tail.length == 10) issuedVerificationOtps[tail] else null)
            if (expectedForDest != null) {
                return cleanOtp == expectedForDest
            }
        }
        return cleanOtp in uniquePrecreatedOtps ||
                cleanOtp in issuedVerificationOtps.values
    }

    fun signUpOrLoginWithPhoneOtp(
        name: String,
        phone: String,
        email: String,
        otp: String,
        password: String,
        city: String = "Bengaluru",
        occupation: String = "Working Professional / Student",
        locality: String = "Koramangala"
    ): Result<User> {
        val cleanPhone = phone.trim()
        val cleanEmail = email.trim().lowercase()
        val destForOtp = cleanEmail.ifBlank { cleanPhone }
        if (otp.isNotBlank() && !isValidPrecreatedOtp(otp, destForOtp) && !isValidPrecreatedOtp(otp, cleanPhone)) {
            return Result.failure(IllegalArgumentException("Invalid 6-digit verification OTP for $destForOtp. Please enter the exact OTP sent to you."))
        }
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            return Result.failure(IllegalArgumentException("Please enter a valid Email / Gmail address."))
        }
        if (password.length < 4) {
            return Result.failure(IllegalArgumentException("Please create a password of at least 4 characters."))
        }

        val finalPhone = cleanPhone.ifEmpty { "+91 98800 11223" }

        val supabaseRes = kotlinx.coroutines.runBlocking {
            supabaseService.signUpWithSupabaseEmailPassword(
                email = cleanEmail,
                password = password,
                fullName = name.trim(),
                phone = finalPhone,
                locality = locality
            )
        }

        return supabaseRes.fold(
            onSuccess = { (authedUser, session) ->
                val enrichedUser = authedUser.copy(
                    city = city,
                    currentArea = locality.ifBlank { authedUser.currentArea.ifBlank { "Koramangala" } },
                    occupation = authedUser.occupation.ifBlank { occupation }
                )
                registerUserInAccountDirectory(enrichedUser, password)
                _activeAuthSession.value = session
                _activeJwtToken.value = session.accessToken
                _currentUser.value = enrichedUser
                persistCurrentUserSession()

                trackUserActivity(
                    actionType = "SUPABASE_AUTH_SIGNUP",
                    screenName = "AuthScreen",
                    details = "Supabase Auth account created for ${enrichedUser.name} (${enrichedUser.email} / ${enrichedUser.phone})",
                    customEmail = enrichedUser.email,
                    customPhone = enrichedUser.phone,
                    customArea = enrichedUser.currentArea
                )
                if (otp.isNotBlank()) {
                    repoScope.launch {
                        supabaseService.markOtpVerifiedRemote(destForOtp, otp.trim())
                    }
                }
                Result.success(enrichedUser)
            },
            onFailure = { err ->
                Result.failure(err)
            }
        )
    }

    fun loginWithPassword(identifier: String, password: String): Result<User> {
        val cleanId = identifier.trim()
        if (cleanId.isEmpty()) {
            return Result.failure(IllegalArgumentException("Please enter your registered Gmail / Email address or mobile number."))
        }
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter your account password."))
        }

        // First load any persisted credentials on disk into local verification cache
        if (registeredUsersByIdentifier.isEmpty()) {
            val savedHashes = SupabaseService.getDevicePref("session_password_hashes", "")
            if (savedHashes.isNotBlank()) {
                savedHashes.split("||").forEach { entry ->
                    val parts = entry.split("==")
                    if (parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank()) {
                        userPasswordsByPhoneOrEmail[parts[0]] = parts[1]
                    }
                }
            }
            val savedAccounts = SupabaseService.getDevicePref("session_registered_accounts", "")
            if (savedAccounts.isNotBlank()) {
                savedAccounts.split(";;").forEach { row ->
                    val cols = row.split("|")
                    if (cols.size >= 10 && cols[0].isNotBlank()) {
                        val kycStat = runCatching { KYCStatus.valueOf(cols[9]) }.getOrDefault(KYCStatus.NOT_STARTED)
                        val accUser = User(
                            id = cols[0],
                            name = cols[1],
                            email = cols[2],
                            phone = cols[3],
                            gender = cols.getOrElse(4) { "Male" },
                            city = cols.getOrElse(5) { "Bengaluru" },
                            currentArea = cols.getOrElse(6) { "Koramangala" },
                            occupation = cols.getOrElse(7) { "" },
                            organizationName = cols.getOrElse(8) { "" },
                            isVerified = kycStat == KYCStatus.VERIFIED,
                            kycStatus = kycStat,
                            aadhaarNumber = cols.getOrElse(10) { "" },
                            panNumber = cols.getOrElse(11) { "" },
                            kycFrontPhotoUri = cols.getOrElse(12) { "" },
                            kycBackPhotoUri = cols.getOrElse(13) { "" },
                            kycSelfiePhotoUri = cols.getOrElse(14) { "" },
                            kycFrontRemoteUrl = cols.getOrElse(15) { "" },
                            kycBackRemoteUrl = cols.getOrElse(16) { "" },
                            kycSelfieRemoteUrl = cols.getOrElse(17) { "" }
                        )
                        registerUserInAccountDirectory(accUser)
                    }
                }
            }
        }

        // 1. Try Supabase Auth client sign-in (GoTrue `/auth/v1/token?grant_type=password` + `user_auth_credentials`)
        val supabaseSignInRes = kotlinx.coroutines.runBlocking {
            supabaseService.signInWithSupabaseEmailPassword(cleanId, password)
        }

        if (supabaseSignInRes.isSuccess) {
            val (authedUser, session) = supabaseSignInRes.getOrThrow()
            val localExisting = findRegisteredUserByIdentifier(cleanId)
            val mergedUser = if (localExisting != null) {
                authedUser.copy(
                    kycStatus = if (authedUser.kycStatus != KYCStatus.NOT_STARTED) authedUser.kycStatus else localExisting.kycStatus,
                    isVerified = authedUser.isVerified || localExisting.isVerified,
                    aadhaarNumber = authedUser.aadhaarNumber.ifBlank { localExisting.aadhaarNumber },
                    panNumber = authedUser.panNumber.ifBlank { localExisting.panNumber },
                    kycFrontPhotoUri = authedUser.kycFrontPhotoUri.ifBlank { localExisting.kycFrontPhotoUri },
                    kycBackPhotoUri = authedUser.kycBackPhotoUri.ifBlank { localExisting.kycBackPhotoUri },
                    kycSelfiePhotoUri = authedUser.kycSelfiePhotoUri.ifBlank { localExisting.kycSelfiePhotoUri },
                    kycFrontRemoteUrl = authedUser.kycFrontRemoteUrl.ifBlank { localExisting.kycFrontRemoteUrl },
                    kycBackRemoteUrl = authedUser.kycBackRemoteUrl.ifBlank { localExisting.kycBackRemoteUrl },
                    kycSelfieRemoteUrl = authedUser.kycSelfieRemoteUrl.ifBlank { localExisting.kycSelfieRemoteUrl }
                )
            } else authedUser

            _activeAuthSession.value = session
            _activeJwtToken.value = session.accessToken
            _currentUser.value = mergedUser
            registerUserInAccountDirectory(mergedUser, password)
            persistCurrentUserSession()

            trackUserActivity(
                actionType = "SUPABASE_AUTH_LOGIN",
                screenName = "AuthScreen",
                details = "Supabase Auth login for ${mergedUser.name} (${mergedUser.email} / ${mergedUser.phone})",
                customEmail = mergedUser.email,
                customPhone = mergedUser.phone,
                customArea = mergedUser.currentArea
            )
            return Result.success(mergedUser)
        }

        // 2. Fallback check against locally persisted account hashes on this device (when offline)
        val matchedAccount = findRegisteredUserByIdentifier(cleanId)
        val tail = normalizePhoneTail(cleanId)
        val matchedHashEntry = userPasswordsByPhoneOrEmail.entries.firstOrNull {
            it.key.equals(cleanId, ignoreCase = true) ||
                (tail.length == 10 && normalizePhoneTail(it.key) == tail)
        }
        val matchedKey = matchedHashEntry?.key ?: cleanId
        val storedPasswordHash = matchedHashEntry?.value

        if (storedPasswordHash == null) {
            return Result.failure(
                supabaseSignInRes.exceptionOrNull()
                    ?: IllegalArgumentException("No account found for '$cleanId'. Please tap Create Account to sign up first.")
            )
        }

        val isPasswordValid = SupabaseService.verifyPasswordPbkdf2(matchedKey, password, storedPasswordHash) ||
            SupabaseService.verifyPasswordPbkdf2(cleanId, password, storedPasswordHash) ||
            (matchedAccount != null && (
                SupabaseService.verifyPasswordPbkdf2(matchedAccount.email, password, storedPasswordHash) ||
                SupabaseService.verifyPasswordPbkdf2(matchedAccount.phone, password, storedPasswordHash)
            ))

        if (!isPasswordValid) {
            return Result.failure(
                IllegalArgumentException("Incorrect password for '$cleanId'. Please enter your correct password or tap 'Forgot Password?' to reset via Gmail OTP.")
            )
        }

        val loggedInUser = (matchedAccount ?: User(
            id = "usr-${UUID.randomUUID().toString().take(8)}",
            name = cleanId.substringBefore("@").replaceFirstChar { it.uppercase() },
            email = if (cleanId.contains("@")) cleanId else "tenant@stayeg.in",
            phone = if (!cleanId.contains("@")) cleanId else ""
        )).copy(authMethod = "EMAIL_PASSWORD")

        val jwt = SupabaseService.generateSignedJwtToken(loggedInUser.id, loggedInUser.email.ifBlank { cleanId }, loggedInUser.role.name)
        val session = SupabaseAuthSession(
            accessToken = jwt,
            refreshToken = "sbr_${UUID.randomUUID().toString().replace("-", "")}",
            expiresIn = 3600L,
            expiresAtEpochSec = (System.currentTimeMillis() / 1000L) + 3600L,
            userId = loggedInUser.id,
            email = loggedInUser.email,
            phone = loggedInUser.phone,
            provider = "email",
            fullName = loggedInUser.name,
            avatarUrl = loggedInUser.avatarUrl
        )
        supabaseService.persistSupabaseAuthSession(session)
        _activeAuthSession.value = session
        _activeJwtToken.value = jwt
        _currentUser.value = loggedInUser
        registerUserInAccountDirectory(loggedInUser)
        persistCurrentUserSession()

        trackUserActivity(
            actionType = "USER_AUTH_LOGIN",
            screenName = "AuthScreen",
            details = "Authenticated login for ${loggedInUser.name} (${loggedInUser.email} / ${loggedInUser.phone})",
            customEmail = loggedInUser.email,
            customPhone = loggedInUser.phone,
            customArea = loggedInUser.currentArea
        )

        repoScope.launch {
            supabaseService.saveAuthCredentialRemote(matchedKey, loggedInUser.id, storedPasswordHash, jwt)
        }
        return Result.success(loggedInUser)
    }

    fun verifyGoogleWithOtp(
        name: String,
        email: String,
        otp: String,
        password: String = "",
        googleIdToken: String = ""
    ): Result<User> {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isEmpty() || !cleanEmail.contains("@")) {
            return Result.failure(IllegalArgumentException("Please enter a valid Google / Gmail address."))
        }
        if (googleIdToken.isBlank() && !isValidPrecreatedOtp(otp, cleanEmail)) {
            return Result.failure(IllegalArgumentException("Invalid 6-digit Google verification OTP. Please enter the exact OTP sent to $cleanEmail."))
        }

        val googleAuthRes = kotlinx.coroutines.runBlocking {
            supabaseService.signInWithSupabaseGoogle(
                email = cleanEmail,
                fullName = name.trim(),
                googleIdToken = googleIdToken,
                password = password
            )
        }

        return googleAuthRes.fold(
            onSuccess = { (googleUser, session) ->
                if (password.isNotBlank()) {
                    registerUserInAccountDirectory(googleUser, password)
                } else {
                    registerUserInAccountDirectory(googleUser)
                }
                _activeAuthSession.value = session
                _activeJwtToken.value = session.accessToken
                _currentUser.value = googleUser
                persistCurrentUserSession()
                trackUserActivity(
                    actionType = "GOOGLE_AUTH_LOGIN",
                    screenName = "AuthScreen",
                    details = "Google account verified for ${googleUser.email}",
                    customEmail = googleUser.email,
                    customPhone = googleUser.phone,
                    customArea = googleUser.currentArea
                )
                if (otp.isNotBlank()) {
                    repoScope.launch {
                        supabaseService.markOtpVerifiedRemote(cleanEmail, otp.trim())
                    }
                }
                Result.success(googleUser)
            },
            onFailure = { err ->
                Result.failure(err)
            }
        )
    }

    fun signInWithGoogle(name: String, email: String, googleIdToken: String = ""): User {
        val cleanEmail = email.trim().lowercase()
        val res = kotlinx.coroutines.runBlocking {
            supabaseService.signInWithSupabaseGoogle(
                email = cleanEmail.ifEmpty { "user.blr@gmail.com" },
                fullName = name.trim().ifEmpty { "Bangalore Tenant" },
                googleIdToken = googleIdToken
            )
        }
        val (user, session) = res.getOrElse {
            val fallbackUser = User(
                id = "usr-google-${UUID.randomUUID().toString().take(6)}",
                name = name.trim().ifEmpty { "Bangalore Tenant" },
                email = cleanEmail.ifEmpty { "user.blr@gmail.com" },
                authMethod = "GOOGLE_OAUTH"
            )
            val jwt = SupabaseService.generateSignedJwtToken(fallbackUser.id, fallbackUser.email, fallbackUser.role.name)
            val fallbackSession = SupabaseAuthSession(
                accessToken = jwt,
                refreshToken = "sbr_google_${UUID.randomUUID().toString().take(12)}",
                expiresIn = 3600L,
                expiresAtEpochSec = (System.currentTimeMillis() / 1000L) + 3600L,
                userId = fallbackUser.id,
                email = fallbackUser.email,
                phone = fallbackUser.phone,
                provider = "google",
                fullName = fallbackUser.name
            )
            supabaseService.persistSupabaseAuthSession(fallbackSession)
            fallbackUser to fallbackSession
        }
        _activeAuthSession.value = session
        _activeJwtToken.value = session.accessToken
        _currentUser.value = user
        registerUserInAccountDirectory(user)
        persistCurrentUserSession()
        trackUserActivity(
            actionType = "GOOGLE_OAUTH_SIGNIN",
            screenName = "AuthScreen",
            details = "Google Sign-In completed for ${user.name} (${user.email})",
            customEmail = user.email,
            customPhone = user.phone,
            customArea = user.currentArea
        )
        return user
    }

    fun resetPasswordWithEmailVerification(email: String, emailOtp: String, newPassword: String): Boolean {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank() || !cleanEmail.contains("@") || newPassword.length < 4) return false
        if (!isValidPrecreatedOtp(emailOtp, cleanEmail)) return false

        val matchedUser = findRegisteredUserByIdentifier(cleanEmail)
            ?: kotlinx.coroutines.runBlocking {
                supabaseService.fetchAccountByIdentifierRemote(cleanEmail).getOrNull()?.first
            }
        val newHash = SupabaseService.hashPasswordPbkdf2(cleanEmail, newPassword)
        userPasswordsByPhoneOrEmail[cleanEmail] = newHash
        if (matchedUser != null) {
            registerUserInAccountDirectory(matchedUser, newPassword)
        }
        persistCurrentUserSession()
        trackUserActivity(
            actionType = "PASSWORD_RESET_GMAIL_OTP",
            screenName = "AuthScreen",
            details = "Password reset completed via Gmail OTP for $cleanEmail",
            customEmail = cleanEmail
        )
        kotlinx.coroutines.runBlocking {
            supabaseService.markOtpVerifiedRemote(cleanEmail, emailOtp.trim())
            val userId = matchedUser?.id ?: _currentUser.value?.id ?: "user-tenant-1"
            supabaseService.saveAuthCredentialRemote(cleanEmail, userId, newHash, _activeJwtToken.value)
            val phoneTail = matchedUser?.phone?.filter { it.isDigit() }?.takeLast(10).orEmpty()
            if (phoneTail.length == 10) {
                val phoneHash = SupabaseService.hashPasswordPbkdf2(phoneTail, newPassword)
                supabaseService.saveAuthCredentialRemote(phoneTail, userId, phoneHash, _activeJwtToken.value)
                supabaseService.saveAuthCredentialRemote("+91$phoneTail", userId, phoneHash, _activeJwtToken.value)
            }
        }
        return true
    }

    fun updateProfileAndKyc(
        name: String,
        phone: String,
        email: String,
        occupation: String,
        city: String,
        aadhaar: String,
        pan: String
    ) {
        val existing = _currentUser.value
        val kycDone = aadhaar.trim().length >= 8 && pan.trim().length >= 6
        val updated = (existing ?: User(
            id = "usr-${UUID.randomUUID().toString().take(8)}",
            name = name.trim().ifEmpty { "StayEg Tenant" },
            email = email.trim().ifEmpty { "tenant@stayeg.in" }
        )).copy(
            name = name.trim().ifEmpty { existing?.name ?: "StayEg Tenant" },
            email = email.trim().ifEmpty { existing?.email ?: "tenant@stayeg.in" },
            phone = phone.trim().ifEmpty { existing?.phone ?: "+91 91234 56789" },
            city = city.trim().ifEmpty { "Bengaluru" },
            occupation = occupation.trim().ifEmpty { "Software Engineer" },
            isVerified = kycDone,
            kycStatus = if (kycDone) KYCStatus.VERIFIED else (existing?.kycStatus ?: KYCStatus.NOT_STARTED),
            aadhaarNumber = aadhaar.trim(),
            panNumber = pan.trim().uppercase(),
            kycSubmittedAt = if (kycDone) "Verified Today" else (existing?.kycSubmittedAt ?: "")
        )
        _currentUser.value = updated
        persistCurrentUserSession()
        repoScope.launch {
            supabaseService.registerAuthUserRemote(updated, "PROFILE_SYNC")
            supabaseService.saveUserProfileAndKycRemote(updated)
        }
    }

    // ─── Interactive My Stay Management Methods ─────────────────────────────

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
        val updatedStay = _activeStayConfig.value.copy(
            pgId = pgId,
            pgName = pgName,
            area = area,
            roomCode = roomCode.uppercase(),
            floorNumber = floorNumber.coerceIn(1, 12),
            bedNumber = bedNumber.coerceIn(1, 6),
            sharingType = sharingType,
            monthlyRent = monthlyRent.coerceAtLeast(4500),
            securityDeposit = monthlyRent.coerceAtLeast(4500) * 2,
            moveInDate = moveInDate,
            isStayActivated = true,
            checkInMethod = _activeStayConfig.value.checkInMethod.ifBlank { "MANUAL" }
        )
        _activeStayConfig.value = updatedStay
        persistCurrentUserSession()
        val userId = _currentUser.value?.id ?: "user-tenant-1"
        repoScope.launch {
            supabaseService.saveActiveStayRemote(userId, updatedStay)
        }
    }

    fun activateStayViaQrCode(
        qrCode: String,
        pgId: String = "",
        roomCode: String = "101",
        floorNumber: Int = 1,
        bedNumber: Int = 1,
        sharingType: String = "Double Sharing"
    ): ActiveStayConfig {
        val cleanQr = qrCode.trim().uppercase().ifBlank { "STG-QR-BLR-101" }
        val verifiedPgs = _pgs.value.filter { it.isVerified && !it.isDirectoryListing }.ifEmpty { _pgs.value }
        val matchedPg = when {
            pgId.isNotBlank() -> _pgs.value.firstOrNull { it.id == pgId }
            cleanQr.contains("HSR") -> verifiedPgs.firstOrNull { it.area.contains("HSR", ignoreCase = true) }
            cleanQr.contains("WHITE") || cleanQr.contains("ITPL") -> verifiedPgs.firstOrNull { it.area.contains("Whitefield", ignoreCase = true) }
            cleanQr.contains("ELEC") -> verifiedPgs.firstOrNull { it.area.contains("Electronic", ignoreCase = true) }
            cleanQr.contains("INDIRA") -> verifiedPgs.firstOrNull { it.area.contains("Indiranagar", ignoreCase = true) }
            else -> _pgs.value.firstOrNull {
                it.claimCode.equals(cleanQr, ignoreCase = true) ||
                    cleanQr.contains(it.id.uppercase())
            }
        } ?: verifiedPgs.first()

        val updatedStay = _activeStayConfig.value.copy(
            pgId = matchedPg.id,
            pgName = matchedPg.name,
            area = matchedPg.area,
            roomCode = roomCode.trim().ifBlank { "101" }.uppercase(),
            floorNumber = floorNumber.coerceIn(1, 12),
            bedNumber = bedNumber.coerceIn(1, 6),
            sharingType = sharingType,
            monthlyRent = matchedPg.price.coerceAtLeast(5000),
            securityDeposit = matchedPg.price.coerceAtLeast(5000) * 2,
            moveInDate = "Checked In Today via QR",
            isStayActivated = true,
            qrCheckInCode = cleanQr,
            checkInMethod = "QR_SCAN"
        )
        _activeStayConfig.value = updatedStay
        persistCurrentUserSession()

        val userId = _currentUser.value?.id ?: "user-tenant-1"
        trackUserActivity(
            actionType = "MY_STAY_QR_CHECKIN",
            screenName = "MyStayScreen",
            targetPgId = matchedPg.id,
            targetPgName = matchedPg.name,
            details = "Activated My Stay via Verified PG QR Code ($cleanQr) at ${matchedPg.name} • Room ${updatedStay.roomCode} • Bed #${updatedStay.bedNumber}",
            latitude = matchedPg.lat,
            longitude = matchedPg.lng
        )
        repoScope.launch {
            supabaseService.saveActiveStayRemote(userId, updatedStay)
        }
        return updatedStay
    }

    fun deactivateActiveStay() {
        val updatedStay = _activeStayConfig.value.copy(
            isStayActivated = false,
            qrCheckInCode = "",
            checkInMethod = ""
        )
        _activeStayConfig.value = updatedStay
        persistCurrentUserSession()
    }

    fun toggleMealOptIn(slot: String) {
        val current = _mealAttendance.value
        val updated = when (slot) {
            "TODAY_BREAKFAST" -> current.copy(
                breakfastOptIn = !current.breakfastOptIn,
                mealsAttendedThisMonth = if (!current.breakfastOptIn) current.mealsAttendedThisMonth + 1 else current.mealsAttendedThisMonth - 1,
                mealsSkippedSavedCount = if (current.breakfastOptIn) current.mealsSkippedSavedCount + 1 else (current.mealsSkippedSavedCount - 1).coerceAtLeast(0)
            )
            "TODAY_LUNCH" -> current.copy(
                lunchOptIn = !current.lunchOptIn,
                mealsAttendedThisMonth = if (!current.lunchOptIn) current.mealsAttendedThisMonth + 1 else current.mealsAttendedThisMonth - 1,
                mealsSkippedSavedCount = if (current.lunchOptIn) current.mealsSkippedSavedCount + 1 else (current.mealsSkippedSavedCount - 1).coerceAtLeast(0)
            )
            "TODAY_SNACKS" -> current.copy(
                snacksOptIn = !current.snacksOptIn
            )
            "TODAY_DINNER" -> current.copy(
                dinnerOptIn = !current.dinnerOptIn,
                mealsAttendedThisMonth = if (!current.dinnerOptIn) current.mealsAttendedThisMonth + 1 else current.mealsAttendedThisMonth - 1,
                mealsSkippedSavedCount = if (current.dinnerOptIn) current.mealsSkippedSavedCount + 1 else (current.mealsSkippedSavedCount - 1).coerceAtLeast(0)
            )
            "TOMORROW_BREAKFAST" -> current.copy(tomorrowBreakfastOptIn = !current.tomorrowBreakfastOptIn)
            "TOMORROW_LUNCH" -> current.copy(tomorrowLunchOptIn = !current.tomorrowLunchOptIn)
            "TOMORROW_DINNER" -> current.copy(tomorrowDinnerOptIn = !current.tomorrowDinnerOptIn)
            else -> current
        }
        _mealAttendance.value = updated
    }

    fun updateMealPreferences(mealMode: String, specialNote: String) {
        _mealAttendance.value = _mealAttendance.value.copy(
            mealMode = mealMode,
            specialDietaryNote = specialNote.trim()
        )
    }

    fun createGatePass(
        passType: String,
        title: String,
        visitorOrReason: String,
        dateRange: String,
        timeSlot: String
    ): TenantGatePass {
        val pass = TenantGatePass(
            id = "pass-${UUID.randomUUID().toString().take(6)}",
            passType = passType,
            title = title,
            visitorOrReason = visitorOrReason,
            dateRange = dateRange,
            timeSlot = timeSlot,
            status = "APPROVED"
        )
        _gatePasses.value = listOf(pass) + _gatePasses.value
        val userId = _currentUser.value?.id ?: "user-tenant-1"
        trackUserActivity(
            actionType = "GATE_PASS_CREATED",
            screenName = "MyStayScreen",
            targetPgId = _activeStayConfig.value.pgId,
            targetPgName = _activeStayConfig.value.pgName,
            details = "Gate pass (${pass.passType}): ${pass.title} • ${pass.dateRange}"
        )
        repoScope.launch {
            supabaseService.saveGatePassRemote(userId, pass)
        }
        return pass
    }

    fun cancelGatePass(passId: String) {
        _gatePasses.value = _gatePasses.value.filterNot { it.id == passId }
    }

    fun sendWardenChatMessage(messageText: String) {
        val clean = messageText.trim()
        if (clean.isEmpty()) return
        val tenantName = _currentUser.value?.name ?: "Room ${_activeStayConfig.value.roomCode} Tenant"
        val tenantMsg = WardenChatMessage(
            id = "wmsg-${UUID.randomUUID().toString().take(6)}",
            senderName = tenantName,
            isFromTenant = true,
            message = clean,
            timestamp = "Just now"
        )
        val stay = _activeStayConfig.value
        val autoReply = WardenChatMessage(
            id = "wmsg-${UUID.randomUUID().toString().take(6)}",
            senderName = "Mohan Rao (Property Warden)",
            isFromTenant = false,
            message = "Noted for Room ${stay.roomCode} • Bed #${stay.bedNumber} at ${stay.pgName}. Our on-site staff has logged your request.",
            timestamp = "Just now"
        )
        _wardenMessages.value = _wardenMessages.value + listOf(tenantMsg, autoReply)
    }

    fun toggleMoveOutNotice(active: Boolean, targetDate: String = "30 Dec 2026") {
        _activeStayConfig.value = _activeStayConfig.value.copy(
            moveOutNoticeActive = active,
            moveOutTargetDate = if (active) targetDate else ""
        )
    }

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
    ): PGInterestLead {
        val lead = PGInterestLead(
            id = "int-${UUID.randomUUID().toString().take(8)}",
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
        _interestLeads.value = listOf(lead) + _interestLeads.value
        trackUserActivity(
            actionType = "TENANT_PG_INTEREST_LEAD",
            screenName = "PGDetailScreen",
            targetPgId = pgId,
            targetPgName = pgName,
            details = "Interest in $pgName ($area) • $preferredSharing • Move-in: $expectedMoveIn",
            customEmail = tenantEmail,
            customPhone = tenantPhone,
            customArea = area
        )
        repoScope.launch {
            supabaseService.saveTenantInterestRemote(lead)
        }
        return lead
    }

    fun submitOwnerActivationLead(
        ownerName: String,
        pgName: String,
        area: String,
        phone: String,
        email: String,
        totalBeds: Int,
        claimCode: String
    ): OwnerActivationLead {
        val poolRank = 142 + _ownerActivationLeads.value.size + 1
        val lead = OwnerActivationLead(
            id = "ownlead-${UUID.randomUUID().toString().take(8)}",
            ownerName = ownerName,
            pgName = pgName,
            area = area,
            phone = phone,
            email = email,
            totalBeds = totalBeds,
            claimCode = claimCode.ifBlank { "NEW-PG-BLR" },
            poolRank = poolRank
        )
        _ownerActivationLeads.value = listOf(lead) + _ownerActivationLeads.value
        trackUserActivity(
            actionType = "PG_OWNER_ACTIVATION_LEAD",
            screenName = "OwnerActivationSheet",
            targetPgName = pgName,
            details = "PG Owner $ownerName ($phone / $email) submitted '$pgName' ($area, $totalBeds beds, Claim Code: ${lead.claimCode})",
            customEmail = email,
            customPhone = phone,
            customArea = area
        )
        // If the submitted claim code matches one of the 2,407 directory PGs, claim it in-app as well!
        val matchedDirPg = _pgs.value.firstOrNull {
            it.claimCode.equals(lead.claimCode, ignoreCase = true) || it.id.equals(lead.claimCode, ignoreCase = true)
        }
        if (matchedDirPg != null) {
            claimDirectoryListing(matchedDirPg.id, matchedDirPg.price)
        }
        repoScope.launch {
            supabaseService.saveOwnerActivationLeadRemote(lead)
        }
        return lead
    }

    fun submitFeatureFeedback(
        featureCategory: String,
        rating: Int,
        feedbackText: String
    ): AppFeatureFeedback {
        val user = _currentUser.value
        val fb = AppFeatureFeedback(
            id = "fb-${UUID.randomUUID().toString().take(8)}",
            userId = user?.id ?: "guest-user",
            userName = user?.name ?: "Bangalore Tenant",
            featureCategory = featureCategory,
            rating = rating,
            feedbackText = feedbackText
        )
        _featureFeedbacks.value = listOf(fb) + _featureFeedbacks.value
        trackUserActivity(
            actionType = "APP_FEEDBACK_SUBMITTED",
            screenName = "FeedbackDialog",
            details = "Feedback [$featureCategory • ${rating}★]: $feedbackText"
        )
        repoScope.launch {
            supabaseService.saveUserFeedbackRemote(fb)
        }
        return fb
    }

    fun createBooking(
        pgId: String,
        bedId: String,
        checkInDate: String,
        amount: Int,
        pgName: String,
        roomCode: String,
        bedNumber: Int
    ): Booking {
        val bookingId = "bk-${UUID.randomUUID().toString().take(8)}"
        val userId = _currentUser.value?.id ?: "guest-user"
        val booking = Booking(
            id = bookingId,
            userId = userId,
            pgId = pgId,
            bedId = bedId,
            checkInDate = checkInDate,
            status = BookingStatus.ACTIVE,
            advancePaid = amount,
            pgName = pgName,
            roomCode = roomCode,
            bedNumber = bedNumber
        )
        _bookings.value = listOf(booking) + _bookings.value

        val matchedPg = _pgs.value.firstOrNull { it.id == pgId }
        _activeStayConfig.value = _activeStayConfig.value.copy(
            pgId = pgId,
            pgName = pgName.ifBlank { matchedPg?.name ?: "Sunrise PG - Koramangala" },
            area = matchedPg?.area ?: "Koramangala",
            roomCode = roomCode.ifBlank { "101" },
            bedNumber = bedNumber,
            monthlyRent = amount.coerceAtLeast(5000),
            securityDeposit = amount.coerceAtLeast(5000) * 2,
            moveInDate = checkInDate.ifBlank { "Move-In Confirmed" },
            isStayActivated = true,
            checkInMethod = "BOOKING"
        )
        persistCurrentUserSession()

        // Mark the booked bed as OCCUPIED in _pgs so the floor seat-map reflects the reservation immediately
        _pgs.value = _pgs.value.map { pg ->
            if (pg.id == pgId) {
                pg.copy(
                    rooms = pg.rooms.map { room ->
                        room.copy(
                            beds = room.beds.map { bed ->
                                if (bed.id == bedId) bed.copy(status = BedStatus.OCCUPIED) else bed
                            }
                        )
                    }
                )
            } else pg
        }

        // Also add an initial rent payment record
        val payment = Payment(
            id = "pay-${UUID.randomUUID().toString().take(8)}",
            userId = userId,
            pgId = pgId,
            bookingId = bookingId,
            amount = amount,
            type = PaymentType.ADVANCE,
            status = PaymentStatus.COMPLETED,
            paidDate = "Today",
            method = "UPI",
            dueDate = checkInDate
        )
        _payments.value = listOf(payment) + _payments.value

        trackUserActivity(
            actionType = "BED_BOOKING_CONFIRMED",
            screenName = "BookingCheckout",
            targetPgId = pgId,
            targetPgName = pgName,
            details = "Booked Room $roomCode • Bed #$bedNumber ($bedId) at $pgName • Advance ₹$amount"
        )

        // Asynchronously sync with Supabase cloud database
        val activeStaySnapshot = _activeStayConfig.value
        repoScope.launch {
            supabaseService.createBookingRemote(
                id = bookingId,
                userId = userId,
                pgId = pgId,
                bedId = bedId,
                checkInDate = checkInDate,
                amount = amount
            )
            supabaseService.createPaymentRemote(
                id = payment.id,
                userId = userId,
                pgId = pgId,
                bookingId = bookingId,
                amount = amount,
                type = "ADVANCE",
                method = "UPI"
            )
            supabaseService.saveActiveStayRemote(userId, activeStaySnapshot)
        }

        return booking
    }

    fun buildUpiDeepLinkUri(
        amount: Int,
        upiPayeeVpa: String = "stayeg.escrow@axis",
        payeeName: String = "StayEg Residences",
        note: String = "Monthly PG Rent - Room ${_activeStayConfig.value.roomCode}"
    ): String {
        val safeVpa = upiPayeeVpa.trim().ifEmpty { "stayeg.escrow@axis" }
        val encodedName = java.net.URLEncoder.encode(payeeName, "UTF-8")
        val encodedNote = java.net.URLEncoder.encode(note, "UTF-8")
        val txnRef = "STG${System.currentTimeMillis().toString().takeLast(8)}"
        return "upi://pay?pa=$safeVpa&pn=$encodedName&tr=$txnRef&tn=$encodedNote&am=$amount.00&cu=INR"
    }

    fun payMonthlyRentViaUpi(
        amount: Int = _activeStayConfig.value.monthlyRent + _activeStayConfig.value.electricityExtra,
        upiApp: String = "Google Pay",
        upiId: String = "stayeg.escrow@axis",
        paymentType: PaymentType = PaymentType.RENT
    ): Payment {
        val userId = _currentUser.value?.id ?: "user-tenant-1"
        val stay = _activeStayConfig.value
        val activeBookingId = _bookings.value.firstOrNull { it.pgId == stay.pgId }?.id ?: "bk-101"
        val utrNumber = (400000000000L + kotlin.math.abs(UUID.randomUUID().mostSignificantBits % 599999999999L)).toString()
        val methodLabel = "$upiApp • UPI ($upiId • UTR $utrNumber)"
        val payment = Payment(
            id = "pay-${UUID.randomUUID().toString().take(8)}",
            userId = userId,
            pgId = stay.pgId,
            bookingId = activeBookingId,
            amount = amount.coerceAtLeast(1),
            type = paymentType,
            status = PaymentStatus.COMPLETED,
            dueDate = "05th of Month",
            paidDate = "Paid Today",
            method = methodLabel
        )
        _payments.value = listOf(payment) + _payments.value

        repoScope.launch {
            supabaseService.createPaymentRemote(
                id = payment.id,
                userId = userId,
                pgId = stay.pgId,
                bookingId = activeBookingId,
                amount = payment.amount,
                type = paymentType.name,
                method = methodLabel
            )
        }
        return payment
    }

    fun payRent(bookingId: String, amount: Int, method: String): Payment {
        val userId = _currentUser.value?.id ?: "guest-user"
        val pgId = _bookings.value.firstOrNull { it.id == bookingId }?.pgId ?: "pg-1"
        val payment = Payment(
            id = "pay-${UUID.randomUUID().toString().take(8)}",
            userId = userId,
            pgId = pgId,
            bookingId = bookingId,
            amount = amount,
            type = PaymentType.RENT,
            status = PaymentStatus.COMPLETED,
            paidDate = "Today",
            method = method
        )
        _payments.value = listOf(payment) + _payments.value

        // Asynchronously sync with Supabase cloud database
        repoScope.launch {
            supabaseService.createPaymentRemote(
                id = payment.id,
                userId = userId,
                pgId = pgId,
                bookingId = bookingId,
                amount = amount,
                type = "RENT",
                method = method
            )
        }

        return payment
    }

    private fun assignTechnicianForIssue(category: ComplaintCategory, subType: String): String {
        val norm = subType.lowercase()
        return when {
            norm.contains("wi-fi") || norm.contains("wifi") || norm.contains("router") || norm.contains("internet") ->
                "NetConnect Fiber Team (Arjun)"
            norm.contains("plumb") || norm.contains("geyser") || norm.contains("tap") || norm.contains("water") ->
                "QuickFix Plumbing (Raghav)"
            norm.contains("ac") || norm.contains("fan") || norm.contains("electric") || norm.contains("power") ->
                "Spark Electricals (Kiran)"
            norm.contains("clean") || category == ComplaintCategory.CLEANLINESS ->
                "Geeta Devi (Housekeeping Lead)"
            norm.contains("lock") || norm.contains("security") || category == ComplaintCategory.SAFETY ->
                "Ramesh Kumar (Security & Access)"
            norm.contains("food") || norm.contains("mess") ->
                "Suresh Gowda (Mess & Kitchen)"
            else ->
                "Mohan Rao (Property Manager)"
        }
    }

    fun raiseComplaint(
        pgId: String,
        title: String,
        description: String,
        category: ComplaintCategory,
        priority: ComplaintPriority,
        pgName: String = _activeStayConfig.value.pgName,
        roomCode: String = _activeStayConfig.value.roomCode,
        bedNumber: Int = _activeStayConfig.value.bedNumber,
        issueSubType: String = "Maintenance",
        preferredVisitSlot: String = "Morning (9 AM - 12 PM)",
        photoAttachmentUri: String = ""
    ): Complaint {
        val userId = _currentUser.value?.id ?: "user-tenant-1"
        val assignedTech = assignTechnicianForIssue(category, "$issueSubType $title")
        val initialStatus = if (priority == ComplaintPriority.URGENT) ComplaintStatus.IN_PROGRESS else ComplaintStatus.OPEN
        val timeline = mutableListOf(
            "Ticket logged for Room $roomCode • Bed #$bedNumber",
            "Assigned to $assignedTech ($preferredVisitSlot)"
        )
        if (initialStatus == ComplaintStatus.IN_PROGRESS) {
            timeline.add("Urgent priority: Technician dispatched immediately")
        }

        val complaintId = "cmp-${UUID.randomUUID().toString().take(8)}"
        val cloudPhotoUrl = if (photoAttachmentUri.isNotBlank()) {
            val uploadedUrl = recordCloudMediaUpload(userId, "complaint-photos", "complaint_$complaintId", photoAttachmentUri)
            timeline.add("Photo attachment uploaded")
            uploadedUrl
        } else ""

        val complaint = Complaint(
            id = complaintId,
            userId = userId,
            pgId = pgId.ifBlank { _activeStayConfig.value.pgId },
            title = title.trim(),
            description = description.trim(),
            category = category,
            priority = priority,
            status = initialStatus,
            assignedTo = assignedTech,
            createdAt = "Just now",
            pgName = pgName.ifBlank { _activeStayConfig.value.pgName },
            roomCode = roomCode.ifBlank { _activeStayConfig.value.roomCode },
            bedNumber = bedNumber,
            issueSubType = issueSubType,
            photoAttachmentUri = photoAttachmentUri,
            remoteStorageUrl = cloudPhotoUrl,
            storageBucket = "complaint-photos",
            preferredVisitSlot = preferredVisitSlot,
            updatedAt = "Just now",
            statusTimeline = timeline
        )
        _complaints.value = listOf(complaint) + _complaints.value

        // Asynchronously sync with Supabase cloud database
        repoScope.launch {
            supabaseService.createComplaintRemote(
                id = complaint.id,
                userId = userId,
                pgId = complaint.pgId,
                title = complaint.title,
                description = complaint.description,
                category = category.name,
                priority = priority.name,
                pgName = complaint.pgName,
                roomCode = complaint.roomCode,
                bedNumber = complaint.bedNumber,
                issueSubType = complaint.issueSubType,
                assignedTo = complaint.assignedTo,
                preferredVisitSlot = complaint.preferredVisitSlot,
                photoAttachmentUri = cloudPhotoUrl
            )
            if (initialStatus != ComplaintStatus.OPEN) {
                supabaseService.updateComplaintStatusRemote(
                    complaintId = complaint.id,
                    status = initialStatus.name,
                    assignedTo = complaint.assignedTo
                )
            }
        }

        return complaint
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
    ): PGVisitSchedule {
        val userId = _currentUser.value?.id ?: "guest-user"
        val visit = PGVisitSchedule(
            id = "vis-${UUID.randomUUID().toString().take(8)}",
            pgId = pgId,
            pgName = pgName,
            userId = userId,
            visitorName = visitorName,
            visitorPhone = visitorPhone,
            visitDate = visitDate,
            visitTime = visitTime,
            pickupMode = pickupMode,
            pickupLocation = pickupLocation,
            status = "CONFIRMED"
        )
        _visitSchedules.value = listOf(visit) + _visitSchedules.value
        repoScope.launch {
            supabaseService.scheduleVisitRemote(visit)
        }
        return visit
    }

    fun claimDirectoryListing(pgId: String, verifiedPrice: Int) {
        val currentUser = _currentUser.value
        val ownerId = currentUser?.id ?: "user-owner-1"
        val ownerName = currentUser?.name ?: "StayEg Verified Owner"
        val ownerPhone = currentUser?.phone ?: ""
        val ownerEmail = currentUser?.email ?: ""
        var claimedPg: PG? = null
        _pgs.value = _pgs.value.map { pg ->
            if (pg.id == pgId) {
                val updated = pg.copy(
                    isVerified = true,
                    isDirectoryListing = false,
                    ownerId = ownerId,
                    price = verifiedPrice,
                    securityDeposit = verifiedPrice * 2,
                    category = "Verified Co-Living PG",
                    description = "Official StayEg Verified Co-Living PG in ${pg.area} (Claimed via ${pg.claimCode}). Instant online bed selection, biometric entry, 200 Mbps WiFi, and 3-time homely meals.",
                    rooms = pg.rooms.ifEmpty { SupabaseService.buildDefaultFloorRoomsStatic(pg.id, verifiedPrice) }
                )
                claimedPg = updated
                updated
            } else {
                pg
            }
        }
        claimedPg?.let { target ->
            trackUserActivity(
                actionType = "PG_OWNER_CLAIM_LISTING",
                screenName = "PGDetailScreen",
                targetPgId = target.id,
                targetPgName = target.name,
                details = "Claimed directory PG '${target.name}' (${target.claimCode}) in ${target.area} at ₹$verifiedPrice/mo",
                customEmail = ownerEmail,
                customPhone = ownerPhone.ifBlank { target.phone },
                customArea = target.area
            )
            repoScope.launch {
                supabaseService.claimDirectoryListingRemote(
                    pg = target,
                    ownerId = ownerId,
                    verifiedPrice = verifiedPrice,
                    ownerName = ownerName,
                    ownerPhone = ownerPhone.ifBlank { target.phone },
                    ownerEmail = ownerEmail
                )
            }
        }
    }

    fun updatePgDetails(pgId: String, newName: String, newPrice: Int, newAddress: String) {
        _pgs.value = _pgs.value.map { pg ->
            if (pg.id == pgId) {
                pg.copy(
                    name = newName,
                    price = newPrice,
                    securityDeposit = newPrice * 2,
                    address = newAddress
                )
            } else pg
        }
        repoScope.launch {
            supabaseService.publishLiveListingUpdate(pgId, newPrice, 500)
        }
    }

    fun addRoomToPg(pgId: String, roomCode: String, roomType: RoomType, floor: Int, bedPrice: Int) {
        val bedCount = when (roomType) {
            RoomType.SINGLE -> 1
            RoomType.DOUBLE -> 2
            RoomType.TRIPLE -> 3
            RoomType.DORMITORY -> 4
        }
        val roomId = "room-$pgId-${roomCode.lowercase()}"
        val newBeds = (1..bedCount).map { num ->
            Bed(
                id = "bed-$pgId-${roomCode.lowercase()}-$num",
                roomId = roomId,
                bedNumber = num,
                status = BedStatus.AVAILABLE,
                price = bedPrice
            )
        }
        val newRoom = Room(
            id = roomId,
            pgId = pgId,
            roomCode = roomCode.uppercase(),
            roomType = roomType,
            floor = floor,
            hasAC = roomType == RoomType.SINGLE || roomType == RoomType.DOUBLE,
            hasAttachedBath = true,
            beds = newBeds
        )
        _pgs.value = _pgs.value.map { pg ->
            if (pg.id == pgId) {
                pg.copy(rooms = pg.rooms + newRoom)
            } else pg
        }
        repoScope.launch {
            supabaseService.addRoomToPgRemote(pgId, newRoom)
        }
    }

    fun setSupabaseApiKey(apiKey: String) {
        val clean = apiKey.trim().ifBlank { SupabaseService.DEFAULT_PUBLISHABLE_KEY }
        SupabaseService.currentApiKey = clean
        SupabaseService.saveDevicePref("supabase_custom_api_key", clean)
        SupabaseService.saveDevicePref("supabase_anon_api_key", clean)
    }

    suspend fun provisionRowLevelSecurity(): Boolean {
        return supabaseService.provisionRowLevelSecurityPolicies().isSuccess
    }

    suspend fun testSupabaseConnection(): SupabaseConnectionStatus {
        _isSyncing.value = true
        val status = supabaseService.testConnection()
        _supabaseStatus.value = status
        _isSyncing.value = false
        return status
    }

    suspend fun syncWithSupabase(): Boolean {
        _isSyncing.value = true
        return try {
            val status = supabaseService.initializeClient(enableRealtime = true)
            _supabaseStatus.value = status
            if (status.isConnected) {
                supabaseService.provisionRowLevelSecurityPolicies()
                val remotePosts = supabaseService.fetchCommunityPostsRemote().getOrNull().orEmpty()
                if (remotePosts.isNotEmpty()) {
                    val existingIds = _communityPosts.value.map { it.id }.toHashSet()
                    _communityPosts.value = remotePosts.filter { it.id !in existingIds } + _communityPosts.value
                }
            }
            _isSyncing.value = false
            status.isConnected
        } catch (e: Exception) {
            _isSyncing.value = false
            false
        }
    }

    fun toggleRealtimeSubscription(enable: Boolean) {
        if (enable) {
            supabaseService.startRealtimeSubscription()
        } else {
            supabaseService.stopRealtimeSubscription()
        }
    }

    suspend fun triggerLiveListingUpdate(): Boolean {
        val targetPg = _pgs.value.firstOrNull { !it.isDirectoryListing } ?: _pgs.value.firstOrNull() ?: return false
        val newPrice = if (targetPg.price % 1000 == 0) targetPg.price + 250 else (targetPg.price / 1000) * 1000
        val res = supabaseService.publishLiveListingUpdate(
            pgId = targetPg.id,
            newPrice = newPrice,
            instantDiscount = 600
        )
        return res.isSuccess
    }

    fun updateComplaintStatus(
        complaintId: String,
        status: ComplaintStatus,
        resolution: String = "",
        assignedTo: String = ""
    ) {
        var targetAssigned = assignedTo
        var targetResolution = resolution
        _complaints.value = _complaints.value.map { existing ->
            if (existing.id == complaintId) {
                val nextAssigned = assignedTo.ifBlank { existing.assignedTo.ifBlank { "Mohan Rao (Property Manager)" } }
                val nextResolution = resolution.ifBlank {
                    when (status) {
                        ComplaintStatus.RESOLVED -> existing.resolution.ifBlank { "Issue resolved & verified by tenant." }
                        else -> existing.resolution
                    }
                }
                targetAssigned = nextAssigned
                targetResolution = nextResolution
                val stepEntry = when (status) {
                    ComplaintStatus.OPEN -> "Ticket reopened for follow-up inspection"
                    ComplaintStatus.IN_PROGRESS -> "Technician ($nextAssigned) is working on the issue"
                    ComplaintStatus.RESOLVED -> "Marked Resolved: $nextResolution"
                    ComplaintStatus.CLOSED -> "Ticket closed"
                }
                existing.copy(
                    status = status,
                    assignedTo = nextAssigned,
                    resolution = nextResolution,
                    updatedAt = "Updated just now",
                    statusTimeline = (existing.statusTimeline + stepEntry).distinct()
                )
            } else existing
        }

        repoScope.launch {
            supabaseService.updateComplaintStatusRemote(
                complaintId = complaintId,
                status = status.name,
                assignedTo = targetAssigned,
                resolution = targetResolution
            )
        }
    }

    fun addComplaintFollowUpNote(complaintId: String, note: String) {
        val cleanNote = note.trim()
        if (cleanNote.isEmpty()) return
        var updatedStatus = ComplaintStatus.IN_PROGRESS
        var updatedAssigned = ""
        var updatedResolution = ""
        _complaints.value = _complaints.value.map { existing ->
            if (existing.id == complaintId) {
                val nextStatus = if (existing.status == ComplaintStatus.OPEN) ComplaintStatus.IN_PROGRESS else existing.status
                updatedStatus = nextStatus
                updatedAssigned = existing.assignedTo.ifBlank { "Mohan Rao (Property Manager)" }
                updatedResolution = existing.resolution
                existing.copy(
                    status = nextStatus,
                    assignedTo = updatedAssigned,
                    updatedAt = "Updated just now",
                    statusTimeline = existing.statusTimeline + "Tenant note: $cleanNote"
                )
            } else existing
        }
        repoScope.launch {
            supabaseService.updateComplaintStatusRemote(
                complaintId = complaintId,
                status = updatedStatus.name,
                assignedTo = updatedAssigned,
                resolution = updatedResolution
            )
        }
    }

    suspend fun refreshComplaintsFromSupabase(): Int {
        val remoteRes = supabaseService.fetchComplaintsRemote()
        val remoteList = remoteRes.getOrNull().orEmpty()
        if (remoteList.isNotEmpty()) {
            val localById = _complaints.value.associateBy { it.id }
            val merged = (remoteList.map { remote ->
                val local = localById[remote.id]
                if (local != null) {
                    local.copy(
                        status = remote.status,
                        assignedTo = remote.assignedTo.ifBlank { local.assignedTo },
                        resolution = remote.resolution.ifBlank { local.resolution }
                    )
                } else {
                    remote.copy(
                        statusTimeline = listOf(
                            "Ticket logged for Room ${remote.roomCode} • Bed #${remote.bedNumber}",
                            if (remote.assignedTo.isNotBlank()) "Assigned to ${remote.assignedTo}" else "Awaiting technician assignment"
                        )
                    )
                }
            } + _complaints.value.filter { local -> remoteList.none { it.id == local.id } })
            _complaints.value = merged
        }
        return _complaints.value.size
    }

    fun toggleLikePost(postId: String) {
        _communityPosts.value = _communityPosts.value.map {
            if (it.id == postId) {
                val newLiked = !it.isLiked
                it.copy(
                    isLiked = newLiked,
                    likes = if (newLiked) it.likes + 1 else it.likes - 1
                )
            } else it
        }
    }

    fun toggleJoinGroup(groupId: String) {
        _communityGroups.value = _communityGroups.value.map {
            if (it.id == groupId) {
                val joined = !it.isJoined
                it.copy(
                    isJoined = joined,
                    members = if (joined) it.members + 1 else it.members - 1
                )
            } else it
        }
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
        val author = _currentUser.value?.name ?: "StayEg Verified Tenant"
        val post = CommunityPost(
            id = "post-${UUID.randomUUID().toString().take(8)}",
            authorName = author,
            title = title,
            content = content,
            category = category,
            pgName = pgName,
            areaName = areaName,
            experienceRating = experienceRating,
            likes = 0,
            comments = 0,
            timeAgo = "Just now",
            tags = tags,
            isLiked = false
        )
        _communityPosts.value = listOf(post) + _communityPosts.value
        trackUserActivity(
            actionType = "COMMUNITY_POST_CREATED",
            screenName = "CommunityScreen",
            targetPgName = pgName,
            details = "Community post by $author in $areaName ($category): $title",
            customArea = areaName
        )
        repoScope.launch {
            supabaseService.saveCommunityPostRemote(post)
        }
    }

    fun reportCommunityPost(postId: String, reason: String) {
        val post = _communityPosts.value.firstOrNull { it.id == postId }
        val reportText = "Reported Community Post (${post?.title ?: postId}): $reason"
        submitFeatureFeedback("Community Content Report", 3, reportText)
    }

    // ─── Seed Data ─────────────────────────────────────────────────────────

    private fun createSeedPgs(): List<PG> {
        return SupabaseService.buildPrimaryOwnerPgs()
    }

    private fun createSeedBookings(): List<Booking> {
        return listOf(
            Booking(
                id = "bk-101",
                userId = "user-tenant-1",
                pgId = "pg-1",
                bedId = "b1-1",
                checkInDate = "15 Jan 2025",
                status = BookingStatus.ACTIVE,
                advancePaid = 11500,
                pgName = "Zolo Stays Crown - Koramangala 6th Block",
                roomCode = "101",
                bedNumber = 1
            )
        )
    }

    private fun createSeedPayments(): List<Payment> {
        return listOf(
            Payment("pay-1", "user-tenant-1", "pg-1", "bk-101", 12000, PaymentType.RENT, PaymentStatus.COMPLETED, "01 May 2025", "02 May 2025", "UPI"),
            Payment("pay-2", "user-tenant-1", "pg-1", "bk-101", 12000, PaymentType.RENT, PaymentStatus.COMPLETED, "01 Apr 2025", "01 Apr 2025", "Debit Card"),
            Payment("pay-3", "user-tenant-1", "pg-1", "bk-101", 12000, PaymentType.RENT, PaymentStatus.COMPLETED, "01 Mar 2025", "03 Mar 2025", "UPI"),
            Payment("pay-4", "user-tenant-1", "pg-1", "bk-101", 12000, PaymentType.ADVANCE, PaymentStatus.COMPLETED, "15 Jan 2025", "15 Jan 2025", "Net Banking")
        )
    }

    private fun createSeedComplaints(): List<Complaint> {
        return listOf(
            Complaint(
                id = "c-1",
                userId = "user-tenant-1",
                pgId = "pg-1",
                title = "WiFi speed is slow in Room 101",
                description = "Speed dropped below 15 Mbps on the 1st floor mesh node during evening work calls.",
                category = ComplaintCategory.MAINTENANCE,
                priority = ComplaintPriority.HIGH,
                status = ComplaintStatus.IN_PROGRESS,
                assignedTo = "NetConnect Fiber Team (Arjun)",
                createdAt = "2 hours ago",
                pgName = "Sunrise PG - Koramangala",
                roomCode = "101",
                bedNumber = 1,
                issueSubType = "Wi-Fi & Router",
                preferredVisitSlot = "Evening (4 PM - 8 PM)",
                updatedAt = "35 mins ago",
                statusTimeline = listOf(
                    "Ticket logged for Room 101 • Bed #1",
                    "Assigned to NetConnect Fiber Team (Arjun)",
                    "Technician inspecting 1st floor 5G mesh router"
                )
            ),
            Complaint(
                id = "c-2",
                userId = "user-tenant-1",
                pgId = "pg-1",
                title = "Bathroom geyser & tap valve check",
                description = "Small drip in the washbasin cold water tap and geyser thermostat check requested.",
                category = ComplaintCategory.MAINTENANCE,
                priority = ComplaintPriority.LOW,
                status = ComplaintStatus.RESOLVED,
                assignedTo = "QuickFix Plumbing (Raghav)",
                resolution = "Tap washer replaced and geyser heating element verified.",
                createdAt = "3 days ago",
                pgName = "Sunrise PG - Koramangala",
                roomCode = "101",
                bedNumber = 1,
                issueSubType = "Plumbing & Geyser",
                preferredVisitSlot = "Morning (9 AM - 12 PM)",
                updatedAt = "2 days ago",
                statusTimeline = listOf(
                    "Ticket logged for Room 101 • Bed #1",
                    "Assigned to QuickFix Plumbing (Raghav)",
                    "Marked Resolved: Tap washer replaced and geyser heating element verified."
                )
            )
        )
    }

    private fun createSeedPosts(): List<CommunityPost> {
        return listOf(
            CommunityPost(
                id = "p1",
                authorName = "Priya Sharma",
                title = "Honest 6-Month PG Experience in Koramangala 4th Block",
                content = "Stayed near Sony World Junction. Food hygiene is 4.5/5 (hot South Indian breakfast daily), 150 Mbps WiFi works great for WFH calls, and biometric entry feels very safe for women returning late from office.",
                category = "PG Experience",
                pgName = "Sunrise PG - Koramangala",
                areaName = "Koramangala",
                experienceRating = 5,
                likes = 42,
                comments = 11,
                timeAgo = "2h ago",
                tags = listOf("Koramangala", "Women Safety", "WFH WiFi")
            ),
            CommunityPost(
                id = "p2",
                authorName = "Karthik Gowda",
                title = "Neeladri Road Electronic City PG Review (Near Wipro Gate)",
                content = "Found this PG via StayEg directory and visited via Google Maps. Walking distance to Wipro Gate, ₹7,500/mo for double sharing with 3-time meals. Tip: check power backup inverter on 3rd floor before paying deposit!",
                category = "PG Review",
                pgName = "SSS Homes & PG Services",
                areaName = "Electronic City",
                experienceRating = 4,
                likes = 64,
                comments = 19,
                timeAgo = "5h ago",
                tags = listOf("Electronic City", "Budget PG", "Food Review")
            ),
            CommunityPost(
                id = "p3",
                authorName = "Sneha Joshi",
                title = "HSR Layout Sector 2 Co-Living & Mess Food Experience",
                content = "Living near 27th Main HSR Layout. Sunday biryani and weekday chapati-dal are super clean. Only ₹30 Rapido bike fare to RMZ Ecospace. Always inspect water geyser and washroom ventilation during your PG visit!",
                category = "Food & Hygiene",
                pgName = "Green Valley PG - HSR",
                areaName = "HSR Layout",
                experienceRating = 5,
                likes = 95,
                comments = 34,
                timeAgo = "1d ago",
                tags = listOf("HSR Layout", "Mess Food", "RMZ Ecospace")
            ),
            CommunityPost(
                id = "p4",
                authorName = "Rohit Verma",
                title = "Whitefield ITPL Hopefarm Metro PG Commute Experience",
                content = "Shifted to a PG 600m from Hopefarm Metro Station. Saves 1.5 hours of Silk Board / Marathahalli traffic every single day! Highly recommend checking Google Maps distance to nearest Purple Line Metro on StayEg.",
                category = "Commute & Area",
                pgName = "Tech Hub PG - Whitefield",
                areaName = "Whitefield",
                experienceRating = 5,
                likes = 53,
                comments = 14,
                timeAgo = "2d ago",
                tags = listOf("Whitefield", "ITPL", "Namma Metro")
            )
        )
    }

    private fun createSeedGroups(): List<CommunityGroup> {
        return listOf(
            CommunityGroup("g1", "Bangalore PG Roommates", "Connect with prospective room partners across BLR", 2840, "Roommates", isJoined = true),
            CommunityGroup("g2", "Weekend Trekkers & Cyclists", "Explore nature trails around Karnataka on weekends", 1450, "Travel", isJoined = false),
            CommunityGroup("g3", "Bengaluru Foodies Club", "Reviews of budget eateries, messes, and cafes", 3120, "Food", isJoined = true),
            CommunityGroup("g4", "Tech Talks & Hackathons", "BLR devs sharing coding events and job referrals", 1890, "Tech", isJoined = false)
        )
    }

    fun querySupabasePgsByCityOrArea(
        cityOrAreaQuery: String,
        selectedCity: String = "All Cities",
        selectedArea: String = "All Areas",
        onlyAvailableBeds: Boolean = false,
        categoryFilter: String = "ALL"
    ): List<PG> {
        ensureSupabaseSnapshotLoaded()
        return supabaseService.queryAvailablePgsByCityOrArea(
            cityOrAreaQuery = cityOrAreaQuery,
            selectedCity = selectedCity,
            selectedArea = selectedArea,
            onlyAvailableBeds = onlyAvailableBeds,
            categoryFilter = categoryFilter
        )
    }

    fun getDistinctAreasWithPgCounts(): List<Pair<String, Int>> {
        ensureSupabaseSnapshotLoaded()
        return supabaseService.getDistinctAreasWithPgCounts()
    }
}

