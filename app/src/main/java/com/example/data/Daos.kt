package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email AND isSoftDeleted = 0 LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id AND isSoftDeleted = 0")
    fun getUserByIdFlow(id: Int): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE role = 'CUSTOMER' AND isSoftDeleted = 0 ORDER BY name ASC")
    fun getAllCustomersFlow(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)
}

@Dao
interface HostelDao {
    @Query("SELECT * FROM hostels WHERE isSoftDeleted = 0 LIMIT 1")
    fun getHostelFlow(): Flow<HostelEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHostel(hostel: HostelEntity)

    @Update
    suspend fun updateHostel(hostel: HostelEntity)
}

@Dao
interface RoomDao {
    @Query("SELECT * FROM rooms WHERE isSoftDeleted = 0 ORDER BY roomNumber ASC")
    fun getAllRoomsFlow(): Flow<List<RoomEntity>>

    @Query("SELECT * FROM rooms WHERE id = :id AND isSoftDeleted = 0")
    suspend fun getRoomById(id: Int): RoomEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: RoomEntity)

    @Update
    suspend fun updateRoom(room: RoomEntity)

    @Delete
    suspend fun deleteRoom(room: RoomEntity)
}

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings WHERE isSoftDeleted = 0 ORDER BY createdAt DESC")
    fun getAllBookingsFlow(): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE customerId = :customerId AND isSoftDeleted = 0 ORDER BY createdAt DESC")
    fun getBookingsByCustomerFlow(customerId: Int): Flow<List<BookingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity): Long

    @Update
    suspend fun updateBooking(booking: BookingEntity)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments WHERE isSoftDeleted = 0 ORDER BY date DESC")
    fun getAllPaymentsFlow(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE customerId = :customerId AND isSoftDeleted = 0 ORDER BY date DESC")
    fun getPaymentsByCustomerFlow(customerId: Int): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Update
    suspend fun updatePayment(payment: PaymentEntity)
}

@Dao
interface ComplaintDao {
    @Query("SELECT * FROM complaints WHERE isSoftDeleted = 0 ORDER BY date DESC")
    fun getAllComplaintsFlow(): Flow<List<ComplaintEntity>>

    @Query("SELECT * FROM complaints WHERE customerId = :customerId AND isSoftDeleted = 0 ORDER BY date DESC")
    fun getComplaintsByCustomerFlow(customerId: Int): Flow<List<ComplaintEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaint(complaint: ComplaintEntity)

    @Update
    suspend fun updateComplaint(complaint: ComplaintEntity)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId AND isSoftDeleted = 0 ORDER BY timestamp DESC")
    fun getNotificationsForUserFlow(userId: Int): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: Int)
}
