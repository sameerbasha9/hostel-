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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.HostelViewModel
import java.text.SimpleDateFormat
import java.util.*

enum class CustomerTab {
    EXPLORE,
    MY_STAY,
    BILLING,
    COMPLAINTS,
    PROFILE
}

@Composable
fun CustomerDashboard(
    viewModel: HostelViewModel,
    onLogout: () -> Unit
) {
    var activeTab by remember { mutableStateOf(CustomerTab.EXPLORE) }

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allRooms by viewModel.allRooms.collectAsStateWithLifecycle()
    val bookings by viewModel.allBookings.collectAsStateWithLifecycle()
    val payments by viewModel.allPayments.collectAsStateWithLifecycle()
    val complaints by viewModel.allComplaints.collectAsStateWithLifecycle()
    val notifications by viewModel.userNotifications.collectAsStateWithLifecycle()

    var showNotificationsDialog by remember { mutableStateOf(false) }

    // Derive active user data safely
    val customerBookings = bookings.filter { it.customerId == (currentUser?.id ?: 0) }
    val activeBooking = customerBookings.find { it.status != "CANCELLED" && it.status != "REJECTED" && it.status != "CHECKED_OUT" }
    val customerPayments = payments.filter { it.customerId == (currentUser?.id ?: 0) }
    val customerComplaints = complaints.filter { it.customerId == (currentUser?.id ?: 0) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                val tabs = listOf(
                    Triple(CustomerTab.EXPLORE, Icons.Default.Explore, "Explore"),
                    Triple(CustomerTab.MY_STAY, Icons.Default.Home, "My Stay"),
                    Triple(CustomerTab.BILLING, Icons.Default.Receipt, "Billing"),
                    Triple(CustomerTab.COMPLAINTS, Icons.Default.Chat, "Complaints"),
                    Triple(CustomerTab.PROFILE, Icons.Default.Person, "Profile")
                )
                tabs.forEach { (tab, icon, label) ->
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
                        modifier = Modifier.testTag("customer_nav_${label.lowercase()}")
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
                // Header
                CustomerHeader(
                    customerName = currentUser?.name ?: "Student Resident",
                    unreadNotificationsCount = notifications.count { !it.isRead },
                    onNotificationsClick = {
                        showNotificationsDialog = true
                        viewModel.clearNotifications()
                    }
                )

                // Tab screens
                Box(modifier = Modifier.weight(1f)) {
                    when (activeTab) {
                        CustomerTab.EXPLORE -> CustomerExploreTab(viewModel, allRooms, activeBooking)
                        CustomerTab.MY_STAY -> CustomerMyStayTab(viewModel, activeBooking, allRooms)
                        CustomerTab.BILLING -> CustomerBillingTab(viewModel, customerPayments)
                        CustomerTab.COMPLAINTS -> CustomerComplaintsTab(viewModel, customerComplaints)
                        CustomerTab.PROFILE -> CustomerProfileTab(viewModel, currentUser, onLogout)
                    }
                }
            }

            // Notifications Dialog
            if (showNotificationsDialog) {
                AlertDialog(
                    onDismissRequest = { showNotificationsDialog = false },
                    confirmButton = {
                        TextButton(onClick = { showNotificationsDialog = false }) {
                            Text("Dismiss", fontWeight = FontWeight.Bold)
                        }
                    },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Notifications Inbox", fontWeight = FontWeight.Bold)
                        }
                    },
                    text = {
                        Box(modifier = Modifier.heightIn(max = 350.dp)) {
                            if (notifications.isEmpty()) {
                                Text("No recent messages or notifications found.", modifier = Modifier.padding(16.dp))
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
fun CustomerHeader(
    customerName: String,
    unreadNotificationsCount: Int,
    onNotificationsClick: () -> Unit
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
                text = "Welcome back, $customerName 👋",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
            )
            Text(
                text = "Liberty Stay Resident • Premium Member ★",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold
            )
        }

        Box {
            IconButton(
                onClick = onNotificationsClick,
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), shape = CircleShape)
            ) {
                Icon(imageVector = Icons.Default.Notifications, contentDescription = "Logs")
            }
            if (unreadNotificationsCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(ColorError, shape = CircleShape)
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = unreadNotificationsCount.toString(),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
fun CustomerExploreTab(
    viewModel: HostelViewModel,
    rooms: List<RoomEntity>,
    activeBooking: BookingEntity?
) {
    var showBookingDialog by remember { mutableStateOf<RoomEntity?>(null) }
    val availableRooms = rooms.filter { it.occupiedCount < it.capacity }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        HostelSectionHeading(
            title = "Explore Accommodations",
            subtitle = "View current vacant rooms and submit an instant booking request."
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (activeBooking != null) {
            // Friendly restriction card explaining they already have a stay
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "🔒 Active Stay Found",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "You already have a stay active or booking request pending. You can manage your booking in the 'My Stay' tab.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        if (availableRooms.isEmpty()) {
            HostelEmptyState(
                title = "All Rooms Full",
                description = "Wow! All rooms in the building are currently fully occupied. Please check back later.",
                icon = Icons.Default.SentimentVeryDissatisfied
            )
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                availableRooms.forEach { room ->
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
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = room.description,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.60f),
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Slots Available: ${(room.capacity - room.occupiedCount)} of ${room.capacity}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { showBookingDialog = room },
                                    enabled = activeBooking == null,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Book", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showBookingDialog != null) {
            val room = showBookingDialog!!
            var termsAccepted by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showBookingDialog = null },
                confirmButton = {
                    Button(
                        onClick = {
                            if (termsAccepted) {
                                // Default range is 1 month from now
                                val start = System.currentTimeMillis()
                                val end = start + 30 * 24 * 3600 * 1000L
                                viewModel.createBooking(room.id, start, end, room.price)
                                showBookingDialog = null
                            } else {
                                viewModel.showToast("Please accept agreement terms", isSuccess = false)
                            }
                        },
                        enabled = termsAccepted
                    ) {
                        Text("Submit Request")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBookingDialog = null }) {
                        Text("Cancel")
                    }
                },
                title = { Text("Book Room ${room.roomNumber}") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Confirm stay parameters for Room ${room.roomNumber} (${room.type}):", fontSize = 13.sp)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = "• Monthly Rent: $${room.price}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = "• Duration: 1 Month (Auto-Renewable)", fontSize = 12.sp)
                                Text(text = "• Refundable Deposit: $0 (Promotional)", fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { termsAccepted = !termsAccepted }
                        ) {
                            Checkbox(checked = termsAccepted, onCheckedChange = { termsAccepted = it })
                            Text(text = "I accept the tenant code of conduct agreement and payment responsibilities.", fontSize = 11.sp, lineHeight = 14.sp)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun CustomerMyStayTab(
    viewModel: HostelViewModel,
    activeBooking: BookingEntity?,
    rooms: List<RoomEntity>
) {
    if (activeBooking == null) {
        HostelEmptyState(
            title = "No Active Stay",
            description = "You do not have any room stay active or booking requests submitted yet. Check out the 'Explore' tab to find rooms!",
            icon = Icons.Default.Weekend
        )
    } else {
        val room = rooms.find { it.id == activeBooking.roomId }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HostelSectionHeading(
                title = "My Room Stay Details",
                subtitle = "Manage your current active stay details, rules, or requests."
            )

            HostelGlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Room ${room?.roomNumber ?: "Pending"}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                        )
                        Text(text = "Type: ${room?.type ?: "N/A"}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }

                    val statusColor = when (activeBooking.status) {
                        "CHECKED_IN" -> ColorSuccess
                        "APPROVED" -> ColorInfo
                        else -> ColorWarning
                    }
                    Box(
                        modifier = Modifier
                            .background(statusColor.copy(alpha = 0.15f), shape = RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(text = activeBooking.status, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Check-in Date", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        Text(text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(activeBooking.startDate)), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Monthly Price", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        Text(text = "$${activeBooking.totalAmount}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Facilities Quick List Card
            HostelGlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Quick Room Rules:", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "• Quiet hours are enforced after 10:00 PM daily.\n• Visitors are allowed in study lounges only.\n• Maintain high standards of room cleanliness.", fontSize = 12.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
            }

            Spacer(modifier = Modifier.weight(1f))

            if (activeBooking.status == "PENDING") {
                Button(
                    onClick = { viewModel.updateBookingStatus(activeBooking.id, "CANCELLED") },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorError),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Cancel Booking Request", fontWeight = FontWeight.Bold)
                }
            } else if (activeBooking.status == "APPROVED" || activeBooking.status == "CHECKED_IN") {
                OutlinedButton(
                    onClick = { viewModel.updateBookingStatus(activeBooking.id, "CHECKED_OUT") },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorError),
                    border = BorderStroke(1.2.dp, ColorError),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Request Move-Out & Check-Out", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CustomerBillingTab(
    viewModel: HostelViewModel,
    payments: List<PaymentEntity>
) {
    var showPayDialog by remember { mutableStateOf<PaymentEntity?>(null) }
    var paymentMethodSelected by remember { mutableStateOf("Card") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        HostelSectionHeading(
            title = "My Rent Ledger",
            subtitle = "Settle pending rent statements and view historic success bills."
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (payments.isEmpty()) {
            HostelEmptyState(
                title = "No Bills Registered",
                description = "Hooray! No billing statements or rent entries are currently requested for your profile.",
                icon = Icons.Default.ReceiptLong
            )
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                payments.forEach { p ->
                    HostelGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Rent for ${p.billingMonth}", fontWeight = FontWeight.Bold)
                                Text(text = "Method: ${p.paymentMethod}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                Text(
                                    text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(p.date)),
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
                                Spacer(modifier = Modifier.height(6.dp))

                                if (p.status == "PENDING") {
                                    Button(
                                        onClick = { showPayDialog = p },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ColorSuccess),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Pay Now", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .background(ColorSuccess.copy(alpha = 0.15f), shape = RoundedCornerShape(20.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("COMPLETED", color = ColorSuccess, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showPayDialog != null) {
            val payment = showPayDialog!!
            AlertDialog(
                onDismissRequest = { showPayDialog = null },
                confirmButton = {
                    Button(onClick = {
                        viewModel.makePayment(payment.id, paymentMethodSelected)
                        showPayDialog = null
                    }) {
                        Text("Process Payment")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPayDialog = null }) {
                        Text("Cancel")
                    }
                },
                title = { Text("Complete Rent Payment") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Pay statement sum of $${payment.amount} for ${payment.billingMonth}:", fontSize = 13.sp)

                        // Mode selection
                        listOf("Card", "Bank Transfer", "UPI").forEach { method ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { paymentMethodSelected = method }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = paymentMethodSelected == method, onClick = { paymentMethodSelected = method })
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = method, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun CustomerComplaintsTab(
    viewModel: HostelViewModel,
    complaints: List<ComplaintEntity>
) {
    var showRaiseDialog by remember { mutableStateOf(false) }

    // Form inputs
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Wi-Fi") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        HostelSectionHeading(
            title = "Maintenance Helpdesk",
            subtitle = "Submit issue reports or track progress on active complaints.",
            trailing = {
                Button(onClick = { showRaiseDialog = true }, shape = RoundedCornerShape(10.dp)) {
                    Text("New Ticket", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (complaints.isEmpty()) {
            HostelEmptyState(
                title = "Support is Clear",
                description = "No active maintenance complaints or tickets registered for your stay.",
                icon = Icons.Default.TaskAlt
            )
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                complaints.forEach { c ->
                    HostelGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = c.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
                                    Text(text = "Category: ${c.category}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
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
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
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
                        }
                    }
                }
            }
        }

        if (showRaiseDialog) {
            AlertDialog(
                onDismissRequest = { showRaiseDialog = false },
                confirmButton = {
                    Button(onClick = {
                        if (title.isBlank() || description.isBlank()) {
                            viewModel.showToast("Please fill all details", isSuccess = false)
                        } else {
                            viewModel.raiseComplaint(title, description, category)
                            showRaiseDialog = false
                            title = ""
                            description = ""
                        }
                    }) {
                        Text("Submit Ticket")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRaiseDialog = false }) {
                        Text("Cancel")
                    }
                },
                title = { Text("Raise Support Ticket", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        HostelTextField(value = title, onValueChange = { title = it }, label = "Issue Title", placeholder = "e.g. Wi-Fi broken")

                        Text("Select Issue Category", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Wi-Fi", "Plumbing", "Electrical", "Food", "Cleaning", "Other").forEach { cat ->
                                val selected = category == cat
                                FilterChip(selected = selected, onClick = { category = cat }, label = { Text(cat) })
                            }
                        }

                        HostelTextField(value = description, onValueChange = { description = it }, label = "Issue Description", placeholder = "Summarize the issue clearly...", singleLine = false, modifier = Modifier.height(100.dp))
                    }
                }
            )
        }
    }
}

@Composable
fun CustomerProfileTab(
    viewModel: HostelViewModel,
    user: UserEntity?,
    onLogout: () -> Unit
) {
    var editName by remember { mutableStateOf(user?.name ?: "") }
    var editPhone by remember { mutableStateOf(user?.phone ?: "") }

    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HostelSectionHeading(
            title = "Profile Settings",
            subtitle = "Update details, credentials, or log out of your session."
        )

        HostelGlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("Update Personal Details", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 12.dp))
            HostelTextField(value = editName, onValueChange = { editName = it }, label = "Full Name", placeholder = "Jane Doe", leadingIcon = Icons.Default.Person)
            Spacer(modifier = Modifier.height(12.dp))
            HostelTextField(value = editPhone, onValueChange = { editPhone = it }, label = "Phone Number", placeholder = "+1 (555) 123-4567", leadingIcon = Icons.Default.Phone, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
            Spacer(modifier = Modifier.height(18.dp))
            HostelButton(
                text = "Save Profile",
                onClick = {
                    if (editName.isBlank()) {
                        viewModel.showToast("Name cannot be empty", isSuccess = false)
                    } else {
                        viewModel.updateProfile(editName, editPhone)
                    }
                }
            )
        }

        HostelGlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("Update Credentials", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 12.dp))
            HostelTextField(value = oldPass, onValueChange = { oldPass = it }, label = "Current Password", placeholder = "••••••••", visualTransformation = PasswordVisualTransformation())
            Spacer(modifier = Modifier.height(12.dp))
            HostelTextField(value = newPass, onValueChange = { newPass = it }, label = "New Password", placeholder = "••••••••", visualTransformation = PasswordVisualTransformation())
            Spacer(modifier = Modifier.height(18.dp))
            HostelButton(
                text = "Change Password",
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
                .testTag("customer_logout_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout from Device", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
        }
    }
}
