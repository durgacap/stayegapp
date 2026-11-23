package com.example.stayeg.data.model

enum class UserRole {
    TENANT, OWNER, VENDOR, ADMIN
}

enum class PGGender {
    MALE, FEMALE, UNISEX
}

enum class RoomType {
    SINGLE, DOUBLE, TRIPLE, DORMITORY
}

enum class BedStatus {
    AVAILABLE, OCCUPIED, MAINTENANCE, RESERVED
}

enum class BookingStatus {
    PENDING, CONFIRMED, ACTIVE, COMPLETED, CANCELLED
}

enum class PaymentType {
    RENT, ADVANCE, SECURITY_DEPOSIT
}

enum class PaymentStatus {
    PENDING, COMPLETED, FAILED, REFUNDED
}

enum class ComplaintCategory {
    MAINTENANCE, CLEANLINESS, NOISE, SAFETY, GENERAL
}

enum class ComplaintPriority {
    LOW, MEDIUM, HIGH, URGENT
}

enum class ComplaintStatus {
    OPEN, IN_PROGRESS, RESOLVED, CLOSED
}

enum class KYCStatus {
    NOT_STARTED, PENDING, VERIFIED, REJECTED
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String = "",
    val role: UserRole = UserRole.TENANT,
    val avatarUrl: String = "",
    val gender: String = "Male",
    val isVerified: Boolean = false,
    val kycStatus: KYCStatus = KYCStatus.NOT_STARTED,
    val city: String = "Bengaluru",
    val currentArea: String = "Koramangala",
    val occupation: String = "",
    val organizationName: String = "",
    val workMode: String = "Hybrid",
    val foodPreference: String = "Veg & Non-Veg",
    val sleepSchedule: String = "Flexible",
    val languagesSpoken: String = "English, Hindi, Kannada",
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val bio: String = "",
    val kycDocumentType: String = "Aadhaar Card",
    val aadhaarNumber: String = "",
    val panNumber: String = "",
    val guardianName: String = "",
    val permanentAddress: String = "",
    val kycFrontPhotoUri: String = "",
    val kycBackPhotoUri: String = "",
    val kycSelfiePhotoUri: String = "",
    val kycFrontRemoteUrl: String = "",
    val kycBackRemoteUrl: String = "",
    val kycSelfieRemoteUrl: String = "",
    val kycStorageBucket: String = "kyc-documents",
    val kycSubmittedAt: String = "",
    val kycStatusNote: String = "",
    val authMethod: String = "OTP"
) {
    val uploadedKycDocsCount: Int
        get() = listOf(kycFrontPhotoUri, kycBackPhotoUri, kycSelfiePhotoUri).count { it.isNotBlank() }

    val hasKycIdNumbers: Boolean
        get() = aadhaarNumber.trim().length >= 8 && panNumber.trim().length >= 6

    val kycVerificationProgressPercent: Int
        get() {
            var pts = 0
            if (aadhaarNumber.trim().length >= 8) pts += 20
            if (panNumber.trim().length >= 6) pts += 20
            if (kycFrontPhotoUri.isNotBlank()) pts += 20
            if (kycBackPhotoUri.isNotBlank()) pts += 20
            if (kycSelfiePhotoUri.isNotBlank()) pts += 20
            return if (kycStatus == KYCStatus.VERIFIED) 100 else pts.coerceAtMost(95)
        }

    val realTimeKycStageLabel: String
        get() = when (kycStatus) {
            KYCStatus.VERIFIED -> "VERIFIED • All ID Checks Complete"
            KYCStatus.PENDING -> "PENDING REVIEW • $uploadedKycDocsCount/3 Docs Uploaded"
            KYCStatus.REJECTED -> "ACTION REQUIRED • Re-upload Clear ID"
            KYCStatus.NOT_STARTED -> {
                if (uploadedKycDocsCount > 0 || aadhaarNumber.isNotBlank() || panNumber.isNotBlank()) {
                    "IN PROGRESS • $uploadedKycDocsCount/3 Docs Attached"
                } else {
                    "NOT STARTED • 0/3 Docs Uploaded"
                }
            }
        }

    val frontDocStatusLabel: String
        get() = when {
            kycFrontPhotoUri.isBlank() -> "NOT UPLOADED"
            kycStatus == KYCStatus.VERIFIED -> "VERIFIED ✓"
            kycStatus == KYCStatus.REJECTED -> "RE-UPLOAD NEEDED"
            else -> "UPLOADED • READY"
        }

    val backDocStatusLabel: String
        get() = when {
            kycBackPhotoUri.isBlank() -> "NOT UPLOADED"
            kycStatus == KYCStatus.VERIFIED -> "VERIFIED ✓"
            kycStatus == KYCStatus.REJECTED -> "RE-UPLOAD NEEDED"
            else -> "UPLOADED • READY"
        }

    val selfieDocStatusLabel: String
        get() = when {
            kycSelfiePhotoUri.isBlank() -> "NOT UPLOADED"
            kycStatus == KYCStatus.VERIFIED -> "VERIFIED ✓"
            kycStatus == KYCStatus.REJECTED -> "RE-UPLOAD NEEDED"
            else -> "UPLOADED • READY"
        }

    val profileCompletionPercent: Int
        get() {
            var score = 0
            if (name.isNotBlank()) score += 15
            if (phone.length >= 10) score += 15
            if (email.contains("@")) score += 10
            if (avatarUrl.isNotBlank()) score += 15
            if (occupation.isNotBlank()) score += 10
            if (organizationName.isNotBlank()) score += 10
            if (currentArea.isNotBlank()) score += 10
            if (bio.isNotBlank()) score += 5
            if (kycStatus == KYCStatus.VERIFIED) {
                score += 10
            } else if (uploadedKycDocsCount > 0) {
                score += 5
            }
            return score.coerceAtMost(100)
        }
}

data class ActiveStayConfig(
    val pgId: String = "pg-1",
    val pgName: String = "Sunrise PG - Koramangala",
    val area: String = "Koramangala 4th Block",
    val roomCode: String = "101",
    val floorNumber: Int = 1,
    val bedNumber: Int = 1,
    val sharingType: String = "Double Sharing",
    val monthlyRent: Int = 12000,
    val securityDeposit: Int = 24000,
    val electricityExtra: Int = 450,
    val moveInDate: String = "01 Nov 2026",
    val lockInMonths: Int = 3,
    val noticePeriodDays: Int = 30,
    val moveOutNoticeActive: Boolean = false,
    val moveOutTargetDate: String = "",
    val isStayActivated: Boolean = false,
    val qrCheckInCode: String = "",
    val checkInMethod: String = "" // "QR_SCAN", "BOOKING", "MANUAL"
)

data class SearchRecommendationsResult(
    val query: String,
    val matchingAreas: List<String>,
    val matchingPgs: List<PG>,
    val nearbyPgs: List<PG>
)

data class MealAttendanceState(
    val breakfastOptIn: Boolean = true,
    val lunchOptIn: Boolean = true,
    val snacksOptIn: Boolean = true,
    val dinnerOptIn: Boolean = true,
    val tomorrowBreakfastOptIn: Boolean = true,
    val tomorrowLunchOptIn: Boolean = true,
    val tomorrowDinnerOptIn: Boolean = true,
    val mealMode: String = "Regular Dining Hall", // "Regular Dining Hall", "Pack Office Lunch Box", "Light Diet / Khichdi"
    val specialDietaryNote: String = "No mushroom, medium spice",
    val mealsAttendedThisMonth: Int = 68,
    val mealsSkippedSavedCount: Int = 6
)

data class TenantGatePass(
    val id: String,
    val passType: String, // "LATE_ENTRY", "GUEST_VISIT", "OUTSTATION_LEAVE"
    val title: String,
    val visitorOrReason: String,
    val dateRange: String,
    val timeSlot: String,
    val status: String = "APPROVED"
)

data class WardenChatMessage(
    val id: String,
    val senderName: String,
    val isFromTenant: Boolean,
    val message: String,
    val timestamp: String
)

data class Bed(
    val id: String,
    val roomId: String,
    val bedNumber: Int,
    val status: BedStatus = BedStatus.AVAILABLE,
    val price: Int = 0
)

data class Room(
    val id: String,
    val pgId: String,
    val roomCode: String,
    val roomType: RoomType = RoomType.DOUBLE,
    val floor: Int = 1,
    val hasAC: Boolean = true,
    val hasAttachedBath: Boolean = true,
    val beds: List<Bed> = emptyList()
)

data class NearbyPlace(
    val name: String,
    val type: String, // "METRO", "TECH_PARK", "BUS_STOP", "HOSPITAL", "FOOD_MESS", "GYM"
    val distanceKm: String,
    val walkMins: Int,
    val bikeMins: Int,
    val rapidoFare: Int
)

data class DailyMealMenu(
    val day: String,
    val breakfast: String,
    val lunch: String,
    val snacks: String,
    val dinner: String,
    val isSpecialNonVeg: Boolean = false
)

data class PGVisitSchedule(
    val id: String,
    val pgId: String,
    val pgName: String,
    val userId: String,
    val visitorName: String,
    val visitorPhone: String,
    val visitDate: String,
    val visitTime: String,
    val pickupMode: String = "BIKE_PICKUP", // "BIKE_PICKUP", "CAB_PICKUP", "SELF_WALKIN"
    val pickupLocation: String = "",
    val status: String = "CONFIRMED"
)

data class PGInterestLead(
    val id: String,
    val pgId: String,
    val pgName: String,
    val area: String,
    val tenantName: String,
    val tenantPhone: String,
    val tenantEmail: String = "",
    val preferredSharing: String = "Double Sharing",
    val expectedMoveIn: String = "This Month",
    val notes: String = "",
    val status: String = "SHARED_WITH_OWNER",
    val createdAt: String = "Just now"
)

data class OwnerActivationLead(
    val id: String,
    val ownerName: String,
    val pgName: String,
    val area: String,
    val phone: String,
    val email: String,
    val totalBeds: Int,
    val claimCode: String = "",
    val poolRank: Int = 142,
    val status: String = "RESERVED_IN_1000_FREE_POOL",
    val createdAt: String = "Just now"
)

data class AppFeatureFeedback(
    val id: String,
    val userId: String,
    val userName: String,
    val featureCategory: String, // "Online Booking", "Payment Gateway", "PG Discovery", "Community", "General"
    val rating: Int = 5,
    val feedbackText: String,
    val createdAt: String = "Just now"
)

data class BangaloreEventSpot(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String, // "TECH_HUB", "LIVE_EVENT", "METRO_CORRIDOR", "FAMOUS_SPOT"
    val area: String,
    val timingOrStatus: String,
    val pgCountNearby: Int,
    val mapQuery: String
)

data class SeoGeoFaqItem(
    val question: String,
    val answer: String,
    val tag: String
)

data class PG(
    val id: String,
    val name: String,
    val ownerId: String,
    val description: String,
    val address: String,
    val city: String,
    val area: String = "Koramangala",
    val category: String = "Co-Living PG",
    val phone: String = "+91 98765 43210",
    val claimCode: String = "",
    val isDirectoryListing: Boolean = false,
    val lat: Double = 12.9716,
    val lng: Double = 77.5946,
    val gender: PGGender = PGGender.UNISEX,
    val price: Int,
    val securityDeposit: Int,
    val amenities: List<String> = emptyList(),
    val imageUrls: List<String> = emptyList(),
    val photoGallery: Map<String, List<String>> = emptyMap(),
    val nearbyPlaces: List<NearbyPlace> = emptyList(),
    val messMenu: List<DailyMealMenu> = emptyList(),
    val wifiSpeedMbps: Int = 150,
    val messHygieneRating: Double = 4.7,
    val instantDiscount: Int = 500,
    val rating: Double = 4.5,
    val totalReviews: Int = 100,
    val isVerified: Boolean = true,
    val rooms: List<Room> = emptyList()
)

data class Booking(
    val id: String,
    val userId: String,
    val pgId: String,
    val bedId: String,
    val checkInDate: String,
    val status: BookingStatus = BookingStatus.ACTIVE,
    val advancePaid: Int,
    val pgName: String = "",
    val roomCode: String = "",
    val bedNumber: Int = 1
)

data class Payment(
    val id: String,
    val userId: String,
    val pgId: String,
    val bookingId: String = "",
    val amount: Int,
    val type: PaymentType = PaymentType.RENT,
    val status: PaymentStatus = PaymentStatus.COMPLETED,
    val dueDate: String = "",
    val paidDate: String = "",
    val method: String = "UPI",
    val couponCode: String? = null,
    val discount: Int = 0
)

data class Complaint(
    val id: String,
    val userId: String,
    val pgId: String,
    val title: String,
    val description: String,
    val category: ComplaintCategory = ComplaintCategory.MAINTENANCE,
    val priority: ComplaintPriority = ComplaintPriority.MEDIUM,
    val status: ComplaintStatus = ComplaintStatus.OPEN,
    val assignedTo: String = "",
    val resolution: String = "",
    val createdAt: String = "Just now",
    val pgName: String = "Sunrise PG - Koramangala",
    val roomCode: String = "101",
    val bedNumber: Int = 1,
    val issueSubType: String = "Wi-Fi & Router",
    val photoAttachmentUri: String = "",
    val remoteStorageUrl: String = "",
    val storageBucket: String = "complaint-photos",
    val preferredVisitSlot: String = "Morning (9 AM - 12 PM)",
    val updatedAt: String = "Just now",
    val statusTimeline: List<String> = emptyList()
) {
    val progressStepIndex: Int
        get() = when (status) {
            ComplaintStatus.OPEN -> if (assignedTo.isNotBlank()) 1 else 0
            ComplaintStatus.IN_PROGRESS -> 2
            ComplaintStatus.RESOLVED, ComplaintStatus.CLOSED -> 3
        }
}

data class SupabaseStorageObject(
    val id: String,
    val userId: String,
    val userName: String = "StayEg Resident",
    val bucketName: String,
    val slotOrTag: String,
    val localUri: String,
    val remotePublicUrl: String,
    val byteSize: Int = 0,
    val sha256Checksum: String = "",
    val uploadedAt: String = "Just now"
)

data class Vendor(
    val id: String,
    val name: String,
    val serviceType: String,
    val phone: String,
    val city: String,
    val area: String,
    val rating: Double = 4.5
)

data class Worker(
    val id: String,
    val name: String,
    val role: String,
    val phone: String,
    val shift: String = "MORNING"
)

data class PricingPlan(
    val id: String,
    val name: String,
    val duration: String,
    val price: Int,
    val originalPrice: Int,
    val discount: Int,
    val features: List<String>,
    val badge: String = "",
    val popular: Boolean = false
)

data class Coupon(
    val code: String,
    val description: String,
    val discountPercent: Int,
    val flatDiscount: Int = 0,
    val minAmount: Int = 0,
    val maxDiscount: Int = 0
)

data class CommunityPost(
    val id: String,
    val authorName: String,
    val title: String,
    val content: String,
    val category: String,
    val pgName: String = "",
    val areaName: String = "Bengaluru",
    val experienceRating: Int = 5,
    val likes: Int = 0,
    val comments: Int = 0,
    val timeAgo: String = "2h ago",
    val tags: List<String> = emptyList(),
    val isLiked: Boolean = false
)

data class CommunityGroup(
    val id: String,
    val name: String,
    val description: String,
    val members: Int,
    val category: String,
    val isJoined: Boolean = false
)

data class Review(
    val id: String,
    val userName: String,
    val rating: Double,
    val comment: String,
    val date: String,
    val cleanliness: Double = 4.5,
    val safety: Double = 4.8,
    val valueForMoney: Double = 4.6
)

data class UserActivityTelemetry(
    val id: String,
    val userId: String,
    val userName: String,
    val userEmail: String,
    val userPhone: String,
    val userArea: String,
    val latitude: Double = 12.9352,
    val longitude: Double = 77.6245,
    val actionType: String, // "SIGN_UP", "LOGIN", "EMAIL_OTP_SENT", "PG_VIEW", "MAP_ROUTE_CHECK", "BED_SELECT", "FEEDBACK_SUBMITTED"
    val screenName: String,
    val targetPgId: String = "",
    val targetPgName: String = "",
    val details: String = "",
    val timestamp: String = "Just now"
)

data class MapSearchHubCoord(
    val areaName: String,
    val landmark: String,
    val lat: Double,
    val lng: Double,
    val metroStation: String,
    val avgBikeTimeMin: Int,
    val avgBikeFareInr: Int
)

data class SupabaseAuthSession(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "bearer",
    val expiresIn: Long = 3600L,
    val expiresAtEpochSec: Long,
    val userId: String,
    val email: String,
    val phone: String = "",
    val provider: String = "email", // "email" or "google"
    val fullName: String = "",
    val avatarUrl: String = ""
) {
    val isExpired: Boolean
        get() = (System.currentTimeMillis() / 1000L) >= expiresAtEpochSec
}



