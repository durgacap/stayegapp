package com.example.stayeg.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stayeg.data.model.CommunityPost
import com.example.stayeg.ui.components.StayEgPopupInfoDialog
import com.example.stayeg.ui.navigation.Screen
import com.example.stayeg.ui.theme.*
import com.example.stayeg.ui.viewmodel.StayEgViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(viewModel: StayEgViewModel) {
    val posts by viewModel.communityPosts.collectAsState()
    var selectedTab by remember { mutableStateOf("All") }
    var showCreatePostDialog by remember { mutableStateOf(false) }
    var showUpcomingClubsPopup by remember { mutableStateOf(false) }
    var reportingPost by remember { mutableStateOf<CommunityPost?>(null) }
    var expandedPostId by remember { mutableStateOf<String?>(null) }

    val segmentedTabs = listOf(
        "All" to "All Experiences",
        "Reviews" to "PG Reviews",
        "Food" to "Food & Area"
    )

    val filteredPosts = remember(posts, selectedTab) {
        when (selectedTab) {
            "Reviews" -> posts.filter { it.category.contains("Review", ignoreCase = true) || it.category.contains("Experience", ignoreCase = true) }
            "Food" -> posts.filter { it.category.contains("Food", ignoreCase = true) || it.category.contains("Area", ignoreCase = true) || it.category.contains("Commute", ignoreCase = true) }
            else -> posts
        }
    }

    Scaffold(
        containerColor = PureWhite,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreatePostDialog = true },
                containerColor = TealDark,
                contentColor = PureWhite,
                icon = {
                    Icon(
                        imageVector = Icons.Default.RateReview,
                        contentDescription = null,
                        tint = MintSoft
                    )
                },
                text = {
                    Text(
                        text = "Share Experience",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                },
                modifier = Modifier.testTag("create_post_btn")
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(PureWhite)
                .padding(padding),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Editorial Header (Exact match to Screen 7 "Messages" in WeDo Reference Image 2)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Community",
                            fontFamily = PlayfairFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            color = TextPrimary,
                            letterSpacing = (-0.4).sp
                        )
                        Text(
                            text = "Verified tenant experiences & Bengaluru stay tips",
                            fontFamily = JakartaFontFamily,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    // Community Clubs Pill
                    Surface(
                        onClick = { showUpcomingClubsPopup = true },
                        shape = RoundedCornerShape(999.dp),
                        color = SoftGoldPill,
                        border = BorderStroke(1.dp, SoftGoldAccent.copy(alpha = 0.4f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = "Community Clubs",
                                tint = GoldDark,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Clubs",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = GoldDark
                            )
                        }
                    }
                }
            }

            // 2. 3-Tab Segmented Underline Bar (Exact match to "All | Vendors | WeDo Team" in Screen 7 of Reference Image 2!)
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        segmentedTabs.forEach { (key, label) ->
                            val isSelected = selectedTab == key
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedTab = key }
                                    .padding(top = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontFamily = JakartaFontFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = if (isSelected) TealDark else TextSecondary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(if (isSelected) 2.5.dp else 1.dp)
                                        .background(if (isSelected) TealPrimary else WhisperBorder)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Screen 7 Style Tenant Experience Thread Rows (Expandable on tap)
            items(filteredPosts, key = { it.id }) { post ->
                CommunityThreadRowCard(
                    post = post,
                    isExpanded = expandedPostId == post.id,
                    onToggleExpand = {
                        expandedPostId = if (expandedPostId == post.id) null else post.id
                    },
                    onLikeClick = { viewModel.toggleLikePost(post.id) },
                    onReportClick = { reportingPost = post }
                )
                HorizontalDivider(color = BorderLight)
            }
        }
    }

    if (showUpcomingClubsPopup) {
        StayEgPopupInfoDialog(
            title = "StayEg Community Clubs",
            subtitle = "Localities, Tech Referrals & Weekend Meetups",
            bulletPoints = listOf(
                "Tenant Experiences: Share and read authentic PG stay reviews, meal ratings, and locality tips.",
                "Bengaluru Area Circles: Connect with residents across Koramangala, HSR Layout, Whitefield, and Electronic City.",
                "Suggest a Club: Let us know which community group or event you'd like to join!"
            ),
            primaryButtonText = "Suggest a Club",
            secondaryButtonText = "Close",
            onDismiss = { showUpcomingClubsPopup = false },
            onPrimaryClick = {
                showUpcomingClubsPopup = false
                viewModel.navigateTo(Screen.FeedbackHub)
            },
            onSecondaryClick = { showUpcomingClubsPopup = false }
        )
    }

    // Create Experience Modal
    if (showCreatePostDialog) {
        var pgName by remember { mutableStateOf("") }
        var areaName by remember { mutableStateOf("Koramangala") }
        var postTitle by remember { mutableStateOf("") }
        var postContent by remember { mutableStateOf("") }
        var postCategory by remember { mutableStateOf("PG Experience") }
        var rating by remember { mutableIntStateOf(5) }

        AlertDialog(
            onDismissRequest = { showCreatePostDialog = false },
            containerColor = PureWhite,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Share Your PG Experience",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = pgName,
                        onValueChange = { pgName = it },
                        label = { Text("PG Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = areaName,
                        onValueChange = { areaName = it },
                        label = { Text("Locality (e.g., HSR Layout)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = postTitle,
                        onValueChange = { postTitle = it },
                        label = { Text("Headline") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("post_title_input")
                    )

                    OutlinedTextField(
                        value = postContent,
                        onValueChange = { postContent = it },
                        label = { Text("Your Experience (Food, Wi-Fi, Cleanliness, Safety)") },
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("post_content_input")
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Rating:",
                            fontFamily = OutfitFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Row {
                            (1..5).forEach { s ->
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "$s Stars",
                                    tint = if (s <= rating) SoftGoldAccent else WhisperBorder,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable { rating = s }
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("PG Experience", "PG Review", "Food & Hygiene").forEach { c ->
                            val isSel = postCategory == c
                            Surface(
                                onClick = { postCategory = c },
                                shape = RoundedCornerShape(999.dp),
                                color = if (isSel) TealPrimary else SoftSagePill
                            ) {
                                Text(
                                    text = c,
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 10.sp,
                                    color = if (isSel) PureWhite else TealDark,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (postTitle.isBlank()) {
                            viewModel.showToast("Please enter a headline for your experience")
                            return@Button
                        }
                        viewModel.createPost(
                            title = postTitle,
                            content = postContent,
                            category = postCategory,
                            tags = listOf(areaName, postCategory, "${rating}★"),
                            pgName = pgName.ifBlank { "Bengaluru PG" },
                            areaName = areaName.ifBlank { "Bengaluru" },
                            experienceRating = rating
                        )
                        showCreatePostDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Publish",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePostDialog = false }) {
                    Text("Cancel", fontFamily = OutfitFontFamily, color = TextSecondary)
                }
            }
        )
    }

    // Report Post Modal
    reportingPost?.let { targetPost ->
        var selectedReason by remember { mutableStateOf("Inaccurate or misleading information") }
        val reasons = listOf(
            "Inaccurate or misleading information",
            "Spam or promotional content",
            "Abusive or inappropriate language"
        )

        AlertDialog(
            onDismissRequest = { reportingPost = null },
            containerColor = PureWhite,
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = "Report Content",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Why are you reporting '${targetPost.title}'?",
                        fontFamily = JakartaFontFamily,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    reasons.forEach { r ->
                        val isSel = selectedReason == r
                        Surface(
                            onClick = { selectedReason = r },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) SoftSagePill else SoftCardSurface,
                            border = BorderStroke(1.dp, if (isSel) TealPrimary else WhisperBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = r,
                                fontFamily = JakartaFontFamily,
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = TextPrimary,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.reportCommunityPost(targetPost.id, selectedReason)
                        reportingPost = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Submit Report", fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { reportingPost = null }) {
                    Text("Cancel", fontFamily = OutfitFontFamily, color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun CommunityThreadRowCard(
    post: CommunityPost,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onLikeClick: () -> Unit,
    onReportClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isExpanded) SoftCardSurface else PureWhite)
            .clickable { onToggleExpand() }
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        // Top Row: Circular Avatar + Title/PG Name + Timestamp & Rating Pill (Exact match to Screen 7 "Messages" in WeDo reference!)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(SoftSagePill),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.authorName.first().toString(),
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TealDark
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (post.pgName.isNotBlank()) post.pgName else post.authorName,
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = post.timeAgo,
                            fontFamily = JakartaFontFamily,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${post.authorName} • ${post.areaName}",
                        fontFamily = JakartaFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = TealPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${post.title} — ${post.content}",
                            fontFamily = JakartaFontFamily,
                            fontSize = 13.sp,
                            color = TextSecondary,
                            maxLines = if (isExpanded) 10 else 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 18.sp,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Rating / Helpful Badge (Matching the circular badge in Screen 7 of Reference Image 2)
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = SoftGoldPill
                        ) {
                            Text(
                                text = "${post.experienceRating}.0 ★",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = GoldDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action Bar: Tags + Helpful + Report
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 58.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                post.tags.take(2).forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = SoftSagePill
                    ) {
                        Text(
                            text = "#$tag",
                            fontFamily = JakartaFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            color = TealDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onLikeClick() }
                ) {
                    Icon(
                        imageVector = if (post.isLiked) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt,
                        contentDescription = "Helpful",
                        tint = if (post.isLiked) TealPrimary else TextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${post.likes} Helpful",
                        fontFamily = JakartaFontFamily,
                        fontSize = 11.sp,
                        fontWeight = if (post.isLiked) FontWeight.Bold else FontWeight.Medium,
                        color = if (post.isLiked) TealPrimary else TextSecondary
                    )
                }

                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = "Report",
                    tint = TextMuted,
                    modifier = Modifier
                        .size(15.dp)
                        .clickable { onReportClick() }
                )
            }
        }
    }
}
