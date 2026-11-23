package com.example.stayeg.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.stayeg.R
import com.example.stayeg.data.model.Bed
import com.example.stayeg.data.model.BedStatus
import com.example.stayeg.data.model.Room
import com.example.stayeg.data.model.RoomType
import com.example.stayeg.data.remote.SupabaseService
import com.example.stayeg.ui.components.*
import com.example.stayeg.ui.navigation.Screen
import com.example.stayeg.ui.theme.*
import com.example.stayeg.ui.viewmodel.StayEgViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PGDetailScreen(
    pgId: String,
    viewModel: StayEgViewModel
) {
    val context = LocalContext.current
    val allPgs by viewModel.allPgs.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val pg = allPgs.firstOrNull { it.id == pgId } ?: allPgs.first()

    val isVerifiedProperty = pg.isVerified && !pg.isDirectoryListing

    // Build interactive rooms & beds (Floor & Bed Booking Deck)
    val interactiveRooms = remember(pg) {
        if (pg.rooms.isNotEmpty()) {
            pg.rooms
        } else {
            SupabaseService.buildDefaultFloorRoomsStatic(pg.id, pg.price)
        }
    }

    var selectedRoomIndex by remember(interactiveRooms) { mutableIntStateOf(0) }
    val activeRoom = interactiveRooms.getOrElse(selectedRoomIndex) { interactiveRooms.first() }
    var selectedBed by remember(activeRoom) {
        mutableStateOf(
            activeRoom.beds.firstOrNull { it.status == BedStatus.AVAILABLE } ?: activeRoom.beds.first()
        )
    }

    // Categorized Photo Gallery state (with real bundled Bengaluru PG photos)
    val galleryMap = remember(pg) {
        pg.photoGallery.ifEmpty { SupabaseService.buildPhotoGallery(0, pg.imageUrls) }
    }
    val photoCategories = remember(galleryMap) { listOf("All Photos") + galleryMap.keys.toList() }
    var selectedPhotoCategory by remember { mutableStateOf("All Photos") }
    val currentCategoryPhotos = remember(selectedPhotoCategory, galleryMap, pg.imageUrls) {
        if (selectedPhotoCategory == "All Photos") {
            (pg.imageUrls + galleryMap.values.flatten()).distinct()
        } else {
            galleryMap[selectedPhotoCategory] ?: pg.imageUrls
        }
    }
    var activePhotoIndex by remember(currentCategoryPhotos) {
        mutableIntStateOf(0)
    }
    val activePhotoUrl = currentCategoryPhotos.getOrNull(activePhotoIndex)
        ?: currentCategoryPhotos.firstOrNull()
        ?: "${SupabaseService.APP_PACKAGE_URI}/img_pg_room_double_1795421747870"

    // Weekly Mess Menu state
    val messMenu = remember(pg) {
        pg.messMenu.ifEmpty { SupabaseService.buildWeeklyMessMenu() }
    }
    var selectedDay by remember { mutableStateOf("Wed") }
    val activeDayMenu = messMenu.firstOrNull { it.day == selectedDay } ?: messMenu.first()

    // Nearby Places & Commute state
    val nearbyPlaces = remember(pg) {
        pg.nearbyPlaces.ifEmpty { SupabaseService.buildNearbyPlacesForArea(pg.area) }
    }

    // Pop-up modal states
    var showBookBedConfirmModal by remember { mutableStateOf(false) }
    var showUnverifiedAlertModal by remember { mutableStateOf(false) }
    var showPaymentGatewayAlertModal by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftGalleryBg),
            contentPadding = PaddingValues(bottom = 118.dp)
        ) {
            // 1. Hero Gallery Slider & Compact Native Header Card
            item {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(255.dp)
                    ) {
                        StayEgImageGallerySlider(
                            imageUrls = currentCategoryPhotos,
                            pgName = pg.name,
                            sliderHeight = 255.dp,
                            categoryLabel = selectedPhotoCategory,
                            showArrows = true,
                            showPaginationDots = true,
                            showCounterBadge = true,
                            enableFullscreenViewer = true,
                            selectedIndex = activePhotoIndex,
                            onPageChanged = { activePhotoIndex = it },
                            testTagPrefix = "detail_hero_gallery_slider"
                        )

                        // Floating Circular Top Buttons: Back, Booking Promise & Google Maps
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                onClick = { viewModel.goBack() },
                                shape = CircleShape,
                                color = PureWhite.copy(alpha = 0.94f),
                                shadowElevation = 3.dp,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Back",
                                        tint = PitchBlack,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    onClick = { showPaymentGatewayAlertModal = true },
                                    shape = CircleShape,
                                    color = PureWhite.copy(alpha = 0.94f),
                                    shadowElevation = 3.dp,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .testTag("detail_payment_alert_btn")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.VerifiedUser,
                                            contentDescription = "Booking Protection Info",
                                            tint = TealPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Surface(
                                    onClick = {
                                        openGoogleMapsIntent(context, "${pg.name}, ${pg.address}, ${pg.area}, Bangalore")
                                    },
                                    shape = CircleShape,
                                    color = PureWhite.copy(alpha = 0.94f),
                                    shadowElevation = 3.dp,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .testTag("detail_open_maps_btn")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.NearMe,
                                            contentDescription = "Open Google Maps",
                                            tint = TealDark,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Bottom-Left Listing Status Pill Overlapping Hero Image
                        Surface(
                            onClick = {
                                if (isVerifiedProperty) {
                                    showPaymentGatewayAlertModal = true
                                } else {
                                    showUnverifiedAlertModal = true
                                }
                            },
                            shape = RoundedCornerShape(999.dp),
                            color = PureWhite,
                            shadowElevation = 3.dp,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                                .testTag("detail_alert_info_badge")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                if (isVerifiedProperty) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Owner Verified",
                                            tint = PureWhite,
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Unverified Listing Info",
                                        tint = GoldDark,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isVerifiedProperty)
                                        "Owner Verified"
                                    else
                                        "Unverified Listing",
                                    fontFamily = JakartaFontFamily,
                                    color = PitchBlack,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Native App Overview Card (Title, Short Description, Photos & Quick Badges)
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        border = BorderStroke(1.dp, WhisperBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp)
                            .offset(y = (-12).dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = pg.name,
                                        fontFamily = PlayfairFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 21.sp,
                                        color = PitchBlack,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "${pg.address.ifBlank { pg.area }}, Bengaluru",
                                        fontFamily = JakartaFontFamily,
                                        fontSize = 11.5.sp,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Rating Badge
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = SoftGoldPill,
                                    border = BorderStroke(1.dp, SoftGoldAccent)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFF59E0B),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = String.format("%.1f", pg.rating),
                                            fontFamily = OutfitFontFamily,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp,
                                            color = PitchBlack
                                        )
                                    }
                                }
                            }

                            // Short 1-2 line summary description (no long text wall!)
                            if (pg.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = pg.description,
                                    fontFamily = JakartaFontFamily,
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Compact 1-line Trusted Info Pill with (i) popup
                            StayEgTrustedListingInfoBanner(
                                onOwnerClaimClick = { viewModel.claimAndVerifyPg(pg.id, pg.price) }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Photo Category Chips
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(photoCategories) { cat ->
                                    val isSelected = selectedPhotoCategory == cat
                                    Surface(
                                        onClick = { selectedPhotoCategory = cat },
                                        shape = RoundedCornerShape(999.dp),
                                        color = if (isSelected) TealDark else SoftSagePill,
                                        border = BorderStroke(1.dp, if (isSelected) TealDark else WhisperBorder)
                                    ) {
                                        Text(
                                            text = cat,
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) PureWhite else TealDark,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Real Bundled + Property Photo Thumbnails Strip (Synchronized with Gallery Slider)
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(currentCategoryPhotos.size) { photoIdx ->
                                    val photoUrl = currentCategoryPhotos[photoIdx]
                                    val isChosen = photoUrl == activePhotoUrl
                                    AsyncImage(
                                        model = photoUrl,
                                        contentDescription = "Property photograph ${photoIdx + 1}",
                                        placeholder = painterResource(id = R.drawable.img_pg_room_double_1795421747870),
                                        error = painterResource(id = R.drawable.img_pg_room_double_1795421747870),
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(width = 76.dp, height = 54.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .border(
                                                width = if (isChosen) 2.dp else 0.5.dp,
                                                color = if (isChosen) TealPrimary else WhisperBorder,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clickable { activePhotoIndex = photoIdx }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 4-Column Quick Highlights
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                EditorialFacilityBox(
                                    icon = Icons.Default.Wifi,
                                    line1 = "${pg.wifiSpeedMbps} Mbps",
                                    line2 = "Fiber Wi-Fi",
                                    modifier = Modifier.weight(1f)
                                )
                                EditorialFacilityBox(
                                    icon = Icons.Default.Restaurant,
                                    line1 = "3-Time",
                                    line2 = "Homely Food",
                                    modifier = Modifier.weight(1f)
                                )
                                EditorialFacilityBox(
                                    icon = Icons.Default.Bed,
                                    line1 = "Furnished",
                                    line2 = "Rooms & Beds",
                                    modifier = Modifier.weight(1f)
                                )
                                EditorialFacilityBox(
                                    icon = Icons.Default.Videocam,
                                    line1 = "24/7 CCTV",
                                    line2 = "Power Backup",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // 2. INTERACTIVE FLOOR, ROOM & BED BOOKING SYSTEM (Restored Native App UI!)
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, WhisperBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Select Your Room & Bed",
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = PitchBlack
                                )
                                Text(
                                    text = "Tap any room below, choose an available bed & book with Zero Brokerage",
                                    fontFamily = JakartaFontFamily,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = SoftSagePill,
                                border = BorderStroke(1.dp, TealPrimary.copy(alpha = 0.35f))
                            ) {
                                Text(
                                    text = "Floor ${activeRoom.floor}",
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    color = TealDark,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Room Selector Cards (Room 101 Double, Room 102 Single, Room 201 Triple, Room 301 Dorm)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(interactiveRooms.size) { index ->
                                val room = interactiveRooms[index]
                                val isRoomSelected = selectedRoomIndex == index
                                val availableCount = room.beds.count { it.status == BedStatus.AVAILABLE }
                                val roomStartingPrice = room.beds.minOfOrNull { it.price } ?: pg.price

                                Surface(
                                    onClick = {
                                        selectedRoomIndex = index
                                        selectedBed = room.beds.firstOrNull { it.status == BedStatus.AVAILABLE }
                                            ?: room.beds.first()
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isRoomSelected) TealDark else BackgroundLight,
                                    border = BorderStroke(
                                        width = if (isRoomSelected) 1.5.dp else 1.dp,
                                        color = if (isRoomSelected) TealDark else BorderLight
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .width(138.dp)
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Room ${room.roomCode}",
                                                fontFamily = OutfitFontFamily,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 14.sp,
                                                color = if (isRoomSelected) PureWhite else PitchBlack
                                            )
                                            Text(
                                                text = "Flr ${room.floor}",
                                                fontFamily = JakartaFontFamily,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isRoomSelected) MintSoft else TextSecondary
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(3.dp))

                                        Text(
                                            text = "${room.roomType.name.lowercase().replaceFirstChar { it.uppercase() }} • ${if (room.hasAC) "AC" else "Non-AC"}",
                                            fontFamily = JakartaFontFamily,
                                            fontSize = 11.sp,
                                            color = if (isRoomSelected) PureWhite.copy(alpha = 0.85f) else TextSecondary
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "₹$roomStartingPrice",
                                                fontFamily = OutfitFontFamily,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 13.sp,
                                                color = if (isRoomSelected) MintSoft else TealDark
                                            )
                                            Text(
                                                text = "$availableCount open",
                                                fontFamily = OutfitFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                color = if (isRoomSelected) PureWhite else Color(0xFF059669)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Active Room Blueprint Header + Legend
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MeetingRoom,
                                    contentDescription = null,
                                    tint = TealPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Room ${activeRoom.roomCode} Bed Layout (${if (activeRoom.hasAttachedBath) "Attached Bath" else "Common Bath"})",
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = PitchBlack
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                BedLegendDot(color = SoftSagePill, label = "Open")
                                BedLegendDot(color = TealDark, label = "Selected")
                                BedLegendDot(color = Color(0xFFE5E7EB), label = "Booked")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Visual Interactive Bed Cards Deck
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            activeRoom.beds.forEach { bed ->
                                val isAvailable = bed.status == BedStatus.AVAILABLE
                                val isSelected = selectedBed.id == bed.id

                                val cardBg = when {
                                    isSelected -> TealDark
                                    isAvailable -> SoftSagePill
                                    else -> Color(0xFFF3F4F6)
                                }
                                val borderColor = when {
                                    isSelected -> MintSoft
                                    isAvailable -> TealPrimary
                                    else -> Color(0xFFD1D5DB)
                                }

                                Surface(
                                    onClick = {
                                        if (isAvailable) {
                                            selectedBed = bed
                                        }
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    color = cardBg,
                                    border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("bed_tile_${activeRoom.roomCode}_${bed.bedNumber}")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SingleBed,
                                            contentDescription = "Bed #${bed.bedNumber}",
                                            tint = when {
                                                isSelected -> MintSoft
                                                isAvailable -> TealDark
                                                else -> Color(0xFF9CA3AF)
                                            },
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Bed #${bed.bedNumber}",
                                            fontFamily = OutfitFontFamily,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp,
                                            color = when {
                                                isSelected -> PureWhite
                                                isAvailable -> PitchBlack
                                                else -> Color(0xFF9CA3AF)
                                            }
                                        )
                                        Text(
                                            text = "₹${bed.price}/mo",
                                            fontFamily = OutfitFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = when {
                                                isSelected -> MintSoft
                                                isAvailable -> TealDark
                                                else -> Color(0xFF9CA3AF)
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Surface(
                                            shape = RoundedCornerShape(999.dp),
                                            color = when {
                                                isSelected -> MintSoft.copy(alpha = 0.22f)
                                                isAvailable -> PureWhite
                                                else -> Color(0xFFE5E7EB)
                                            }
                                        ) {
                                            Text(
                                                text = when {
                                                    isSelected -> "SELECTED"
                                                    isAvailable -> "AVAILABLE"
                                                    bed.status == BedStatus.RESERVED -> "RESERVED"
                                                    else -> "OCCUPIED"
                                                },
                                                fontFamily = OutfitFontFamily,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 9.sp,
                                                color = when {
                                                    isSelected -> MintSoft
                                                    isAvailable -> Color(0xFF059669)
                                                    else -> Color(0xFF6B7280)
                                                },
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Active Selection Summary & Instant Book Button Inside Card
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = SoftSagePill.copy(alpha = 0.7f),
                            border = BorderStroke(1.dp, WhisperBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Selected: Room ${activeRoom.roomCode} • Bed #${selectedBed.bedNumber}",
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = TealDark
                                    )
                                    Text(
                                        text = "₹${selectedBed.price}/mo • Zero Brokerage • Lock into My Stay",
                                        fontFamily = JakartaFontFamily,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }

                                Button(
                                    onClick = { showBookBedConfirmModal = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = TealDark),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text(
                                        text = "Book Bed #${selectedBed.bedNumber}",
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        color = PureWhite
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. INTERACTIVE GOOGLE MAPS PROPERTY MARKER VIEW
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, WhisperBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        StayEgRapidoMapRouteCard(
                            pg = pg,
                            viewModel = viewModel,
                            onOpenFullGoogleMaps = {
                                openGoogleMapsIntent(context, "${pg.name}, ${pg.address}, ${pg.area}, Bangalore")
                            }
                        )
                    }
                }
            }

            // 4. Included PG Facilities Card
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, WhisperBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Included PG Facilities",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = PitchBlack
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        val hasAc = pg.amenities.any { it.equals("AC", ignoreCase = true) }
                        val hasParking = pg.amenities.any { it.contains("Parking", ignoreCase = true) }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            FacilityVisualCard(
                                title = "High-Speed Wi-Fi",
                                subtitle = "${pg.wifiSpeedMbps} Mbps Broadband",
                                icon = Icons.Default.Wifi,
                                isAvailable = true,
                                modifier = Modifier.weight(1f)
                            )
                            FacilityVisualCard(
                                title = "Homely Food",
                                subtitle = "3-Time Daily Meals",
                                icon = Icons.Default.Restaurant,
                                isAvailable = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            FacilityVisualCard(
                                title = "Washing Machine",
                                subtitle = "Free Laundry Access",
                                icon = Icons.Default.LocalLaundryService,
                                isAvailable = true,
                                modifier = Modifier.weight(1f)
                            )
                            FacilityVisualCard(
                                title = "Housekeeping",
                                subtitle = "Regular Room Cleaning",
                                icon = Icons.Default.CleaningServices,
                                isAvailable = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            FacilityVisualCard(
                                title = "Air Conditioning",
                                subtitle = if (hasAc) "AC Rooms Available" else "Ventilated Rooms",
                                icon = Icons.Default.AcUnit,
                                isAvailable = true,
                                modifier = Modifier.weight(1f)
                            )
                            FacilityVisualCard(
                                title = "2-Wheeler Parking",
                                subtitle = if (hasParking) "Covered Parking" else "Gated Parking",
                                icon = Icons.Default.LocalParking,
                                isAvailable = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 5. Food & Dining Schedule Card
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, WhisperBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Food & Mess Menu",
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = PitchBlack
                                )
                                Text(
                                    text = "Breakfast, lunch, evening tea & dinner included",
                                    fontFamily = JakartaFontFamily,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SoftGoldPill,
                                border = BorderStroke(1.dp, SoftGoldAccent)
                            ) {
                                Text(
                                    text = "${String.format("%.1f", pg.messHygieneRating)}★ Hygiene",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GoldDark,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(messMenu) { dayMenu ->
                                val isDaySelected = selectedDay == dayMenu.day
                                Surface(
                                    onClick = { selectedDay = dayMenu.day },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isDaySelected) TealDark else SoftSagePill,
                                    border = BorderStroke(1.dp, if (isDaySelected) TealDark else WhisperBorder)
                                ) {
                                    Text(
                                        text = dayMenu.day,
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDaySelected) PureWhite else TealDark,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        MealVisualRow(Icons.Default.FreeBreakfast, "Breakfast (7:30 – 9:30 AM)", activeDayMenu.breakfast)
                        MealVisualRow(Icons.Default.LunchDining, "Lunch (12:30 – 2:30 PM)", activeDayMenu.lunch)
                        MealVisualRow(Icons.Default.EmojiFoodBeverage, "Evening Tea (5:00 – 6:00 PM)", activeDayMenu.snacks)
                        MealVisualRow(Icons.Default.DinnerDining, "Dinner (8:00 – 10:00 PM)", activeDayMenu.dinner)
                    }
                }
            }

            // 6. Nearby Landmarks & Commute Card
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, WhisperBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Nearby Landmarks & Commute",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = PitchBlack
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        nearbyPlaces.forEach { place ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BackgroundLight)
                                    .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                                    .clickable {
                                        openGoogleMapsIntent(context, "${place.name}, ${pg.area}, Bangalore")
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(SoftSagePill),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (place.type) {
                                                "TECH_PARK" -> Icons.Default.Business
                                                "METRO" -> Icons.Default.Train
                                                "BUS_STOP" -> Icons.Default.DirectionsBus
                                                "FOOD_MESS" -> Icons.Default.Restaurant
                                                "GYM" -> Icons.Default.FitnessCenter
                                                else -> Icons.Default.LocalHospital
                                            },
                                            contentDescription = null,
                                            tint = TealPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = place.name,
                                            fontFamily = OutfitFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "${place.distanceKm} • ${place.walkMins}m walk • ${place.bikeMins}m drive",
                                            fontFamily = JakartaFontFamily,
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.Directions,
                                    contentDescription = "Directions",
                                    tint = TealPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 7. Property Owner Claim & Live Inventory Management Card
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isVerifiedProperty) SoftSagePill else SoftGoldPill
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isVerifiedProperty) TealPrimary.copy(alpha = 0.4f) else SoftGoldAccent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (isVerifiedProperty) TealDark else GoldDark),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isVerifiedProperty) Icons.Default.Verified else Icons.Default.DomainVerification,
                                        contentDescription = null,
                                        tint = PureWhite,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (isVerifiedProperty)
                                            "Owner Verified Property"
                                        else
                                            "Own or Manage ${pg.name}?",
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = if (isVerifiedProperty) TealDark else GoldDark
                                    )
                                    Text(
                                        text = if (isVerifiedProperty)
                                            "Live room & bed inventory managed on StayEg"
                                        else
                                            "Claim this listing to manage live room & bed inventory",
                                        fontFamily = JakartaFontFamily,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (!isVerifiedProperty) {
                                Button(
                                    onClick = { viewModel.claimAndVerifyPg(pg.id, pg.price) },
                                    colors = ButtonDefaults.buttonColors(containerColor = TealDark),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .testTag("claim_pg_listing_btn")
                                ) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = MintSoft, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Claim & Verify Listing",
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = PureWhite
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.navigateTo(
                                        if (isVerifiedProperty) Screen.OwnerDashboard else Screen.OwnerJoinLead
                                    )
                                },
                                border = BorderStroke(1.2.dp, TealDark),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("open_owner_portal_from_detail_btn")
                            ) {
                                Text(
                                    text = if (isVerifiedProperty) "Owner Dashboard →" else "Partner Portal",
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = TealDark
                                )
                            }
                        }
                    }
                }
            }
        }

        // Sticky Bottom Bar: Shows Active Room & Bed + Google Maps + Book Selected Bed
        Surface(
            color = PureWhite,
            shadowElevation = 14.dp,
            border = BorderStroke(0.5.dp, BorderLight),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(0.95f)) {
                    Text(
                        text = "Room ${activeRoom.roomCode} • Bed #${selectedBed.bedNumber}",
                        fontFamily = JakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = TealPrimary
                    )
                    Text(
                        text = "₹${selectedBed.price} / mo",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = TealDark
                    )
                }

                OutlinedButton(
                    onClick = {
                        openGoogleMapsIntent(context, "${pg.name}, ${pg.address}, ${pg.area}, Bangalore")
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.2.dp, TealDark),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Map",
                        tint = Color(0xFFEA4335),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Map",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TealDark
                    )
                }

                Button(
                    onClick = { showBookBedConfirmModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = TealDark),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.25f)
                        .height(46.dp)
                        .testTag("detail_book_now_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Bed,
                        contentDescription = null,
                        tint = MintSoft,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Book Bed #${selectedBed.bedNumber}",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.5.sp,
                        color = PureWhite
                    )
                }
            }
        }
    }

    // ─── POP-UP INFO & IN-APP BED BOOKING MODALS ─────────────────────────────

    // 1. Free In-App Bed Booking Confirmation Modal
    if (showBookBedConfirmModal) {
        var moveInDate by remember { mutableStateOf("This Week") }
        var notes by remember { mutableStateOf("Booking Room ${activeRoom.roomCode}, Bed #${selectedBed.bedNumber} in StayEg.") }

        AlertDialog(
            onDismissRequest = { showBookBedConfirmModal = false },
            containerColor = PureWhite,
            shape = RoundedCornerShape(24.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(TealDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Bed, contentDescription = null, tint = MintSoft)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Book Bed #${selectedBed.bedNumber} (Free)",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Room ${activeRoom.roomCode} • Floor ${activeRoom.floor} • Zero Brokerage",
                            fontFamily = JakartaFontFamily,
                            fontSize = 11.sp,
                            color = TealPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SoftSagePill,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Zero Brokerage for Tenants: Confirm your bed below to lock Room ${activeRoom.roomCode} • Bed #${selectedBed.bedNumber} and activate your stay inside the 'My Stay' tab!",
                            fontFamily = JakartaFontFamily,
                            fontSize = 12.sp,
                            color = TealDark,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    OutlinedTextField(
                        value = moveInDate,
                        onValueChange = { moveInDate = it },
                        label = { Text("Check-In Date / Timeline") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Stay Preferences") },
                        minLines = 2,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.bookBed(
                            pgId = pg.id,
                            bedId = selectedBed.id,
                            checkInDate = moveInDate,
                            amount = selectedBed.price,
                            pgName = pg.name,
                            roomCode = activeRoom.roomCode,
                            bedNumber = selectedBed.bedNumber
                        )
                        viewModel.expressInterestInPg(
                            pgId = pg.id,
                            pgName = pg.name,
                            area = pg.area,
                            tenantName = currentUser?.name ?: "StayEg Tenant",
                            tenantPhone = currentUser?.phone ?: "In-App Verified",
                            tenantEmail = currentUser?.email ?: "tenant@stayeg.in",
                            preferredSharing = activeRoom.roomType.name,
                            expectedMoveIn = moveInDate,
                            notes = notes
                        )
                        showBookBedConfirmModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Confirm Free Bed Booking →",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        color = PureWhite
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showBookBedConfirmModal = false }) {
                    Text("Cancel", fontFamily = OutfitFontFamily, color = TextSecondary)
                }
            }
        )
    }

    // 2. Unverified Directory Listing Info Modal
    if (showUnverifiedAlertModal) {
        StayEgPopupInfoDialog(
            title = "Unverified Directory Listing • Room ${activeRoom.roomCode}",
            subtitle = "${pg.name} • ${pg.area}",
            bulletPoints = listOf(
                "Public Directory Listing: This property is listed from public directory records and has not yet been verified by its owner.",
                "In-Person Map Check Recommended: Always inspect the property in person using the Google Maps pin before paying any deposit.",
                "Free Bed Booking & Claim: You can reserve Room ${activeRoom.roomCode} • Bed #${selectedBed.bedNumber} at zero brokerage, or owners can claim this listing."
            ),
            primaryButtonText = "Book Bed #${selectedBed.bedNumber}",
            secondaryButtonText = "Owner: Claim PG",
            onDismiss = { showUnverifiedAlertModal = false },
            onPrimaryClick = {
                showUnverifiedAlertModal = false
                showBookBedConfirmModal = true
            },
            onSecondaryClick = {
                viewModel.claimAndVerifyPg(pg.id, pg.price)
                showUnverifiedAlertModal = false
            }
        )
    }

    // 3. Zero-Brokerage Booking Protection Modal
    if (showPaymentGatewayAlertModal) {
        StayEgPopupInfoDialog(
            title = "Zero-Brokerage Booking Promise",
            subtitle = "Trusted PG Discovery & Direct Check-In",
            bulletPoints = listOf(
                "No Advance Payment Required: Selecting and booking your room & bed on StayEg is completely free.",
                "Direct Property Check-In: Pay rent and refundable deposit directly at the PG only after physically inspecting your room via Google Maps.",
                "Resident Dashboard: Manage meals, Wi-Fi, and maintenance tickets in your My Stay dashboard."
            ),
            primaryButtonText = "Share Feedback",
            secondaryButtonText = "Close",
            onDismiss = { showPaymentGatewayAlertModal = false },
            onPrimaryClick = {
                showPaymentGatewayAlertModal = false
                viewModel.navigateTo(Screen.FeedbackHub)
            },
            onSecondaryClick = { showPaymentGatewayAlertModal = false }
        )
    }
}

@Composable
private fun BedLegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, TealDark, CircleShape)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = label,
            fontFamily = OutfitFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

@Composable
private fun MealVisualRow(icon: ImageVector, slot: String, itemsText: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundLight)
            .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SoftSagePill),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(slot, fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
            Text(itemsText, fontFamily = JakartaFontFamily, fontSize = 12.sp, color = TextSecondary)
        }
    }
}
