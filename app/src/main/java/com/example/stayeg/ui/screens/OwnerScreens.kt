package com.example.stayeg.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.stayeg.data.model.BedStatus
import com.example.stayeg.data.model.ComplaintStatus
import com.example.stayeg.ui.components.AmenityChip
import com.example.stayeg.ui.theme.*
import com.example.stayeg.ui.viewmodel.StayEgViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerPGsScreen(viewModel: StayEgViewModel) {
    val allPgs by viewModel.allPgs.collectAsState()
    val ownerPgs = remember(allPgs) { allPgs.filter { !it.isDirectoryListing || it.isVerified } }

    var editingPgId by remember { mutableStateOf<String?>(null) }
    var editName by remember { mutableStateOf("") }
    var editPrice by remember { mutableStateOf("") }
    var editAddress by remember { mutableStateOf("") }

    var addingRoomPgId by remember { mutableStateOf<String?>(null) }
    var newRoomCode by remember { mutableStateOf("401") }
    var newRoomFloor by remember { mutableStateOf("4") }
    var newRoomPrice by remember { mutableStateOf("10500") }
    var newRoomType by remember { mutableStateOf(com.example.stayeg.data.model.RoomType.DOUBLE) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Properties & Rooms (${ownerPgs.size})", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.goBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(ownerPgs, key = { it.id }) { pg ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(pg.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SuccessGreenLight)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("VERIFIED LIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                            }
                        }

                        Text(pg.address, fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Base Rent: ₹${pg.price}/mo", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BluePrimary)
                            Text("Rating: ${pg.rating}★ (${pg.totalReviews} reviews)", fontSize = 12.sp, color = GoldDark)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Room Configurations (${pg.rooms.size} rooms):", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))

                        pg.rooms.forEach { room ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Room ${room.roomCode} (${room.roomType.name} • Floor ${room.floor})", fontSize = 12.sp, color = TextPrimary)
                                val availableBeds = room.beds.count { it.status == BedStatus.AVAILABLE }
                                Text(
                                    text = "$availableBeds/${room.beds.size} beds vacant",
                                    fontSize = 11.sp,
                                    color = if (availableBeds > 0) SuccessGreen else TextMuted,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    editingPgId = pg.id
                                    editName = pg.name
                                    editPrice = pg.price.toString()
                                    editAddress = pg.address
                                },
                                modifier = Modifier.weight(1f).height(36.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Edit PG", fontSize = 12.sp)
                            }
                            Button(
                                onClick = {
                                    addingRoomPgId = pg.id
                                    newRoomCode = "${(pg.rooms.size + 1) * 100 + 1}"
                                    newRoomFloor = "${pg.rooms.size + 1}"
                                    newRoomPrice = pg.price.toString()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                modifier = Modifier.weight(1f).height(36.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("+ Add Room", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (editingPgId != null) {
        AlertDialog(
            onDismissRequest = { editingPgId = null },
            title = { Text("Edit Property Details", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Property Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPrice,
                        onValueChange = { editPrice = it.filter { c -> c.isDigit() } },
                        label = { Text("Base Monthly Rent (₹)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editAddress,
                        onValueChange = { editAddress = it },
                        label = { Text("Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val priceVal = editPrice.toIntOrNull() ?: 10000
                        viewModel.updatePgDetails(editingPgId!!, editName, priceVal, editAddress)
                        editingPgId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text("Save & Sync")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingPgId = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (addingRoomPgId != null) {
        AlertDialog(
            onDismissRequest = { addingRoomPgId = null },
            title = { Text("Add New Room & Beds", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newRoomCode,
                        onValueChange = { newRoomCode = it },
                        label = { Text("Room Code (e.g. 204)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newRoomFloor,
                        onValueChange = { newRoomFloor = it.filter { c -> c.isDigit() } },
                        label = { Text("Floor Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newRoomPrice,
                        onValueChange = { newRoomPrice = it.filter { c -> c.isDigit() } },
                        label = { Text("Rent Per Bed (₹)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Sharing Type:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        com.example.stayeg.data.model.RoomType.entries.forEach { rt ->
                            val isSel = newRoomType == rt
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) BluePrimary else BlueLight)
                                    .clickable { newRoomType = rt }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(rt.name, fontSize = 10.sp, color = if (isSel) Color.White else BlueDeep, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addRoomToPg(
                            pgId = addingRoomPgId!!,
                            roomCode = newRoomCode.ifBlank { "302" },
                            roomType = newRoomType,
                            floor = newRoomFloor.toIntOrNull() ?: 1,
                            bedPrice = newRoomPrice.toIntOrNull() ?: 10000
                        )
                        addingRoomPgId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text("Add Room")
                }
            },
            dismissButton = {
                TextButton(onClick = { addingRoomPgId = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerRentScreen(viewModel: StayEgViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rent Collection Roster", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.goBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BluePrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Current Month Rent Overview", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("₹1,86,400 Collected / ₹2,10,400 Total", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("89% collection rate this cycle", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
                    }
                }
            }

            item {
                Text("Tenant Rent Records", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
            }

            val tenants = listOf(
                Triple("Vikram Singh", "Room A101 · Bed 1", Pair(12000, "PAID")),
                Triple("Rohan Mehta", "Room A101 · Bed 2", Pair(12000, "PAID")),
                Triple("Amit Patel", "Room A102 · Single", Pair(16000, "PENDING")),
                Triple("Siddharth Jain", "Room A201 · Bed 1", Pair(9500, "PAID")),
                Triple("Kunal Verma", "Room A201 · Bed 3", Pair(9500, "PENDING"))
            )

            items(tenants) { (name, room, statusPair) ->
                val (amount, status) = statusPair
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                            Text(room, fontSize = 12.sp, color = TextSecondary)
                            Text("Rent: ₹$amount/mo", fontSize = 11.sp, color = BluePrimary)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (status == "PAID") SuccessGreenLight else WarningAmberLight)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = status,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (status == "PAID") SuccessGreen else WarningAmber
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (status == "PENDING") {
                                Button(
                                    onClick = { viewModel.showToast("WhatsApp reminder sent to $name") },
                                    colors = ButtonDefaults.buttonColors(containerColor = BlueLight, contentColor = BlueDeep),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reminder", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerComplaintsScreen(viewModel: StayEgViewModel) {
    val complaints by viewModel.complaints.collectAsState()
    val storageBucketObjects by viewModel.storageBucketObjects.collectAsState()
    var resolveDialogTarget by remember { mutableStateOf<String?>(null) }
    var resolutionNotes by remember { mutableStateOf("") }
    var selectedBucketFilter by remember { mutableStateOf("ALL") }

    LaunchedEffect(Unit) {
        viewModel.refreshComplaintsFromSupabase()
        viewModel.refreshStorageBucketsFromSupabase()
    }

    val filteredStorageObjects = remember(storageBucketObjects, selectedBucketFilter) {
        when (selectedBucketFilter) {
            "KYC" -> storageBucketObjects.filter { it.bucketName == "kyc-documents" }
            "COMPLAINTS" -> storageBucketObjects.filter { it.bucketName == "complaint-photos" }
            else -> storageBucketObjects
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tenant Complaints & Cloud Media", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.goBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.refreshComplaintsFromSupabase()
                        viewModel.refreshStorageBucketsFromSupabase()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Buckets", tint = TealPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Admin & Property Owner Remote Supabase Storage Bucket Inspector (kyc-documents & complaint-photos)
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WhisperBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Resident KYC & Maintenance Media Vault",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Verified resident ID documents & maintenance ticket attachments",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = SoftSagePill
                            ) {
                                Text(
                                    text = "${storageBucketObjects.size} Files",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = TealDark,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "ALL" to "All Media",
                                "KYC" to "Resident KYC",
                                "COMPLAINTS" to "Ticket Photos"
                            ).forEach { (key, label) ->
                                val isSel = selectedBucketFilter == key
                                Surface(
                                    onClick = { selectedBucketFilter = key },
                                    shape = RoundedCornerShape(999.dp),
                                    color = if (isSel) TealDark else SoftCardSurface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) TealDark else WhisperBorder)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) PureWhite else TextSecondary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        if (filteredStorageObjects.isEmpty()) {
                            Text(
                                text = "No uploaded media in this category yet. Resident KYC ID uploads and ticket photo attachments appear here automatically.",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        } else {
                            filteredStorageObjects.take(6).forEach { obj ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = SoftCardSurface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, WhisperBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = obj.localUri.ifBlank { obj.remotePublicUrl },
                                            contentDescription = obj.slotOrTag,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(SoftSagePill)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "${obj.userName} • ${obj.slotOrTag}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = TextPrimary
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = SoftSagePill
                                                ) {
                                                    Text(
                                                        text = if (obj.bucketName == "kyc-documents") "KYC Verified" else "Maintenance",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = TealDark,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "Encrypted Cloud Media • Verified Access",
                                                fontSize = 9.sp,
                                                color = TealPrimary,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            items(complaints) { c ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(c.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (c.status == ComplaintStatus.RESOLVED) SuccessGreenLight else WarningAmberLight)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    c.status.name,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (c.status == ComplaintStatus.RESOLVED) SuccessGreen else WarningAmber
                                )
                            }
                        }

                        if (c.description.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(c.description, fontSize = 12.sp, color = TextSecondary)
                        }

                        if (c.photoAttachmentUri.isNotBlank() || c.remoteStorageUrl.isNotBlank()) {
                            val displayUrl = c.photoAttachmentUri.ifBlank { c.remoteStorageUrl }
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SoftCardSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, WhisperBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = displayUrl,
                                        contentDescription = "Tenant Complaint Photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Resident Photo Attachment",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = TealDark
                                        )
                                        Text(
                                            text = "Available to Assigned Maintenance Technician",
                                            fontSize = 9.sp,
                                            color = TealPrimary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Room ${c.roomCode} · ${c.category.name} · ${c.createdAt}", fontSize = 11.sp, color = TextMuted)

                            if (c.status != ComplaintStatus.RESOLVED) {
                                Button(
                                    onClick = { resolveDialogTarget = c.id },
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Mark Resolved", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (resolveDialogTarget != null) {
        AlertDialog(
            onDismissRequest = { resolveDialogTarget = null },
            title = { Text("Resolve Issue", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Add resolution notes for the tenant:", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = resolutionNotes,
                        onValueChange = { resolutionNotes = it },
                        placeholder = { Text("e.g. Technician replaced adapter. Fixed.") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resolveComplaint(resolveDialogTarget!!, resolutionNotes.ifEmpty { "Fixed by caretaker" })
                        resolveDialogTarget = null
                        resolutionNotes = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { resolveDialogTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
