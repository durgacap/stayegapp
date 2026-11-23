package com.example.stayeg

import com.example.stayeg.data.model.ComplaintCategory
import com.example.stayeg.data.model.ComplaintPriority
import com.example.stayeg.data.model.KYCStatus
import com.example.stayeg.data.model.RoomType
import com.example.stayeg.data.repository.StayEgRepository
import com.example.stayeg.ui.navigation.Screen
import com.example.stayeg.ui.viewmodel.StayEgViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class StayEgEndToEndCujTest {

    private lateinit var repository: StayEgRepository
    private lateinit var viewModel: StayEgViewModel

    @Before
    fun setUp() {
        repository = StayEgRepository()
        viewModel = StayEgViewModel(repository)
    }

    @Test
    fun `test multiple signup and login flows - email, mobile, google, and password reset`() {
        val uniqueSuffix = (System.currentTimeMillis() % 100000).toString().padStart(5, '0')
        val email = "cuj.resident.$uniqueSuffix@gmail.com"
        val phone = "98765$uniqueSuffix"
        val initialPass = "StayEg#2026"

        // 1. Request OTP and Sign Up with Email + Mobile + Password
        val issuedOtp = viewModel.requestVerificationOtp(phone)
        assertEquals(6, issuedOtp.length)
        assertTrue(viewModel.isValidOtp(issuedOtp, phone))

        var signupSucceeded = false
        viewModel.signUpOrLoginWithPhoneOtp(
            name = "Aarav Bengaluru",
            phone = phone,
            email = email,
            otp = issuedOtp,
            password = initialPass,
            locality = "HSR Layout",
            onSuccess = { signupSucceeded = true },
            onError = { fail("Sign up should succeed: $it") }
        )
        assertTrue("Sign up with OTP must succeed", signupSucceeded)
        assertEquals("Aarav Bengaluru", viewModel.currentUser.value?.name)
        assertEquals(email, viewModel.currentUser.value?.email)

        // 2. Log out and Log back in using EMAIL + PASSWORD
        viewModel.logout()
        var emailLoginSucceeded = false
        viewModel.loginWithPassword(
            identifier = email,
            password = initialPass,
            onSuccess = { emailLoginSucceeded = true },
            onError = { fail("Email login should succeed: $it") }
        )
        assertTrue("Login with email + password must succeed", emailLoginSucceeded)

        // 3. Log out and Log back in using 10-DIGIT MOBILE + PASSWORD
        viewModel.logout()
        var phoneLoginSucceeded = false
        viewModel.loginWithPassword(
            identifier = phone,
            password = initialPass,
            onSuccess = { phoneLoginSucceeded = true },
            onError = { fail("Mobile login should succeed: $it") }
        )
        assertTrue("Login with mobile number + password must succeed", phoneLoginSucceeded)

        // 4. Forgot Password Flow via Gmail OTP -> Reset Password -> Login with New Password
        val resetOtp = viewModel.requestVerificationOtp(email)
        val newPass = "NewPass#9988"
        var resetSucceeded = false
        viewModel.resetPasswordWithEmailVerification(
            email = email,
            emailOtp = resetOtp,
            newPassword = newPass,
            onSuccess = { resetSucceeded = true },
            onError = { fail("Password reset should succeed: $it") }
        )
        assertTrue("Password reset via Gmail OTP must succeed", resetSucceeded)

        var reloginNewPassSucceeded = false
        viewModel.loginWithPassword(
            identifier = email,
            password = newPass,
            onSuccess = { reloginNewPassSucceeded = true },
            onError = { fail("Login with new password should succeed: $it") }
        )
        assertTrue("Login with newly reset password must succeed", reloginNewPassSucceeded)

        // 5. Google Sign-In with OTP & Password -> Subsequent Direct Password Login
        val googleEmail = "google.user.$uniqueSuffix@gmail.com"
        val googleOtp = viewModel.requestVerificationOtp(googleEmail)
        var googleAuthSucceeded = false
        viewModel.verifyGoogleWithOtp(
            name = "Divya Google User",
            email = googleEmail,
            otp = googleOtp,
            password = "GoogleLogin#123",
            onSuccess = { googleAuthSucceeded = true },
            onError = { fail("Google OTP verification should succeed: $it") }
        )
        assertTrue("Google Sign-In flow must succeed", googleAuthSucceeded)
        assertEquals(googleEmail, viewModel.currentUser.value?.email)

        // Verify subsequent direct password login for Google user
        viewModel.logout()
        var googlePassLoginOk = false
        viewModel.loginWithPassword(
            identifier = googleEmail,
            password = "GoogleLogin#123",
            onSuccess = { googlePassLoginOk = true },
            onError = { fail("Google user password login should succeed: $it") }
        )
        assertTrue("Direct password login for Google user must succeed", googlePassLoginOk)
    }

    @Test
    fun `test full exploring, filtering, owner claiming, room management, and booking journey`() {
        val allPgs = viewModel.allPgs.value
        assertTrue("Catalog should load 2,400+ Bengaluru PGs", allPgs.size >= 2400)

        // 1. Filter by Locality and Category via querySupabasePgsByCityOrArea
        val koramangalaPgs = viewModel.querySupabasePgsByCityOrArea(
            cityOrAreaQuery = "Koramangala",
            selectedCity = "Bengaluru",
            selectedArea = "Koramangala",
            categoryFilter = "COLIVING"
        )
        assertTrue("Filtered Koramangala Co-Living PGs should not be empty", koramangalaPgs.isNotEmpty())

        // Reset filters
        viewModel.resetFilters()

        // 2. Claim an unverified directory PG listing as Owner
        val directoryPg = viewModel.allPgs.value.first { it.isDirectoryListing && !it.isVerified }
        viewModel.claimAndVerifyPg(directoryPg.id, verifiedPrice = 11500)

        val claimedPg = viewModel.allPgs.value.first { it.id == directoryPg.id }
        assertTrue("Claimed PG must be marked verified", claimedPg.isVerified)
        assertFalse("Claimed PG must have active rooms & beds generated", claimedPg.rooms.isEmpty())
        assertEquals(11500, claimedPg.price)

        // 3. Add a new Room to the claimed PG
        val initialRoomCount = claimedPg.rooms.size
        viewModel.addRoomToPg(
            pgId = claimedPg.id,
            roomCode = "505",
            roomType = RoomType.DOUBLE,
            floor = 5,
            bedPrice = 12000
        )
        val updatedPg = viewModel.allPgs.value.first { it.id == claimedPg.id }
        assertEquals(initialRoomCount + 1, updatedPg.rooms.size)

        // 4. Book a bed in the claimed PG
        val targetRoom = updatedPg.rooms.first()
        val targetBed = targetRoom.beds.first()
        viewModel.bookBed(
            pgId = updatedPg.id,
            bedId = targetBed.id,
            checkInDate = "This Week",
            amount = targetBed.price,
            pgName = updatedPg.name,
            roomCode = targetRoom.roomCode,
            bedNumber = targetBed.bedNumber
        )

        assertEquals(Screen.MyStay, viewModel.currentScreen.value)
        assertEquals(updatedPg.name, viewModel.activeStayConfig.value.pgName)
        assertEquals(targetRoom.roomCode, viewModel.activeStayConfig.value.roomCode)
    }

    @Test
    fun `test payments, my stay services, digital kyc, and community workflows`() {
        // 1. Pay Monthly Rent & Generate Receipt
        val beforePaymentsCount = viewModel.payments.value.size
        val receipt = viewModel.payMonthlyRentViaUpi(amount = 11500, upiApp = "Google Pay")
        assertEquals(11500, receipt.amount)
        assertEquals(beforePaymentsCount + 1, viewModel.payments.value.size)

        // 2. My Stay: Meal RSVP & Gate Pass & Maintenance Ticket
        viewModel.toggleMealOptIn("DINNER")
        val beforePasses = viewModel.gatePasses.value.size
        viewModel.createGatePass(
            passType = "GUEST_VISIT",
            title = "Parent Visit",
            visitorOrReason = "Parents visiting from Mysuru",
            dateRange = "Today",
            timeSlot = "6:00 PM - 9:00 PM"
        )
        assertEquals(beforePasses + 1, viewModel.gatePasses.value.size)

        val beforeComplaints = viewModel.complaints.value.size
        viewModel.raiseComplaint(
            pgId = viewModel.activeStayConfig.value.pgId,
            title = "AC Filter Cleaning",
            description = "Need routine AC filter service in Room 204",
            category = ComplaintCategory.MAINTENANCE,
            priority = ComplaintPriority.MEDIUM,
            preferredVisitSlot = "Evening (5 PM - 8 PM)"
        )
        assertEquals(beforeComplaints + 1, viewModel.complaints.value.size)

        // 3. Profile & Manual Digital KYC Submission
        viewModel.submitManualDigitalKyc(
            documentType = "Aadhaar Card",
            aadhaarOrIdNumber = "458923107845",
            panNumber = "ABCDE1234F",
            guardianName = "Rajesh Resident",
            permanentAddress = "Indiranagar, Bengaluru",
            frontPhotoUri = "https://images.unsplash.com/photo-1554224155-6726b3ff858f?w=600",
            selfiePhotoUri = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=600",
            backPhotoUri = "https://images.unsplash.com/photo-1554224154-26032ffc0d07?w=600",
            targetStatus = KYCStatus.VERIFIED
        )
        assertEquals(KYCStatus.VERIFIED, viewModel.currentUser.value?.kycStatus)

        // 4. Community Post Creation & Liking
        val beforePosts = viewModel.communityPosts.value.size
        viewModel.createPost(
            title = "Weekend Badminton in HSR",
            content = "Looking for a badminton partner in HSR Sector 2 this Saturday morning!",
            category = "Events",
            tags = listOf("HSR Layout", "Sports"),
            pgName = "Sunrise PG",
            areaName = "HSR Layout",
            experienceRating = 5
        )
        assertEquals(beforePosts + 1, viewModel.communityPosts.value.size)
        val newestPost = viewModel.communityPosts.value.first()
        val initialLikes = newestPost.likes
        viewModel.toggleLikePost(newestPost.id)
        val updatedPost = viewModel.communityPosts.value.first { it.id == newestPost.id }
        assertEquals(initialLikes + 1, updatedPost.likes)
    }

    @Test
    fun `test fresh account my stay state, qr code check-in activation, gps near me, and search recommendations`() {
        // 1. Fresh account should NOT have a pre-populated active PG stay
        assertFalse(
            "Fresh account must not show a fake active stay before booking or scanning a PG QR code",
            viewModel.activeStayConfig.value.isStayActivated
        )

        // 2. Live Search Recommendations for PG Name or Locality
        val koramangalaRecs = viewModel.getSearchRecommendations("Koramangala")
        assertTrue("Search recommendations must return matching areas for Koramangala", koramangalaRecs.matchingAreas.any { it.contains("Koramangala", ignoreCase = true) })
        assertTrue("Search recommendations must return matching PGs for Koramangala", koramangalaRecs.matchingPgs.isNotEmpty())

        val zoloRecs = viewModel.getSearchRecommendations("Zolo")
        assertTrue("Search recommendations must return matching PGs for PG name query", zoloRecs.matchingPgs.isNotEmpty())

        // 3. GPS Near Me Tracking & Sorting by Distance
        viewModel.activateNearMeGpsLocation(12.9121, 77.6446)
        assertTrue("GPS Near Me filter should be active", viewModel.filters.value.nearMeActive)
        val nearbyPgs = viewModel.filteredPgs.value
        assertTrue("Nearby PGs around HSR Layout GPS must not be empty", nearbyPgs.isNotEmpty())
        val firstDist = viewModel.distanceFromUserKm(nearbyPgs.first())
        val lastDist = viewModel.distanceFromUserKm(nearbyPgs.last())
        assertTrue("Nearby PGs must be sorted by ascending GPS distance", firstDist <= lastDist)

        // 4. Verified PG QR Code Check-In Activation for My Stay
        val verifiedPg = viewModel.allPgs.value.first()
        viewModel.activateStayViaQrCode(
            qrCode = "STAYEG-QR-${verifiedPg.id.uppercase()}-302",
            pgId = verifiedPg.id,
            roomCode = "302",
            floorNumber = 3,
            bedNumber = 2,
            sharingType = "Double Sharing"
        )
        assertTrue("My Stay must be activated after scanning verified PG QR code", viewModel.activeStayConfig.value.isStayActivated)
        assertEquals("302", viewModel.activeStayConfig.value.roomCode)
        assertEquals("QR_SCAN", viewModel.activeStayConfig.value.checkInMethod)
    }
}

