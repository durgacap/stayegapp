package com.example.stayeg.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.stayeg.R
import com.example.stayeg.data.remote.SupabaseService
import com.example.stayeg.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Resolves a photo angle caption based on index and optional category.
 */
fun resolvePgPhotoAngleLabel(index: Int, category: String = "All Photos"): String {
    if (category != "All Photos" && category.isNotBlank()) return category
    return when (index % 5) {
        0 -> "Room View"
        1 -> "Building Exterior"
        2 -> "Furnished Bedroom"
        3 -> "Mess & Dining"
        else -> "Co-Work Lounge"
    }
}

/**
 * Resolves a deterministic high-quality bundled Bengaluru PG drawable fallback resource.
 */
fun resolvePgFallbackDrawable(seed: Int): Int {
    return when (kotlin.math.abs(seed) % 5) {
        0 -> R.drawable.img_pg_room_double_1795421747870
        1 -> R.drawable.img_pg_room_single_1795421748285
        2 -> R.drawable.img_pg_building_exterior_1795421746872
        3 -> R.drawable.img_pg_mess_dining_1795421746459
        else -> R.drawable.img_pg_lounge_washroom_1795421747598
    }
}

/**
 * Interactive Coil-powered HorizontalPager Image Gallery Slider for PG Listings.
 *
 * Features:
 * - Smooth swipe gestures across multiple high-quality PG photos via `HorizontalPager`
 * - Coil `AsyncImage` with crossfade transition & instant bundled Bengaluru PG drawable fallbacks
 * - Left/Right navigation arrows, live `1 / N` photo counter badge, and animated pagination dots
 * - Optional synchronized active index so external thumbnail strips stay in lockstep
 * - Optional fullscreen Lightbox zoom modal
 */
@Composable
fun StayEgImageGallerySlider(
    imageUrls: List<String>,
    pgName: String,
    modifier: Modifier = Modifier,
    sliderHeight: Dp = 255.dp,
    cornerRadius: Dp = 0.dp,
    categoryLabel: String = "All Photos",
    showArrows: Boolean = true,
    showPaginationDots: Boolean = true,
    showCounterBadge: Boolean = true,
    enableFullscreenViewer: Boolean = false,
    selectedIndex: Int = 0,
    onPageChanged: ((Int) -> Unit)? = null,
    testTagPrefix: String = "pg_gallery_slider"
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val safePhotos = remember(imageUrls) {
        imageUrls.filter { it.isNotBlank() }.distinct().ifEmpty {
            listOf(
                "${SupabaseService.APP_PACKAGE_URI}/img_pg_room_double_1795421747870",
                "${SupabaseService.APP_PACKAGE_URI}/img_pg_building_exterior_1795421746872",
                "${SupabaseService.APP_PACKAGE_URI}/img_pg_room_single_1795421748285",
                "${SupabaseService.APP_PACKAGE_URI}/img_pg_mess_dining_1795421746459",
                "${SupabaseService.APP_PACKAGE_URI}/img_pg_lounge_washroom_1795421747598"
            )
        }
    }

    val pagerState = rememberPagerState(
        initialPage = selectedIndex.coerceIn(0, (safePhotos.size - 1).coerceAtLeast(0)),
        pageCount = { safePhotos.size }
    )

    // Keep pager synchronized when parent selects a different thumbnail or category
    LaunchedEffect(selectedIndex, safePhotos) {
        val target = selectedIndex.coerceIn(0, (safePhotos.size - 1).coerceAtLeast(0))
        if (pagerState.currentPage != target) {
            pagerState.animateScrollToPage(target)
        }
    }

    // Notify parent when user swipes the pager
    LaunchedEffect(pagerState.currentPage) {
        onPageChanged?.invoke(pagerState.currentPage)
    }

    var showFullscreenDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(sliderHeight)
            .let {
                if (cornerRadius > 0.dp) it.clip(RoundedCornerShape(cornerRadius)) else it
            }
            .background(SoftCardSurface)
            .testTag(testTagPrefix)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .testTag("${testTagPrefix}_pager")
        ) { page ->
            val photoUrl = safePhotos[page]
            val fallbackRes = remember(pgName, page) {
                resolvePgFallbackDrawable(pgName.hashCode() + page)
            }

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(photoUrl)
                    .crossfade(220)
                    .build(),
                contentDescription = "$pgName photo ${page + 1} of ${safePhotos.size}",
                placeholder = painterResource(id = fallbackRes),
                error = painterResource(id = fallbackRes),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .let { mod ->
                        if (enableFullscreenViewer) {
                            mod.clickable { showFullscreenDialog = true }
                        } else {
                            mod
                        }
                    }
            )
        }

        // Subtle bottom gradient scrim for high-contrast indicators and overlaid badges
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (sliderHeight >= 220.dp) 95.dp else 56.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, PitchBlack.copy(alpha = 0.72f))
                    )
                )
        )

        // Left & Right Navigation Arrow Pills for 1-Tap Photo Cycling
        if (showArrows && safePhotos.size > 1) {
            // Previous Arrow
            Surface(
                onClick = {
                    scope.launch {
                        val prevPage = if (pagerState.currentPage > 0) {
                            pagerState.currentPage - 1
                        } else {
                            safePhotos.lastIndex
                        }
                        pagerState.animateScrollToPage(prevPage)
                    }
                },
                shape = CircleShape,
                color = PitchBlack.copy(alpha = 0.45f),
                border = BorderStroke(0.8.dp, PureWhite.copy(alpha = 0.35f)),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
                    .size(if (sliderHeight >= 220.dp) 34.dp else 26.dp)
                    .testTag("${testTagPrefix}_prev_btn")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Photo",
                        tint = PureWhite,
                        modifier = Modifier.size(if (sliderHeight >= 220.dp) 20.dp else 16.dp)
                    )
                }
            }

            // Next Arrow
            Surface(
                onClick = {
                    scope.launch {
                        val nextPage = (pagerState.currentPage + 1) % safePhotos.size
                        pagerState.animateScrollToPage(nextPage)
                    }
                },
                shape = CircleShape,
                color = PitchBlack.copy(alpha = 0.45f),
                border = BorderStroke(0.8.dp, PureWhite.copy(alpha = 0.35f)),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
                    .size(if (sliderHeight >= 220.dp) 34.dp else 26.dp)
                    .testTag("${testTagPrefix}_next_btn")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Photo",
                        tint = PureWhite,
                        modifier = Modifier.size(if (sliderHeight >= 220.dp) 20.dp else 16.dp)
                    )
                }
            }
        }

        // Animated Pagination Dots Indicator
        if (showPaginationDots && safePhotos.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (sliderHeight >= 220.dp) 18.dp else 32.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val maxDots = safePhotos.size.coerceAtMost(7)
                for (i in 0 until maxDots) {
                    val isCurrent = (pagerState.currentPage % maxDots) == i
                    val dotWidth by animateDpAsState(
                        targetValue = if (isCurrent) 16.dp else 6.dp,
                        animationSpec = tween(durationMillis = 200),
                        label = "dotWidth"
                    )
                    val dotColor by animateColorAsState(
                        targetValue = if (isCurrent) MintSoft else PureWhite.copy(alpha = 0.55f),
                        animationSpec = tween(durationMillis = 200),
                        label = "dotColor"
                    )
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(dotWidth)
                            .clip(CircleShape)
                            .background(dotColor)
                            .clickable {
                                scope.launch {
                                    pagerState.animateScrollToPage(i)
                                }
                            }
                    )
                }
            }
        }

        // Live Photo Counter & Angle Pill (e.g. "Room View • 1/5")
        if (showCounterBadge && safePhotos.isNotEmpty()) {
            val currentIdx = pagerState.currentPage.coerceIn(0, safePhotos.lastIndex)
            val angleText = resolvePgPhotoAngleLabel(currentIdx, categoryLabel)

            Surface(
                onClick = {
                    if (enableFullscreenViewer) {
                        showFullscreenDialog = true
                    } else if (safePhotos.size > 1) {
                        scope.launch {
                            pagerState.animateScrollToPage((currentIdx + 1) % safePhotos.size)
                        }
                    }
                },
                shape = RoundedCornerShape(999.dp),
                color = TealDeep.copy(alpha = 0.84f),
                border = BorderStroke(0.8.dp, PureWhite.copy(alpha = 0.25f)),
                modifier = Modifier
                    .align(if (sliderHeight >= 220.dp) Alignment.BottomEnd else Alignment.TopEnd)
                    .padding(
                        end = if (sliderHeight >= 220.dp) 16.dp else 40.dp,
                        bottom = if (sliderHeight >= 220.dp) 16.dp else 0.dp,
                        top = if (sliderHeight >= 220.dp) 0.dp else 8.dp
                    )
                    .testTag("${testTagPrefix}_counter_badge")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(
                        horizontal = if (sliderHeight >= 220.dp) 10.dp else 7.dp,
                        vertical = if (sliderHeight >= 220.dp) 5.dp else 3.dp
                    )
                ) {
                    Icon(
                        imageVector = if (enableFullscreenViewer) Icons.Default.Fullscreen else Icons.Default.PhotoLibrary,
                        contentDescription = "Photo Gallery Counter",
                        tint = MintSoft,
                        modifier = Modifier.size(if (sliderHeight >= 220.dp) 13.dp else 10.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$angleText • ${currentIdx + 1}/${safePhotos.size}",
                        fontFamily = JakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (sliderHeight >= 220.dp) 10.5.sp else 8.5.sp,
                        color = PureWhite
                    )
                }
            }
        }
    }

    // Optional Fullscreen Gallery Lightbox Modal
    if (showFullscreenDialog) {
        Dialog(
            onDismissRequest = { showFullscreenDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            val modalPagerState = rememberPagerState(
                initialPage = pagerState.currentPage,
                pageCount = { safePhotos.size }
            )

            LaunchedEffect(modalPagerState.currentPage) {
                onPageChanged?.invoke(modalPagerState.currentPage)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PitchBlack.copy(alpha = 0.96f))
            ) {
                HorizontalPager(
                    state = modalPagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val photoUrl = safePhotos[page]
                    val fallbackRes = resolvePgFallbackDrawable(pgName.hashCode() + page)
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(photoUrl)
                                .crossfade(200)
                                .build(),
                            contentDescription = "$pgName Fullscreen Photo ${page + 1}",
                            placeholder = painterResource(id = fallbackRes),
                            error = painterResource(id = fallbackRes),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(360.dp)
                                .padding(horizontal = 12.dp)
                                .clip(RoundedCornerShape(20.dp))
                        )
                    }
                }

                // Top Header Bar inside Lightbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = pgName,
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = PureWhite,
                            maxLines = 1
                        )
                        Text(
                            text = "${resolvePgPhotoAngleLabel(modalPagerState.currentPage, categoryLabel)} • Photo ${modalPagerState.currentPage + 1} of ${safePhotos.size}",
                            fontFamily = JakartaFontFamily,
                            fontSize = 12.sp,
                            color = MintSoft
                        )
                    }

                    Surface(
                        onClick = { showFullscreenDialog = false },
                        shape = CircleShape,
                        color = PureWhite.copy(alpha = 0.16f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Gallery",
                                tint = PureWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Bottom Interactive Thumbnail Strip inside Fullscreen Lightbox
                LazyRow(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(safePhotos) { idx, url ->
                        val isChosen = idx == modalPagerState.currentPage
                        val fallbackRes = resolvePgFallbackDrawable(pgName.hashCode() + idx)
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(url)
                                .crossfade(150)
                                .build(),
                            contentDescription = "Thumbnail ${idx + 1}",
                            placeholder = painterResource(id = fallbackRes),
                            error = painterResource(id = fallbackRes),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(width = 76.dp, height = 54.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    width = if (isChosen) 2.dp else 1.dp,
                                    color = if (isChosen) MintSoft else PureWhite.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    scope.launch {
                                        modalPagerState.animateScrollToPage(idx)
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}
