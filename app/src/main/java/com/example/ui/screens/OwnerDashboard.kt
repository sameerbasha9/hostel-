package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.BookingEntity
import com.example.data.ComplaintEntity
import com.example.data.PaymentEntity
import com.example.data.RoomEntity
import com.example.data.UserEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.HostelViewModel
import java.text.SimpleDateFormat
import java.util.*

enum class OwnerTab {
    OVERVIEW,
    ROOMS,
    TENANTS,
    BOOKINGS,
    PAYMENTS,
    COMPLAINTS,
    SETTINGS
}

@Composable
fun OwnerDashboard(
    viewModel: HostelViewModel,
    onLogout: () -> Unit
) {
    var activeTab by remember { mutableStateOf(OwnerTab.OVERVIEW) }

    // Live state collected reactively
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allRooms by viewModel.allRooms.collectAsStateWithLifecycle()
    val allBookings by viewModel.allBookings.collectAsStateWithLifecycle()
    val allPayments by viewModel.allPayments.collectAsStateWithLifecycle()
    val allComplaints by viewModel.allComplaints.collectAsStateWithLifecycle()
    val allCustomers by viewModel.allCustomers.collectAsStateWithLifecycle()
    val notifications by viewModel.userNotifications.collectAsStateWithLifecycle()

    var showNotificationsDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        bottomBar = {
            // Standard Navigation Bar (M3)
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                val navigationItems = listOf(
                    Triple(OwnerTab.OVERVIEW, Icons.Default.Dashboard, "Overview"),
                    Triple(OwnerTab.ROOMS, Icons.Default.MeetingRoom, "Rooms"),
                    Triple(OwnerTab.TENANTS, Icons.Default.People, "Tenants"),
                    Triple(OwnerTab.BOOKINGS, Icons.Default.BookOnline, "Bookings"),
                    Triple(OwnerTab.PAYMENTS, Icons.Default.Payment, "Payments"),
                    Triple(OwnerTab.COMPLAINTS, Icons.Default.ReportProblem, "Complaints")
                )
                navigationItems.forEach { (tab, icon, label) ->
                    NavigationBarItem(
                        selected = activeTab == tab,
                        onClick = { activeTab = tab },
                        icon = { Icon(imageVector = icon, contentDescription = label) },
                        label = { Text(text = label, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.testTag("nav_tab_${label.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Panel
                OwnerHeader(
                    ownerName = currentUser?.name ?: "Hostel Owner",
                    notificationsCount = notifications.count { !it.isRead },
                    onNotificationsClick = {
                        showNotificationsDialog = true
                        viewModel.clearNotifications()
                    },
                    onSettingsClick = { activeTab = OwnerTab.SETTINGS }
                )

                // Sub views based on active tab selection
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (activeTab) {
                        OwnerTab.OVERVIEW -> OwnerOverviewTab(viewModel, allRooms, allBookings, allPayments, allComplaints)
                        OwnerTab.ROOMS -> OwnerRoomsTab(viewModel, allRooms)
                        OwnerTab.TENANTS -> OwnerTenantsTab(viewModel, allCustomers, allBookings, allRooms)
                        OwnerTab.BOOKINGS -> OwnerBookingsTab(viewModel, allBookings, allCustomers, allRooms)
                        OwnerTab.PAYMENTS -> OwnerPaymentsTab(viewModel, allPayments, allBookings, allCustomers)
                        OwnerTab.COMPLAINTS -> OwnerComplaintsTab(viewModel, allComplaints, allCustomers)
                        OwnerTab.SETTINGS -> OwnerSettingsTab(viewModel, onLogout)
                    }
                }
            }

            // Notifications Overlay Dialog
            if (showNotificationsDialog) {
                AlertDialog(
                    onDismissRequest = { showNotificationsDialog = false },
                    confirmButton = {
                        TextButton(onClick = { showNotificationsDialog = false }) {
                            Text("Close", fontWeight = FontWeight.Bold)
                        }
                    },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Activity Stream", fontWeight = FontWeight.Bold)
                        }
                    },
                    text = {
                        Box(modifier = Modifier.heightIn(max = 350.dp)) {
                            if (notifications.isEmpty()) {
                                Text(
                                    "No new logs or notifications in the stream.",
                                    modifier = Modifier.padding(16.dp),
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                Column(
                                    modifier = Modifier
                                        .verticalScroll(rememberScrollState())
                                        .fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    notifications.forEach { notif ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(text = notif.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(text = notif.message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(notif.timestamp)),
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    textAlign = TextAlign.End
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun OwnerHeader(
    ownerName: String,
    notificationsCount: Int,
    onNotificationsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Director • $ownerName ★",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(ColorSuccess, shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Liberty Stay General Manager (Online)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box {
                IconButton(
                    onClick = onNotificationsClick,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), shape = CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Notifications, contentDescription = "Logs")
                }
                if (notificationsCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .background(ColorError, shape = CircleShape)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = notificationsCount.toString(),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), shape = CircleShape)
            ) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
            }
        }
    }
}

@Composable
fun OwnerOverviewTab(
    viewModel: HostelViewModel,
    rooms: List<RoomEntity>,
    bookings: List<BookingEntity>,
    payments: List<PaymentEntity>,
    complaints: List<ComplaintEntity>
) {
    val scrollState = rememberScrollState()

    // Calculations
    val totalRooms = rooms.size
    val occupiedRooms = rooms.count { it.occupiedCount > 0 }
    val vacantRooms = rooms.count { it.occupiedCount == 0 }
    val pendingComplaints = complaints.count { it.status == "PENDING" }
    val totalRevenue = payments.filter { it.status == "COMPLETED" }.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HostelSectionHeading(
            title = "Business Performance",
            subtitle = "Overview of rooms occupancy, collected rent, and support queue."
        )

        // Rent Revenue Box (Sleek Stripe Card style)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "REVENUE COLLECTED",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$${String.format(Locale.US, "%,.2f", totalRevenue)}",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = (-1).sp
                    )
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Completed Payments",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${payments.count { it.status == "COMPLETED" }} paid",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Occupancy Stats Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HostelGlassCard(modifier = Modifier.weight(1f)) {
                Icon(imageVector = Icons.Default.MeetingRoom, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Total Rooms", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                Text(text = totalRooms.toString(), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            }
            HostelGlassCard(modifier = Modifier.weight(1f)) {
                Icon(imageVector = Icons.Default.HowToReg, contentDescription = null, tint = ColorSuccess, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Occupied", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                Text(text = occupiedRooms.toString(), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = ColorSuccess)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HostelGlassCard(modifier = Modifier.weight(1f)) {
                Icon(imageVector = Icons.Default.DoorBack, contentDescription = null, tint = ColorWarning, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Vacant", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                Text(text = vacantRooms.toString(), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = ColorWarning)
            }
            HostelGlassCard(modifier = Modifier.weight(1f)) {
                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = ColorError, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Complaints", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                Text(text = pendingComplaints.toString(), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = ColorError)
            }
        }

        // Recent Bookings Feed (Compact Linear Style)
        HostelSectionHeading(
            title = "Recent Bookings Activity",
            subtitle = "Latest booking actions pending your approval."
        )

        val pendingBookings = bookings.filter { it.status == "PENDING" }
        if (pendingBookings.isEmpty()) {
            HostelGlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "No pending bookings. All set!",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            pendingBookings.take(3).forEach { booking ->
                HostelGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Room Booking Request", fontWeight = FontWeight.Bold)
                            Text(
                                text = "Amount: $${booking.totalAmount}/mo",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(ColorWarning.copy(alpha = 0.15f), shape = RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(text = "PENDING", color = ColorWarning, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OwnerRoomsTab(
    viewModel: HostelViewModel,
    rooms: List<RoomEntity>
) {
    val searchQuery by viewModel.roomSearchQuery.collectAsStateWithLifecycle()
    val typeFilter by viewModel.roomTypeFilter.collectAsStateWithLifecycle()
    val statusFilter by viewModel.roomStatusFilter.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }

    // Dialog state
    var roomNum by remember { mutableStateOf("") }
    var roomType by remember { mutableStateOf("Single") }
    var capacity by remember { mutableStateOf("1") }
    var price by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    // Filter results
    val filteredRooms = rooms.filter {
        val matchesSearch = it.roomNumber.contains(searchQuery, ignoreCase = true) || it.type.contains(searchQuery, ignoreCase = true)
        val matchesType = typeFilter == "All" || it.type.equals(typeFilter, ignoreCase = true)
        val matchesStatus = statusFilter == "All" ||
                (statusFilter == "Available" && it.occupiedCount < it.capacity) ||
                (statusFilter == "Full" && it.occupiedCount >= it.capacity)
        matchesSearch && matchesType && matchesStatus
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_room_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Room")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            HostelSectionHeading(
                title = "Hostel Rooms",
                subtitle = "Manage inventory, set pricing, and monitor active occupancy."
            )

            // Search Bar & Filters
            HostelTextField(
                value = searchQuery,
                onValueChange = { viewModel.roomSearchQuery.value = it },
                label = "Search Room",
                placeholder = "Search by Room Number or Type...",
                leadingIcon = Icons.Default.Search
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filters selector row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val types = listOf("All", "Single", "Double", "Deluxe", "Dorm")
                types.forEach { t ->
                    val selected = typeFilter == t
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.roomTypeFilter.value = t },
                        label = { Text(t) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (filteredRooms.isEmpty()) {
                HostelEmptyState(
                    title = "No Rooms Found",
                    description = "Try adjusting your search filter query or create a new room inventory using the '+' button.",
                    icon = Icons.Default.MeetingRoom
                )
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    filteredRooms.forEach { room ->
                        var isEditing by remember { mutableStateOf(false) }
                        var editPrice by remember { mutableStateOf(room.price.toString()) }
                        var editCapacity by remember { mutableStateOf(room.capacity.toString()) }

                        HostelGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Room ${room.roomNumber}",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(text = room.type, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = room.description,
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.People, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Occupancy: ${room.occupiedCount} / ${room.capacity}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "$${room.price}/mo",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row {
                                        IconButton(onClick = { isEditing = true }) {
                                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(onClick = { viewModel.deleteRoom(room) }) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = ColorError, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }

                            if (isEditing) {
                                AlertDialog(
                                    onDismissRequest = { isEditing = false },
                                    confirmButton = {
                                        Button(onClick = {
                                            val pr = editPrice.toDoubleOrNull()
                                            val cap = editCapacity.toIntOrNull()
                                            if (pr != null && cap != null) {
                                                viewModel.editRoom(room.copy(price = pr, capacity = cap))
                                                isEditing = false
                                            } else {
                                                viewModel.showToast("Invalid pricing or capacity parameters", isSuccess = false)
                                            }
                                        }) {
                                            Text("Save Changes")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { isEditing = false }) {
                                            Text("Cancel")
                                        }
                                    },
                                    title = { Text("Edit Room ${room.roomNumber}") },
                                    text = {
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            HostelTextField(value = editPrice, onValueChange = { editPrice = it }, label = "Price Per Month ($)", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                                            HostelTextField(value = editCapacity, onValueChange = { editCapacity = it }, label = "Max Capacity", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Add Room dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                confirmButton = {
                    Button(onClick = {
                        val cap = capacity.toIntOrNull() ?: 1
                        val pr = price.toDoubleOrNull()
                        if (roomNum.isBlank() || pr == null) {
                            viewModel.showToast("Please fill all parameters correctly", isSuccess = false)
                        } else {
                            viewModel.addRoom(roomNum, roomType, cap, pr, desc)
                            showAddDialog = false
                            // Reset
                            roomNum = ""
                            desc = ""
                            price = ""
                        }
                    }) {
                        Text("Add Room")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                },
                title = { Text("Create New Room Entry", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HostelTextField(value = roomNum, onValueChange = { roomNum = it }, label = "Room Number", placeholder = "e.g. 303-A")

                        // Dropdown-style selectors for type
                        Text("Room Category Type", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Single", "Double", "Deluxe", "Dorm").forEach { t ->
                                val selected = roomType == t
                                FilterChip(selected = selected, onClick = { roomType = t }, label = { Text(t) })
                            }
                        }

                        HostelTextField(value = capacity, onValueChange = { capacity = it }, label = "Max Capacity", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        HostelTextField(value = price, onValueChange = { price = it }, label = "Rent Amount ($ / mo)", placeholder = "e.g. 250", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        HostelTextField(value = desc, onValueChange = { desc = it }, label = "Room Description", placeholder = "Brief facilities summary", singleLine = false, modifier = Modifier.height(80.dp))
                    }
                }
            )
        }
    }
}

@Composable
fun OwnerTenantsTab(
    viewModel: HostelViewModel,
    customers: List<UserEntity>,
    bookings: List<BookingEntity>,
    rooms: List<RoomEntity>
) {
    val searchQuery by viewModel.customerSearchQuery.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    // Dialog Input states
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    val filteredCustomers = customers.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.email.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White) {
                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Add Customer")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            HostelSectionHeading(
                title = "Customer Directory",
                subtitle = "Search directory, add tenant profiles, or manage system users."
            )

            HostelTextField(
                value = searchQuery,
                onValueChange = { viewModel.customerSearchQuery.value = it },
                label = "Search Tenants",
                placeholder = "Search by Name or Email...",
                leadingIcon = Icons.Default.Search
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (filteredCustomers.isEmpty()) {
                HostelEmptyState(
                    title = "No Tenants Found",
                    description = "No results found matching search criteria. Use the '+' button to add manually.",
                    icon = Icons.Default.People
                )
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    filteredCustomers.forEach { customer ->
                        val activeBooking = bookings.find { it.customerId == customer.id && (it.status == "CHECKED_IN" || it.status == "APPROVED") }
                        val activeRoom = activeBooking?.let { ab -> rooms.find { it.id == ab.roomId } }

                        HostelGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = customer.name.take(1).uppercase(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = customer.name, fontWeight = FontWeight.Bold)
                                        Text(text = customer.email, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                        if (customer.phone.isNotBlank()) {
                                            Text(text = customer.phone, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                        }
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    if (activeRoom != null) {
                                        Box(
                                            modifier = Modifier
                                                .background(ColorSuccess.copy(alpha = 0.1f), shape = RoundedCornerShape(20.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(text = "Room ${activeRoom.roomNumber}", color = ColorSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), shape = RoundedCornerShape(20.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(text = "No Room Assigned", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    IconButton(onClick = { viewModel.deleteCustomer(customer) }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Tenant", tint = ColorError, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                confirmButton = {
                    Button(onClick = {
                        if (name.isBlank() || email.isBlank() || pass.isBlank()) {
                            viewModel.showToast("All fields are required", isSuccess = false)
                        } else {
                            viewModel.addCustomer(name, email, phone, pass)
                            showAddDialog = false
                            // reset
                            name = ""
                            email = ""
                            phone = ""
                            pass = ""
                        }
                    }) {
                        Text("Add Tenant")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                },
                title = { Text("Create Tenant Profile") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        HostelTextField(value = name, onValueChange = { name = it }, label = "Full Name", placeholder = "Jane Student")
                        HostelTextField(value = email, onValueChange = { email = it }, label = "Email Address", placeholder = "student@gmail.com", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
                        HostelTextField(value = phone, onValueChange = { phone = it }, label = "Phone Number", placeholder = "+1 (555) 234-4555", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                        HostelTextField(value = pass, onValueChange = { pass = it }, label = "Temporary Password", placeholder = "e.g. welcome123")
                    }
                }
            )
        }
    }
}

@Composable
fun OwnerBookingsTab(
    viewModel: HostelViewModel,
    bookings: List<BookingEntity>,
    customers: List<UserEntity>,
    rooms: List<RoomEntity>
) {
    val filterStatus by viewModel.bookingFilter.collectAsStateWithLifecycle()

    val filteredBookings = bookings.filter {
        filterStatus == "All" || it.status.equals(filterStatus, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        HostelSectionHeading(
            title = "Room Booking Pipeline",
            subtitle = "Approve requests, manage checking-in, and supervise check-outs."
        )

        // Filters list
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "PENDING", "APPROVED", "CHECKED_IN", "CHECKED_OUT", "CANCELLED", "REJECTED").forEach { status ->
                val selected = filterStatus == status
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.bookingFilter.value = status },
                    label = { Text(status) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredBookings.isEmpty()) {
            HostelEmptyState(
                title = "No Bookings Found",
                description = "There are no bookings found corresponding to the selected filter status.",
                icon = Icons.Default.BookOnline
            )
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                filteredBookings.forEach { booking ->
                    val customer = customers.find { it.id == booking.customerId }
                    val room = rooms.find { it.id == booking.roomId }

                    HostelGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = customer?.name ?: "Unknown Customer", fontWeight = FontWeight.Bold)
                                    Text(text = "Room: ${room?.roomNumber ?: "Unknown"} (${room?.type ?: "N/A"})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    Text(text = "Submitted: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(booking.createdAt))}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                }

                                val statusColor = when (booking.status) {
                                    "APPROVED" -> ColorSuccess
                                    "CHECKED_IN" -> ColorInfo
                                    "PENDING" -> ColorWarning
                                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(statusColor.copy(alpha = 0.15f), shape = RoundedCornerShape(20.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(text = booking.status, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                            Spacer(modifier = Modifier.height(10.dp))

                            // Action buttons based on status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (booking.status == "PENDING") {
                                    Button(
                                        onClick = { viewModel.updateBookingStatus(booking.id, "APPROVED") },
                                        colors = ButtonDefaults.buttonColors(containerColor = ColorSuccess),
                                        modifier = Modifier.weight(1f).height(40.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Approve", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                    }
                                    OutlinedButton(
                                        onClick = { viewModel.updateBookingStatus(booking.id, "REJECTED") },
                                        modifier = Modifier.weight(1f).height(40.dp),
                                        border = BorderStroke(1.dp, ColorError),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorError),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Reject", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                } else if (booking.status == "APPROVED") {
                                    Button(
                                        onClick = { viewModel.updateBookingStatus(booking.id, "CHECKED_IN") },
                                        colors = ButtonDefaults.buttonColors(containerColor = ColorInfo),
                                        modifier = Modifier.fillMaxWidth().height(40.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Complete Check-In", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                    }
                                } else if (booking.status == "CHECKED_IN") {
                                    Button(
                                        onClick = { viewModel.updateBookingStatus(booking.id, "CHECKED_OUT") },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        modifier = Modifier.fillMaxWidth().height(40.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Complete Check-Out", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                    }
                                } else {
                                    Text(
                                        text = "No action required",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
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

@Composable
fun OwnerPaymentsTab(
    viewModel: HostelViewModel,
    payments: List<PaymentEntity>,
    bookings: List<BookingEntity>,
    customers: List<UserEntity>
) {
    var showRequestDialog by remember { mutableStateOf(false) }

    // Dialog form states
    var selectedBookingId by remember { mutableStateOf(0) }
    var billingMonth by remember { mutableStateOf("July 2026") }
    var amount by remember { mutableStateOf("") }

    val activeTenantsBookings = bookings.filter { it.status == "CHECKED_IN" || it.status == "APPROVED" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        HostelSectionHeading(
            title = "Rent Payment Ledger",
            subtitle = "Track monthly rent collections, pending requests, and billing cycles.",
            trailing = {
                Button(
                    onClick = { showRequestDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Charge Rent", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (payments.isEmpty()) {
            HostelEmptyState(
                title = "Ledger is Empty",
                description = "No payment records are currently registered in the database ledger.",
                icon = Icons.Default.Payment
            )
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                payments.forEach { p ->
                    val customer = customers.find { it.id == p.customerId }

                    HostelGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = customer?.name ?: "Unknown Tenant", fontWeight = FontWeight.Bold)
                                Text(text = "Billing: ${p.billingMonth}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                Text(
                                    text = "Date: ${SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(p.date))}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$${p.amount}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (p.status == "COMPLETED") ColorSuccess else ColorWarning
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = (if (p.status == "COMPLETED") ColorSuccess else ColorWarning).copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = p.status,
                                        color = if (p.status == "COMPLETED") ColorSuccess else ColorWarning,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showRequestDialog) {
            AlertDialog(
                onDismissRequest = { showRequestDialog = false },
                confirmButton = {
                    Button(onClick = {
                        val amt = amount.toDoubleOrNull()
                        if (selectedBookingId == 0 || amt == null || billingMonth.isBlank()) {
                            viewModel.showToast("All fields are required", isSuccess = false)
                        } else {
                            val b = activeTenantsBookings.find { it.id == selectedBookingId }
                            if (b != null) {
                                viewModel.requestRent(b.id, b.customerId, amt, billingMonth)
                                showRequestDialog = false
                                amount = ""
                            }
                        }
                    }) {
                        Text("Charge Tenant")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRequestDialog = false }) {
                        Text("Cancel")
                    }
                },
                title = { Text("Charge Rent to Tenant", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Select Active Tenant / Booking", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))

                        // A simple scrolling selection row for testability
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            activeTenantsBookings.forEach { b ->
                                val cust = customers.find { it.id == b.customerId }
                                val isSelected = selectedBookingId == b.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedBookingId = b.id
                                        amount = b.totalAmount.toString()
                                    },
                                    label = { Text(cust?.name ?: "Booking #${b.id}") }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                        }

                        HostelTextField(value = billingMonth, onValueChange = { billingMonth = it }, label = "Billing Month", placeholder = "July 2026")
                        HostelTextField(value = amount, onValueChange = { amount = it }, label = "Rent Amount ($)", placeholder = "e.g. 250", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                }
            )
        }
    }
}

@Composable
fun OwnerComplaintsTab(
    viewModel: HostelViewModel,
    complaints: List<ComplaintEntity>,
    customers: List<UserEntity>
) {
    var showResolveDialog by remember { mutableStateOf<ComplaintEntity?>(null) }
    var resolutionNotes by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        HostelSectionHeading(
            title = "Maintenance Complaints Queue",
            subtitle = "Track filed issues, assign contractors, and write resolution summaries."
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (complaints.isEmpty()) {
            HostelEmptyState(
                title = "Complaints Queue is Empty",
                description = "Hooray! No support tickets have been submitted by the tenants.",
                icon = Icons.Default.CheckCircle
            )
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                complaints.forEach { c ->
                    val customer = customers.find { it.id == c.customerId }

                    HostelGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = c.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
                                    Text(text = "Tenant: ${customer?.name ?: "Unknown"} • Category: ${c.category}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }

                                val statusColor = if (c.status == "RESOLVED") ColorSuccess else ColorError
                                Box(
                                    modifier = Modifier
                                        .background(statusColor.copy(alpha = 0.15f), shape = RoundedCornerShape(20.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(text = c.status, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = c.description,
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                            )

                            if (c.status == "RESOLVED" && c.resolutionNotes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(ColorSuccess.copy(alpha = 0.05f), shape = RoundedCornerShape(8.dp))
                                        .border(1.dp, ColorSuccess.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "Resolution Notes: ${c.resolutionNotes}",
                                        fontSize = 12.sp,
                                        color = ColorSuccess,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            if (c.status == "PENDING") {
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { showResolveDialog = c },
                                    colors = ButtonDefaults.buttonColors(containerColor = ColorSuccess),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Mark Resolved", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showResolveDialog != null) {
            val comp = showResolveDialog!!
            AlertDialog(
                onDismissRequest = { showResolveDialog = null },
                confirmButton = {
                    Button(onClick = {
                        if (resolutionNotes.isBlank()) {
                            viewModel.showToast("Please write resolution notes", isSuccess = false)
                        } else {
                            viewModel.resolveComplaint(comp.id, resolutionNotes)
                            showResolveDialog = null
                            resolutionNotes = ""
                        }
                    }) {
                        Text("Confirm Resolution")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResolveDialog = null }) {
                        Text("Cancel")
                    }
                },
                title = { Text("Resolve Complaint: ${comp.title}") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Summarize what was fixed and how (visible to the customer):", fontSize = 13.sp)
                        HostelTextField(value = resolutionNotes, onValueChange = { resolutionNotes = it }, label = "Resolution Summary", placeholder = "e.g. Electrician replaced the circuit breaker. Fully tested.", singleLine = false, modifier = Modifier.height(100.dp))
                    }
                }
            )
        }
    }
}

@Composable
fun OwnerSettingsTab(
    viewModel: HostelViewModel,
    onLogout: () -> Unit
) {
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HostelSectionHeading(
            title = "Account Settings",
            subtitle = "Update password credentials or securely end your session."
        )

        HostelGlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("Update Credentials", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 12.dp))
            HostelTextField(value = oldPass, onValueChange = { oldPass = it }, label = "Current Password", placeholder = "••••••••", visualTransformation = PasswordVisualTransformation())
            Spacer(modifier = Modifier.height(12.dp))
            HostelTextField(value = newPass, onValueChange = { newPass = it }, label = "New Password", placeholder = "••••••••", visualTransformation = PasswordVisualTransformation())
            Spacer(modifier = Modifier.height(18.dp))
            HostelButton(
                text = "Update Password",
                onClick = {
                    if (oldPass.isBlank() || newPass.length < 6) {
                        viewModel.showToast("Password must be at least 6 characters", isSuccess = false)
                    } else {
                        viewModel.changePassword(oldPass, newPass)
                        oldPass = ""
                        newPass = ""
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = ColorError),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("owner_logout_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("End Session & Logout", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
        }
    }
}
