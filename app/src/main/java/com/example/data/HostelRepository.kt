package com.example.data

import kotlinx.coroutines.flow.Flow

class HostelRepository(private val database: AppDatabase) {

    private val userDao = database.userDao()
    private val hostelDao = database.hostelDao()
    private val roomDao = database.roomDao()
    private val bookingDao = database.bookingDao()
    private val paymentDao = database.paymentDao()
    private val complaintDao = database.complaintDao()
    private val notificationDao = database.notificationDao()

    // Users
    suspend fun getUserByEmail(email: String): UserEntity? = userDao.getUserByEmail(email)
    fun getUserByIdFlow(id: Int): Flow<UserEntity?> = userDao.getUserByIdFlow(id)
    val allCustomers: Flow<List<UserEntity>> = userDao.getAllCustomersFlow()
    suspend fun insertUser(user: UserEntity): Long = userDao.insertUser(user)
    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)
    suspend fun deleteUser(user: UserEntity) = userDao.deleteUser(user)

    // Hostel Info
    val hostelInfo: Flow<HostelEntity?> = hostelDao.getHostelFlow()
    suspend fun insertHostel(hostel: HostelEntity) = hostelDao.insertHostel(hostel)
    suspend fun updateHostel(hostel: HostelEntity) = hostelDao.updateHostel(hostel)

    // Rooms
    val allRooms: Flow<List<RoomEntity>> = roomDao.getAllRoomsFlow()
    suspend fun getRoomById(id: Int): RoomEntity? = roomDao.getRoomById(id)
    suspend fun insertRoom(room: RoomEntity) = roomDao.insertRoom(room)
    suspend fun updateRoom(room: RoomEntity) = roomDao.updateRoom(room)
    suspend fun deleteRoom(room: RoomEntity) = roomDao.deleteRoom(room)

    // Bookings
    val allBookings: Flow<List<BookingEntity>> = bookingDao.getAllBookingsFlow()
    fun getBookingsByCustomer(customerId: Int): Flow<List<BookingEntity>> = bookingDao.getBookingsByCustomerFlow(customerId)
    suspend fun insertBooking(booking: BookingEntity): Long = bookingDao.insertBooking(booking)
    suspend fun updateBooking(booking: BookingEntity) = bookingDao.updateBooking(booking)

    // Payments
    val allPayments: Flow<List<PaymentEntity>> = paymentDao.getAllPaymentsFlow()
    fun getPaymentsByCustomer(customerId: Int): Flow<List<PaymentEntity>> = paymentDao.getPaymentsByCustomerFlow(customerId)
    suspend fun insertPayment(payment: PaymentEntity): Long = paymentDao.insertPayment(payment)
    suspend fun updatePayment(payment: PaymentEntity) = paymentDao.updatePayment(payment)

    // Complaints
    val allComplaints: Flow<List<ComplaintEntity>> = complaintDao.getAllComplaintsFlow()
    fun getComplaintsByCustomer(customerId: Int): Flow<List<ComplaintEntity>> = complaintDao.getComplaintsByCustomerFlow(customerId)
    suspend fun insertComplaint(complaint: ComplaintEntity) = complaintDao.insertComplaint(complaint)
    suspend fun updateComplaint(complaint: ComplaintEntity) = complaintDao.updateComplaint(complaint)

    // Notifications
    fun getNotificationsForUser(userId: Int): Flow<List<NotificationEntity>> = notificationDao.getNotificationsForUserFlow(userId)
    suspend fun insertNotification(notification: NotificationEntity) = notificationDao.insertNotification(notification)
    suspend fun markNotificationsAsRead(userId: Int) = notificationDao.markAllAsRead(userId)
}
