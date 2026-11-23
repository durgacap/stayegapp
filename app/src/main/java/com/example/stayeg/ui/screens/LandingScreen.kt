package com.example.stayeg.ui.screens

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
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.stayeg.data.model.PG
import com.example.stayeg.ui.components.StayEgGpsNearMePillButton
import com.example.stayeg.ui.components.StayEgInteractivePgMapRadar
import com.example.stayeg.ui.components.StayEgPopupInfoDialog
import com.example.stayeg.ui.components.StayEgSearchRecommendationsDropdown
import com.example.stayeg.ui.components.StayEgTrustedListingInfoBanner
import com.example.stayeg.ui.components.openGoogleMapsIntent
import com.example.stayeg.ui.navigation.Screen
import com.example.stayeg.ui.theme.*
import com.example.stayeg.ui.viewmodel.StayEgViewModel

private data class IconCategoryItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)

private data class TechHubCardData(
    val area: String,
    val landmark: String,
    val startingRent: String,
    val photoUrl: String
)

@Composable
fun LandingScreen(viewModel: StayEgViewModel) {
    val context = LocalContext.current
    val filters by viewModel.filters.collectAsState()
    val allPgs by viewModel.allPgs.collectAsState()
    val filteredPgs by viewModel.filteredPgs.collectAsState()

    var activeAlertModalType by remember { mutableStateOf<String?>(null) }
    var showSearchRecommendations by remember { mutableStateOf(false) }

    val featuredPgs = remember(allPgs, filteredPgs, filters.nearMeActive) {
        if (filters.nearMeActive) {
            filteredPgs.take(12)
        } else {
            allPgs.take(12)
        }
    }

    // Horizontal Scrollable Icon Category Bar (Ladies, Gents, Co-living, Unisex, Hostels)
    val iconCategories = remember {
        listOf(
            IconCategoryItem(
                id = "ALL",
                title = "All PGs",
                subtitle = "Bengaluru",
                icon = Icons.Default.GridView
            ),
            IconCategoryItem(
                id = "LADIES",
                title = "Ladies",
                subtitle = "From ₹7,500",
                icon = Icons.Default.Female
            ),
            IconCategoryItem(
                id = "GENTS",
                title = "Gents",
                subtitle = "From ₹6,800",
                icon = Icons.Default.Male
            ),
            IconCategoryItem(
                id = "COLIVING",
                title = "Co-living",
                subtitle = "From ₹9,500",
                icon = Icons.Default.Groups
            ),
            IconCategoryItem(
                id = "UNISEX",
                title = "Unisex",
                subtitle = "From ₹8,500",
                icon = Icons.Default.People
            ),
            IconCategoryItem(
                id = "HOSTELS",
                title = "Hostels",
                subtitle = "From ₹5,500",
                icon = Icons.Default.Apartment
            )
        )
    }

    val techHubs = remember {
        listOf(
            TechHubCardData(
                area = "Koramangala",
                landmark = "Near Sony Signal & Forum Nexus",
                startingRent = "₹8,500/mo",
                photoUrl = "https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?w=500&h=360&fit=crop"
            ),
            TechHubCardData(
                area = "HSR Layout",
                landmark = "27th Main & Sector 2",
                startingRent = "₹8,000/mo",
                photoUrl = "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=500&h=360&fit=crop"
            ),
            TechHubCardData(
                area = "Whitefield",
                landmark = "Near ITPL & EPIP Zone",
                startingRent = "₹7,500/mo",
                photoUrl = "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=500&h=360&fit=crop"
            ),
            TechHubCardData(
                area = "Electronic City",
                landmark = "Phase 1 & Wipro Gate",
                startingRent = "₹6,500/mo",
                photoUrl = "https://images.unsplash.com/photo-1555854877-bab0e564b8d5?w=500&h=360&fit=crop"
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftGalleryBg),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. CLEAN HOOK HERO SEARCH CARD + LIVE RECOMMENDATIONS + GPS NEAR ME
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = TealDark,
                    shadowElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "StayEg",
                                        style = TextStyle(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 22.sp,
                                            letterSpacing = (-0.5).sp,
                                            brush = Brush.linearGradient(
                                                colors = listOf(
                                                    MintSoft,
                                                    CreamLight,
                                                    Color(0xFFFDE68A)
                                                )
                                            )
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = SoftGoldPill
                                    ) {
                                        Text(
                                            text = "₹0 BROKERAGE",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            color = GoldDark,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        onClick = { viewModel.navigateTo(Screen.MyStay) },
                                        shape = RoundedCornerShape(999.dp),
                                        color = PureWhite.copy(alpha = 0.14f)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.QrCodeScanner,
                                                contentDescription = "Scan PG QR",
                                                tint = MintSoft,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Scan QR",
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 10.sp,
                                                color = PureWhite
                                            )
                                        }
                                    }

                                    Surface(
                                        onClick = { activeAlertModalType = "PAYMENT_GATEWAY_INFO" },
                                        shape = RoundedCornerShape(999.dp),
                                        color = PureWhite.copy(alpha = 0.14f),
                                        modifier = Modifier.testTag("home_payment_alert_tile")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = "Directory Info",
                                                tint = MintSoft,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Find Your Perfect Bengaluru PG",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 21.sp,
                                color = PureWhite,
                                lineHeight = 26.sp
                            )
                            Text(
                                text = "Live GPS nearby discovery • Interactive map pins • Direct check-in",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 11.sp,
                                color = MintSoft.copy(alpha = 0.9f)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Pill-Rounded Search Input Field with Live Autocomplete Trigger
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = PureWhite,
                                shadowElevation = 4.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 6.dp, end = 6.dp, top = 4.dp, bottom = 4.dp)
                                ) {
                                    TextField(
                                        value = filters.query,
                                        onValueChange = {
                                            viewModel.setQuery(it)
                                            showSearchRecommendations = true
                                        },
                                        placeholder = {
                                            Text(
                                                text = "Search PG name, Koramangala, HSR, Whitefield...",
                                                fontFamily = PoppinsFontFamily,
                                                fontSize = 12.sp,
                                                color = TextMuted,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = null,
                                                tint = TealPrimary,
                                                modifier = Modifier.size(19.dp)
                                            )
                                        },
                                        trailingIcon = {
                                            if (filters.query.isNotEmpty()) {
                                                IconButton(
                                                    onClick = {
                                                        viewModel.setQuery("")
                                                        showSearchRecommendations = false
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Clear",
                                                        tint = TextSecondary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(999.dp),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("hero_search_input")
                                    )

                                    Surface(
                                        onClick = { viewModel.navigateTo(Screen.Search) },
                                        shape = CircleShape,
                                        color = TealPrimary,
                                        modifier = Modifier
                                            .size(42.dp)
                                            .testTag("hero_search_button")
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowForward,
                                                contentDescription = "Search PGs",
                                                tint = PureWhite,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Clean Quick Action Hook Row: GPS Near Me + Live Map + 1/2/3 Share
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                item {
                                    StayEgGpsNearMePillButton(viewModel = viewModel)
                                }

                                item {
                                    Surface(
                                        onClick = { viewModel.navigateTo(Screen.Explore) },
                                        shape = RoundedCornerShape(999.dp),
                                        color = PureWhite.copy(alpha = 0.16f)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Map,
                                                contentDescription = null,
                                                tint = MintSoft,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Map View",
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = PureWhite
                                            )
                                        }
                                    }
                                }

                                items(
                                    listOf(
                                        "SINGLE" to "1 Share",
                                        "DOUBLE" to "2 Share",
                                        "TRIPLE" to "3 Share"
                                    )
                                ) { (shareKey, label) ->
                                    Surface(
                                        onClick = {
                                            viewModel.setPreferredSharingFilter(shareKey)
                                            viewModel.navigateTo(Screen.Explore)
                                        },
                                        shape = RoundedCornerShape(999.dp),
                                        color = PureWhite.copy(alpha = 0.14f)
                                    ) {
                                        Text(
                                            text = label,
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 11.sp,
                                            color = PureWhite,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Instant Search Autocomplete Recommendations Dropdown when user types in Hero Search
                if (filters.query.isNotEmpty() || showSearchRecommendations) {
                    StayEgSearchRecommendationsDropdown(
                        query = filters.query,
                        viewModel = viewModel,
                        onSelectArea = { area ->
                            viewModel.setSelectedArea(area)
                            viewModel.setQuery(area)
                            showSearchRecommendations = false
                            viewModel.navigateTo(Screen.Search)
                        },
                        onSelectPg = { selectedPg ->
                            showSearchRecommendations = false
                            viewModel.navigateTo(Screen.PGDetail(selectedPg.id))
                        },
                        onViewAllResults = {
                            showSearchRecommendations = false
                            viewModel.navigateTo(Screen.Search)
                        }
                    )
                }
            }
        }

        // 2. HORIZONTAL SCROLLABLE ICON CATEGORY BAR
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Browse Categories",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Explore All PGs >",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = TealPrimary,
                        modifier = Modifier
                            .clickable { viewModel.navigateTo(Screen.Explore) }
                            .testTag("browse_all_2415_button")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(iconCategories, key = { it.id }) { cat ->
                        val isSelected = filters.pgCategoryType == cat.id
                        Surface(
                            onClick = {
                                viewModel.selectPgCategory(cat.id)
                                if (cat.id != "ALL") {
                                    viewModel.navigateTo(Screen.Explore)
                                }
                            },
                            shape = RoundedCornerShape(18.dp),
                            color = if (isSelected) TealDark else PureWhite,
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) TealPrimary else WhisperBorder
                            ),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .width(106.dp)
                                .testTag("category_card_${cat.id.lowercase()}")
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) TealPrimary else SoftSagePill),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = cat.icon,
                                        contentDescription = cat.title,
                                        tint = if (isSelected) MintSoft else TealPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = cat.title,
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSelected) PureWhite else TextPrimary,
                                    maxLines = 1
                                )
                                Text(
                                    text = cat.subtitle,
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 9.sp,
                                    color = if (isSelected) MintSoft else TextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. TRUSTED PUBLIC DIRECTORY BANNER ABOVE PG LISTINGS
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                StayEgTrustedListingInfoBanner(
                    onOwnerClaimClick = { viewModel.navigateTo(Screen.OwnerJoinLead) }
                )
            }
        }

        // 4. FEATURED BENGALURU PG LISTINGS CAROUSEL
        item {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (filters.nearMeActive) "PGs Near Your GPS (${filters.userLocationLabel})" else "Featured Bengaluru PGs",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "View All >",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = TealPrimary,
                        modifier = Modifier.clickable {
                            viewModel.navigateTo(Screen.Explore)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(featuredPgs, key = { pg -> "home_curated_${pg.id}" }) { pg ->
                        HomeCuratedShowcaseCard(
                            pg = pg,
                            distanceKm = viewModel.distanceFromUserKm(pg),
                            onInfoBadgeClick = { activeAlertModalType = "PAYMENT_GATEWAY_INFO" },
                            onClick = { viewModel.navigateTo(Screen.PGDetail(pg.id)) }
                        )
                    }
                }
            }
        }

        // 5. INTERACTIVE GOOGLE MAPS PG MARKER EXPLORER
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                StayEgInteractivePgMapRadar(
                    pgs = featuredPgs,
                    selectedArea = filters.selectedArea,
                    onSelectPg = { selectedPg ->
                        viewModel.navigateTo(Screen.PGDetail(selectedPg.id))
                    },
                    onLaunchGoogleMapsForPg = { selectedPg ->
                        openGoogleMapsIntent(
                            context,
                            "${selectedPg.name}, ${selectedPg.address}, ${selectedPg.area}, Bengaluru"
                        )
                    }
                )
            }
        }

        // 6. POPULAR BENGALURU TECH CORRIDORS (2x2 Visual Hub Grid)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Popular Tech Corridors",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        techHubs.take(2).forEach { hub ->
                            ZoloTechHubCard(
                                hub = hub,
                                onClick = {
                                    viewModel.setSelectedArea(hub.area)
                                    viewModel.navigateTo(Screen.Explore)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        techHubs.drop(2).forEach { hub ->
                            ZoloTechHubCard(
                                hub = hub,
                                onClick = {
                                    viewModel.setSelectedArea(hub.area)
                                    viewModel.navigateTo(Screen.Explore)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // 7. OWNER PARTNER BANNER
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                Surface(
                    onClick = { viewModel.navigateTo(Screen.OwnerJoinLead) },
                    shape = RoundedCornerShape(20.dp),
                    color = SoftSagePill,
                    border = BorderStroke(1.dp, TealPrimary.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = SoftGoldPill
                            ) {
                                Text(
                                    text = "PARTNER WITH STAYEG",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = GoldDark,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Own a PG in Bengaluru?",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Claim your listing to activate verified room & bed inventory.",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = TealDark,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = MintSoft,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (activeAlertModalType == "PAYMENT_GATEWAY_INFO") {
        StayEgPopupInfoDialog(
            title = "StayEg Trusted Public Directory",
            subtitle = "Transparent PG & Co-Living Discovery",
            bulletPoints = listOf(
                "Unverified Public Directory Listings: Properties are listed from public locality records so you can compare rents, amenities, and map routes easily.",
                "Inspect via Live Map First: Use the in-app interactive map to visit and verify the room in person before paying any rent or deposit.",
                "Zero Brokerage Promise: You never pay any agent commission or platform booking fee on StayEg."
            ),
            primaryButtonText = "Owner Portal",
            secondaryButtonText = "Close",
            onDismiss = { activeAlertModalType = null },
            onPrimaryClick = {
                activeAlertModalType = null
                viewModel.navigateTo(Screen.OwnerJoinLead)
            },
            onSecondaryClick = { activeAlertModalType = null }
        )
    }
}

@Composable
private fun ZoloTechHubCard(
    hub: TechHubCardData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, WhisperBorder),
        shadowElevation = 2.dp,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp)
                    .clip(RoundedCornerShape(13.dp))
            ) {
                AsyncImage(
                    model = hub.photoUrl,
                    contentDescription = hub.area,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                            )
                        )
                )
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = PureWhite.copy(alpha = 0.92f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                ) {
                    Text(
                        text = hub.startingRent,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = TealDark,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = hub.area,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = PureWhite,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = hub.landmark,
                    fontFamily = PoppinsFontFamily,
                    fontSize = 10.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun HomeCuratedShowcaseCard(
    pg: PG,
    distanceKm: Double,
    onInfoBadgeClick: () -> Unit,
    onClick: () -> Unit
) {
    var isSaved by remember { mutableStateOf(false) }
    val isVerifiedProperty = pg.isVerified && !pg.isDirectoryListing

    val compactAmenities = remember(pg.amenities) {
        listOf(
            Icons.Default.Wifi to "High-Speed WiFi",
            Icons.Default.Restaurant to "Homely Meals",
            Icons.Default.Security to "CCTV Security"
        )
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, WhisperBorder),
        shadowElevation = 3.dp,
        modifier = Modifier
            .width(276.dp)
            .testTag("pg_card_${pg.id}")
    ) {
        Column(modifier = Modifier.padding(11.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(152.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(SoftCardSurface)
            ) {
                AsyncImage(
                    model = pg.imageUrls.firstOrNull()
                        ?: "https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?w=600&h=400&fit=crop",
                    contentDescription = pg.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Top-Right Heart Button
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(PureWhite.copy(alpha = 0.92f))
                        .clickable { isSaved = !isSaved },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Save",
                        tint = if (isSaved) TealPrimary else TealDark,
                        modifier = Modifier.size(15.dp)
                    )
                }

                // Bottom-Left Badge: Clickable Unverified Listing (or Owner Verified) + GPS Distance
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        onClick = onInfoBadgeClick,
                        shape = RoundedCornerShape(999.dp),
                        color = PureWhite.copy(alpha = 0.95f),
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isVerifiedProperty) Icons.Default.Verified else Icons.Default.Info,
                                contentDescription = "Listing Info",
                                tint = if (isVerifiedProperty) TealPrimary else GoldDark,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isVerifiedProperty) "Verified" else "Unverified ⓘ",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PitchBlack
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = TealDark.copy(alpha = 0.88f)
                    ) {
                        Text(
                            text = String.format("%.1f km", distanceKm),
                            fontFamily = PoppinsFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MintSoft,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title + Star Pill Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pg.name,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = pg.address.ifBlank { pg.area },
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = SoftGoldPill
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = SoftGoldAccent,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = String.format("%.1f", pg.rating),
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = GoldDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Clean 3-Column Icon-Based Amenity Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                compactAmenities.forEach { (icon, label) ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SoftCardSurface,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = TealPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = label,
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 9.sp,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Price + View PG CTA Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "₹${pg.price}",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = " /mo",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 1.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = TealDark
                ) {
                    Text(
                        text = "View PG →",
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
}
