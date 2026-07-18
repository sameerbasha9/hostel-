package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class Screen {
    LANDING,
    LOGIN,
    REGISTER,
    OWNER_DASHBOARD,
    CUSTOMER_DASHBOARD
}

class HostelViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DatabaseLocator.getRepository(application)

    // Active User State
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Screen Navigation State
    private val _currentScreen = MutableStateFlow(Screen.LANDING)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Screen Navigation Stack for Back Button
    private val navigationHistory = mutableListOf<Screen>()

    // Notification / Toast Message State
    private val _toastState = MutableStateFlow<Pair<String, Boolean>?>(null)
    val toastState: StateFlow<Pair<String, Boolean>?> = _toastState.asStateFlow()

    // Live Database Flows
    val hostelInfo: StateFlow<HostelEntity?> = repository.hostelInfo
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allRooms: StateFlow<List<RoomEntity>> = repository.allRooms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBookings: StateFlow<List<BookingEntity>> = repository.allBookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPayments: StateFlow<List<PaymentEntity>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allComplaints: StateFlow<List<ComplaintEntity>> = repository.allComplaints
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCustomers: StateFlow<List<UserEntity>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notifications Flow for current logged-in user
    val userNotifications: StateFlow<List<NotificationEntity>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) repository.getNotificationsForUser(user.id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search and Filter States for dashboards
    val roomSearchQuery = MutableStateFlow("")
    val roomTypeFilter = MutableStateFlow("All") // "All", "Single", "Double", "Deluxe", "Dorm"
    val roomStatusFilter = MutableStateFlow("All") // "All", "Available", "Full"

    val customerSearchQuery = MutableStateFlow("")

    val bookingFilter = MutableStateFlow("All") // "All", "PENDING", "APPROVED", "REJECTED", "CHECKED_IN", "CHECKED_OUT"

    val paymentFilter = MutableStateFlow("All") // "All", "PENDING", "COMPLETED", "FAILED"

    // Toast triggers
    fun showToast(message: String, isSuccess: Boolean = true) {
        _toastState.value = Pair(message, isSuccess)
    }

    fun dismissToast() {
        _toastState.value = null
    }

    // Navigation Methods
    fun navigateTo(screen: Screen) {
        navigationHistory.add(_currentScreen.value)
        _currentScreen.value = screen
    }

    fun navigateBack() {
        if (navigationHistory.isNotEmpty()) {
            _currentScreen.value = navigationHistory.removeAt(navigationHistory.size - 1)
        } else {
            _currentScreen.value = Screen.LANDING
        }
    }

    // Auth Actions
    fun login(email: String, passwordCheck: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserByEmail(email)
            if (user != null && user.passwordHash == passwordCheck) {
                _currentUser.value = user
                showToast("Welcome back, ${user.name}!", isSuccess = true)
                if (user.role == "OWNER") {
                    navigateTo(Screen.OWNER_DASHBOARD)
                } else {
                    navigateTo(Screen.CUSTOMER_DASHBOARD)
                }
            } else {
                showToast("Invalid email or password", isSuccess = false)
            }
        }
    }

    fun register(name: String, email: String, passwordCheck: String, role: String, phone: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.getUserByEmail(email)
            if (existing != null) {
                showToast("Account with this email already exists", isSuccess = false)
                return@launch
            }

            val newUser = UserEntity(
                name = name,
                email = email,
                passwordHash = passwordCheck,
                role = role,
                phone = phone
            )
            val newId = repository.insertUser(newUser).toInt()
            val createdUser = newUser.copy(id = newId)

            _currentUser.value = createdUser
            showToast("Account created successfully!", isSuccess = true)

            // Auto trigger welcome notification
            repository.insertNotification(
                NotificationEntity(
                    userId = newId,
                    title = "Account Setup Complete",
                    message = "Welcome to Hostel Manager, $name! Explore available rooms and make your booking today."
                )
            )

            if (role == "OWNER") {
                navigateTo(Screen.OWNER_DASHBOARD)
            } else {
                navigateTo(Screen.CUSTOMER_DASHBOARD)
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        navigationHistory.clear()
        _currentScreen.value = Screen.LANDING
        showToast("Logged out successfully", isSuccess = true)
    }

    fun changePassword(oldPass: String, newPass: String) {
        val user = _currentUser.value ?: return
        if (user.passwordHash != oldPass) {
            showToast("Incorrect current password", isSuccess = false)
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val updatedUser = user.copy(passwordHash = newPass)
            repository.updateUser(updatedUser)
            _currentUser.value = updatedUser
            showToast("Password changed successfully", isSuccess = true)
        }
    }

    fun updateProfile(name: String, phone: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val updatedUser = user.copy(name = name, phone = phone)
            repository.updateUser(updatedUser)
            _currentUser.value = updatedUser
            showToast("Profile updated successfully", isSuccess = true)
        }
    }

    // Room Management (Owner)
    fun addRoom(roomNumber: String, type: String, capacity: Int, price: Double, description: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newRoom = RoomEntity(
                hostelId = 1,
                roomNumber = roomNumber,
                type = type,
                capacity = capacity,
                price = price,
                description = description
            )
            repository.insertRoom(newRoom)
            showToast("Room $roomNumber added successfully!", isSuccess = true)
        }
    }

    fun editRoom(room: RoomEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateRoom(room)
            showToast("Room ${room.roomNumber} updated successfully!", isSuccess = true)
        }
    }

    fun deleteRoom(room: RoomEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateRoom(room.copy(isSoftDeleted = true))
            showToast("Room ${room.roomNumber} deleted", isSuccess = true)
        }
    }

    // Customer Management (Owner Add/Edit/Delete Customers)
    fun addCustomer(name: String, email: String, phone: String, passwordCheck: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.getUserByEmail(email)
            if (existing != null) {
                showToast("User with this email already exists", isSuccess = false)
                return@launch
            }
            repository.insertUser(
                UserEntity(
                    name = name,
                    email = email,
                    passwordHash = passwordCheck,
                    role = "CUSTOMER",
                    phone = phone
                )
            )
            showToast("Customer $name added successfully!", isSuccess = true)
        }
    }

    fun editCustomer(user: UserEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUser(user)
            showToast("Customer ${user.name} updated", isSuccess = true)
        }
    }

    fun deleteCustomer(user: UserEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUser(user.copy(isSoftDeleted = true))
            showToast("Customer ${user.name} deleted", isSuccess = true)
        }
    }

    // Booking Actions (Both Customer & Owner)
    fun createBooking(roomId: Int, startDate: Long, endDate: Long, amountPerMonth: Double) {
        val user = _currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val room = repository.getRoomById(roomId) ?: return@launch
            if (room.occupiedCount >= room.capacity) {
                showToast("This room is already fully occupied", isSuccess = false)
                return@launch
            }

            // Standard duration calculation or flat rate
            val bookingId = repository.insertBooking(
                BookingEntity(
                    customerId = user.id,
                    roomId = roomId,
                    startDate = startDate,
                    endDate = endDate,
                    status = "PENDING",
                    totalAmount = amountPerMonth
                )
            ).toInt()

            // Trigger owner notification
            repository.insertNotification(
                NotificationEntity(
                    userId = 1, // Default owner ID
                    title = "New Booking Request",
                    message = "${user.name} requested to book Room ${room.roomNumber} (${room.type})."
                )
            )

            // Trigger customer notification
            repository.insertNotification(
                NotificationEntity(
                    userId = user.id,
                    title = "Booking Request Sent",
                    message = "Your booking request for Room ${room.roomNumber} has been submitted. Status: Pending Approval."
                )
            )

            showToast("Booking request submitted!", isSuccess = true)
        }
    }

    fun updateBookingStatus(bookingId: Int, newStatus: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val bookings = allBookings.value
            val booking = bookings.find { it.id == bookingId } ?: return@launch
            val room = repository.getRoomById(booking.roomId) ?: return@launch

            var updatedRoom = room
            if (newStatus == "APPROVED") {
                // Generate a pending payment for the tenant
                repository.insertPayment(
                    PaymentEntity(
                        bookingId = bookingId,
                        customerId = booking.customerId,
                        amount = booking.totalAmount,
                        billingMonth = "Current Month",
                        status = "PENDING",
                        paymentMethod = "Card"
                    )
                )
                repository.insertNotification(
                    NotificationEntity(
                        userId = booking.customerId,
                        title = "Booking Approved!",
                        message = "Your booking for Room ${room.roomNumber} is approved. Please complete your rent payment."
                    )
                )
            } else if (newStatus == "CHECKED_IN") {
                // Occupied count increases
                updatedRoom = room.copy(occupiedCount = (room.occupiedCount + 1).coerceAtMost(room.capacity))
                repository.insertNotification(
                    NotificationEntity(
                        userId = booking.customerId,
                        title = "Checked In",
                        message = "You have successfully checked in to Room ${room.roomNumber}. Welcome home!"
                    )
                )
            } else if (newStatus == "CHECKED_OUT" || newStatus == "CANCELLED" || newStatus == "REJECTED") {
                // If checking out or cancelling, reduce occupied count if they were checked in
                if (booking.status == "CHECKED_IN" || booking.status == "APPROVED") {
                    updatedRoom = room.copy(occupiedCount = (room.occupiedCount - 1).coerceAtLeast(0))
                }
                repository.insertNotification(
                    NotificationEntity(
                        userId = booking.customerId,
                        title = "Booking Update",
                        message = "Your booking status for Room ${room.roomNumber} is now: $newStatus."
                    )
                )
            }

            repository.updateRoom(updatedRoom)
            repository.updateBooking(booking.copy(status = newStatus))
            showToast("Booking updated to $newStatus", isSuccess = true)
        }
    }

    // Payment Actions (Rent tracker, Pay Rent)
    fun makePayment(paymentId: Int, paymentMethod: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val payments = allPayments.value
            val payment = payments.find { it.id == paymentId } ?: return@launch
            val updated = payment.copy(status = "COMPLETED", paymentMethod = paymentMethod, date = System.currentTimeMillis())
            repository.updatePayment(updated)

            // Trigger notification
            repository.insertNotification(
                NotificationEntity(
                    userId = payment.customerId,
                    title = "Payment Successful",
                    message = "Your payment of $${payment.amount} for ${payment.billingMonth} is received."
                )
            )

            repository.insertNotification(
                NotificationEntity(
                    userId = 1, // Owner
                    title = "Rent Payment Received",
                    message = "Received rent payment of $${payment.amount} for ${payment.billingMonth}."
                )
            )

            showToast("Payment processed successfully!", isSuccess = true)
        }
    }

    fun requestRent(bookingId: Int, customerId: Int, amount: Double, billingMonth: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertPayment(
                PaymentEntity(
                    bookingId = bookingId,
                    customerId = customerId,
                    amount = amount,
                    billingMonth = billingMonth,
                    status = "PENDING"
                )
            )
            repository.insertNotification(
                NotificationEntity(
                    userId = customerId,
                    title = "Rent Payment Requested",
                    message = "Rent of $${amount} has been requested for $billingMonth."
                )
            )
            showToast("Rent requested successfully!", isSuccess = true)
        }
    }

    // Complaints Actions (Customer Raise, Owner Resolve)
    fun raiseComplaint(title: String, description: String, category: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val newComplaint = ComplaintEntity(
                customerId = user.id,
                title = title,
                description = description,
                category = category,
                status = "PENDING"
            )
            repository.insertComplaint(newComplaint)

            // Notify owner
            repository.insertNotification(
                NotificationEntity(
                    userId = 1,
                    title = "New Complaint Filed",
                    message = "${user.name} filed a complaint: $title."
                )
            )

            showToast("Complaint registered successfully", isSuccess = true)
        }
    }

    fun resolveComplaint(complaintId: Int, notes: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val complaints = allComplaints.value
            val complaint = complaints.find { it.id == complaintId } ?: return@launch
            val updated = complaint.copy(status = "RESOLVED", resolutionNotes = notes)
            repository.updateComplaint(updated)

            // Notify customer
            repository.insertNotification(
                NotificationEntity(
                    userId = complaint.customerId,
                    title = "Complaint Resolved!",
                    message = "Your complaint regarding '${complaint.title}' is marked resolved. Notes: $notes"
                )
            )
            showToast("Complaint resolved successfully!", isSuccess = true)
        }
    }

    fun clearNotifications() {
        val user = _currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.markNotificationsAsRead(user.id)
        }
    }
}
