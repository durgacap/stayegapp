package com.example.stayeg.ui.navigation

sealed class Screen(val route: String) {
    object WelcomeAuth : Screen("welcome_auth")
    object Landing : Screen("landing")
    object Explore : Screen("explore")
    object Search : Screen("search")
    data class PGDetail(val pgId: String) : Screen("pg_detail/$pgId")
    data class Booking(
        val pgId: String,
        val bedId: String = "",
        val price: Int = 10000,
        val roomCode: String = "A101"
    ) : Screen("booking/$pgId")
    object OwnerJoinLead : Screen("owner_join_lead")
    object FeedbackHub : Screen("feedback_hub")
    object TenantHome : Screen("tenant_home")
    object MyStay : Screen("my_stay")
    object Payments : Screen("payments")
    object Complaints : Screen("complaints")
    object OwnerDashboard : Screen("owner_dashboard")
    object OwnerPGs : Screen("owner_pgs")
    object OwnerRent : Screen("owner_rent")
    object OwnerComplaints : Screen("owner_complaints")
    object Community : Screen("community")
    object Pricing : Screen("pricing")
    object Profile : Screen("profile")
    object About : Screen("about")
    object Contact : Screen("contact")
    object SafeUse : Screen("safe_use")
    object Terms : Screen("terms")
    object SupabaseSync : Screen("supabase_sync")
}
