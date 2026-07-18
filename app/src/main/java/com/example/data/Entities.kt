package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val passwordHash: String,
    val role: String, // "OWNER", "CUSTOMER"
    val phone: String = "",
    val avatarUrl: String = "",
    val isSoftDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "hostels")
data class HostelEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val address: String,
    val description: String,
    val facilities: String, // Wi-Fi, AC, Laundry, Gym, Food, CCTV
    val priceRange: String = "$150 - $400/mo",
    val imageUrl: String = "",
    val isSoftDeleted: Boolean = false
)

@Entity(tableName = "rooms")
data class RoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hostelId: Int,
    val roomNumber: String,
    val capacity: Int,
    val occupiedCount: Int = 0,
    val price: Double,
    val type: String, // "Single", "Double", "Deluxe", "Dorm"
    val description: String = "",
    val isAvailable: Boolean = true,
    val isSoftDeleted: Boolean = false
)

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerId: Int,
    val roomId: Int,
    val startDate: Long,
    val endDate: Long,
    val status: String, // "PENDING", "APPROVED", "REJECTED", "CHECKED_IN", "CHECKED_OUT"
    val totalAmount: Double,
    val isSoftDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookingId: Int,
    val customerId: Int,
    val amount: Double,
    val billingMonth: String, // e.g. "July 2026"
    val date: Long = System.currentTimeMillis(),
    val status: String, // "PENDING", "COMPLETED", "FAILED"
    val paymentMethod: String = "Card",
    val isSoftDeleted: Boolean = false
)

@Entity(tableName = "complaints")
data class ComplaintEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerId: Int,
    val title: String,
    val description: String,
    val category: String, // Plumbing, Electrical, Wi-Fi, Food, Cleaning
    val status: String, // "PENDING", "RESOLVED"
    val date: Long = System.currentTimeMillis(),
    val resolutionNotes: String = "",
    val isSoftDeleted: Boolean = false
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isSoftDeleted: Boolean = false
)
