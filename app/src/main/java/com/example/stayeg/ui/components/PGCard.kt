package com.example.stayeg.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.stayeg.R
import com.example.stayeg.data.model.BedStatus
import com.example.stayeg.data.model.PG
import com.example.stayeg.data.model.PGGender
import com.example.stayeg.data.remote.SupabaseService
import com.example.stayeg.ui.theme.*
import kotlin.math.abs

private data class CardAmenityItem(
    val icon: ImageVector,
    val shortLabel: String
)

@Composable
fun PGCard(
    pg: PG,
    onCardClick: () -> Unit,
    onBookClick: () -> Unit,
    modifier: Modifier = Modifier,
    customImageHeight: Dp? = null
) {
    val isUnverified = !pg.isVerified || pg.isDirectoryListing
    var isSaved by remember { mutableStateOf(false) }
    var showUnverifiedPopup by remember { mutableStateOf(false) }

    val availableBedsCount = remember(pg.rooms) {
        pg.rooms.sumOf { room -> room.beds.count { it.status == BedStatus.AVAILABLE } }.coerceAtLeast(4)
    }

    val genderLabel = when (pg.gender) {
        PGGender.FEMALE -> "Ladies"
        PGGender.MALE -> "Gents"
        PGGender.UNISEX -> "Co-Living"
    }

    // Organic staggered image height so cards in LazyVerticalStaggeredGrid have natural visual rhythm
    val staggeredImageHeight = remember(pg.id, customImageHeight) {
        customImageHeight ?: when (abs(pg.id.hashCode()) % 3) {
            0 -> 158.dp
            1 -> 134.dp
            else -> 146.dp
        }
    }

    // Clean 2x2 grid of icon-based amenities (WiFi, Food, AC, Laundry)
    val amenityGridItems = remember(pg.amenities) {
        val defaults = listOf(
            CardAmenityItem(Icons.Default.Wifi, "5G WiFi"),
            CardAmenityItem(Icons.Default.Restaurant, "3x Food"),
            CardAmenityItem(Icons.Default.AcUnit, "AC Room"),
            CardAmenityItem(Icons.Default.LocalLaundryService, "Laundry")
        )
        if (pg.amenities.isEmpty()) {
            defaults
        } else {
            val mapped = pg.amenities.take(4).map { raw ->
                val norm = raw.lowercase()
                val shortName = when {
                    norm.contains("wifi") || norm.contains("wi-fi") -> "5G WiFi"
                    norm.contains("meal") || norm.contains("food") || norm.contains("mess") -> "3x Food"
                    norm.contains("ac") || norm.contains("air") -> "AC Room"
                    norm.contains("laundry") || norm.contains("wash") -> "Laundry"
                    norm.contains("power") || norm.contains("backup") -> "Backup"
                    norm.contains("cctv") || norm.contains("security") -> "CCTV"
                    norm.contains("housekeeping") || norm.contains("clean") -> "Cleaning"
                    norm.contains("parking") -> "Parking"
                    norm.contains("gym") -> "Gym"
                    else -> raw.take(8)
                }
                CardAmenityItem(facilityIconFor(raw), shortName)
            }
            if (mapped.size < 4) {
                (mapped + defaults).distinctBy { it.shortLabel }.take(4)
            } else {
                mapped
            }
        }
    }

    val fallbackDrawableRes = remember(pg.id) {
        when (abs(pg.id.hashCode()) % 5) {
            0 -> R.drawable.img_pg_room_double_1795421747870
            1 -> R.drawable.img_pg_room_single_1795421748285
            2 -> R.drawable.img_pg_building_exterior_1795421746872
            3 -> R.drawable.img_pg_mess_dining_1795421746459
            else -> R.drawable.img_pg_lounge_washroom_1795421747598
        }
    }

    Surface(
        onClick = onCardClick,
        shape = RoundedCornerShape(20.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, WhisperBorder),
        shadowElevation = 3.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("pg_card_${pg.id}")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            val allCardPhotos = remember(pg.id, pg.imageUrls) {
                pg.imageUrls.ifEmpty {
                    listOf(
                        "${SupabaseService.APP_PACKAGE_URI}/img_pg_room_double_1795421747870",
                        "${SupabaseService.APP_PACKAGE_URI}/img_pg_building_exterior_1795421746872",
                        "${SupabaseService.APP_PACKAGE_URI}/img_pg_room_single_1795421748285",
                        "${SupabaseService.APP_PACKAGE_URI}/img_pg_mess_dining_1795421746459",
                        "${SupabaseService.APP_PACKAGE_URI}/img_pg_lounge_washroom_1795421747598"
                    )
                }
            }
            var photoIndex by remember(pg.id) { mutableIntStateOf(0) }

            // 1. High-Quality Swipeable Coil Image Gallery Slider with Subtle Scrim & Micro-Badges
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(staggeredImageHeight)
                    .clip(RoundedCornerShape(15.dp))
                    .background(SoftCardSurface)
            ) {
                StayEgImageGallerySlider(
                    imageUrls = allCardPhotos,
                    pgName = pg.name,
                    sliderHeight = staggeredImageHeight,
                    cornerRadius = 15.dp,
                    showArrows = true,
                    showPaginationDots = true,
                    showCounterBadge = true,
                    enableFullscreenViewer = false,
                    selectedIndex = photoIndex,
                    onPageChanged = { photoIndex = it },
                    testTagPrefix = "pg_card_slider_${pg.id}"
                )

                // Top-Left Gender Pill
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = PureWhite.copy(alpha = 0.92f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = genderLabel,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = TealDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                // Top-Right Floating Save Heart Button
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(PureWhite.copy(alpha = 0.92f))
                        .clickable { isSaved = !isSaved },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Save PG",
                        tint = if (isSaved) TealPrimary else TealDark,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Bottom-Left Listing Status Badge + Bottom-Right Star Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isUnverified) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = PureWhite,
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(13.dp)
                                        .clip(CircleShape)
                                        .background(TealPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Owner Verified",
                                        tint = PureWhite,
                                        modifier = Modifier.size(9.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Owner Verified",
                                    fontFamily = JakartaFontFamily,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TealDark
                                )
                            }
                        }
                    } else {
                        // Unverified Listing Pill
                        Surface(
                            onClick = { showUnverifiedPopup = true },
                            shape = RoundedCornerShape(999.dp),
                            color = SoftGoldPill,
                            shadowElevation = 2.dp,
                            modifier = Modifier.testTag("unverified_alert_badge_${pg.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Unverified Listing Info",
                                    tint = GoldDark,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Unverified Listing",
                                    fontFamily = JakartaFontFamily,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldDark
                                )
                            }
                        }
                    }

                    // Soft Gold Star Rating Pill
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = SoftGoldPill
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = SoftGoldAccent,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = String.format("%.1f", pg.rating),
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = GoldDark
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(9.dp))

            // 2. Concise Title & Authentic Street Address + Locality
            Text(
                text = pg.name,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = pg.address.ifBlank { pg.area },
                    fontFamily = JakartaFontFamily,
                    fontSize = 10.5.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(9.dp))

            // 3. Clean 2x2 Grid of Icon-Based Amenities (WiFi, 3x Food, AC Room, Laundry)
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    amenityGridItems.take(2).forEach { item ->
                        AmenityMiniGridTile(
                            icon = item.icon,
                            label = item.shortLabel,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    amenityGridItems.drop(2).take(2).forEach { item ->
                        AmenityMiniGridTile(
                            icon = item.icon,
                            label = item.shortLabel,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Price + Action Pill Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "₹${pg.price}",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "/mo",
                            fontFamily = JakartaFontFamily,
                            fontSize = 10.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 1.dp)
                        )
                    }
                    Text(
                        text = "₹0 Brokerage",
                        fontFamily = JakartaFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 9.sp,
                        color = TealPrimary
                    )
                }

                Surface(
                    onClick = onBookClick,
                    shape = RoundedCornerShape(999.dp),
                    color = if (!isUnverified) TealDark else SoftSagePill,
                    modifier = Modifier.testTag("pg_card_action_${pg.id}")
                ) {
                    Text(
                        text = if (!isUnverified) "Select Bed" else "View PG",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (!isUnverified) PureWhite else TealDark,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }

    if (showUnverifiedPopup) {
        StayEgPopupInfoDialog(
            title = pg.name,
            subtitle = "${pg.area}, Bengaluru • Unverified Directory Listing",
            bulletPoints = listOf(
                "Unverified Public Listing: Curated from Bengaluru public PG records with locality map, nearby commute hubs, and facility details.",
                "Inspect via Live Map: Check the interactive map route and visit the property in person before paying any rent or deposit.",
                "Zero Brokerage: Direct discovery with no middleman fees."
            ),
            primaryButtonText = "View Details & Map",
            secondaryButtonText = "Close",
            onDismiss = { showUnverifiedPopup = false },
            onPrimaryClick = {
                showUnverifiedPopup = false
                onBookClick()
            },
            onSecondaryClick = { showUnverifiedPopup = false }
        )
    }
}

@Composable
private fun AmenityMiniGridTile(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(9.dp),
        color = SoftCardSurface,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 5.dp, horizontal = 5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = TealPrimary,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontFamily = JakartaFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 9.sp,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
