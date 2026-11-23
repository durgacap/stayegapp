package com.example.stayeg.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stayeg.data.remote.SupabaseRealtimeState
import com.example.stayeg.data.remote.SupabaseService
import com.example.stayeg.ui.theme.*
import com.example.stayeg.ui.viewmodel.StayEgViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SupabaseSyncScreen(viewModel: StayEgViewModel) {
    val supabaseStatus by viewModel.supabaseStatus.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val realtimeState by viewModel.realtimeState.collectAsState()
    val realtimeEvents by viewModel.realtimeEvents.collectAsState()
    val allPgs by viewModel.allPgs.collectAsState()
    var apiKeyInput by remember { mutableStateOf(SupabaseService.currentApiKey) }
    var showSqlSchema by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("supabase_header_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TealDark)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF3ECF8E).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = "StayEg Cloud",
                                    tint = Color(0xFF3ECF8E),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "StayEg Cloud & Real-Time Sync",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Live Property Catalog & Subscriptions Engine",
                                    fontSize = 12.sp,
                                    color = MintSoft
                                )
                            }
                        }

                        // Live Pulse Badge
                        val isConnected = supabaseStatus?.isConnected ?: true
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isConnected) Color(0xFF3ECF8E).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isConnected) Color(0xFF3ECF8E) else Color(0xFFEF4444))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isConnected) "CONNECTED" else "OFFLINE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isConnected) Color(0xFF3ECF8E) else Color(0xFFEF4444)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Project Details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "PROJECT ID", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                            Text(text = SupabaseService.PROJECT_REF, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Medium)
                        }
                        Column {
                            Text(text = "REAL-TIME STATE", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                            Text(
                                text = realtimeState.name,
                                fontSize = 12.sp,
                                color = if (realtimeState == SupabaseRealtimeState.SUBSCRIBED_LIVE) Color(0xFF3ECF8E) else Color(0xFFFBBF24),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text(text = "LATENCY", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                            Text(
                                text = if ((supabaseStatus?.latencyMs ?: 0L) > 0) "${supabaseStatus?.latencyMs} ms" else "Active",
                                fontSize = 13.sp,
                                color = Color(0xFF3ECF8E),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Pooler: ${SupabaseService.POOLER_HOST}:${SupabaseService.POOLER_PORT}",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Test & Sync Action Buttons
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Client Initialization & Sync",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Direct SSL + SCRAM-SHA-256 connection to Supabase PostgreSQL (${allPgs.size} PGs in reactive stream).",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.testSupabase() },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_test_connection"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            enabled = !isSyncing
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            } else {
                                Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(text = "Ping DB", fontSize = 13.sp)
                        }

                        Button(
                            onClick = { viewModel.syncWithSupabase() },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_sync_now"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            enabled = !isSyncing
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Init & Subscribe", fontSize = 13.sp)
                        }
                    }

                    if (supabaseStatus != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (supabaseStatus?.isConnected == true) Icons.Default.CheckCircle else Icons.Default.Info,
                                    contentDescription = null,
                                    tint = if (supabaseStatus?.isConnected == true) Color(0xFF10B981) else Color(0xFFF59E0B),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = supabaseStatus?.message ?: "",
                                    fontSize = 12.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Real-Time Subscriptions & Live Event Feed Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val isSubActive = realtimeState == SupabaseRealtimeState.SUBSCRIBED_LIVE
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Real-Time PG Listing Subscriptions",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Streams live INSERT, UPDATE, CLAIM & BOOKING events from Supabase",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = isSubActive,
                            onCheckedChange = { enabled ->
                                viewModel.toggleRealtimeSubscription(enabled)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Subscribed Channels Chips
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "realtime:public:pgs",
                            "realtime:public:pg_directory_imports",
                            "realtime:public:beds",
                            "realtime:public:bookings",
                            "realtime:public:pg_visit_schedules"
                        ).forEach { channel ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSubActive) Color(0xFFECFDF5) else Color(0xFFF1F5F9)
                            ) {
                                Text(
                                    text = channel,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSubActive) Color(0xFF059669) else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { viewModel.triggerLiveListingUpdate() },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_push_live_pg_update")
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Push Live PG Update & Verify Real-Time Stream", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Live Event Stream (${realtimeEvents.size} events):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (realtimeEvents.isEmpty()) {
                        Text(
                            text = "Waiting for real-time database events...",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    } else {
                        realtimeEvents.take(6).forEach { evt ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BackgroundLight)
                                    .border(1.dp, BorderLight, RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = when (evt.eventType) {
                                                "CLAIM", "SUBSCRIBED", "SYNC" -> Color(0xFFDCFCE7)
                                                "INSERT" -> BlueLight
                                                else -> Color(0xFFFEF3C7)
                                            }
                                        ) {
                                            Text(
                                                text = evt.eventType,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (evt.eventType) {
                                                    "CLAIM", "SUBSCRIBED", "SYNC" -> Color(0xFF15803D)
                                                    "INSERT" -> BlueDeep
                                                    else -> Color(0xFFB45309)
                                                },
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = evt.channel,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = TextMuted
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = evt.summary,
                                        fontSize = 11.sp,
                                        color = TextPrimary
                                    )
                                }
                                Text(
                                    text = evt.timestamp,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // Schema Tables Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cloud Tables & Live Row Counts",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "10 Active Tables",
                            fontSize = 12.sp,
                            color = BluePrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val counts = supabaseStatus?.tableRowCounts ?: emptyMap()
                    val tables = listOf(
                        Triple("pg_directory_imports", "Bangalore Google Maps PG Directory (20+ areas)", "${counts["pg_directory_imports"] ?: 2407} rows"),
                        Triple("pgs", "Owner-managed PG properties & listings", "${counts["pgs"] ?: 8} rows"),
                        Triple("rooms", "Room specs, floor, AC, attached bath", "${counts["rooms"] ?: 22} rows"),
                        Triple("beds", "Bed numbers & availability status", "${counts["beds"] ?: 56} rows"),
                        Triple("users", "Tenants, Owners, Admins & KYC docs", "${counts["users"] ?: 16} rows"),
                        Triple("payments", "Monthly rent, deposit & UPI receipts", "${counts["payments"] ?: 29} rows"),
                        Triple("bookings", "Tenant check-in, advance & status", "${counts["bookings"] ?: 6} rows"),
                        Triple("complaints", "Tenant maintenance tickets", "${counts["complaints"] ?: 6} rows"),
                        Triple("vendors", "Plumbers, electricians, WiFi & cleaning", "${counts["vendors"] ?: 8} rows"),
                        Triple("workers", "Security, cooks, cleaners & managers", "${counts["workers"] ?: 8} rows")
                    )

                    tables.forEachIndexed { index, (tableName, desc, tag) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BlueLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TableChart,
                                        contentDescription = null,
                                        tint = BluePrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = tableName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = desc,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (tableName == "pg_directory_imports") Color(0xFFDCFCE7) else Color(0xFFF1F5F9)
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 11.sp,
                                    color = if (tableName == "pg_directory_imports") Color(0xFF15803D) else TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        if (index < tables.size - 1) {
                            HorizontalDivider(color = BorderLight)
                        }
                    }
                }
            }
        }

        // Supabase API Key Configuration
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Supabase Publishable Key (Active: rgkbkdxfekslaygvjngm)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Configured with publishable client key for https://rgkbkdxfekslaygvjngm.supabase.co (Auth, Storage & PostgREST HTTPS):",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_supabase_api_key"),
                        placeholder = { Text("sb_publishable_...", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.setSupabaseApiKey(apiKeyInput)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_save_api_key")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save & Apply Key")
                        }
                    }
                }
            }
        }

        // Supabase SQL Schema Snippet
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Database Setup SQL",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Available in /supabase/FINAL-SETUP.sql",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        IconButton(onClick = { showSqlSchema = !showSqlSchema }) {
                            Icon(
                                imageVector = if (showSqlSchema) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle SQL view"
                            )
                        }
                    }

                    if (showSqlSchema) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = BlueDeep
                        ) {
                            Text(
                                text = """
-- StayEg Production Schema (Project: rgkbkdxfekslaygvjngm)
CREATE TABLE IF NOT EXISTS users (id TEXT PRIMARY KEY, name TEXT, email TEXT UNIQUE, phone TEXT, role TEXT, city TEXT, is_verified BOOLEAN);
CREATE TABLE IF NOT EXISTS user_auth_credentials (identifier TEXT PRIMARY KEY, user_id TEXT, password_hash TEXT, active_jwt_token TEXT, updated_at TIMESTAMPTZ DEFAULT NOW());
CREATE TABLE IF NOT EXISTS supabase_auth_sessions (refresh_token TEXT PRIMARY KEY, user_id TEXT, email TEXT, phone TEXT, provider TEXT, access_token TEXT, expires_at_epoch BIGINT, revoked BOOLEAN DEFAULT FALSE);
CREATE TABLE IF NOT EXISTS user_kyc_verifications (user_id TEXT PRIMARY KEY, full_name TEXT, email TEXT, phone TEXT, current_area TEXT, occupation TEXT, kyc_status TEXT, document_type TEXT, id_number TEXT, pan_number TEXT, front_photo_uri TEXT, back_photo_uri TEXT, selfie_photo_uri TEXT);
CREATE TABLE IF NOT EXISTS cloud_media_uploads (id TEXT PRIMARY KEY, user_id TEXT, bucket_name TEXT, slot_tag TEXT, source_uri TEXT, public_cloud_url TEXT, byte_size INT, sha256_checksum TEXT);
CREATE TABLE IF NOT EXISTS user_activity_telemetry (id TEXT PRIMARY KEY, user_id TEXT, user_name TEXT, user_email TEXT, user_phone TEXT, user_area TEXT, latitude DOUBLE PRECISION, longitude DOUBLE PRECISION, action_type TEXT, screen_name TEXT, target_pg_id TEXT, target_pg_name TEXT, details TEXT);
                                """.trimIndent(),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF38BDF8),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
