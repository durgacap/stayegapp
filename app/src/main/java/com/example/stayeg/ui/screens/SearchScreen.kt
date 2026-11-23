package com.example.stayeg.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.stayeg.data.model.PG
import com.example.stayeg.data.model.PGGender
import com.example.stayeg.ui.components.StayEgGpsNearMePillButton
import com.example.stayeg.ui.components.StayEgInteractivePgMapRadar
import com.example.stayeg.ui.components.StayEgPopupInfoDialog
import com.example.stayeg.ui.components.StayEgSearchRecommendationsDropdown
import com.example.stayeg.ui.components.StayEgTrustedListingInfoBanner
import com.example.stayeg.ui.components.facilityIconFor
import com.example.stayeg.ui.components.openGoogleMapsIntent
import com.example.stayeg.ui.navigation.Screen
import com.example.stayeg.ui.theme.*
import com.example.stayeg.ui.viewmodel.StayEgViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: StayEgViewModel) {
    val context = LocalContext.current
    val filters by viewModel.filters.collectAsState()
    val allPgs by viewModel.allPgs.collectAsState()

    var searchQuery by remember(filters.query) { mutableStateOf(filters.query) }
    var selectedCity by remember(filters.city) {
        mutableStateOf(filters.city.ifBlank { "Bengaluru" })
    }
    var selectedArea by remember(filters.selectedArea) {
        mutableStateOf(filters.selectedArea.ifBlank { "All Areas" })
    }
    var onlyAvailableBeds by remember(filters.onlyAvailableBeds) {
        mutableStateOf(filters.onlyAvailableBeds)
    }
    var selectedCategory by remember(filters.pgCategoryType) {
        mutableStateOf(filters.pgCategoryType.ifBlank { "ALL" })
    }
    var showFilterDrawer by remember { mutableStateOf(false) }
    var showLiveMapExplorer by remember { mutableStateOf(true) }
    var showSearchRecommendations by remember { mutableStateOf(false) }
    var showUnverifiedInfoPopup by remember { mutableStateOf(false) }

    val areaOptions = remember(allPgs) {
        viewModel.getDistinctAreasWithPgCounts().map { it.first }
    }

    val searchResults = remember(
        allPgs,
        searchQuery,
        selectedCity,
        selectedArea,
        onlyAvailableBeds,
        selectedCategory,
        filters.nearMeActive,
        filters.userLat,
        filters.userLng
    ) {
        viewModel.querySupabasePgsByCityOrArea(
            cityOrAreaQuery = searchQuery,
            selectedCity = selectedCity,
            selectedArea = selectedArea,
            onlyAvailableBeds = onlyAvailableBeds,
            categoryFilter = selectedCategory
        )
    }

    var visibleLimit by remember(searchQuery, selectedCity, selectedArea, onlyAvailableBeds, selectedCategory, filters.nearMeActive) {
        mutableIntStateOf(40)
    }
    val displayedPgs = remember(searchResults, visibleLimit) {
        searchResults.take(visibleLimit)
    }

    val categoryPills = remember {
        listOf(
            "ALL" to "All PGs",
            "LADIES" to "Ladies PG",
            "GENTS" to "Gents PG",
            "COLIVING" to "Co-Living"
        )
    }

    val hasActiveFilter = selectedArea != "All Areas" ||
        searchQuery.isNotEmpty() ||
        onlyAvailableBeds ||
        selectedCategory != "ALL" ||
        filters.nearMeActive

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftGalleryBg)
            .testTag("supabase_search_results_list"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Sleek, Compact Search & Filter Box + Live Autocomplete Recommendations
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PureWhite,
                    border = BorderStroke(1.dp, WhisperBorder),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Compact Search Input + Filter Button + Live Map Toggle Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SoftCardSurface,
                                border = BorderStroke(1.dp, WhisperBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                TextField(
                                    value = searchQuery,
                                    onValueChange = { q ->
                                        searchQuery = q
                                        viewModel.setQuery(q)
                                        showSearchRecommendations = true
                                    },
                                    placeholder = {
                                        Text(
                                            text = "Search PG name, street, or area...",
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
                                            contentDescription = "Search PGs",
                                            tint = TealPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(
                                                onClick = {
                                                    searchQuery = ""
                                                    viewModel.setQuery("")
                                                    showSearchRecommendations = false
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Clear search",
                                                    tint = TextSecondary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("supabase_search_input")
                                )
                            }

                            // Filter Drawer Toggle Button
                            Surface(
                                onClick = { showFilterDrawer = !showFilterDrawer },
                                shape = RoundedCornerShape(12.dp),
                                color = if (showFilterDrawer || selectedArea != "All Areas") TealDark else SoftSagePill,
                                border = BorderStroke(
                                    1.dp,
                                    if (showFilterDrawer || selectedArea != "All Areas") TealDark else WhisperBorder
                                ),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("toggle_filter_panel_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 11.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Filter PGs",
                                        tint = if (showFilterDrawer || selectedArea != "All Areas") MintSoft else TealDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Filter",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (showFilterDrawer || selectedArea != "All Areas") PureWhite else TealDark
                                    )
                                }
                            }

                            // Map Toggle Button
                            Surface(
                                onClick = { showLiveMapExplorer = !showLiveMapExplorer },
                                shape = RoundedCornerShape(12.dp),
                                color = if (showLiveMapExplorer) TealPrimary else SoftCardSurface,
                                border = BorderStroke(
                                    1.dp,
                                    if (showLiveMapExplorer) TealPrimary else WhisperBorder
                                ),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("toggle_live_map_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 11.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Map,
                                        contentDescription = "Toggle Live Map",
                                        tint = if (showLiveMapExplorer) PureWhite else TealPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Map",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (showLiveMapExplorer) PureWhite else TextPrimary
                                    )
                                }
                            }
                        }

                        // Compact Quick Filter Row (GPS Near Me + Gender/Category + Selected Area Pill + Reset)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                StayEgGpsNearMePillButton(viewModel = viewModel)
                            }

                            items(categoryPills, key = { it.first }) { (catKey, label) ->
                                val isSel = selectedCategory == catKey
                                Surface(
                                    onClick = {
                                        selectedCategory = if (isSel && catKey != "ALL") "ALL" else catKey
                                    },
                                    shape = RoundedCornerShape(999.dp),
                                    color = if (isSel) TealDark else SoftCardSurface,
                                    border = BorderStroke(1.dp, if (isSel) TealDark else WhisperBorder)
                                ) {
                                    Text(
                                        text = label,
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp,
                                        color = if (isSel) PureWhite else TextSecondary,
                                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
                                    )
                                }
                            }

                            if (selectedArea != "All Areas") {
                                item {
                                    Surface(
                                        onClick = {
                                            selectedArea = "All Areas"
                                            viewModel.setSelectedArea("All Areas")
                                        },
                                        shape = RoundedCornerShape(999.dp),
                                        color = SoftSagePill,
                                        border = BorderStroke(1.dp, TealPrimary)
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
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "$selectedArea ×",
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = TealDark
                                            )
                                        }
                                    }
                                }
                            }

                            if (hasActiveFilter) {
                                item {
                                    Text(
                                        text = "Reset",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = TealPrimary,
                                        modifier = Modifier
                                            .clickable {
                                                searchQuery = ""
                                                selectedCity = "Bengaluru"
                                                selectedArea = "All Areas"
                                                onlyAvailableBeds = false
                                                selectedCategory = "ALL"
                                                showSearchRecommendations = false
                                                viewModel.resetFilters()
                                            }
                                            .padding(horizontal = 6.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        // Collapsible Locality Filter Drawer
                        AnimatedVisibility(visible = showFilterDrawer) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            ) {
                                HorizontalDivider(color = WhisperBorder)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Select Locality",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Done",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = TealPrimary,
                                        modifier = Modifier.clickable { showFilterDrawer = false }
                                    )
                                }

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("search_area_chips")
                                ) {
                                    item {
                                        val isAll = selectedArea == "All Areas"
                                        Surface(
                                            onClick = {
                                                selectedArea = "All Areas"
                                                viewModel.setSelectedArea("All Areas")
                                            },
                                            shape = RoundedCornerShape(999.dp),
                                            color = if (isAll) TealDark else SoftCardSurface,
                                            border = BorderStroke(1.dp, if (isAll) TealDark else WhisperBorder)
                                        ) {
                                            Text(
                                                text = "All Areas",
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = if (isAll) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 11.sp,
                                                color = if (isAll) PureWhite else TextPrimary,
                                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                                            )
                                        }
                                    }

                                    items(areaOptions, key = { it }) { areaName ->
                                        val isSelected = selectedArea.equals(areaName, ignoreCase = true)
                                        Surface(
                                            onClick = {
                                                val next = if (isSelected) "All Areas" else areaName
                                                selectedArea = next
                                                viewModel.setSelectedArea(next)
                                            },
                                            shape = RoundedCornerShape(999.dp),
                                            color = if (isSelected) TealPrimary else SoftCardSurface,
                                            border = BorderStroke(1.dp, if (isSelected) TealPrimary else WhisperBorder)
                                        ) {
                                            Text(
                                                text = areaName,
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 11.sp,
                                                color = if (isSelected) PureWhite else TextPrimary,
                                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Instant Search Autocomplete Recommendations Dropdown
                if (searchQuery.isNotEmpty() || showSearchRecommendations) {
                    StayEgSearchRecommendationsDropdown(
                        query = searchQuery,
                        viewModel = viewModel,
                        onSelectArea = { area ->
                            selectedArea = area
                            viewModel.setSelectedArea(area)
                            searchQuery = ""
                            viewModel.setQuery("")
                            showSearchRecommendations = false
                        },
                        onSelectPg = { selectedPg ->
                            showSearchRecommendations = false
                            viewModel.navigateTo(Screen.PGDetail(selectedPg.id))
                        },
                        onViewAllResults = {
                            showSearchRecommendations = false
                        }
                    )
                }
            }
        }

        // 2. Optional Interactive Map Explorer (Map-only integration)
        if (showLiveMapExplorer && displayedPgs.isNotEmpty()) {
            item {
                StayEgInteractivePgMapRadar(
                    pgs = displayedPgs,
                    selectedArea = selectedArea,
                    onSelectPg = { selectedPg ->
                        viewModel.navigateTo(Screen.PGDetail(selectedPg.id))
                    },
                    onLaunchGoogleMapsForPg = { selectedPg ->
                        openGoogleMapsIntent(
                            context,
                            "${selectedPg.name}, ${selectedPg.address}, ${selectedPg.area}, Bengaluru"
                        )
                    },
                    onTrackMapCheckIn = { selectedPg ->
                        viewModel.trackUserActivity(
                            actionType = "SEARCH_MAP_PIN_INSPECT",
                            screenName = "SearchMapExplorer",
                            targetPgId = selectedPg.id,
                            targetPgName = selectedPg.name,
                            details = "Inspected PG location on Search Map Explorer: ${selectedPg.name}",
                            latitude = selectedPg.lat,
                            longitude = selectedPg.lng
                        )
                    }
                )
            }
        }

        // 3. Compact Trusted Public Directory Info Pill with (i) Button
        item {
            StayEgTrustedListingInfoBanner(
                onOwnerClaimClick = { viewModel.navigateTo(Screen.OwnerJoinLead) }
            )
        }

        // 4. Scrollable List of Authentic PG Listings
        if (searchResults.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = PureWhite,
                    border = BorderStroke(1.dp, WhisperBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Matching PG Listings Found",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try searching for Koramangala, HSR Layout, Whitefield, Electronic City, Indiranagar, or BTM Layout.",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                searchQuery = ""
                                selectedCity = "Bengaluru"
                                selectedArea = "All Areas"
                                onlyAvailableBeds = false
                                selectedCategory = "ALL"
                                showSearchRecommendations = false
                                viewModel.resetFilters()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TealDark),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Show All Bengaluru PGs",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                        }
                    }
                }
            }
        } else {
            items(displayedPgs, key = { it.id }) { pg ->
                SupabasePgSearchListCard(
                    pg = pg,
                    distanceKm = viewModel.distanceFromUserKm(pg),
                    onInfoBadgeClick = { showUnverifiedInfoPopup = true },
                    onOpenDetail = { viewModel.navigateTo(Screen.PGDetail(pg.id)) },
                    onAreaClick = { clickedArea ->
                        selectedArea = clickedArea
                        viewModel.setSelectedArea(clickedArea)
                    }
                )
            }

            if (visibleLimit < searchResults.size) {
                item {
                    OutlinedButton(
                        onClick = { visibleLimit += 40 },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, TealPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Text(
                            text = "Load More PG Listings",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary
                        )
                    }
                }
            }
        }
    }

    if (showUnverifiedInfoPopup) {
        StayEgPopupInfoDialog(
            title = "How StayEg Listings Work",
            subtitle = "Trusted Public PG Directory • Zero Brokerage",
            bulletPoints = listOf(
                "Unverified Public Directory Listings: Properties are listed from public locality records across Bengaluru so you can compare rents, photos, and exact map locations.",
                "Inspect via Live Map First: Use the interactive map to visit and verify the room and facilities in person before paying any rent or deposit.",
                "Owner Claim & Verification: PG owners can claim their listing anytime to activate verified room, bed, and QR check-in access."
            ),
            primaryButtonText = "Got It",
            secondaryButtonText = "Owner Portal",
            onDismiss = { showUnverifiedInfoPopup = false },
            onPrimaryClick = { showUnverifiedInfoPopup = false },
            onSecondaryClick = {
                showUnverifiedInfoPopup = false
                viewModel.navigateTo(Screen.OwnerJoinLead)
            }
        )
    }
}

@Composable
fun SupabasePgSearchListCard(
    pg: PG,
    distanceKm: Double = 0.0,
    onInfoBadgeClick: () -> Unit = {},
    onOpenDetail: () -> Unit,
    onAreaClick: (String) -> Unit
) {
    val context = LocalContext.current
    val isVerifiedPg = pg.isVerified && !pg.isDirectoryListing

    val bedroomPhotos = pg.photoGallery["Bedroom"].orEmpty()
    val buildingPhotos = pg.photoGallery["Building"].orEmpty()
    val messPhotos = pg.photoGallery["Food & Mess"].orEmpty()

    val photoSlides = remember(pg.id, pg.imageUrls) {
        buildList {
            val firstRoom = bedroomPhotos.firstOrNull() ?: pg.imageUrls.firstOrNull()
            val firstBuilding = buildingPhotos.firstOrNull() ?: pg.imageUrls.getOrNull(1)
            val firstMess = messPhotos.firstOrNull() ?: pg.imageUrls.getOrNull(2)
            if (!firstRoom.isNullOrBlank()) add("Room Interior" to firstRoom)
            if (!firstBuilding.isNullOrBlank()) add("Building Exterior" to firstBuilding)
            if (!firstMess.isNullOrBlank()) add("Dining & Mess" to firstMess)
            if (isEmpty()) {
                add("Room Interior" to "https://images.unsplash.com/photo-1555854877-bab0e564b8d5?w=800&h=550&fit=crop")
            }
        }
    }

    var selectedPhotoIdx by remember(pg.id) { mutableIntStateOf(0) }
    val currentSlide = photoSlides[selectedPhotoIdx % photoSlides.size]

    val genderLabel = when (pg.gender) {
        PGGender.FEMALE -> "Ladies PG"
        PGGender.MALE -> "Gents PG"
        PGGender.UNISEX -> "Co-Living"
    }

    Card(
        onClick = onOpenDetail,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, if (isVerifiedPg) TealPrimary.copy(alpha = 0.4f) else WhisperBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("search_result_pg_card_${pg.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. Multi-Angle Curated Photo Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(178.dp)
                    .background(SoftCardSurface)
            ) {
                AsyncImage(
                    model = currentSlide.second,
                    contentDescription = "${pg.name} - ${currentSlide.first}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Top-Left: Clickable Unverified Listing (or Owner Verified) + Gender Pill + Distance
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        onClick = onInfoBadgeClick,
                        shape = RoundedCornerShape(999.dp),
                        color = if (isVerifiedPg) TealDark.copy(alpha = 0.92f) else PureWhite.copy(alpha = 0.94f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isVerifiedPg) Icons.Default.Verified else Icons.Default.Info,
                                contentDescription = "Listing Info",
                                tint = if (isVerifiedPg) MintSoft else GoldDark,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isVerifiedPg) "Owner Verified" else "Unverified ⓘ",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = if (isVerifiedPg) PureWhite else PitchBlack
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = PureWhite.copy(alpha = 0.92f)
                    ) {
                        Text(
                            text = genderLabel,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = TealDark,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }

                    if (distanceKm > 0.0) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = TealDark.copy(alpha = 0.88f)
                        ) {
                            Text(
                                text = String.format("%.1f km", distanceKm),
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = MintSoft,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Top-Right: Rating Badge
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
                            fontSize = 10.sp,
                            color = PureWhite
                        )
                    }
                }

                // Bottom-Left: Photo Angle Switcher Pill
                Surface(
                    onClick = { selectedPhotoIdx = (selectedPhotoIdx + 1) % photoSlides.size },
                    shape = RoundedCornerShape(999.dp),
                    color = Color.Black.copy(alpha = 0.68f),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Switch Photo",
                            tint = MintSoft,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = currentSlide.first,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            color = PureWhite
                        )
                    }
                }

                // Bottom-Right: Thumbnail Preview Strip
                if (photoSlides.size > 1) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        photoSlides.forEachIndexed { idx, slide ->
                            val isActive = (selectedPhotoIdx % photoSlides.size) == idx
                            Box(
                                modifier = Modifier
                                    .size(width = 34.dp, height = 26.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isActive) MintSoft else PureWhite.copy(alpha = 0.6f))
                                    .clickable { selectedPhotoIdx = idx }
                                    .padding(if (isActive) 1.5.dp else 0.dp)
                            ) {
                                AsyncImage(
                                    model = slide.second,
                                    contentDescription = slide.first,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(5.dp))
                                )
                            }
                        }
                    }
                }
            }

            // 2. PG Title, Authentic Street Address, Amenities & Map/Detail Actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = pg.name,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onAreaClick(pg.area) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = TealPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = pg.address.ifBlank { "${pg.area}, ${pg.city}" },
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "₹${pg.price}/mo",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = TealDark
                        )
                        Text(
                            text = "Zero Brokerage",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            color = TealPrimary
                        )
                    }
                }

                // Icon-Based Amenity Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    pg.amenities.take(4).forEach { amenity ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SoftSagePill.copy(alpha = 0.65f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = facilityIconFor(amenity),
                                    contentDescription = amenity,
                                    tint = TealPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = amenity,
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp,
                                    color = TealDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Action Row: Map Location + View PG Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            openGoogleMapsIntent(context, "${pg.name} ${pg.address} ${pg.city}")
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, WhisperBorder),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Google Maps",
                            tint = TealPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Map",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = TextPrimary
                        )
                    }

                    Button(
                        onClick = onOpenDetail,
                        colors = ButtonDefaults.buttonColors(containerColor = TealDark),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                    ) {
                        Text(
                            text = "View PG Details & Photos",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = PureWhite
                        )
                    }
                }
            }
        }
    }
}
