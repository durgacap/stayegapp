package com.example.stayeg.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.stayeg.data.model.Complaint
import com.example.stayeg.data.model.ComplaintCategory
import com.example.stayeg.data.model.ComplaintPriority
import com.example.stayeg.data.model.ComplaintStatus
import com.example.stayeg.ui.theme.*
import com.example.stayeg.ui.viewmodel.StayEgViewModel

private data class MaintenanceSubTypeOption(
    val label: String,
    val icon: ImageVector,
    val category: ComplaintCategory,
    val defaultTitleHint: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintsScreen(viewModel: StayEgViewModel) {
    val complaints by viewModel.complaints.collectAsState()
    val activeStay by viewModel.activeStayConfig.collectAsState()

    // Automatically sync tickets from Supabase table on screen launch
    LaunchedEffect(Unit) {
        viewModel.refreshComplaintsFromSupabase()
    }

    val subTypeOptions = remember {
        listOf(
            MaintenanceSubTypeOption(
                label = "Wi-Fi & Router",
                icon = Icons.Default.Wifi,
                category = ComplaintCategory.MAINTENANCE,
                defaultTitleHint = "Wi-Fi speed drop or router signal issue"
            ),
            MaintenanceSubTypeOption(
                label = "Plumbing & Geyser",
                icon = Icons.Default.Plumbing,
                category = ComplaintCategory.MAINTENANCE,
                defaultTitleHint = "Bathroom geyser not heating / tap leakage"
            ),
            MaintenanceSubTypeOption(
                label = "AC & Electrical",
                icon = Icons.Default.ElectricalServices,
                category = ComplaintCategory.MAINTENANCE,
                defaultTitleHint = "AC cooling issue or power socket repair"
            ),
            MaintenanceSubTypeOption(
                label = "Room Cleaning",
                icon = Icons.Default.CleaningServices,
                category = ComplaintCategory.CLEANLINESS,
                defaultTitleHint = "Deep room & washroom housekeeping required"
            ),
            MaintenanceSubTypeOption(
                label = "Laundry & Appliances",
                icon = Icons.Default.LocalLaundryService,
                category = ComplaintCategory.MAINTENANCE,
                defaultTitleHint = "Washing machine or water dispenser check"
            ),
            MaintenanceSubTypeOption(
                label = "Mess & Water",
                icon = Icons.Default.Restaurant,
                category = ComplaintCategory.GENERAL,
                defaultTitleHint = "Drinking water refill or dining hall request"
            ),
            MaintenanceSubTypeOption(
                label = "Security & Locks",
                icon = Icons.Default.Lock,
                category = ComplaintCategory.SAFETY,
                defaultTitleHint = "Room door lock or cupboard key assistance"
            )
        )
    }

    val visitSlots = remember {
        listOf(
            "Morning (9 AM - 12 PM)",
            "Afternoon (12 PM - 4 PM)",
            "Evening (4 PM - 8 PM)",
            "Immediate / Urgent"
        )
    }

    // Inline form state
    var showFormExpanded by remember { mutableStateOf(true) }
    var selectedSubType by remember { mutableStateOf(subTypeOptions.first()) }
    var titleInput by remember { mutableStateOf("") }
    var descriptionInput by remember { mutableStateOf("") }
    var roomCodeInput by remember(activeStay.roomCode) { mutableStateOf(activeStay.roomCode) }
    var selectedPriority by remember { mutableStateOf(ComplaintPriority.MEDIUM) }
    var selectedVisitSlot by remember { mutableStateOf(visitSlots.first()) }
    var attachedPhotoUri by remember { mutableStateOf("") }

    // Modal dialog state (for FAB / TopBar '+' button)
    var showNewComplaintDialog by remember { mutableStateOf(false) }

    // Filter tabs & search query
    var selectedTab by remember { mutableIntStateOf(0) }
    var ticketSearchQuery by remember { mutableStateOf("") }

    // Photo Picker for attaching maintenance issue photo
    val issuePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            attachedPhotoUri = uri.toString()
        }
    }

    val openCount = complaints.count { it.status == ComplaintStatus.OPEN }
    val inProgressCount = complaints.count { it.status == ComplaintStatus.IN_PROGRESS }
    val resolvedCount = complaints.count { it.status == ComplaintStatus.RESOLVED || it.status == ComplaintStatus.CLOSED }

    val filteredComplaints = remember(complaints, selectedTab, ticketSearchQuery) {
        val byTab = when (selectedTab) {
            1 -> complaints.filter { it.status == ComplaintStatus.OPEN || it.status == ComplaintStatus.IN_PROGRESS }
            2 -> complaints.filter { it.status == ComplaintStatus.RESOLVED || it.status == ComplaintStatus.CLOSED }
            else -> complaints
        }
        val q = ticketSearchQuery.trim()
        if (q.isEmpty()) {
            byTab
        } else {
            byTab.filter {
                it.title.contains(q, ignoreCase = true) ||
                        it.description.contains(q, ignoreCase = true) ||
                        it.issueSubType.contains(q, ignoreCase = true) ||
                        it.roomCode.contains(q, ignoreCase = true) ||
                        it.assignedTo.contains(q, ignoreCase = true) ||
                        it.id.contains(q, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Maintenance & Support",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "${activeStay.pgName} • Room ${activeStay.roomCode}",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.goBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TealDark
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshComplaintsFromSupabase() },
                        modifier = Modifier.testTag("sync_complaints_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Refresh Ticket Status",
                            tint = TealPrimary
                        )
                    }
                    IconButton(
                        onClick = { showNewComplaintDialog = true },
                        modifier = Modifier.testTag("raise_complaint_top_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "New Complaint",
                            tint = TealDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PureWhite)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showNewComplaintDialog = true },
                containerColor = TealDark,
                contentColor = PureWhite,
                icon = { Icon(Icons.Default.Build, contentDescription = null, tint = MintSoft) },
                text = {
                    Text(
                        text = "Raise Issue",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                modifier = Modifier.testTag("fab_raise_complaint")
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftGalleryBg)
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Hero Maintenance & Live Ticket Metrics Card
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = TealDark,
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
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "24/7 On-Site Helpdesk",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 19.sp,
                                        color = PureWhite
                                    )
                                    Text(
                                        text = "Room ${activeStay.roomCode} • Bed #${activeStay.bedNumber} • Real-time ticket tracking",
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 11.sp,
                                        color = MintSoft.copy(alpha = 0.9f)
                                    )
                                }

                                Surface(
                                    onClick = { showFormExpanded = !showFormExpanded },
                                    shape = RoundedCornerShape(999.dp),
                                    color = SoftGoldPill
                                ) {
                                    Text(
                                        text = if (showFormExpanded) "Hide Form" else "+ Report Issue",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = GoldDark,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            // 4 Live Status Counters
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = TealDeep.copy(alpha = 0.55f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp, horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TicketCountStat("${complaints.size}", "Total")
                                    TicketStatDivider()
                                    TicketCountStat("$openCount", "Open")
                                    TicketStatDivider()
                                    TicketCountStat("$inProgressCount", "In Progress")
                                    TicketStatDivider()
                                    TicketCountStat("$resolvedCount", "Resolved")
                                }
                            }
                        }
                    }
                }
            }

            // 2. Inline Maintenance Submission Form
            if (showFormExpanded) {
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = PureWhite,
                        border = BorderStroke(1.dp, WhisperBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("complaint_submission_form")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Report a Maintenance Issue",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Select issue category and preferred technician visit window",
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Engineering,
                                    contentDescription = null,
                                    tint = TealPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // Maintenance Sub-Type Chips
                            Text(
                                text = "Issue Category",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(subTypeOptions, key = { it.label }) { option ->
                                    val isSelected = selectedSubType.label == option.label
                                    Surface(
                                        onClick = { selectedSubType = option },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) TealDark else SoftSagePill,
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) TealDark else WhisperBorder
                                        )
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Icon(
                                                imageVector = option.icon,
                                                contentDescription = option.label,
                                                tint = if (isSelected) MintSoft else TealDark,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = option.label,
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = if (isSelected) PureWhite else TealDark
                                            )
                                        }
                                    }
                                }
                            }

                            // Issue Title & Room Number Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = titleInput,
                                    onValueChange = { titleInput = it },
                                    label = { Text("Issue Title *") },
                                    placeholder = { Text(selectedSubType.defaultTitleHint) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("complaint_title_input")
                                )

                                OutlinedTextField(
                                    value = roomCodeInput,
                                    onValueChange = { roomCodeInput = it },
                                    label = { Text("Room") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .width(92.dp)
                                        .testTag("complaint_room_input")
                                )
                            }

                            // Issue Description
                            OutlinedTextField(
                                value = descriptionInput,
                                onValueChange = { descriptionInput = it },
                                label = { Text("Problem Details & Location in Room") },
                                placeholder = {
                                    Text("Describe what needs fixing in Room $roomCodeInput (e.g., router blinking red, geyser switch)...")
                                },
                                minLines = 2,
                                maxLines = 4,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("complaint_desc_input")
                            )

                            // Priority Selector
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Urgency Level",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = TextPrimary
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ComplaintPriority.entries.forEach { priority ->
                                        val isSel = selectedPriority == priority
                                        val (activeBg, activeFg) = when (priority) {
                                            ComplaintPriority.URGENT -> Pair(ErrorRed, PureWhite)
                                            ComplaintPriority.HIGH -> Pair(GoldDark, PureWhite)
                                            ComplaintPriority.MEDIUM -> Pair(TealPrimary, PureWhite)
                                            ComplaintPriority.LOW -> Pair(TealDark, PureWhite)
                                        }
                                        Surface(
                                            onClick = { selectedPriority = priority },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSel) activeBg else SoftCardSurface,
                                            border = BorderStroke(
                                                1.dp,
                                                if (isSel) activeBg else WhisperBorder
                                            ),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = priority.name,
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                color = if (isSel) activeFg else TextSecondary,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(vertical = 8.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Preferred Technician Visit Window
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Preferred Technician Visit Slot",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = TextPrimary
                                )
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(visitSlots) { slot ->
                                        val isSel = selectedVisitSlot == slot
                                        Surface(
                                            onClick = { selectedVisitSlot = slot },
                                            shape = RoundedCornerShape(999.dp),
                                            color = if (isSel) SoftGoldPill else SoftCardSurface,
                                            border = BorderStroke(
                                                1.dp,
                                                if (isSel) SoftGoldAccent else WhisperBorder
                                            )
                                        ) {
                                            Text(
                                                text = slot,
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 11.sp,
                                                color = if (isSel) GoldDark else TextSecondary,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Photo Attachment + Submit Ticket Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        issuePhotoPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, TealPrimary),
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("attach_complaint_photo_btn")
                                ) {
                                    Icon(
                                        imageVector = if (attachedPhotoUri.isNotBlank()) Icons.Default.CheckCircle else Icons.Default.AddAPhoto,
                                        contentDescription = "Attach Issue Photo",
                                        tint = TealPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (attachedPhotoUri.isNotBlank()) "Photo Added" else "Add Photo",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = TealPrimary
                                    )
                                }

                                Button(
                                    onClick = {
                                        val finalTitle = titleInput.trim().ifEmpty {
                                            selectedSubType.defaultTitleHint
                                        }
                                        val finalDesc = descriptionInput.trim().ifEmpty {
                                            "${selectedSubType.label} issue reported for Room ${roomCodeInput.ifBlank { activeStay.roomCode }} • Preferred slot: $selectedVisitSlot"
                                        }
                                        viewModel.raiseComplaint(
                                            pgId = activeStay.pgId,
                                            title = finalTitle,
                                            description = finalDesc,
                                            category = selectedSubType.category,
                                            priority = selectedPriority,
                                            pgName = activeStay.pgName,
                                            roomCode = roomCodeInput.ifBlank { activeStay.roomCode },
                                            bedNumber = activeStay.bedNumber,
                                            issueSubType = selectedSubType.label,
                                            preferredVisitSlot = selectedVisitSlot,
                                            photoAttachmentUri = attachedPhotoUri
                                        )
                                        titleInput = ""
                                        descriptionInput = ""
                                        attachedPhotoUri = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("submit_complaint_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Submit Ticket",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Filter Tabs & Search Bar for Real-Time Tickets
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SoftSagePill)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val tabs = listOf(
                            0 to "All Tickets (${complaints.size})",
                            1 to "Active (${openCount + inProgressCount})",
                            2 to "Resolved ($resolvedCount)"
                        )
                        tabs.forEach { (idx, label) ->
                            val isSelected = selectedTab == idx
                            Surface(
                                onClick = { selectedTab = idx },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) TealDark else Color.Transparent,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = label,
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp,
                                    color = if (isSelected) PureWhite else TealDark,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 9.dp, horizontal = 4.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = ticketSearchQuery,
                        onValueChange = { ticketSearchQuery = it },
                        placeholder = {
                            Text(
                                "Search your tickets by issue, room, or technician...",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = TealPrimary
                            )
                        },
                        trailingIcon = {
                            if (ticketSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { ticketSearchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = PureWhite,
                            unfocusedContainerColor = PureWhite,
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = WhisperBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_complaints_input")
                    )
                }
            }

            // 4. Ticket List or Empty State
            if (filteredComplaints.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = PureWhite,
                        border = BorderStroke(1.dp, WhisperBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = TealPrimary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No Tickets Found",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Use the form above or tap 'Raise Issue' to report a maintenance request for your room.",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredComplaints, key = { it.id }) { complaint ->
                    RealTimeComplaintTicketCard(
                        complaint = complaint,
                        onMarkInProgress = {
                            viewModel.updateComplaintStatus(
                                complaintId = complaint.id,
                                status = ComplaintStatus.IN_PROGRESS
                            )
                        },
                        onResolve = { resolutionNote ->
                            viewModel.resolveComplaint(
                                complaintId = complaint.id,
                                resolution = resolutionNote
                            )
                        },
                        onReopen = {
                            viewModel.updateComplaintStatus(
                                complaintId = complaint.id,
                                status = ComplaintStatus.OPEN
                            )
                        },
                        onAddFollowUp = { note ->
                            viewModel.addComplaintFollowUpNote(complaint.id, note)
                        }
                    )
                }
            }
        }
    }

    // Modal Dialog for quick ticket creation from FAB or TopBar '+'
    if (showNewComplaintDialog) {
        var dialogTitle by remember { mutableStateOf("") }
        var dialogDesc by remember { mutableStateOf("") }
        var dialogSubType by remember { mutableStateOf(subTypeOptions.first()) }
        var dialogPriority by remember { mutableStateOf(ComplaintPriority.MEDIUM) }
        var dialogSlot by remember { mutableStateOf(visitSlots.first()) }

        AlertDialog(
            onDismissRequest = { showNewComplaintDialog = false },
            title = {
                Text(
                    text = "Raise Maintenance Ticket",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Room ${activeStay.roomCode} • Bed #${activeStay.bedNumber} • ${activeStay.pgName}",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp,
                        color = TealPrimary,
                        fontWeight = FontWeight.SemiBold
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(subTypeOptions) { opt ->
                            val isSel = dialogSubType.label == opt.label
                            Surface(
                                onClick = { dialogSubType = opt },
                                shape = RoundedCornerShape(999.dp),
                                color = if (isSel) TealDark else SoftSagePill
                            ) {
                                Text(
                                    text = opt.label,
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) PureWhite else TealDark,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = dialogTitle,
                        onValueChange = { dialogTitle = it },
                        label = { Text("Issue Title *") },
                        placeholder = { Text(dialogSubType.defaultTitleHint) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = dialogDesc,
                        onValueChange = { dialogDesc = it },
                        label = { Text("Description & Room Details") },
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Priority:",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ComplaintPriority.entries.forEach { pri ->
                            val isSel = dialogPriority == pri
                            Surface(
                                onClick = { dialogPriority = pri },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) TealPrimary else SoftCardSurface
                            ) {
                                Text(
                                    text = pri.name,
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) PureWhite else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalTitle = dialogTitle.trim().ifEmpty { dialogSubType.defaultTitleHint }
                        val finalDesc = dialogDesc.trim().ifEmpty {
                            "${dialogSubType.label} issue reported for Room ${activeStay.roomCode}"
                        }
                        viewModel.raiseComplaint(
                            pgId = activeStay.pgId,
                            title = finalTitle,
                            description = finalDesc,
                            category = dialogSubType.category,
                            priority = dialogPriority,
                            pgName = activeStay.pgName,
                            roomCode = activeStay.roomCode,
                            bedNumber = activeStay.bedNumber,
                            issueSubType = dialogSubType.label,
                            preferredVisitSlot = dialogSlot
                        )
                        showNewComplaintDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text(
                        text = "Submit Ticket",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewComplaintDialog = false }) {
                    Text("Cancel", fontFamily = PoppinsFontFamily, color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun RealTimeComplaintTicketCard(
    complaint: Complaint,
    onMarkInProgress: () -> Unit,
    onResolve: (String) -> Unit,
    onReopen: () -> Unit,
    onAddFollowUp: (String) -> Unit
) {
    var showFollowUpInput by remember { mutableStateOf(false) }
    var followUpText by remember { mutableStateOf("") }
    var showTimeline by remember { mutableStateOf(false) }

    val isResolved = complaint.status == ComplaintStatus.RESOLVED || complaint.status == ComplaintStatus.CLOSED

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(
            1.dp,
            if (isResolved) TealPrimary.copy(alpha = 0.35f) else WhisperBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("complaint_card_${complaint.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Status, Priority & Timestamp Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(complaint.status)
                    PriorityBadge(complaint.priority)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SoftCardSurface
                    ) {
                        Text(
                            text = "#${complaint.id.takeLast(4).uppercase()}",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = complaint.createdAt,
                    fontFamily = PoppinsFontFamily,
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }

            // Sub-Type & Room Pill Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = SoftSagePill
                ) {
                    Text(
                        text = complaint.issueSubType.ifBlank { complaint.category.name },
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = TealDark,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = "Room ${complaint.roomCode} • Bed #${complaint.bedNumber}",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            // Title & Description
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = complaint.title,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                if (complaint.description.isNotBlank()) {
                    Text(
                        text = complaint.description,
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 17.sp
                    )
                }
            }

            // Optional Attached Photo Preview (Synced to Supabase Storage complaint-photos bucket)
            if (complaint.photoAttachmentUri.isNotBlank() || complaint.remoteStorageUrl.isNotBlank()) {
                val displayPhotoUrl = complaint.photoAttachmentUri.ifBlank { complaint.remoteStorageUrl }
                val remoteBucketUrl = complaint.remoteStorageUrl.ifBlank { complaint.photoAttachmentUri }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SoftCardSurface,
                    border = BorderStroke(1.dp, WhisperBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = displayPhotoUrl,
                            contentDescription = "Issue Attachment",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Attached Issue Photo • Shared with Maintenance Team",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = TealDark
                                )
                            }
                            Text(
                                text = "Accessible to Property Manager & Assigned Technician",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // 4-Step Real-Time Ticket Progress Stepper
            TicketProgressStepper(activeStepIndex = complaint.progressStepIndex)

            // Assigned Technician & Visit Window Info Bar
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SoftCardSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Engineering,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = complaint.assignedTo.ifBlank { "On-Site Maintenance Team" },
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Slot: ${complaint.preferredVisitSlot}",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Text(
                        text = if (showTimeline) "Hide Log ▲" else "Activity Log ▼",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = TealPrimary,
                        modifier = Modifier.clickable { showTimeline = !showTimeline }
                    )
                }
            }

            // Expandable Activity Timeline
            if (showTimeline && complaint.statusTimeline.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SoftSagePill.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        complaint.statusTimeline.forEach { entry ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(TealPrimary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = entry,
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 11.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Resolution Banner when Resolved
            if (complaint.resolution.isNotBlank()) {
                Surface(
                    color = SoftSagePill,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TaskAlt,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Resolution: ${complaint.resolution}",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 11.sp,
                            color = TealDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Follow-Up Note Input Box (when toggled)
            if (showFollowUpInput) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = followUpText,
                        onValueChange = { followUpText = it },
                        placeholder = { Text("Add note for technician...", fontSize = 11.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            if (followUpText.isNotBlank()) {
                                onAddFollowUp(followUpText)
                                followUpText = ""
                                showFollowUpInput = false
                                showTimeline = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Post", fontFamily = PoppinsFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { showFollowUpInput = !showFollowUpInput },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, WhisperBorder),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddComment,
                        contentDescription = null,
                        tint = TealDark,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Add Note",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = TealDark
                    )
                }

                if (complaint.status == ComplaintStatus.OPEN) {
                    OutlinedButton(
                        onClick = onMarkInProgress,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, SoftGoldAccent),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = "Start Work",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = GoldDark
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (!isResolved) {
                    Button(
                        onClick = { onResolve("Confirmed resolved by tenant") },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("resolve_complaint_btn_${complaint.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Mark Resolved",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                } else {
                    TextButton(
                        onClick = onReopen,
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = "Reopen Ticket",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketProgressStepper(activeStepIndex: Int) {
    val steps = listOf("Logged", "Assigned", "In Progress", "Resolved")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        steps.forEachIndexed { idx, label ->
            val isCompleted = idx <= activeStepIndex
            val isCurrent = idx == activeStepIndex
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = if (idx < steps.lastIndex) Modifier.weight(1f) else Modifier
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCompleted -> TealPrimary
                                    else -> WhisperBorder
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = label,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 9.sp,
                        color = if (isCompleted) TealDark else TextMuted
                    )
                }

                if (idx < steps.lastIndex) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .height(2.dp)
                            .background(
                                if (idx < activeStepIndex) TealPrimary else WhisperBorder
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: ComplaintStatus) {
    val (bg, fg, label) = when (status) {
        ComplaintStatus.OPEN -> Triple(ErrorRedLight, ErrorRed, "OPEN")
        ComplaintStatus.IN_PROGRESS -> Triple(SoftGoldPill, GoldDark, "IN PROGRESS")
        ComplaintStatus.RESOLVED -> Triple(SoftSagePill, TealDark, "RESOLVED")
        ComplaintStatus.CLOSED -> Triple(SoftCardSurface, TextSecondary, "CLOSED")
    }
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bg
    ) {
        Text(
            text = label,
            fontFamily = PoppinsFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun PriorityBadge(priority: ComplaintPriority) {
    val (bg, fg) = when (priority) {
        ComplaintPriority.URGENT -> Pair(ErrorRedLight, ErrorRed)
        ComplaintPriority.HIGH -> Pair(SoftGoldPill, GoldDark)
        ComplaintPriority.MEDIUM -> Pair(SoftCardSurface, TealDark)
        ComplaintPriority.LOW -> Pair(SoftCardSurface, TextSecondary)
    }
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bg
    ) {
        Text(
            text = priority.name,
            fontFamily = PoppinsFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = fg,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun TicketCountStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            color = PureWhite
        )
        Text(
            text = label,
            fontFamily = PoppinsFontFamily,
            fontSize = 10.sp,
            color = MintSoft.copy(alpha = 0.85f)
        )
    }
}

@Composable
private fun TicketStatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(24.dp)
            .background(PureWhite.copy(alpha = 0.18f))
    )
}
