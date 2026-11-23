package com.example.stayeg.ui.components

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.stayeg.data.model.PG
import com.example.stayeg.ui.theme.*
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

// Authentic Google Maps Daylight & Hybrid Color Palette
private val GMapLandDaylight = Color(0xFFF8F9FA)
private val GMapUrbanBlock = Color(0xFFF1F3F4)
private val GMapParkGreen = Color(0xFFCEEAD6)
private val GMapParkAccent = Color(0xFFA8DAB5)
private val GMapWaterBlue = Color(0xFFA0C3FF)
private val GMapHighwayFill = Color(0xFFFDE293)
private val GMapHighwayStroke = Color(0xFFF6AD01)
private val GMapStreetWhite = Color(0xFFFFFFFF)
private val GMapStreetBorder = Color(0xFFDADCE0)
private val GMapMetroPurple = Color(0xFF8E24AA)
private val GMapMetroGreen = Color(0xFF1E8E3E)
private val GMapPinRed = Color(0xFFEA4335)
private val GMapPinDarkRed = Color(0xFFB31412)
private val GMapBlueDot = Color(0xFF4285F4)

private val TrianglePointerShape = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width / 2f, size.height)
    close()
}

private data class ProjectedMarker(
    val pg: PG,
    val normX: Float,
    val normY: Float
)

private data class BengaluruMapZoneLabel(
    val name: String,
    val normX: Float,
    val normY: Float
)

private val BengaluruZoneLabels = listOf(
    BengaluruMapZoneLabel("INDIRANAGAR", 0.56f, 0.24f),
    BengaluruMapZoneLabel("WHITEFIELD", 0.84f, 0.28f),
    BengaluruMapZoneLabel("MARATHAHALLI", 0.72f, 0.44f),
    BengaluruMapZoneLabel("KORAMANGALA", 0.44f, 0.52f),
    BengaluruMapZoneLabel("BTM LAYOUT", 0.34f, 0.66f),
    BengaluruMapZoneLabel("HSR LAYOUT", 0.54f, 0.68f),
    BengaluruMapZoneLabel("ELECTRONIC CITY", 0.64f, 0.86f),
    BengaluruMapZoneLabel("NAGAVARA", 0.36f, 0.18f)
)

/**
 * Projects a list of PG listings onto normalized (0.12..0.88) map coordinates using their
 * real latitude and longitude, with subtle collision separation so every marker pin on the
 * Google Maps view is clearly visible and tappable.
 */
private fun projectPgsToMapViewport(pgs: List<PG>): List<ProjectedMarker> {
    if (pgs.isEmpty()) return emptyList()
    if (pgs.size == 1) {
        return listOf(ProjectedMarker(pgs.first(), 0.50f, 0.48f))
    }

    val lats = pgs.map { it.lat }
    val lngs = pgs.map { it.lng }
    val minLat = lats.minOrNull() ?: 12.84
    val maxLat = lats.maxOrNull() ?: 13.04
    val minLng = lngs.minOrNull() ?: 77.58
    val maxLng = lngs.maxOrNull() ?: 77.75

    val latSpan = (maxLat - minLat).coerceAtLeast(0.012)
    val lngSpan = (maxLng - minLng).coerceAtLeast(0.012)

    val rawPositions = pgs.mapIndexed { index, pg ->
        val rawX = ((pg.lng - minLng) / lngSpan).toFloat().coerceIn(0f, 1f)
        // Higher latitude is further North (top of map -> smaller Y)
        val rawY = (1.0 - ((pg.lat - minLat) / latSpan)).toFloat().coerceIn(0f, 1f)

        // Add slight deterministic offset when multiple PGs share similar locality coordinates
        val goldenAngle = index * 2.399963f
        val jitterRadius = 0.055f * ((index % 4) + 1) * 0.35f
        val nx = (0.14f + rawX * 0.72f + cos(goldenAngle) * jitterRadius).coerceIn(0.12f, 0.88f)
        val ny = (0.16f + rawY * 0.66f + sin(goldenAngle) * jitterRadius).coerceIn(0.14f, 0.82f)
        ProjectedMarker(pg, nx, ny)
    }.toMutableList()

    // Simple 2-pass relaxation to prevent marker overlap
    for (pass in 0..1) {
        for (i in rawPositions.indices) {
            for (j in i + 1 until rawPositions.size) {
                val a = rawPositions[i]
                val b = rawPositions[j]
                val dx = b.normX - a.normX
                val dy = b.normY - a.normY
                val distSq = dx * dx + dy * dy
                val minSep = 0.13f
                if (distSq < minSep * minSep) {
                    val angle = (i * 1.7f + j * 0.9f)
                    val pushX = cos(angle) * 0.055f
                    val pushY = sin(angle) * 0.055f
                    rawPositions[j] = b.copy(
                        normX = (b.normX + pushX).coerceIn(0.12f, 0.88f),
                        normY = (b.normY + pushY).coerceIn(0.14f, 0.82f)
                    )
                }
            }
        }
    }

    return rawPositions
}

/**
 * Professional Interactive Google Maps Marker View for PG Listings Pages (Explore, Search, Home).
 * Replaces booking-style UI with a clean marker-based Google Maps experience:
 * - Authentic Google Maps daylight street cartography + optional Live Google Maps WebView embed
 * - Directly clickable price & pin markers projected by real (lat, lng) coordinates
 * - Pan/drag gestures + floating Google Maps zoom (+ / -) and recenter controls
 * - Clean selected-marker preview card with street address, rent, and 1-tap Google Maps launch
 */
@Composable
fun StayEgGoogleMapsListingsView(
    pgs: List<PG>,
    selectedArea: String,
    onSelectPg: (PG) -> Unit,
    onLaunchGoogleMapsForPg: (PG) -> Unit,
    onTrackMapCheckIn: (PG) -> Unit = {},
    modifier: Modifier = Modifier,
    defaultMapHeight: Dp = 265.dp
) {
    val visibleMapPgs = remember(pgs) { pgs.take(14) }
    var activePg by remember(visibleMapPgs) { mutableStateOf(visibleMapPgs.firstOrNull()) }
    var zoomLevel by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var isExpandedMap by remember { mutableStateOf(false) }
    var useLiveGoogleWebView by remember { mutableStateOf(false) }
    var mapCanvasSize by remember { mutableStateOf(IntSize.Zero) }

    val projectedMarkers = remember(visibleMapPgs) {
        projectPgsToMapViewport(visibleMapPgs)
    }

    val currentMapHeight = if (isExpandedMap) 380.dp else defaultMapHeight

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(1.dp, WhisperBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("explore_interactive_map_radar")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // 1. Clean Google Maps Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFE8F0FE),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Google Maps Marker View",
                                tint = GMapPinRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (selectedArea == "All Areas" || selectedArea.isBlank())
                                "Bengaluru PG Listings Map"
                            else
                                "$selectedArea PG Markers",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Tap any marker pin to inspect PG details & open Google Maps",
                            fontFamily = JakartaFontFamily,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Toggle between Interactive Multi-Marker Map and Embedded Google Maps WebView
                    Surface(
                        onClick = { useLiveGoogleWebView = !useLiveGoogleWebView },
                        shape = RoundedCornerShape(999.dp),
                        color = if (useLiveGoogleWebView) Color(0xFFE8F0FE) else SoftCardSurface,
                        border = BorderStroke(
                            1.dp,
                            if (useLiveGoogleWebView) GMapBlueDot else WhisperBorder
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = "Toggle Live Google Embed",
                                tint = if (useLiveGoogleWebView) GMapBlueDot else TealPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (useLiveGoogleWebView) "Marker Pins" else "Satellite / Tile",
                                fontFamily = JakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = if (useLiveGoogleWebView) GMapBlueDot else TextPrimary
                            )
                        }
                    }

                    // Expand / Collapse Map Height
                    Surface(
                        onClick = { isExpandedMap = !isExpandedMap },
                        shape = RoundedCornerShape(999.dp),
                        color = SoftSagePill
                    ) {
                        Icon(
                            imageVector = if (isExpandedMap) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = "Expand Map",
                            tint = TealDark,
                            modifier = Modifier
                                .padding(horizontal = 7.dp, vertical = 5.dp)
                                .size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Interactive Google Maps Viewport (Multi-Marker Canvas OR Live Embedded Google Maps WebView)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(currentMapHeight)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GMapLandDaylight)
                    .border(1.dp, GMapStreetBorder, RoundedCornerShape(16.dp))
                    .onSizeChanged { mapCanvasSize = it }
                    .pointerInput(useLiveGoogleWebView) {
                        if (!useLiveGoogleWebView) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val maxPan = 140f * zoomLevel
                                panOffset = Offset(
                                    x = (panOffset.x + dragAmount.x).coerceIn(-maxPan, maxPan),
                                    y = (panOffset.y + dragAmount.y).coerceIn(-maxPan, maxPan)
                                )
                            }
                        }
                    }
            ) {
                if (useLiveGoogleWebView && activePg != null) {
                    val targetPg = activePg!!
                    GoogleMapsEmbeddedWebView(
                        queryOrCoords = "${targetPg.lat},${targetPg.lng}",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Authentic Google Maps Daylight Street, Park, Lake & Arterial Highway Canvas
                    GoogleMapsDaylightStreetCanvas(
                        zoomLevel = zoomLevel,
                        panOffset = panOffset,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Locality Labels Overlay on the Map
                    val density = LocalDensity.current
                    if (mapCanvasSize.width > 0 && mapCanvasSize.height > 0) {
                        BengaluruZoneLabels.forEach { zone ->
                            val zx = ((zone.normX - 0.5f) * zoomLevel + 0.5f) * mapCanvasSize.width + panOffset.x
                            val zy = ((zone.normY - 0.5f) * zoomLevel + 0.5f) * mapCanvasSize.height + panOffset.y
                            if (zx in 24f..(mapCanvasSize.width - 80f) && zy in 16f..(mapCanvasSize.height - 28f)) {
                                Text(
                                    text = zone.name,
                                    fontFamily = JakartaFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 8.5.sp,
                                    letterSpacing = 0.8.sp,
                                    color = Color(0xFF5F6368).copy(alpha = 0.78f),
                                    modifier = Modifier.offset {
                                        IntOffset(zx.roundToInt(), zy.roundToInt())
                                    }
                                )
                            }
                        }

                        // Render Clickable Google Maps PG Marker Pins at Projected Coordinates
                        projectedMarkers.forEach { marker ->
                            val isSelected = activePg?.id == marker.pg.id
                            val px = ((marker.normX - 0.5f) * zoomLevel + 0.5f) * mapCanvasSize.width + panOffset.x
                            val py = ((marker.normY - 0.5f) * zoomLevel + 0.5f) * mapCanvasSize.height + panOffset.y

                            val clampedX = px.coerceIn(
                                with(density) { 42.dp.toPx() },
                                (mapCanvasSize.width - with(density) { 48.dp.toPx() }).coerceAtLeast(50f)
                            )
                            val clampedY = py.coerceIn(
                                with(density) { 28.dp.toPx() },
                                (mapCanvasSize.height - with(density) { 36.dp.toPx() }).coerceAtLeast(40f)
                            )

                            val markerHalfW = with(density) { (if (isSelected) 46.dp else 36.dp).toPx() }
                            val markerH = with(density) { (if (isSelected) 36.dp else 28.dp).toPx() }

                            GoogleMapPgMarkerPin(
                                pg = marker.pg,
                                isSelected = isSelected,
                                onClick = {
                                    activePg = marker.pg
                                    onTrackMapCheckIn(marker.pg)
                                },
                                modifier = Modifier.offset {
                                    IntOffset(
                                        x = (clampedX - markerHalfW).roundToInt(),
                                        y = (clampedY - markerH).roundToInt()
                                    )
                                }
                            )
                        }
                    }
                }

                // Top-Left Active Locality & Marker Pill Overlay
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = PureWhite.copy(alpha = 0.95f),
                    shadowElevation = 3.dp,
                    border = BorderStroke(0.5.dp, GMapStreetBorder),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(GMapPinRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedArea == "All Areas" || selectedArea.isBlank())
                                "Bengaluru • Interactive Map"
                            else
                                "$selectedArea • Map View",
                            fontFamily = JakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            color = Color(0xFF202124)
                        )
                    }
                }

                // Bottom-Left Google Maps Wordmark Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = PureWhite.copy(alpha = 0.90f),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = GMapBlueDot,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Google Maps",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp,
                            color = Color(0xFF5F6368)
                        )
                    }
                }

                // Right-Side Floating Google Maps Zoom & Recenter Controls (+ / - / Recenter)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        onClick = {
                            panOffset = Offset.Zero
                            zoomLevel = 1f
                        },
                        shape = CircleShape,
                        color = PureWhite,
                        shadowElevation = 4.dp,
                        border = BorderStroke(0.5.dp, GMapStreetBorder),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Recenter Map",
                                tint = GMapBlueDot,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PureWhite,
                        shadowElevation = 4.dp,
                        border = BorderStroke(0.5.dp, GMapStreetBorder)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clickable {
                                        zoomLevel = (zoomLevel + 0.25f).coerceAtMost(1.85f)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Zoom In",
                                    tint = Color(0xFF3C4043),
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            HorizontalDivider(
                                color = GMapStreetBorder,
                                modifier = Modifier.width(24.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clickable {
                                        zoomLevel = (zoomLevel - 0.25f).coerceAtLeast(0.75f)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Zoom Out",
                                    tint = Color(0xFF3C4043),
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Horizontal Marker Selector Strip (Syncs with pins on the map)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                visibleMapPgs.forEach { itemPg ->
                    val isSelected = activePg?.id == itemPg.id
                    Surface(
                        onClick = {
                            activePg = itemPg
                            onTrackMapCheckIn(itemPg)
                        },
                        shape = RoundedCornerShape(999.dp),
                        color = if (isSelected) TealDark else SoftCardSurface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) TealDark else WhisperBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (isSelected) Color(0xFFFF8A80) else GMapPinRed,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${itemPg.name.take(18)} • ₹${itemPg.price}",
                                fontFamily = JakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                color = if (isSelected) PureWhite else TextPrimary
                            )
                        }
                    }
                }
            }

            // 4. Clean Google Maps Selected Marker Info Card
            activePg?.let { selectedPg ->
                val isVerifiedPg = selectedPg.isVerified && !selectedPg.isDirectoryListing
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SoftCardSurface,
                    border = BorderStroke(1.dp, WhisperBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Thumbnail Image
                        AsyncImage(
                            model = selectedPg.imageUrls.firstOrNull()
                                ?: "https://images.unsplash.com/photo-1555854877-bab0e564b8d5?w=400&h=300&fit=crop",
                            contentDescription = selectedPg.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(68.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PureWhite)
                                .clickable { onSelectPg(selectedPg) }
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        // Title, Street Address, Badge & Rent
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = if (isVerifiedPg) SoftSagePill else SoftGoldPill
                                ) {
                                    Text(
                                        text = if (isVerifiedPg) "Owner Verified" else "Unverified Listing",
                                        fontFamily = JakartaFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 8.5.sp,
                                        color = if (isVerifiedPg) TealDark else GoldDark,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = String.format("%.1f", selectedPg.rating),
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = TextPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = selectedPg.name,
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.5.sp,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = selectedPg.address.ifBlank { "${selectedPg.area}, Bengaluru" },
                                fontFamily = JakartaFontFamily,
                                fontSize = 10.5.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "₹${selectedPg.price}/mo • Zero Brokerage",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = TealDark
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Action Buttons: Google Maps Directions & View Listing
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Surface(
                                onClick = {
                                    onTrackMapCheckIn(selectedPg)
                                    onSelectPg(selectedPg)
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = TealDark
                            ) {
                                Text(
                                    text = "View PG",
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = PureWhite,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }

                            Surface(
                                onClick = { onLaunchGoogleMapsForPg(selectedPg) },
                                shape = RoundedCornerShape(10.dp),
                                color = PureWhite,
                                border = BorderStroke(1.dp, WhisperBorder)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Directions,
                                        contentDescription = "Open in Google Maps",
                                        tint = GMapBlueDot,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Maps",
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.5.sp,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Single-Property Google Maps Marker Location Card for PGDetailScreen.
 * Replaces the old pickup/booking route UI with a clean marker-based Google Maps location view.
 */
@Composable
fun StayEgPropertyGoogleMapCard(
    pg: PG,
    onOpenFullGoogleMaps: () -> Unit,
    modifier: Modifier = Modifier
) {
    var zoomLevel by remember { mutableFloatStateOf(1.1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var useLiveGoogleWebView by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(1.dp, WhisperBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("rapido_in_app_map_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFE8F0FE),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Property Google Map Location",
                                tint = GMapPinRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Property Location on Google Maps",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = pg.address.ifBlank { "${pg.area}, Bengaluru" },
                            fontFamily = JakartaFontFamily,
                            fontSize = 11.5.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    onClick = { useLiveGoogleWebView = !useLiveGoogleWebView },
                    shape = RoundedCornerShape(999.dp),
                    color = if (useLiveGoogleWebView) Color(0xFFE8F0FE) else SoftCardSurface,
                    border = BorderStroke(
                        1.dp,
                        if (useLiveGoogleWebView) GMapBlueDot else WhisperBorder
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = "Toggle Live Google Map",
                            tint = if (useLiveGoogleWebView) GMapBlueDot else TealPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (useLiveGoogleWebView) "Marker View" else "Live Tile",
                            fontFamily = JakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = if (useLiveGoogleWebView) GMapBlueDot else TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Google Maps Viewport Centered on the PG Marker Pin
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GMapLandDaylight)
                    .border(1.dp, GMapStreetBorder, RoundedCornerShape(16.dp))
                    .pointerInput(useLiveGoogleWebView) {
                        if (!useLiveGoogleWebView) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                panOffset = Offset(
                                    x = (panOffset.x + dragAmount.x).coerceIn(-120f, 120f),
                                    y = (panOffset.y + dragAmount.y).coerceIn(-120f, 120f)
                                )
                            }
                        }
                    }
            ) {
                if (useLiveGoogleWebView) {
                    GoogleMapsEmbeddedWebView(
                        queryOrCoords = "${pg.lat},${pg.lng}",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    GoogleMapsDaylightStreetCanvas(
                        zoomLevel = zoomLevel,
                        panOffset = panOffset,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Center Google Maps Marker Pin for this PG
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset {
                                IntOffset(panOffset.x.roundToInt(), panOffset.y.roundToInt())
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        GoogleMapPgMarkerPin(
                            pg = pg,
                            isSelected = true,
                            onClick = onOpenFullGoogleMaps
                        )
                    }
                }

                // Top-Left Locality Pill
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = PureWhite.copy(alpha = 0.95f),
                    shadowElevation = 3.dp,
                    border = BorderStroke(0.5.dp, GMapStreetBorder),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = GMapPinRed,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${pg.area} • ${String.format("%.4f", pg.lat)}, ${String.format("%.4f", pg.lng)}",
                            fontFamily = JakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color(0xFF202124)
                        )
                    }
                }

                // Right-Side Google Maps Zoom Controls
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        onClick = {
                            panOffset = Offset.Zero
                            zoomLevel = 1.1f
                        },
                        shape = CircleShape,
                        color = PureWhite,
                        shadowElevation = 4.dp,
                        border = BorderStroke(0.5.dp, GMapStreetBorder),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Recenter",
                                tint = GMapBlueDot,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PureWhite,
                        shadowElevation = 4.dp,
                        border = BorderStroke(0.5.dp, GMapStreetBorder)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { zoomLevel = (zoomLevel + 0.2f).coerceAtMost(1.8f) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color(0xFF3C4043), modifier = Modifier.size(16.dp))
                            }
                            HorizontalDivider(color = GMapStreetBorder, modifier = Modifier.width(22.dp))
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { zoomLevel = (zoomLevel - 0.2f).coerceAtLeast(0.8f) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color(0xFF3C4043), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Open in Google Maps CTA
            Button(
                onClick = onOpenFullGoogleMaps,
                colors = ButtonDefaults.buttonColors(containerColor = TealDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("open_live_google_maps_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = null,
                    tint = MintSoft,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Open Location in Google Maps",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = PureWhite
                )
            }
        }
    }
}

/**
 * Clickable Google Maps Price & Pin Marker rendered at a PG's projected (x, y) map coordinates.
 */
@Composable
private fun GoogleMapPgMarkerPin(
    pg: PG,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "marker_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .scale(scale)
            .clickable { onClick() }
            .testTag("google_map_marker_${pg.id}")
    ) {
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = if (isSelected) GMapPinRed else PureWhite,
            border = BorderStroke(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) PureWhite else GMapPinRed.copy(alpha = 0.75f)
            ),
            shadowElevation = if (isSelected) 6.dp else 3.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(
                    horizontal = if (isSelected) 9.dp else 7.dp,
                    vertical = if (isSelected) 4.dp else 3.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = pg.name,
                    tint = if (isSelected) PureWhite else GMapPinRed,
                    modifier = Modifier.size(if (isSelected) 13.dp else 11.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = if (isSelected)
                        "${pg.name.take(12)} • ₹${pg.price / 1000}k"
                    else
                        "₹${pg.price / 1000}k",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = if (isSelected) 10.sp else 9.5.sp,
                    color = if (isSelected) PureWhite else Color(0xFF202124),
                    maxLines = 1
                )
            }
        }

        // Pointer Triangle + Ground Shadow Dot
        Box(
            modifier = Modifier
                .size(width = 8.dp, height = 5.dp)
                .background(
                    color = if (isSelected) GMapPinDarkRed else GMapPinRed,
                    shape = TrianglePointerShape
                )
        )
        Box(
            modifier = Modifier
                .size(width = 8.dp, height = 3.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.22f))
        )
    }
}

/**
 * Authentic Google Maps Daylight Street, Park, Lake, Metro & Arterial Highway Canvas.
 */
@Composable
private fun GoogleMapsDaylightStreetCanvas(
    zoomLevel: Float,
    panOffset: Offset,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f + panOffset.x
        val cy = h * 0.5f + panOffset.y

        // 1. Base Daylight Land Fill
        drawRect(color = GMapLandDaylight)

        // 2. Urban City Block Polygons
        val blockOffsets = listOf(
            Offset(-0.32f, -0.28f) to Size(0.24f, 0.18f),
            Offset(0.06f, -0.30f) to Size(0.28f, 0.20f),
            Offset(-0.36f, 0.04f) to Size(0.22f, 0.24f),
            Offset(0.10f, 0.08f) to Size(0.26f, 0.22f)
        )
        blockOffsets.forEach { (relPos, relSize) ->
            drawRoundRect(
                color = GMapUrbanBlock,
                topLeft = Offset(
                    cx + relPos.x * w * zoomLevel,
                    cy + relPos.y * h * zoomLevel
                ),
                size = Size(relSize.width * w * zoomLevel, relSize.height * h * zoomLevel),
                cornerRadius = CornerRadius(12f, 12f)
            )
        }

        // 3. Google Maps Green Parks (Cubbon Park / Agara Green Belt / HSR Sector Parks)
        drawRoundRect(
            color = GMapParkGreen,
            topLeft = Offset(cx - w * 0.38f * zoomLevel, cy - h * 0.34f * zoomLevel),
            size = Size(w * 0.22f * zoomLevel, h * 0.20f * zoomLevel),
            cornerRadius = CornerRadius(24f, 24f)
        )
        drawRoundRect(
            color = GMapParkGreen,
            topLeft = Offset(cx + w * 0.14f * zoomLevel, cy + h * 0.14f * zoomLevel),
            size = Size(w * 0.24f * zoomLevel, h * 0.19f * zoomLevel),
            cornerRadius = CornerRadius(22f, 22f)
        )
        drawCircle(
            color = GMapParkAccent.copy(alpha = 0.55f),
            radius = 28f * zoomLevel,
            center = Offset(cx - w * 0.27f * zoomLevel, cy - h * 0.24f * zoomLevel)
        )

        // 4. Google Maps Lakes (Bellandur Lake & Agara Lake)
        val lakePath = Path().apply {
            moveTo(cx + w * 0.05f * zoomLevel, cy - h * 0.02f * zoomLevel)
            cubicTo(
                cx + w * 0.18f * zoomLevel, cy - h * 0.10f * zoomLevel,
                cx + w * 0.30f * zoomLevel, cy - h * 0.04f * zoomLevel,
                cx + w * 0.26f * zoomLevel, cy + h * 0.07f * zoomLevel
            )
            cubicTo(
                cx + w * 0.18f * zoomLevel, cy + h * 0.12f * zoomLevel,
                cx + w * 0.06f * zoomLevel, cy + h * 0.08f * zoomLevel,
                cx + w * 0.05f * zoomLevel, cy - h * 0.02f * zoomLevel
            )
            close()
        }
        drawPath(path = lakePath, color = GMapWaterBlue)

        drawRoundRect(
            color = GMapWaterBlue,
            topLeft = Offset(cx - w * 0.14f * zoomLevel, cy + h * 0.22f * zoomLevel),
            size = Size(w * 0.16f * zoomLevel, h * 0.11f * zoomLevel),
            cornerRadius = CornerRadius(28f, 28f)
        )

        // 5. Secondary Sector Streets Grid (White roads with subtle gray borders)
        val streetSteps = listOf(-0.36f, -0.22f, -0.08f, 0.06f, 0.20f, 0.34f)
        streetSteps.forEach { factor ->
            val y = cy + factor * h * zoomLevel
            drawLine(
                color = GMapStreetBorder,
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 6.5f * zoomLevel
            )
            drawLine(
                color = GMapStreetWhite,
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 4.5f * zoomLevel
            )

            val x = cx + factor * w * zoomLevel
            drawLine(
                color = GMapStreetBorder,
                start = Offset(x, 0f),
                end = Offset(x, h),
                strokeWidth = 6.5f * zoomLevel
            )
            drawLine(
                color = GMapStreetWhite,
                start = Offset(x, 0f),
                end = Offset(x, h),
                strokeWidth = 4.5f * zoomLevel
            )
        }

        // 6. Google Maps Yellow Arterial Highways (Outer Ring Road & Hosur Road NH-44)
        val orrHighway = Path().apply {
            moveTo(0f, cy + h * 0.28f * zoomLevel)
            quadraticTo(
                cx + w * 0.06f * zoomLevel,
                cy + h * 0.12f * zoomLevel,
                w,
                cy - h * 0.26f * zoomLevel
            )
        }
        drawPath(
            path = orrHighway,
            color = GMapHighwayStroke,
            style = Stroke(width = 13f * zoomLevel, cap = StrokeCap.Round)
        )
        drawPath(
            path = orrHighway,
            color = GMapHighwayFill,
            style = Stroke(width = 9.5f * zoomLevel, cap = StrokeCap.Round)
        )

        val hosurHighway = Path().apply {
            moveTo(cx - w * 0.28f * zoomLevel, 0f)
            quadraticTo(
                cx - w * 0.05f * zoomLevel,
                cy,
                cx + w * 0.24f * zoomLevel,
                h
            )
        }
        drawPath(
            path = hosurHighway,
            color = GMapHighwayStroke,
            style = Stroke(width = 12f * zoomLevel, cap = StrokeCap.Round)
        )
        drawPath(
            path = hosurHighway,
            color = GMapHighwayFill,
            style = Stroke(width = 8.5f * zoomLevel, cap = StrokeCap.Round)
        )

        // 7. Namma Metro Transit Line (Subtle Google Maps Transit Layer)
        drawLine(
            color = GMapMetroPurple.copy(alpha = 0.55f),
            start = Offset(0f, cy - h * 0.18f * zoomLevel),
            end = Offset(w, cy - h * 0.22f * zoomLevel),
            strokeWidth = 2.5f * zoomLevel
        )
        drawLine(
            color = GMapMetroGreen.copy(alpha = 0.50f),
            start = Offset(cx - w * 0.18f * zoomLevel, 0f),
            end = Offset(cx - w * 0.12f * zoomLevel, h),
            strokeWidth = 2.5f * zoomLevel
        )
    }
}

/**
 * Embedded Interactive Google Maps WebView for live street tiles & satellite preview.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun GoogleMapsEmbeddedWebView(
    queryOrCoords: String,
    modifier: Modifier = Modifier
) {
    val encoded = remember(queryOrCoords) {
        android.net.Uri.encode(queryOrCoords)
    }
    val embedUrl = remember(encoded) {
        "https://maps.google.com/maps?q=$encoded&hl=en&z=15&output=embed"
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webViewClient = WebViewClient()
                loadUrl(embedUrl)
            }
        },
        update = { webView ->
            if (webView.url != embedUrl) {
                webView.loadUrl(embedUrl)
            }
        }
    )
}
