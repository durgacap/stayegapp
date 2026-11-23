package com.example.stayeg.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.stayeg.ui.components.PGCard
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

private data class BengaluruHubDestination(
    val areaName: String,
    val startingPrice: String,
    val imageUrl: String
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExploreScreen(viewModel: StayEgViewModel) {
    val context = LocalContext.current
    val filters by viewModel.filters.collectAsState()
    val pgs by viewModel.filteredPgs.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }
    var showUnverifiedInfoPopup by remember { mutableStateOf(false) }
    var showLiveMapRadar by remember { mutableStateOf(true) }
    var showSearchRecommendations by remember { mutableStateOf(false) }

    val bengaluruHubs = remember {
        listOf(
            BengaluruHubDestination(
                areaName = "Koramangala",
                startingPrice = "From ₹ 8,500/mo",
                imageUrl = "https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?w=500&h=400&fit=crop"
            ),
            BengaluruHubDestination(
                areaName = "HSR Layout",
                startingPrice = "From ₹ 8,000/mo",
                imageUrl = "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=500&h=400&fit=crop"
            ),
            BengaluruHubDestination(
                areaName = "Whitefield",
                startingPrice = "From ₹ 7,500/mo",
                imageUrl = "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=500&h=400&fit=crop"
            ),
            BengaluruHubDestination(
                areaName = "Electronic City",
                startingPrice = "From ₹ 6,500/mo",
                imageUrl = "https://images.unsplash.com/photo-1555854877-bab0e564b8d5?w=500&h=400&fit=crop"
            )
        )
    }

    val categoryOptions = remember {
        listOf(
            "ALL" to "All PGs",
            "LADIES" to "Ladies PG",
            "GENTS" to "Gents PG",
            "COLIVING" to "Co-living",
            "UNISEX" to "Unisex PG",
            "HOSTELS" to "Hostels"
        )
    }

    val sharingOptions = remember {
        listOf(
            "ALL" to "All Sharing",
            "SINGLE" to "1 Share",
            "DOUBLE" to "2 Share",
            "TRIPLE" to "3 Share"
        )
    }

    var visibleCount by remember(filters) { mutableIntStateOf(40) }
    val displayedPgs = remember(pgs, visibleCount) { pgs.take(visibleCount) }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(minSize = 158.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(SoftGalleryBg),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 92.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalItemSpacing = 12.dp
    ) {
        // 1. Editorial Header + Compact Search & Filter Bar
        item(span = StaggeredGridItemSpan.FullLine) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Explore Bengaluru",
                            fontFamily = PlayfairFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Curated PGs & co-living spaces • Zero Brokerage",
                            fontFamily = JakartaFontFamily,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            onClick = { viewModel.navigateTo(Screen.Search) },
                            shape = RoundedCornerShape(999.dp),
                            color = TealDark,
                            modifier = Modifier.testTag("open_supabase_search_screen_btn")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ViewList,
                                    contentDescription = "Search List View",
                                    tint = MintSoft,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "List Search",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(SoftSagePill)
                                .clickable { showUnverifiedInfoPopup = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Info",
                                tint = TealPrimary,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        Surface(
                            onClick = {
                                val nextSort = when (filters.sortBy) {
                                    "Rating" -> "Price: Low to High"
                                    "Price: Low to High" -> "Price: High to Low"
                                    else -> "Rating"
                                }
                                viewModel.setSortBy(nextSort)
                            },
                            shape = RoundedCornerShape(999.dp),
                            color = PureWhite,
                            border = BorderStroke(1.dp, WhisperBorder)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Sort, contentDescription = "Sort", modifier = Modifier.size(14.dp), tint = TealPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = filters.sortBy,
                                    fontFamily = JakartaFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Compact Search Field + Filter Sheet Button
                TextField(
                    value = filters.query,
                    onValueChange = {
                        viewModel.setQuery(it)
                        showSearchRecommendations = true
                    },
                    placeholder = {
                        Text(
                            "Search localities, PGs or tech parks...",
                            fontFamily = JakartaFontFamily,
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (filters.query.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        viewModel.setQuery("")
                                        showSearchRecommendations = false
                                    }
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                            IconButton(
                                onClick = { showFilterSheet = true },
                                modifier = Modifier.testTag("open_filters_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Filters",
                                    tint = TealDark,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("explore_search_field"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = PureWhite,
                        unfocusedContainerColor = PureWhite,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )

                // Live Search Recommendations Dropdown
                if (filters.query.isNotEmpty() || showSearchRecommendations) {
                    Spacer(modifier = Modifier.height(8.dp))
                    StayEgSearchRecommendationsDropdown(
                        query = filters.query,
                        viewModel = viewModel,
                        onSelectArea = { area ->
                            viewModel.setSelectedArea(area)
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

                Spacer(modifier = Modifier.height(10.dp))

                // Unified Single-Row Pill Filter Bar with GPS Near Me Button
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        StayEgGpsNearMePillButton(viewModel = viewModel)
                    }

                    item {
                        val isSelected = filters.verificationFilter == "ALL" && !filters.onlyAvailableBeds && filters.pgCategoryType == "ALL" && filters.selectedArea == "All Areas" && !filters.nearMeActive
                        Surface(
                            onClick = { viewModel.resetFilters() },
                            shape = RoundedCornerShape(999.dp),
                            color = if (isSelected) TealDark else PureWhite,
                            border = BorderStroke(1.dp, if (isSelected) TealDark else WhisperBorder)
                        ) {
                            Text(
                                text = "All Listings",
                                fontFamily = JakartaFontFamily,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) PureWhite else TextPrimary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }

                    items(categoryOptions.drop(1)) { (catKey, label) ->
                        val isSelected = filters.pgCategoryType == catKey
                        Surface(
                            onClick = { viewModel.selectPgCategory(if (isSelected) "ALL" else catKey) },
                            shape = RoundedCornerShape(999.dp),
                            color = if (isSelected) TealDark else PureWhite,
                            border = BorderStroke(1.dp, if (isSelected) TealDark else WhisperBorder)
                        ) {
                            Text(
                                text = label,
                                fontFamily = JakartaFontFamily,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) PureWhite else TextPrimary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Horizontal Bengaluru Hub Destination Cards
        item(span = StaggeredGridItemSpan.FullLine) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Popular Bengaluru Hubs",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    if (filters.selectedArea != "All Areas") {
                        Text(
                            text = "Clear (${filters.selectedArea}) ×",
                            fontFamily = JakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = TealPrimary,
                            modifier = Modifier.clickable { viewModel.setSelectedArea("All Areas") }
                        )
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(bengaluruHubs, key = { it.areaName }) { hub ->
                        BengaluruHubGridCard(
                            hub = hub,
                            isSelected = filters.selectedArea.equals(hub.areaName, ignoreCase = true),
                            onClick = {
                                val nextArea = if (filters.selectedArea.equals(hub.areaName, ignoreCase = true)) "All Areas" else hub.areaName
                                viewModel.setSelectedArea(nextArea)
                            },
                            modifier = Modifier.width(142.dp)
                        )
                    }
                }
            }
        }

        // 3. Trusted Public Directory Info Banner + Live Interactive Map Explorer
        item(span = StaggeredGridItemSpan.FullLine) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StayEgTrustedListingInfoBanner(
                    onOwnerClaimClick = { viewModel.navigateTo(Screen.OwnerJoinLead) }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (filters.selectedArea == "All Areas") "Curated PG Listings" else "${filters.selectedArea} PG Listings",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            onClick = { showLiveMapRadar = !showLiveMapRadar },
                            shape = RoundedCornerShape(999.dp),
                            color = if (showLiveMapRadar) TealDark else SoftSagePill,
                            modifier = Modifier.testTag("toggle_explore_map_radar_btn")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null,
                                    tint = if (showLiveMapRadar) SoftGoldAccent else TealPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (showLiveMapRadar) "Hide Map" else "Show Map",
                                    fontFamily = JakartaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = if (showLiveMapRadar) PureWhite else TealPrimary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = SoftSagePill
                        ) {
                            Text(
                                text = "Zero Brokerage",
                                fontFamily = JakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = TealPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                if (showLiveMapRadar && displayedPgs.isNotEmpty()) {
                    StayEgInteractivePgMapRadar(
                        pgs = displayedPgs,
                        selectedArea = filters.selectedArea,
                        onSelectPg = { selectedPg ->
                            viewModel.navigateTo(Screen.PGDetail(selectedPg.id))
                        },
                        onLaunchGoogleMapsForPg = { selectedPg ->
                            viewModel.trackUserActivity(
                                actionType = "EXPLORE_MAP_NAVIGATION",
                                screenName = "ExploreMapRadar",
                                targetPgId = selectedPg.id,
                                targetPgName = selectedPg.name,
                                details = "Opened Google Maps route from Explore Map Explorer for ${selectedPg.name}",
                                latitude = selectedPg.lat,
                                longitude = selectedPg.lng
                            )
                            openGoogleMapsIntent(
                                context,
                                "${selectedPg.name}, ${selectedPg.address}, ${selectedPg.area}, Bangalore"
                            )
                        },
                        onTrackMapCheckIn = { selectedPg ->
                            viewModel.trackUserActivity(
                                actionType = "EXPLORE_MAP_PIN_INSPECT",
                                screenName = "ExploreMapRadar",
                                targetPgId = selectedPg.id,
                                targetPgName = selectedPg.name,
                                details = "Inspected PG pin on Explore Map Explorer: ${selectedPg.name} (${selectedPg.area})",
                                latitude = selectedPg.lat,
                                longitude = selectedPg.lng
                            )
                        }
                    )
                }
            }
        }

        // 4. Empty State or Responsive Staggered Grid of PG Cards
        if (pgs.isEmpty()) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = TextMuted
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Matching PGs Found",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Try resetting filters or selecting All Bengaluru hubs.",
                            fontFamily = JakartaFontFamily,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { viewModel.resetFilters() },
                            colors = ButtonDefaults.buttonColors(containerColor = TealDark),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Reset All Filters", fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, color = PureWhite)
                        }
                    }
                }
            }
        } else {
            items(displayedPgs, key = { it.id }) { pg ->
                PGCard(
                    pg = pg,
                    onCardClick = { viewModel.navigateTo(Screen.PGDetail(pg.id)) },
                    onBookClick = { viewModel.navigateTo(Screen.PGDetail(pg.id)) }
                )
            }

            if (visibleCount < pgs.size) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    OutlinedButton(
                        onClick = { visibleCount += 50 },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, TealPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Load More PG Listings",
                            fontFamily = OutfitFontFamily,
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
                "Unverified Listings: Public directory listings across Bengaluru localities so you can compare rents, photos, and exact map locations.",
                "In-Person Map Check: Always inspect the room and facilities in person using the live map before paying any deposit.",
                "Owner Claim & Verification: PG owners can claim their listing anytime to activate verified room and bed inventory."
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

    // Filter Bottom Sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = PureWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PG Search & Room Filters",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Filter by sharing, monthly rent, and amenities",
                            fontFamily = JakartaFontFamily,
                            fontSize = 11.sp,
                            color = TealPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    TextButton(onClick = { viewModel.resetFilters() }) {
                        Text("Reset All", fontFamily = OutfitFontFamily, color = ErrorRed, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Preferred Room Sharing",
                    fontFamily = OutfitFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    sharingOptions.forEach { (key, label) ->
                        val isSel = filters.preferredSharingFilter == key
                        Surface(
                            onClick = { viewModel.setPreferredSharingFilter(key) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) TealPrimary else SoftCardSurface,
                            border = BorderStroke(1.dp, if (isSel) TealPrimary else WhisperBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = label,
                                fontFamily = OutfitFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) PureWhite else TextPrimary,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Monthly Rent Budget (₹${filters.minPrice} – ₹${filters.maxPrice})",
                    fontFamily = OutfitFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                RangeSlider(
                    value = filters.minPrice.toFloat()..filters.maxPrice.toFloat(),
                    onValueChange = { range ->
                        viewModel.setPriceRange(range.start.toInt(), range.endInclusive.toInt())
                    },
                    valueRange = 5000f..30000f,
                    steps = 24,
                    colors = SliderDefaults.colors(
                        thumbColor = TealPrimary,
                        activeTrackColor = TealPrimary,
                        inactiveTrackColor = SoftSagePill
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Required Facilities",
                    fontFamily = OutfitFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                val availableAmenities = listOf(
                    "WiFi",
                    "Meals",
                    "AC",
                    "Laundry",
                    "Parking",
                    "Housekeeping",
                    "CCTV",
                    "Power Backup",
                    "Gym"
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    availableAmenities.forEach { amenity ->
                        val isChecked = filters.selectedAmenities.contains(amenity)
                        FilterChip(
                            selected = isChecked,
                            onClick = { viewModel.toggleAmenity(amenity) },
                            label = { Text(amenity, fontFamily = OutfitFontFamily, fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = facilityIconFor(amenity),
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TealDark,
                                selectedLabelColor = MintSoft,
                                selectedLeadingIconColor = MintSoft
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = { showFilterSheet = false },
                    colors = ButtonDefaults.buttonColors(containerColor = TealDark),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = "Apply Filters",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun BengaluruHubGridCard(
    hub: BengaluruHubDestination,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var liked by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SoftCardSurface)
        ) {
            AsyncImage(
                model = hub.imageUrl,
                contentDescription = hub.areaName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.28f))
                    .clickable { liked = !liked },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (liked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Save Hub",
                    tint = PureWhite,
                    modifier = Modifier.size(13.dp)
                )
            }

            if (isSelected) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = TealPrimary,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                ) {
                    Text(
                        text = "Selected",
                        fontFamily = JakartaFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = hub.areaName,
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = if (isSelected) TealPrimary else TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = hub.startingPrice,
            fontFamily = JakartaFontFamily,
            fontSize = 11.sp,
            color = TextSecondary
        )
    }
}
